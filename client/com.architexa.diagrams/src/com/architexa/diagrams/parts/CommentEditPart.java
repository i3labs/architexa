/**
 * 
 */
package com.architexa.diagrams.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.diagrams.figures.CommentFigure;
import com.architexa.diagrams.figures.EntityFigure;
import com.architexa.diagrams.figures.NoteFigure;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.Entity;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.draw2d.ChopboxAnchor;
import com.architexa.org.eclipse.draw2d.ConnectionAnchor;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FigureCanvas;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.PrecisionRectangle;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.ConnectionEditPart;
import com.architexa.org.eclipse.gef.DefaultEditDomain;
import com.architexa.org.eclipse.gef.DragTracker;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.NodeEditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import com.architexa.org.eclipse.gef.editpolicies.ResizableEditPolicy;
import com.architexa.org.eclipse.gef.requests.ChangeBoundsRequest;
import com.architexa.org.eclipse.gef.requests.CreateConnectionRequest;
import com.architexa.org.eclipse.gef.tools.DirectEditManager;

/**
 * @author Abhishek Rakshit
 * 
 */
public class CommentEditPart extends AbstractGraphicalEditPart implements
PropertyChangeListener, UndoableLabelSource, NodeEditPart {


	private AnnoLabelDirectEditPolicy editPolicy;

	@Override
	public void activate() {
		if (isActive())
			return;
		super.activate();
		((ArtifactFragment) getModel()).addPropertyChangeListener(this);

	}

	@Override
	public void deactivate() {
		if (!isActive())
			return;
		super.deactivate();
		((ArtifactFragment) getModel()).removePropertyChangeListener(this);
	}

	public IFigure getAnnoLabelFigure() {
		return getFigure();
	}

	public void setAnnoLabelText(String text) {
		((Comment) this.getModel()).setAnnoLabelText(text);
	}

	public String getAnnoLabelText() {
		return ((Comment) this.getModel()).getAnnoLabelText();
	}
	
	public String getOldAnnoLabelText() {
		return ((Comment) this.getModel()).getOldAnnoLabelText();
	}

	public void setOldAnnoLabelText(String str) {
		((Comment) this.getModel()).setOldAnnoLabelText(str);
	}


	public void propertyChange(PropertyChangeEvent evt) {
		// super.propertyChange(evt);
		super.refresh();
	}

	@Override
	protected void refreshVisuals() {
		((CommentFigure) getFigure()).setText(getAnnoLabelText());
		figure.setSize(figure.getPreferredSize());
		PointPositionedDiagramPolicy.getLocToFig((ArtifactFragment) this.getModel(), (Figure) this.getFigure());
		super.refreshVisuals();
	}
	
	@Override
	protected void refreshTargetConnections() {
		super.refreshTargetConnections();
	}

	@Override
	protected IFigure createFigure() {
		if(getModel() instanceof Entity) {
			Entity entity = (Entity) getModel();
			EntityFigure entityFig;
			if (entity.getIconDescriptor() != null) // is actor or database entity
				entityFig = new EntityFigure(entity.getEntityName(), ImageCache.calcImageFromDescriptor(entity.getIconDescriptor()));
			else { // is user imported image
				Image image = new Image(Display.getDefault(), entity.getImageData());
				entityFig = new EntityFigure(entity.getEntityName(), image);
			}
			entityFig.resizeImg(new Rectangle(entityFig.getBounds().x, entityFig.getBounds().y, entity.getSize().width, entity.getSize().height));
			return entityFig;
		}
		return new NoteFigure() {
			@Override
			public Rectangle getClientArea(Rectangle rect) {
				Rectangle origArea = new Rectangle(super.getClientArea(rect).getCopy());
				origArea.expand(6, 0); // To make sure that the editing part encompasses the whole comment and not scrol to next line
				return origArea;
			}
		};
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new CommentComponentEditPolicy());
		// helps countering overlaps in comments
		// need to check if we need to check for overlaps
//		installEditPolicy(EditPolicy.LAYOUT_ROLE, new CommentOrderedLayoutEditPolicy());
		
		NonResizableEditPolicy resizePolicy;
		if (getModel() instanceof Entity) { 
			resizePolicy = new ResizableEditPolicy(){
				@Override
				protected void showChangeBoundsFeedback(
						ChangeBoundsRequest request) {
					if(!isStrata())
						super.showChangeBoundsFeedback(request);
					
					showFeedback(request, getHostFigure(), getDragSourceFeedbackFigure(), getInitialFeedbackBounds());

				}
			};
		} else {
			resizePolicy = new NonResizableEditPolicy(){
				
				@Override
				protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {
					if(!isStrata())
						super.showChangeBoundsFeedback(request);
					
					showFeedback(request, getHostFigure(), getDragSourceFeedbackFigure(), getInitialFeedbackBounds());
				}
			};
		}
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, resizePolicy);
		editPolicy = new AnnoLabelDirectEditPolicy(this, "Edit Comment");
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, editPolicy);
		installEditPolicy(ArtifactRelModificationEditPolicy.KEY, new ArtifactRelModificationEditPolicy());
	}
	
	protected boolean isStrata() {
		IEditorPart mpEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		RSEEditor editor = (RSEEditor) RootEditPartUtils.getEditorFromRSEMultiPageEditor(mpEditor);
		if (!RSEEditor.strataEditorId.equalsIgnoreCase(editor.getEditorId()))
			return false;
		return true;
	}
	
	protected void showFeedback(ChangeBoundsRequest request, IFigure host, IFigure feedback, Rectangle intFeedback) {
		/**
		 * Currently not allowing comments/entity(little man, DB etc) to be dragged out of the viewport bounds. We are centering
		 * the Strata diagram and pulling the comment out of the viewport bounds sometimes caused the 
		 * layout to go into infinite loop trying to center the diagram alongwith moving the comment.
		 * Also look at class ContainableEditPart method createMoveCommand() where the same has been done
		 * for the actual comment.
		 */
		PrecisionRectangle rect = new PrecisionRectangle(intFeedback.getCopy());
		host.translateToAbsolute(rect);
		rect.translate(request.getMoveDelta());
		rect.resize(request.getSizeDelta());
//		org.eclipse.swt.graphics.Rectangle viewportBounds = getViewer().getControl().getBounds();
		
		if (//!RSE.isExploratorySrvr() && 
				RootEditPartUtils.isOutOfViewportBounds((FigureCanvas) getViewer().getControl(), rect))
			return;
		feedback.translateToRelative(rect);
		feedback.setBounds(rect);
	}

	protected DirectEditManager manager;

	protected void performDirectEdit() {
		EditDomain editDomain = getRoot().getViewer().getEditDomain();
		RSEEditor editor = (RSEEditor) ((DefaultEditDomain)editDomain).getEditorPart();
		editor.rseInjectableCommentEditor.handleTextEditing(getAnnoLabelText(), this, manager, getAnnoLabelFigure());
	}

	@Override
	public void performRequest(Request request) {
		if (request.getType() == RequestConstants.REQ_DIRECT_EDIT || request.getType() == RequestConstants.REQ_OPEN)
			performDirectEdit();
		else
			super.performRequest(request);
	}

	/**
	 * @see com.architexa.org.eclipse.gef.EditPart#setSelected(int)
	 */
	@Override
	public void setSelected(int value) {
		super.setSelected(value);

		// make the text in notes appear editable as soon as added to diagram
		if (!(getModel() instanceof Entity) &&
				value == EditPart.SELECTED_PRIMARY
				&& getAnnoLabelText() == Comment.defaultComment) {
			performDirectEdit();
		}
	}

	@Override
	public DragTracker getDragTracker(Request request) {
		return new com.architexa.org.eclipse.gef.tools.DragEditPartsTracker(this){
			@Override
			protected boolean isMove() {
				EditPart part = getSourceEditPart();
				while (part != getTargetEditPart() && part != null) {
					if ( part.getSelected() != EditPart.SELECTED_NONE)
						return true;
					part = part.getParent();
				}
				return false;
			}
		};
	}
	
	@Override
	protected List getModelTargetConnections() {
		return ((ArtifactFragment)getModel()).getShownTargetConnections();
	}
	@Override
	protected List getModelSourceConnections() {
		return ((ArtifactFragment)getModel()).getShownSourceConnections();
	}

	// connection anchors
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getFigure());
	}


	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new ChopboxAnchor(getFigure());
	}


	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new ChopboxAnchor(getFigure());
	}


	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new ChopboxAnchor(getFigure());
	}
	
	@Override
	public Command getCommand(Request request) {
		// Only General Connections are allowed for comments
		if (request instanceof CreateConnectionRequest) {
			Object obj = ((CreateConnectionRequest) request).getNewObject();
			if (!RSECore.namedRel.equals(((NamedRel)obj).getType()))
					return null;
		}
		return super.getCommand(request);
	}
}
