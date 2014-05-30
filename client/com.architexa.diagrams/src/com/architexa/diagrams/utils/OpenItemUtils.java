package com.architexa.diagrams.utils;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.IStructuredSelection;

public class OpenItemUtils {
	
	public static boolean containsProject(IStructuredSelection sel) {
		for (Object obj : sel.toList()) {
			if (obj instanceof IPackageFragmentRoot || obj instanceof IJavaProject) return true; 
		}
		return false;
	}
	
	public static boolean containsPackage(IStructuredSelection sel) {
		for (Object obj : sel.toList()) {
			if (obj instanceof IPackageFragment) return true; 
		}
		return false;
	}
	
	public static boolean containsMethodOrField(IStructuredSelection sel) {
		for (Object obj : sel.toList()) {
			if (obj instanceof IField || obj instanceof IMethod) return true; 
		}
		return false;
	}

}
