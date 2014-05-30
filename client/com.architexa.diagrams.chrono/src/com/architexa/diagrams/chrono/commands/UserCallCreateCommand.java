package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.UserCreatedMethodInvocationModel;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

public class UserCallCreateCommand extends UserConnCreateCommand {

	UserCreatedMethodInvocationModel invocation;
	private UserMethodBoxCreateCommand addInvocation;
	private ConnectionCreateCommand outgoing;
	private ConnectionCreateCommand incoming;
	private CompoundCommand command;
	private UserMethodBoxDeleteCommand delTargetFromInstace;
	private UserMethodBoxCreateCommand addTargetAsSrcChild;
	
	public UserCallCreateCommand(MethodBoxModel sourceModel) {
		super(sourceModel);
	}

	@Override
	public void execute() {
		setupCommands();
		addToCompoundCommand();
		executeCmd();
	}
	
	private void executeCmd() {
		if (command == null) return;
		command.execute();
	}

	private void setupCommands () {
		
		//Create Invocation
		if (sourceModel.getParent() instanceof InstanceModel)
			invocation = new UserCreatedMethodInvocationModel((InstanceModel) sourceModel.getParent(), MemberModel.access);
		else
			invocation = new UserCreatedMethodInvocationModel((InstanceModel) sourceModel.getInstanceModel(), MemberModel.access);
		// Add Invocation
		addInvocation = new UserMethodBoxCreateCommand(invocation, sourceModel, "");
		// Add outgoing
		outgoing = new ConnectionCreateCommand(invocation, targetModel, targetModel.getMethodName(), ConnectionModel.CALL);
		// Add Incoming
		incoming =  new ConnectionCreateCommand(targetModel, invocation, "void", ConnectionModel.RETURN);
		
		// Both in same instance
		if (sourceModel.getInstanceModel().equals(targetModel.getInstanceModel())) {
			// delete target from instance
			delTargetFromInstace = new UserMethodBoxDeleteCommand(targetModel.getInstanceModel(), (MethodBoxModel) targetModel);
			// add target as source of child
			addTargetAsSrcChild = new UserMethodBoxCreateCommand((MethodBoxModel) targetModel, invocation, "");
			addTargetAsSrcChild.setAccessPartner(invocation, sourceModel);
		}
	}
	
	private void addToCompoundCommand() {
		command = new CompoundCommand();
		if (delTargetFromInstace != null)
			command.add(delTargetFromInstace);
			
		command.add(addInvocation);
		if (addTargetAsSrcChild != null)
			command.add(addTargetAsSrcChild);
		command.add(outgoing);
		command.add(incoming);
	}

	@Override
	public void redo() {
		if (command == null) return;
		command.redo();
	}
	
	@Override
	public void undo() {
		if (command == null || !command.canUndo()) return;
		command.undo();
	}
}
