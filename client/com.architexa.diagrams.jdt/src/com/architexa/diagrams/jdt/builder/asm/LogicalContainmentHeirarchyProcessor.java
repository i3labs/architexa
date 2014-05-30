/* 
 * Copyright (c) 2004-2006 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */

/*
 * Created on Oct 3, 2006
 */
package com.architexa.diagrams.jdt.builder.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IStartup;
import org.openrdf.model.Resource;
import org.openrdf.sesame.sailimpl.nativerdf.model.NativeURI;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.AtxaBuildVisitor;
import com.architexa.diagrams.jdt.services.PluggableBuildProcessor;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.store.PckgDirRepo;
import com.architexa.store.ReloRdfRepository;

/**
 * Create and define package dirs and package dir contains<br>
 * <br>
 * If we are given parentPckg=a and childPckg=a.b, with a.b containing classes
 * C, D, and E <br>
 * <br>
 * The default contains relationship is: proj -> a.b -> {a.b.C, a.b.D, a.b.E} <br>
 * We want to make a relationship to be: proj -> a.* -> a.b.* -> a.b
 * <br>
 * a.b is a regular package<br>
 * xyz.* are indirect packages<br>
 * both will be true for being packgeDirTypes<br>
 * <br>
 * The main need for such a system is to allow the querying towards children
 */
public class LogicalContainmentHeirarchyProcessor extends PackageBasedRDFBuildProcessor implements IStartup {
    static final Logger logger = Activator.getLogger(LogicalContainmentHeirarchyProcessor.class);
    
	private PckgDirRepo pdr;
    
    @Override
    public void init(ReloRdfRepository rdfRepo, Artifact projArt, IProject project) {
    	super.init(rdfRepo, projArt, project);
    	pdr = PckgDirRepo.getPDR(rdfRepo, RJCore.pckgDirContains);
    	pdr.init(false); // clear map for new build
    }

