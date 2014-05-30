package com.architexa.diagrams.chrono.controlflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.action.Action;

import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.MethodInvocationModel;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartViewer;

public class AddUserCreatedControlFlowAction extends Action {
	
	private List<EditPart> selectedEps;
	private DiagramEditPart diagramEP;
	private EditPartViewer viewer;

	public AddUserCreatedControlFlowAction(List<EditPart> selectedEps, DiagramEditPart diagramEP, EditPartViewer viewer) {
		super("Add Control Flow");
		this.selectedEps = selectedEps;
		this.diagramEP = diagramEP;
		this.viewer = viewer;
	}
	
	@Override
	public void run() {
		UserCreatedControlFlowModel cfModel = new UserCreatedControlFlowModel((DiagramModel) diagramEP.getModel(), "Double Click to edit conditional statement");
		List<MemberModel> stmts = new ArrayList<MemberModel>();
		for (EditPart selectedEP : selectedEps) {
			if (selectedEP.getModel() instanceof MemberModel ) {
				MemberModel model = (MemberModel) selectedEP.getModel();
				// might need something like this to account for nested conditionals
				//List<ControlFlowBlock> containers = model.getConditionalBlocksContainedIn();
				//if (containers!=null && containers.size()>0) {
				//	System.err.println();
				//}
				if (model instanceof MethodBoxModel) { // add ivocation model of selected methods
					ConnectionModel inCon = model.getIncomingConnection();
					if (inCon == null) continue;
					ArtifactFragment src = inCon.getSource();
					if (src instanceof MethodInvocationModel)
						stmts.add((MemberModel) src);
				}
			}
			
		}
		Collections.sort(stmts, new Comparator<MemberModel>() {
			public int compare(MemberModel arg0, MemberModel arg1) {
				Point top1 = arg0.getFigure().getBounds().getTopLeft();
				Point top2 = arg1.getFigure().getBounds().getTopLeft();
				if (top1.y < top2.y ) return 0;
				else return 1;
			}
		});
		
		cfModel.setStatements(stmts );
		AddLoopBlockCommand cmd = new AddLoopBlockCommand((DiagramModel) diagramEP.getModel(), cfModel);
		((DiagramEditPart) viewer.getContents()).execute(cmd);
	}

}
