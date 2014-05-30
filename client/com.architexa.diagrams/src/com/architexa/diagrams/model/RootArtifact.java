package com.architexa.diagrams.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.commands.AddNodeCommand;
import com.architexa.intro.AtxaIntroPlugin;
import com.architexa.intro.preferences.PreferenceConstants;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.RepositoryMgr;

/**
 * Because we can have alot of functionality in a RootArtifact - we are only
 * going to include things here that can't be elsewhere. Most 'business rules'
 * type of functionality will be in the browse model
 */
public abstract class RootArtifact extends DerivedArtifact {
    static final Logger logger = Activator.getLogger(RootArtifact.class);
    public static String PROPERTY_COMMENT_CHILDREN = "commentChildren"; 
    
	public RootArtifact() {
		// we are unique in that out parent artifact is null
		super(null);
		PreferenceConstants.loadPrefs();
		detailLvl = AtxaIntroPlugin.getDefault().getPreferenceStore().getInt(PreferenceConstants.LabelDetailLevelKey);
		this.setParentArt(this);
	}

	private List<Comment> commentChildren = new ArrayList<Comment>();

	public boolean addComment(Comment comment) {
		if (comment != null && commentChildren.add(comment)) {
			comment.setParentArt(this);
			firePropChang(PROPERTY_COMMENT_CHILDREN);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.architexa.diagrams.model.ArtifactFragment#appendShownChild(com.architexa.diagrams.model.ArtifactFragment)
	 * Explicitly checking for comment as the addVisibleArt may have comments in case of Relo which needs to be handled.
	 */
	@Override
	public void appendShownChild(ArtifactFragment child) {
		if (child instanceof Comment) {
			addComment((Comment) child);
			return;
		} else
			super.appendShownChild(child);
	}
		
	/**
	 * Method used to explicitly remove comments from the comment children list 
	 * @param comment
	 * @return
	 */
	public boolean removeShownChild(Comment comment) {
		if (commentChildren.contains(comment)) {
			commentChildren.remove(comment);
			firePropChang(PROPERTY_COMMENT_CHILDREN);
			return true;
		}
		return false;
	}

	public List<Comment> getCommentChildren() {
		return commentChildren;
	}
	
	@Override
	public Artifact getEnclosingArtifact() {
		logger.error("Root Document asked for artifact", new Throwable());
	    return null;
	}
	

	@Override
	public boolean containsChild(ArtifactFragment child) {
		if (child instanceof Comment)
			return hasComment((Comment) child);
		return super.containsChild(child);
	}

	private boolean hasComment(Comment child) {
		if (getCommentChildren().contains(child))
			return true;
		return false;
	}

	@Override
	public RootArtifact getRootArt() {
		return this;
	}
	
	
	private RepositoryMgr reloRdfRepository = new RepositoryMgr();
    public void setRepo(ReloRdfRepository rdfModel) {
//    	System.err.println("Setting repo: " + reloRdfRepository);
    	reloRdfRepository.setStoreRepo(rdfModel);
    }
    public RepositoryMgr getRepo() {
//    	System.err.println("Getting repo: " + reloRdfRepository);
        return reloRdfRepository;
    }
	
	private BrowseModel bm = null;
	
	// details can be 0:high, 1:medium, 2:low
	private int detailLvl;
	private int theme;
	
    public void setBrowseModel(BrowseModel _bm) {
    	bm = _bm;
    }
    public BrowseModel getBrowseModel() {
        return bm;
    }
	
	public ArtifactFragment getArtifact(Object src) {
		return bm.getArtifact(src);
	}
	
    public boolean artViewable(Artifact art) {
    	if (art == null) return false;
        Resource artType = art.queryType(getRepo());
        
        boolean isInitialized = art.isInitialized(getRepo());
        if (!isInitialized) return false;
        if (!bm.artifactLoadable(art, artType)) return false;
        return true;
    }
    /**
     * Being a container AF will remove the container if the contents are 
     * removed.
     * 
     * Relo returns true here for packages, rest return false
     */
    public boolean isContainerAF(ArtifactFragment artFrag) {
        Resource artType = artFrag.queryType(getRepo());
    	return bm.containerArtifact(artFrag.getArt(), artType);
    }

    abstract public boolean isLibCodeInDiagram();

    public Artifact getTopMostShowableParentBefore(ArtifactFragment parent, Artifact currChild) {
		Artifact queriedParent = currChild.queryParentArtifact(getRepo());
		// do different recursion if unindexed
		if (queriedParent == null || !artViewable(queriedParent)) {
			return getTopMostShowableUnindexedParentBefore(parent, currChild);
		}
		if (queriedParent == null 
				|| !artViewable(queriedParent)
				|| queriedParent.elementRes.equals(parent.getArt().elementRes)
				|| queriedParent.elementRes.equals(currChild.elementRes))
			return currChild;
		else
			return getTopMostShowableParentBefore(parent, queriedParent);
    }

	// need to do this recursion slightly differently when handeling items that
	// have not been indexed
    public Artifact getTopMostShowableUnindexedParentBefore(ArtifactFragment parent, Artifact currChild) {
		Resource parentRes = getBrowseModel().getParentRes(getArtifact(currChild), false, getRepo());
    	if(parentRes==null) return currChild;
    	Artifact queriedParent = getArtifact(parentRes).getArt();
		if (queriedParent == null 
				|| queriedParent.elementRes.equals(parent.getArt().elementRes)
				|| queriedParent.elementRes.equals(currChild.elementRes))
			return currChild;
		else
			return getTopMostShowableUnindexedParentBefore(parent, queriedParent);
    }
	/**
     * Finds or creates the parent ArtifactFragment of an "unavailable" code 
     * fragment, ie a piece of library code, code from a past team revision,
     * or a fragment created by the user via the palette
     */
    public ArtifactFragment getUnavailableCodeParent(CompoundCommand cc, 
    		ArtifactFragment child, Map<Artifact, ArtifactFragment> addedArtToAF) {

    	boolean isUserCreated = RSECore.isUserCreated(getRepo(), child.getArt().elementRes);
    	if(isUserCreated) {
    		if (child.getParentArt() != null)
    			return child.getParentArt();
    		ArtifactFragment parentFrag = getBrowseModel().getUserCreatedEnclosingFrag(child);
    		if(parentFrag!=null) {
    			// Know its parent, so just add to that. 

    			// Check that parent is in diagram and if not, add it
    			if(getMatchingNestedShownChildren(parentFrag.getArt()).isEmpty())
    				addVisibleArt(cc, parentFrag, addedArtToAF);

    			return parentFrag;
    		}
    	}

    	// Only determine parent if frag is user created or if frag is
    	// lib code and user has preference set to show lib code in diagram
    	if(!(isUserCreated || isLibCodeInDiagram())) return null;

    	ArtifactFragment parentArt = child.getParentArt();
    	if(parentArt!=null) return parentArt; // If we know its parent, just add to that. 

    	// Otherwise, create parent from rdf Resource
    	Resource parentRes = getBrowseModel().getParentRes(child, isUserCreated, getRepo());
    	if(parentRes==null) return null;
    	ArtifactFragment parentFrag = getArtifact(parentRes);

    	// check if parent is already in the diagram or is queued to be added
    	if (stringHasBeenAdded(parentRes, addedArtToAF))
    		return addedArtToAF.get(parentFrag.getArt());
    	
    	for (ArtifactFragment af : getMatchingNestedShownChildren(parentFrag.getArt())) {
    		if (af.getArt().elementRes.toString().equals(parentRes.toString()))
    			return af;
    	}
    	
    	if(parentFrag!=null) addVisibleArt(cc, parentFrag, addedArtToAF); // add parent to diagram
    	return parentFrag;
    }

    // used by readRDF. Is shared and used by strata. Ignored by Relo
    public ArtifactFragment addVisibleArt(ArtifactFragment child, ArtifactFragment parent) {
    	return addVisibleArt(child);
    }
    
	public ArtifactFragment addVisibleArt(ArtifactFragment child) {
		CompoundCommand cc = new CompoundCommand();
		// Using the otheraddVisibleArt method so we can determine the correct parents of non-indexed items from their resource
		// AddNodeCommand addNodeCmd = addVisibleArt(cc, child);
		
		Map<Artifact, ArtifactFragment> addedArtToAF = new HashMap<Artifact, ArtifactFragment>();
		AddNodeCommand addNodeCmd = addVisibleArt(cc, child, addedArtToAF );
		cc.execute();
		return addNodeCmd.getNewArtFrag();
	}
	
	public ArtifactFragment addVisibleArt(Artifact child) {
		CompoundCommand cc = new CompoundCommand();
		AddNodeCommand addNodeCmd = addVisibleArt(cc, child);
		cc.execute();
		return addNodeCmd.getNewArtFrag();
	} 
   
	// we will add this child if it has not been added
    public AddNodeCommand addVisibleArt(CompoundCommand cc, Artifact child, Map<Artifact, ArtifactFragment> addedArtToAF) {
    	if ( addedArtToAF == null) addedArtToAF = new HashMap<Artifact, ArtifactFragment>();
    	// check if child was already added by this compound command
    	if (addedArtToAF.containsKey(child)) return new AddNodeCommand(getRootArt(),null, addedArtToAF.get(child)); 
    	
    	// check if item exists in the diagram 1 or more times, if so add to the first occurence
    	List <ArtifactFragment> foundChildren = getMatchingNestedShownChildren(child);
    	if (!foundChildren.isEmpty()) return new AddNodeCommand(getRootArt(),null, foundChildren.get(0));
    	
    	// it doesn't so just call the below method
    	ArtifactFragment childAF = getArtifact(child);
    	addedArtToAF.put(child, childAF);
    	return addVisibleArt(cc, childAF, addedArtToAF);
    }
    public AddNodeCommand addVisibleArt(CompoundCommand cc, ArtifactFragment child, Map<Artifact, ArtifactFragment> addedArtToAF) {

    	// Adding a item that is not indexed (library code) 
    	if (!child.getArt().isInitialized(getRepo())) {
//    		System.err.println("New Child : " + child + "\n");
    		ArtifactFragment parentArt = getUnavailableCodeParent(cc, child, addedArtToAF);
    		if(parentArt!=null) {
    			addedArtToAF.put(child.getArt(), child);
    			AddNodeCommand addedCmd = new AddNodeCommand(getRootArt(), parentArt, child);
    			cc.add(addedCmd);
    			return addedCmd;
    		}
    	}

    	// I. Find parent
    	//System.err.println("adding: " + child);
    	Artifact parent = child.getArt().queryParentArtifact(getRepo());
    	ArtifactFragment parentAF = null;
		if (parent == null || !artViewable(parent))
			parentAF = this;
		else if (addedArtToAF.containsKey(parent)) {
			parentAF = addedArtToAF.get(parent);
		} else {
			List<ArtifactFragment> foundParents = getMatchingNestedShownChildren(parent);
			if (!foundParents.isEmpty()) parentAF = foundParents.get(0);
		}
   	
    	// II. Deal with the case when parent can be shown but is not shown
    	AddNodeCommand parentAFGC = null;
    	if (parentAF == null) {
    		parentAFGC = addVisibleArt(cc, parent, addedArtToAF);
    	}

    	// III. Add child to parent
		AddNodeCommand addedCmd = null;
		if (parentAF != null) {
			addedCmd = new AddNodeCommand(getRootArt(), parentAF, child);
			cc.add(addedCmd);
		} else if (parentAFGC != null) {
			addedCmd = new AddNodeCommand(getRootArt(), parentAFGC.getNewArtFrag(), child);
			cc.add(addedCmd);
		}
    	return addedCmd;
	}
    
    //used to check if un indexed items are in list of items that are being added
    private boolean stringHasBeenAdded(Resource parentRes, Map<Artifact, ArtifactFragment> addedArtToAF) {
		for (Artifact a : addedArtToAF.keySet()) {
			if (parentRes.toString().equals(a.elementRes.toString()))
				return true;
		}
		return false;
	}

    // XXX: Examine for removal   (Only used by CreateParentCommand and expand collapse buttons
	// we will add this child if it has not been added  
    public AddNodeCommand addVisibleArt(CompoundCommand cc, Artifact child) {
    	// check if child already exists
    	List <ArtifactFragment> foundChildren = getMatchingNestedShownChildren(child);
    	if (!foundChildren.isEmpty()) return new AddNodeCommand(getRootArt(),null, foundChildren.get(0));
    	
    	// it doesn't so just call the below method
    	ArtifactFragment childAF = getArtifact(child);
    	return addVisibleArt(cc, childAF);
    }
    // XXX: Examine for removal
    // we are definitely adding the given child
    //  if you want to see if the child already exists use: getNestedShownChild
    public AddNodeCommand  addVisibleArt(CompoundCommand cc, ArtifactFragment child ) {
    	// I. Find parent
    	//System.err.println("adding: " + child);
    	Artifact parent = child.getArt().queryParentArtifact(getRepo());
    	ArtifactFragment parentAF = null;
		if (parent == null || !artViewable(parent))
			parentAF = this;
		else {
			List<ArtifactFragment> foundParents = getMatchingNestedShownChildren(parent);
			if (!foundParents.isEmpty()) parentAF = foundParents.get(0);
		}
   	
    	// II. Deal with the case when parent can be shown but is not shown
    	AddNodeCommand parentAFGC = null;
    	if (parentAF == null) {
    		parentAFGC = addVisibleArt(cc, parent);
    	}

    	// III. Add child to parent
		AddNodeCommand addedCmd = null;
		if (parentAF != null) {
			addedCmd = new AddNodeCommand(getRootArt(), parentAF, child);
			cc.add(addedCmd);
		} else if (parentAFGC != null) {
			addedCmd = new AddNodeCommand(getRootArt(), parentAFGC.getNewArtFrag(), child);
			cc.add(addedCmd);
		}
    	return addedCmd;
    }
    
	public void removeVisibleArt(ArtifactFragment artFrag) {
		ArtifactFragment parentAF = artFrag.getParentArt();

		// remove node
		parentAF.removeShownChild(artFrag);
		
		// remove containers if they are empty
		if (isContainerAF(parentAF) && parentAF.getShownChildren().isEmpty() && parentAF.getParentArt()!= null) {
			removeVisibleArt(parentAF);
		}
	}
	public ArtifactRel addRel(Artifact srcArt, URI property, Artifact dstArt) {
		ArtifactFragment dstArtFrag = getMatchingNestedShownChildren(dstArt).get(0);
		ArtifactFragment srcArtFrag = getMatchingNestedShownChildren(srcArt).get(0);
		return addRel(srcArtFrag, property, dstArtFrag);
	}
	public static ArtifactRel addRel(ArtifactFragment srcArtFrag, URI property, ArtifactFragment dstArtFrag) {
		ArtifactRel rel = new ArtifactRel(srcArtFrag, dstArtFrag, property);
		if (srcArtFrag.sourceConnectionsContains(rel) || dstArtFrag.targetConnectionsContains(rel))
			return null;

		srcArtFrag.addSourceConnection(rel);
		dstArtFrag.addTargetConnection(rel);
		return rel;	
	}

	public static void hideRel(ArtifactRel rel) {
		if (!rel.getSrc().sourceConnectionsContains(rel) || !rel.getDest().targetConnectionsContains(rel)) {
			logger.info("ERR: Relationship not found during removal", new Exception());
			return;
		}
		rel.getSrc().removeSourceConnection(rel);
		rel.getDest().removeTargetConnection(rel);
	}

	public void setDetailLevel(int detailLvl) {
		this.detailLvl = detailLvl;
		firePropChangeForAllChildren();
	}

	public void setColorTheme(int theme) {
		this.theme = theme;
		firePropChangeForAllChildren();
	}
	public int getColorTheme() {
		return this.theme;
	}
	
	public int getDetailLevel() {
		return detailLvl;
	}

}
