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
 * Created on Jun 12, 2004
 *
 */
package com.architexa.diagrams.relo.parts;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.InstanceofPredicate;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.draw2d.NonEmptyFigure;
import com.architexa.diagrams.draw2d.NonEmptyFigureSupport;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.DerivedArtifact;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.parts.ArtifactRelEditPart;
import com.architexa.diagrams.parts.BasicRootController;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.parts.IRSERootEditPart;
import com.architexa.diagrams.parts.NavAidsEditPart;
import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.relo.agent.ReloBrowseModel;
import com.architexa.diagrams.relo.agent.ScriptManager;
import com.architexa.diagrams.relo.commands.LoadCommand;
import com.architexa.diagrams.relo.graph.GraphLayoutManager;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.relo.ui.ReloEditor;
import com.architexa.diagrams.relo.ui.ReloView;
import com.architexa.diagrams.utils.RelUtils;
import com.architexa.intro.preferences.LibraryPreferences;
import com.architexa.org.eclipse.draw2d.ConnectionLayer;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.FreeformLayer;
import com.architexa.org.eclipse.draw2d.GraphAnimation;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.gef.DefaultEditDomain;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CommandStackListener;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.ui.parts.GraphicalView.ViewEditDomain;
import com.architexa.rse.BuildStatus;
import com.architexa.store.ReloRdfRepository;


/**
 * @author vineet
 *
 */
public class ReloController extends ArtifactEditPart implements BasicRootController, IRSERootEditPart {
    
	static final Logger logger = ReloPlugin.getLogger(ReloController.class);

	public ReloBrowseModel bm = null;

	private IFigure feedbackLayer;

    public ReloController() {
        //ConsoleView.setVariable("rc", this);
    }
    
    @Override
    public ReloBrowseModel getBrowseModel() {
        return bm;
    }
    
    public RootArtifact getRootArtifact() {
    	return (RootArtifact) this.getModel();
    }

    public ReloDoc getReloDoc() {
    	return (ReloDoc) this.getModel();
    }

