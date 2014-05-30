package com.architexa.diagrams.jdt.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;


/**
 * @author abhishek
 * Class to get info about all the packages and class info in the workspace. This is specifically for the exploration server 
 */
public class ProjectInfo {
	
	private static Map<String, List<String>> projectToPackageMap = new HashMap<String, List<String>>();
	private static Map<String, List<String>> packageToClassMap = new HashMap<String, List<String>>();
	
	public static void getAllProjectInfo() {
		// Get the root of the workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		// Loop over all projects
		for (IProject project : projects) {
			try {
				populateProjectPackageMap(project);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void populateProjectPackageMap(IProject project) throws CoreException,
			JavaModelException {
		// Check if we have a Java project which is open
		if (project.isOpen() && project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
//			System.out.println("Working in project " + project.getName());
			IJavaProject javaProject = JavaCore.create(project);
			projectToPackageMap.put(javaProject.getElementName(), getPackageListInfos(javaProject));
		}
	}

	private static List<String> getPackageListInfos(IJavaProject javaProject)
			throws JavaModelException {
		IPackageFragment[] packages = javaProject.getPackageFragments();
		List<String> packageList = new ArrayList<String>();
		for (IPackageFragment mypackage : packages) {
			// Package fragments include all packages in the
			// classpath
			// We will only look at the package from the source
			// folder
			// K_BINARY would include also included JARS, e.g.
			// rt.jar
			if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				if (mypackage.getCompilationUnits().length == 0 ) continue; // Show only ones with classes
//				System.out.println("Package " + mypackage.getElementName());
				packageList.add(mypackage.getElementName());
				packageToClassMap.put(mypackage.getElementName(), printICompilationUnitInfo(mypackage));
			}
		}
		return packageList;
	}

	private static List<String> printICompilationUnitInfo(IPackageFragment mypackage)
			throws JavaModelException {
		List<String> classes = new ArrayList<String>();
		for (ICompilationUnit unit : mypackage.getCompilationUnits()) {
			classes.add(unit.getElementName());
//			System.out.println("Source file " + unit.getElementName());
//			Document doc = new Document(unit.getSource());
//			System.out.println("Has number of lines: " + doc.getNumberOfLines());
//			printIMethods(unit);
		}
		return classes;
	}

	private static void printIMethods(ICompilationUnit unit) throws JavaModelException {
		IType[] allTypes = unit.getAllTypes();
		for (IType type : allTypes) {
			IMethod[] methods = type.getMethods();
			for (IMethod method : methods) {

				System.out.println("Method name " + method.getElementName());
				System.out.println("Signature " + method.getSignature());
				System.out.println("Return Type " + method.getReturnType());

			}
		}
	}
}
