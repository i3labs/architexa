package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.editparts.GroupedMethodBoxEditPart;
import com.architexa.diagrams.chrono.editparts.InstanceEditPart;
import com.architexa.diagrams.chrono.editparts.MethodBoxEditPart;
import com.architexa.diagrams.chrono.editparts.SeqNodeEditPart;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.models.UserCreatedMethodBoxModel;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.gef.commands.Command;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class ReorderNodeCommand extends Command {

	private int oldIndex, newIndex;
	private SeqNodeEditPart childEP;
	private ArtifactFragment childModel;
	private ArtifactFragment parentModel;

	public ReorderNodeCommand(SeqNodeEditPart childEP, ArtifactFragment childModel, 
			ArtifactFragment parent, int newIndex) {
		this.childEP = childEP;
		this.childModel = childModel;
		this.parentModel = parent;
		this.newIndex = newIndex;
	}

	@Override
	public void execute() {
		if(parentModel instanceof DiagramModel) {
			oldIndex = ((DiagramModel)parentModel).getChildren().indexOf(childModel);
			((DiagramModel)parentModel).reorderChild(childModel, newIndex);
		}
		else if(parentModel instanceof NodeModel) {
			oldIndex = ((NodeModel)parentModel).getChildren().indexOf(childModel);
			((NodeModel)parentModel).reorderChild((NodeModel)childModel, newIndex);
		}
		else return;
		updateConnections();
	}

	@Override
	public void undo() {
		if(parentModel instanceof DiagramModel) {
			((DiagramModel)parentModel).reorderChild(childModel, oldIndex);
		} else if(parentModel instanceof NodeModel) {
			((NodeModel)parentModel).reorderChild((NodeModel) childModel, oldIndex);
		} 
		else return;
		updateConnections();
	}

	private void updateConnections() {
		if(!(childEP instanceof InstanceEditPart)) return;

		for(Object ep : childEP.getChildren()) {
			if(!(ep instanceof MethodBoxEditPart) || ep instanceof GroupedMethodBoxEditPart) continue;
			updateConnections((MethodBoxEditPart)ep);
		}
	}

	// Adding/removing the highlighting and hiding/showing the connections for
	// methods that are/are no longer the source or target of a backward message
	private void updateConnections(MethodBoxEditPart method) {

		// Update highlight of invocation and declaration of call
		method.highlightAsBackwardCall();
		MethodBoxEditPart partner = method.getPartnerEP();
		if(partner!=null) partner.highlightAsBackwardCall();

		// Update visibility of invocation and return messages of call
		boolean visible = !method.isABackwardCall();
		if (method.getModel() instanceof MethodBoxModel) {
			MethodBoxModel methodModel = (MethodBoxModel) method.getModel();
			ConnectionModel outgoingConnection = methodModel.getOutgoingConnection();
			ConnectionModel incomingConnection = methodModel.getIncomingConnection();
			if (outgoingConnection!=null) outgoingConnection.setVisible(visible);
			if (incomingConnection!=null) incomingConnection.setVisible(visible);
		} 
		// Do the same for any addition calls made by a declaration or any nested calls
		for(Object ep : method.getChildren()) {
			if(!(ep instanceof MethodBoxEditPart)) continue;
			updateConnections((MethodBoxEditPart)ep);
		}
	}

}
