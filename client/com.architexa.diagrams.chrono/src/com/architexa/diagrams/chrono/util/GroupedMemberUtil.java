package com.architexa.diagrams.chrono.util;

import java.util.List;


import com.architexa.diagrams.chrono.commands.ConnectionCreateCommand;
import com.architexa.diagrams.chrono.commands.ConnectionDeleteCommand;
import com.architexa.diagrams.chrono.commands.GroupedFieldDeleteCommand;
import com.architexa.diagrams.chrono.commands.MemberDeleteCommand;
import com.architexa.diagrams.chrono.commands.UserMethodBoxCreateCommand;
import com.architexa.diagrams.chrono.commands.UserMethodBoxDeleteCommand;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.GroupedFieldModel;
import com.architexa.diagrams.chrono.models.GroupedInstanceModel;
import com.architexa.diagrams.chrono.models.GroupedMethodBoxModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.MethodInvocationModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.models.UserCreatedInstanceModel;
import com.architexa.diagrams.chrono.models.UserCreatedMethodInvocationModel;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

public class GroupedMemberUtil {

	// to create grouped method declarations
	public static GroupedMethodBoxModel createGroupedDeclaration(ArtifactFragment partner,
			ArtifactFragment declaration, GroupedInstanceModel groupedInstanceModel, CompoundCommand command) {
		GroupedMethodBoxModel groupedMBM = new GroupedMethodBoxModel(groupedInstanceModel , GroupedMethodBoxModel.declaration);

		ConnectionModel incoming = null;
		ConnectionModel outgoing = null;
		if(declaration instanceof MemberModel){
			groupedMBM.setMethodName(((MethodBoxModel)declaration).getMethodName());
			incoming = ((MemberModel) declaration).getIncomingConnection();
			outgoing = ((MemberModel) declaration).getOutgoingConnection();
		}else if(declaration instanceof MemberModel){
			groupedMBM.setMethodName(((GroupedMethodBoxModel)declaration).getMethodName());
			incoming = ((MemberModel) declaration).getIncomingConnection();
			outgoing = ((MemberModel) declaration).getOutgoingConnection();
		}
		String in = incoming.getLabel();
		String out = outgoing.getLabel();
		// create incoming connection delete commands
		command.add(new ConnectionDeleteCommand(incoming));
		command.add(new ConnectionDeleteCommand(outgoing));

		UserMethodBoxCreateCommand createGroupedMethodBox = new UserMethodBoxCreateCommand(groupedMBM, groupedInstanceModel, "");
		createGroupedMethodBox.setAccessPartner(partner, partner.getParentArt());
		// connection add commands
		ConnectionCreateCommand callConn = new ConnectionCreateCommand( partner, groupedMBM, in, ConnectionModel.CALL);							
		ConnectionCreateCommand returnConn = new ConnectionCreateCommand( groupedMBM, partner, out, ConnectionModel.RETURN);
		command.add(callConn);
		command.add(returnConn);
		command.add(createGroupedMethodBox);
		return groupedMBM;
	}

	//method to create grouped method invocations
	public static UserCreatedMethodInvocationModel createGroupedInvocation(ArtifactFragment invocation,
			ArtifactFragment declaration, UserCreatedInstanceModel groupedInstanceModel, CompoundCommand command) {

		UserCreatedMethodInvocationModel groupedInvoc = new UserCreatedMethodInvocationModel(groupedInstanceModel, MemberModel.access);

		ConnectionModel incoming = null;
		ConnectionModel outgoing = null;

		if(invocation instanceof MemberModel){
			incoming = ((MemberModel) invocation).getIncomingConnection();
			outgoing = ((MemberModel) invocation).getOutgoingConnection();
			groupedInvoc.setMethodInvocationModel((MethodInvocationModel) invocation);
		}else if(invocation instanceof MemberModel){
			incoming = ((MemberModel) invocation).getIncomingConnection();
			outgoing = ((MemberModel) invocation).getOutgoingConnection();
			groupedInvoc.setMethodInvocationModel(((UserCreatedMethodInvocationModel)invocation).getMethodInvocationModel());
		}

		command.add(new ConnectionDeleteCommand(incoming));
		command.add(new ConnectionDeleteCommand(outgoing));

		command.add(new ConnectionCreateCommand( groupedInvoc, declaration, outgoing.getLabel(), ConnectionModel.CALL));
		command.add(new ConnectionCreateCommand( declaration, groupedInvoc, incoming.getLabel(), ConnectionModel.RETURN));

		return groupedInvoc;
	}

