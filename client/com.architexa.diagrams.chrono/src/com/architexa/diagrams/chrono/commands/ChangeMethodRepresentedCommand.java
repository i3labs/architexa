package com.architexa.diagrams.chrono.commands;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;

import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.org.eclipse.gef.commands.Command;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class ChangeMethodRepresentedCommand extends Command {

	MethodBoxModel methodModel;

	IMethod originalIMethod;
	ASTNode originalASTNode;

	IMethod newIMethod;
	ASTNode newASTNode;

	public ChangeMethodRepresentedCommand(MethodBoxModel methodModel, IMethod newIMethod, ASTNode newASTNode) {
		this.methodModel = methodModel;
		this.originalIMethod = methodModel.getMethod();
		this.originalASTNode = methodModel.getASTNode();
		this.newIMethod = newIMethod;
		this.newASTNode = newASTNode;
	}

	@Override
	public void execute() {
		change();
	}

	@Override
	public void undo() {
		methodModel.changeMethodRepresented(originalIMethod, originalASTNode);
	}

	@Override
	public void redo() {
		change();
	}

	private void change() {
		methodModel.changeMethodRepresented(newIMethod, newASTNode);
	}

}
