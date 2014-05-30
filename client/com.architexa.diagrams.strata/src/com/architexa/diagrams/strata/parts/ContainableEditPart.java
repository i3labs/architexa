/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.commands.AddCommentCommand;
import com.architexa.diagrams.commands.MoveCommentCommand;
import com.architexa.diagrams.draw2d.IFigureWithContents;
import com.architexa.diagrams.jdt.JDTSelectionUtils;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.model.UserCreatedFragment;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.ColorDPolicy;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.parts.RSEEditPart;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.cache.DepNdx;
import com.architexa.diagrams.strata.cache.PartitionerSupport;
import com.architexa.diagrams.strata.commands.CreateVertLayerAndMoveCmd;
import com.architexa.diagrams.strata.commands.ModelUtils;
import com.architexa.diagrams.strata.commands.MoveCommand;
import com.architexa.diagrams.strata.figures.ContainerFigure;
import com.architexa.diagrams.strata.model.CompositeLayer;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataFactory;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.LayerPositionedDPolicy;
import com.architexa.diagrams.strata.ui.ColorScheme;
import com.architexa.diagrams.ui.LongCommand;
import com.architexa.diagrams.utils.BuildPreferenceUtils;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.draw2d.ChopboxAnchor;
import com.architexa.org.eclipse.draw2d.ConnectionAnchor;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FigureCanvas;
import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LayoutAnimator;
import com.architexa.org.eclipse.draw2d.LayoutManager;
import com.architexa.org.eclipse.draw2d.RectangleFigure;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.PrecisionRectangle;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.ConnectionEditPart;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.NodeEditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CommandStack;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import com.architexa.org.eclipse.gef.requests.ChangeBoundsRequest;
import com.architexa.org.eclipse.gef.requests.CreateConnectionRequest;
import com.architexa.org.eclipse.gef.requests.CreateRequest;
import com.architexa.store.ReloRdfRepository;


public abstract class ContainableEditPart extends RSEEditPart implements NodeEditPart {
	
	public static final Logger logger = StrataPlugin.getLogger(ContainableEditPart.class);
	protected Color currBase;
	
	public StrataRootEditPart getRootController() {
		return (StrataRootEditPart) this.getRoot().getContents();
	}

	public StrataRootDoc getRootModel() {
		return getRootController().getRootModel();
	}
	
	public ReloRdfRepository getRepo() {
		return getRootController().getRepo();
	}

	public DepNdx getDepCalculator() {
		return getRootController().getDepCalculator();
	}
	
	///////////////
	// Property change listener stuff
	///////////////
	@Override
	public void activate() {
		//System.err.println("activating: " + this);
		super.activate();
		this.getArtFrag().addPropertyChangeListener(this);
	}
	@Override
	public void deactivate() {
		//System.err.println("deactivating: " + this);
		super.deactivate();
		this.getArtFrag().removePropertyChangeListener(this);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		if (evt.getPropertyName().equals(ArtifactFragment.Theme_Changed)) {
			ColorScheme.init();
			refreshVisuals();
		}
		
		String propName = evt.getPropertyName();
		if (ArtifactFragment.Policy_Contents_Changed.equals(propName)) {
			colorFigure(ColorDPolicy.getColor(getArtFrag()));
		}
		
		// Strata: major events map
		// if layerschanges -> refreshHeirarchy
		// if cacheChange -> Layer (but Layer is already listening to that)
		// if contents changed -> rebuilddep (but DepCache is already listening to that)

		//System.err.println("SAFE: " + evt.getPropertyName() + " areDepBuilt: "
		//		+ StrataArtFragEditPart.this.getDepCache().areDepBuilt()
		//		+ " this: " + this.getModel().toString());

		//if (evt.getPropertyName().equals(LayersDPolicy.LayersChanged)) {

			// perhaps should also happen if an ArtFrag is moved
			//refreshHeirarchyInUI();
		//}
		
		//if (evt.getPropertyName().equals(DepCache.Cache_Change)) {
		//	// we don't need to do anything here
		//}
		//
		//if (evt.getPropertyName().equals(ArtifactFragment.Contents_Changed )) {
		//	// we don't need to do anythin here
		//}
	}
	
	public void colorFigure(Color color) {
		IFigure fig = getFigure();
		if (!(fig instanceof ContainerFigure)) return;
		((ContainerFigure)fig).colorFigure(color);
		
		for(Object child : this.getChildren()) {
			((ContainableEditPart)child).colorFigure(color);
			for (Object multiLayeredChild : ((ContainableEditPart)child).getChildren()) {
				if (multiLayeredChild instanceof LayerEditPart)
					((LayerEditPart)multiLayeredChild).colorFigure(color);
			}
		}
	}
	
	protected abstract IFigure createContainerFigure();

