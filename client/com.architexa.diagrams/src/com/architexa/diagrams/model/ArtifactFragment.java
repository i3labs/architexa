package com.architexa.diagrams.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RdfDocumentWriter;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.diagrams.utils.DbgRes;
import com.architexa.diagrams.utils.LoadUtils;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.RepositoryMgr;
import com.architexa.store.StoreUtil;



/**
 * This class is not expected to be subclassed. - The only exception is for
 * providing utility methods that hold no data, and effectively clone this
 * ArtFrag so that data is copied over.
 * 
 * 
 * 
 * Like Artifact but comes into play when we are talking about an Artifact and
 * both its children and its parent. Also since we are maintaining some state we
 * also support property change listeners.
 * 
 * If more than one definition of children is needed then you will need a
 * DerivedCodeUnit
 * 
 * @tag examine-postRearch: Since an ArtifactFragment has attributes and
 *      references other than the underlying resource, we cannot have equality
 *      in identity as is likely the case right now.
 * 
 * @author vineet
 * 
 */
public class ArtifactFragment {
	
	public static final String PROPERTY_EDIT_INSTANCE_NAME = "instanceName";

	public static String libraryAnnotation = " [Library Code]";

	static final Logger logger = Activator.getLogger(ArtifactFragment.class);
    
    protected Artifact node = null;

    public ArtifactFragment() {
    	node = null;
    }
    public ArtifactFragment(Resource _elementRes) {
        node = new Artifact(_elementRes);
    }
    public ArtifactFragment(Artifact _art) {
    	node = _art;
    }
    public Artifact getArt() {
    	return node;
    }
    public void setArt(Artifact _node) {
    	node = _node;
    }
    
    protected void clone(ArtifactFragment _tosubsume) {
    	this.instanceName = _tosubsume.instanceName;
    	this.instanceRes = _tosubsume.instanceRes;
    	this.node = _tosubsume.node;
    	this.parentArt = _tosubsume.parentArt;
    	this.pcSupport = _tosubsume.pcSupport;
    	this.policies = _tosubsume.policies;
    	this.shownChildrenArt = _tosubsume.shownChildrenArt;
    	this.srcRel = _tosubsume.srcRel;
    	//this.tag = _tosubsume.tag;
    	this.tgtRel = _tosubsume.tgtRel;
    }

	// common methods to do querying, we just delegate calls to artifact
    public Resource queryType(ReloRdfRepository repo) {
	    return this.getArt().queryType(repo);
	}
	public List<Artifact> queryChildrenArtifacts(ReloRdfRepository repo, Predicate filterPred) {
    	return this.getArt().queryChildrenArtifacts(repo, filterPred);
    }
	public List<Artifact> queryChildrenArtifacts(ReloRdfRepository repo) {
    	return this.getArt().queryChildrenArtifacts(repo);
    }
    
    ////////////////////
	// support for non-resource backed artifacts - which typically have an 
    // associated resource backed artifact associated with it
	////////////////////
    public Artifact getNonDerivedBaseArtifact() {
        return this.getArt();
    }

    
	/*
	 * Debugging/logging support
	 */
	protected DbgRes tag = new DbgRes(ArtifactFragment.class, this);

