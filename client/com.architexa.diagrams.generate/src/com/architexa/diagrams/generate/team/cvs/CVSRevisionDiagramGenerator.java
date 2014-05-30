package com.architexa.diagrams.generate.team.cvs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.client.listeners.LogEntry;
import org.eclipse.team.internal.ccvs.core.filehistory.CVSFileRevision;
import org.eclipse.team.internal.ccvs.ui.CVSHistoryPage;
import org.eclipse.team.internal.ui.history.GenericHistoryView;
import org.eclipse.team.ui.history.IHistoryPage;

import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.generate.team.RevisionDiagramGenerator;
import com.architexa.diagrams.generate.team.RevisionViewGenerateAction;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class CVSRevisionDiagramGenerator extends RevisionDiagramGenerator {

	private static final Logger logger = GeneratePlugin.getLogger(CVSRevisionDiagramGenerator.class);

	public CVSRevisionDiagramGenerator() {
		super(new CVSUncommittedChangesDiagramGenerator());
	}

	@Override
	public void createGenerateAction(GenericHistoryView view) {
		if(!(view.getHistoryPage() instanceof CVSHistoryPage)) {
			return;
		}
		super.createGenerateAction(view);
	}

	@Override
	protected void listenForSelections(final IHistoryPage historyPage, final List<RevisionViewGenerateAction> diagramGenerateActions) {

		if(!(historyPage instanceof CVSHistoryPage)) return;
		final CVSHistoryPage cvsHistoryPage = (CVSHistoryPage) historyPage;

		final TreeViewer treeViewer = cvsHistoryPage.getTreeViewer();
		treeViewer.getTree().addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = treeViewer.getSelection();
				if(!(selection instanceof IStructuredSelection)) {
					for(RevisionViewGenerateAction action : diagramGenerateActions) action.clearSelection();
					return;
				}

				IStructuredSelection ss = (IStructuredSelection) selection;
//				Object o = ss.getFirstElement();
				Iterator itr = ss.iterator();
				List<CVSChangeSet> selList = new ArrayList<CVSChangeSet>();
				ICVSRemoteFile remoteRes = null;
				try {
					while (itr.hasNext()) {
						Object o = itr.next();

						// Items in CVS History View are of type
						// CVSFileRevision,
						// from which we can get a cvs log entry
						if (!(o instanceof CVSFileRevision)) {
							for (RevisionViewGenerateAction action : diagramGenerateActions)
								action.clearSelection();
							return;
						}
						remoteRes = ((CVSFileRevision) o).getCVSRemoteFile();
						if (remoteRes == null) {
							for (RevisionViewGenerateAction action : diagramGenerateActions)
								action.clearSelection();
							return;
						}

						ILogEntry logEntry = remoteRes.getLogEntry(new NullProgressMonitor());
						if (!(logEntry instanceof LogEntry)) {
							for (RevisionViewGenerateAction action : diagramGenerateActions)
								action.clearSelection();
							return;
						}
						
						selList.add(new CVSChangeSet((LogEntry) logEntry));
						
					}	
					
					for (RevisionViewGenerateAction action : diagramGenerateActions)
						action.setSelection(selList);
					
				} catch (TeamException e1) {
					logger.warn("Unable to get log entry for remote CVS resource " + remoteRes.getName(), e1);
				}
			}
		});
	}

}
