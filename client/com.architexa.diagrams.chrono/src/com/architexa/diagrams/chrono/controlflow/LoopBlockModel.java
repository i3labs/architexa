package com.architexa.diagrams.chrono.controlflow;

import java.util.ArrayList;
import java.util.List;

import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.MemberModel;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class LoopBlockModel extends ControlFlowModel {

	public LoopBlockModel(DiagramModel diagram, String conditionalLabel) {
		super(diagram, conditionalLabel);
	}


	private List<MemberModel> loopStmts = new ArrayList<MemberModel>();
	private List<MemberModel> origLoopStmts ;


	@Override
	public List<MemberModel> getStatements() {
		return loopStmts;
	}
	
	public void setLoopStmts(List<MemberModel> loopStmts) {
		
		this.loopStmts = loopStmts;
		this.origLoopStmts = new ArrayList<MemberModel>(loopStmts);
	}
	
}
