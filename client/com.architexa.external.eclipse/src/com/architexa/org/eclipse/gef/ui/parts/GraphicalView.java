package com.architexa.org.eclipse.gef.ui.parts;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.GraphicalViewer;
import com.architexa.org.eclipse.gef.KeyHandler;
import com.architexa.org.eclipse.gef.commands.CommandStack;
import com.architexa.org.eclipse.gef.commands.CommandStackListener;
import com.architexa.org.eclipse.gef.tools.SelectionTool;
import com.architexa.org.eclipse.gef.ui.actions.ActionRegistry;
import com.architexa.org.eclipse.gef.ui.actions.DeleteAction;
import com.architexa.org.eclipse.gef.ui.actions.PrintAction;
import com.architexa.org.eclipse.gef.ui.actions.RedoAction;
import com.architexa.org.eclipse.gef.ui.actions.SelectAllAction;
import com.architexa.org.eclipse.gef.ui.actions.UndoAction;
import com.architexa.org.eclipse.gef.ui.actions.UpdateAction;
import com.architexa.org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import com.architexa.org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import com.architexa.org.eclipse.gef.ui.parts.SelectionSynchronizer;

// code from GEF: bare minimum needed to be copied to get code to run (copied from GraphicalEditor)
// TODO: move to seperate plugin
public abstract class GraphicalView extends ViewPart implements CommandStackListener, ISelectionListener {

	public abstract static class GraphicalAction implements IViewActionDelegate, IPropertyChangeListener, IActionDelegate2 {
		protected GraphicalView view = null;
		protected IAction proxyAction = null;
		public void init(IViewPart _view) {
			view = (GraphicalView) _view;
			getChildAction().addPropertyChangeListener(this);
		}
		protected abstract IAction getChildAction();
		public void init(IAction action) {
			proxyAction = action;
		}
		public void run(IAction action) {}
		public void selectionChanged(IAction action, ISelection selection) {}
		public void propertyChange(PropertyChangeEvent event) {
			if (proxyAction != null) {
				if (getChildAction().isEnabled()) 
					proxyAction.setEnabled(true);
				else
					proxyAction.setEnabled(false);
			}
		}
		public void dispose() {
			getChildAction().removePropertyChangeListener(this);
		}
		public void runWithEvent(IAction action, Event event) {
			getChildAction().run();
		}
	}
	public static class GraphicalUndoAction extends GraphicalAction {
		@Override
		protected IAction getChildAction() {
			return view.getActionRegistry().getAction(ActionFactory.UNDO.getId());
		}
	}
	public static class GraphicalRedoAction extends GraphicalAction {
		@Override
		protected IAction getChildAction() {
			return view.getActionRegistry().getAction(ActionFactory.REDO.getId());
		}
	}
	
	public static class ViewEditDomain extends EditDomain {
		private IViewPart viewPart;
		public ViewEditDomain(IViewPart viewPart) {
			setViewPart(viewPart);
		}
		public IViewPart getViewPart() {
			return viewPart;
		}
		protected void setViewPart(IViewPart viewPart) {
			this.viewPart = viewPart;
		}
	}

	public GraphicalView() {
		EditDomain viewEditDomain = new ViewEditDomain(this);
		viewEditDomain.setActiveTool(new SelectionTool());
		setEditDomain(viewEditDomain);
	}

