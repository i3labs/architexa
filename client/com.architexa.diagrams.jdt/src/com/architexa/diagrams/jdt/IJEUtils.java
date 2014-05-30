package com.architexa.diagrams.jdt;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class IJEUtils {

	public static boolean isNestedType(IType type) throws JavaModelException {
		// from binding documentation: Nested types ... subdivide into member types, local
		// types, and anonymous types
		// also: http://blogs.sun.com/darcy/entry/nested_inner_member_and_top
		if (type.isMember() || type.isLocal() || type.isAnonymous())
			return true;
		else
			return false;
	}

	/**
	 * Exists to bridge needs between Eclipse 3.2-3.5. Eclipse 3.2 does not have ITypeRoot
	 */
	public static boolean isTypeRoot(IJavaElement ije) {
		if (ije instanceof IClassFile || ije instanceof ICompilationUnit)
			return true;
		else
			return false;
	}

	public static IJavaElement ije_getTypeRoot(IJavaElement ije) {
		// ije.getTypeRoot() not available in Eclipse 3.2
		IJavaElement element = ije.getParent();
		while (element instanceof IMember) {
			element = element.getParent();
		}
		return element;
	}
}
