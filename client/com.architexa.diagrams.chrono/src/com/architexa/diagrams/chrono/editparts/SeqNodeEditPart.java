package com.architexa.diagrams.chrono.editparts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.chrono.editpolicies.SeqComponentEditPolicy;
import com.architexa.diagrams.chrono.editpolicies.SeqNodeEditPolicy;
import com.architexa.diagrams.chrono.figures.AbstractSeqFigure;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.sequence.MessageAnchor;
import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.diagrams.chrono.util.SeqRelUtils;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.ColorDPolicy;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;
import com.architexa.diagrams.parts.RSEEditPart;
import com.architexa.org.eclipse.draw2d.ConnectionAnchor;
import com.architexa.org.eclipse.draw2d.FigureListener;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.MouseEvent;
import com.architexa.org.eclipse.draw2d.MouseListener;
import com.architexa.org.eclipse.draw2d.MouseMotionListener;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.ConnectionEditPart;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.NodeEditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.tools.CellEditorLocator;
import com.architexa.org.eclipse.gef.tools.LabelDirectEditManager;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
abstract public class SeqNodeEditPart extends RSEEditPart implements FigureListener, PropertyChangeListener, NodeEditPart, MouseListener, MouseMotionListener {

	private ConnectionAnchor anchor;
	private Rectangle oldBounds ;

	@Override
	public void activate() {
		if (isActive()) return;
		super.activate();
		((ArtifactFragment)getModel()).addPropertyChangeListener(this);

	}

	@Override
	public void deactivate() {
		if (!isActive()) return;
		super.deactivate();
		((ArtifactFragment)getModel()).removePropertyChangeListener(this);
	}

	@Override
	abstract protected IFigure createFigure();

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new SeqComponentEditPolicy());
		installEditPolicy(EditPolicy.NODE_ROLE, new SeqNodeEditPolicy());
	}

	public void colorFigure(Color color){};
	
	public abstract IAction getOpenInJavaEditorAction(String actionName, ImageDescriptor image);

	
	public class UnfocusableLabelDirectEditManager extends LabelDirectEditManager{

		public UnfocusableLabelDirectEditManager(GraphicalEditPart source, Class<TextCellEditor> editorType,
				CellEditorLocator locator, IFigure directEditFigure) {
			super(source, editorType, locator, directEditFigure);
		}
		
		@Override
		protected void commit() {
			showIsEditableTooltip(false);
			super.commit();
		}
	}
	
	protected void showIsEditableTooltip(boolean isEditable){};
	
	protected void changeName() {};
	
	@Override
	protected List<ArtifactRel> getModelSourceConnections() {
		return ((ArtifactFragment)getModel()).getSourceConnections();
	}

	@Override
	protected List<ArtifactRel> getModelTargetConnections() {
		return ((ArtifactFragment)getModel()).getTargetConnections();
	}

	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		if(connection.getModel() instanceof ConnectionModel)
			return getConnectionAnchor((ConnectionModel)connection.getModel());
		return getConnectionAnchor();
	}

	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return getConnectionAnchor();
	}

	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		if(connection.getModel() instanceof ConnectionModel)
			return getConnectionAnchor((ConnectionModel)connection.getModel());
		return getConnectionAnchor();
	}

	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return getConnectionAnchor();
	}

	protected ConnectionAnchor getConnectionAnchor() {
		if (anchor == null) anchor = ((AbstractSeqFigure)getFigure()).getConnectionAnchor();
		return anchor;
	}

	protected ConnectionAnchor getConnectionAnchor(ConnectionModel connectionModel) {
		if (getModel() instanceof MethodBoxModel)
			anchor = new MessageAnchor(getFigure(), (MethodBoxModel)getModel(), connectionModel);
		else if (getModel() instanceof FieldModel)
			anchor = new MessageAnchor(getFigure(), (FieldModel)getModel(), connectionModel);
		else
			anchor = ((AbstractSeqFigure)getFigure()).getConnectionAnchor();
		connectionModel.setAnchor(anchor);
		return anchor;
	}

	// Also look getFeedbackLayer in SeqSelectionEPOL and SeqOrderedLayoutEpol
	public IFigure getHandleLayer() {
		return getLayer(LayerConstants.HANDLE_LAYER);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().equals(ArtifactFragment.Theme_Changed)) {
			ColorScheme.init();
			refreshVisuals();
		}
		
		String prop = evt.getPropertyName();
		if(NodeModel.PROPERTY_CHILDREN.equals(prop)) {
			refreshChildren();
		} else if(NodeModel.PROPERTY_REORDER.equals(prop)) {
			ArtifactFragment childModel = (ArtifactFragment) evt.getOldValue();
			EditPart childEP = null;
			for(Object ep : getChildren()) {
				if(ep instanceof EditPart && ((EditPart)ep).getModel().equals(childModel)) {
					childEP = (EditPart) ep;
					break;
				}
			}
			if(childEP != null) reorderChild(childEP, (Integer)evt.getNewValue());
		} else if (NodeModel.PROPERTY_SIZE.equals(prop) || NodeModel.PROPERTY_LOCATION.equals(prop)) {
			refreshVisuals();
		} else if (NodeModel.PROPERTY_SOURCE_CONNNECTION.equals(prop)) {
			refreshSourceConnections();
		} else if (NodeModel.PROPERTY_TARGET_CONNECTION.equals(prop)) {
			refreshTargetConnections();
		} else if (ArtifactFragment.Contents_Changed.equals(prop) || NodeModel.PROPERTY_EDIT_INSTANCE_NAME.equals(prop)){
			refresh();
		} else if (ArtifactFragment.Policy_Contents_Changed.equals(prop)) {
			colorFigure(ColorDPolicy.getColor((ArtifactFragment) getModel()));
		}
	}

	// Subclasses should override these mouse event handling methods

	public void mouseDoubleClicked(MouseEvent me) {}
	public void mousePressed(MouseEvent me) {}
	public void mouseReleased(MouseEvent me) {}

	public void mouseDragged(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited(MouseEvent me) {}
	public void mouseHover(MouseEvent me) {}
	public void mouseMoved(MouseEvent me) {}

	// Support for moving comments that are Anchored to this EP
	public void figureMoved(IFigure source) {
		for (CommentEditPart cep : SeqRelUtils.getAnchoredComments(this)) {
			PointPositionedDiagramPolicy policy = (PointPositionedDiagramPolicy) ((Comment) cep.getModel()).getDiagramPolicy(PointPositionedDiagramPolicy.DefaultKey);

			// Disabled for now - Reimplement later to be consistent with Relo
			// TODO fix Look at Comment and make sure relDist is updated when topleft changes
//			Point relDistVal = ((Comment) cep.getModel()).getRelDistance();
//			if (relDistVal != null) {
//				Point srcTopLeft = source.getBounds().getTopLeft();
//				policy.setTopLeft(new Point(srcTopLeft.x + relDistVal.x, srcTopLeft.y + relDistVal.y));
//			}
		}
	}
}
