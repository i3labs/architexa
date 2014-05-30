package com.architexa.diagrams.chrono.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.dom.ASTNode;

import com.architexa.diagrams.chrono.commands.ConnectionCreateCommand;
import com.architexa.diagrams.chrono.commands.ConnectionDeleteCommand;
import com.architexa.diagrams.chrono.commands.GroupedFieldCreateCommand;
import com.architexa.diagrams.chrono.commands.InstanceCreateCommand;
import com.architexa.diagrams.chrono.commands.InstanceDeleteCommand;
import com.architexa.diagrams.chrono.commands.UserInstanceCreateCommand;
import com.architexa.diagrams.chrono.commands.UserInstanceDeleteCommand;
import com.architexa.diagrams.chrono.commands.UserMethodBoxCreateCommand;
import com.architexa.diagrams.chrono.editparts.InstanceEditPart;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.GroupedFieldModel;
import com.architexa.diagrams.chrono.models.GroupedInstanceModel;
import com.architexa.diagrams.chrono.models.GroupedMethodBoxModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.MethodInvocationModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.models.UserCreatedInstanceModel;
import com.architexa.diagrams.chrono.models.UserCreatedMethodInvocationModel;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractEditPart;

public class GroupedUtil {

	public static void createGroupFromModels(List<ArtifactFragment> instanceModelList, DiagramModel diagram, CompoundCommand cmd){
		if(instanceModelList== null || instanceModelList.size()<=1) return;
		int indexOfFirst = diagram.getChildren().indexOf(instanceModelList.get(0));
		for(ArtifactFragment frag:instanceModelList){
			int index = diagram.getChildren().indexOf(frag);
			if(indexOfFirst > index)
				indexOfFirst = index;
		}
		createGroupedModels(instanceModelList, diagram, cmd, indexOfFirst);
	}

	/**
	 * Method to create a grouped Instance from multiple instances
	 * @param selectedParts
	 * @param diagram
	 * @param command
	 */
	public static void createGroupFromEditParts(List<InstanceEditPart> selectedParts, DiagramModel diagram, CompoundCommand command){
		List<ArtifactFragment> modelsToRemoveList = new ArrayList<ArtifactFragment>();
		int indexOfFirstInstanceInGroup = diagram.getChildren().indexOf(((AbstractEditPart) selectedParts.get(0)).getModel());
		for(InstanceEditPart selectedPart : selectedParts) {
			ArtifactFragment artFrag = (ArtifactFragment) (selectedPart).getModel();
			if(diagram.getChildren().indexOf(artFrag) <indexOfFirstInstanceInGroup)
				indexOfFirstInstanceInGroup=diagram.getChildren().indexOf(artFrag);

			modelsToRemoveList.add(artFrag);
		}
		createGroupedModels(modelsToRemoveList, diagram, command, indexOfFirstInstanceInGroup);
	}

	private static List<InstanceModel> getAllInstanceModelsToGroup(List<ArtifactFragment> toAdd){
		List<InstanceModel> instanceList = new ArrayList<InstanceModel>();
		for(ArtifactFragment artFrag: toAdd){
			if(artFrag instanceof InstanceModel)
				instanceList.add((InstanceModel) artFrag);
			if(artFrag instanceof GroupedInstanceModel){
				instanceList.addAll((((GroupedInstanceModel)artFrag).getInstanceChildren()));
			}
		}
		return instanceList;
	}

	public static void createGroupedModels( List<ArtifactFragment> instanceModelsToRemoveList, DiagramModel diagram, CompoundCommand command, int indexOfFirstInstanceInGroup){

		List<InstanceModel> instList = getAllInstanceModelsToGroup(instanceModelsToRemoveList);
		if (instList.size() < 2) return;
		GroupedInstanceModel groupedInstanceModel = new GroupedInstanceModel(diagram, instList);

		//create GroupedInstanceModel
		UserInstanceCreateCommand createGI = new UserInstanceCreateCommand(diagram,groupedInstanceModel,indexOfFirstInstanceInGroup);
		command.add(createGI);
		populateBoxedInstance(groupedInstanceModel, instanceModelsToRemoveList, command);

		//remove instances from diagram
		for (ArtifactFragment artifactFrag:instanceModelsToRemoveList){
			if (diagram.getChildren().contains(artifactFrag)){
				if (artifactFrag instanceof InstanceModel){
					InstanceDeleteCommand delInstance = new InstanceDeleteCommand(diagram,(InstanceModel) artifactFrag,diagram.getChildren().indexOf(artifactFrag));
					command.add(delInstance);
				} else if(artifactFrag instanceof GroupedInstanceModel){
					UserInstanceDeleteCommand delGroup = new UserInstanceDeleteCommand(diagram,(GroupedInstanceModel) artifactFrag,diagram.getChildren().indexOf(artifactFrag));
					command.add(delGroup);
				}
			}
		}
	}

