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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextEditorAction;

import com.architexa.diagrams.relo.jdt.JavaEditorlet;
import com.architexa.diagrams.relo.jdt.ParseUtilities;


/**
 * @author vineet
 *
 */
public class Script2Action implements IObjectActionDelegate {


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
			//dumpMethodBody((IMethod) selList.get(0));

			//System.err.println(
			//	"trying to execute action: "
			//		+ action.getClass()
			//		+ " -- "
			//		+ action.getText());

			//ReloPlugin
			//	.getDefault()
			//	.getWorkbench()
			//	.getActiveWorkbenchWindow()
			//	.getActivePage()
			//	.openEditor(new ListEditorInput(selList), "com.architexa.diagrams.relo.editor");
			

			IMethod method = (IMethod) selList.get(0);
			IFile file = (IFile) ((IMember)method).getCompilationUnit().getResource();
			IWorkbenchPage page = getActivePage();
	        //IEditorDescriptor editorDesc = IDE.getEditorDescriptor(file);
	        JavaEditorlet jelet = (JavaEditorlet) page.openEditor(
					new FileEditorInput(file), "com.architexa.diagrams.relo.jdt.JavaEditorlet", true);

	        /*
	        String editorId = "com.architexa.diagrams.relo.jdt.JavaEditorlet";
	        FileEditorInput input = new FileEditorInput(file);
	        JavaEditorlet jelet = null;
	        
			IEditorPart editor = null;
	        
			// Remember the old visible editor
			IEditorPart oldVisibleEditor = page.getActiveEditor();

			// Otherwise, create a new one. This may cause the new editor to
			// become the visible (i.e top) editor.
			IEditorReference ref = null;
			IEditorRegistry reg = WorkbenchPlugin.getDefault().getEditorRegistry();
			EditorDescriptor desc = (EditorDescriptor) reg.findEditor(editorId);
			if (desc == null) {
				throw new PartInitException(WorkbenchMessages.format("EditorManager.unknownEditorIDMessage", new Object[] { editorId })); //$NON-NLS-1$
			}

			//ref = getEditorManager().openEditor(editorID, input, true);
			//ref = openEditorFromDescriptor(new Editor(), desc, input);
			//if (ref != null) {
			//	editor = ref.getEditor(true);
			//	addPart(ref);
			//}

			// Open the instance.
			editor = createPart(desc);
			EditorSite site = new EditorSite(ref, editor, (WorkbenchPage) page, desc);
			if (desc != null)
				site.setActionBars(createEditorActionBars(desc));
			else
				site.setActionBars(createEmptyEditorActionBars());
			
			part.init(site, input);

			createEditorTab(ref, desc, input, setVisible);
			
			/*
			//firePartOpened(editor);
			setEditorAreaVisible(true);
			activate(editor);
			window.firePerspectiveChanged(
					this,
					getPerspective(),
					ref,
					CHANGE_EDITOR_OPEN);
			window.firePerspectiveChanged(
				this,
				getPerspective(),
				CHANGE_EDITOR_OPEN);
			*/
			
			
			//jelet = (JavaEditorlet) editor;
	        //*/
	        
	        
	        
			IAction toggleAction= jelet.getEditorSite().getActionBars().getGlobalActionHandler(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY);
			if (toggleAction != null && toggleAction.isEnabled() && toggleAction.isChecked()) {
				if (toggleAction instanceof TextEditorAction) {
					// Reset the action 
					((TextEditorAction)toggleAction).setEditor(null);
					// Restore the action 
					((TextEditorAction)toggleAction).setEditor(jelet);
				} else {
					// Uncheck 
					toggleAction.run();
					// Check
					toggleAction.run();
				}
			}
			
			// launched editor!!
			//ISourceRange srcRange = ParseUtilities.getBodyRange(method);
			//jelet.setShowRange(srcRange);

			//ConsoleView.setVariable("lm", jelet);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		Shell shell = new Shell();
		MessageDialog.openInformation(
			shell,
			"JBrowse Plug-in",
			"New Action was executed.");
		*/
	}


    private IWorkbenchPage getActivePage() {
        IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) 
            return null;
        else
            return window.getActivePage();
    }

    // this is just a method for debugging (hence suppressing unused)
    @SuppressWarnings("unused")
	private void dumpMethodBody(IMethod method) throws CoreException, IOException {
		ISourceRange srcRange = ParseUtilities.getBodyRange(method);
		//System.err.println(body.toString());
		System.err.println(srcRange.getOffset());
		System.err.println(srcRange.getLength());
		
		IFile srcFile = (IFile) method.getCompilationUnit().getResource();
		InputStream is = srcFile.getContents();
		is.skip(srcRange.getOffset());
		byte[] b = new byte[srcRange.getLength()];
		is.read(b);

		System.err.println("body>>");
		System.err.write(b);
		System.err.println("");
		System.err.println("body>>");
	}


}
