package com.architexa.diagrams.generate.subclipse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.LabelProvider;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.commands.GetRemoteResourceCommand;
import org.tigris.subversion.subclipse.core.history.LogEntry;
import org.tigris.subversion.subclipse.core.history.LogEntryChangePath;
import org.tigris.subversion.svnclientadapter.SVNRevision;

import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.generate.team.ChangeSet;
import com.architexa.diagrams.generate.team.EmptyRevision;

public class SVNChangeSet extends ChangeSet {

	private static final Logger logger = GeneratePlugin.getLogger(SVNChangeSet.class);

	public SVNChangeSet(LogEntry logEntry) {
		super(logEntry);
	}

	@Override
	public LogEntry getLogEntry() {
		// Log entry passed in constructor is of type
		// org.tigris.subversion.subclipse.core.history.LogEntry, 
		// so can cast to that
		return (LogEntry) super.getLogEntry();
	}

	@Override
	public Map getAffectedResourcesNamesToLogEntrMap() {
		Map nameToLogEntryMap = new HashMap();
		for (LogEntryChangePath path : getAffectedResourceList()){
			String p = path.getPath();
			int lastSlash = p.lastIndexOf("/");
			String pth = p.substring(1, lastSlash);
			String className = p.substring(lastSlash + 1);
			String name = className + " (" + pth + ")";
			nameToLogEntryMap.put(path, name);
		}
		return nameToLogEntryMap;
	}
	
	@Override
	public Object[] getAffectedResources() {
		return getAffectedResourceList().toArray();
	}
	
	public List<LogEntryChangePath> getAffectedResourceList() {
		LogEntry svnLogEntry = getLogEntry();
		LogEntryChangePath[] affectedResources = svnLogEntry.getLogEntryChangePaths();
		List<LogEntryChangePath> list = new ArrayList<LogEntryChangePath>(Arrays.asList(affectedResources));
		List<LogEntryChangePath> templist = new ArrayList<LogEntryChangePath>(list);
		for (LogEntryChangePath res : list) {
			String path = res.getPath();
			// cannot be a .java file
			if (path.length() > 6 && path.substring(path.length() - 5).equalsIgnoreCase(".java"))
				continue;
			
			templist.remove(res);
		}
		return templist;
	}

	@Override
	public LabelProvider getAffectedResourcesSelectionLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				try {
					// The items in the array returned by 
					// SVNChangeSet.getAffectedResources()
					// are of type LogEntryChangePath, so
					// can cast to that
					return ((LogEntryChangePath)element).getPath();
				} catch(Exception e) {
					e.printStackTrace();
				}
				return super.getText(element);
			}
		};
	}

	@Override
	public String getSelectedRevisionNumber() {
		LogEntry svnLogEntry = getLogEntry();
		long selectedRevisionNum = svnLogEntry.getRevision().getNumber();
		return String.valueOf(selectedRevisionNum);
	}

	@Override
	public ISVNRemoteResource getSelectedRemoteRes(Object selection) {
		try {
			// The items in the dialog are of type LogEntryChangePath, so
			// selection will be of that type and can cast to it
			LogEntryChangePath path = (LogEntryChangePath) selection;
			ISVNRemoteResource selectedRemoteRes = path.getRemoteResource();

			return selectedRemoteRes;
		} catch(SVNException e) {
			logger.error("Unexpected exception while getting " +
					"remote resource for selection "+selection+". ", e);
		}
		return null;
	}

	@Override
	public Object getPreviousRemoteRes(Object selection, String previousRevisionNum){
		try {
			// The items in the dialog are of type LogEntryChangePath, so
			// selection will be of that type and can cast to it
			LogEntryChangePath path = (LogEntryChangePath) selection;

			if (path.getAction() == 'A' || path.getAction() == 'a')
				return new EmptyRevision();
			// Getting the svn remote resource of the selection's previous
			// revision (See LogEntryChangePath.getRemoteResource())
			SVNRevision prevRevision = SVNRevision.getRevision(String.valueOf(previousRevisionNum));
			GetRemoteResourceCommand command = new GetRemoteResourceCommand(
					path.getLogEntry().getResource().getRepository(), 
					path.getUrl(), 
					prevRevision);
			command.run(null);
			return command.getRemoteResource();
		} catch(Exception e) {
			logger.error("Unexpected exception while getting " +
					"remote resource for revision "+previousRevisionNum+". ", e);
		}
		return null;
	}

	@Override
	public void addToMap(Object sel, Map<Object, Object> selectedResToPreviousVersionOfRes) {
		long previousRevisionNum = Long.parseLong(getSelectedRevisionNumber()) - 1;
		Object selectedRemoteRes = getSelectedRemoteRes(sel);
		Object previousRemoteRes = getPreviousRemoteRes(sel, String.valueOf(previousRevisionNum));
		if(selectedRemoteRes!=null && previousRemoteRes!=null)
			selectedResToPreviousVersionOfRes.put(selectedRemoteRes, previousRemoteRes);
		else 
			logger.error("Unable to get required remote information for "+
					"selection " + sel);
	}
}
