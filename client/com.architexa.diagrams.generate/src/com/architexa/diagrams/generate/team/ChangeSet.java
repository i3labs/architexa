package com.architexa.diagrams.generate.team;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.architexa.diagrams.generate.GeneratePlugin;

/**
 * 
 * Wraps a version control specific change set.
 *
 */
public abstract class ChangeSet {

	private static final Logger logger = GeneratePlugin.getLogger(ChangeSet.class);

	private Object logEntry;

	public ChangeSet(Object logEntry) {
		this.logEntry = logEntry;
	}

	public Object getLogEntry() {
		return logEntry;
	}

	/**
	 * @return an array of resources modified in the selected change set
	 */
	public abstract Object[] getAffectedResources();
	
	public Map getAffectedResourcesNamesToLogEntrMap(){
		return new HashMap();
	};

	/**
	 * @return a LabelProvider to properly display the affected
	 * resources in the dialog that allows the user to select which
	 * of those affected resources he wants to include in the diagram
	 */
	public abstract LabelProvider getAffectedResourcesSelectionLabelProvider();

	/**
	 * @return the revision number of the selected change set
	 */
	public abstract String getSelectedRevisionNumber();

	/**
	 * @return the resource in the version-control repository that
	 * corresponds to the selected affected resource.
	 */
	public abstract Object getSelectedRemoteRes(Object selection);

	/**
	 * @return the resource in the version-control repository that
	 * corresponds to the previous version of the selected affected resource.
	 */
	public abstract Object getPreviousRemoteRes(Object selection, String previousRevisionNum);

	/**
	 * Gets the current and previous revisions from the appropriate repository
	 * based on 'sel' and adds them to the map
	 * 
	 * @param sel
	 * @param selectedResToPreviousVersionOfRes
	 */
	public abstract void addToMap(Object sel, Map<Object, Object> selectedResToPreviousVersionOfRes);
	
	/**
	 * @param monitor 
	 * @return a Map from the version of a file in the selected change
	 * set X to the version of that file in its previous change set X-1
	 */
	public ListSelectionDialog openAffectedFilesSelectionDialog() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		// Get affected resources in log entry
		Object[] affectedResources = getAffectedResources();

		// Open a dialog so user can select which of those affected
		// resources' changes he wants to include in the diagram
		ListSelectionDialog selectDialog = new ListSelectionDialog(
				shell, 
				affectedResources, 
				new ArrayContentProvider(), 
				getAffectedResourcesSelectionLabelProvider(), 
				"Selected change set contained changes to the following resources. " +
		"\nSelect resources whose revision changes you want to see in a diagram:");
		selectDialog.setTitle("Choose Resources");

		return selectDialog;
	}
}