	private static boolean isDuplicateCommand(List<EditPart> selectedParts, Object modelOrConnToDelete){
		for(EditPart part: selectedParts){
			if(part.getModel().equals(modelOrConnToDelete)) //delete command has been created or will be created 
				return true;
		}
		return false;
	}

	/*
	 * During multiple selection of item
	 * If the parent Instance is present it takes care of all the deletion
	 * 
	 * If Declaration is present it is responsible for creating delete commands for the 
	 * invocation and both the connections.
	 * 
	 * If Invocation is present it takes care of deleting self and the connections
	 * 
	 * If a Field model is present then only the Field declaration can initiate the delete
	 */
	public static void getGroupedMemberDeleteCommand(NodeModel parent, MemberModel memberChild, List<EditPart> selectedParts, CompoundCommand command){

		//Grouped Instance is selected
		if(isDuplicateCommand(selectedParts, parent)) return;
		ArtifactFragment partner = memberChild.getPartner();

		if(memberChild instanceof GroupedFieldModel && !isDuplicateCommand(selectedParts, memberChild.getParent())){
			if(memberChild instanceof GroupedFieldModel){
				if(memberChild.getPartner() instanceof GroupedFieldModel)
					deleteGroupedFieldDeclaration((GroupedFieldModel) memberChild.getPartner(), (NodeModel)memberChild.getPartner().getParentArt(),command);
				else
					MemberUtil.deleteFieldDeclaration((FieldModel) memberChild.getPartner(),(NodeModel)memberChild.getPartner().getParentArt(), command, true);
			}else{
				deleteGroupedFieldDeclaration((GroupedFieldModel) memberChild, parent, command);
			}
			return;
		}

		if(memberChild.isDeclaration())
			getDeclarationDeleteCommand(memberChild, parent, selectedParts, command);
		else if(partner!=null && !isDuplicateCommand(selectedParts, partner))
			getInvocationDeleteCommand(memberChild, parent, command);
	}


	public static void deleteGroupedFieldDeclaration(GroupedFieldModel model,
			NodeModel parent, CompoundCommand command) {
		ArtifactFragment partner = model.getPartner();
		if(partner instanceof GroupedFieldModel)
			command.add(new GroupedFieldDeleteCommand((NodeModel) ((GroupedFieldModel) partner).getParent(),(GroupedFieldModel) partner));
		else if(partner instanceof FieldModel)
			command.add(new MemberDeleteCommand(((FieldModel) partner).getParent(),(MemberModel) partner, true));

		command.add(new ConnectionDeleteCommand(model.getIncomingConnection()));
		command.add(new GroupedFieldDeleteCommand(parent, model));
	}


	public static void getInvocationDeleteCommand(
			MemberModel memberChild, NodeModel parent,
			CompoundCommand command) {
		command.add(new ConnectionDeleteCommand(memberChild.getIncomingConnection()));
		command.add(new ConnectionDeleteCommand(memberChild.getOutgoingConnection()));
		command.add(new UserMethodBoxDeleteCommand(parent,(MethodBoxModel) memberChild));
	}


	public static void getDeclarationDeleteCommand(
			MemberModel memberChild, NodeModel parent,
			List<EditPart> selectedParts, CompoundCommand command) {

		ArtifactFragment invocation = MemberUtil.getPartner(memberChild);
		removeChildPartnerConnections(memberChild, command, selectedParts, parent);
		if(invocation!=null){
			command.add(new ConnectionDeleteCommand(memberChild.getIncomingConnection()));
			command.add(new ConnectionDeleteCommand(memberChild.getOutgoingConnection()));
		}
		command.add(new UserMethodBoxDeleteCommand(parent, (MethodBoxModel) memberChild));

		if(invocation instanceof MemberModel){
			command.add(new MemberDeleteCommand(((MemberModel) invocation).getParent(),(MemberModel) invocation, true));
		}else if(invocation instanceof MemberModel){
			command.add(new UserMethodBoxDeleteCommand((NodeModel) ((MemberModel) invocation).getParent(), (MethodBoxModel) invocation));
		}

	}

	private static void removeChildPartnerConnections(
			MemberModel memberChild, CompoundCommand command,
			List<EditPart> selectedParts, NodeModel parent) {
		for(MemberModel child : memberChild.getMemberChildren()){
			if(child instanceof UserCreatedMethodInvocationModel){
				command.add(new ConnectionDeleteCommand(child.getIncomingConnection()));
				command.add(new ConnectionDeleteCommand(child.getOutgoingConnection()));
			}else if(child instanceof GroupedFieldModel){
				command.add(new ConnectionDeleteCommand(child.getOutgoingConnection()));
			}
		}
	}
}
