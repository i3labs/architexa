/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
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
 * Created on Jul 17, 2004
 *  
 */
package com.architexa.diagrams.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.diagrams.utils.DbgRes;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.RepositoryMgr;
import com.architexa.store.StoreUtil;


/**
 * There represent Relationship instances
 * 
 * Property relationRes should correspond to RelType 
 * 
 * @author vineet
 *
 */
public class ArtifactRel {
	// below only stored for debugging purposes
    static final Logger logger = Activator.getLogger(ArtifactRel.class);
	public DbgRes tag = new DbgRes(ArtifactRel.class, this);
	
	protected ArtifactFragment srcArt;
	protected ArtifactFragment dstArt;

	public URI relationRes;
	
	private boolean isUserCreated = false;
	
	public ArtifactRel() {}
	
	public ArtifactRel(ArtifactFragment srcArt, ArtifactFragment dstArt, URI relationRes) {
        init(srcArt, dstArt, relationRes);
	}
	
    public void init(ArtifactFragment srcArt, ArtifactFragment dstArt, URI relationRes) {
        if (relationRes == null) throw new IllegalArgumentException();
        
        this.srcArt = srcArt;
        this.dstArt = dstArt;
        
        this.relationRes = relationRes;
    }

    protected Resource instanceRes = null;
    public Resource getInstanceRes() {
        if (instanceRes == null) instanceRes = StoreUtil.createBNode();
        return instanceRes;
    }
    public void setInstanceRes(Resource instanceRes) {
        this.instanceRes = instanceRes;
    }
    
    public ArtifactFragment getSrc() {
	    return srcArt;
	}
	public ArtifactFragment getDest() {
	    return dstArt;
	}
    public URI getType() {
        return relationRes;
    }
    public void setType(URI relationRes) {
    	this.relationRes = relationRes;
    }

    public void connect(ArtifactFragment srcAF, ArtifactFragment dstAF) {
	    if (srcAF.sourceConnectionsContains(this)) {
	    	logger.error("ERR: Trying to add connection when relation already exists", new Exception());
	    	return;
	    }
    
    	srcAF.addSourceConnection(this);
	    if (dstAF.targetConnectionsContains(this)) {
	    	logger.error("ERR: Trying to add connection when relation already exists", new Exception());
	    	return;
	    }
    	dstAF.addTargetConnection(this);
	}

	@Override
    public String toString() {
		return  srcArt + " --> " + dstArt;
	}
	

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((srcArt == null) ? 0 : srcArt.hashCode());
		result = prime * result
		+ ((relationRes == null) ? 0 : relationRes.hashCode());
		result = prime * result + ((dstArt == null) ? 0 : dstArt.hashCode());
		result = prime * result
				+ ((instanceRes == null) ? 0 : instanceRes.hashCode());
		return result;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		ArtifactRel other = (ArtifactRel) obj;

		if (srcArt == null) {
			if (other.srcArt != null) return false;
		} else if (!srcArt.equals(other.srcArt))
			return false;

		if (relationRes == null) {
			if (other.relationRes != null) return false;
		} else if (!relationRes.equals(other.relationRes))
			return false;

		if (dstArt == null) {
			if (other.dstArt != null) return false;
		} else if (!dstArt.equals(other.dstArt))
			return false;

		// instanceRes may be BNodes that are not equal,  we want to test if src and dest are the same
//		if (instanceRes == null) {
//			if (other.instanceRes != null) return false;
//		} else if (!instanceRes.equals(other.instanceRes))
//			return false;

