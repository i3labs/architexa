/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
package com.architexa.diagrams.relo.jdt.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ScriptAction implements IObjectActionDelegate {

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

	ArrayList<Object> selList = new ArrayList<Object>(10);

    @SuppressWarnings("unchecked")
    private static List<Object> asListObject(List sourceConnections) {
        return sourceConnections;
    }
    
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
    public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) selection;
			//printTypes("selectionChanged", iss.toList());
			selList.clear();
			selList.addAll(asListObject(iss.toList()));
		} else {
			System.err.println(
				"Selection not structured! Type:"
					+ selection.getClass().toString());
		}
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		try {
			//System.err.println(
			//	"trying to execute action: "
			//		+ action.getClass()
			//		+ " -- "
			//		+ action.getText());
			
		    //ConsoleView.logBeg("Running script");
			Iterator<?> selIt = selList.iterator();
			while (selIt.hasNext()) {
				IJavaElement ije = (IJavaElement) selIt.next();
				process(ije);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    //ConsoleView.logEnd();
		}
		/*
		Shell shell = new Shell();
		MessageDialog.openInformation(
			shell,
			"JBrowse Plug-in",
			"New Action was executed.");
		*/
	}

	/**
	 * @param ije
	 * @throws JavaModelException
	 */
	private void process(IJavaElement ije) throws JavaModelException {
			
		/*
		 * BrowseLoggingView.log("Processing," + ije.getElementName()); if (ije
		 * instanceof ICompilationUnit) { ije =
		 * ((ICompilationUnit)ije).findPrimaryType();
		 * //BrowseLoggingView.log("Processing," + ije.getElementName()); }
		 * 
		 * if (ije instanceof IType) { CodeUnit typeCU = new CodeUnit(ije);
		 * //Iterator superTypeIt = typeCU.getAllSuperTypesCU().iterator();
		 * //Iterator superTypeIt = typeCU.getSuperTypesCU().iterator();
		 * Iterator superTypeIt = typeCU.printAllSuperTypesPairsCU().iterator();
		 * while(superTypeIt.hasNext()) { BrowseLoggingView.log("Supertype," +
		 * ije.getElementName()+","+ superTypeIt.next().toString()); }
		 *  } else { BrowseLoggingView.log("Unexpected type: " +
		 * ije.getClass()); }
		 */
	}


}