	@Override
    public String toString() {
		return ">" + getArt().toString() + "<" + tag.getAbsoluteTrailer();
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
	public void firePropChang(String prop) {
		pcSupport.firePropertyChange(prop, "Old", "New");
	}
	public void firePropertyChange(String prop, Object oldValue, Object newValue) {
		pcSupport.firePropertyChange(prop, oldValue, newValue);
	}
	public PropertyChangeListener[] dbg_getPCL() {
		return pcSupport.getPropertyChangeListeners();
	}
	public static final String Parent_Changed = "ParentChanged";
	public static final String Contents_Changed = "ContentsChanged";
	public static final String Theme_Changed = "ThemeChanged";
	public static final String Attrib_Changed = "AttribChanged";
	public static final String Policy_Contents_Changed = "PolicyContentsChanged";	// Change in the contents of a *Diagram* Policy

	public void fireContentsChanged() {
		firePropChang(Contents_Changed);
	}
	public void fireThemeChanged() {
		firePropChang(Theme_Changed);
	}
	public void fireAttribChanged() {
		firePropChang(Attrib_Changed);
	}
	public void fireParentChanged() {
		firePropChang(Parent_Changed);
	}
	public void firePolicyContentsChanged() {
		firePropChang(Policy_Contents_Changed);
	}

	////////////////////
	// support for parents
	////////////////////
	protected ArtifactFragment parentArt = null;
	public ArtifactFragment getParentArt() {
		return parentArt;
	}
	public void setParentArt(ArtifactFragment _parentArt) {
		if (parentArt != _parentArt) {
			parentArt = _parentArt;
			fireParentChanged();
		}
	}
	//public void initParentArt(ReloRdfRepository repo) {
	//	//if (parentArt == null)
	//	//	parentArt = rootDoc.getUniqueAF(this.getParentArtifact(repo));
	//}
	public RootArtifact getRootArt() {
		return parentArt.getRootArt();
	}
	
	////////////////////
	// support for shown children
	////////////////////
    protected List<ArtifactFragment> shownChildrenArt = new ArrayList<ArtifactFragment>();
    public List<ArtifactFragment> getShownChildren() {
        return shownChildrenArt;
    }
    public int getShownChildrenCnt() {
        return shownChildrenArt.size();
    }
    public boolean isShowingChildren() {
        return shownChildrenArt.isEmpty();
    }
    public boolean containsChild(ArtifactFragment af) {
        return shownChildrenArt.contains(af);
    }
    public void appendShownChild(ArtifactFragment child) {
        appendShownChild(child, -1);
    }
    
    public static final boolean owningContainer(ArtifactFragment af) {
    	return !(af instanceof INonOwnerContainerFragment);
    }

	/**
	 * Does three things (first does them and then fires the events):<br>
	 * i: Adds the node as a child<br>
	 * ii: If taking ownership, sets the parent to be self<br>
	 * iii: If taking ownership, removes the given node from previous parent<br>
	 * <br>
	 * Support for those (most) ArtFrag's which maintain ownership of their
	 * children is done by "!(this instanceof INonOwnerContainerFragment)"
	 */
    public void appendShownChild(ArtifactFragment childAF, int ndx) {
		if (shownChildrenArt.contains(childAF)) {
			logger.warn("artFrag already in collection: " + childAF);
			return;
		}
    	//logger.info("appending: " + child + " to: " + this);

		// prep
		ArtifactFragment oldParentAF = childAF.parentArt;

		// do all the changes
		if (ndx == -1)																// [[i]]
			shownChildrenArt.add(childAF);
		else
			shownChildrenArt.add(ndx, childAF);
    	
    	if (owningContainer(this)) {
    		childAF.parentArt = this;												// [[ii]]
    		if (oldParentAF != null) oldParentAF.shownChildrenArt.remove(childAF);	// [[iii]]
    	}
    	
    	// fire the events (in the same order as the above)
    	fireContentsChanged();														// [[i]]
    	if (owningContainer(this)) {
        	childAF.fireParentChanged();											// [[ii]]
    		if (oldParentAF != null) oldParentAF.fireContentsChanged();				// [[iii]]
    	}
    	return;
    }
    /**
     * Works similarly to appendShownChild
     */
    public void appendShownChildren(List<? extends ArtifactFragment> childrenAF) {
		// prep - get old parent (for owned containers)
    	Map<ArtifactFragment, ArtifactFragment> oldParentAFMap = null;
    	if (owningContainer(this)) {
    		oldParentAFMap = new HashMap<ArtifactFragment, ArtifactFragment> (childrenAF.size());
        	for (ArtifactFragment childAF : childrenAF) {
        		oldParentAFMap.put(childAF, childAF.parentArt);
			}
    	}
    	
		for (ArtifactFragment childAF : childrenAF) {
			if (shownChildrenArt.contains(childAF)) {
				logger.warn("artFrag already in collection: " + childAF);
				return;
			}
			ArtifactFragment oldParentAF = childAF.parentArt; 

			// the actual changes
	    	shownChildrenArt.add(childAF);												// [i]
	    	if (owningContainer(this)) {
	    		childAF.parentArt = this;												// [[ii]]
	    		if (oldParentAF != null) oldParentAF.shownChildrenArt.remove(childAF);	// [[iii]]
	    	}
		}

		// firing events
    	fireContentsChanged();															// [[i]]
		for (ArtifactFragment childAF : childrenAF) {
	    	if (owningContainer(this)) {
				ArtifactFragment oldParentAF = oldParentAFMap.get(childAF); 
	        	childAF.fireParentChanged();											// [[ii]]
	    		if (oldParentAF != null) oldParentAF.fireContentsChanged();				// [[iii]]
	    	}
		}
    }
	public void moveChild(ArtifactFragment child) {
		appendShownChild(child);
		//fireAttribChanged();
	}
	public void moveChildren(List<? extends ArtifactFragment> listAF) {
		appendShownChildren(listAF);
		//for (ArtifactFragment artFrag : listAF) {
		//	appendShownChild(artFrag);
		//}
		//fireAttribChanged();
	}
    public boolean removeShownChild(Object child) {
        boolean retVal = shownChildrenArt.remove(child);
    	if (owningContainer(this) && child instanceof ArtifactFragment)
    		((ArtifactFragment)child).parentArt = null;
        if (retVal == true) fireContentsChanged();
        return retVal; 
    }
	public void removeShownChildren(List<ArtifactFragment> childrenToRemove) {
		for (ArtifactFragment artifactFragment : childrenToRemove) {
			removeShownChild(artifactFragment);
		}
	}
    public void clearShownChildren() {
        shownChildrenArt.clear();
        fireContentsChanged();
    }

    public ArtifactFragment getShownChild(Object givenChild) {
    	Resource givenChildRes = null;
    	if (givenChild instanceof Artifact) givenChildRes = ((Artifact) givenChild).elementRes;

    	for (ArtifactFragment shownChild : shownChildrenArt) {
			if (shownChild.equals(givenChild)) return shownChild;
	    	if (shownChild.getArt().elementRes.equals(givenChildRes)) return shownChild;
		}
    	return null;
    }
    public ArtifactFragment getShownArtFrag(Object givenChild) {
		return getShownChild(givenChild);
    }

    private List<ArtifactFragment> getMatchingChildren(Artifact givenChild) {
    	if (givenChild == null) return Collections.emptyList();
    	
    	List<ArtifactFragment> matchedChildren = new ArrayList<ArtifactFragment>(5);
    	for (ArtifactFragment shownChildAF : shownChildrenArt) {
	    	if (shownChildAF.getArt().elementRes.equals(givenChild.elementRes)) matchedChildren.add(shownChildAF);
		}
    	return matchedChildren;
    }

	/**
	 * Find ArtFrag's that would point to the given Artifact. We return a list
	 * because we want to support duplicates.<br>
	 * <br>
	 * We intentionally don't have a getNestedShownChild(AF) because there
	 * should never be a need for it<br>
	 * this method (below) is needed for checking and adding nodes
	 */
    public List<ArtifactFragment> getMatchingNestedShownChildren(Artifact givenChild) {
    	List<ArtifactFragment> foundChildren = this.getMatchingChildren(givenChild);
    	if (!foundChildren.isEmpty()) return foundChildren;
    	
    	// we need to search in the right branch
    	Artifact tgtParent = getRootArt().getTopMostShowableParentBefore(this, givenChild);
    	if (tgtParent == givenChild) return Collections.emptyList();
    	List<ArtifactFragment> foundBranches = this.getMatchingChildren(tgtParent);
    	for (ArtifactFragment myChild : foundBranches) {
			foundChildren.addAll(myChild.getMatchingNestedShownChildren(givenChild));
		}
		return foundChildren;
    }

    public static List<ArtifactFragment> getAllNestedShownChildren(ArtifactFragment parent, List<ArtifactFragment> flatChildrenList) {
    	List<ArtifactFragment> children = parent.getShownChildren();
    	flatChildrenList.addAll(children);
    	for (ArtifactFragment child : children) {
    		getAllNestedShownChildren(child, flatChildrenList);
    	}
    	return flatChildrenList;
    }

	////////////////////
    // support for edges
	////////////////////
    protected List<ArtifactRel> srcRel = new ArrayList<ArtifactRel>();
    protected List<ArtifactRel> tgtRel = new ArrayList<ArtifactRel>();
    public List<ArtifactRel> getSourceConnections() {
        return srcRel;
    }
    public List<ArtifactRel> getShownSourceConnections() {
    	return getSourceConnections();
    }
    public boolean sourceConnectionsContains(ArtifactRel conn) {
    	boolean containsConn = false;
    	for (ArtifactRel rel : srcRel) {
			containsConn = containsConn || rel.equals(conn);
        }
        return containsConn;
        // rels may have different hashcodes
    	// return srcRel.contains(conn);
    }
    public void addSourceConnection(ArtifactRel conn) {
        if (srcRel.add(conn)) fireContentsChanged();
    }
    public void addSourceConnectionWithoutRefresh(ArtifactRel conn) {
    	srcRel.add(conn);
    }
    public void removeSourceConnection(ArtifactRel conn) {
        if (srcRel.remove(conn)) fireContentsChanged();
    }
    public List<ArtifactRel> getTargetConnections() {
        return tgtRel;
    }
    public List<ArtifactRel> getShownTargetConnections() {
    	return getTargetConnections();
    }
    public boolean targetConnectionsContains(Object conn) {
    	boolean containsConn = false;
    	for (ArtifactRel rel : tgtRel) {
			containsConn = containsConn || rel.equals(conn);
        }
        return containsConn;
     // rels may have different hashcodes
    	// return tgtRel.contains(conn);
    }
    public void addTargetConnection(ArtifactRel conn) {
    	if (tgtRel.add(conn)) fireContentsChanged();
    }
    public void addTargetConnectionWithoutRefresh(ArtifactRel conn) {
    	tgtRel.add(conn);
	}
    public void removeTargetConnection(ArtifactRel conn) {
    	if (tgtRel.remove(conn)) fireContentsChanged();
    }
    
    
	////////////////////
    // support for instances
	// - instances are optional and by default null
	// - instanceArt represent the particular instance of a class in a view
	////////////////////
	protected String instanceName = null;
	protected Resource instanceRes = null;
	protected void initInstanceRes() {
        if (instanceRes == null) instanceRes = StoreUtil.createBNode();
	}
    public Resource getInstanceRes() {
        initInstanceRes();
        return instanceRes;
    }
    public void setInstanceRes(Resource _instanceRes) {
        this.instanceRes = _instanceRes;
		firePropChang("instanceRes");
    }
	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String _instanceName) {
		if (instanceName != _instanceName) {
			instanceName = _instanceName;
			initInstanceRes();
			firePropChang(PROPERTY_EDIT_INSTANCE_NAME);
		}
	}

