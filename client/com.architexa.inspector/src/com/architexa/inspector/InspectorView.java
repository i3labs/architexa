package com.architexa.inspector;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.architexa.diagrams.jdt.actions.ReinitRepository;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.relo.jdt.services.PluggableEditPartSupport;
import com.architexa.diagrams.ui.ImageCache;



/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class InspectorView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.architexa.inspector.views.InspectorView";

	private static InspectorView thisView = null;
	
	private TreeViewer viewer;
	private TreeNode invisibleRoot = new TreeNode();
	

	public static class TreeNode implements IAdaptable {
		public ArtifactFragment af;
		public boolean hasChild;

		private TreeNode parent;
		
		public TreeNode() {
		}
		public TreeNode(ArtifactFragment af, boolean hasChild) {
			this.af = af;
			this.hasChild = hasChild;
		}
		public void setParent(TreeNode parent) {
			this.parent = parent;
		}
		public TreeNode getParent() {
			return parent;
		}
		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class type) {
			if (type==ArtifactFragment.class) {
				// the below is correct, but we need to make a copy of the AF's
				// when creating diagrams and we are not doing that - therefore
				// we make a copy here (and not have unintended side-effects).
				// Goal is to eventually fix the RSE core.
				//return af;
				return new ArtifactFragment(af.getArt());
			}
			return null;
		}
	}
	
	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof TreeNode) {
				return ((TreeNode)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent == invisibleRoot)
				return ModelUtils.getProjects().toArray();
			if (parent instanceof TreeNode)
				return ModelUtils.getChildren((TreeNode)parent).toArray();
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent == invisibleRoot)
				return true;
			if (parent instanceof TreeNode)
				return ((TreeNode)parent).hasChild;
			return false;
		}
	}
	
	class ViewLabelProvider extends LabelProvider {
		@Override
		public String getText(Object obj) {
			if (obj instanceof TreeNode) {
				return ModelUtils.getName((TreeNode)obj);
			}
			return obj.toString();
		}
		@Override
		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj == invisibleRoot)
				   imageKey = ISharedImages.IMG_OBJ_FOLDER;
			if (obj instanceof TreeNode) {
				TreeNode tn = (TreeNode) obj;
				return ImageCache.calcImageFromDescriptor(
						PluggableEditPartSupport.getIconDescriptor(ModelUtils.getRepo(), tn.af.getArt(), tn.af.queryType(ModelUtils.getRepo()))
						);
			}
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}

	/**
	 * The constructor.
	 */
	public InspectorView() {
		thisView = this;
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new ViewerSorter() {});
		viewer.setInput(getViewSite());
		hookContextMenu();
		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
		tbm.add(
				new Action("Refresh", Activator.getImageDescriptor("icons/refresh.gif")) {
					@Override
					public void run() {
						viewer.refresh();
					}
				}
			);
		tbm.add(
				new Action("Clear DB" , Activator.getImageDescriptor("icons/new_con.gif")) {
					@Override
					public void run() {
						ReinitRepository.launchJob();
					}
				}
			);
		tbm.add(
				new Action("Dump", Activator.getImageDescriptor("icons/details_view.gif")) {
					@Override
					public void run() {
						ModelUtils.dumpDB();
					}
				}
			);
		for (IAction a : additionalViewActions) {
			getViewSite().getActionBars().getToolBarManager().add(a);
		}
		getViewSite().setSelectionProvider(viewer);
	}
	
	private static List<IAction> additionalViewActions = new ArrayList<IAction>();
	public static void addViewAction(IAction a) {
		additionalViewActions.add(a);
		if (thisView != null) {
			thisView.getViewSite().getActionBars().getToolBarManager().add(a);
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}