	private static void addFieldDeclarationsToGroupedInstance(GroupedInstanceModel groupedInstanceModel, List<ArtifactFragment> fieldsToAdd, CompoundCommand command){
		for(ArtifactFragment fieldModel:fieldsToAdd){
			ArtifactFragment partner = getPartner(fieldModel);
			ConnectionModel incoming = null;
			ASTNode astNode = null;
			IField iField = null;
			GroupedFieldModel groupedFieldDecl = null;
			if(fieldModel instanceof MemberModel){
				incoming = ((MemberModel) fieldModel).getIncomingConnection();
				astNode = ((FieldModel) fieldModel).getASTNode();
				iField = ((FieldModel) fieldModel).getMember();
				groupedFieldDecl = new GroupedFieldModel(groupedInstanceModel, iField, astNode,(FieldModel) fieldModel);
			}else if(fieldModel instanceof MemberModel){
				incoming = ((MemberModel) fieldModel).getIncomingConnection();
				astNode = ((GroupedFieldModel) fieldModel).getASTNode();
				iField = ((GroupedFieldModel) fieldModel).getMember();
				groupedFieldDecl = new GroupedFieldModel(groupedInstanceModel, iField, astNode, ((GroupedFieldModel) fieldModel).getFieldModel());

				//re-adding connections between field model removed due to earlier grouping 
				if(groupedInstanceModel.getAllInstances().contains(getParentInstance(partner))){
					command.add(new ConnectionDeleteCommand(incoming));
					FieldModel declaration = ((GroupedFieldModel)fieldModel).getFieldModel();
					FieldModel invocation = null;
					if(partner instanceof FieldModel){
						invocation = (FieldModel)partner;
					}else if(partner instanceof GroupedFieldModel){
						invocation = ((GroupedFieldModel)partner).getFieldModel();
					}
					command.add(new ConnectionCreateCommand(invocation,declaration,incoming.getLabel(),ConnectionModel.CALL));	
				}
			}

			if(!groupedInstanceModel.getAllInstances().contains(getParentInstance(partner))){
				command.add(new ConnectionDeleteCommand(incoming));
				command.add(new GroupedFieldCreateCommand(groupedInstanceModel,groupedFieldDecl));
				command.add(new ConnectionCreateCommand(partner, groupedFieldDecl, "", ConnectionModel.CALL));
			}
		}
	}

	// method to get the first method inside a group from which the call has originated 
	private static ArtifactFragment getFirstMethodInChain(ArtifactFragment member, GroupedInstanceModel groupedInstanceModel) {

		ArtifactFragment partner = null;
		ArtifactFragment parentInstance = null;

		if (member instanceof MemberModel)
			partner = ((MemberModel) member).getPartner();

		if (partner != null) {
			if(partner instanceof MemberModel)
				parentInstance = ((MemberModel)partner).getInstanceModel();

			if(groupedInstanceModel.getAllInstances().contains(parentInstance)) 
				return getFirstMethodInChain(partner.getParentArt(), groupedInstanceModel);
		}

		return member;
	}

