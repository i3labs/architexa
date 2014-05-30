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
package com.architexa.diagrams.relo.jdt.browse;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.openrdf.model.URI;

import com.architexa.diagrams.commands.AddNodeAndRelCmd;
import com.architexa.diagrams.jdt.ui.JDTLinkedTrackerBase;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.diagrams.relo.jdt.parts.MethodEditPart;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.diagrams.relo.ui.ReloEditor;
import com.architexa.diagrams.relo.ui.ReloView;
import com.architexa.diagrams.ui.RSEAction;
import com.architexa.diagrams.ui.RSEEditorViewCommon.IRSEEditorViewCommon;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.rse.WorkbenchUtils;


/**
 * TODO: To make tracker robust (in order of dependencies):
 * 1] Support inheritance/call-hierarchy views
 * 2] Move common portions back into Relo-Core
 * @author vineet
 *
 */
public class JDTLinkedTracker extends JDTLinkedTrackerBase {
    static final Logger logger = ReloJDTPlugin.getLogger(JDTLinkedTracker.class);

	private ReloController rc = null;

//	private final RootArtifact rootArt;

    public JDTLinkedTracker(ReloController _rc, IWorkbenchPart _reloWbPart) {
    	super(_rc.getRootArtifact(), _reloWbPart);
		rc = _rc;
//		this.rootArt = rc.getRootArtifact();
    }

    // allow users to disable automatic expansion of methods 
	protected IAction embeddedMethodAction = null;
	protected boolean embeddedMethodActionIsChecked = false;
	
    public static class LinkedTrackerAction extends RSEAction {
    	
    	private Map<IRSEEditorViewCommon, JDTLinkedTracker> linkedEditors = new HashMap<IRSEEditorViewCommon, JDTLinkedTracker>();

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
    	public void run(IAction action) {
    		ReloView view = (ReloView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ReloView.viewId);
    		
    		IEditorPart activeReloEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    		activeReloEditor = (IEditorPart) RootEditPartUtils.getEditorFromRSEMultiPageEditor(activeReloEditor);
    		
    		IRSEEditorViewCommon reloWBPart = null; 
			if (view != null ) reloWBPart = view;
    		else reloWBPart = (IRSEEditorViewCommon) activeReloEditor; 

			if(reloWBPart==null) return;
    		JDTLinkedTracker tracker = linkedEditors.get(reloWBPart);
    		
    		if (action.isChecked()) {
    			final IRSEEditorViewCommon reloWB = reloWBPart;
    			tracker = new JDTLinkedTracker((ReloController) reloWB.getRootController(), reloWB);
    			linkedEditors.put(reloWB, tracker);
    			final JDTLinkedTracker linkedTracker = tracker;

    			Timer t = new Timer();
    			t.schedule(new TimerTask() {
    				@Override
    				public void run() {
    					for (IContributionItem item : tbm.getItems()) {
    						if (EmbeddedMethodAction.embeddedMethodId.equals(item.getId())) {
    							linkedTracker.embeddedMethodAction = ((ActionContributionItem) item).getAction();
    							linkedTracker.embeddedMethodAction.addPropertyChangeListener(new IPropertyChangeListener() {
    								public void propertyChange(PropertyChangeEvent event) {
    									IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    									if(reloWB.equals(activeEditor)) {
    										linkedTracker.embeddedMethodActionIsChecked = linkedTracker.embeddedMethodAction.isChecked();
    									}
    								}
    							});
    						}
    					}
    				}
    			}, 1000);

    			boolean ok = linkedTracker.addListeners();
    			if (!ok) action.setChecked(false); // cancelled
    		} else if(tracker!=null) { // just a double check, tracker should always be non-null here
    			tracker.removeListeners();
    			linkedEditors.remove(reloWBPart);
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

			if (linkedActiveEditor != null)
    			linkedActiveEditor.embeddedMethodAction.setChecked(linkedActiveEditor.embeddedMethodActionIsChecked);
    	}
    }
    


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
    
    @SuppressWarnings("restriction")
	@Override
    protected void addNavigationItem(IWorkbenchPart selectedPart, Object selElement) {
        Artifact art = bm.getArtifact(selElement).getArt();
        logger.info("scheduling art for adding: " + art);
        //TODO what does Editor_Nav do???
    	if (EDITOR_NAV_ONLY && 
    			!(selectedPart instanceof org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor)) return;
        if (art != null && rootArt.artViewable(art)) {
        	scheduleNavItemForDelayedAddition(art, selectedPart);
        }
    }

