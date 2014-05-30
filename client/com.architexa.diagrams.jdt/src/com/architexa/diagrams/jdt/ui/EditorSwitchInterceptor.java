package com.architexa.diagrams.jdt.ui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.internal.ui.viewsupport.ProblemTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.architexa.collab.proxy.PluginUtils;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.utils.RootEditPartUtils;

/**
 * The Package Explorer has a listener that is responsible for opening
 * an editor for a selection when the Link with Editor button is
 * toggled on. EditorSwitchInterceptor is responsible for preventing
 * such switching away from a diagram editor when Link with Editor is 
 * toggled on and the user selects something in the Package Explorer
 * in order to drag it into the diagram.
 * 
 */
public class EditorSwitchInterceptor {
	static final Logger logger = Activator.getLogger(EditorSwitchInterceptor.class);

	private IWorkbenchPart lastActivePart;

	/**
	 * Finds the listener that switches to an editor for the selection and 
	 * replaces it with our own that will not open such an editor when the 
	 * active editor is a diagram.
	 */
	public void initialize() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
				if (window == null) return;
				IWorkbenchPart mpEditor = window.getActivePage().getActivePart();
				lastActivePart = RootEditPartUtils.getEditorFromRSEMultiPageEditor(mpEditor);

				IViewReference[] openViews = window.getActivePage().getViewReferences();
				for(IViewReference ref : openViews) {
					IViewPart openView = ref.getView(false);
					if(!(openView instanceof PackageExplorerPart)) continue;
					replaceSelectionListener((PackageExplorerPart)openView);
					break;
				}