	public void writeRDF(RdfDocumentWriter rdfWriter, Resource parentInstance, List<ArtifactFragment> savedNodes, List<ArtifactRel> savedRels) throws IOException {
		
		// write me
		writeRDFNode(rdfWriter, parentInstance);
		
		// write connections
		for (ArtifactRel ar : getSourceConnections()) {
	        ar.writeRDF(rdfWriter, savedRels);
		}

		// write children
		for (ArtifactFragment childAF : getShownChildren()) {
			childAF.writeRDF(rdfWriter, getInstanceRes(), savedNodes, savedRels);
		}
		
		savedNodes.add(this);
	}

    ////////////// the below 3 methods need to be reviewed
    protected void writeRDFNode(RdfDocumentWriter rdfWriter, Resource parentInstance) throws IOException {
		ReloRdfRepository storeRepo = this.getRootArt().getRepo();

		// tie node to parent
    	rdfWriter.writeStatement(parentInstance, RSECore.contains, getInstanceRes());
    	
		// write node to the file
    	rdfWriter.writeStatement(getInstanceRes(), storeRepo.rdfType, RSECore.node);
        rdfWriter.writeStatement(getInstanceRes(), RSECore.model, getArt().elementRes);

        if(LoadUtils.outdatedCheckingOn) {
            // Add 'details' to enable opening the file in case the content has been deleted
        	// we should move to only store 'descriptional properties'
            Resource detailsNode =  storeRepo.createBNode();
        	rdfWriter.writeStatement(getInstanceRes(), RSECore.detailsNode, detailsNode);
        	StatementIterator iter =  storeRepo.getStatements(getArt().elementRes, null, null);
            while(iter.hasNext()) {
            	Statement stmt = iter.next();
            	rdfWriter.writeStatement(detailsNode, stmt.getPredicate(), stmt.getObject());
            }
            iter.close();
        }

        // write policies
    	for (DiagramPolicy diagPolicy : this.getDiagramPolicies()) {
    		try {
    			diagPolicy.writeRDF(rdfWriter);
    		} catch (Throwable t){
    			logger.error("Error while Saving, some data may be lost",t);
    		}
		}

//    	// Check if user created
//        if (RSECore.isUserCreated(storeRepo, getArt().elementRes)) {
//        	if (getArt().elementRes.toString().contains("com.architexa.diagrams.chrono.commands"))
//        		"".toCharArray();
//        	rdfWriter.writeStatement(getArt().elementRes, RSECore.userCreated, StoreUtil.createMemLiteral("true"));
//        }
        
        if(LoadUtils.outdatedCheckingOn) {
        	// Support for Error Makers - Review needed for below
            ReloRdfRepository fileStmts = ((RepositoryMgr)getRootArt().getRepo()).getFileRepo();
            StatementIterator fileStmtsIter =  storeRepo.getStatements(getArt().elementRes, null, null);
            while(fileStmtsIter.hasNext()) {
            	Statement stmt = fileStmtsIter.next();
            	fileStmts.startTransaction();
            	fileStmts.addStatement(getArt().elementRes, stmt.getPredicate(), stmt.getObject());
            	fileStmts.commitTransaction();
            }
        }
        
    	for (ArtifactFragment child : this.getShownChildren()) {
        	rdfWriter.writeStatement(getArt().elementRes, RSECore.contains, child.getArt().elementRes);
    	}
        
        	// Old method uses repo to get statements, this should not be necessary. nesting should be able to be determined from the model alone.
    	
//            StatementIterator iter =  storeRepo.getStatements(getArt().elementRes, RSECore.contains, null);
//            while(iter.hasNext()) {
//            	Statement stmt = iter.next();
//            	for (ArtifactFragment child : this.getShownChildren()) {
//            		if (stmt.getObject().equals(child.getArt().elementRes))
//            			rdfWriter.writeStatement(getArt().elementRes, RSECore.contains, stmt.getObject());
//            	}
//            }
	}

