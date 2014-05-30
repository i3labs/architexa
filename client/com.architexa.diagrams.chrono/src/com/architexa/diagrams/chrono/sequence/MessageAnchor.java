package com.architexa.diagrams.chrono.sequence;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.UserCreatedMethodBoxModel;
import com.architexa.diagrams.chrono.ui.SeqEditorFilter;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.draw2d.AbstractConnectionAnchor;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class MessageAnchor extends AbstractConnectionAnchor {

	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	public MethodBoxModel methodBox;
//	public UserCreatedMethodBoxModel userMethodBox;
	public FieldModel field;
	public ConnectionModel connection;
	int newConnectionsDisplayed = 0;

	public MessageAnchor(IFigure owner, MethodBoxModel methodBox, ConnectionModel connection) {
		super(owner);
		this.methodBox = methodBox;
		this.connection = connection;
	}

//	public MessageAnchor(IFigure owner, UserCreatedMethodBoxModel methodBox, ConnectionModel connection) {
//		super(owner);
//		this.userMethodBox = methodBox;
//		this.connection = connection;
//	}

	public MessageAnchor(IFigure owner, FieldModel field, ConnectionModel connection) {
		super(owner);
		this.field = field;
		this.connection = connection;
	}

	public Point getLocation(Point reference) {

		ArtifactFragment sourceInstance =  connection.getSource();
		ArtifactFragment targetInstance =  connection.getTarget();

		if(sourceInstance instanceof MemberModel) sourceInstance = ((MemberModel)sourceInstance).getInstanceModel();
//		else if(sourceInstance instanceof UserCreatedMemberModel) sourceInstance = ((UserCreatedMemberModel)sourceInstance).getUserInstanceModel();
		if(targetInstance instanceof MemberModel) targetInstance = ((MemberModel)targetInstance).getInstanceModel();
//		else if(targetInstance instanceof UserCreatedMemberModel) targetInstance = ((UserCreatedMemberModel)targetInstance).getUserInstanceModel();

		for(HiddenNodeModel hiddenNode : SeqEditorFilter.getHiddenNodes()) {
			if(hiddenNode.getChildren().contains(sourceInstance) || hiddenNode.getChildren().contains(targetInstance)) {
				return new Point(-1, -1);
			} 
		}

		Rectangle box = Rectangle.SINGLETON;
		box.setBounds(getOwner().getBounds());
		getOwner().translateToAbsolute(box);

		boolean sameClass = sourceInstance.equals(targetInstance);
		Point point;
		int width = 0;

		if(methodBox != null){
			if(connection.equals(methodBox.getIncomingConnection()) &&
					RJCore.calls.equals(connection.getType())) {
				point = sameClass ? new Point(box.getTopRight().x-MethodBoxFigure.DEFAULT_SIZE.width/2, box.getTopRight().y) : box.getTopLeft();
				width = sameClass ? -1*((MethodBoxFigure)getOwner()).getBorderWidth() : ((MethodBoxFigure)getOwner()).getBorderWidth();
			} else if(connection.equals(methodBox.getIncomingConnection()) &&
					RJCore.returns.equals(connection.getType())) {
				point = new Point(box.getBottomRight().x-MethodBoxFigure.DEFAULT_SIZE.width/2, box.getBottomRight().y);
				width = -1*((MethodBoxFigure)getOwner()).getBorderWidth();
			} else if (connection.equals(methodBox.getOutgoingConnection()) &&
					RJCore.calls.equals(connection.getType())) {
				point = new Point(box.getTopRight().x-MethodBoxFigure.DEFAULT_SIZE.width/2, box.getTopRight().y);
				width = -1*((MethodBoxFigure)getOwner()).getBorderWidth();
			} else if(connection.equals(methodBox.getOutgoingConnection()) &&
					RJCore.returns.equals(connection.getType())) {
				point = sameClass ? new Point(box.getBottomRight().x-MethodBoxFigure.DEFAULT_SIZE.width/2, box.getBottomRight().y) : box.getBottomLeft();
				width = sameClass ? -1*((MethodBoxFigure)getOwner()).getBorderWidth() : ((MethodBoxFigure)getOwner()).getBorderWidth();
			} else if(methodBox.equals(connection.getSource()) &&
					RJCore.overrides.equals(connection.getType())) { 
				Rectangle overrideBounds = ((MethodBoxFigure) getOwner()).getOverridesIndicator().getBounds().getCopy();
				(((MethodBoxFigure) getOwner()).getOverridesIndicator()).translateToAbsolute(overrideBounds);
				point = overrideBounds.getTopLeft();
			} else if(methodBox.equals(connection.getTarget()) &&
					RJCore.overrides.equals(connection.getType())) {
				Rectangle overridenBounds = ((MethodBoxFigure) getOwner()).getOverriddenIndicator().getBounds().getCopy();
				(((MethodBoxFigure) getOwner()).getOverriddenIndicator()).translateToAbsolute(overridenBounds);
				point = overridenBounds.getTopLeft();
			} else {
				point = box.getLeft();
				width = ((MethodBoxFigure)getOwner()).getBorderWidth();
			}
			return new Point(point.x + width, point.y);
		}


//		if(userMethodBox != null){
//			if(connection.equals(userMethodBox.getIncomingConnection()) &&
//					RJCore.calls.equals(connection.getType())) {
//				point = sameClass ? new Point(box.getTopRight().x-MethodBoxFigure.DEFAULT_SIZE.width/2, box.getTopRight().y) : box.getTopLeft();
//				width = sameClass ? -1*((MethodBoxFigure)getOwner()).getBorderWidth() : ((MethodBoxFigure)getOwner()).getBorderWidth();
//			} else if(connection.equals(userMethodBox.getIncomingConnection()) &&
//					RJCore.returns.equals(connection.getType())) {
//				point = new Point(box.getBottomRight().x-MethodBoxFigure.DEFAULT_SIZE.width/2, box.getBottomRight().y);
//				width = -1*((MethodBoxFigure)getOwner()).getBorderWidth();
//			} else if (connection.equals(userMethodBox.getOutgoingConnection()) &&
//					RJCore.calls.equals(connection.getType())) {
//				point = new Point(box.getTopRight().x-MethodBoxFigure.DEFAULT_SIZE.width/2, box.getTopRight().y);
//				width = -1*((MethodBoxFigure)getOwner()).getBorderWidth();
//			} else if(connection.equals(userMethodBox.getOutgoingConnection()) &&
//					RJCore.returns.equals(connection.getType())) {
//				point = sameClass ? new Point(box.getBottomRight().x-MethodBoxFigure.DEFAULT_SIZE.width/2, box.getBottomRight().y) : box.getBottomLeft();
//				width = sameClass ? -1*((MethodBoxFigure)getOwner()).getBorderWidth() : ((MethodBoxFigure)getOwner()).getBorderWidth();
//			}else {
//				point = box.getLeft();
//				width = ((MethodBoxFigure)getOwner()).getBorderWidth();
//			}
//			return new Point(point.x + width, point.y);
//		}

		if(field!=null) {
			point = new Point(box.getCenter());
			if(connection.equals(field.getOutgoingConnection()) || sameClass) {
				width = 2;
			} else if(connection.equals(field.getIncomingConnection())) {
				width = -4;
			}
			return new Point(point.x + width, point.y);
		}

		return null;
	}

	public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
		if (l != null) listeners.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
		if (l != null) listeners.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String property, Object oldValue, Object newValue) {
		if (listeners.hasListeners(property)) 
			listeners.firePropertyChange(property, oldValue, newValue);
	}

	protected void firePropertyChange(String property, Object child) {
		if (listeners.hasListeners(property)) 
			listeners.firePropertyChange(property, null, child);
	}

	public void setConnection(ConnectionModel connection) {
		this.connection = connection;
	}

}