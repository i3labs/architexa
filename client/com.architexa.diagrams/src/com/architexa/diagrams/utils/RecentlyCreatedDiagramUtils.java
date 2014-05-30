package com.architexa.diagrams.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.Parser;
import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.StatementHandlerException;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.ui.PluggableDiagramsSupport;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.ui.RSEEditorViewCommon;
import com.architexa.diagrams.ui.RSEEditorViewCommon.IRSEEditorViewCommon;
import com.architexa.rse.BuildStatus;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class RecentlyCreatedDiagramUtils {

	private static final int SAVE_LIMIT = 20;
	public static IPath recentDiagramPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append("/.metadata/.plugins/com.architexa.intro/");


	public static void saveDiagram(final RSEEditorViewCommon.IRSEEditorViewCommon editor, IContainer defaultProject, final RootArtifact rootArt) {
		// TODO: 
		//  same name handling
		// 		use static Utils class to get list of recent diagrams and compare names etc 

		Job saveJob = new Job("Save Recent Diagram") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (getRecentItems()!=null && getRecentItems().length >=SAVE_LIMIT)
					deleteOldest(getRecentItems());
				
				File recentDiagramPathFile = recentDiagramPath.toFile();
				try {
					if (!recentDiagramPathFile.exists()) {
						recentDiagramPathFile.mkdirs();
					}
				} catch (Throwable e) {
					System.err.println("Error creating Directory");
					e.printStackTrace();
				}
				
				
				FileWriter fstream;
				String fileName = getContentBasedFileName(editor, ArtifactFragment.getAllNestedShownChildren(rootArt, new ArrayList<ArtifactFragment>()));
				try {
					fstream = new FileWriter(recentDiagramPath + fileName + ".atxa" );
				    BufferedWriter out = new BufferedWriter(fstream);
				    out.close();
				} catch (IOException e) {
					System.err.println("Error creating recent diagram file");
					e.printStackTrace();
				}

				Path path = new Path(recentDiagramPath + fileName + ".atxa");
				File javaFile = path.toFile();
				try {
					if (!javaFile.exists()) {
						javaFile.createNewFile();
					}
					saveJavaFile(editor, monitor, javaFile);
				} catch (Throwable e) {
					System.err.println("Error saving recent diagram file");
					e.printStackTrace();
				}
				done(Status.OK_STATUS);
				return Status.OK_STATUS;
			}
		};
		saveJob.setSystem(true);
		saveJob.setUser(false);
		// need to delay here so job does not conflict with eclipse
		saveJob.schedule(1000);
	}

	
	private static void deleteOldest(File[] files) {
		File oldestFile = null;
		long oldestTime = -1;
		for (int i=0; i<files.length; i++ ) {
			File file = files[i];
			String fileName = file.getName();
			long time =  Long.parseLong(fileName.substring(fileName.lastIndexOf("DATE~")+5,fileName.lastIndexOf("TYPE~")));
			if (time < oldestTime || oldestTime == -1) {
				oldestTime = time;
				oldestFile = file;
			}
		}
		oldestFile.delete();
	}


	private static void saveJavaFile(RSEEditorViewCommon.IRSEEditorViewCommon rseEditorView, IProgressMonitor monitor, File file) {
		BuildStatus.addUsage("RecentlyCreatedDiagram Saved: " + file);
		RSEEditorViewCommon.saveJavaFile(rseEditorView, file, monitor);
		rseEditorView.clearDirtyFlag();
	}


	public static String getContentBasedFileName(IRSEEditorViewCommon editor, List<ArtifactFragment> children) {
		return getLabel(children) + getDate() + getDiagramType(editor);
	}
	private static String getDiagramType(IRSEEditorViewCommon editor) {
		return "TYPE~" + editor.getClass();
	}
	private static String getDate() {
		return "DATE~"+new Date().getTime();
	}
	private static String getLabel(List<ArtifactFragment> children) {
		
		String label = "NAME~";
		
		if (children.size() == 0)
			return label + "Blank";
		
		for (int i=0; i<3 && i<children.size(); i++) {
			if (children.get(i).getArt().elementRes.toString().contains("<clinit>"))
				continue;
			label += getLabelForRes( children.get(i).getArt().elementRes)+", ";
		}
		label = label.substring(0,label.length()-2);
		return label;
	}

	public static String getDisplayName(String fileName) {
		return fileName.substring(fileName.indexOf("NAME~")+5, fileName.indexOf("DATE~"));
	}



	private static String getLabelForRes(Resource elementRes) {
		String trimRes = RSECore.resourceWithoutWorkspace(elementRes);
		
		trimRes = trimRes.substring(trimRes.lastIndexOf(".")+1);
		if (trimRes.equals("*")) { // ignore packagefrags, get the real package and remove workspace root incase it is top level
			trimRes = elementRes.toString().substring(elementRes.toString().indexOf("#")+1, elementRes.toString().length()-2);
			trimRes = trimRes.substring(trimRes.lastIndexOf(".")+1);
		}
			
		String fileName = trimRes;
		if (trimRes.contains("$")) {
			fileName = trimRes.substring(trimRes.lastIndexOf("$")+1);
		}
		
		if (fileName.length()==0) return "(system)";

		// capitalize
		String letter1 = fileName.substring(0,1).toUpperCase(); 
		
		// some filesNames might only be one char long?
		if (fileName.length()>1)
			fileName = letter1 + fileName.substring(1);
		else
			fileName = letter1;
		
		// remove parens / params
		fileName = fileName.replaceAll("\\([^\\(]*\\)", "");
		
		return fileName;
	}


	public static File[] getRecentItems() {
		File folder = new File(recentDiagramPath.toString());
		return folder.listFiles(); 
	}

	
	public static ImageDescriptor getImageDescriptor(File file) {
		
		// disable for now: we don't want to create engine instances each time we open the menu just to get images
		if (true) return null;
		
		String fileName = file.getName();
		String type = fileName.substring(fileName.indexOf("TYPE~")+5);
		
		ImageDescriptor desc = null;
		for (IRSEDiagramEngine engine : PluggableDiagramsSupport.getRegisteredDiagramEngines()) {
			String id = engine.editorId().replace(".editor","");
			if (type.contains(id)) {
				try {
					RSEEditor editor = engine.getEditorClass().newInstance();
					desc = editor.getImageDescriptor();
					return  desc;
				} catch (Throwable e) {
					System.err.println("Error getting editor Image for recent diagram");
					e.printStackTrace();
					return null;
				}
				
			}
		}
		return desc;
		
	}


	public static void readFile(String partName, InputStream inputStream, final ReloRdfRepository dstRDFRepo) {
		if (inputStream == null || dstRDFRepo == null) return;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			Parser parser = StoreUtil.getRDFParser(dstRDFRepo);
			parser.setStatementHandler(new StatementHandler() {
				public void handleStatement(Resource subj, URI pred, Value obj) throws StatementHandlerException {
					dstRDFRepo.addStatement(subj, pred, obj);
				}});
			dstRDFRepo.startTransaction();
			parser.parse(in, ReloRdfRepository.atxaRdfNamespace);
			dstRDFRepo.commitTransaction();
			in.close();
			inputStream.close();
		} catch (Throwable e) {
			System.err.println("Error reading recent diagram file");
			e.printStackTrace();
		}
			
	}
}