	@Override
	public void createPartControl(Composite parent) {
		createGraphicalViewer(parent);
	}
	protected void createGraphicalViewer(Composite parent) {
		GraphicalViewer viewer = new ScrollingGraphicalViewer();
		viewer.createControl(parent);
		setGraphicalViewer(viewer);
		configureGraphicalViewer();
		hookGraphicalViewer();
		initializeGraphicalViewer();
	}
	protected void setGraphicalViewer(GraphicalViewer viewer) {
		getEditDomain().addViewer(viewer);
		this.graphicalViewer = viewer;
	}
	public boolean trapKeys(KeyEvent e) {
		if (e.stateMask == SWT.CTRL) {
			switch (e.keyCode) {
			case 'z' :
				getActionRegistry().getAction(ActionFactory.UNDO.getId()).run();
				e.doit = false;
				return true;
			case 'y' :
				getActionRegistry().getAction(ActionFactory.REDO.getId()).run();
				e.doit = false;
				return true;
			case 'a' :
				getActionRegistry().getAction(ActionFactory.SELECT_ALL.getId()).run();
				e.doit = false;
				return true;
			}
		}
		if (e.stateMask == SWT.None) {
			switch (e.keyCode) {
			case SWT.DEL :
				getActionRegistry().getAction(ActionFactory.DELETE.getId()).run();
				e.doit = false;
				return true;
			}
		}
		return false;
	}
	protected void configureGraphicalViewer() {
		getGraphicalViewer().getControl().setBackground(ColorConstants.listBackground);
		GraphicalViewer viewer = getGraphicalViewer();
        KeyHandler keyHandler = new GraphicalViewerKeyHandler(viewer) {
        	@Override
			public boolean keyPressed(KeyEvent e) {
        		if (trapKeys(e))
        			return true;
        		else
        			return super.keyPressed(e);
        	}
        	
        };

		viewer.setKeyHandler(keyHandler);
	}
	protected EditDomain getEditDomain() {
		return editDomain;
	}
	protected void setEditDomain(EditDomain ed) {
		editDomain = ed;
	}
	protected GraphicalViewer getGraphicalViewer() {
		return graphicalViewer;
	}
	private EditDomain editDomain;
	private GraphicalViewer graphicalViewer;
	protected void hookGraphicalViewer() {
		getSelectionSynchronizer().addViewer(getGraphicalViewer());
		getSite().setSelectionProvider(getGraphicalViewer());
	}
	private SelectionSynchronizer synchronizer;
	protected SelectionSynchronizer getSelectionSynchronizer() {
		if (synchronizer == null)
			synchronizer = new SelectionSynchronizer();
		return synchronizer;
	}
	protected abstract void initializeGraphicalViewer();
	private ActionRegistry actionRegistry;
	protected ActionRegistry getActionRegistry() {
		if (actionRegistry == null)
			actionRegistry = new ActionRegistry();
		return actionRegistry;
	}
	protected CommandStack getCommandStack() {
		return getEditDomain().getCommandStack();
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
        setSite(site);
    	//setInput(null);
    	getCommandStack().addCommandStackListener(this);
    	getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
    	initializeActionRegistry();
	}
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// If not the active editor, ignore selection changed.
		if (this.equals(getSite().getPage().getActivePart()))
			updateActions(selectionActions);
	}
	protected void initializeActionRegistry() {
		createActions();
		updateActions(propertyActions);
		updateActions(stackActions);
	}
	@SuppressWarnings("unchecked")
	protected void createActions() {
		ActionRegistry registry = getActionRegistry();
		IAction action;
		
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
		
		registry.registerAction(new PrintAction(this));
	}
	protected void updateActions(List actionIds) {
		ActionRegistry registry = getActionRegistry();
		Iterator iter = actionIds.iterator();
		while (iter.hasNext()) {
			IAction action = registry.getAction(iter.next());
			if (action instanceof UpdateAction)
				((UpdateAction)action).update();
		}
	}
	protected List getStackActions() {
		return stackActions;
	}
	protected List getSelectionActions() {
		return selectionActions;
	}
	protected List getPropertyActions() {
		return propertyActions;
	}
	@SuppressWarnings("serial")
	private static class ActionIDList extends ArrayList {
		@SuppressWarnings("unchecked")
		@Override
		public boolean add(Object o) {
			if (o instanceof IAction) {
				try {
					IAction action = (IAction) o;
					o = action.getId();
					throw new IllegalArgumentException(
							"Action IDs should be added to lists, not the action: " + action); //$NON-NLS-1$
				} catch (IllegalArgumentException exc) {
					exc.printStackTrace();
				}
			}
			return super.add(o);
		}
	}
	private List selectionActions = new ActionIDList();
	private List stackActions = new ActionIDList();
	private List propertyActions = new ActionIDList();
	public void commandStackChanged(EventObject event) {
		updateActions(stackActions);
	}
	@Override
	public Object getAdapter(Class type) {
		if (type == GraphicalViewer.class)
			return getGraphicalViewer();
		if (type == CommandStack.class)
			return getCommandStack();
		if (type == ActionRegistry.class)
			return getActionRegistry();
		if (type == EditPart.class && getGraphicalViewer() != null)
			return getGraphicalViewer().getRootEditPart();
		if (type == IFigure.class && getGraphicalViewer() != null)
			return ((GraphicalEditPart)getGraphicalViewer().getRootEditPart()).getFigure();
		return super.getAdapter(type);
	}
}
