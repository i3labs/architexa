package com.architexa.diagrams.chrono.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;

import com.architexa.diagrams.chrono.sequence.FieldRead;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class FieldReadFinder extends ASTVisitor {

	List<FieldRead> fieldReads = new ArrayList<FieldRead>();

	public FieldReadFinder(MethodDeclaration methodDeclaration) {
		methodDeclaration.accept(this);
	}

	@Override
	public boolean visit(FieldAccess node) {
		fieldReads.add(new FieldRead(node));
		return false;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		fieldReads.add(new FieldRead(node));
		return false;
	}

	@Override
	public boolean visit(QualifiedName node) {
		fieldReads.add(new FieldRead(node));
		return false;
	}

	/**
	 * 
	 * @return a list of all the field accesses made in the compilation unit
	 */
	public List<FieldRead> getAllReads() {
		return fieldReads;
	}

}
