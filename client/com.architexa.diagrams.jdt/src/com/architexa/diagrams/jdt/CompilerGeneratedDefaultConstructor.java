package com.architexa.diagrams.jdt;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceMethod;

/**
 * IBinding.getJavaElement() returns null in cases where the binding 
 * corresponds to the compiler generated default constructor of a source 
 * class. Use CompilerGeneratedDefaultConstructor to represent the java 
 * element for such default constructors.
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class CompilerGeneratedDefaultConstructor extends SourceMethod implements IMethod {


	public CompilerGeneratedDefaultConstructor(String name, IJavaElement declaringClass) {
		super((JavaElement)declaringClass, name, new String[]{});
	}

	@Override
	public String[] getParameterNames() throws JavaModelException {
		return new String[]{};
	}

	@Override
	public String[] getRawParameterNames() throws JavaModelException {
		return new String[]{};
	}

	@Override
	public String getReturnType() throws JavaModelException {
		return Signature.SIG_VOID;
	}

	@Override
	public boolean isConstructor() throws JavaModelException {
		return true;
	}

	@Override
	public boolean isMainMethod() throws JavaModelException {
		return false;
	}

	@Override
	public String[] getCategories() throws JavaModelException {
		return new String[]{};
	}

	@Override
	public String toString() {
		String s = super.toString();
		return markCompilerGenerated(s);
	}

	public static String markCompilerGenerated(String s) {
		s = s.replace("(not open)", "");
		StringBuffer buff = new StringBuffer(s);
		int paramParen = s.indexOf(")");
		buff.insert(paramParen+1, " (compiler generated)");
		s = buff.toString();
		return s;
	}

}
