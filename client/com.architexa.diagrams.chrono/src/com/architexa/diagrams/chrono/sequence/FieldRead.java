package com.architexa.diagrams.chrono.sequence;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class FieldRead {

	private Expression fieldRead;

	public FieldRead(FieldAccess fieldAccess) {
		this.fieldRead = fieldAccess;
	}

	public FieldRead(SuperFieldAccess superFieldAccess) {
		fieldRead = superFieldAccess;
	}

	public FieldRead(QualifiedName qualifiedName) {
		fieldRead = qualifiedName;
	}

	public Expression getFieldRead() {
		return fieldRead;
	}

	public final int getStartPosition() {
		return fieldRead.getStartPosition();
	}

	public final int getLength() {
		return fieldRead.getLength();
	}

	public SimpleName getName() {
		if(fieldRead instanceof FieldAccess) return ((FieldAccess)fieldRead).getName();
		if(fieldRead instanceof SuperFieldAccess) return ((SuperFieldAccess)fieldRead).getName();
		if(fieldRead instanceof QualifiedName) return ((QualifiedName)fieldRead).getName();
		return null;
	}

	public IVariableBinding resolveFieldBinding() {
		if(fieldRead instanceof FieldAccess) return ((FieldAccess)fieldRead).resolveFieldBinding();
		if(fieldRead instanceof SuperFieldAccess) return ((SuperFieldAccess)fieldRead).resolveFieldBinding();
		if(fieldRead instanceof QualifiedName) return (IVariableBinding) ((QualifiedName)fieldRead).resolveBinding();
		return null;
	}

	@Override
	public String toString() {
		return getStringRepresentationOfFieldRead(fieldRead);
	}

	public static String getStringRepresentationOfFieldRead(ASTNode node) {
		if(node==null) return "";
		return node.toString();
	}

}