    public void readRDFNode(ReloRdfRepository queryRepo) {
    	// lets call the diagram policies
    	for (DiagramPolicy diagPolicy : this.getDiagramPolicies()) {
    		diagPolicy.readRDF(queryRepo);
		}
    }

    public static void readRDF(RootArtifact rootArt,
			ReloRdfRepository docRepo,
			Map<Resource, ArtifactFragment> instanceRes2AFMap,
			Resource instanceRes, ArtifactFragment parent) {
    	
        Resource modelRes = (Resource) docRepo.getStatement(instanceRes, RSECore.model, null).getObject();
        ReloRdfRepository storeRepo = rootArt.getRepo();
        Resource artType = (Resource) docRepo.getStatement(modelRes, docRepo.rdfType, null).getObject();
        Literal isUserCreated = (Literal) docRepo.getStatement(modelRes, RSECore.userCreated, null).getObject();
//        System.err.println("Adding Frags: " + modelRes + "\t" + isUserCreated + "\n"  + docRepo);
        if (RSECore.resourceWithoutWorkspace(modelRes).equals("") && 
        		!RSECore.commentType.equals(artType) && !RSECore.entityType.equals(artType) && isUserCreated == null) return;
        if (storeRepo instanceof RepositoryMgr) {
            readViewIntoCache(((RepositoryMgr)storeRepo).getFileRepo(), docRepo, instanceRes, modelRes); 
        }
        
//        Resource artType = (Resource) storeRepo.getStatement(modelRes, docRepo.rdfType, null).getObject();
        ArtifactFragment af = PluggableTypes.getAF(artType);
        if (af != null) {
            af.setArt(new Artifact(modelRes));
            // call getArtifact to ensure than DiagramPolicies are installed
            af = rootArt.getArtifact(af);
			if (af == null) return; //Strata Comments will be null since they are loaded in the RootEditPart
        } else {
        	// create the default ArtFrag
            af = rootArt.getArtifact(modelRes);
        }
        if (isUserCreated != null) {
//        	System.err.println("Reading " + modelRes);
        	Literal name = (Literal)docRepo.getStatement(modelRes, RSECore.userCreatedNameText, null).getObject();
        	af.setInstanceName(name.getLabel());
        	af.setEnclosingFrag(parent);
        }
        
		af.setInstanceRes(instanceRes);		
		
		// load comments and entiry rdf before creating EPs
		if (isCommentType(artType))
			af.readRDFNode(docRepo);
		
		rootArt.addVisibleArt(af, parent);
		instanceRes2AFMap.put(instanceRes, af);
		if (!isCommentType(artType))
			af.readRDFNode(docRepo);
		
		// load children
		readChildren(rootArt, docRepo, instanceRes2AFMap, instanceRes, af);
    	
    	// VS: we need to assert parenthood!!
		// This work without doing so since the parent is being created before
		// the child - but this will need to be fixed
    }

