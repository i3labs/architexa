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
package com.architexa.diagrams.strata.model.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.AtxaBuildVisitor;
import com.architexa.diagrams.jdt.builder.asm.PackageBasedRDFBuildProcessor;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.store.ReloRdfRepository;

/**
 * This class works by iterating through all the children of a class and
 * building.<br>
 * Building involves following the target relationship and adding it to all the
 * destination's parents.<br>
 * <br>
 * TODO: We assume that if a class changes then all the
 * RJCore.containmentCacheValid property for it will be removed by some other
 * processor
 * 
 * @author vineet
 */
public class DepStrengthSummarizerProcessor_Old extends PackageBasedRDFBuildProcessor {
    static final Logger logger = StrataPlugin.getLogger(DepStrengthSummarizerProcessor_Old.class);

    @Override
    public void processPckg(Artifact packageArt) {
    	// until the cache is broken when the code changes, we really can't do this incrementally
    	// the common case that is causing problems is when the dependencies
		//   destination has containment information added later, in which case we
		//   possibly need to break the cache on the entire containment heirarchy
		//   of the destination artifact and the heirarchy of all sources of
		//   dependencies.
    	if (true) return;
    	
    	// we actually process only the indirect packages here (i.e. the x.y.z.* cases)
    	List<Artifact> pckgChildren = packageArt.queryPckgDirContainsArtList(rdfRepo, RJCore.fwdPckgDirContains);
    	for (Artifact pckgChild : pckgChildren) {
			if (rdfRepo.hasStatement(pckgChild.elementRes, RJCore.indirectPckgDirType, true)) {
				// should really be only one child where this happens
				//if (isIndirectPckgDirType(pckgChild)) logger.info("Processing : " + pckgChild +  " cached: " + rdfRepo.contains(pckgChild.elementRes, RJCore.containmentCacheValid, true));
		    	process(pckgChild);
			} else {
				// these are the complex cases which will be dealt by the finishProcessing case
			}
		}
    	
    }

    @Override
    public void processProj(AtxaBuildVisitor builder, IProgressMonitor monitor) {
        //logger.error("processing: " + projArt);
        process(projArt);
    }

    @Override
    public void cleanProj(AtxaBuildVisitor builder, IProgressMonitor monitor) {
        // we really should be cleaning up after ourselves here, but for now
        // LogicalContainmentHeirarchyProcessor is doing it
    }

    private void process(Artifact currArt) {
        process(currArt, RJCore.calls, RJCore.containmentBasedCalls);
        process(currArt, RJCore.inherits, RJCore.containmentBasedInherits);
        process(currArt, RJCore.refType, RJCore.containmentBasedRefType);
    }
    
    private HashMap<Artifact, Integer> process(Artifact currArt, URI rel, URI virtualRel) {
        if (rdfRepo.contains(currArt.elementRes, RJCore.containmentCacheValid, virtualRel)) {
            return readDepStrength(rdfRepo, currArt, virtualRel);
        } else {
        	// build dependencies for curArt by 'building' on it and all its
			// children
            HashMap<Artifact, Integer> depStrength = null;
            depStrength = buildDepStrength(rdfRepo, currArt, rel);
            
            for (Artifact childArt : getContainedArts(rdfRepo, currArt)) {
                addStrengthsMap(depStrength, process(childArt, rel, virtualRel));
            }
            
            writeDepStrength(rdfRepo, currArt, virtualRel, depStrength);

            return depStrength;
        }
    }
    
    ///////////////////////////////
    // utility methods (hence static)
    ///////////////////////////////

    // builds dependencies by following relationship and all the destinations parents
    protected static HashMap<Artifact, Integer> buildDepStrength(ReloRdfRepository rdfRepo, Artifact currArt, URI rel) {
        HashMap<Artifact, Integer> depStrength = new HashMap<Artifact, Integer> (100);

        List<Artifact> dstArts = currArt.queryArtList(rdfRepo, DirectedRel.getFwd(rel));
        for (Artifact dstArt : dstArts) {
            List<Artifact> parentArts = getContainerArts(rdfRepo, dstArt);
            for (Artifact parentArt : parentArts) {
                int oldStrength = 0;
                if (depStrength.containsKey(parentArt)) oldStrength = depStrength.get(parentArt);
                depStrength.put(parentArt, oldStrength + 1);
            }
        }
        
        return depStrength;
    }

