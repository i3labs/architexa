package com.architexa.diagrams.chrono.ui;


import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.openrdf.model.URI;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.util.LinkedTrackerUtil;
import com.architexa.diagrams.jdt.ui.JDTLinkedTrackerBase;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.ui.RSEAction;
import com.architexa.diagrams.ui.RSEEditorViewCommon.IRSEEditorViewCommon;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.rse.WorkbenchUtils;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class JDTLinkedTracker extends JDTLinkedTrackerBase {

	private DiagramEditPart dc = null;

//	private final RootArtifact rootArt;

	NodeModel mostRecentNav;

	int mostRecentSelLineNum;
	int currentSelLineNum;
	Logger logger  = Activator.getLogger(JDTLinkedTracker.class);

	public JDTLinkedTracker(BrowseModel _bm, DiagramEditPart _dc, IWorkbenchPart _seqWbPart) {
		super((DiagramModel)_dc.getModel(), _seqWbPart);
		dc = _dc;
		bm = _bm;
//		this.rootArt = (DiagramModel)dc.getModel();
	}

	public static class LinkedExploration extends RSEAction {

		JDTLinkedTracker linkedTracker = null;
    	private Map<IRSEEditorViewCommon, JDTLinkedTracker> linkedEditors = new HashMap<IRSEEditorViewCommon, JDTLinkedTracker>();

    	@Override
    	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    		if (targetEditor == null) return;
    		if (!(RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor) instanceof SeqEditor)) {
				action.setEnabled(false);
				return;
			} else
				action.setEnabled(true);
    		
    		super.setActiveEditor(action, targetEditor);

    		SeqEditor seqEditor = (SeqEditor) RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor);
			DiagramModel diagramModel = seqEditor.getModel();

			BrowseModel bm = diagramModel.getBrowseModel();
			DiagramEditPart dc = seqEditor.getDiagramController();
			initAction(seqEditor, bm, dc);
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

		private void initAction(IEditorPart targetEditor, BrowseModel bm, DiagramEditPart dc) {
//    		JDTLinkedTracker tracker = linkedEditors.get(targetEditor);
//			if (linkedTracker == null) linkedTracker = new JDTLinkedTracker(bm, dc, targetEditor);
		}

		@Override
		public void run(IAction action) {
    		SeqView view = (SeqView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(SeqView.viewId);
    		IEditorPart targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    		
    		targetEditor = (IEditorPart) RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor);
    		IRSEEditorViewCommon seqWBPart = null; 
			if (view != null ) seqWBPart = view;
    		else seqWBPart = (IRSEEditorViewCommon) targetEditor; 

			if(seqWBPart==null) return;
			JDTLinkedTracker tracker = linkedEditors.get(seqWBPart);
			
			if (action.isChecked()) {
	    		DiagramEditPart dc = (DiagramEditPart) seqWBPart.getRootController();
	    		BrowseModel bm = ((DiagramModel)dc.getModel()).getBrowseModel();

    			tracker = new JDTLinkedTracker(bm, dc, targetEditor);
    			linkedEditors.put(seqWBPart, tracker);

				boolean ok = tracker.addListeners();
				if (!ok) action.setChecked(false); // cancelled
			} else if (tracker!=null) {
				tracker.removeListeners();
    			linkedEditors.remove(seqWBPart);
			}
		}

		@Override
		public void init(IViewPart view) {
			if (view == null) return;
			SeqView seqView = (SeqView) view;
			DiagramModel diagramModel = seqView.getModel();

			BrowseModel bm = diagramModel.getBrowseModel();
			DiagramEditPart dc = seqView.getDiagramController();
			if (linkedTracker == null) linkedTracker = new JDTLinkedTracker(bm, dc, view);
		}
	}


	@Override
	protected void processSelection(IWorkbenchPart selectedPart,
			ITextSelection textSelection) {
		currentSelLineNum = textSelection.getStartLine()+1;
		super.processSelection(selectedPart, textSelection);
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
		if (bm == null) {
			logger.error("Browse Model is null for : " + selectedPart);
			return;
		}
		Artifact art = bm.getArtifact(selElement).getArt();
		if (EDITOR_NAV_ONLY && 
				!(selectedPart instanceof org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor)) return;
	       
		if (art != null && rootArt.artViewable(art)) {
			scheduleNavItemForDelayedAddition(art, selectedPart, selElement);
		}
	}

	/**
	 * The goal here is to add items after a small delay (1s). This will allow
	 * items that the user randomly clicks on to not be added
	 */
	private Timer delayedNavItemAdder = null;
	private void scheduleNavItemForDelayedAddition(final Artifact art, final IWorkbenchPart currSelectedPart, final Object currSelectedElement) {
		if (delayedNavItemAdder != null) delayedNavItemAdder.cancel();

		// @tag cmd-infrastructure: this should be made a command (with the
		// selection chained on). Being a command which will automatically do
		// the layout after execution. We will also likely want support for
		// something similar to a rc.delayedExecute
		delayedNavItemAdder = runDelayedInUIThread(1000, new Runnable() {
			public void run() {

				CompoundCommand command = new CompoundCommand();
				mostRecentNav = LinkedTrackerUtil.createAddNodeAndRelCommand(dc, art, mostRecentNav, mostRecentSelLineNum, currSelectedElement, command);
				dc.execute(command);

				mostRecentSelLineNum = currentSelLineNum;
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

	@Override
	protected void addNavigationPath(Object prevElement, URI addingRel, Object selElement) {
		Artifact srcArt = bm.getArtifact(prevElement).getArt();

		if(mostRecentNav==null || mostRecentNav.getArt().elementRes==null || 
				!mostRecentNav.getArt().elementRes.equals(srcArt.elementRes)) 
			mostRecentNav = null;
	}

}
