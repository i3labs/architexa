package com.architexa.diagrams.chrono.util;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.dom.ASTNode;

import com.architexa.diagrams.chrono.commands.ConnectionCreateCommand;
import com.architexa.diagrams.chrono.commands.ConnectionDeleteCommand;
import com.architexa.diagrams.chrono.commands.GroupedFieldDeleteCommand;
import com.architexa.diagrams.chrono.commands.MemberDeleteCommand;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.GroupedFieldModel;
import com.architexa.diagrams.chrono.models.GroupedInstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

public class GroupedFieldUtil {
	public static void deleteGroupedField(NodeModel parent,
			GroupedFieldModel field, CompoundCommand command) {
		FieldModel fieldModel = ((GroupedFieldModel)field).getFieldModel();
		command.add(new MemberDeleteCommand(fieldModel.getParent(),fieldModel, true));
		command.add(new GroupedFieldDeleteCommand(parent,(GroupedFieldModel) field));
	}

	public static MemberModel createGroupedFieldAccess(
			ArtifactFragment fieldAccess, ArtifactFragment partner,
			GroupedInstanceModel groupedInstanceModel, CompoundCommand command) {

		ConnectionModel outGoing = null;
		ASTNode astNode = null;
		IField iField = null;
		GroupedFieldModel groupedFieldAccess = null;
		if (fieldAccess instanceof MemberModel) {
			outGoing = ((MemberModel) fieldAccess).getOutgoingConnection();
			astNode = ((FieldModel) fieldAccess).getASTNode();
			iField = ((FieldModel) fieldAccess).getMember();
			groupedFieldAccess = new GroupedFieldModel(groupedInstanceModel, iField, astNode, (FieldModel) fieldAccess);
		} else if (fieldAccess instanceof MemberModel) {
			outGoing = ((MemberModel) fieldAccess).getOutgoingConnection();
			astNode = ((GroupedFieldModel) fieldAccess).getASTNode();
			iField = ((GroupedFieldModel) fieldAccess).getMember();
			groupedFieldAccess = new GroupedFieldModel(groupedInstanceModel, iField, astNode,(FieldModel) ((GroupedFieldModel) fieldAccess).getFieldModel());
		}

		command.add(new ConnectionDeleteCommand(outGoing));
		command.add(new ConnectionCreateCommand(groupedFieldAccess,partner,"",ConnectionModel.CALL));

		return groupedFieldAccess;
	}

	public static void addFieldAccess(GroupedFieldModel groupedFieldAccess,
			CompoundCommand command) {
		ConnectionDeleteCommand connDel = new ConnectionDeleteCommand(groupedFieldAccess.getOutgoingConnection());
		command.add(connDel);
		ConnectionCreateCommand connCreate = new ConnectionCreateCommand(groupedFieldAccess.getFieldModel(),groupedFieldAccess.getPartner(),"", ConnectionModel.CALL);
		command.add(connCreate);
	}

	public static void addFieldDeclaration(GroupedFieldModel child,
			CompoundCommand command) {
		ConnectionDeleteCommand connDel = new ConnectionDeleteCommand(child.getIncomingConnection());
		command.add(connDel);
		ConnectionCreateCommand connCreate = new ConnectionCreateCommand(child.getPartner(),child.getFieldModel(),"",ConnectionModel.CALL);
		command.add(connCreate);
	}

}
