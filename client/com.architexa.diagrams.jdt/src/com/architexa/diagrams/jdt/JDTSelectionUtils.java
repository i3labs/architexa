package com.architexa.diagrams.jdt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.internal.ui.synchronize.UnchangedResourceModelElement;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.architexa.collab.UIUtils;
import com.architexa.diagrams.jdt.compat.SCUtils;

@SuppressWarnings("restriction")
public class JDTSelectionUtils  {
    private static final Logger logger = Activator.getLogger(JDTSelectionUtils.class);

	public static IJavaElement getSelectedJDTElement() {
		List<IJavaElement> selJDTElelements = getSelectedJDTElements(false);
		if (selJDTElelements.isEmpty())
			return null;
		else
			return selJDTElelements.get(0);
    }
	
	public static interface IConverter {
		public IJavaElement getJE(Object in);
	}
	
	public static Map<Class<?>, IConverter> classToConverterMap = new HashMap<Class<?>, JDTSelectionUtils.IConverter>();  
	
	/**
	 * @param referenced - if selection is on a type return that type as well?
	 */
	public static List<IJavaElement> getSelectedJDTElements(boolean referenced) {
		List<IJavaElement> selJDTElelements = new ArrayList<IJavaElement>(10);
		
    	final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    	ISelection selection = activeWorkbenchWindow.getSelectionService().getSelection();
    	if (selection == null) {
    		// do nothing
    	} else if (selection instanceof IStructuredSelection) {
    		IStructuredSelection ss = (IStructuredSelection)selection;
    		for(Object sel : ss.toArray()) {
    			for (Class<?> c : classToConverterMap.keySet()) {
    				if (sel.getClass().equals(c)) {
						IConverter iConverter = classToConverterMap.get(sel.getClass());
						if (iConverter != null)
							selJDTElelements.add(iConverter.getJE(sel));
					}
    			}
    			if (sel instanceof IJavaElement)
    				selJDTElelements.add((IJavaElement) sel);
    			else if (sel instanceof IDiffElement) {
    				List<IDiffElement> errorDiffs = new ArrayList<IDiffElement>();
    				selJDTElelements.addAll(getNestedDiffIJavaElements((IDiffElement) sel, errorDiffs));
    				if (!errorDiffs.isEmpty())
    					UIUtils.openErrorPromptDialog(
    							"Error Opening Compare Diagram",
    							"You have selected an unknown type to open in a diagram.\n" +
    							"("+errorDiffs.toString()+")\n" +
    							" Please select a Java file ending in '.java'");
    			} else if (sel instanceof IAdaptable) {
    				Object adapted = ((IAdaptable)sel).getAdapter(IJavaElement.class);
    				if (adapted != null && adapted instanceof IJavaElement)
    					selJDTElelements.add((IJavaElement) adapted);
    			}
    		}
    	} else if (selection instanceof ITextSelection) {
    		try {
    			ITextSelection txtSel = (ITextSelection)selection;
        		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        		if(activeEditor instanceof JavaEditor) {
            		JavaEditor currentEditor = (JavaEditor) activeEditor;
            		IJavaElement currEditorInput = SCUtils.SelectionConverter_getInput(currentEditor);
            		IJavaElement currDeclaredElement = SelectionConverter.resolveEnclosingElement(currEditorInput, txtSel);
                    selJDTElelements.add(currDeclaredElement);

                    if (referenced) {
                        IJavaElement[] referencedElements = SelectionConverter.codeResolve(currEditorInput, txtSel);
                        if (referencedElements!=null) selJDTElelements.addAll(Arrays.asList(referencedElements));
                    }
        		}
    		} catch (JavaModelException e) {
    			logger.error("Unexpected Exception", e);
    		}
		} else {
	        logger.error("Can't deal with selection type: " + selection.getClass());
		}
		return new ArrayList<IJavaElement>(selJDTElelements);
    }

	// get IJE for elements in the Synchronize View
	private static Set<IJavaElement> getNestedDiffIJavaElements(IDiffElement diffSel, List<IDiffElement> errorDiffs) {
		Set<IJavaElement> nestedJavaElements = new HashSet<IJavaElement>();
		if (diffSel instanceof UnchangedResourceModelElement) { //project
			UnchangedResourceModelElement urme = (UnchangedResourceModelElement) diffSel;
			for (IDiffElement nestedDiffSel : urme.getChildren()) {
				nestedJavaElements.addAll(getNestedDiffIJavaElements(nestedDiffSel, errorDiffs));
			}
		} else if (diffSel instanceof SyncInfoModelElement) { //leaf
			try {
				IJavaElement ije = getIJavaElement((SyncInfoModelElement) diffSel);
				nestedJavaElements.add(ije);
			} catch (Throwable t) {
				// Ignore errors for now...
				// errorDiffs.add(diffSel);
			}
		} else {
			// errorDiffs.add(diffSel);
		}
		return nestedJavaElements;
	}

	private static IJavaElement getIJavaElement(SyncInfoModelElement syncSel) {
		IResource res = syncSel.getResource();

		if (!(res instanceof IFile)) return null;
		ICompilationUnit cu = JavaCore.createCompilationUnitFrom((IFile) res);	
		return (IJavaElement) cu.getPrimaryElement();
	}
	
}
