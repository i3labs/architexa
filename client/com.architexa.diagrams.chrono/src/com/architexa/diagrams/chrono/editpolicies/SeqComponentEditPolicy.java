package com.architexa.diagrams.chrono.editpolicies;

import java.util.ArrayList;
import java.util.List;

import com.architexa.diagrams.chrono.commands.ControlFlowDeleteCommand;
import com.architexa.diagrams.chrono.controlflow.ControlFlowBlock;
import com.architexa.diagrams.chrono.controlflow.ControlFlowEditPart;
import com.architexa.diagrams.chrono.controlflow.ControlFlowModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.GroupedInstanceModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.util.DerivedInstanceUtil;
import com.architexa.diagrams.chrono.util.GroupedMemberUtil;
import com.architexa.diagrams.chrono.util.InstanceUtil;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editpolicies.ComponentEditPolicy;
import com.architexa.org.eclipse.gef.requests.GroupRequest;
/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqComponentEditPolicy extends ComponentEditPolicy {

	@Override
	protected Command createDeleteCommand(GroupRequest request) {

		Object parent = getHost().getParent().getModel();
		Object child = getHost().getModel();
		CompoundCommand command = new CompoundCommand();
		List<EditPart> selectedParts = new ArrayList<EditPart>(request.getEditParts());

		if(parent instanceof DiagramModel && child instanceof NodeModel) {
				command.setLabel("Delete Instance");
				InstanceUtil.getInstanceDeleteCommand((DiagramModel)parent, (InstanceModel)child, selectedParts, command);
		}
		
		if(child instanceof ControlFlowModel) {
			command.setLabel("Delete Control Flow");
			command.add(new ControlFlowDeleteCommand((ControlFlowBlock) ((ControlFlowEditPart) getHost()).getFigure()));
		}
		

		if(parent instanceof NodeModel && child instanceof MemberModel) {
			if (((MemberModel) child).isUserCreated()) {
				command.setLabel("Delete User Created Memeber");
				GroupedMemberUtil.getGroupedMemberDeleteCommand((NodeModel)parent, (MemberModel)child, selectedParts, command);
			} else {
				command.setLabel("Delete Member");
				MemberUtil.getMemberDeleteCommand((MemberModel)child, (NodeModel)parent, selectedParts, command);
			}
		}
			

		if(parent instanceof DiagramModel && child instanceof GroupedInstanceModel) {
			command.setLabel("Delete Grouped Instance");
			DerivedInstanceUtil.getGroupedInstanceDeleteCommand((DiagramModel)parent, (GroupedInstanceModel)child, true, selectedParts, command);
		}
		
		// check if command list is empty
		if(!command.getCommands().isEmpty())
			return command;
		else
			return super.createDeleteCommand(request);
	}
}