	protected Color getContainerBaseColor(int nestingLvl) {
		Color base = ColorScheme.containerColors[nestingLvl%10];
		
		// Store the default color in diagram
		if (currBase == null)
			currBase = base;
		
		// Used when the parent is highlighted. If your close and open package the child should have the modified color 
		if (getParentTAFEP()!=null) {	
			Color color = ColorDPolicy.getColor((ArtifactFragment) this.getParentTAFEP().getModel());
			if (color!=null)
				base=color;
		}
	    return base;
	}
	
	@Override
	protected final IFigure createFigure() {
		IFigure fig = createContainerFigure();
		this.getRootController().updateFigFromSnapshot(this, fig);
		if (fig != null) fig.addLayoutListener(LayoutAnimator.getDefault());
		return fig;
	}

	@Override
	public IFigure getContentPane() {
		return ((IFigureWithContents)getFigure()).getContentFig();
	}
	

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new NonResizableEditPolicy(){
			@Override
			protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
				/**
				 * When an element is dragged in Strata outside the canvas boundaries
				 * it triggers the canvas size to increase. The increase in canvas 
				 * makes the whole diagram move to the center of the continuously increasing
				 * canvas as the diagram is centered.
				 * 
				 * Here we check if the dragged item is going out of canvas bounds we stop showing 
				 * any feedback
				 * Same for comments have been done here: class ContainableEditPart method createMoveCommand() and
				 * class CommentEditPart method showFeedback()
				 */
				
				IFigure feedback = getDragSourceFeedbackFigure();
				
				PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
				getHostFigure().translateToAbsolute(rect);
				rect.translate(request.getMoveDelta());
				rect.resize(request.getSizeDelta());
//				org.eclipse.swt.graphics.Rectangle viewportBounds = getViewer().getControl().getBounds();
				if (//!RSE.isExploratorySrvr() && 
						RootEditPartUtils.isOutOfViewportBounds((FigureCanvas) getViewer().getControl(), rect))
					return;
				feedback.translateToRelative(rect);
				feedback.setBounds(rect);
			}
		});
	}
	
	@Override
	public List<?> getModelChildren() {
		return this.getArtFrag().getShownChildren();
	}
	
    @Override
    protected List<ArtifactRel> getModelSourceConnections() {
        return getArtFrag().getShownSourceConnections();
    }

    @Override
    protected List<ArtifactRel> getModelTargetConnections() {
        return getArtFrag().getShownTargetConnections();
    }

	@Override
	public void performRequest(Request req) {
		Command cmd = getCommand(req);
		if (cmd == null) return;
		LongCommand.checkForSchedulingPrepAndExecute(this, cmd);
		//super.performRequest(req);
		//System.err.println("CEP.performRequest: " + req.getType());
	}
	
	public CommandStack getCommandStack() {
		return this.getViewer().getEditDomain().getCommandStack();
	}
	
	private class FeedbackFigure extends RectangleFigure {}
	private FeedbackFigure insertionFig;
	private IFigure lepFig;

	@Override
	public void showTargetFeedback(Request request) {
		super.showTargetFeedback(request);

		if(request instanceof ChangeBoundsRequest) {
			showFeedbackFig((ChangeBoundsRequest)request);
		}
	}

	/*
	 * Show a line indicating the target insertion location
	 */
	private void showFeedbackFig(ChangeBoundsRequest req) {
		RSEEditPart insertionPart = getInsertionPart(req);
		List<?> epChildren = getChildren();
		int insertionIndex = epChildren.indexOf(insertionPart);
		
		// only show feedback for movable items
		if (!(req.getEditParts().get(0) instanceof TitledArtifactEditPart)) return;
		
		// If the selected part would stay in the same location
		// once the mouse is released, don't show feedback
		if(req.getEditParts().size()==1) {
			Object movingPart = req.getEditParts().get(0);

			// part not moved far enough left to reorder with child left of it
			if(movingPart.equals(insertionPart)) return; 

			// part not moved far enough right to reorder with the child right of it
			int indexOfSel = epChildren.indexOf(movingPart);
			if(indexOfSel!=-1 && indexOfSel==insertionIndex-1) return;

			// trying to move the part to the last index, but it's already there
			if(epChildren.size()>0 &&
					insertionIndex == -1 &&
					epChildren.get(epChildren.size()-1).equals(movingPart)) return;
		}

		// clear any feedback figures already in the feedback layer before
		// adding another feedback fig
		List<Object> feedbackFigs = new ArrayList<Object>();
		for(Object child : getLayer(LayerConstants.SCALED_FEEDBACK_LAYER).getChildren())
			if(child instanceof FeedbackFigure) feedbackFigs.add(child);
		for(Object fig : feedbackFigs)
			getLayer(LayerConstants.SCALED_FEEDBACK_LAYER).remove((IFigure)fig);

		FeedbackFigure feedbackFig = getLineFeedback();
		if (this instanceof TitledArtifactEditPart
				&& getLayerChild(req.getLocation(), getChildren()) == null
				&& !(this instanceof CompositeLayerEditPart) 
				&& ClosedContainerDPolicy.isShowingChildren(this.getArtFrag())
				&& children!=null && !children.isEmpty()) {
			// move will create a new vertical layer, so indicate that
		
			// make the feedback fig half as tall as this containing
			// part's fig and center it vertically within this
			int feedbackHeight = getFigure().getSize().height/2;
			feedbackFig.setSize(10, feedbackHeight);
			int feedbackX = -1;
			int feedbackY = getFigure().getBounds().getTop().y+feedbackHeight/2;
			// if we are root the container figure is too big so we need to calc pos differently
			if (this instanceof StrataRootEditPart)
				feedbackY = getFigure().getBounds().getTop().y +10;
			
			// new vertical layer could be a left vertical layer or a right vertical layer
			Rectangle childBounds = Rectangle.SINGLETON;
			LayerEditPart child = (LayerEditPart) children.get(0);
			childBounds.setBounds(((LayerEditPart) child ).getFigure().getBounds());
			((LayerEditPart) child).getFigure().translateToAbsolute(childBounds);
			Point dropPoint = req.getLocation();
			if (dropPoint.x < childBounds.x && !LayerPositionedDPolicy.containsVertLayer(getArtFrag(), false /*on right*/)) // is on left
				feedbackX = getFigure().getBounds().getTopLeft().x;
			else if (!LayerPositionedDPolicy.containsVertLayer(getArtFrag(), true /*on right*/) && dropPoint.x > childBounds.x +childBounds.width) {// is on right
				Point topRight = getFigure().getBounds().getTopRight();
				// if we are root the container figure is too big so we need to calc pos differently
				if (this instanceof StrataRootEditPart) {
					topRight = ((IFigure) ((AbstractGraphicalEditPart) getChildren().get(0)).getFigure()).getBounds().getTopRight();
					feedbackX = topRight.x+3;
				} else
					feedbackX = topRight.x-feedbackFig.getSize().width-3;
			}
			if (feedbackX == -1) return;
			feedbackFig.setLocation(new Point(feedbackX, feedbackY));

			feedbackFig.setBackgroundColor(getFigure().getBackgroundColor());
			getLayer(LayerConstants.SCALED_FEEDBACK_LAYER).add(feedbackFig);
		} else {
			// do not show feedback for composite layers since we dont add to them
			// unless it's a vertical layout (move will create a new horizontal layer)
			if (this instanceof CompositeLayerEditPart &&
					getFigure().getLayoutManager() instanceof FlowLayout &&
					((FlowLayout)getFigure().getLayoutManager()).isHorizontal()) 
				return;
			// be careful when adding feedback to a figure that may not have the
			// right index, just add to end
			try {
				if (this instanceof TitledArtifactEditPart && getChildren().size() > 0) {
					LayerEditPart lep = getLayerChild(req.getLocation(), getChildren());
					lepFig = lep.getFigure().getParent();
					lepFig.add(feedbackFig, insertionIndex);
				} else
					getFigure().add(feedbackFig, insertionIndex);
			} catch (IndexOutOfBoundsException e) {
				getFigure().add(feedbackFig);
			}
		}
	}

	

	@Override
	public void eraseTargetFeedback(Request request) {
		super.eraseTargetFeedback(request);

		if (insertionFig != null) {
			if(getFigure().getChildren().contains(insertionFig))
				getFigure().remove(insertionFig);
			if(lepFig!=null && lepFig.getChildren().contains(insertionFig)) {
				lepFig.remove(insertionFig);
				lepFig = null;
			} if(getLayer(LayerConstants.SCALED_FEEDBACK_LAYER).getChildren().contains(insertionFig))
				getLayer(LayerConstants.SCALED_FEEDBACK_LAYER).remove(insertionFig);
			insertionFig = null;
		}
	}

	protected FeedbackFigure getLineFeedback() {
		if (insertionFig == null) {
			insertionFig = new FeedbackFigure();
			insertionFig.setSize(10, 10);
		}
		return insertionFig;
	}

	// Anchors for newly added connection
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getFigure());
	}
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getFigure());
	}

	// Anchors for Feedback line
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		if(request instanceof CreateConnectionRequest) 
			return getSourceConnectionAnchor((CreateConnectionRequest)request);
		return new ChopboxAnchor(getFigure());
	}
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		if(request instanceof CreateConnectionRequest) 
			return getTargetConnectionAnchor((CreateConnectionRequest)request);
		return new ChopboxAnchor(getFigure());
	}
	protected ConnectionAnchor getSourceConnectionAnchor(CreateConnectionRequest request) {
		if(!validSource(request)) return null;
		return new ChopboxAnchor(getFigure());
	}
	protected ConnectionAnchor getTargetConnectionAnchor(CreateConnectionRequest request) {
		if(!validTarget(request)) return null;
		return new ChopboxAnchor(getFigure());
	}

	protected boolean validSource(CreateConnectionRequest request) {
		return true;
	}

	protected boolean validTarget(CreateConnectionRequest request) {

		// Don't allow connections to the root
		if(this instanceof StrataRootEditPart) return false;

		if(!RequestConstants.REQ_CONNECTION_END.equals(request.getType())) 
			return true;

		// Don't allow connections to self
		CreateConnectionRequest ccr = (CreateConnectionRequest) request;
		if(this.equals(ccr.getSourceEditPart())) return false;

		return true;
	}

	private RSEEditPart getInsertionPart(ChangeBoundsRequest request) {
		Point dropLocation = request.getLocation();
		LayoutManager layoutMgr = getFigure().getLayoutManager();
		boolean isHorizontalLayout = layoutMgr instanceof FlowLayout ?
				((FlowLayout)layoutMgr).isHorizontal() : false;
		for(int i=0; i<getChildren().size(); i++) {
			Object child = getChildren().get(i);
			if(!(child instanceof RSEEditPart)) continue;

			Figure childFigure = (Figure) ((RSEEditPart)child).getFigure();

			if(isHorizontalLayout && childFigure.getLocation().x >= dropLocation.x) 
				return (RSEEditPart) child;
			if(!isHorizontalLayout && childFigure.getLocation().y >= dropLocation.y)
				return (RSEEditPart) child;
		}
		return null;
	}

	@Override
	public Command getCommand(Request request) {
		// Adds items that are dragged onto the screen, uses Selection Collector in the same way as OpenStrata
		if (request.getType().equals(RequestConstants.REQ_RESIZE_CHILDREN)) {
	    	Command cmd = RootEditPartUtils.getResizeCommand((ChangeBoundsRequest)request);
	    	if(cmd!=null) return cmd;
	    }
		if (request.getType() == RequestConstants.REQ_CREATE) {
			// Use JDTSelUtils so that we get any spring elmts
			Object reqObj = ((CreateRequest)request).getNewObject();
			if(reqObj instanceof Comment )
				return createCommentCommand((Comment) reqObj,(CreateRequest) request);
			else if(reqObj instanceof UserCreatedFragment ) {
				CompoundCommand cc = new CompoundCommand("Add User Created Class");
//				StrataArtFragEditPart.addSingleUserCreatedArt(cc, ((CreateRequest) request).getLocation(), this, (UserCreatedFragment) reqObj, getRootModel());
				StrataArtFragEditPart.addSingleArt(cc, ((CreateRequest) request).getLocation(), this, (UserCreatedFragment) reqObj, getRootModel());
				return cc;
		 	} else {
				List<IJavaElement> reqList = JDTSelectionUtils.getSelectedJDTElements(false);
				if(reqList.isEmpty() && reqObj instanceof List) {
					// request may have originated from selection(s) in Open Type dialog, in
					// which case reqList from JDTSelectionUtils will be empty and we
					// should open the reqObj list
					for(Object o : (List<?>)reqObj)
						if(o instanceof IJavaElement) reqList.add((IJavaElement)o);
				}

				// create a compound command and add an "Add command" for each item selected 
				if (BuildPreferenceUtils.selectionInBuild(reqList)) {
					return ModelUtils.collectAndOpenItems(reqList, getRootModel(), ((CreateRequest) request).getLocation(), this);
				}
			}
		}
		
		// Adds the requested item(s) to the NEW parent
		// Moving a part from its package to the root or to a different package
		if (request.getType() == RequestConstants.REQ_ADD ) {
			return createMoveCommand((ChangeBoundsRequest)request);
		}
		// Used to move a ArtFrag to a different location in the same Layer within the same parent
		// reordering elements within the same layer
		if (request.getType() == RequestConstants.REQ_MOVE_CHILDREN ) {
			return createMoveCommand((ChangeBoundsRequest)request);
		}
		// else
		if (request.getType() == RequestConstants.REQ_DELETE
				|| request.getType() == RequestConstants.REQ_MOVE
				|| request.getType() == RequestConstants.REQ_ORPHAN
				|| request.getType() == RequestConstants.REQ_ORPHAN_CHILDREN
				) {
			// these happen often
			return super.getCommand(request);
		} else {
			logger.info("CEP.getCommand: " + request.getType() + " EP: " + this.getClass() + " // Model: " + this.getModel().getClass());
			return super.getCommand(request);
		}
	}
	
	private Command createCommentCommand(final Comment reqObj, final CreateRequest request) {
    	StrataRootDoc root= this.getRootModel();
		StrataFactory.initAF(reqObj);
		Point rootLoc = getRootController().contentFig.getBounds().getTopLeft();
		Point loc = request.getLocation();
		
		// Handle Scrolling
		getFigure().translateToRelative(loc);
		
		Comment.initComment(reqObj, new Point(rootLoc.x - loc.x, rootLoc.y - loc.y));
		return new AddCommentCommand(root, reqObj, loc);
	}
    
	@Override
	public void execute(Command cmd) {
    	LongCommand.checkForSchedulingPrepAndExecute(this, cmd);
    }
    
	// TODO: Fix Moving: need to support removal from old locations as well, and removal of old Lyr (and recursive)
	private Command createMoveCommand(ChangeBoundsRequest req) {
		// error check
		if (req.getEditParts().isEmpty() || !(req.getEditParts().get(0) instanceof EditPart)) {
			logger.error("Request is empty. No item to move");
			return null;
		}
			
		@SuppressWarnings("unchecked")
		List<EditPart> selectedEPs = req.getEditParts();
		CompoundCommand compoundMoveCmd = new CompoundCommand("Move Item");
		// moving onto a 'closed' TitledEditPart results in an show command being queued. 
		// This flag prevents this command from being queued multiple times
		boolean alreadyShowing = false;
		
		for (EditPart movedEP : selectedEPs) {
			ContainableEditPart destEP = this;
			if (movedEP instanceof CommentEditPart && (destEP instanceof StrataRootEditPart)) {
				CommentEditPart child = (CommentEditPart)movedEP;
				Rectangle origBounds = ((CommentEditPart)child).getFigure().getBounds();
				Rectangle newBounds = ((ChangeBoundsRequest)req).getTransformedRectangle(origBounds);
				
				/**
				 * Currently not allowing comments to be dragged out of the viewport bounds. We are centering
				 * the Strata diagram and pulling the comment out of the viewport bounds sometimes caused the 
				 * layout to go into infinite loop trying to center the diagram alongwith moving the comment.
				 * Also look at class CommentEditPart method showFeedback() where the same has been done
				 * for the feedback figure
				 * We do same for Strata figures look at showChangeBoundsFeedback()
				 */
				if (//!RSE.isExploratorySrvr() && 
						RootEditPartUtils.isOutOfViewportBounds((FigureCanvas) getViewer().getControl(), newBounds)) {
					continue;
				}
				
				compoundMoveCmd.add(new MoveCommentCommand((Comment)child.getModel(), origBounds, newBounds, 
						this.getRootController().contentFig.getBounds().getTopLeft()));
			}
			if (!(movedEP instanceof StrataArtFragEditPart)) continue;
				StrataArtFragEditPart movedAFEP = (StrataArtFragEditPart) movedEP;
			
			Layer oldParentLyr = null;
			if (movedAFEP.getParent() instanceof LayerEditPart)
				oldParentLyr = ((LayerEditPart)movedAFEP.getParent()).getLayer();
			
			// if moving onto an editpart check and show children (open package if unopened)
			// then move into the proper layer index
			if (destEP instanceof TitledArtifactEditPart) {
				// If we are adding to a user created Fragment make sure to add correctly
				if (destEP.getArtFrag() instanceof UserCreatedFragment) {
					compoundMoveCmd.add(StrataArtFragEditPart.getAddUserCreatedFrag(destEP, movedAFEP, getRootModel()));
					alreadyShowing = true;
					continue;
				}
				else if (!ClosedContainerDPolicy.isShowingChildren(this.getArtFrag()) && !alreadyShowing) {

					// When moving on top of a closed titled EP we either want
					// people to drop and open thepackage and move the dragged
					// item into it, or just not allow this type of dragging.
					// Here we do the latter 
//					CompoundCommand showAFEPChildrenCmd = new CompoundCommand("Show Children");
//					ClosedContainerDPolicy.queryAndShowChildren(showAFEPChildrenCmd, this.getArtFrag());
//					compoundMoveCmd.add(showAFEPChildrenCmd);
//					alreadyShowing = true;
//					compoundMoveCmd.add(createTitledMoveCommand(req, movedAFEP, oldParentLyr));
					continue;
				}
				compoundMoveCmd.add(createTitledMoveCommand(req, movedAFEP, oldParentLyr));
			} else if (destEP instanceof CompositeLayerEditPart) {
				compoundMoveCmd.add(createTitledMoveCommand(req, movedAFEP, oldParentLyr));
			}
			if (destEP instanceof LayerEditPart && !(destEP instanceof CompositeLayerEditPart)) {
				compoundMoveCmd.add(createLayerMoveCommand(req, movedAFEP, oldParentLyr));
			}
		}
		
		return compoundMoveCmd;
	}
	
	// Moving onto a titled AFEP. We need to determine how to add a new layer
	// If we are to the left or right of the bounds of the TAFEP's layers we add a vertical layer
	// If we are between child Layers of the TAFEP then we need to add a layer in the correct spot
	// TODO: integrate the logic here with the logic for displaying feedback boxes that show where the move will go
	private Command createTitledMoveCommand(ChangeBoundsRequest req, StrataArtFragEditPart movedAFEP, Layer oldParentLyr) {
		int newLayerNdx = this.getChildren().size();
		Layer newLayer = new Layer(this.getRepo());

		// Check if moving to index 0 (top of titled AFEP) then create appropriate move cmd.
		// regular move cmd logic does not handle this case
		if (insertAtIndexZero(req.getLocation(), getChildren())) {
			ArtifactFragment newParentAF = this.getArtFrag();
			ArtifactFragment oldParentAF = movedAFEP.getParentTAFEP().getArtFrag();
			return createMoveCommand(
					newParentAF,
					newLayer, 
					oldParentAF, 
					oldParentLyr,
					movedAFEP.getArtFrag(), -1, 0);
		
		}
		if (getLayerChild(req.getLocation(), getChildren()) == null) { 
			if (this instanceof CompositeLayerEditPart) return null;	
			
			CreateVertLayerAndMoveCmd vertCmd = null;
			
			
			Rectangle childBounds = Rectangle.SINGLETON;
			if (children!=null && !children.isEmpty()) {
				LayerEditPart child = (LayerEditPart) children.get(0);
				childBounds.setBounds(((LayerEditPart) child ).getFigure().getBounds());
				((LayerEditPart) child).getFigure().translateToAbsolute(childBounds);

				Point dropPoint = req.getLocation();
				
				// create vert layer on right or left dep on location
				if (dropPoint.x < childBounds.x && !LayerPositionedDPolicy.containsVertLayer(getArtFrag(), false)) 
					vertCmd = new CreateVertLayerAndMoveCmd(movedAFEP.getArtFrag(), oldParentLyr, getArtFrag(), false /*isOnRight*/);
				else if (dropPoint.x > childBounds.x +childBounds.width && !LayerPositionedDPolicy.containsVertLayer(getArtFrag(), true))
					vertCmd =new CreateVertLayerAndMoveCmd(movedAFEP.getArtFrag(), oldParentLyr, getArtFrag());
			}
			return vertCmd;
			
		} else if (getChildren().indexOf(getLayerChild(req.getLocation(), getChildren())) == -1) {
			// Move to 'correct' layer
			List<Layer> layers = new ArrayList<Layer>();
			for (Object obj : getModelChildren()) {
				if (obj instanceof Layer) layers.add((Layer)obj);
			}
			newLayerNdx = PartitionerSupport.getCorrectIndex(getRootModel(), this.getArtFrag(), movedAFEP.getArtFrag(), layers);
			// only create a new layer if one at the new index does not exist yet
			if (layers.size() > newLayerNdx && ClosedContainerDPolicy.isShowingChildren(this.getArtFrag())) 
				newLayer = layers.get(newLayerNdx);
		} else newLayerNdx = getChildren().indexOf(getLayerChild(req.getLocation(), getChildren()))+1;

		logger.info("moving to TitledArtifactEditPart " + newLayerNdx);
		ArtifactFragment newParentAF = this.getArtFrag();
		ArtifactFragment oldParentAF = movedAFEP.getParentTAFEP().getArtFrag();
		if (this instanceof CompositeLayerEditPart)
			oldParentAF = (ArtifactFragment) getModel();
		return createMoveCommand(
				newParentAF,
				newLayer, 
				oldParentAF, 
				oldParentLyr,
				movedAFEP.getArtFrag(), -1, newLayerNdx);
	}

	private static boolean insertAtIndexZero(Point dropPoint, List<?> layers) {
		// if we are above all layers return index 0
		if (!layers.isEmpty()) {
			Object firstChild = layers.get(0);
			Rectangle firstChildBounds = Rectangle.SINGLETON;
			firstChildBounds.setBounds(((LayerEditPart) firstChild).getFigure().getBounds());
			if (dropPoint.y < firstChildBounds.y ) 
				return true;
		}
		return false;
	}

	private Command createLayerMoveCommand(ChangeBoundsRequest req, StrataArtFragEditPart movedAFEP, Layer oldParentLyr ) {
		// if adding to a layer check if its a multiLayer and get special
		// indices, otherwise create standard move command for layers
		Layer layer = ((LayerEditPart) this).getLayer();
		boolean isHorz = layer.getLayout();
		if ((layer instanceof CompositeLayer) && ((CompositeLayer) layer).getMultiLayer()) {
			// edge case:
			// if adding to the multiLayer below child layers then add at
			// end of the last layer (do not create new layer)
			int newLayerChildNdx = -1;
			int newLayerNdx = ((LayerEditPart) this).getLayer().getShownChildren().size()-1;

			LayerEditPart child = getLayerChild(req.getLocation(), getChildren());
			if (child != null) {
				newLayerNdx = getChildren().indexOf(getLayerChild(req.getLocation(), getChildren()))+1;
				newLayerChildNdx = child.getChildren().indexOf(getEPChild(req.getLocation(), child, isHorz));
			}
			logger.info("moving to MultiLayer" + this.getClass() + newLayerChildNdx);
			return createMoveCommand(
					this.getParentTAFEP().getArtFrag(), // target AF
					// target Layer (child of multiLayer)
					((Layer) ((LayerEditPart) this).getLayer().getShownChildren().get(newLayerNdx)), 
					movedAFEP.getParentTAFEP().getArtFrag(), // old AF
					oldParentLyr, // old layer
					movedAFEP.getArtFrag(), newLayerChildNdx, -1); // moving AF, index to insert into in Layer, -1 (not new layer)
		}
		int newLayerChildNdx = ((LayerEditPart) this).getLayer().getShownChildren().size();
		if(getEPChild(req.getLocation(), this, isHorz) != null)
			newLayerChildNdx = getChildren().indexOf(getEPChild(req.getLocation(), this, isHorz));
		logger.info("moving to Layer " + this.getClass() + newLayerChildNdx);
		return createMoveCommand(
				this.getParentTAFEP().getArtFrag(),
				((LayerEditPart) this).getLayer(),
				movedAFEP.getParentTAFEP().getArtFrag(),
				oldParentLyr,
				movedAFEP.getArtFrag(), newLayerChildNdx, -1);
	}

	private Command createMoveCommand(
			final ArtifactFragment newParentAF,	final Layer newParentLyr,
		    final ArtifactFragment oldParentAF, final Layer oldParentLyr,
		    final ArtifactFragment movingAF, int newLayerChildNdx, int newLayerNdx) {
		return new MoveCommand(oldParentLyr, oldParentAF, newParentAF, movingAF, newParentLyr, newLayerChildNdx, newLayerNdx);
	}

	// returns the closest StrataArtFragEditPart (child of target container) to the right of the drop
	// location
	// returns null if none is found
	protected static StrataArtFragEditPart getEPChild(Point dropPoint, EditPart target, boolean isHorz) {
		for (Object child : target.getChildren()) {
			if (!(child instanceof StrataArtFragEditPart))
				continue;
			Rectangle childBounds = Rectangle.SINGLETON;
			childBounds.setBounds(((StrataArtFragEditPart) child).getFigure().getBounds());
			((StrataArtFragEditPart) child).getFigure().translateToAbsolute(childBounds);
			if (isHorz) {
				if (dropPoint.x <= childBounds.x)
					return (StrataArtFragEditPart) child;	
			} else {
				if (dropPoint.y <= childBounds.y)
					return (StrataArtFragEditPart) child;	
			}
		}
		return null;
	}
	protected static StrataArtFragEditPart getEPChild(Point dropPoint, EditPart target) {
		return getEPChild(dropPoint, target, true);
	}
	// returns the closest LayerEditPart directly below if between LayerEPs. Null if above or to the left or right
	@SuppressWarnings("unchecked")
	protected static LayerEditPart getLayerChild(Point dropPoint, List<?> children) {
		List<Layer> layers = new ArrayList<Layer>((List<Layer>)children);
		// reverse layers so we find the closest from the bottom up
		Collections.reverse(layers);
		for (Object child : layers) {
			if (!(child instanceof LayerEditPart)) continue;
			Rectangle childBounds = Rectangle.SINGLETON;
			childBounds.setBounds(((LayerEditPart) child).getFigure().getBounds());
			((LayerEditPart) child).getFigure().translateToAbsolute(childBounds);

			// only return a child if drop loc is directly below an existing layer
			if (dropPoint.x >= childBounds.x &&  dropPoint.x <= childBounds.x + childBounds.width) 
				if (dropPoint.y >= childBounds.y + childBounds.height) return (LayerEditPart) child;
		}
		return null;
	}

	@Override
	public boolean understandsRequest(Request req) {
		if (req.getType() == RequestConstants.REQ_CREATE) return true;
		if (req.getType() == RequestConstants.REQ_MOVE) return true;
		System.err.println("CEP.understandsRequest: " + req.getType());
		return super.understandsRequest(req);
	}
	@Override
	public EditPart getTargetEditPart(Request req) {
		if (req.getType() == RequestConstants.REQ_CREATE) return this;
		if (req.getType() == RequestConstants.REQ_ADD) return this;
		if (req.getType() == RequestConstants.REQ_MOVE) return this;
		if (req.getType() != RequestConstants.REQ_SELECTION)
			System.err.println("CEP.getTargetEditPart: " + req.getType());
		return super.getTargetEditPart(req);
	}

	
	@Override
	public AbstractGraphicalEditPart findEditPart(Object model) {
	    return (AbstractGraphicalEditPart) getViewer().getEditPartRegistry().get(model);
	}
	
	public int getNestingLevel() {
		EditPart parent = this.getParent();
		while (parent != null && !(parent instanceof ContainableEditPart)) {
			parent = parent.getParent();
		}
		if (parent == null)
			return 0;
		else
			return ((ContainableEditPart)parent).getNestingLevel() + 1;
	}
	
	// @tag move-to-sgef
	public ArtifactFragment getArtFrag() {
		return (ArtifactFragment) getModel();
	}

	public String getLabel() {
		if (this.getModel() instanceof UserCreatedFragment) {
			return ((UserCreatedFragment)this.getModel()).getLabel(getRepo());
		}
		return CodeUnit.getLabel(getRepo(), this.getArtFrag(), null, false);
//		return CodeUnit.getLabelWithContext(getRepo(), this.getArtFrag());
	}
	
	public String getContextualizedLabel() {
		// Changed from getLabel() since we need the full context so
		// intermediate autoBroken packages do not get removed
		String completeLbl = CodeUnit.getLabel(getRepo(), getArtFrag(), null, true);//this.getLabel();
		
		// strip number for anon class (shows in tooltip)
		if (RSECore.isAnonClassName(completeLbl))
			completeLbl = RSECore.stripAnonNumber(completeLbl);
		
		TitledArtifactEditPart parentTAFEP = this.getParentTAFEP();
		if (parentTAFEP != null) {
			String contextLbl = null;
			contextLbl = getCntxLabel(parentTAFEP);
			if (completeLbl.startsWith(contextLbl) && !contextLbl.equals(".")) {
				completeLbl = completeLbl.substring(contextLbl.length());
			}
		}
		completeLbl = strTruncEnd(completeLbl, ".*");
		completeLbl = strTruncBeg(completeLbl, ".");
		if (completeLbl.length() == 0) completeLbl = ".";
		return completeLbl;
	}
	
	// gets the EPs common context for use in creating labels / label links
	protected String getCntxLabel(ContainableEditPart editPart) {
		if (editPart instanceof StrataRootEditPart)
			return ((TitledArtifactEditPart) editPart).getCommonContextLabel();
		else
//			return strTruncEnd(CodeUnit.getLabelWithContext(getRepo(), editPart.getArtFrag()), ".*") + ".";
			return strTruncEnd(CodeUnit.getLabel(getRepo(), editPart.getArtFrag(), null, true), ".*") + ".";
	}

	private String strTruncEnd(String inLabel, String truncStr) {
		inLabel = inLabel.trim();
        if (inLabel.endsWith(truncStr))
			return inLabel.substring(0, inLabel.length() - truncStr.length());
		else
			return inLabel;
	}
	private String strTruncBeg(String inLabel, String truncStr) {
		inLabel = inLabel.trim();
        if (inLabel.startsWith(truncStr))
			return inLabel.substring(truncStr.length());
		else
			return inLabel;
	}

	public TitledArtifactEditPart getParentTAFEP() {
		EditPart parent = this.getParent();
		while (parent != null && (!(parent instanceof TitledArtifactEditPart))) {
			parent = parent.getParent();
		}
		return (TitledArtifactEditPart) parent;
	}

	@Override
	protected void refreshVisuals() {
		// We need to actively refresh layers since they are not directly part of the EPHierarchy and do not get refreshed unless we are moving/reLayering
		if (getParent() instanceof LayerEditPart && getParent().getParent()!=null && getParent().getParent() instanceof CompositeLayerEditPart) {
			getParent().getParent().refresh();
		} 
		if (getParent() instanceof LayerEditPart) {
			getParent().refresh();
		} 
		super.refreshVisuals();
	}
	
	public void refreshHeirarchy() {
		this.refresh();
		getFigure().revalidate();
		for (Object childEP : this.getChildren()) {
			if (childEP instanceof ContainableEditPart) ((ContainableEditPart)childEP).refreshHeirarchy();
		}
	}
	/*
	public void invalidateHeirarchy() {
		this.refresh();
		for (Object childEP : this.getChildren()) {
			if (childEP instanceof ContainableEditPart) ((ContainableEditPart)childEP).refreshHeirarchy();
		}
	}
	*/

	public void removeEmptyLayers() {
		@SuppressWarnings("unchecked")
		List<ContainableEditPart> epChildren = getChildren();
		for (ContainableEditPart containableEP : epChildren) {
			containableEP.removeEmptyLayers();
			containableEP.removeSingleCompositeLayers();
		}
	}
	public void removeSingleCompositeLayers() {
		@SuppressWarnings("unchecked")
		List<ContainableEditPart> epChildren = getChildren();
		for (ContainableEditPart containableEP : epChildren) {
			containableEP.removeEmptyLayers();
			containableEP.removeSingleCompositeLayers();
		}
	}

	public static Collection<ArtifactFragment> getAllAFs(ArtifactFragment afParent) {
		List<ArtifactFragment> childrenArts = new ArrayList<ArtifactFragment>();
    	if (afParent.getArt().elementRes == null) return null;
    	childrenArts.add(afParent);
    	for (ArtifactFragment afChild : afParent.getShownChildren()) {
			childrenArts.addAll(getAllAFs(afChild));
    	}
    	return childrenArts;
	}
	
}
