package com.architexa.diagrams.chrono.commands;


import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.util.InstanceUtil;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.org.eclipse.gef.commands.Command;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class TransferMethodConnectionsCommand extends Command {

	ConnectionModel callConn;
	ConnectionModel returnConn;
	MethodBoxModel invocationModel;
	MethodBoxModel declarationModel;
	MethodBoxModel origInvocationModel;
	MethodBoxModel origDeclarationModel;

	public TransferMethodConnectionsCommand(ConnectionModel callConn, ConnectionModel returnConn, MethodBoxModel invocation, MethodBoxModel declaration) {
		this.callConn = callConn;
		this.returnConn = returnConn;
		this.invocationModel = invocation;
		this.declarationModel = declaration;
		origInvocationModel = (MethodBoxModel) callConn.getSource();
		origDeclarationModel = (MethodBoxModel) callConn.getTarget();
	}

	@Override
	public boolean canExecute() {
		if (callConn == null || returnConn == null) return false;

		// can't have source -> source connections
		if (invocationModel.equals(declarationModel)) return false;

		// does source -> target connection already exist
		if(invocationModel.equals(callConn.getSource()) && 
				declarationModel.equals(callConn.getTarget())) return false;

		return true;
	}

	@Override
	public void execute() {

		updateMethodModels(invocationModel, declarationModel);
		updateConnectionLabels(invocationModel, declarationModel);
		
		callConn.reconnect(invocationModel, declarationModel);
		returnConn.reconnect(declarationModel, invocationModel);
	}

	private void updateMethodModels(MethodBoxModel invocation, MethodBoxModel declaration) {
		
		// the original call was a self call so now that declaration will be instance parents child
		for(MethodBoxModel callToSameClass : invocation.getMethodChildren()) {
			invocation.removeChild(callToSameClass);
			invocation.getInstanceModel().addChild(callToSameClass);
		}
		invocation.changeMethodRepresented(declaration.getMethod(), null);

		// declaration is already being called by some other method so create new declaration
		if(declaration.getIncomingConnection()!=null) {
			MethodBoxModel newModelForSameDeclaration = new MethodBoxModel(declaration.getInstanceModel(), declaration.getMethod(), MethodBoxModel.declaration);
			declaration.getInstanceModel().addChild(newModelForSameDeclaration);
			declaration = newModelForSameDeclaration;
		}
		
		// if declaration needs to be a self call 
		if(declaration.getInstanceModel().equals(invocation.getInstanceModel())){
			declaration.getInstanceModel().removeChild(declaration);
			invocation.addChild(declaration);
		}
		
	}

	private void updateConnectionLabels(MethodBoxModel invocation, MethodBoxModel declaration) {

		boolean isAConstructorCall = false;
		if (declaration.getASTNode() instanceof MethodDeclaration)
			isAConstructorCall = ((MethodDeclaration)declaration.getASTNode()).isConstructor();
		boolean isASuperCall = InstanceUtil.isASubClass(invocation.getInstanceModel(), declaration.getInstanceModel());
		boolean isACallToTheSameClass = invocation.getInstanceModel().equals(declaration.getInstanceModel());

		AST ast = ((MethodDeclaration)invocation.getDeclarationContainer().getASTNode()).getAST();
		Invocation newInvocation;
		if(isASuperCall && isAConstructorCall) {
			newInvocation = new Invocation(ast.newSuperConstructorInvocation());
		}
		else if(isASuperCall) {
			newInvocation = new Invocation(ast.newSuperMethodInvocation());
		}
		else if(isAConstructorCall && isACallToTheSameClass) {
			declaration.getParent().removeChild(declaration);
			invocation.addChild(declaration);
			newInvocation = new Invocation(ast.newConstructorInvocation());
		}
		else if(isAConstructorCall) {
			newInvocation = new Invocation(ast.newClassInstanceCreation());
		}
		else {
			newInvocation = new Invocation(ast.newMethodInvocation());
		}
		if (declaration.getASTNode() instanceof MethodDeclaration)
			newInvocation.setMethodBinding(((MethodDeclaration)declaration.getASTNode()).resolveBinding());

		callConn.setLabel(MethodUtil.getMethodName(declaration.getMethodRes(), newInvocation));
		returnConn.setLabel(MethodUtil.getReturnMessage(newInvocation));
	}
	
	@Override
	public void undo(){
		updateMethodModels(origInvocationModel, origDeclarationModel);
		updateConnectionLabels(origInvocationModel, origDeclarationModel);
		
		callConn.reconnect(origInvocationModel, origDeclarationModel);
		returnConn.reconnect(origDeclarationModel, origInvocationModel);
	}
}
