package com.architexa.diagrams.utils;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.architexa.collab.UIUtils;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.store.ReloRdfRepository;


//build related errors
public class ErrorUtils {

	/* 
	 * if the AF/artifact is null or has no type then it does not contain javaElements that can be opened by RSE
	 *	so throw error (only once)
	 *
	 * Use as follow to make sure error is only shown once
	 * 
	 * 		    boolean isValidType = ErrorUtils.isValidType(newAF, repo, errorShown);
	 *	        if (!isValidType) {
	 *		      	errorShown = !isValidType;
	 *	        	continue;
	 *		    }
	 *	        
	 */
	public static boolean isValidType(Object obj, ReloRdfRepository repo, boolean errorShown) {
		
		if (obj instanceof ArtifactFragment
				&& ((ArtifactFragment) obj).getArt()!=null && ((ArtifactFragment) obj).getArt().queryOptionalType(repo) == null) {
			if (!errorShown) {
				errorShown = true;
				openBuildError(null, "Could not find needed file type! \n"
						+ "Architexa has encountered a problem while attempting to open this Diagram.\n"
						+ "If you have just created this class we may \n" 
						+ "be waiting for the Eclipse Builder to catch up.\n"
						+ "Otherwise, consider rebuilding your Architexa Index\n\n"
						+ "File -> Architexa -> Rebuild Complete Index");
			}
			return false;
		}
		
	    if (obj == null) {
	    	if (!errorShown) {
	    		errorShown = true;
	    		openError("Package, class, method, or field");
	    	}
	    	return false;
	    }
	    return true;
	}

	public static void openBuildError(Shell shell, String msg) {
		UIUtils.openErrorPromptDialog("Error Opening Diagram", msg);
	}

	public static void openError(String types) {
		UIUtils.openErrorPromptDialog("Architexa RSE - Invalid File Type", "Make sure the item you are trying to add is a valid file type. \n" +
				types + ". \n\n");
	}

}
