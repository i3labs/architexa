package com.architexa.diagrams.chrono.controlflow;

import java.util.List;

import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.MemberModel;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class UserCreatedControlFlowModel extends ControlFlowModel {

	public UserCreatedControlFlowModel(DiagramModel diagram, String conditionalLabel) {
		super(diagram, conditionalLabel);
	}

	private List<MemberModel> userCreatedStatements;

	@Override
	public List<MemberModel> getStatements() {
		return userCreatedStatements;
	}
	
	public void setStatements(List<MemberModel> stmts) {
		userCreatedStatements = stmts;
	}
 
	public void addStatement(MemberModel stmt) {
		userCreatedStatements.add(stmt);
	}
	
}
