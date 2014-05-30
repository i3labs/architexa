package com.architexa.diagrams.chrono.controlflow;

import java.beans.PropertyChangeListener;

import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.diagrams.parts.AnnoLabelDirectEditPolicy;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.org.eclipse.draw2d.ConnectionAnchor;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.MouseEvent;
import com.architexa.org.eclipse.draw2d.MouseListener;
import com.architexa.org.eclipse.gef.ConnectionEditPart;
import com.architexa.org.eclipse.gef.DefaultEditDomain;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.NodeEditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.tools.DirectEditManager;

public class UserCreatedControlFlowEditPart extends ControlFlowEditPart  implements
PropertyChangeListener, UndoableLabelSource, NodeEditPart, MouseListener {

	
	private AnnoLabelDirectEditPolicy editPolicy;

	@Override
	protected void createEditPolicies() {
		editPolicy = new AnnoLabelDirectEditPolicy(this, "Edit Conditional");
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, editPolicy);
		super.createEditPolicies();
	}
	
	
	@Override
	protected IFigure createFigure() {
		IFigure fig = super.createFigure();
		fig.addMouseListener(this);
		return fig;
	}
	
	
	public String getAnnoLabelText() {
		return ((ControlFlowModel) this.getModel()).getConditionalLabel();
	}

	public void setAnnoLabelText(String text) {
		System.err.println(text);
 		((ControlFlowModel) this.getModel()).setConditionalLabel(text);
 		((Label) getAnnoLabelFigure()).setText(text);
	}

	public IFigure getAnnoLabelFigure() {
		return ((ControlFlowBlock)getFigure()).conditionLabel;
	}

	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return null;
	}

	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return null;
	}

	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return null;
	}

	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return null;
	}

	public String getOldAnnoLabelText() {
		return null;
	}

	public void setOldAnnoLabelText(String str) {
		
	}
	protected DirectEditManager manager;

	public void performDirectEdit() {
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


	public void mousePressed(MouseEvent me) {
	}


	public void mouseReleased(MouseEvent me) {
	}


	public void mouseDoubleClicked(MouseEvent me) {
		performDirectEdit();
	}
}
