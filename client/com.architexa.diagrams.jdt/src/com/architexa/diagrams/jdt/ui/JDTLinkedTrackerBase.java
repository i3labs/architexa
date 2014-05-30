package com.architexa.diagrams.jdt.ui;

import org.apache.log4j.Logger;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.utils.ArraySingleElementSelectionDialog;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.utils.RootEditPartUtils;

public class JDTLinkedTrackerBase extends JDTTracker {
    static final Logger logger = Activator.getLogger(JDTTracker.class);

	protected BrowseModel bm = null;
	protected RootArtifact rootArt = null;

	protected final IWorkbenchPart rseWbPart;

	protected static IDebugEventSetListener debugListener = null;

	public JDTLinkedTrackerBase(RootArtifact _rootArt, IWorkbenchPart _reloWbPart) {
		this.rootArt = _rootArt;
		this.bm = rootArt.getBrowseModel();
		this.rseWbPart = _reloWbPart;
		}

	public static void setDebugListener(IDebugEventSetListener listener) {
		debugListener = listener;
	}

	// remove listeners when the editpart is closed
	protected final IPartListener cleanupListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {}
		public void partBroughtToTop(IWorkbenchPart part) {}
		public void partDeactivated(IWorkbenchPart part) {}
		public void partOpened(IWorkbenchPart part) {}
		public void partClosed(IWorkbenchPart part) {
			// Need to get the RSE editor from the MultiPageEditor
			part = RootEditPartUtils.getEditorFromRSEMultiPageEditor(part);
			if (part != rseWbPart) return;
			removeListeners();
		}
	};

	@Override
	// add multiple listener-types and provide selection dialog for selecting the right workbench
	protected boolean addListeners() {
		final IWorkbenchWindow[] wwin = PlatformUI.getWorkbench().getWorkbenchWindows();

		IWorkbenchWindow tgtWkbnchWin = wwin[0];
		if (wwin.length > 1) {
			ArraySingleElementSelectionDialog workbenchSelectionDlg 
			= new ArraySingleElementSelectionDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
					"Listen:", 
					wwin, 
					new WorkbenchWindowLabelProvider());
			workbenchSelectionDlg.open();
			if (workbenchSelectionDlg.sel == null) return false;
			tgtWkbnchWin = (IWorkbenchWindow) workbenchSelectionDlg.sel;
		}

		tgtWkbnchWin.getSelectionService().addPostSelectionListener(selProcessor);
		tgtWkbnchWin.getPartService().addPartListener(cleanupListener);

		if(debugListener!=null)
			DebugPlugin.getDefault().addDebugEventListener(debugListener);

		return true;
	}

	private final class WorkbenchWindowLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            if (!(element instanceof IShellProvider)) return null;
            Shell shell = ((IShellProvider)element).getShell();
            return shell.getText();
        }
    }
    

	@Override
	// we need to remove the mouseMoveListener and the cleanupListener as well
	protected void removeListeners() {
		IWorkbenchWindow[] wwin = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow tgtWkbnchWin : wwin) {
			tgtWkbnchWin.getSelectionService().removePostSelectionListener(selProcessor);
			tgtWkbnchWin.getPartService().removePartListener(cleanupListener);
		}
		if(debugListener!=null)
			DebugPlugin.getDefault().removeDebugEventListener(debugListener);
	}

}
