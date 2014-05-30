package com.architexa.diagrams.chrono.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IMember;

import com.architexa.diagrams.chrono.commands.ChangeParentOfMethodCommand;
import com.architexa.diagrams.chrono.commands.ConnectionDeleteCommand;
import com.architexa.diagrams.chrono.commands.ControlFlowDeleteCommand;
import com.architexa.diagrams.chrono.commands.GroupedFieldDeleteCommand;
import com.architexa.diagrams.chrono.commands.MemberDeleteCommand;
import com.architexa.diagrams.chrono.commands.UpdateControlBlockCommand;
import com.architexa.diagrams.chrono.commands.UserMethodBoxDeleteCommand;
import com.architexa.diagrams.chrono.controlflow.ControlFlowBlock;
import com.architexa.diagrams.chrono.controlflow.IfBlock;
import com.architexa.diagrams.chrono.editparts.MemberEditPart;
import com.architexa.diagrams.chrono.figures.FieldFigure;
import com.architexa.diagrams.chrono.figures.MemberFigure;
import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.GroupedFieldModel;
import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.MethodInvocationModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.models.UserCreatedMethodInvocationModel;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.jdt.InitializerWrapper;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class MemberUtil {
	private static final Logger logger = SeqPlugin.getLogger(MemberUtil.class);

	/**
	 * Returns the name (including parameter types if 
	 * a method) and type of the given member. If the type
	 * cannot be determined, the member name only is returned.
	 * @return the name of the given member, with type if available
	 */
	public static String getFullName(IMember member) {
		if (member instanceof InitializerWrapper)
			return CodeUnit.initializerStringRep;
		if (member == null) return "";
		int keyIndex = member.toString().indexOf("{key");
		if(keyIndex>0) return member.toString().substring(0, keyIndex);

		int parentInfoIndex = member.toString().indexOf("[in");
		if(parentInfoIndex>0) return member.toString().substring(0, parentInfoIndex);

		return member.getElementName();
	}

	/**
	 * 
	 * @param access the figure that represents the access of a member
	 * @param declaration the figure that represents the declaration of that accessed member
	 * @return true if the access of the member is made in the same class in 
	 * which that member is declared, false otherwise
	 */
	public static boolean isAnAccessToTheSameClass(MemberFigure access, MemberFigure declaration) {
		if(access instanceof MethodBoxFigure 
				&& declaration instanceof MethodBoxFigure) return ((MethodBoxFigure)access).getContainer().getChildren().contains(declaration);
		if(access instanceof FieldFigure 
				&& declaration instanceof FieldFigure) return access.getParent().equals(declaration.getParent());
		return false;
	}

	//TODO check for indexing in two exactly same calls
	public static int getDeclarationIndex(ArtifactFragment declarationParent, ArtifactFragment accessPartner, ArtifactFragment parentOfAccess) {

		int declarationIndex = declarationParent.getShownChildren().size();
		List<ArtifactFragment> childrenOfAccessParent;
		if(parentOfAccess instanceof MemberModel)
			childrenOfAccessParent = ((MemberModel)parentOfAccess).getChildren();
		else
			childrenOfAccessParent = parentOfAccess.getShownChildren();
		List<ArtifactFragment> childrenOfDeclarationParent = declarationParent.getShownChildren();


		// self field access
		if(accessPartner instanceof FieldModel && declarationParent.equals(parentOfAccess)){
			return parentOfAccess.getShownChildren().indexOf(accessPartner)+1;
		}

		for (ArtifactFragment siblingDeclaration : childrenOfDeclarationParent) {
			ArtifactFragment partnerOfSibling = null;
			if (declarationParent instanceof NodeModel 
					&& siblingDeclaration instanceof MemberModel) // partner could be a member or a grouped model
				partnerOfSibling = ((MemberModel) siblingDeclaration).getPartner();
			else if (declarationParent instanceof NodeModel
					&& siblingDeclaration instanceof HiddenNodeModel)
				partnerOfSibling = ((HiddenNodeModel) siblingDeclaration).getPartner();

			if (partnerOfSibling == null) continue;

			// invocations from different methods in the same instance
			ArtifactFragment instance= parentOfAccess.getParentArt();
			if(!parentOfAccess.equals(partnerOfSibling.getParentArt()) // not invocations from same declaration
					&& parentOfAccess.getParentArt().equals(partnerOfSibling.getParentArt().getParentArt()) // methods of same instance
					&& instance.getShownChildren().indexOf(parentOfAccess)<instance.getShownChildren().indexOf(partnerOfSibling.getParentArt())){
				if(declarationIndex>declarationParent.getShownChildren().indexOf(siblingDeclaration)){
					declarationIndex=declarationParent.getShownChildren().indexOf(siblingDeclaration);
					continue;
				}
			}
			// invocations from same method box
			if(childrenOfAccessParent.contains(partnerOfSibling)){
				if(partnerOfSibling instanceof MemberModel){ // invocation is a member model
					boolean accessPartnerComesBeforePartnerOfSibling = ((MemberModel)accessPartner).getCharStart() < ((MemberModel)partnerOfSibling).getCharStart() 
					&& ((MemberModel)accessPartner).getCharEnd() < ((MemberModel)partnerOfSibling).getCharEnd();
					if(accessPartnerComesBeforePartnerOfSibling) return declarationParent.getShownChildren().indexOf(siblingDeclaration);
					else declarationIndex = declarationParent.getShownChildren().indexOf(siblingDeclaration) + 1;

					boolean accessPartnerIsParameter = ((MemberModel)accessPartner).getCharStart() > ((MemberModel)partnerOfSibling).getCharStart() 
					&& ((MemberModel)accessPartner).getCharEnd() < ((MemberModel)partnerOfSibling).getCharEnd();
					if(accessPartnerIsParameter) return declarationParent.getShownChildren().indexOf(siblingDeclaration); 
				}else if(accessPartner instanceof UserCreatedMethodInvocationModel 
						&& childrenOfAccessParent.contains(accessPartner)){ // invocation is a grouped model
					boolean accessPartnerComesBeforePartnerOfSibling = childrenOfAccessParent.indexOf(accessPartner) < childrenOfAccessParent.indexOf(partnerOfSibling);
					if(accessPartnerComesBeforePartnerOfSibling) return declarationParent.getShownChildren().indexOf(siblingDeclaration);
					else declarationIndex = declarationParent.getShownChildren().indexOf(siblingDeclaration) + 1;
				}else 
					return -1;
			}

			// invocations from methods in different instances
			if(!parentOfAccess.getParentArt().equals(partnerOfSibling.getParentArt().getParentArt())){  
				if(declarationParent instanceof NodeModel && parentOfAccess instanceof NodeModel){
					boolean accessPartnerComesBeforePartnerOfSibling = ((MemberModel)accessPartner).getCharStart() < ((MemberModel)partnerOfSibling).getCharStart() 
					&& ((MemberModel)accessPartner).getCharEnd() < ((MemberModel)partnerOfSibling).getCharEnd();
					if(accessPartnerComesBeforePartnerOfSibling) return declarationParent.getShownChildren().indexOf(siblingDeclaration);
					else declarationIndex = declarationParent.getShownChildren().indexOf(siblingDeclaration) + 1;
				}else if(parentOfAccess.getParentArt().getShownChildren().indexOf(parentOfAccess) 
						< partnerOfSibling.getParentArt().getParentArt().getShownChildren().indexOf(partnerOfSibling.getParentArt())){
					declarationIndex = declarationParent.getShownChildren().indexOf(siblingDeclaration);
				}
			}
		}
		return declarationIndex;
	}

	private static void removeOverrideConnections(MemberModel model, CompoundCommand command){
		List<ConnectionModel> overrideConnections = new ArrayList<ConnectionModel>();
		if(model instanceof MethodBoxModel){
			if(((MethodBoxModel)model).getOverridesConnection()!= null)
				overrideConnections.add(((MethodBoxModel)model).getOverridesConnection());
			if(!((MethodBoxModel)model).getOverriderConnections().isEmpty())
				overrideConnections.addAll(((MethodBoxModel)model).getOverriderConnections());
		}
		for(ConnectionModel conn:overrideConnections){
			command.add(new ConnectionDeleteCommand(conn));
		}
	}

	private static void removeChildPartnerConnections(MemberModel model, 
			CompoundCommand command, 
			List<EditPart> selectedParts, NodeModel parent){
		for (MemberModel child:model.getMemberChildren()){
			if (child instanceof MethodInvocationModel){
				MethodBoxModel childPartner = (MethodBoxModel) child.getPartner();
				if (childPartner != null 
						&& childPartner.getInstanceModel() !=null
						&& !isDuplicateCommand(selectedParts, childPartner)){ 
					command.add(new ConnectionDeleteCommand(child.getOutgoingConnection()));
					command.add(new ConnectionDeleteCommand(child.getIncomingConnection()));

					//self Invocation and the invocation is not in the selected parts list
					if (childPartner.getInstanceModel().equals(model.getInstanceModel()) 
							&& !isDuplicateCommand(selectedParts, child)) {
						// change of parent should be called only if the parent member is deleted. If the instance is deleted the 
						// connections to members in other instances should be removed
						if (!isDuplicateCommand(selectedParts, childPartner.getInstanceModel()))
							command.add(new ChangeParentOfMethodCommand(childPartner,parent,parent.getChildren().indexOf(model)));
						else 
							removeChildPartnerConnections(childPartner, command, selectedParts, childPartner.getInstanceModel());
					}
				}
			} else if (child.getType() == MemberModel.access) {
				ArtifactFragment childPartner = getPartner(child);
				if (childPartner instanceof FieldModel)
					deleteFieldDeclaration((FieldModel) childPartner, ((FieldModel)childPartner).getParent(), command, true);
				else if (getPartner(child) instanceof GroupedFieldModel)
					GroupedMemberUtil.deleteGroupedFieldDeclaration((GroupedFieldModel) childPartner, (NodeModel) ((GroupedFieldModel)childPartner).getParent(), command);
			}

			// create the command only once and that for the last child in the Child list.
			for (ControlFlowBlock block : child.getConditionalBlocksContainedIn()) {
				int lastStmtIndx = block.getStatements().size() - 1;
				if (!block.getStatements().get(lastStmtIndx).equals(child))
					break;
				command.add(new ControlFlowDeleteCommand(block));
			}
		}
	}

	private static boolean isDuplicateCommand(List<EditPart> selectedParts, Object modelOrConnToDelete){
		for(EditPart part: selectedParts){
			if(part.getModel().equals(modelOrConnToDelete)) //delete command has been created or will be created 
				return true;
		}
		return false;
	}

	public static void deleteFieldDeclaration(FieldModel fieldModel, NodeModel parent, 
			CompoundCommand command, boolean removeFromControlBlock) {
		ArtifactFragment partner = getPartner(fieldModel);
		if(partner != null) //delete connection
			command.add(new ConnectionDeleteCommand(fieldModel.getIncomingConnection()));
		//delete declaration
		command.add(new MemberDeleteCommand(parent, fieldModel, removeFromControlBlock)); 
		//delete Invocation 
		if(partner!=null){
			if(partner instanceof FieldModel)
				command.add(new MemberDeleteCommand((NodeModel)partner.getParentArt(),(FieldModel)partner, removeFromControlBlock));
			else if(partner instanceof MemberModel)
				command.add(new GroupedFieldDeleteCommand((NodeModel) ((MemberModel) partner).getParent(),(GroupedFieldModel) partner));
		}
	}

	public static ArtifactFragment getPartner(ArtifactFragment model){
		ArtifactFragment partner = null; 
		if(model instanceof MemberModel && ((MemberModel) model).getPartner()!= null)
				partner = ((MemberModel) model).getPartner();
		return partner;
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
	public static void getMemberDeleteCommand(MemberModel model, NodeModel parent, List<EditPart> selectedParts, CompoundCommand command){

		//Instance is not present in the selected list
		if(isDuplicateCommand(selectedParts, parent) || isDuplicateCommand(selectedParts, model.getInstanceModel())) return;

		if(model instanceof FieldModel && !isDuplicateCommand(selectedParts, model.getParent())){
			if(((FieldModel)model).isDeclaration())
				deleteFieldDeclaration((FieldModel) model, parent, command, true);
			else{
				ArtifactFragment fieldPartner = getPartner(model);
				if(!isDuplicateCommand(selectedParts, fieldPartner) && fieldPartner instanceof FieldModel)
					deleteFieldDeclaration((FieldModel) fieldPartner, ((FieldModel) fieldPartner).getParent(), command, true);
				else if(fieldPartner instanceof GroupedFieldModel){
					GroupedMemberUtil.deleteGroupedFieldDeclaration((GroupedFieldModel) fieldPartner, (NodeModel) ((GroupedFieldModel) fieldPartner).getParent(), command);
				}
			}
			return;
		}
		ArtifactFragment partner = getPartner(model);
		if(model.isDeclaration())
			getDeclarationDeleteCommand(model, parent, selectedParts, command, true);
		else if(partner != null && !isDuplicateCommand(selectedParts, partner))
			getInvocationDeleteCommand(model, parent, command, selectedParts);
	}

	public static void getInvocationDeleteCommand(MemberModel invocation,
			NodeModel parent, CompoundCommand command, List<EditPart> selectedParts) {
		if(invocation.getOutgoingConnection()!= null)
			command.add(new ConnectionDeleteCommand(invocation.getOutgoingConnection()));
		if(invocation.getIncomingConnection()!= null)
			command.add(new ConnectionDeleteCommand(invocation.getIncomingConnection()));

		// When an invocation is deleted, declaration remains. With a call to 
		// the same class, the invocation contains the declaration, so in 
		// order for the declaration to remain in this case and not be deleted,
		// need to move the declaration to be a child of the instance
		ArtifactFragment partner = getPartner(invocation);
		if(partner instanceof MemberModel){
			MemberModel declaration = (MemberModel) partner;
			if(declaration.getInstanceModel().equals(invocation.getInstanceModel())) {
				int topLevelDeclIndex = -1;
				MethodBoxModel declContainer = (MethodBoxModel) invocation.getParent();
				while(declContainer.getParent() instanceof MethodBoxModel) {
					declContainer = (MethodBoxModel) declContainer.getParent();
				}
				topLevelDeclIndex = invocation.getInstanceModel().getChildren().indexOf(declContainer);
				command.add(new ChangeParentOfMethodCommand((MethodBoxModel)declaration, ((MethodBoxModel)invocation).getInstanceModel(), topLevelDeclIndex+1));
			}
		}
		command.add(new MemberDeleteCommand(parent, invocation, true));
		deleteControlFlowBlock(invocation, command, true, selectedParts);
		collapseControlFlowBlock(invocation, command, true, selectedParts);
	}

	public static void getDeclarationDeleteCommand(MemberModel model,
			NodeModel parent, List<EditPart> selectedParts, CompoundCommand command, boolean removeFromControlBlocks) {

		ArtifactFragment invocation = getPartner(model);
		removeOverrideConnections(model, command);
		removeChildPartnerConnections(model, command, selectedParts, parent);
		if(invocation!=null){
			command.add(new ConnectionDeleteCommand(model.getIncomingConnection()));
			command.add(new ConnectionDeleteCommand(model.getOutgoingConnection()));
		}
		command.add(new MemberDeleteCommand(parent,model, removeFromControlBlocks));

		if(invocation instanceof MemberModel){
			command.add(new MemberDeleteCommand(((MemberModel) invocation).getParent(),(MemberModel) invocation, removeFromControlBlocks));
			deleteControlFlowBlock((MemberModel) invocation, command, removeFromControlBlocks, selectedParts);
			collapseControlFlowBlock((MemberModel) invocation, command, removeFromControlBlocks, selectedParts);
		} else if(invocation instanceof UserCreatedMethodInvocationModel){
			command.add(new UserMethodBoxDeleteCommand((NodeModel) ((MemberModel) invocation).getParent(),(MethodBoxModel) invocation));
		}
	}

	private static void collapseControlFlowBlock(MemberModel invocation,
			CompoundCommand command, boolean removeFromControlBlocks,
			List<EditPart> selectedParts) {
		if(!removeFromControlBlocks) return;
		List<MemberModel> invocList = getInvocationList(selectedParts);
		for(ControlFlowBlock block : invocation.getConditionalBlocksContainedIn()){
			if(!(block instanceof IfBlock) || !doCollapse(invocation, (IfBlock) block, invocList)) continue;
			//collapse block
			UpdateControlBlockCommand updateCmd = new UpdateControlBlockCommand((IfBlock) block,invocation);
			command.add(updateCmd);
		}
	}

	private static boolean doCollapse(MemberModel invocation,
			IfBlock block, List<MemberModel> invocList) {
		// if all the invocations are going to be deleted then do not collapse
		// if a block has a outer conditional block dont remove it but collapse it
		if (invocList.containsAll(block.getStatements())
				&& !outerBlocksHaveChildren(block))
			return false;

		Boolean isLastInSelectedList = false;
		//find if the invocation is the last in the selected list for all the statements
		List<MemberModel> allStmts = block.getStatements();
		for(int i = invocList.size()-1 ; i>=0 ; i-- ){
			MemberModel invoc = invocList.get(i);
			if(allStmts.contains(invoc) && invocation.equals(invoc)) isLastInSelectedList = true;
		}

		// the commands will be created only by the first statement of the block to be collapsed
		List<MemberModel> thenStmts = block.getThenStmts();
		List<MemberModel> elseStmts = block.getElseStmts();
		Map<String, List<MemberModel>> elseIfStmts= block.getElseIfStmts();

		boolean ifEmpty = invocList.containsAll(thenStmts) && !thenStmts.isEmpty();
		boolean elseEmpty = invocList.containsAll(elseStmts) && !elseStmts.isEmpty();

		if(ifEmpty && thenStmts.size()>0 && isLastInSelectedList) return true;
		if(elseEmpty && elseStmts.size()>0 && isLastInSelectedList) return true;

		for(String str : elseIfStmts.keySet()){
			if(!elseIfStmts.get(str).isEmpty() && invocList.containsAll(elseIfStmts.get(str)) && elseIfStmts.get(str).size()>0 &&
					isLastInSelectedList)
				return true;
		}

		return false;
	}

	public static boolean outerBlocksHaveChildren(ControlFlowBlock block) {
		if (block == null)
			return false;
		if (!block.getStatements().isEmpty())
			return true;
		else
			return outerBlocksHaveChildren(block.getOuterConditionalBlock());
	}

	private static void deleteControlFlowBlock(MemberModel invocation, 
			CompoundCommand command,
			boolean removeFromControlBlocks, List<EditPart> selectedParts) {
		if(!removeFromControlBlocks) return;
		List<MemberModel> invocList = getInvocationList(selectedParts);
		for(ControlFlowBlock block : invocation.getConditionalBlocksContainedIn()){
			if(!doDelete(invocation, block, invocList) || outerBlocksHaveChildren(block)) continue;
			//delete block
			command.add(new ControlFlowDeleteCommand(block));
		}
	}
//	
//	public static void deleteControlFlowBlock(CompoundCommand command, boolean removeFromControlBlocks, List<EditPart> selectedParts) {
//		if(!removeFromControlBlocks) return;
////		List<MemberModel> invocList = getInvocationList(selectedParts);
////		for(ControlFlowBlock block : invocation.getConditionalBlocksContainedIn()){
////			if(!doDelete(invocation, block, invocList) || outerBlocksHaveChildren(block)) continue;
////			//delete block
//			command.add(new ControlFlowDeleteCommand(block));
////		}
//	}
//	

	private static boolean doDelete(MemberModel invocation,
			ControlFlowBlock block, List<MemberModel> invocList) {
		List<MemberModel> stmtList = new ArrayList<MemberModel>(block.getStatements());
		if(!invocList.containsAll(stmtList)) return false;
		return true;
	}

	private static List<MemberModel> getInvocationList(List<EditPart> selectedParts) {
		List<MemberModel> invocList = new ArrayList<MemberModel>();
		for (EditPart part : selectedParts) {
			if (!(part.getModel() instanceof ArtifactFragment))
				continue; // check for connections
			ArtifactFragment model = (ArtifactFragment) part.getModel();
			if (model instanceof MethodInvocationModel)
				invocList.add((MemberModel) model);
			else if (model instanceof MethodBoxModel && ((MethodBoxModel) model).getPartner() != null)
				invocList.add(((MethodBoxModel) model).getPartner());
			else if (model instanceof FieldModel) {
				if (((FieldModel) model).getType() == MemberModel.access)
					invocList.add((MemberModel) model);
				else
					invocList.add(((FieldModel) model).getPartner());
			}
		}
		return invocList;
	}


	public static void showFullOrAbbrevConnLabel(int value, MemberModel model) {
		boolean selected = value==EditPart.SELECTED || value==EditPart.SELECTED_PRIMARY;
		if(model.getIncomingConnection()!=null) {
			if(selected) model.getIncomingConnection().getLine().showFullLabelText();
			else model.getIncomingConnection().getLine().showAbbreviatedLabelText();
		}
		if(model.getOutgoingConnection()!=null) {
			if(selected) model.getOutgoingConnection().getLine().showFullLabelText();
			else model.getOutgoingConnection().getLine().showAbbreviatedLabelText();
		}
	}

	public static void updateFullOrAbbrvConnectionLabels(AbstractGraphicalEditPart part, boolean show) {

		int status;
		if (show) status = EditPart.SELECTED;
		else status = EditPart.SELECTED_NONE;
		if (part instanceof MemberEditPart &&  !(part.getModel() instanceof InstanceModel) 
				&& ((MemberModel)part.getModel()).getType() == MemberModel.access) {
			MemberUtil.showFullOrAbbrevConnLabel(status, (MemberModel) part.getModel());

			// check if we have self child add update those connections
			InstanceModel parent = ((MemberModel)part.getModel()).getInstanceModel();
			InstanceModel partnerParent = ((MemberModel)part.getModel()).getPartner().getInstanceModel(); 
			if (parent.equals(partnerParent))
				updateFullOrAbbrvConnectionLabels(((MemberEditPart)part).getPartnerEP(), show);
			return;
		}

		for (Object child : part.getChildren()) {
			if (child instanceof AbstractGraphicalEditPart)
				updateFullOrAbbrvConnectionLabels((AbstractGraphicalEditPart) child, show);
		}
	}

}
