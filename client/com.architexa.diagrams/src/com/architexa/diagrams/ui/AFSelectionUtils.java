package com.architexa.diagrams.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.model.ArtifactFragment;

public class AFSelectionUtils {
	static final Logger logger = Activator.getLogger(AFSelectionUtils.class);

	public static List<ArtifactFragment> getSelection() {
		List<ArtifactFragment> selAF = new ArrayList<ArtifactFragment>(10);
		
    	final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    	ISelection selection = activeWorkbenchWindow.getSelectionService().getSelection();
    	if (selection == null) {
    		// do nothing
    	} if (selection instanceof ArtifactFragment) {
    		selAF.add((ArtifactFragment) selection);
    	} else if (selection instanceof IStructuredSelection) {
    		IStructuredSelection ss = (IStructuredSelection)selection;
    		for(Object sel : ss.toArray()) {
    			if (sel instanceof IAdaptable) {
    				Object adapted = ((IAdaptable)sel).getAdapter(ArtifactFragment.class);
    				if (adapted != null && adapted instanceof ArtifactFragment)
    					selAF.add((ArtifactFragment) adapted);
    			}
    		}
    	} else {
	        logger.error("Can't deal with selection type: " + selection.getClass());
		}
		return selAF;
    }

}
