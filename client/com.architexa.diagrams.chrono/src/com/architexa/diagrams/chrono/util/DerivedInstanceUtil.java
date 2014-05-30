package com.architexa.diagrams.chrono.util;

import java.util.List;


import com.architexa.diagrams.chrono.commands.UserInstanceDeleteCommand;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.GroupedFieldModel;
import com.architexa.diagrams.chrono.models.GroupedInstanceModel;
import com.architexa.diagrams.chrono.models.GroupedMethodBoxModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.UserCreatedInstanceModel;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

public class DerivedInstanceUtil {

	/**
	 * Method to get command to delete grouped Instance from Diagram
	 * @param diagramModel
	 * @param groupedInstanceModel
	 * @param removePartners
	 * @param selectedParts 
	 * @param command
	 * @return
	 */
	public static CompoundCommand getGroupedInstanceDeleteCommand(DiagramModel diagramModel, GroupedInstanceModel groupedInstanceModel, boolean removePartners, List<EditPart> selectedParts, CompoundCommand command){

		if (removePartners) {
			for (int i = groupedInstanceModel.getChildren().size()-1; i >= 0; i--) {
				ArtifactFragment model = groupedInstanceModel.getChildren().get(i);
				if(model instanceof GroupedMethodBoxModel)
					GroupedMemberUtil.getDeclarationDeleteCommand((MemberModel) model, groupedInstanceModel, selectedParts, command);
				else if(model instanceof GroupedFieldModel)
					GroupedMemberUtil.deleteGroupedFieldDeclaration((GroupedFieldModel) model, groupedInstanceModel, command);
			}
		}
		UserInstanceDeleteCommand delGroupedInstance = new UserInstanceDeleteCommand(diagramModel, groupedInstanceModel, diagramModel.getChildren().indexOf(groupedInstanceModel));
		command.add(delGroupedInstance);
		return command;
	}

	public static CompoundCommand getUserInstanceDeleteCommand(DiagramModel diagramModel,
			UserCreatedInstanceModel instanceModel, CompoundCommand command) {
		UserInstanceDeleteCommand delGroupedInstance = new UserInstanceDeleteCommand(diagramModel, instanceModel, diagramModel.getChildren().indexOf(instanceModel));
		command.add(delGroupedInstance);
		return command;
	}
}