	/**
	 * Method to add InstanceModel and methodBoxModel information in the groupedInstanceModel
	 * @param groupedInstanceModel 
	 * @param instanceArtifactChildrenList List of grouped or non-grouped instances in group
	 * @param command - compound command
	 */
	private static void populateBoxedInstance(GroupedInstanceModel groupedInstanceModel,
			List<ArtifactFragment> instanceArtifactChildrenList, CompoundCommand command ) {

		List<ArtifactFragment> methodsToAdd = new ArrayList<ArtifactFragment>();
		List<ArtifactFragment> fieldDeclarationsToAdd = new ArrayList<ArtifactFragment>();

		for (ArtifactFragment instanceArt: instanceArtifactChildrenList) {
			if (instanceArt instanceof InstanceModel) {
				fieldDeclarationsToAdd.addAll(((NodeModel) instanceArt).getFieldChildren());
				methodsToAdd.addAll(((NodeModel) instanceArt).getMethodChildren());

				groupedInstanceModel.addInstanceModelChildrenToGroup((InstanceModel) instanceArt, instanceArt.getParentArt().getShownChildren().indexOf(instanceArt));

				for (MemberModel child:((NodeModel) instanceArt).getMemberChildren()) {
					groupedInstanceModel.addToChildInstanceMap(child, (InstanceModel) instanceArt); 
				}
			} else if (instanceArt instanceof GroupedInstanceModel) {
				fieldDeclarationsToAdd.addAll(((NodeModel) instanceArt).getFieldChildren());
				methodsToAdd.addAll(((NodeModel) instanceArt).getMethodChildren());

				// create instance children map
				groupedInstanceModel.addAllInstanceChildrenToGroup(((GroupedInstanceModel) instanceArt).getInstanceToIndexMap());

				//create member to parent map
				groupedInstanceModel.addAllToChildInstanceMap(((GroupedInstanceModel) instanceArt).getChildToInstanceMap());

			}
			groupedInstanceModel.addToAllInstances(instanceArt);
		}

		addFieldDeclarationsToGroupedInstance(groupedInstanceModel,fieldDeclarationsToAdd, command);

		List<ArtifactFragment> visitedModels = new ArrayList<ArtifactFragment>();
		for(ArtifactFragment member:methodsToAdd){
			if(visitedModels.contains(member)) continue;

			ArtifactFragment model = getFirstMethodInChain(member,groupedInstanceModel);
			visitedModels.add(model);

			List<ArtifactFragment> methodList = new ArrayList<ArtifactFragment>();
			methodList.add(model);
			List<MemberModel> invocList = getGroupedInvocationList( model, groupedInstanceModel, command, visitedModels,methodList);


			// adding grouped MBM for inbound
			ArtifactFragment partnerArt = getPartner(model);
			ArtifactFragment partnerParent = null;
			if(partnerArt!=null)	// partner is a member model
				partnerParent = partnerArt.getParentArt();

			GroupedMethodBoxModel groupedMBM=null;
			//if inbound connections are present
			if(partnerArt!=null && (!groupedInstanceModel.getAllInstances().contains(partnerParent) || partnerArt instanceof MemberModel) ){
				groupedMBM = GroupedMemberUtil.createGroupedDeclaration(partnerArt,model,groupedInstanceModel,command);
			}

			if(!invocList.isEmpty()){
				if(groupedMBM==null){ //no inbound but outbound present
					groupedMBM = new GroupedMethodBoxModel(groupedInstanceModel , GroupedMethodBoxModel.declaration);
					UserMethodBoxCreateCommand createGroupedMethodBox = new UserMethodBoxCreateCommand(groupedMBM, groupedInstanceModel, "");
					command.add(createGroupedMethodBox);
				}
				if(model instanceof MemberModel)
					groupedMBM.setMethodName(((MethodBoxModel)model).getMethodName());
				else if(model instanceof MemberModel)
					groupedMBM.setMethodName(((GroupedMethodBoxModel)model).getMethodName());

				for(MemberModel groupedModel:invocList){
					if (groupedModel instanceof UserCreatedMethodInvocationModel) {
						UserMethodBoxCreateCommand createGroupMethodBox = new UserMethodBoxCreateCommand((MethodBoxModel) groupedModel, groupedMBM, "");
						command.add(createGroupMethodBox);
					} else if (groupedModel instanceof GroupedFieldModel) {
						GroupedFieldCreateCommand groupedField = new GroupedFieldCreateCommand(groupedMBM,(GroupedFieldModel) groupedModel);
						command.add(groupedField);
					}

				}
			}
			if(groupedMBM!=null){
				for(ArtifactFragment methodModel: methodList)
					if(methodModel instanceof MethodBoxModel)
						groupedMBM.addMethodToList((MethodBoxModel) methodModel);
					else if(methodModel instanceof GroupedMethodBoxModel){
						groupedMBM.addMethodToList(((GroupedMethodBoxModel) methodModel).getSavedMethodChildrenList());
					}
			}
		}
	}

