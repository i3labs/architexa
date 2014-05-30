package com.architexa.diagrams.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;

import com.architexa.org.eclipse.gef.editparts.ZoomManager;
import com.architexa.org.eclipse.gef.ui.actions.ActionRegistry;
import com.architexa.org.eclipse.gef.ui.actions.DeleteAction;
import com.architexa.org.eclipse.gef.ui.actions.PrintAction;
import com.architexa.org.eclipse.gef.ui.actions.RedoAction;
import com.architexa.org.eclipse.gef.ui.actions.SelectAllAction;
import com.architexa.org.eclipse.gef.ui.actions.UndoAction;
import com.architexa.org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import com.architexa.org.eclipse.gef.ui.parts.GraphicalEditor;

// NOTE: To support 3.2, use of ITextEditor required adding
// org.eclipse.ui.workbench.texteditor as a dependency in MANIFEST.MF
/**
 * Manages the installation/deinstallation of global actions for multi-page
 * editors. Responsible for the redirection of global actions to the active
 * editor. Multi-page contributor replaces the contributors for the individual
 * editors in the multi-page editor.
 */
public class MultiPageEditorContributor extends
		MultiPageEditorActionBarContributor {
	private IEditorPart activeEditorPart;

	private ActionRegistry registry = new ActionRegistry();
	private List<String> globalActionKeys = new ArrayList<String>();

	/**
	 * Returns the action registed with the given text editor.
	 * 
	 * @return IAction or null if editor is null.
	 */
	protected IAction getAction(ITextEditor editor, String actionID) {
		return (editor == null ? null : editor.getAction(actionID));
	}

	/*
	 * (non-JavaDoc) Method declared in
	 * AbstractMultiPageEditorActionBarContributor.
	 */

	protected void buildActions() {
		addAction(new SelectAllAction(activeEditorPart));
		addAction(new PrintAction(activeEditorPart));
		addAction(new DeleteAction((IWorkbenchPart)activeEditorPart));
		addAction(new UndoAction(activeEditorPart));
		addAction(new RedoAction(activeEditorPart));
	}

	protected ActionRegistry getActionRegistry() {
		return registry;
	}

	protected void addAction(IAction action) {
		getActionRegistry().registerAction(action);
		addGlobalActionKey(action.getId());
	}

	protected void addGlobalActionKey(String key) {
		globalActionKeys.add(key);
	}

	@Override
	public void setActivePage(IEditorPart part) {
		if (activeEditorPart == part)
			return;

		activeEditorPart = part;

		IActionBars actionBars = getActionBars();
		if (actionBars != null) {

			actionBars.clearGlobalActionHandlers();

			if (part instanceof GraphicalEditor) {
				ActionRegistry registry = (ActionRegistry) part
						.getAdapter(ActionRegistry.class);
				for (int i = 0; i < globalActionKeys.size(); i++) {
					String id = (String) globalActionKeys.get(i);
					actionBars.setGlobalActionHandler(id,
							registry.getAction(id));
				}
			} 
			
//			else if (part instanceof ITextEditor) {
//				ITextEditor editor = (part instanceof ITextEditor) ? (ITextEditor) part	: null;
//
//				actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
//						getAction(editor, ITextEditorActionConstants.DELETE));
//				actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
//						getAction(editor, ITextEditorActionConstants.UNDO));
//				actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
//						getAction(editor, ITextEditorActionConstants.REDO));
//				actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
//						getAction(editor, ITextEditorActionConstants.CUT));
//				actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
//						getAction(editor, ITextEditorActionConstants.COPY));
//				actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
//						getAction(editor, ITextEditorActionConstants.PASTE));
//				actionBars
//						.setGlobalActionHandler(
//								ActionFactory.SELECT_ALL.getId(),
//								getAction(editor,
//										ITextEditorActionConstants.SELECT_ALL));
//				actionBars.setGlobalActionHandler(ActionFactory.FIND.getId(),
//						getAction(editor, ITextEditorActionConstants.FIND));
//				actionBars.setGlobalActionHandler(
//						IDEActionFactory.BOOKMARK.getId(),
//						getAction(editor, IDEActionFactory.BOOKMARK.getId()));
//				actionBars.updateActionBars();
//			}
		}
//		init(actionBars);
	}

	@Override
	public void init(IActionBars bars) {
		buildActions();
		super.init(bars);

	}

	// private void createActions() {
	// sampleAction = new Action() {
	// @Override
	// public void run() {
	// MessageDialog.openInformation(null, "MultiPageTest",
	// "Sample Action Executed");
	// }
	// };
	// sampleAction.setText("Sample Action");
	// sampleAction.setToolTipText("Sample Action tool tip");
	// sampleAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
	// getImageDescriptor(IDE.SharedImages.IMG_OBJS_TASK_TSK));
	// }
	// @Override
	// public void contributeToMenu(IMenuManager manager) {
	// IMenuManager menu = new MenuManager("Editor &Menu");
	// manager.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS, menu);
	// menu.add(sampleAction);
	// }
	
	 @Override
	 public void contributeToToolBar(IToolBarManager manager) {
//	 manager.add(new Separator());
//	 manager.add(sampleAction);
		manager.add(new Separator());
		String[] zoomStrings = new String[] { ZoomManager.FIT_ALL,
				ZoomManager.FIT_HEIGHT, ZoomManager.FIT_WIDTH };
		manager.add(new ZoomComboContributionItem(getPage(), zoomStrings));
	 }
}
