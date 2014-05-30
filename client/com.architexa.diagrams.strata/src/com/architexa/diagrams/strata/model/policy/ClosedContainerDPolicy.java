package com.architexa.diagrams.strata.model.policy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CUSupport;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.DiagramPolicy;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.commands.AddChildrenCommand;
import com.architexa.diagrams.strata.commands.ModelUtils;
import com.architexa.diagrams.strata.commands.RedoAddedChildrenCommand;
import com.architexa.diagrams.strata.commands.ShowChildrenCommand;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


// @tag move-to-sgef: likely both Relo and Chrono want this functionality - 
//   and we are asking the user to call us for getShownChildren
public class ClosedContainerDPolicy extends DiagramPolicy {
	// //
	// basic setup
	////
	static final Logger logger = StrataPlugin.getLogger(ClosedContainerDPolicy.class);
	public static final String DefaultKey = "ClosedContainerDPolicy";
	public static final ClosedContainerDPolicy Type = new ClosedContainerDPolicy();

	public static final URI containerState = RSECore.createRseUri("core#containerState");
	public static final URI breakableURI = RSECore.createRseUri("core#containerState");
	public static final URI userHiddenURI = RSECore.createRseUri("core#containerState");
	
	////
	// Policy Fields, Constructors and Methods 
	////
	private boolean showingChildren = false;
	private boolean breakable = true;
	private boolean userHidden = false;

	public ClosedContainerDPolicy() {
	}

	public void setShowingChildren(boolean _showingChildren) {
		showingChildren = _showingChildren;
		getHostAF().fireContentsChanged();
	}
	public void setBreakable(boolean _breakable) {
		breakable = _breakable;
	}
	public void setUserHidden(boolean _userHidden) {
		userHidden  = _userHidden;
	}
	public void hideChildren(CompoundCommand tgtCmd) {
		setUserHidden(true);
		tgtCmd.add(new ShowChildrenCommand(getHostAF(), "Hide Children", false, (StrataRootDoc) getHostAF().getRootArt()));
	}
	public void queryAndShowChildren(CompoundCommand tgtCmd) {
		if (getHostAF().getShownChildrenCnt() <= 0) {
			//do not auto break classes
			ReloRdfRepository repo = ((StrataRootDoc) getHostAF().getRootArt()).getRepo();
			if(getHostAF().getArt().queryWarnedType(repo) != null && getHostAF().getArt().queryWarnedType(repo).equals(RJCore.classType)   )
				setBreakable(false);

			List<ArtifactFragment> theChildren = queriedChildren();
			// do not add/show if there is no classes to show
			boolean containsClass = false;
			if (theChildren == null) return;
			for (ArtifactFragment childAF : new ArrayList<ArtifactFragment>(theChildren)) {
				Resource childType = childAF.getArt().queryWarnedType(repo);
				if (childAF == null || childType == null) continue;
				
				
				Resource s = childAF.queryType(repo);
				if (CUSupport.isGraphNode(s, childAF.getArt(), repo) || CUSupport.isPackage(s) || CUSupport.isPackageFolder(s))
					containsClass = true; 
				else theChildren.remove(childAF);
			}
			if (!containsClass) {
				tgtCmd = null;
				return;
			}
			
			// we should not break if we are opening a package with only one class
			if (theChildren.size() == 1)
				ClosedContainerDPolicy.setBreakable(getHostAF(), false );
			
			tgtCmd.add(new AddChildrenCommand(theChildren, getHostAF(), (StrataRootDoc) getHostAF().getRootArt()));
		}else
			tgtCmd.add(new RedoAddedChildrenCommand(getHostAF(), (StrataRootDoc) getHostAF().getRootArt()));
		showChildren(tgtCmd, (StrataRootDoc) getHostAF().getRootArt());
	}
	
	private void addAndShowChild(CompoundCommand tgtCmd, ArtifactFragment child, StrataRootDoc rootDoc, Layer layer, int newLayerChildNdx, boolean showChildren) {
		List<ArtifactFragment> theChildren = new ArrayList<ArtifactFragment>();
		theChildren.add(child);
		ReloRdfRepository repo = rootDoc.getRepo();
		if ( !(getHostAF() instanceof StrataRootDoc) && getHostAF().getArt().queryWarnedType(repo) != null && getHostAF().getArt().queryWarnedType(repo).equals(RJCore.classType))
			setBreakable(false);

		tgtCmd.add(new AddChildrenCommand(theChildren, getHostAF(), rootDoc, layer, newLayerChildNdx));
		// here when adding to the root we try to show the first child. this
		// should be done automatically by the auto commands if necessary
		// if (getHostAF() == rootDoc)
		//	 tgtCmd.add(new ShowChildrenCommand(theChildren.get(0), "Show Children", rootDoc));
		if (showChildren)
			showChildren(tgtCmd, rootDoc);
	}
	private void addAndShowChild(CompoundCommand tgtCmd, ArtifactFragment child, StrataRootDoc rootDoc, Layer layer, int newLayerChildNdx) {
		addAndShowChild(tgtCmd, child, rootDoc, layer, newLayerChildNdx,true);
	}


	
	public void showChildren(CompoundCommand tgtCmd, StrataRootDoc strataRootDoc) {
		tgtCmd.add(new ShowChildrenCommand(getHostAF(), "Show Children", strataRootDoc));
	}

