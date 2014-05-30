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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.editors.RSEMultiPageEditor;
import com.architexa.diagrams.jdt.JDTSelectionUtils;
import com.architexa.diagrams.jdt.actions.OpenVizAction;
import com.architexa.diagrams.jdt.builder.ResourceQueue;
import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.relo.actions.ListEditorInput;
import com.architexa.diagrams.relo.commands.CreateCommand;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.diagrams.relo.jdt.browse.ClassStrucBrowseModel;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.diagrams.relo.ui.ReloEditor;
import com.architexa.diagrams.relo.ui.ReloView;
import com.architexa.diagrams.ui.RSEShareableDiagramEditorInput;
import com.architexa.diagrams.utils.OpenItemUtils;
import com.architexa.diagrams.utils.RunWithObjectParam;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.rse.BuildStatus;


public class OpenForBrowsingAction extends OpenVizAction {
    static final Logger logger = ReloJDTPlugin.getLogger(OpenForBrowsingAction.class);

    public OpenForBrowsingAction() {
    	setText("Class Diagram");
    	setImageDescriptor(ReloPlugin.getImageDescriptor("icons/relo-document.png"));
    }

    @Override
	public void selectionChanged(IAction action, ISelection sel) {
        IJavaElement selectedElt = JDTSelectionUtils.getSelectedJDTElement();
        // if its a project/ contains a project dont open
        
        if(selectedElt==null || selectedElt instanceof IJavaProject ||
        		(sel instanceof IStructuredSelection && OpenItemUtils.containsProject((IStructuredSelection) sel)) ||
        		selectedElt instanceof IPackageFragmentRoot) action.setEnabled(false);
        else action.setEnabled(true);
        
		try {
			if (sel instanceof TreeSelection)
				BuildStatus.addUsage("Open Class Diagram from Explorer - " + ((TreeSelection) sel).getPaths().length + ". ");
			else if (sel instanceof StructuredSelection)
				BuildStatus.addUsage("Open Class Diagram from existing Diagram - " + ((StructuredSelection) sel).size() + ". ");
			else if (sel instanceof TextSelection)
				BuildStatus.addUsage("Open Class Diagram from Code. ");
		} catch (Exception e) {
			logger.error("Error while getting selection usage: " + e.getMessage());
		}
    }

    @Override
    public void openViz(IWorkbenchWindow activeWorkbenchWindow, List<?> selList) {
    	openViz(activeWorkbenchWindow, selList, null, null, null);
    }

    public static String editorID = "com.architexa.diagrams.relo.editor";

    public static void openReloViz(IWorkbenchWindow activeWorkbenchWindow, final List<?> selList, final Map<IDocument, IDocument> lToRDocMap, final Map<IDocument, String> lToPathMap, final StringBuffer docBuff, final IPath wkspcPath, final RunWithObjectParam runWithEditor)  {
        try {
	        ReloView view = (ReloView) activeWorkbenchWindow.getActivePage().findView(ReloView.viewId);
	        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	        if (view != null) {
	        	// Check why support for Relo View is being added here
	        	// view is always null here?
	        	
	            // make sure the view is shown
	            activeWorkbenchWindow.getActivePage().activate(view);
	            
	            ReloController rc = view.getReloController();
	            CreateCommand createCmd = new CreateCommand(rc, selList);
	            CompoundCommand cc = new CompoundCommand("Show Included Relationships");
		        cc.add(createCmd);
		        ((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(cc, createCmd.getAddedAFs());
		        
		        if (!ResourceQueue.isEmpty())
		        	rc.addUnbuiltWarning();
		        rc.execute(cc);        
	            return;
	        }

	        new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, false, new IRunnableWithProgress(){

	        	public void run(IProgressMonitor monitor)
	        	throws InvocationTargetException, InterruptedException {
	        		monitor.beginTask("Creating Class Diagram...", IProgressMonitor.UNKNOWN);

	        		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){

	        			public void run() {
	        				try {
	        					ReloEditor reloEditor = null; 
	        					RSEMultiPageEditor mpe = null;
	        					RSEShareableDiagramEditorInput edInput = null;
	        					if (lToRDocMap == null || lToPathMap == null) {
	        						edInput = new ListEditorInput(new ArrayList<Object>(), new ClassStrucBrowseModel());
	        					}
	        					else {
	        						edInput = new ListEditorInput(new ArrayList<Object>(), new ClassStrucBrowseModel(), lToRDocMap, lToPathMap, docBuff);
	        					}

	        					// The exploration server uses this to dynamically get the repo for a diagram from a code element
	        					if (wkspcPath != null) {
	        						edInput.wkspcPath = wkspcPath;
//	        						((ListEditorInput)edInput).browseModel.setRepo(StoreUtil.getStoreRepository(wkspcPath));
	        					}
	        					
	        					mpe = (RSEMultiPageEditor) page.openEditor(edInput, RSEMultiPageEditor.editorId);
	        					if (runWithEditor != null) runWithEditor.run(mpe);

	        					reloEditor = (ReloEditor) mpe.getRseEditor();

	        					ReloController rc = reloEditor.getReloController();

	        					CreateCommand createCmd = new CreateCommand(reloEditor.getReloController(), selList);

	        					CompoundCommand cc = new CompoundCommand("Show Included Relationships");
	        					cc.add(createCmd);
	        					((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(cc, createCmd.getAddedAFs());

	        					if (!ResourceQueue.isEmpty())
	        						rc.addUnbuiltWarning();

	        					rc.execute(cc);        
	        				} catch (PartInitException e) {
	        					logger.error("Unexpected Exception.", e);
	        				}
	        			}
	        		});
	        	}
	        });
        } catch (Throwable e) {
            logger.error("Unexepected exception while opening relo visualization", e);
        }
    }

	@Override
	public void openViz(IWorkbenchWindow activeWorkbenchWindow,
			List<?> selList, Map<IDocument, IDocument> lToRDocMap, Map<IDocument, String> lToPathMap, StringBuffer docBuff) {
		BuildStatus.addUsage("Relo");
    	openReloViz(activeWorkbenchWindow, selList, lToRDocMap, lToPathMap, docBuff, null, null);
	}

}
