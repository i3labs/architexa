package com.architexa.diagrams.generate.team.cvs;


import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.client.listeners.LogEntry;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;

import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.generate.team.ChangeSet;

public class CVSChangeSet extends ChangeSet {

	private static final Logger logger = GeneratePlugin.getLogger(CVSChangeSet.class);

	public CVSChangeSet(LogEntry logEntry) {
		super(logEntry);
	}

	@Override
	public LogEntry getLogEntry() {
		// Log entry passed in constructor is of type
		// org.eclipse.team.internal.ccvs.core.client.listeners.LogEntry, 
		// so can cast to that
		return (LogEntry) super.getLogEntry();
	}

	@Override
	public Object[] getAffectedResources() {
		LogEntry cvsLogEntry = getLogEntry();
		ICVSRemoteFile[] resources = new ICVSRemoteFile[]{getLogEntry().getRemoteFile()};
		return resources;
	}

	@Override
	public LabelProvider getAffectedResourcesSelectionLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				/*try {
					// The items in the array returned by 
					// CVSChangeSet.getAffectedResources()
					// are of type [], so
					// can cast to that
					return (()element).getPath();
				} catch(Exception e) {
					e.printStackTrace();
				}*/
				return super.getText(element);
			}
		};
	}

	@Override
	public String getSelectedRevisionNumber() {
		LogEntry cvsLogEntry = getLogEntry();
		return cvsLogEntry.getRevision();
	}

	@Override
	public Object getSelectedRemoteRes(Object selection) {
		RemoteFile path = (RemoteFile) selection;
		ILogEntry[] logEntries;
		try {
			return path.getLogEntry(new NullProgressMonitor()).getRemoteFile();
		} catch (CVSException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Object getPreviousRemoteRes(Object selection, String selectedRevisionNum) {
		try {
			// The items in the dialog are of type LogEntryChangePath, so
			// selection will be of that type and can cast to it
			RemoteFile path = (RemoteFile) selection;
			ILogEntry[] logEntries = path.getLogEntries(new NullProgressMonitor());
			int i = 0;
			for (ILogEntry entry: logEntries) {
				i++;
				String rev = entry.getRevision();
				if (entry.getRevision().equals(String.valueOf(selectedRevisionNum))) {
					// get the current selection + 1, this will be the previous version
					return logEntries[i].getRemoteFile(); 
				}
			}
		} catch(Exception e) {
			logger.error("Unexpected exception while getting " +
					"remote resource for revision "+selectedRevisionNum+". ", e);
		}
		return null;
	}

	@Override
	public void addToMap(Object sel, Map<Object, Object> selectedResToPreviousVersionOfRes) {
		ICVSRemoteResource selectedRemoteRes = (ICVSRemoteResource) getSelectedRemoteRes(sel);
		ICVSRemoteResource previousRemoteRes = (ICVSRemoteResource) getPreviousRemoteRes(sel, getSelectedRevisionNumber());
		if(selectedRemoteRes!=null && previousRemoteRes!=null)
			selectedResToPreviousVersionOfRes.put(selectedRemoteRes, previousRemoteRes);
		else 
			logger.error("Unable to get required remote information for "+
					"selection " + sel);
	}

	
}