    @Override
    public void setParent(EditPart parent) {
    	super.setParent(parent);
    	
        EditDomain editDomain = getRoot().getViewer().getEditDomain();
        if (editDomain instanceof DefaultEditDomain) {
            ReloEditor editor = (ReloEditor) ((DefaultEditDomain)editDomain).getEditorPart();
            bm = (ReloBrowseModel) editor.getRootModel().getBrowseModel();
        } else if (editDomain instanceof ViewEditDomain) {
            ReloView view = (ReloView) ((ViewEditDomain)editDomain).getViewPart();
            bm = (ReloBrowseModel) view.getRootModel().getBrowseModel();
        } else {
        	logger.error("Unable to understand EditDomain - type: " + editDomain.getClass(), new Exception());
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
    protected IFigure createFigure() {
		Figure f = new FreeformLayer() {
			@Override
            public String toString() {
				return "{ReloController.createFigure()->Figure}";
			}
		};
		f.setOpaque(true);
		f.setLayoutManager(new GraphLayoutManager(this, getRoot(), ScriptManager.selection));
		IFigure label = new Label(" \n Drag packages, classes, or methods from the Package Explorer " +
				"into the diagram to explore and understand code." +
				"\n Or, use the expandable Palette on the right side of the diagram " +
				"to design.");
		this.nonEmptyFigure = new NonEmptyFigure(label);
		NonEmptyFigureSupport.instructionHighlight(label, 10);

		feedbackLayer = getLayer(LayerConstants.FEEDBACK_LAYER);
		feedbackLayer.setLayoutManager(new FlowLayout(false)); 
		
		feedbackLayer.add(this.nonEmptyFigure);
		
		return f;
	}
	private NonEmptyFigure nonEmptyFigure;
	private Label warning;

	public void addUnbuiltWarning() {
		warning = new Label("");
		warning.setLayoutManager(new ToolbarLayout());
		NonEmptyFigureSupport.instructionHighlight(warning, 10);
		feedbackLayer.add(this.warning);
		warning.setText(" \n  Some elements in this diagram may not have been indexed." +
				"\n  To update your index, go to the menu: Architexa->Update Indexes");
	}
	
	@Override
	protected IFigure createFigure(IFigure curFig, int newDL) {
		// we implement createFigure() so we don't need this here
		return null;
	}
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();

		if ((getViewer().getControl().getStyle() & SWT.MIRRORED ) == 0)
	    	((ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER)).setAntialias(SWT.ON);
	}
    
    public void performUndoableLayout() {
        IFigure f = this.getFigure();
        GraphLayoutManager glm = (GraphLayoutManager) f.getLayoutManager();
        glm.undoableLayout();
    }
    public GraphLayoutManager getLayoutMgr() {
        return (GraphLayoutManager) this.getFigure().getLayoutManager();
    }
    public Command getLayoutCmd() {
        return getLayoutMgr().getLayoutCmd();
    }
    
    /**
     * Returns the editor part for this controller
     * 
     */
    public ReloEditor getReloEditor() {
        EditDomain editDomain = getRoot().getViewer().getEditDomain();
        if (editDomain instanceof DefaultEditDomain) {
            return (ReloEditor) ((DefaultEditDomain)editDomain).getEditorPart();
        } else {
        	// caller needs to support when we are not opening in an editor
        	return null;
        }
    }
    
    /**
     * Returns the file for the editor part of this controller
     * 
     */
    public IResource getReloEditorResource() {
    	ReloEditor editor = getReloEditor();
    	if (editor == null) return null;
    	
		IEditorInput input = editor.getEditorInput();
    	if (input instanceof FileEditorInput) {
    		return ((FileEditorInput)input).getFile();
    	}
    	return null;
    }

    // XXX  Examine for Removal - Called by outdated command's undos
    public void hideRel(AbstractReloRelationPart relEP) {
        ArtifactRel rel = (ArtifactRel) relEP.getModel();

        ArtifactEditPart srcAEP = (ArtifactEditPart) relEP.getSource();
        if (srcAEP != null && srcAEP.isActive()) RelUtils.removeModelSourceConnections(srcAEP.getArtifact(), rel);

        ArtifactEditPart dstAEP = (ArtifactEditPart) relEP.getTarget();
        if (dstAEP != null && dstAEP.isActive()) RelUtils.removeModelTargetConnections(dstAEP.getArtifact(), rel);

        logger.debug(rel.tag + " Hiding " + rel.relationRes + ": " + rel.toString());
    }
	// called by AddNodeAndRelCmd undo, Delete Command, and Hide Action for Relationships
	public void hideRel(ArtifactRel rel) {
		RelUtils.removeModelSourceConnections(rel.getSrc(), rel);
		RelUtils.removeModelTargetConnections(rel.getDest(), rel);

        logger.debug(rel.tag + " Hiding " + rel.relationRes + ": " + rel.toString());
	}

    // AddRel Functionality moved to ReloDoc
	
//    public void addRel(ArtifactFragment srcArtFrag, ArtifactRel rel, ArtifactFragment dstArtFrag) {
//    	ModelControllerManager.addModelSourceConnections(srcArtFrag, rel);
//    	ModelControllerManager.addModelTargetConnections(dstArtFrag, rel);
//    }
    public ArtifactRel addRel(ArtifactFragment srcArtFrag, URI relationRes, ArtifactFragment dstArtFrag) {
		RootArtifact.addRel(srcArtFrag, relationRes, dstArtFrag);
    	ArtifactRel rel = new ArtifactRel(srcArtFrag, dstArtFrag, relationRes);
//        addRel(srcArtFrag, rel, dstArtFrag);
        return rel;
    }
    public ReloArtifactRelEditPart addRel(ArtifactEditPart srcAEP, URI relationRes, ArtifactEditPart dstAEP) {
		RootArtifact.addRel(srcAEP.getArtifact(), relationRes, dstAEP.getArtifact());
    	ArtifactRel rel = new ArtifactRel(srcAEP.getArtifact(), dstAEP.getArtifact(), relationRes);
//        addRel(srcAEP.getArtifact(), rel, dstAEP.getArtifact());
        return this.findEditPart(rel);
    }
    public ArtifactRel addRel(Artifact srcArt, URI relationRes, Artifact dstArt) {
		ArtifactFragment srcAF = createOrFindArtifactEditPart(srcArt).getArtifact();
		ArtifactFragment dstAF = createOrFindArtifactEditPart(dstArt).getArtifact();
		return addRel(srcAF, relationRes, dstAF);
	}

	
    // XXX Examine for Removal
	public void reConnectRel(ArtifactEditPart newSrcAEP, ReloArtifactRelEditPart relEP, ArtifactEditPart newDstAEP) {
        ArtifactEditPart oldSrcAEP = (ArtifactEditPart) relEP.getSource();
        ArtifactEditPart oldDstAEP = (ArtifactEditPart) relEP.getTarget();
        if (newSrcAEP != oldSrcAEP) {
        	RelUtils.removeModelSourceConnections(oldSrcAEP.getArtifact(), relEP.getArtifactRel());
        	RelUtils.addModelSourceConnections(newSrcAEP.getArtifact(), relEP.getArtifactRel());
        }
        if (newDstAEP != oldDstAEP) {
        	RelUtils.removeModelTargetConnections(oldDstAEP.getArtifact(), relEP.getArtifactRel());
        	RelUtils.addModelTargetConnections(newDstAEP.getArtifact(), relEP.getArtifactRel());
        }
        //ArtifactRel rel = relEP.getArtifactRel();
        //logger.debug(rel.tag + " Creating " + rel.relationRes + ": " + rel.toString());
    }

	
	public ArtifactEditPart createOrFindArtifactEditPart(Resource res) {
        return createOrFindArtifactEditPart(new Artifact(res));
    }
    public ArtifactEditPart findArtifactEditPart(ArtifactFragment af) {
        return (ArtifactEditPart) findEditPart(af);
    }
    public ArtifactEditPart findArtifactEditPart(Artifact art) {
    	return (ArtifactEditPart) findEditPart(art);
    }
	@Override
	public AbstractGraphicalEditPart findEditPart(Object model) {
		if (model instanceof Artifact) {
			List<ArtifactFragment> possChildren = this.getRootArtifact().getMatchingNestedShownChildren((Artifact)model);
			if (!possChildren.isEmpty()) model = possChildren.get(0);
		}
		return super.findEditPart(model);
	}
	// @tag rearch-review: when is this needed?
	public ArtifactEditPart createOrFindArtifactEditPart(Artifact art) {
        ArtifactEditPart aep = findArtifactEditPart(art);
        if (aep != null) return aep;

        return createArtifactEditPart(art);
    }
	
	// do not need to 'find' since we are now allowing duplicates when adding
	@Deprecated
	public ArtifactEditPart createOrFindArtifactEditPart(ArtifactFragment art, ArtifactFragment parentAF) {
        ArtifactEditPart aep = findArtifactEditPart(art);
        if (aep != null) return aep;

        return createArtifactEditPart(art, new HashMap<Artifact,ArtifactFragment>());
    }
	public ReloArtifactRelEditPart findEditPart(ArtifactRel model) {
		// ignore non relo connections
		if (findEditPart((Object)model) instanceof ReloArtifactRelEditPart)
				return (ReloArtifactRelEditPart) findEditPart((Object)model);
		else return null;
		
	}
	public boolean canAddRel(ArtifactFragment srcAF, DirectedRel rel, Artifact dstArt) {
		ArtifactEditPart dstAEP = findArtifactEditPart(dstArt);
        if (dstAEP == null) {
			if (modelCreatable(dstArt)) 
				return true;
			else
				return false;
		}
        
        // art has already been added - check if relationship is shown or not
        ReloArtifactRelEditPart arep;
    	if (rel.isFwd)
    		arep = findEditPart(new ArtifactRel(srcAF, dstAEP.getArtifact(), rel.res));
    	else
    		arep = findEditPart(new ArtifactRel(dstAEP.getArtifact(), srcAF, rel.res));

    	if (arep == null)
			return true;
		else
			return false;
	}

    
    /**
     * Note: This method only places art with the right parent, it does *not*
     * check if art has any valid children to be place inside it,for which just
     * call ArtifactEditPart.assertParenthood (which can take time to go through
     * all the children, which is why it is not checked by default)
     * 
     * @param art
     * @param epInit
     * @return
     */
	@Deprecated
    public ArtifactEditPart createArtifactEditPart(Artifact art) {
        // check if any parent is visible (and add art as a child to that parent) 
        Artifact parentArt = art;
        ArtifactEditPart parentEP = null;
        do {
            parentArt = parentArt.queryParentArtifact(getBrowseModel().getRepo());
            parentEP = findArtifactEditPart(parentArt);
        } while (parentArt != null && parentEP == null);
        // either: parentArt==null or parentEP!=null

        if (parentArt == null)  // add to top level 
            parentEP = this;
        // now: parentEP!=null
        
        return ModelControllerManager.appendModelAndChild(parentEP.getArtifact(), art, parentEP.getRootController());
    }
	
	/**
	 * @param art Object of Comment type.
	 * @return Existing or newly created edit part.
	 */
	public CommentEditPart createOrFindCommentEditPart(ArtifactFragment art){
		AbstractGraphicalEditPart agep=findEditPart(art);
		if(agep!=null) return (CommentEditPart) agep; 
		this.getRootArtifact().addComment((Comment) art);
		return (CommentEditPart)findEditPart(art);
	}


	/* (non-Javadoc)
	 * @see com.architexa.diagrams.relo.parts.ArtifactEditPart#getModelChildren()
	 * Mehtod has been overridden to also get comment children, 
	 * which are saved in a separate list
	 */
	@Override
	public List<ArtifactFragment> getModelChildren(){
		List<ArtifactFragment> childList=new ArrayList<ArtifactFragment>();
		childList.addAll(super.getModelChildren());
		childList.addAll(((ReloDoc)getModel()).getCommentChildren());
		return childList;
	}

    public ArtifactEditPart createArtifactEditPart(ArtifactFragment art) {
    	this.getRootArtifact().addVisibleArt(art);
    	return this.findArtifactEditPart(art);
    }
    public ArtifactEditPart createArtifactEditPart(ArtifactFragment art, Map<Artifact, ArtifactFragment> addedArtToAF) {
		CompoundCommand cc = new CompoundCommand();
    	cc.add(getRootArtifact().addVisibleArt(cc, art, addedArtToAF));
		cc.execute();
    	return this.findArtifactEditPart(art);
    }
    
    
    public boolean artCreatable(Artifact art) {
    	return getRootArtifact().artViewable(art);
    }
    
    // XXX Examine for Removal - Not Used
    public void removeNode(ArtifactEditPart nodeAEP) {
        //((ArtifactEditPart) nodeAEP.getParent()).removeArtFrag(nodeAEP.getArtifact());
    	ArtifactEditPart.removeArtFrag(nodeAEP.getArtifact());
    }
    
    public List<ArtifactEditPart> getSelectedArtifactEditParts() {
    	List<ArtifactEditPart> allParts = getVisibleArtifactEditParts();
		List<ArtifactEditPart> selectedParts = new ArrayList<ArtifactEditPart>();		
		for(ArtifactEditPart part : allParts) {
			if(part.getSelected()!=EditPart.SELECTED_NONE) {
				selectedParts.add(part);
			}
		}    
		return selectedParts;
    }
    
    @SuppressWarnings("unchecked")
    public List<ArtifactRelEditPart> getVisibleArtifactRelEditParts() {
	    List allChildren = new LinkedList (getViewer().getEditPartRegistry().values());
        CollectionUtils.filter(allChildren, new InstanceofPredicate(ArtifactRelEditPart.class));
        return allChildren;
	}
    
	@SuppressWarnings("unchecked")
    public List<ArtifactEditPart> getVisibleArtifactEditParts() {
	    List allChildren = new LinkedList (getViewer().getEditPartRegistry().values());
        CollectionUtils.filter(allChildren, new InstanceofPredicate(ArtifactEditPart.class));
        return allChildren;
	}

	// Note: this method return's CodeUnitEditPart's
	public List<ArtifactEditPart> getVisibleNonDerivedArtifactEditParts() {
	    List<ArtifactEditPart> visAEPs = getVisibleArtifactEditParts();
        CollectionUtils.filter(visAEPs, new Predicate() {
            public boolean evaluate(Object elem) {
                if (((EditPart) elem).getModel() instanceof DerivedArtifact) {
                    return false;
                }
                return true;
            }});
        return visAEPs;
	}

	
	public void removeTopLevelCU(Artifact art) {
	    removeModelChild(art);
	}


	/**
	 * @see com.architexa.org.eclipse.gef.editparts.AbstractEditPart#isSelectable()
	 */
	@Override
    public boolean isSelectable() {
		return false;
	}

	CommandStackListener animationStackListener = new CommandStackListener() {
		public void commandStackChanged(EventObject event) {
			if (!GraphAnimation.captureLayout(getFigure()))
				return;
			while (GraphAnimation.step())
				getFigure().getUpdateManager().performUpdate();
			GraphAnimation.end();
		}
	};

	/**
	 * @see org.eclipse.gef.examples.flow.parts.ActivityPart#activate()
	 */
	@Override
    public void activate() {
		try {
			super.activate();
			getViewer().getEditDomain().getCommandStack().addCommandStackListener(animationStackListener);
			//logger.info("Loading: " + ((ReloDoc) getModel()).getItems());

			ReloRdfRepository inputRepo = ((ReloDoc) getRootArtifact()).getInputRDFRepo();
			if (inputRepo!=null) {
				Value detailLevel = inputRepo.getStatement(RSECore.createRseUri("DetailNode"), RSECore.detailLevelURI, null).getObject();
				if (detailLevel != null) {
					getRootArtifact().setDetailLevel(Integer.valueOf(((Literal) detailLevel).getLabel()));
				}
			}
			
			NonEmptyFigureSupport.rootListenToModel(getArtFrag(), nonEmptyFigure, feedbackLayer);
			LoadCommand loadCmd = new LoadCommand(this, getReloDoc(), getRepo());
			getViewer().getEditDomain().getCommandStack().execute(loadCmd);
			// only command that has run till now is in loading - so don't flag dirty bit
			ReloEditor editor = getReloEditor();
			if (editor != null) editor.clearDirtyFlag();
		} catch (Exception e) {
		    logger.error("Unexpected exception", e);
		}
	}

	/**
	 * @see org.eclipse.gef.examples.flow.parts.ActivityPart#deactivate()
	 */
	@Override
    public void deactivate() {
		BuildStatus.updateDiagramItemMap(getViewer().getEditDomain()
				.getCommandStack().toString(), getAllChildren());
		getViewer()
			.getEditDomain()
			.getCommandStack()
			.removeCommandStackListener(
			animationStackListener);
		super.deactivate();
	}
	
	private int getAllChildren() {
		// need to recursively find all children
		return getViewer().getEditPartRegistry().keySet().size();
	}

	public void updateVisibility() {
		if (true) return;
		
		for(ArtifactEditPart aep : getVisibleArtifactEditParts()) {
			logger.error("Set opaque to false for: " + aep);
			aep.getFigure().setOpaque(false);
			//aep.getFigure().set
			aep.getContentPane().setOpaque(false);
			
			//if (aep instanceof MoreItemsEditPart) {
			//	((MoreItemsEditPart)aep).getMoreBtn().setOpaque(false);
			//}
		}
	}

    public boolean modelCreatable(Object model) {
        if (!(model instanceof Artifact)) 
        	return false;

        // This method called when testing if can show
        // a ref in a nav aid, so if model is lib code and relo preference
        // set to show lib code in menus or diagram, return true
        if(!RSECore.isInitialized(getRepo(), ((Artifact)model).elementRes) && 
        		!LibraryPreferences.isReloLibCodeHidden()) return true;
        return this.artCreatable((Artifact) model);
    }

    public ImageDescriptor getIconDescriptor(NavAidsEditPart aep, Object relModel) {
        Artifact relArt = (Artifact) relModel;
        return ((MoreItemsEditPart)aep).getIconDescriptor(relArt, relArt.queryType(aep.getRepo()));
    }

	public List<EditPart> getCommentEPChildren() {
		return new ArrayList<EditPart>();
	}

	public void setCommentEPChildren(List<EditPart> children) {
		
	}
}