	private static List<MemberModel> getGroupedInvocationList(ArtifactFragment member, GroupedInstanceModel groupedInstanceModel, CompoundCommand command, List<ArtifactFragment> visitedModels, List<ArtifactFragment> methodList){
		if(!visitedModels.contains(member))
			visitedModels.add(member);
		ArtifactFragment partner = null;
		List<MemberModel> invocChildrenList = new ArrayList<MemberModel>();

		List<ArtifactFragment> memberChildren = new ArrayList<ArtifactFragment>();
		if(member instanceof MemberModel)
			memberChildren = ((NodeModel) member).getChildren();
		else if(member instanceof MemberModel)
			memberChildren = member.getShownChildren();

		// go through all children and collect any out going calls
		for(ArtifactFragment modelInvoc:memberChildren){
			ArtifactFragment partnerParentInstance = null ;
			partner = getPartner(modelInvoc);
			if(partner!=null){
				if(partner instanceof MemberModel) // to make sure self call is not selected
					partnerParentInstance = ((MemberModel) partner).getInstanceModel();
				else
					partnerParentInstance = partner.getParentArt();
			}

			//add connections deleted due to previous grouping
			if((modelInvoc instanceof GroupedMethodBoxModel || partner instanceof GroupedMethodBoxModel)  && groupedInstanceModel.getAllInstances().contains(partnerParentInstance))
				reConnect(modelInvoc, partner, groupedInstanceModel, command);

			//find children till end of box
			if(partner != null && groupedInstanceModel.getAllInstances().contains(partnerParentInstance)){
				if(member instanceof MemberModel && !((MemberModel) member).getInstanceModel().equals(partnerParentInstance))// not a self invocation
					methodList.add(partner);
				List<MemberModel> groupedModelList = getGroupedInvocationList(partner, groupedInstanceModel, command, visitedModels,methodList);
				if(groupedModelList!=null){
					invocChildrenList.addAll(groupedModelList);
				}
			}
			//creation of grouped invocation model
			if(partner != null && !groupedInstanceModel.getAllInstances().contains(partnerParentInstance)){
				if(modelInvoc instanceof MethodBoxModel || modelInvoc instanceof GroupedMethodBoxModel)
					invocChildrenList.add(GroupedMemberUtil.createGroupedInvocation(modelInvoc,partner,groupedInstanceModel,command));
				if(modelInvoc instanceof FieldModel || modelInvoc instanceof GroupedFieldModel)
					invocChildrenList.add(GroupedFieldUtil.createGroupedFieldAccess(modelInvoc, partner , groupedInstanceModel , command));

			}
		}
		return invocChildrenList;
	}

	/**
	 * Method to ungroup a grouped instance model
	 * @param diagram
	 * @param groupedInstanceModel
	 * @param command
	 */
	public static void ungroup(DiagramModel diagram, GroupedInstanceModel groupedInstanceModel, CompoundCommand command){

		//add instances
		for(InstanceModel instance : groupedInstanceModel.getInstanceChildren()){
			if(diagram.getChildren().contains(instance)) continue;
			int indexInst = groupedInstanceModel.getInstanceToIndexMap().get(instance);
			Command instCmd;
			if (instance.isUserCreated())
				instCmd = new UserInstanceCreateCommand(diagram, (UserCreatedInstanceModel) instance, indexInst);
			else 
				instCmd = new InstanceCreateCommand(instance, diagram, indexInst, true, true);
			command.add(instCmd);
		}

		for(ArtifactFragment child: groupedInstanceModel.getChildren()){

			if(child instanceof GroupedFieldModel){
				GroupedFieldUtil.addFieldDeclaration((GroupedFieldModel) child,command);
				continue;
			}
			GroupedMethodBoxModel model = (GroupedMethodBoxModel) child;
			MethodBoxModel methodBox = model.getSavedMethodChildrenList().get(0);

			ArtifactFragment partner = model.getPartner();
			if(partner!=null){
				ConnectionModel conn = model.getIncomingConnection();
				ConnectionDeleteCommand connDelete = new ConnectionDeleteCommand(conn);
				command.add(connDelete);
				conn = model.getOutgoingConnection();
				connDelete = new ConnectionDeleteCommand(conn);
				command.add(connDelete);

				String out = model.getIncomingConnection().getLabel();
				String in = model.getOutgoingConnection().getLabel();

				ConnectionCreateCommand connCreate = new ConnectionCreateCommand(partner, methodBox, out, ConnectionModel.CALL);
				command.add(connCreate);
				connCreate = new ConnectionCreateCommand(methodBox,partner, in ,ConnectionModel.RETURN);
				command.add(connCreate);

			}

			for(ArtifactFragment groupedInvoc:model.getChildren()){

				if(groupedInvoc instanceof GroupedFieldModel){
					GroupedFieldUtil.addFieldAccess((GroupedFieldModel) groupedInvoc,command);
					continue;
				}
				partner = ((MethodBoxModel)groupedInvoc).getPartner();
				MethodInvocationModel invocation = ((UserCreatedMethodInvocationModel)groupedInvoc).getMethodInvocationModel();
				ConnectionDeleteCommand connDel = new ConnectionDeleteCommand(((MethodBoxModel)groupedInvoc).getIncomingConnection());
				command.add(connDel);
				connDel = new ConnectionDeleteCommand(((MethodBoxModel)groupedInvoc).getOutgoingConnection());
				command.add(connDel);

				String in = ((MemberModel) groupedInvoc).getIncomingConnection().getLabel();
				String out = ((MemberModel) groupedInvoc).getOutgoingConnection().getLabel();

				ConnectionCreateCommand conn = new ConnectionCreateCommand(invocation,partner,out,ConnectionModel.CALL);
				command.add(conn);
				conn = new ConnectionCreateCommand(partner, invocation, in, ConnectionModel.RETURN);
				command.add(conn);
			}
		}
		DerivedInstanceUtil.getGroupedInstanceDeleteCommand(diagram, groupedInstanceModel, false, new ArrayList<EditPart>(), command);
	}

