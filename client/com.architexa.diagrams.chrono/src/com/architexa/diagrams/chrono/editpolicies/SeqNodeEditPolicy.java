package com.architexa.diagrams.chrono.editpolicies;


import org.openrdf.model.URI;

import com.architexa.diagrams.chrono.commands.ConnectionCreateCommand;
import com.architexa.diagrams.chrono.commands.TransferMethodConnectionsCommand;
import com.architexa.diagrams.chrono.commands.UserCallCreateCommand;
import com.architexa.diagrams.chrono.commands.UserConnCreateCommand;
import com.architexa.diagrams.chrono.commands.UserOverridesCreateCommand;
import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.editparts.MethodBoxEditPart;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.AnnotatedRel;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.parts.ArtifactRelModificationEditPolicy;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.requests.CreateConnectionRequest;
import com.architexa.org.eclipse.gef.requests.ReconnectRequest;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqNodeEditPolicy extends ArtifactRelModificationEditPolicy {

	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		// ignore this policy when dealing with Annotated rels
		if (request.getStartCommand() == null) return null;
		if (request.getStartCommand() instanceof ConnectionCreateCommand) {
			ConnectionCreateCommand command = (ConnectionCreateCommand)request.getStartCommand();
			command.setTarget(getNodeModel());
			if (command.canExecute()) 
				return command;
		}
		
		if (request.getStartCommand() instanceof UserConnCreateCommand && 
				getHost().getModel() instanceof MethodBoxModel) {
			UserConnCreateCommand cmd = (UserConnCreateCommand) request.getStartCommand();
			cmd.setTargetModel((MethodBoxModel) getHost().getModel());
			if (cmd.canExecute())
				return cmd;
		}
		return super.getConnectionCompleteCommand(request);
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		if (request.getTargetEditPart() instanceof MethodBoxEditPart) {
			MethodBoxEditPart part = (MethodBoxEditPart)request.getTargetEditPart();
			URI connType = ((ArtifactRel)request.getNewObject()).getType();
			Command cmd;
			if(RJCore.overrides.equals(connType))
				cmd = new UserOverridesCreateCommand((MethodBoxModel) part.getModel(), 
						(DiagramEditPart)getHost().getViewer().getContents());
			else cmd = new UserCallCreateCommand((MethodBoxModel) part.getModel());
			request.setStartCommand(cmd);
			return cmd;
		}
		return null;
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		if (request.getConnectionEditPart().getModel() instanceof AnnotatedRel) return super.getReconnectTargetCommand(request);
		ConnectionModel connection = (ConnectionModel) request.getConnectionEditPart().getModel();
		if(!(connection.getSource() instanceof MethodBoxModel) || 
				!(getNodeModel() instanceof MethodBoxModel)) return null;

		MethodBoxModel invocation = (MethodBoxModel) connection.getSource();
		MethodBoxModel newDeclaration = (MethodBoxModel) getNodeModel();

		if(MethodBoxModel.access!=invocation.getType() ||
				MethodBoxModel.declaration!=newDeclaration.getType()) return null;

		TransferMethodConnectionsCommand cmd = new TransferMethodConnectionsCommand(connection, invocation.getIncomingConnection(), invocation, newDeclaration);
		return cmd;
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		if (request.getConnectionEditPart().getModel() instanceof AnnotatedRel) return super.getReconnectSourceCommand(request);
		ConnectionModel connection = (ConnectionModel)request.getConnectionEditPart().getModel();
		if(!(connection.getTarget() instanceof MethodBoxModel) || 
				!(getNodeModel() instanceof MethodBoxModel)) return null;

		MethodBoxModel invocation = (MethodBoxModel) connection.getTarget();
		MethodBoxModel newDeclaration = (MethodBoxModel) getNodeModel();

		if(MethodBoxModel.access!=invocation.getType() ||
				MethodBoxModel.declaration!=newDeclaration.getType()) return null;

		TransferMethodConnectionsCommand cmd = new TransferMethodConnectionsCommand(invocation.getOutgoingConnection(), connection, invocation, newDeclaration);
		return cmd;
	}

	protected NodeModel getNodeModel() {
		return (NodeModel)getHost().getModel();
	}
}