    // provided for debugging
    @SuppressWarnings("unused")
	private static final String dump(HashMap<Artifact, Integer> depStrength) {
    	boolean first = true;
    	String dumpStr = "{";
    	for (Map.Entry<Artifact, Integer> dep : depStrength.entrySet()) {
    		if (!first) dumpStr += ", ";
    		dumpStr += dep.getKey() + "==>" + dep.getValue();
			first = false;
		}
    	dumpStr +="}";
    	return dumpStr;
	}

    private static HashMap<Artifact, Integer> readDepStrength(ReloRdfRepository rdfRepo, Artifact srcArt, URI virtualRel) {
        List<Artifact> dstArts = srcArt.queryArtList(rdfRepo, DirectedRel.getFwd(virtualRel));
        HashMap<Artifact, Integer> depStrength = new HashMap<Artifact, Integer> (dstArts.size());
        for (Artifact dstArt : dstArts) {
            depStrength.put(dstArt, readDepStrength(rdfRepo, srcArt, dstArt, virtualRel));
        }
        
        return depStrength;
    }
    
    public static int readDepStrength(ReloRdfRepository repo, Artifact srcArt, Artifact dstArt, URI virtualRel) {
        if (!repo.hasStatement(srcArt.elementRes, virtualRel, dstArt.elementRes)) return 0;
        Statement depStmt = repo.createStmt(srcArt.elementRes, virtualRel, dstArt.elementRes);
        Statement depStrength = repo.getStatement(depStmt, RJCore.containmentBasedDepStrength, null);
        if (depStrength.getObject() == null) return 1;
        return Integer.parseInt(((Literal)depStrength.getObject()).getLabel());
    }

    protected static void writeDepStrength(ReloRdfRepository rdfRepo, Artifact currArt, URI virtualRel, HashMap<Artifact, Integer> dstWithDepStrength) {
        for (Map.Entry<Artifact, Integer> depStrengthEntry : dstWithDepStrength.entrySet()) {
            Artifact dstArt = depStrengthEntry.getKey();
            Integer dstArtStrength = depStrengthEntry.getValue();
            rdfRepo.addStatement(currArt.elementRes, virtualRel, dstArt.elementRes);
            Statement depStmt = rdfRepo.createStmt(currArt.elementRes, virtualRel, dstArt.elementRes);
            rdfRepo.addStatement(depStmt, RJCore.containmentBasedDepStrength, dstArtStrength.toString());
        }
        rdfRepo.addStatement(currArt.elementRes, RJCore.containmentCacheValid, virtualRel);
    }

    protected static void addStrengthsMap(HashMap<Artifact, Integer> lhs, HashMap<Artifact, Integer> rhs) {
        for (Map.Entry<Artifact, Integer> rhsEntry : rhs.entrySet()) {
            Artifact currKey = rhsEntry.getKey();
            int currStrength = rhsEntry.getValue();
            if (lhs.containsKey(currKey)) currStrength += lhs.get(currKey);
            lhs.put(currKey, currStrength);
        }
    }


    private static List<Artifact> getContainedArts(ReloRdfRepository repo, Artifact cu) {
        List<Artifact> listArt = null;

        listArt = cu.queryPckgDirContainsArtList(repo, DirectedRel.getFwd(RJCore.pckgDirContains));

        if (listArt.size() == 0) {
            listArt = cu.queryArtList(repo, RSECore.fwdContains);
        }
        return listArt;
    }
    
    private static List<Artifact> getContainerArts(ReloRdfRepository repo, Artifact cu) {
        List<Artifact> parentArts = new ArrayList<Artifact> (20);
        parentArts.add(cu);
        getContainerArts(repo, cu, parentArts);
        return parentArts;
    }
    
    private static void getContainerArts(ReloRdfRepository repo, Artifact cu, List<Artifact> collectorList) {
        List<Artifact> parentArts = null;

        parentArts = cu.queryPckgDirContainsArtList(repo, DirectedRel.getRev(RJCore.pckgDirContains));

        if (parentArts.size() == 0)
        	parentArts = cu.queryArtList(repo, RSECore.revContains);
        
        for (Artifact parentArt : parentArts) {
            collectorList.add(parentArt);
            getContainerArts(repo, parentArt, collectorList);
		}

        if (parentArts.size() > 1) {
        	// likey because of a bug somewhere else, because there are no cases
        	// TODO XXX remove comment below: Commented while rdf statement removal support does not exist
            //logger.warn("Multiple containers of: " + cu + " :: " + parentArts);
        }
        
        return;
    }
    
}