	// Method to find parent Instance (Grouped or UnGrouped) for a model 
	public static ArtifactFragment getParentInstance(ArtifactFragment model){
		ArtifactFragment parent = null;
		if(model instanceof MemberModel)
			parent = ((MemberModel) model).getInstanceModel();
		return parent;
	}

	//Method to find a Grouped or UnGrouped existing partner for a model 
	public static ArtifactFragment getPartner(ArtifactFragment model){
		ArtifactFragment partner = null;
		if (model instanceof MemberModel)
			partner = ((MemberModel) model).getPartner();
		return partner;
	}

	//Method to reconnect connections which have been removed due to previous grouping
	public static void reConnect(ArtifactFragment model, ArtifactFragment partner, GroupedInstanceModel groupedInstanceModel, CompoundCommand command){
		ConnectionModel incomingConn = null;
		ConnectionModel outgoingConn = null;
		ArtifactFragment invocation = null;
		ArtifactFragment declaration = null;
		if(model instanceof MemberModel && partner instanceof GroupedMethodBoxModel){
			outgoingConn = ((MethodBoxModel)model).getOutgoingConnection();
			incomingConn = ((MethodBoxModel)model).getIncomingConnection();
			invocation = model;
			declaration = ((GroupedMethodBoxModel)partner).getSavedMethodChildrenList().get(0);
		}else if(model instanceof GroupedMethodBoxModel && partner instanceof MemberModel){
			outgoingConn = ((GroupedMethodBoxModel)model).getOutgoingConnection();
			incomingConn = ((GroupedMethodBoxModel)model).getIncomingConnection();
			invocation = ((UserCreatedMethodInvocationModel)model).getMethodInvocationModel();
			declaration = partner;
		}else if(model instanceof GroupedMethodBoxModel && partner instanceof GroupedMethodBoxModel){
			outgoingConn = ((GroupedMethodBoxModel)model).getOutgoingConnection();
			incomingConn = ((GroupedMethodBoxModel)model).getIncomingConnection();
			invocation = ((UserCreatedMethodInvocationModel)model).getMethodInvocationModel();
			declaration = ((GroupedMethodBoxModel)partner).getSavedMethodChildrenList().get(0);
		}
		command.add(new ConnectionDeleteCommand(outgoingConn));
		command.add(new ConnectionDeleteCommand(incomingConn));
		command.add(new ConnectionCreateCommand(invocation,declaration,outgoingConn.getLabel(),ConnectionModel.CALL));
		command.add(new ConnectionCreateCommand(declaration,invocation,incomingConn.getLabel(),ConnectionModel.RETURN));
	}
}
