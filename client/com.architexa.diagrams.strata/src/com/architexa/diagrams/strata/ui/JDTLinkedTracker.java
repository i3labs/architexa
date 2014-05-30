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

/*
 * Created on Aug 12, 2005
 */
package com.architexa.diagrams.strata.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.jdt.ui.JDTLinkedTrackerBase;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.relo.jdt.browse.AbstractJDTBrowseModel;
import com.architexa.diagrams.relo.ui.ReloEditor;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.ui.LongCommandStack.HidePrep;
import com.architexa.diagrams.ui.RSEAction;
import com.architexa.diagrams.ui.RSEEditorViewCommon.IRSEEditorViewCommon;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.ui.actions.WorkbenchPartAction;
import com.architexa.rse.WorkbenchUtils;


/**
 * @author vineet
 *
 */
public class JDTLinkedTracker extends JDTLinkedTrackerBase {
    static final Logger logger = StrataPlugin.getLogger(JDTLinkedTracker.class);

	private StrataRootEditPart rc = null;

	// true if all the classes corresponding to any open tabs
	// should be added to the diagram, and false if only the
	// class of the active tab being explored should be added
	private boolean allTabsMode = false; 

    public JDTLinkedTracker(BrowseModel _bm, StrataRootEditPart _rc, IWorkbenchPart _rseWbPart) {
    	super(_rc.getRootArtifact(), _rseWbPart);
		rc = _rc;
    }

	
    public static class LinkedTrackerActionDelegate extends RSEAction {

    	private Map<IWorkbenchPart, JDTLinkedTracker> linkedEditors = new HashMap<IWorkbenchPart, JDTLinkedTracker>();
    	
    	@Override
    	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    		if (targetEditor == null) return;
    		if (!(RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor) instanceof ReloEditor)) {
				action.setEnabled(false);
				return;
			} else
				action.setEnabled(true);
    		
