package com.architexa.diagrams.chrono.controlflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.architexa.diagrams.chrono.commands.CollapseExpandButtonCommand;
import com.architexa.diagrams.chrono.commands.ConnectionCreateCommand;
import com.architexa.diagrams.chrono.commands.HiddenNodeCreateCommand;
import com.architexa.diagrams.chrono.commands.HiddenNodeDeleteCommand;
import com.architexa.diagrams.chrono.commands.InstanceCreateCommand;
import com.architexa.diagrams.chrono.commands.MemberCreateCommand;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.org.eclipse.draw2d.Button;
import com.architexa.org.eclipse.draw2d.FigureListener;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.PolylineConnection;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

/**
 * 
 * @author Elizabeth L. Murnane
 * 
 */
public class CollapseExpandButton extends Button implements FigureListener {

	String type = "";
	Label labelOfSection;

	List<MemberModel> stmts = new ArrayList<MemberModel>();
	List<CallInfo> callInfoList = new ArrayList<CallInfo>();
	Map<MemberModel, HiddenNodeModel> memberToHidden = new HashMap<MemberModel, HiddenNodeModel>();
	List<HiddenNodeModel> hiddenChildList = new ArrayList<HiddenNodeModel>();

	DiagramModel diagram;

	public CollapseExpandButton(String type, Label label,
			List<MemberModel> stmts, DiagramModel diagram) {
		super(type);
		this.type = type;
		this.labelOfSection = label;
		this.stmts = stmts;
		this.diagram = diagram;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator getListeners(Class clazz) {
		return super.getListeners(clazz);
	}

	public void setType(String type) {
		this.type = type;
		setContents(new Label(type));
	}

	public String getType() {
		return type;
	}

	public boolean sectionIsShowing() {
		return ControlFlowBlock.SHOWING.equals(getType());
	}

	public Label getLabelOfSection() {
		return labelOfSection;
	}

	public List<MemberModel> getStatements() {
		return stmts;
	}

	public Map<MemberModel, HiddenNodeModel> getMemberToHiddenMap() {
		return memberToHidden;
	}

	public List<HiddenNodeModel> getHiddenChildList() {
		return hiddenChildList;
	}

	public void collapseOrExpand(boolean switchType,
			CollapseExpandButtonCommand command) {
		if (getStatements().isEmpty())
			return;

		if (getType().equals(ControlFlowBlock.HIDING)) // expand the block
			removeHiddenAddCalls(command);
		else
			// Collapse block; add call Info's, remove calls and add
			// corresponding hidden
			removeCallsAddHidden(command);

	}

	private void removeHiddenAddCalls(CollapseExpandButtonCommand command) {
		for (CallInfo call : callInfoList) {
			command.add(new MemberCreateCommand(call.invocation,
					call.invocationParent, ""));

			if (call.declarationParent instanceof InstanceModel
					&& !diagram.getChildren().contains(call.declarationParent)) { // If
				// declaration
				// parent
				// not
				// present
				int index = diagram.getChildren().indexOf(
						call.invocationParent.getInstanceModel()) + 1;
				command.add(new InstanceCreateCommand(
						(InstanceModel) call.declarationParent, diagram, index,
						true, true));
			}
			MemberCreateCommand createDeclaration = new MemberCreateCommand(
					call.declaration, call.declarationParent, "");
			command.add(createDeclaration);
			createDeclaration.setAccessPartner(call.invocation,
					call.invocationParent);
			command.add(new ConnectionCreateCommand(call.invocation,
					call.declaration, call.callMessage, ConnectionModel.CALL));
			if (call.returnMessage != null)
				command.add(new ConnectionCreateCommand(call.declaration,
						call.invocation, call.returnMessage,
						ConnectionModel.RETURN));
		}

		for (HiddenNodeModel hidden : getHiddenChildList())
			command.add(new HiddenNodeDeleteCommand(hidden, (NodeModel) hidden
					.getParentArt(), true, hidden.getTooltip()));

	}

	private void removeCallsAddHidden(CollapseExpandButtonCommand command) {
		getMemberToHiddenMap().clear();
		getHiddenChildList().clear();
		callInfoList.clear();
		for (MemberModel invocation : getStatements()) {
			if (invocation.getParent() != null) {
				addToCallList(invocation);
				removeCallChain(invocation.getPartner(), command);
				// remove calls
				if (invocation.getIncomingConnection() != null)
					MemberUtil.getDeclarationDeleteCommand(invocation
							.getPartner(), invocation.getPartner().getParent(),
							new ArrayList<EditPart>(), command, false);
				else
					MemberUtil.deleteFieldDeclaration((FieldModel) invocation
							.getPartner(), invocation.getPartner().getParent(),
							command, false);

				// add hidden
				HiddenNodeModel hiddenModelInvoc = new HiddenNodeModel(true,
						invocation.getParent(), invocation.getName(),
						invocation.getConditionalBlocksContainedIn());
				command.add(new HiddenNodeCreateCommand(hiddenModelInvoc,
						invocation.getParent(), invocation.getParent()
						.getChildren().indexOf(invocation)));
				getMemberToHiddenMap().put(invocation, hiddenModelInvoc);
				getHiddenChildList().add(hiddenModelInvoc);
			}
		}
	}

	// Method to remove call chains originating from this control flow block
	private void removeCallChain(MemberModel model, CompoundCommand command) {
		if (model.getMemberChildren().isEmpty())
			return;
		for (MemberModel invocation : model.getMemberChildren()) {
			addToCallList(invocation);
			removeCallChain(invocation.getPartner(), command);
			if (invocation.getIncomingConnection() != null)
				MemberUtil.getDeclarationDeleteCommand(invocation.getPartner(),
						invocation.getPartner().getParent(),
						new ArrayList<EditPart>(), command, false);
			else
				MemberUtil.deleteFieldDeclaration((FieldModel) invocation
						.getPartner(), invocation.getPartner().getParent(),
						command, false);
		}
	}

	private void addToCallList(MemberModel invocation) {
		if (invocation.getPartner() == null)
			return;
		MemberModel declaration = invocation.getPartner();
		String outgoing = invocation.getOutgoingConnection().getLabel();
		String incoming = invocation.getIncomingConnection() != null ? invocation
				.getIncomingConnection().getLabel()
				: null;
				callInfoList.add(new CallInfo((MemberModel) invocation.getParent(),
						(NodeModel) declaration.getParentArt(), invocation,
						declaration, outgoing, incoming));
	}

	public class CallInfo {
		MemberModel invocationParent;
		NodeModel declarationParent;
		MemberModel invocation;
		MemberModel declaration;
		String callMessage;
		String returnMessage;
		boolean declarationParentNew;

		public CallInfo(MemberModel invocationParent,
				NodeModel declarationParent, MemberModel invocation,
				MemberModel declaration, String callMessage,
				String returnMessage) {
			this.invocationParent = invocationParent;
			this.declarationParent = declarationParent;
			this.invocation = invocation;
			this.declaration = declaration;
			this.callMessage = callMessage;
			this.returnMessage = returnMessage;
		}
	}

	// set location of button out of the display
	public void hideButton() {
		Point p = new Point(-50, -50);
		this.setLocation(p);
		return;
	}

	public void figureMoved(IFigure source) {
		if (!(source instanceof Label) && !(source instanceof ControlFlowBlock))
			return;

		// this section is to hide the button when there are no statements in
		// the button
		boolean allStmtsAndCollapsedRemovedFromDiagram = true;
		for (MemberModel model : stmts) {
			if (model.getParent() != null) {
				allStmtsAndCollapsedRemovedFromDiagram = false;
				break;
			}
		}

		for (MemberModel model : getMemberToHiddenMap().keySet()) {
			if (getMemberToHiddenMap().get(model).getParentArt() != null) {
				allStmtsAndCollapsedRemovedFromDiagram = false;
				break;
			}
		}

		if (allStmtsAndCollapsedRemovedFromDiagram && this.getParent() != null) {
			hideButton();
			return;
		}
		// ***********************
		int size = Math.max(getMinimumSize().height, getMinimumSize().width);
		setSize(size, size);

		int x;
		if (source instanceof Label
				&& source.getParent() instanceof PolylineConnection) {
			x = ((PolylineConnection) source.getParent()).getEnd().x - size;
		} else if (source instanceof Label) {
			x = source.getParent().getBounds().getTopRight().x - size;
		} else {
			x = source.getBounds().getTopRight().x - size;
		}
		setLocation(new Point(x, labelOfSection.getBounds().getTopRight().y));
	}

}