		return true;
	}
    
	////////////////////
    // property change support
	////////////////////
    private PropertyChangeSupport pcSupport = new PropertyChangeSupport(this);
	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcSupport.addPropertyChangeListener(l);
	}
	public void removePropertyChangeListener(PropertyChangeListener l) {
		pcSupport.removePropertyChangeListener(l);
	}
	protected void firePropChang(String prop) {
		pcSupport.firePropertyChange(prop, "Old", "New");
	}
	public void firePropertyChange(String prop, Object oldValue, Object newValue) {
		pcSupport.firePropertyChange(prop, oldValue, newValue);
	}

	
	 // support for diagram policies
    protected Map<Object, DiagramPolicy> policies = new HashMap<Object, DiagramPolicy> (2);

    public Collection<DiagramPolicy> getDiagramPolicies() {
    	return policies.values();
    }
	public static <T extends DiagramPolicy> void ensureInstalledPolicy(ArtifactRel artRel, String policyKey, Class<T> diagPolicyClass) {
	    if (artRel != null && artRel.getDiagramPolicy(policyKey) == null)
			try {
				artRel.installDiagramPolicy(policyKey, diagPolicyClass.newInstance());
			} catch (Exception e) {
				logger.error("Could not instantiate policy: " + diagPolicyClass, e);
			}
	}
    public void installDiagramPolicy(String policyKey, DiagramPolicy diagPolicy) {
    	policies.put(policyKey, diagPolicy);
    	diagPolicy.setHost(this);
    }
    public DiagramPolicy getDiagramPolicy(String key) {
    	return policies.get(key);
    }
    
    @SuppressWarnings("unchecked")
	public <T extends DiagramPolicy> T getTypedDiagramPolicy(T type, String key) {
		DiagramPolicy policy = policies.get(key);
		if (policy == null || !(policy.getClass().isInstance(type))) {
			logger.error("Policy " + key + " not installed on rel. Relation: " + getInstanceRes() + " Class: " + getClass());
			return null;
		}
    	return (T) policy;
    }
    
    /////////////////////////////////
	// support for read and write rdf
	/////////////////////////////////
	public void writeRDF(RdfDocumentWriter rdfWriter, List<ArtifactRel> savedRels) throws IOException {
        ReloRdfRepository rdfRepo = StoreUtil.getDefaultStoreRepository();
        
        rdfWriter.writeStatement(getInstanceRes(), rdfRepo.rdfType, RSECore.link);
        rdfWriter.writeStatement(getInstanceRes(), RSECore.model, this.relationRes);
        rdfWriter.writeStatement(getInstanceRes(), rdfRepo.rdfSubject, this.getSrc().getInstanceRes());
        rdfWriter.writeStatement(getInstanceRes(), rdfRepo.rdfObject, this.getDest().getInstanceRes());
        
        // write policies
    	for (DiagramPolicy diagPolicy : this.getDiagramPolicies()) {
    		try {
    			diagPolicy.writeRDF(rdfWriter);
    		} catch (Throwable t){
    			logger.error("Error while Saving, some data may be lost",t);
    		}
		}
        
		savedRels.add(this);
    }

    public static void readRDF(RootArtifact rootArt, 
            ReloRdfRepository docRepo, 
            Resource instanceRes,
            Map<Resource,ArtifactFragment> instanceRes2AFMap,
            Map<Resource,ArtifactRel> instanceRes2ARMap) {
    	
        URI linkRes = (URI) docRepo.getStatement(instanceRes, RSECore.model, null).getObject(); 
//        StatementIterator itr = docRepo.getStatements(null, RSECore.userCreated, null);
//		while (itr.hasNext()) {
//			Statement stmt = itr.next();
//			System.err.println("Subject: " + stmt.getSubject());
////			System.out.println("Object: " + stmt.getObject());
//		}
//        docRepo.addStatement(StoreUtil.createMemURI("Test repo"), RSECore.userCreated, StoreUtil.createMemLiteral("true"));
//        Literal userCreated = (Literal)docRepo.getStatement(instanceRes, RSECore.userCreated, null).getObject();
//        System.err.println("Adding rels: " + instanceRes + "\t" + userCreated + "\t" + docRepo);
        ArtifactFragment srcAF = instanceRes2AFMap.get( docRepo.getStatement(instanceRes, docRepo.rdfSubject, null).getObject() );
        ArtifactFragment dstAF = instanceRes2AFMap.get( docRepo.getStatement(instanceRes, docRepo.rdfObject, null).getObject() );

        ArtifactRel artRel = PluggableTypes.getAR(linkRes);
        if (artRel != null) {
        	// we were given a class location - load it (needed when NamedRel's are being saved)
            artRel.init(srcAF, dstAF, linkRes);
        } else {
        	// create the default ArtFrag
            artRel = new ArtifactRel(srcAF, dstAF, linkRes);
        }
        
        artRel.setInstanceRes(instanceRes);
        instanceRes2ARMap.put(instanceRes, artRel);

        artRel.connect(srcAF, dstAF);

        //Add the file's relation related statements to the cache repo 
        //so that the diagram can be drawn the way it was saved (even if 
        //its code elements have changed)
        ReloRdfRepository fileStmts = ((RepositoryMgr)rootArt.getRepo()).getFileRepo();
        fileStmts.startTransaction();
        fileStmts.addStatement(srcAF.getArt().elementRes, artRel.relationRes, dstAF.getArt().elementRes);
        fileStmts.commitTransaction();
        
        artRel.readRDF(docRepo);
    }

	protected void readRDF(ReloRdfRepository queryRepo) {
    	// @tag rearch-stabilize: delete below?
    	//((ReloController) getRoot().getContents()).getLayoutMgr().anchorPart(this);

        // write policies
    	for (DiagramPolicy diagPolicy : this.getDiagramPolicies()) {
    		try {
    			diagPolicy.readRDF(queryRepo);
    		} catch (Throwable t){
    			logger.error("Error while Saving, some data may be lost",t);
    		}
		}
    }

	public void setUserCreated(boolean isUserCreated) {
		this.isUserCreated = isUserCreated;
	}

	public boolean isUserCreated() {
		return isUserCreated;
	}
    
}