    		super.setActiveEditor(action, targetEditor);
    	}
        
        @Override
		public void init(IViewPart view) {}

    	@Override
		public void run(IAction action) {
    		StrataView view = (StrataView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(StrataView.viewId);
    		IEditorPart targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    		
    		targetEditor = (IEditorPart) RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor);
    		IRSEEditorViewCommon strataWBPart = null; 
			if (view != null ) strataWBPart = view;
    		else strataWBPart = (IRSEEditorViewCommon) targetEditor; 

			if(strataWBPart==null) return;
			JDTLinkedTracker tracker = linkedEditors.get(strataWBPart);
			
			if (action.isChecked()) {
//    			StrataEditor strataEditor = (StrataEditor) activeStrataEditor;
    			StrataRootEditPart rc = (StrataRootEditPart) strataWBPart.getRootController();
    			BrowseModel bm = rc.getRootModel().getBrowseModel();

    			tracker = new JDTLinkedTracker(bm, rc, strataWBPart);
    			tracker.allTabsMode = "com.architexa.diagrams.strata.ui.JDTLinkedTrackerAllTabs".equals(action.getId());
    			linkedEditors.put(strataWBPart, tracker);

    			// @tag Vineet-TODO: why is the below line needed?
    			tracker.bm.setRepo(rc.getRootArtifact().getRepo());
    			boolean ok = tracker.addListeners();
    			if (!ok) action.setChecked(false); // cancelled
    		} else if (tracker!=null) { // just a double check, tracker should always be non-null here
    			tracker.removeListeners();
    			linkedEditors.remove(strataWBPart);
    		}
    	}
		
		@Override
		public void selectionChanged(IAction action, ISelection selection) {
			// ignore during startup when activepage is not set
			if (WorkbenchUtils.getActivePage() == null ) return;
			
			if (!(WorkbenchUtils.getActivePart() instanceof IRSEEditorViewCommon)) return;
    		IRSEEditorViewCommon wbPart = (IRSEEditorViewCommon) WorkbenchUtils.getActivePart(); 

			JDTLinkedTracker linkedActiveEditor = linkedEditors.get(wbPart);

			// button should look toggled on when editor
			// is linked and toggled off otherwise
			boolean editorIsLinked = linkedActiveEditor!=null;
			action.setChecked(editorIsLinked);
		}

    }
    
    @Override
    protected boolean addListeners() {
    	boolean added = super.addListeners();
    	if(allTabsMode) addOpenEditors();
    	return added;
    }

    private void addOpenEditors() {
    	CompoundCommand cmpdAddCmd = new CompoundCommand();
    	for(final IEditorReference editorRef : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences()) {
    		IEditorInput editorInput = null;
    		try {
    			editorInput = editorRef.getEditorInput();
    		} catch (PartInitException e) {
    			logger.error("Unexpected exception while determining open java editors. ", e);
    		}
    		if(!(editorInput instanceof IFileEditorInput)) continue;

    		final IJavaElement editorClass = JavaCore.create(((IFileEditorInput)editorInput).getFile());
    		if(editorClass==null) continue;

    		Command addCmd = new Command() {
    			@Override
    			public void execute() {
    				addNavigationItem(editorRef.getEditor(false), editorClass);
    			}
    		};
    		cmpdAddCmd.add(addCmd);
    	}
    	rc.execute(cmpdAddCmd);
    }

    public static class LinkedTrackerAction extends WorkbenchPartAction {
        protected BrowseModel bm = new AbstractJDTBrowseModel() {};
        protected StrataRootEditPart rc = null;
    	JDTLinkedTracker linkedTracker = null;

		public LinkedTrackerAction(IWorkbenchPart part) {
			super(part, Action.AS_CHECK_BOX);
		}

		@Override
		protected boolean calculateEnabled() {
			return true;
		}
		
		@Override
		public void run() {
			if (linkedTracker == null) {
				IRSEEditorViewCommon strataWBPart = (IRSEEditorViewCommon) getWorkbenchPart();
	            rc = (StrataRootEditPart) strataWBPart.getRootController();
	            bm.setRootArt(rc.getRootArtifact());
				linkedTracker = new JDTLinkedTracker(bm, rc, strataWBPart);
			}

			if (isChecked()) {
				// @tag Vineet-TODO: why is the below line needed?
                linkedTracker.bm.setRepo(rc.getRootArtifact().getRepo());
				boolean ok = linkedTracker.addListeners();
				if (!ok) this.setChecked(false); // cancelled
			} else
				linkedTracker.removeListeners();
		}
    	
    };

    
    /**
     * 
     */
    ////////////////////////////////////////////////////////////
    /// Modify Selection Processing Code to:
    ///  Not worry about the navigation path; and instead
    ///  1> Add directly to the view
    ///  2> Expand method implementation in the linking editor
    ////////////////////////////////////////////////////////////
    
    // TODO: figure out why the below are not needed
    //private IJavaElement prevDeclaredElement;
    //private JavaEditor prevEditor;
    //private ITextSelection prevTextSelection;
    
    private static class HiddenPrepCompoundCommand extends CompoundCommand implements HidePrep{};
    
    @SuppressWarnings("restriction")
	@Override
    protected void addNavigationItem(IWorkbenchPart selectedPart, Object selElement) {
        if (!rc.isActive()) return;
        if (EDITOR_NAV_ONLY && !(selectedPart instanceof org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor)) return;
        
        logger.info("selected element: " + selElement.getClass());
        Artifact art = bm.getArtifact(selElement).getArt();
        
        if(art==null) return;
        final Artifact classArt = art;
        logger.info("adding art: " + classArt);

        if (classArt != null && rc.modelCreatable(classArt)) {
            // select the added items (after a delay because the layout was not happening otherwise)
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                        	CompoundCommand cc = new HiddenPrepCompoundCommand ();
                        	StrataArtFragEditPart.addArtAndContainers(cc, null, rc, rc.getRepo(), classArt);
                        	rc.getViewer().getEditDomain().getCommandStack().execute(cc);               
                        	
                        	List<ArtifactFragment> matchingNestedShownChildren = rc.getRootModel().getMatchingNestedShownChildren(classArt);
                        	if(!matchingNestedShownChildren.isEmpty()) {
                        		ArtifactFragment newAF = rc.getRootModel().getMatchingNestedShownChildren(classArt).get(0);
                        		EditPart newEP = (EditPart) rc.getViewer().getEditPartRegistry().get(newAF);
                        		if(newEP != null)
                        			rc.getViewer().select(newEP);
                        	}
                        }});
                }}, 1000);
        }
    }
   

}