				window.getActivePage().addPartListener(new IPartListener() {
					public void partActivated(IWorkbenchPart part) {
						if(part instanceof PackageExplorerPart)
							replaceSelectionListener((PackageExplorerPart)part);
						else 
							lastActivePart = RootEditPartUtils.getEditorFromRSEMultiPageEditor(part);
					}
					public void partBroughtToTop(IWorkbenchPart part) {}
					public void partClosed(IWorkbenchPart part) {}
					public void partDeactivated(IWorkbenchPart part) {}
					public void partOpened(IWorkbenchPart part) {
					}
				});
			}
		});
	}

	private void replaceSelectionListener(final PackageExplorerPart packageExplorer) {
		Field[] fields = PackageExplorerPart.class.getDeclaredFields();

		try {
			// Get the Package Explorer viewer
			ProblemTreeViewer fViewerField = null;
			for (Field f : fields) {
				if (!"fViewer".equals(f.getName())) continue;
				f.setAccessible(true);

				fViewerField = (ProblemTreeViewer) f.get(packageExplorer);
				if(fViewerField!=null) break;
			}
			final ProblemTreeViewer fViewer = fViewerField;
			if(fViewer==null) return;

			// Find the selection listener that opens
			// an editor when linked mode is on
			ISelectionChangedListener fPostSelectionListener = getSelListener(packageExplorer);

			// Replace listener with a new listener that only
			// opens an editor if the active editor wasn't a diagram
			fViewer.removePostSelectionChangedListener(fPostSelectionListener);
			ISelectionChangedListener newListener = new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					handlePostSelectionChanged(event);
				}
				private void handlePostSelectionChanged(SelectionChangedEvent event) {
					try {
						ISelection selection= event.getSelection();

						// If open editor is a diagram, don't switch away from it
						// Otherwise, link to editor for selection as PackageExplorerPart normally would
						linkToEditorIfDiagramNotActive(selection, packageExplorer, fViewer);
					} catch (Exception e) {
						logger.error("Unexpected exception while opening editor corresponding " +
								"to selection in Package Explorer "+event.getSelection(), e);
					}
				}
			};
			fViewer.addPostSelectionChangedListener(newListener);

			double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
			if(jdtUIVer < 3.5) {
				Field fPostSelectionListenerField = PackageExplorerPart.class.getDeclaredField("fPostSelectionListener");
				fPostSelectionListenerField.setAccessible(true);
				fPostSelectionListenerField.set(packageExplorer, newListener);
			}
		} catch (Exception e) {
			logger.error("Unexpected exception while replacing the " +
					"Package Explorer selection listener.", e);
		}
	}

	private ISelectionChangedListener getSelListener(PackageExplorerPart 
			packageExplorer) throws SecurityException, NoSuchFieldException, 
			IllegalArgumentException, IllegalAccessException {
		double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
		if(jdtUIVer >= 3.5) {
			// Since 3.5, PackageExplorerPart keeps a OpenAndLinkWithEditorHelper
			// that stores the selection listener
			Field helperField = PackageExplorerPart.class.getDeclaredField("fOpenAndLinkWithEditorHelper");
			helperField.setAccessible(true);

			/*OpenAndLinkWithEditorHelper*/ Object helper = helperField.get(packageExplorer);
			// anon class, so get its superclass
			Class helperClass = helper.getClass().getSuperclass();
			Field listenerField = helperClass.getDeclaredField("listener");
			listenerField.setAccessible(true);
			/*InternalListener*/ Object listener = listenerField.get(helper);
			return (ISelectionChangedListener) listener;
		} else {
			// In earlier eclipse versions, PackageExplorer stores the selection listener
			Field fPostSelectionListenerField = PackageExplorerPart.class.getDeclaredField("fPostSelectionListener");
			fPostSelectionListenerField.setAccessible(true);
			return (ISelectionChangedListener) fPostSelectionListenerField.get(packageExplorer);
		}
	}

	private void linkToEditorIfDiagramNotActive(ISelection selection, 
			PackageExplorerPart packageExplorer, ProblemTreeViewer viewer) throws 
			IllegalArgumentException, IllegalAccessException, SecurityException, 
			NoSuchFieldException, NoSuchMethodException, InvocationTargetException {

		// If open editor is a diagram, don't switch away from it
		if(lastActivePart instanceof RSEEditor) return;

		// Otherwise, link to editor for selection as PackageExplorerPart normally would
		if(!packageExplorer.isLinkingEnabled()) return;

		double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
		if(jdtUIVer >= 3.5) {

			Field helperField = PackageExplorerPart.class.getDeclaredField("fOpenAndLinkWithEditorHelper");
			helperField.setAccessible(true);
			/*OpenAndLinkWithEditorHelper*/ Object helper = helperField.get(packageExplorer);
			Class helperClass = helper.getClass().getSuperclass();

			Field lastOpenSelection = helperClass.getDeclaredField("lastOpenSelection");
			lastOpenSelection.setAccessible(true);

			if (packageExplorer.isLinkingEnabled() && !selection.equals(lastOpenSelection) && viewer.getControl().isFocusControl()) {
				for(Method m : helperClass.getDeclaredMethods()) {
					if("linkToEditor".equals(m.getName())) {
						m.setAccessible(true);
						m.invoke(helper, (IStructuredSelection)selection);
						break;
					}
				}
				lastOpenSelection.set(helper, null);
			}
		} else {
			Field fLastOpenSelection = PackageExplorerPart.class.getDeclaredField("fLastOpenSelection");
			fLastOpenSelection.setAccessible(true);
			if(fLastOpenSelection==null) return;
			final ISelection fLastOpenSelectionValue = (ISelection) fLastOpenSelection.get(packageExplorer);

			if(packageExplorer.isLinkingEnabled() && !selection.equals(fLastOpenSelectionValue)) {
				for(Method m : PackageExplorerPart.class.getDeclaredMethods()) {
					if("linkToEditor".equals(m.getName())) {
						m.setAccessible(true);
						m.invoke(packageExplorer, (IStructuredSelection)selection);
						break;
					}
				}
			}
			fLastOpenSelection.set(packageExplorer, null);
		}
	}

}
