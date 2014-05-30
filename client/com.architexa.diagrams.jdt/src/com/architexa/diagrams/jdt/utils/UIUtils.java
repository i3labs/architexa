package com.architexa.diagrams.jdt.utils;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.Signature;

import com.architexa.diagrams.jdt.Activator;

// VS: Don't like the name
public class UIUtils {
	
    static final Logger logger = Activator.getLogger(UIUtils.class);

	public static String getName(IJavaElement element) {
		String name = null;
		if(element instanceof IMethod || element instanceof IField) {
			IMember member = (IMember) element;

			String className = member.getDeclaringType().getFullyQualifiedName();
			int index = className.lastIndexOf('.');
			if(index==-1) {
				name = className;
			} else {
				name = className.substring(0, index) + "$" + className.substring(index+1) + "." + member.getElementName();
			}
			if(element instanceof IMethod) {
				String[] parameters = ((IMethod)element).getParameterTypes();
				String parameterList ="";
				int i=0;
				while(i < parameters.length) {
					parameterList = parameterList.concat(Signature.getSignatureSimpleName(parameters[i]) + ",");
					i++;
				}
				name = name + "(" + parameterList + ")";
			}
		} else if (element instanceof IPackageFragment) {
			IPackageFragment pack = (IPackageFragment)element;
			name = pack.getElementName();
		} else if(element instanceof ICompilationUnit) {
			ICompilationUnit icu = (ICompilationUnit)element;

			String className = icu.findPrimaryType().getFullyQualifiedName();
			int index = className.lastIndexOf('.');
			if(index==-1) {
				name = className;
			} else {
				name = className.substring(0, index) + "$" + className.substring(index+1);
			}
		}
		return name;
	}

}
