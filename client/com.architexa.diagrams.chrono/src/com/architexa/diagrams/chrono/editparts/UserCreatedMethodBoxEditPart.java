package com.architexa.diagrams.chrono.editparts;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TextCellEditor;

import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.figures.UserCreatedMethodBoxFigure;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.parts.AnnoLabelCellEditorLocator;
import com.architexa.diagrams.parts.AnnoLabelDirectEditPolicy;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.MouseEvent;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.tools.DirectEditManager;

public class UserCreatedMethodBoxEditPart extends MethodBoxEditPart implements UndoableLabelSource{

	
	@Override
	/**
	 * SeqEditorContextMenuProvider.buildContextMenu() does not add this
	 * action for grouped instances or grouped methods, but just in case
	 * it does get accidentally added, disabling 'Open in Java Editor' 
	 * for grouped methods since they don't correspond to any one 
	 * particular code component that could be opened.
	 * 
	 * (Disabling rather than returning an empty action since that would
	 * make an empty line in the context menu that looks like a bug).
	 */
	public IAction getOpenInJavaEditorAction(String actionName,
			ImageDescriptor image) {
		IAction action = super.getOpenInJavaEditorAction(actionName, image);
		action.setEnabled(false);
		return action;
	}
	
	@Override
	public void performRequest(Request req) { 
		if (this instanceof GroupedMethodBoxEditPart)
			return;
		
		if(REQ_OPEN.equals(req.getType())) {
			// Edit label on double click
			performDirectEdit();
		}
	}
	
	@Override
	protected void addChildVisual(EditPart childEditPart, int index) {
		super.addChildVisual(childEditPart, index);
	}
	
	protected DirectEditManager manager;
	private String oldMethodName;
	private Label toolTip;

	protected void performDirectEdit() {
		if (manager == null)
			manager = new UnfocusableLabelDirectEditManager(this, TextCellEditor.class,
					new AnnoLabelCellEditorLocator(getAnnoLabelFigure()),
					getAnnoLabelFigure());
		manager.show();
	}
	
	public IFigure getAnnoLabelFigure() {
		return ((MethodBoxFigure)getFigure()).getNoConnectionsBoxLabel();
	}
	
	public String getAnnoLabelText() {
		return ((MethodBoxModel)getModel()).getMethodName();
	}

	public String getOldAnnoLabelText() {
		return oldMethodName;
	}
	
	public void setOldAnnoLabelText(String oldName) {
		oldMethodName = oldName;
	}
	
	public void setAnnoLabelText(String str) {

		if (str == null) return;
		// Check for line feed(10) and carriage return(13)
		if (str.contains("\n") || str.contains("\r") || str.contains("\t")) {
			str.replaceAll("\n", "");
			str.replaceAll("\r", "");
			str.replaceAll("\t", "");
			((UnfocusableLabelDirectEditManager) manager).commit();
			return;
		}
		updateLabels(str, false);
	}

	public void updateLabels(String str, boolean fromConnection) {
		((MethodBoxFigure)getFigure()).setNoConnectionsBoxLabel(str);
		((MethodBoxModel)getModel()).setMethodName(str);
		updateToolTipLabel(str);
		if (!fromConnection)
			updateIncommingConnectionLabel(str);		
	}

	private void updateIncommingConnectionLabel(String str) {
		ConnectionModel incommingConn = ((MethodBoxModel)getModel()).getIncomingConnection();
		if (incommingConn == null) return;
		incommingConn.firePropertyChange(ConnectionModel.PROPERTY_REFRESH, "", str);
	}

	private void updateToolTipLabel(String str) {
		if (toolTip == null) {
			toolTip = new Label(str);
			return;
		}
		
		toolTip.setText(str);
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new AnnoLabelDirectEditPolicy(this, "Edit Method Name"));
	}
	
	@Override
	protected void addOrRemoveSourceOrTargetConn(ConnectionModel connection, boolean add, boolean source) {
		MethodBoxModel model = (MethodBoxModel) getModel();
		
		if(RJCore.overrides.equals(connection.getType())) {
			if(source) model.setOverridesConnection(add?connection:null);
			else model.addOrRemoveOverriderConnection(connection, add);
			addOrRemoveOverrideIndicator(add, source);
		} else {
			if (source) model.setOutgoingConnection(add?connection:null);
			else model.setIncomingConnection(add?connection:null);
			((UserCreatedMethodBoxFigure)getFigure()).setPartner(getPartnerEP() == null ? null : (MethodBoxFigure)getPartnerEP().getFigure());
		}
		//TODO
		//		highlightAsBackwardCall();
		//		updateNoConnectionsLabel();
	}

	@Override
	protected IFigure createFigure() {
		MethodBoxModel model = (MethodBoxModel) getModel();
		Label tooltip = createTooltipFigure();
		UserCreatedMethodBoxFigure  groupedMethodBoxFigure = new UserCreatedMethodBoxFigure(model.getType(), tooltip, model.getMethodName());
		model.setFigure(groupedMethodBoxFigure);
		
		groupedMethodBoxFigure.addPropertyChangeListener(this);
		groupedMethodBoxFigure.addMouseListener(this);
		groupedMethodBoxFigure.addMouseMotionListener(this);
		groupedMethodBoxFigure.addFigureListener(this);
		
		return groupedMethodBoxFigure;
	}
	
	protected Label createTooltipFigure() {
		toolTip = new Label(((MethodBoxModel) getModel()).getMethodName());
		return toolTip;
	}

	@Override
	public void figureMoved(IFigure source) {
		updateBounds();
	}

	// Overriding mouse enter and exit handling so that user created
	// connections will remain visible at all times rather than being
	// hidden on mouse exit
	@Override
	public void mouseEntered(MouseEvent me) {}
	@Override
	public void mouseExited(MouseEvent me) {}
}
