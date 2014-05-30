package com.architexa.diagrams.jdt.builder;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.openrdf.model.Resource;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.asm.DepAndChildrenStrengthSummarizer;
import com.architexa.diagrams.jdt.builder.asm.InferOverridesBuildProcessor;
import com.architexa.diagrams.jdt.builder.asm.ResolveBrokenReferencesBuildProcessor;
import com.architexa.diagrams.jdt.services.PluggableBuildProcessor;
import com.architexa.diagrams.model.Artifact;
import com.architexa.rse.RSE;
import com.architexa.store.PckgDirRepo;
import com.architexa.store.ReloRdfRepository;


/**
 * Common base class for the build visitors
 * 
 * @author vineet
 */
public class AtxaBuildVisitor {
    static final Logger logger = Activator.getLogger(AtxaBuildVisitor.class);
    static final Logger buildPerflogger = Activator.getLogger(AtxaBuildVisitor.class, "BuildPerf" );
	private static boolean debug = false;

    protected IProgressMonitor monitor;
    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
        monitor.beginTask("Parsing for " + RSE.appName, taskSize);
    }
	public void updateTask(String taskStr) {
		// we artificially show more progress since users would rather see a wait on 100/100 than 0/100
		if (monitor.isCanceled()) return;
		monitor.subTask(new StringBuffer("[")
								.append(taskDone+1)
								.append("/")
								.append(ResourceQueue.getTotalTaskSizeFromCachedQueue())
								.append("] ")
								.append(taskStr)
//								.append(" for " + RSE.appName + " ")
								.toString());
	}

    protected int taskSize;
    protected int taskDone;
    protected int taskPrev;
    public int getTaskSize() {
        return taskSize;
    }
    
    public void setTaskSize(int taskSize, String projName) {
    	taskPrev = this.taskSize;
        this.taskSize = taskSize;
        monitor.beginTask("Parsing Project: " + projName, ResourceQueue.getTotalTaskSizeFromCachedQueue() * 2);
        monitor.worked(this.taskDone * 2);
    }
	//public void resetTaskDone(){
	//	this.taskDone = 0;
	//    monitor.beginTask("Parsing for " + RSE.appName, taskSize);
	//}
    public void incTaskDone() {
    	taskDone++;
		monitor.worked(1);
    }
    public boolean isTaskCancelled() {
    	return monitor.isCanceled();
    }

    protected ReloRdfRepository reloRdf;
    public void setRepo(ReloRdfRepository _reloRDF) {
        this.reloRdf = _reloRDF;
    }
    public ReloRdfRepository getRepo() {
        return reloRdf;
    }

    // allow modular addition of extra processing
    private final List<AtxaRDFBuildProcessor> buildProcessors;
    
    // used to be a processor, but now dep caching is becoming important and we
	// depend on it as well
    protected DepAndChildrenStrengthSummarizer dss = null;
    public DepAndChildrenStrengthSummarizer getDSS() {
    	return dss;
    }

    private String projectName;
    protected Resource projectResource;

    public AtxaBuildVisitor(ReloRdfRepository reloRdf, boolean resetDACSS) {
    	this.reloRdf = reloRdf;
        this.buildProcessors = PluggableBuildProcessor.getRegisteredProcessors();
        // clear static DCSS field so that the new repo is used
        if (resetDACSS)
        	DepAndChildrenStrengthSummarizer.resetDACSS();
        this.dss = DepAndChildrenStrengthSummarizer.getDACSS(this.reloRdf, null, PckgDirRepo.getPDR(this.reloRdf, RJCore.pckgDirContains));
        
    }
    
    public String getProjectName() {
    	return this.projectName;
    }
    public Resource getProjectResource() {
    	return this.projectResource;
    }

    // for debugging
    private static long processorRunDelay = 0;
    
    /**
     * Initializes an IProject project so that the project's resources that need it can be processed
     * */
    public void initProject(IProject project){
    	reloRdf.startTransaction();
        this.projectName = project.getName();
        projectResource = RSECore.eclipseProjectToRDFResource(reloRdf, project);
        reloRdf.addTypeStatement(projectResource, RJCore.projectType);
        /*
         * Needed for Javadoc support
         */
        reloRdf.addProjectTypeStatement(projectResource, RJCore.projectType);
        reloRdf.addNameStatement(projectResource, RSECore.name, this.projectName);
        reloRdf.addInitializedStatement(projectResource, RSECore.initialized, true);
        reloRdf.commitTransaction();

        // init the build processors
        Artifact projArt = new Artifact(projectResource);
        for (AtxaRDFBuildProcessor processor : buildProcessors) {
        	buildPerflogger.info("Build Job - Initing Processor: " + processor);
            processor.init(reloRdf, projArt, project);
            buildPerflogger.info("Build Job - Processor Inited: " + processor);
        }
        processorRunDelay = 0;
    }
    
    public void runProcessors(Resource classRes, boolean clean) {
    	// bug in sesame?: seems like namespaces are only saved on commitTransaction
		reloRdf.commitTransaction(); reloRdf.startTransaction(); 
        for (AtxaRDFBuildProcessor processor : buildProcessors) {
        	long startMS = System.currentTimeMillis();
        	if (clean)
        		processor.cleanRes(classRes);
        	else
        		processor.processRes(classRes);
            processorRunDelay += System.currentTimeMillis() - startMS;
        }
    }
    public boolean finishProcessing() {
    	buildPerflogger.info("Build Job - Running Processor Delay (ms): " + processorRunDelay);
    	reloRdf.startTransaction();
        for (AtxaRDFBuildProcessor processor : buildProcessors) {
        	// System.err.println(processor);
        	// Alloted 47% to Override and BrokenRef processors each and 2% to everybody else
        	int allotedTaskSize = (int) (.02 * (taskSize - taskPrev));
        	if (processor instanceof InferOverridesBuildProcessor || processor instanceof ResolveBrokenReferencesBuildProcessor)
        		allotedTaskSize = (int) (.47 *(taskSize - taskPrev));
        	SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, allotedTaskSize, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        	if (isTaskCancelled()) return true;
        	buildPerflogger.info("Build Job - Finishing Processor: " + processor);
        	long procStart =  System.currentTimeMillis();
        	if (removeAnnotations)
        		processor.cleanProj(this, subMonitor);
            else {
				// try catch needed here so that if an unexpected error occurs
				// during processing, we can handle it and continue processing
				// without leaving items in the queue
            	try {
            		processor.processProj(this, subMonitor);	
            	} catch(Throwable t) {
            		t.printStackTrace();
            	}
            }
            buildPerflogger.info("Build Job - Processor Finished: " + processor);
            long procEnd =  System.currentTimeMillis();
            if (debug)
            	logger.info("Time taken by Processor: " + processor.getClass() + "\t" + (procEnd - procStart)/1000);
            subMonitor.done();
        }
        dss.finishSummarizing();
    	reloRdf.commitTransaction();
    	monitor.done();
    	return false;
    }

    /**
     * forces removing annotations for IResourceVisitor (IResourceDeltaVisitor
     * already has remove/add flag). Default is false (does not remove
     * annotations - not needed when connecting for the first time, and mostly
     * when change is needed you get an IResourceDeltaVisitor)
     */
    public boolean removeAnnotations = false;
    
    /**
     * forces adding annotations for IResourceVisitor (IResourceDeltaVisitor
     * already has remove/add flag). Default is true (adds annotations)
     */
    public boolean addAnnotations = true;
    
    
	public static final String ReloExt = "relo";
	public static final String ClassExt = "class";
	public static final String JarExt = "jar";
	public static final String ZipExt = "zip";
	public void setProject(Resource projectResource) {
		this.projectResource = projectResource;
	}
	
}
