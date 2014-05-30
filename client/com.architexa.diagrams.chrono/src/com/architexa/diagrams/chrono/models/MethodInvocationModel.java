package com.architexa.diagrams.chrono.models;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.openrdf.model.Resource;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class MethodInvocationModel extends MethodBoxModel {

	public MethodInvocationModel(InstanceModel instance, IMethod method, ASTNode node, int type) {
		super(instance, method, type);
		setASTNode(node);
	}

	public MethodInvocationModel(InstanceModel instance, Resource res, int type) {
		super(instance, res, type); 
	}


	public MethodInvocationModel(InstanceModel instance, int type) {
		super(instance, type);
	}

	@Override
	public void changeMethodRepresented(IMethod method, ASTNode node) {
		super.changeMethodRepresented(method, node);
		setASTNode(node);
	}

}