	// @tag design-issue: we sometimes change the identity of the object here,
    // so callers need to unregister and register before calling us, not sure
    // what the right solutions is. ... I think the model should not change, but
    // the effective model can change, since in the cases that the model is
    // really changing the items are represent the same thing <-- to implement
	private List<ArtifactFragment> queriedChildren() {
		StrataRootDoc rootDoc = (StrataRootDoc) getHostAF().getRootArt();
		Artifact pckgArt = getHostAF().getArt();
		List<Artifact> pckgChildren = ModelUtils.queryChldren(pckgArt, rootDoc.getRepo());
		
		if (pckgChildren.size() == 0) return null;
		getHostAF().setArt(rootDoc.createArtFrag(pckgArt.elementRes).getArt());
		
		List<ArtifactFragment> itemsToAdd = new ArrayList<ArtifactFragment> (pckgChildren.size());
		for (Artifact pckgChildArt : pckgChildren) {
            ArtifactFragment pckgChildAF = rootDoc.createArtFrag(pckgChildArt.elementRes);
            if (!getHostAF().containsChild(pckgChildAF)) itemsToAdd.add(pckgChildAF);
		}
		return itemsToAdd;
	}

	private static final List<ArtifactFragment> emptyArtFragList = new ArrayList<ArtifactFragment> ();
	
	public List<ArtifactFragment> getShownChildren() {
		if (!showingChildren)
			return emptyArtFragList;
		else {
			return getHostAF().getShownChildren();
		}
	}
	public List<ArtifactFragment> getNestedShownChildren() {
		List<ArtifactFragment> shownAFList = new ArrayList<ArtifactFragment> ();
		getNestedShownChildren(getHostAF(), shownAFList);
		return shownAFList;
	}
	public void getNestedShownChildren(ArtifactFragment currAF, List<ArtifactFragment> shownAFList) {
		shownAFList.add(currAF);
		if (ClosedContainerDPolicy.isShowingChildren(currAF)) {
			for (ArtifactFragment childAF : ClosedContainerDPolicy.getShownChildren(currAF)) {
				getNestedShownChildren(childAF, shownAFList);
			}
		}
	}
	
	// @tag implement-for-correct-saving-support
	@Override
	public void writeRDF(RdfDocumentWriter rdfWriter) throws IOException {
			rdfWriter.writeStatement(getHostAF().getInstanceRes(), containerState, StoreUtil.createMemLiteral(Boolean.toString(showingChildren)));
			rdfWriter.writeStatement(getHostAF().getInstanceRes(), breakableURI, StoreUtil.createMemLiteral(Boolean.toString(breakable)));
			rdfWriter.writeStatement(getHostAF().getInstanceRes(), userHiddenURI, StoreUtil.createMemLiteral(Boolean.toString(userHidden)));
	}
	@Override
	public void readRDF(ReloRdfRepository queryRepo) {
		Value showingChildren = queryRepo.getStatement(getHostAF().getInstanceRes(), containerState, null).getObject();
		if (showingChildren != null) {
			this.showingChildren = Boolean.parseBoolean(((Literal)showingChildren).getLabel());
		}
		Value breakable = queryRepo.getStatement(getHostAF().getInstanceRes(), breakableURI, null).getObject();
		if (showingChildren != null) {
			this.breakable = Boolean.parseBoolean(((Literal)breakable).getLabel());
		}
		Value userHidden = queryRepo.getStatement(getHostAF().getInstanceRes(), userHiddenURI, null).getObject();
		if (showingChildren != null) {
			this.userHidden = Boolean.parseBoolean(((Literal)userHidden).getLabel());
		}
	}


	////
	// Static Helpers 
	////
	public static boolean isInstalled(ArtifactFragment artFrag) {
		if (artFrag.getDiagramPolicy(DefaultKey) != null) return true;
		return false;
	}
	public static boolean isBreakable(ArtifactFragment artFrag) {
		ClosedContainerDPolicy closedContainerDPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			return closedContainerDPolicy.breakable;
		else
			return true;
	}
	public static boolean isUserHidden(ArtifactFragment artFrag) {
		ClosedContainerDPolicy closedContainerDPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			return closedContainerDPolicy.userHidden;
		else
			return true;
	}
	
	//	determines if children should be displayed (including directed connections) ?
	public static boolean isShowingChildren(ArtifactFragment artFrag) {
		ClosedContainerDPolicy closedContainerDPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			return closedContainerDPolicy.showingChildren;
		else
			return false;
	}
	
