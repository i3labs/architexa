package com.architexa.diagrams.generate.subclipse;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.team.internal.ui.history.GenericHistoryView;
import org.eclipse.team.ui.history.IHistoryPage;
import org.tigris.subversion.subclipse.core.history.LogEntry;
import org.tigris.subversion.subclipse.ui.history.SVNHistoryPage;

import com.architexa.diagrams.generate.team.RevisionDiagramGenerator;
import com.architexa.diagrams.generate.team.RevisionViewGenerateAction;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SubclipseRevisionDiagramGenerator extends RevisionDiagramGenerator {

	public SubclipseRevisionDiagramGenerator() {
		super(new SubclipseUncommittedChangesDiagramGenerator());
	}

	@Override
	public void createGenerateAction(GenericHistoryView view) {
		if(!(view.getHistoryPage() instanceof SVNHistoryPage)) {
			return;
		}
		super.createGenerateAction(view);
	}

	@Override
	protected void listenForSelections(IHistoryPage historyPage, final List<RevisionViewGenerateAction> diagramGenerateActions) {

		if(!(historyPage instanceof SVNHistoryPage)) return;

		final TableViewer table = lookuptableHistoryViewer((SVNHistoryPage)historyPage);
		table.getTable().addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {

				ISelection selection = table.getSelection();
				if(!(selection instanceof IStructuredSelection)) {
					for(RevisionViewGenerateAction action : diagramGenerateActions) action.clearSelection();
					return;
				}
				IStructuredSelection ss = (IStructuredSelection) selection;
//				Object o = ss.getFirstElement();
				List<SVNChangeSet> selList = new ArrayList<SVNChangeSet>();
				Iterator itr = ss.iterator();
				while (itr.hasNext()) {
					Object o = itr.next();
					if(!(o instanceof LogEntry)) {
						for(RevisionViewGenerateAction action : diagramGenerateActions) action.clearSelection();
						return;
					}
					LogEntry entry = (LogEntry) o;
					selList.add(new SVNChangeSet(entry));
				}
//				ISVNResource baseResource = entry.getResource();
//				IResource file = baseResource.getResource();
//				if(!(file instanceof IFile)) {
//					for(RevisionViewGenerateAction action : diagramGenerateActions) action.clearSelection();
//					return;
//				}

//				ISVNRemoteResource remoteRes = entry.getRemoteResource();
//				if(!(remoteRes instanceof ISVNRemoteFile)) {
//					for(RevisionViewGenerateAction action : diagramGenerateActions) action.clearSelection();
//					return;
//				}

//				for(RevisionViewGenerateAction action : diagramGenerateActions) action.setSelection(new SVNChangeSet(entry));
				for(RevisionViewGenerateAction action : diagramGenerateActions) action.setSelection(selList);
			}
		});
	}

	private static TableViewer lookuptableHistoryViewer(SVNHistoryPage page) {
		Field[] fields = SVNHistoryPage.class.getDeclaredFields();
		for (Field f : fields) {
			if (!f.getName().equals("tableHistoryViewer")) continue;
			f.setAccessible(true);
			try {
				Object retVal = f.get(page);
				return (TableViewer) retVal;
			} catch (Exception e) {}
		}
		return null;
	}

}