    private static boolean isCommentType(Resource artType) {
    	 return RSECore.commentType.equals(artType) || RSECore.entityType.equals(artType);
	}
	// Only used by user created frags
    public void setEnclosingFrag(ArtifactFragment parent) {    };
    
    public static void readChildren(RootArtifact rootArt,
			ReloRdfRepository docRepo,
			Map<Resource, ArtifactFragment> instanceRes2AFMap,
			Resource instanceRes, 
			ArtifactFragment parent) {
    	
    	StatementIterator childrenStmtsIt = docRepo.getStatements(instanceRes, RSECore.contains, null);
    	while (childrenStmtsIt.hasNext()) {
    		Resource childInstance = (Resource) childrenStmtsIt.next().getObject();
            ArtifactFragment.readRDF(rootArt, docRepo, instanceRes2AFMap, childInstance, parent);
    	}
    	childrenStmtsIt.close();
	}

    /**
     *  Put the file statements into a cached repo so that the diagram can be drawn the way it was saved
     * @param modelRes 
     */
    private static void readViewIntoCache(ReloRdfRepository cachedRepo, ReloRdfRepository docRepo, Resource instanceRes, Resource modelRes) {
    	// Trying to make statements off of the modelRes in the document to be
		// those that should have been in the rdf repository - such as is used
		// by comments derivedArtifacts for their modelResource but have their
		// types saved in the rdf-document
    	StatementIterator modelIter = docRepo.getStatements(modelRes, null, null);
    	cachedRepo.startTransaction();
    	while(modelIter.hasNext()) {
    		Statement stmt = modelIter.next();
    		cachedRepo.addStatement(modelRes, stmt.getPredicate(), stmt.getObject());
    	}
    	modelIter.close();
    	cachedRepo.commitTransaction();

        // Part 1 of 2: ...when model is the value of the statement, i.e. contained by 
        // (relationships are taken care of in ArtifactRelEditPart)
        StatementIterator modelContainedByIter = docRepo.getStatements(null, RSECore.contains, modelRes);
        cachedRepo.startTransaction();
        while(modelContainedByIter.hasNext()) {
        	Resource detailNode = modelContainedByIter.next().getSubject();
        	if(LoadUtils.outdatedCheckingOn) {
        		StatementIterator nodeIt = docRepo.getStatements(null, RSECore.detailsNode, detailNode);
        		if(nodeIt.hasNext()) {
        			Resource node = nodeIt.next().getSubject();
        			Resource model = (Resource) docRepo.getStatement(node, RSECore.model, null).getObject();
        			cachedRepo.addStatement(model, RSECore.contains, modelRes);
        		}
        	}
        }
        modelContainedByIter.close();
        cachedRepo.commitTransaction();
        
        // Part 2 of 2: ...when model is the subject of the statement, including: (i) contains, (ii) properties 
        // of the subject, and (iii) relationships from the subject (though these are not needed, but we just store them because we are lazy)
        if(LoadUtils.outdatedCheckingOn) {
        	Resource detailsNode = (Resource) docRepo.getStatement(instanceRes, RSECore.detailsNode, null).getObject();
        	if (detailsNode == null) return;        // document does not have details in it
        	StatementIterator modelDetailsIter = docRepo.getStatements(detailsNode, null, null);
        	cachedRepo.startTransaction();
        	while(modelDetailsIter.hasNext()) {
        		Statement stmt = modelDetailsIter.next();
        		cachedRepo.addStatement(modelRes, stmt.getPredicate(), stmt.getObject());
        	}
        	modelDetailsIter.close();
        	cachedRepo.commitTransaction();
        }
    }

    
    // support for diagram policies
    protected Map<Object, DiagramPolicy> policies = new HashMap<Object, DiagramPolicy> (2);

    public Collection<DiagramPolicy> getDiagramPolicies() {
    	return policies.values();
    }
	public static <T extends DiagramPolicy> void ensureInstalledPolicy(ArtifactFragment artFrag, String policyKey, Class<T> diagPolicyClass) {
	    if (artFrag != null && artFrag.getDiagramPolicy(policyKey) == null)
			try {
				artFrag.installDiagramPolicy(policyKey, diagPolicyClass.newInstance());
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
			logger.error("Policy " + key + " not installed on fragment. Artifact: " + getArt() + " Class: " + getClass());
			return null;
		}
    	return (T) policy;
    }
	
    protected void firePropChangeForAllChildren() {
		for (ArtifactFragment afChild : getShownChildren()) {
			afChild.firePropChangeForAllChildren();
		}
		fireThemeChanged();
	}
}