	public static void setAllParentsShowingChildren(ArtifactFragment artFrag, boolean _showingChildren) {
		setShowingChildren(artFrag, _showingChildren);
		if (artFrag.getParentArt()!=null) setAllParentsShowingChildren(artFrag.getParentArt(), _showingChildren);
	}
	//never runs? not needed?
	public static void setShowingChildren(ArtifactFragment artFrag, boolean _showingChildren) {
		ClosedContainerDPolicy closedContainerDPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			closedContainerDPolicy.setShowingChildren(_showingChildren);
	}
	//runs on open command - when closing a package?
	public static void hideChildren(CompoundCommand tgtCmd, ArtifactFragment artFrag) {
		ClosedContainerDPolicy closedContainerDPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			closedContainerDPolicy.hideChildren(tgtCmd);
	}
	//runs on Open Command - when opening a package?
	public static void queryAndShowChildren(CompoundCommand tgtCmd, ArtifactFragment artFrag) {
		ClosedContainerDPolicy closedContainerDPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			closedContainerDPolicy.queryAndShowChildren(tgtCmd);
	}
	//runs when showing the child of a class?
	public static void addAndShowChild(CompoundCommand tgtCmd, ArtifactFragment parentFrag, ArtifactFragment childFrag) {
		ClosedContainerDPolicy closedContainerDPolicy = parentFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			closedContainerDPolicy.addAndShowChild(tgtCmd, childFrag, (StrataRootDoc) parentFrag.getRootArt(), null, -1);
	}
	public static void addAndShowChild(CompoundCommand tgtCmd, ArtifactFragment parentFrag, ArtifactFragment childFrag, StrataRootDoc rootDoc) {
		ClosedContainerDPolicy closedContainerDPolicy = parentFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			closedContainerDPolicy.addAndShowChild(tgtCmd, childFrag, rootDoc, null, -1);
	}
	public static void addAndShowChild(CompoundCommand tgtCmd, ArtifactFragment parentFrag, ArtifactFragment childFrag, StrataRootDoc rootDoc, Layer layer, int newLayerChildNdx) {
		ClosedContainerDPolicy closedContainerDPolicy = parentFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			closedContainerDPolicy.addAndShowChild(tgtCmd, childFrag, rootDoc, layer, newLayerChildNdx);
	}
	public static void addAndShowChild(CompoundCommand tgtCmd, ArtifactFragment parentFrag, ArtifactFragment childFrag, StrataRootDoc rootDoc, boolean showChildren) {
		ClosedContainerDPolicy closedContainerDPolicy = parentFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			closedContainerDPolicy.addAndShowChild(tgtCmd, childFrag, rootDoc, null, -1, showChildren);
	}
	
	//runs on show in diagram?
	public static void showChildren(CompoundCommand tgtCmd, ArtifactFragment artFrag, StrataRootDoc strataRootDoc) {
		ClosedContainerDPolicy closedContainerDPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			closedContainerDPolicy.showChildren(tgtCmd, strataRootDoc);
	}
	//runs for each child shown on every(?) action?
	public static List<ArtifactFragment> getShownChildren(ArtifactFragment artFrag) {
		ClosedContainerDPolicy closedContainerDPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			return closedContainerDPolicy.getShownChildren();
		return new ArrayList<ArtifactFragment>();
	}
	//runs when opening packages to show children?
	public static List<ArtifactFragment> getNestedShownChildren(ArtifactFragment artFrag) {
		ClosedContainerDPolicy closedContainerDPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			return closedContainerDPolicy.getNestedShownChildren();
		return new ArrayList<ArtifactFragment>();
	}

	public static List<Comment> getCommentChildren(ArtifactFragment artFrag){
		ClosedContainerDPolicy closedContainerDPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			return closedContainerDPolicy.getCommentChildren();
		return new ArrayList<Comment>();
	}

	private List<Comment> getCommentChildren() {
		if(getHostAF() instanceof RootArtifact)
			return ((RootArtifact)getHostAF()).getCommentChildren();

		return null;
	}
	
	public static void setBreakable(ArtifactFragment artFrag, boolean _breakable){
		ClosedContainerDPolicy closedContainerDPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			closedContainerDPolicy.setBreakable(_breakable);
	}

	public static void showAllSingleChildren(CompoundCommand tgtCmd, ArtifactFragment artFrag) {
		ClosedContainerDPolicy closedContainerDPolicy = artFrag.getTypedDiagramPolicy(Type, DefaultKey);
		if (closedContainerDPolicy != null)
			closedContainerDPolicy.showAllSingleChildren(tgtCmd);
	}

	private void showAllSingleChildren(CompoundCommand tgtCmd) {
		for (ArtifactFragment childAF : getHostAF().getShownChildren()) {
			showSingleChildren(childAF, tgtCmd);	
		}
	}
	private void showSingleChildren(ArtifactFragment af, CompoundCommand tgtCmd) {
		if (af == null) return;
		if (af.getShownChildrenCnt() !=1 ) return;
		for (ArtifactFragment childAF : af.getShownChildren()) {
			showSingleChildren(childAF, tgtCmd);
		}
		showChildren(tgtCmd, af, (StrataRootDoc) af.getRootArt());
	}
}