    public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
		PluggableBuildProcessor.registerLast(this);
	}
    
    @Override
    public void processPckg(Artifact packageArt) {
    	addIndirectPckg(packageArt);
    }
    
	// indirect packages for the x.y.z package is the x.y.z.* package
    private void addIndirectPckg(Artifact givenPckg) {
        String indirectPckgName = givenPckg.queryName(rdfRepo) + ".*";
        Artifact indirectPckgDir = getPckgNameRes(indirectPckgName);

        // add x.y.z.* -> x.y.z
        rdfRepo.addStatement(givenPckg.elementRes, RJCore.pckgDirType, true);
        rdfRepo.addStatement(givenPckg.elementRes, RSECore.initialized, true);

        rdfRepo.addStatement(indirectPckgDir.elementRes, RJCore.pckgDirType, true);
        pdr.addContains(indirectPckgDir.elementRes, givenPckg.elementRes);
        rdfRepo.addStatement(indirectPckgDir.elementRes, RSECore.initialized, true);
	}


    @Override
    public void processProj(AtxaBuildVisitor builder, IProgressMonitor monitor) {
    	super.processProj(builder, monitor);

    	if (packgList == null) createPackgList();
    	
    	// I. Get all packages in the project, find their container indirect
		// packages, and place in a {name->resource} map
        List<Artifact> projPckgs = projArt.queryArtList(rdfRepo, RSECore.fwdContains);
        monitor.beginTask("Logical Containment Heirarchy processing: ", projPckgs.size());
        Map<String, Artifact> pckgNamesToArt = new HashMap<String, Artifact>();
        for (Artifact pckgArt : projPckgs) {
        	if (packgList.contains(((NativeURI)pckgArt.elementRes).getLocalName()))
    			continue;
        	Resource parentRes = pdr.getStatementSubj((Resource)null, RJCore.pckgDirContains, pckgArt.elementRes);
        	if (parentRes == null) {
        		// TODO: find cause - likely happens when a project is deleted
        		logger.warn("Unexpected null parent for: " + pckgArt.elementRes);
        	} else
        		pckgNamesToArt.put(pckgArt.queryName(rdfRepo) + ".*", new Artifact(parentRes));
        }
        
        // II. For all packages in the map get all the parent package names (and add them to the database)
        for (String pckgName : new HashSet<String>(pckgNamesToArt.keySet())) {
        	if (monitor.isCanceled()) return;
        	monitor.subTask(pckgName);
        	monitor.worked(1);
        	Artifact pckgNameArt = pckgNamesToArt.get(pckgName);
            
            String parentPckgName = getPckgNameParent(pckgName);
            while (!pckgNamesToArt.containsKey(parentPckgName)) {
                if (parentPckgName == null) break;  // top level

                Artifact parentPckgArt = getPckgNameRes(parentPckgName);
                pckgNamesToArt.put(parentPckgName, parentPckgArt);
                addPckgNameContains(parentPckgArt, pckgNameArt, pckgName, pckgNamesToArt);

                // for next recursive loop
                pckgNameArt = parentPckgArt;
                pckgName = parentPckgName;
                parentPckgName = getPckgNameParent(pckgName);
            };
            if (parentPckgName == null) {
                // we reached to the top level - connect to the project (exited because of break)
                addPckgNameContains(projArt, pckgNameArt, pckgName, pckgNamesToArt);
            } else {
                // connect to the found package (exited because of containsKey returned true)
                addPckgNameContains(pckgNamesToArt.get(parentPckgName), pckgNameArt, pckgName, pckgNamesToArt);
            }
        }
    }

    private String getPckgNameParent(String pckgName) {
    	// do '.length()-2' to ignore the last '.*'
        int lastDot = pckgName.lastIndexOf(".", pckgName.length()-2-1);
        if (lastDot == -1) 
            return null;
        else
            return pckgName.substring(0, lastDot) + ".*";
    }

    // TODO: move to AsmPackageSupport
    private final Artifact getPckgNameRes(String pckgName) {
        Resource parentPckgRes = packageDirToResource(rdfRepo, pckgName);
        // we use the packageType since it has all the same properties as a package 
        // ideally we will want to have use subclassing and a packageNameType or something similar
        rdfRepo.addNameStatement(parentPckgRes, RSECore.name, pckgName);
        rdfRepo.addTypeStatement(parentPckgRes, RJCore.indirectPckgDirType);
        return new Artifact(parentPckgRes);
    }
    /**
     * Exists to represent resources for higher level empty packages. Note: this
     * has to be consistent with the packages in RJCore.elementToResource, since
     * we don't want to deal with RDF's notions of resource equality, etc.
     */
    // TODO: move to AsmPackageSupport
    public static Resource packageDirToResource(ReloRdfRepository reloRepo, String pckgName) {
        return RSECore.idToResource(reloRepo, RJCore.jdtWkspcNS, pckgName);
    }
    private final void addPckgNameContains(Artifact parentPckg, Artifact childPckg, String childPckgName, Map<String, Artifact> pckgNameToArt) {
        rdfRepo.addStatement(parentPckg.elementRes, RJCore.pckgDirType, true);
        pdr.addContains(parentPckg.elementRes, childPckg.elementRes);
    }

    @Override
    public void cleanProj(AtxaBuildVisitor builder, IProgressMonitor monitor) {
        // we should be cleaning up only things that we added, but until then lets remove everything
        
        // lets build a list of all the packages
        List<Artifact> allPckgs = new ArrayList<Artifact> (50);

        if (project != null && packgList == null)
			createPackgList();
        
        Queue<Artifact> pckgsQueue = new LinkedList<Artifact>();
        pckgsQueue.add(projArt);
        while (!pckgsQueue.isEmpty()) {
            Artifact currArt = pckgsQueue.remove();
            allPckgs.add(currArt);
            
            List<Artifact> currChildren = currArt.queryPckgDirContainsArtList(pdr, DirectedRel.getFwd(RJCore.pckgDirContains));
            pckgsQueue.addAll(currChildren);
        }
        
        // remove statements
        for (Artifact artifact : allPckgs) {
            rdfRepo.removeStatements(artifact.elementRes, null, null);
        }
    }
}
