/* 
 * Copyright (c) 2004-2006 Massachusetts Institute of Technology. This code was
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

package com.architexa.diagrams.strata.ui;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.relo.jdt.browse.ClassStrucBrowseModel;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.ui.LongCommandStack;
import com.architexa.diagrams.ui.RSEEditorViewCommon;
import com.architexa.diagrams.ui.RSETransferDropTargetListener;
import com.architexa.diagrams.ui.RSEView;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.KeyHandler;
import com.architexa.org.eclipse.gef.ui.actions.ActionRegistry;
import com.architexa.org.eclipse.gef.ui.actions.PrintAction;
import com.architexa.org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import com.architexa.rse.AccountSettings;



public class StrataView extends RSEView {
    public static final Logger logger = StrataPlugin.getLogger(StrataView.class);

    public static final String viewId = "com.architexa.diagrams.strata.ui.StrataView";
	
    public StrataView() {
		EditDomain defaultEditDomain = getEditDomain();
		defaultEditDomain.setCommandStack(new LongCommandStack("Strata"));
		setEditDomain(defaultEditDomain);
    }

    StrataRootDoc rootContent = null;

	@Override
	public RootArtifact getRootModel() {
		return rootContent;
	}
    
    @Override
    protected void initializeGraphicalViewer() {
        try {
        	rootContent = OpenStrata.getStrataDoc();
        	getGraphicalViewer().setContents(rootContent);
    		((ClosedContainerDPolicy) rootContent.getDiagramPolicy(ClosedContainerDPolicy.DefaultKey)).setShowingChildren(true);
        	TransferDropTargetListener dragSrc = new RSETransferDropTargetListener(this, getGraphicalViewer());
            getGraphicalViewer().addDropTargetListener(dragSrc);

            // Try to use current selection when opening view
			//final IWorkbenchWindow activeWorkbenchWindow = StrataPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
			//ISelection sel = activeWorkbenchWindow.getSelectionService().getSelection();
			//if (!(sel instanceof IStructuredSelection)) return;
			//List<?> selList = ((IStructuredSelection) sel).toList();
			//if (selList.size() == 0) return;
			//if (rootContent == null) {
			//    rootContent = OpenStrata.buildAndOpenStrataDoc(new ArrayList<Object>());
			//    getGraphicalViewer().setContents(rootContent);
			//}
        } catch (Throwable t) {
            logger.error("Unexpected Error", t);
        }
    }

	public boolean canDropOnEditor(IStructuredSelection sel) {
		return StrataViewEditorCommon.canDropOnEditor(sel);
	}

    private KeyHandler getCommonKeyHandler(ScrollingGraphicalViewer viewer) {
    	return RSEEditorViewCommon.getCommonKeyHandler(this, viewer);	
    }
    
    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer)getGraphicalViewer();
        StrataViewEditorCommon.configureGraphicalViewer(this, getGraphicalViewer(), getActionRegistry(), getCommonKeyHandler(viewer), null);
        
        // Use to change zoom level of the View
		// root.getZoomManager().setZoomLevelContributions(zoomLevels);
		// root.getZoomManager().setUIMultiplier(0.75);
		// root.getZoomManager().setZoom(1.0/root.getZoomManager().getUIMultiplier());
    }

	public boolean isSaveOnCloseNeeded() {
		// TODO Auto-generated method stub
		return false;
	}

	// @tag unify-core
    public StrataRootEditPart getRootController() {
        return (StrataRootEditPart) this.getGraphicalViewer().getEditPartRegistry().get(this.rootContent);
    }
	
	// bm should be set by a setInput method like StrataEditor?
	public BrowseModel bm = new ClassStrucBrowseModel();
	
	public static final URI browseModel = RSECore.createRseUri("core#browseModel");

	   @Override
		public void writeFile(RdfDocumentWriter rdfWriter, Resource rootAFRes) throws IOException {
	    	StrataRootEditPart rc = (StrataRootEditPart) getGraphicalViewer().getRootEditPart().getContents();
	    	StrataViewEditorCommon.writeFile(this, rc, bm, browseModel, rootContent, rdfWriter, rootAFRes);
		}

		@Override
		public IFile getNewSaveFile() {
			StrataRootEditPart scep = (StrataRootEditPart) getGraphicalViewer().getRootEditPart().getContents();
			return StrataViewEditorCommon.getNewSaveFile(this, scep);
		}
	    @SuppressWarnings("unchecked")
		@Override
		protected void createActions() {
			super.createActions();

			ActionRegistry registry = getActionRegistry();
			IAction action;

			// below copied from super class
			/*
			action = new UndoAction(this);
			registry.registerAction(action);
			getStackActions().add(action.getId());
			
			action = new RedoAction(this);
			registry.registerAction(action);
			getStackActions().add(action.getId());
			
			action = new SelectAllAction(this);
			registry.registerAction(action);
			
			action = new DeleteAction((IWorkbenchPart)this);
			registry.registerAction(action);
			getSelectionActions().add(action.getId());
			
			action = new SaveAction(this);
			registry.registerAction(action);
			getPropertyActions().add(action.getId());
			*/
			
			registry.registerAction(new PrintAction(this));
			
			// May want to add support down the road
			// registry.registerAction(new JDTLinkedTracker.LinkedTrackerAction(this));
			
	        action = new BreakAction(this);
	        registry.registerAction(action);
	        getSelectionActions().add(action.getId());

	        action = new FocusAction(this, rootContent);
	        registry.registerAction(action);
	        getSelectionActions().add(action.getId());

	        action = new ReLayerAction(this);
	        registry.registerAction(action);
	        getSelectionActions().add(action.getId());

			action = new OpenInJDTEditorAction(this);
			registry.registerAction(action);
			getSelectionActions().add(action.getId());
			
			// Experimental Actions
			if (AccountSettings.EXPERIMENTAL_MODE) {
				action = new CollapseAllPackagesAction(this);
				registry.registerAction(action);
				getSelectionActions().add(action.getId());
				
				action = new ReduceAction(this);
				registry.registerAction(action);
				getSelectionActions().add(action.getId());
				
				action = new ShowDependersAndDependeesAction(this);
				registry.registerAction(action);
				getSelectionActions().add(action.getId());
				
				action = new ShowInteractionsAction(this);
				registry.registerAction(action);
				getSelectionActions().add(action.getId());
			}
		}
}