    /**
	 * The goal here is to add items after a small delay (1s). This will allow
	 * items that the user randomly clicks on to not be added
	 */
    private Timer delayedNavItemAdder = null;
    private void scheduleNavItemForDelayedAddition(final Artifact art, final IWorkbenchPart currSelectedPart) {
    	if (delayedNavItemAdder != null) delayedNavItemAdder.cancel();
    	// @tag cmd-infrastructure: this should be made a command (with the
		// selection chained on). Being a command which will automatically do
		// the layout after execution. We will also likely want support for
		// something similar to a rc.delayedExecute
    	delayedNavItemAdder = runDelayedInUIThread(1000, new Runnable() {
			public void run() {
				HashMap<Artifact, ArtifactFragment> addedArtToAF = new HashMap<Artifact, ArtifactFragment>();
				AddNodeAndRelCmd addCmd = new AddNodeAndRelCmd(rc, art, addedArtToAF);
				CompoundCommand tgtCmd = new CompoundCommand();
				tgtCmd.add(addCmd);
				((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(tgtCmd, addCmd.getNewArtFrag());
				rc.execute(tgtCmd);
				
				final ArtifactEditPart aep = rc.findArtifactEditPart(addCmd.getNewArtFrag());

	            //logger.error("Added artficact: ");
	            if (embeddedMethodActionIsChecked)
	            	openNavigatedArtifact( currSelectedPart, aep);

	            // delay selection changes to after layout happens
		        runDelayedInUIThread(1000, new Runnable() {
					public void run() {
			            rc.getRoot().getViewer().deselectAll();
			            rc.getRoot().getViewer().appendSelection(aep);
					}});
			}
		});
	}

   
	private static Timer runDelayedInUIThread(int delay, final Runnable delayedRunnable) {
		Timer delayTimer = new Timer();
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						delayedRunnable.run();
					}});
			}
		};
		delayTimer.schedule(tt, delay);
		return delayTimer;
	}

	MethodEditPart recentDeclaredMethod = null;
    private void openNavigatedArtifact(final IWorkbenchPart selectedPart, final ArtifactEditPart declaredAEP) {
        if (!(declaredAEP instanceof MethodEditPart)) return;
        if (recentDeclaredMethod == declaredAEP) return;
        if (recentDeclaredMethod != null) {
            if (recentDeclaredMethod.getDetailLevel() > recentDeclaredMethod.getMinimalDL())
                recentDeclaredMethod.suggestDetailLevelDecrease();
        }
        Runnable r = new Runnable() {
            public void run() {
                declaredAEP.suggestDetailLevelIncrease();
                selectedPart.setFocus();
            }
        };
    	Display.getDefault().timerExec(500, r);
        recentDeclaredMethod = (MethodEditPart) declaredAEP;
    }

    @Override
    protected void addNavigationPath(Object prevElement, URI addingRel, Object selElement) {
        Artifact srcArt = bm.getArtifact(prevElement).getArt();
        Artifact dstArt = bm.getArtifact(selElement).getArt();
        if (srcArt != null && dstArt != null) {
        	ArtifactEditPart foundSrcEP = rc.findArtifactEditPart(srcArt);
    		if(foundSrcEP==null) {
    			foundSrcEP = rc.createOrFindArtifactEditPart(srcArt);
    		}
        	CompoundCommand tgtCmd = new CompoundCommand();
        	Map<Artifact, ArtifactFragment> addedArtToAF = new HashMap<Artifact, ArtifactFragment>();
        	AddNodeAndRelCmd addCmd = new AddNodeAndRelCmd(rc, foundSrcEP.getArtifact(), new DirectedRel(addingRel, true), dstArt, addedArtToAF); 
    		tgtCmd.add(addCmd);
        	((ReloDoc) rc.getRootArtifact()).showIncludedRelationships(tgtCmd, addCmd.getNewArtFrag());
        	rc.execute(tgtCmd);
        }		
//       	rc.addRel(srcArt, addingRel, dstArt);
    }



}
