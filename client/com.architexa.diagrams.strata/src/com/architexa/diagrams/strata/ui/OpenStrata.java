/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */
package com.architexa.diagrams.strata.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.openrdf.model.Resource;

import com.architexa.diagrams.editors.RSEMultiPageEditor;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.actions.OpenVizAction;
import com.architexa.diagrams.jdt.builder.ResourceQueue;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.relo.jdt.browse.ClassStrucBrowseModel;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.cache.DepNdx;
import com.architexa.diagrams.strata.commands.ModelUtils;
import com.architexa.diagrams.strata.model.StrataFactory;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.ui.RSEShareableDiagramEditorInput;
import com.architexa.diagrams.utils.DbgRes;
import com.architexa.diagrams.utils.RunWithObjectParam;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.rse.BuildStatus;
import com.architexa.store.PckgDirRepo;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.RepositoryMgr;
import com.architexa.store.StoreUtil;

public class OpenStrata extends OpenVizAction {
	public static final Logger logger = StrataPlugin.getLogger(OpenStrata.class);

	public OpenStrata() {
		setText("Layered Diagram");
		setImageDescriptor(StrataPlugin.getImageDescriptor("icons/office-document.png"));
	}

	@Override
	public void selectionChanged(IAction action, ISelection sel) {
		// Only open if there is something other than a method/field selected
	
		List<?>  items = this.getSelection();

		for (Object selectedElt : items) {
			// we only need one good item

			if (selectedElt instanceof IJavaProject && ((IJavaProject) selectedElt).getProject().isOpen()) {
				action.setEnabled(true); return;
			} else if (selectedElt instanceof ArtifactFragment) {
				Resource selType = ((ArtifactFragment)selectedElt).queryType(StoreUtil.getDefaultStoreRepository());
				if (selType != null) {
					if (selType.equals(RJCore.projectType) || selType.equals(RJCore.packageType))
						action.setEnabled(true); return;
				}
			}
			//} else if (selectedElt instanceof IProject) {
			//} else if(selectedElt==null || selectedElt instanceof IMethod ||
			//		(sel instanceof IStructuredSelection && OpenItemUtils.containsMethodOrField((IStructuredSelection) sel)) ||
			//		selectedElt instanceof IField) {
			//} else {
			//}
		}
	}


	@Override
	public void openViz(IWorkbenchWindow activeWorkbenchWindow, List<?> selList) {
		openViz(activeWorkbenchWindow, selList, null, null, null);
	}


	@Override
	public void openViz(IWorkbenchWindow activeWorkbenchWindow,
			List<?> selList, Map<IDocument, IDocument> lToRDocMap, Map<IDocument, String> lToPathMap, StringBuffer docBuff) {
		BuildStatus.addUsage("Strata");
		if(selList==null || selList.size() == 0) return;
		buildAndOpenStrataDoc(activeWorkbenchWindow, selList, lToRDocMap, lToPathMap, docBuff, null);
	}


	public static void buildAndOpenStrataDoc(IWorkbenchWindow activeWorkbenchWindow, List<?> selList, Map<IDocument,
			IDocument> lToRDocMap, Map<IDocument, String> lToPathMap, StringBuffer docBuff, IPath repoPath) {
		
		boolean afsOnly = true;
		for (Object sel : selList) {
			if (!(sel instanceof ArtifactFragment)) afsOnly = false;
		}
		buildAndOpenStrataDoc(activeWorkbenchWindow, selList, lToRDocMap, lToPathMap, docBuff, repoPath, afsOnly, null);
//		StrataView view = (StrataView) activeWorkbenchWindow.getActivePage().findView(StrataView.viewId);
//		CompoundCommand loadCmd = new CompoundCommand("Load Dependencies");
//		StrataRootDoc doc = getStrataDoc(repoPath);
//		if (view != null) {
//	    	// make sure the view is shown
//	        activeWorkbenchWindow.getActivePage().activate(view);
//			doc = view.rootContent;
//			
//			if (!ResourceQueue.isEmpty())
//				view.getRootController().addUnbuiltWarning();
//		}
////		doc = buildStrataDoc(selList, loadCmd, doc, null);
//		loadCmd = ModelUtils.collectAndOpenItems(selList, doc);
//		if (doc!= null && !loadCmd.getCommands().isEmpty())
//			openItems(doc, loadCmd, view, lToRDocMap, lToPathMap, docBuff, repoPath);
//		return doc;
	}


	public static void buildAndOpenStrataDoc(IWorkbenchWindow activeWorkbenchWindow, List<?> selList, Map<IDocument,
			IDocument> lToRDocMap, Map<IDocument, String> lToPathMap, StringBuffer docBuff, IPath repoPath, boolean fromExplCodElem, RunWithObjectParam runWithEditor) {
		StrataView view = (StrataView) activeWorkbenchWindow.getActivePage().findView(StrataView.viewId);
		CompoundCommand loadCmd = new CompoundCommand("Load Dependencies");
		StrataRootDoc doc = getStrataDoc(repoPath);
		if (view != null) {
	    	// make sure the view is shown
	        activeWorkbenchWindow.getActivePage().activate(view);
			doc = view.rootContent;
			
			if (!ResourceQueue.isEmpty())
				view.getRootController().addUnbuiltWarning();
		}
//		doc = buildStrataDoc(selList, loadCmd, doc, null);
		if (fromExplCodElem)
			loadCmd = ModelUtils.collectAndOpenExplCodeItems(selList, doc, null, null);
		else
			loadCmd = ModelUtils.collectAndOpenItems(selList, doc, null, null);
		if (doc!= null && !loadCmd.getCommands().isEmpty())
			openItems(doc, loadCmd, view, lToRDocMap, lToPathMap, docBuff, repoPath, runWithEditor);
	}


	/**
	 * We want to open a Strata document based on the given selection. However,
	 * doing so has three key challenges:<br>
	 * <br>
	 * 1. When the selected item is a top-level node, like projects and source
	 * folders, these items often have the first few folders usually being items
	 * like org and com, whose children therefore include the whole workspace.
	 * So we basically need to iterate through the input and make sure to dive
	 * deeper to the package level.<br>
	 * <br>
	 * 2. Doing the above has the challenge of deciding when to stop diving in.
	 * Returning n packages for a project will mean performing n*n queries even
	 * if a user does not care about some combinations - and this can be a
	 * challenge for extremely large projects. We therefore will return all
	 * packages but queries will only be performed for shown nodes.<br>
	 * <br>
	 * 3. Deal with separate projects com.acme.prj1 and com.acme.prj1.vert2
	 * which might be vert2 building on base package and not contained inside of
	 * it. We therefore want to show on a project-by-project basis, or more
	 * appropriate when we iterate through children to the package level we
	 * should make sure to build the hierarchy.
	 * @param strataRootEditPart 
	 */
//	public static StrataRootDoc buildStrataDoc(List<?> selList, CompoundCommand loadCmd, StrataRootDoc strataDoc, StrataRootEditPart strataRootEditPart) {
		// build a list of rdf resources that we want to show - these still
		// might need to be grouped if both a parent and a child are selected
//		SelectionCollector selColl = new SelectionCollector(strataDoc);
//		selColl.collectMultiType(selList);
//		selColl.removeSingleChildArtFrags();
//		
//		if (selColl.isEmpty()) {
//			logger.error("Not enough dependencies");
//			String msg = "Architexa has encountered a problem while attempting to open this Diagram \n\n" +
//			"Not enough dependencies available to create a diagram \n" +
//			"Consider Rebuilding your Architexa Index \n" +
//			"File -> Architexa -> Rebuild Complete Index";
//			
//			ErrorUtils.openBuildError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), msg);
//			return null;
//		}
//		getLoadCommand(loadCmd, strataDoc, selColl);
//		loadCmd = ModelUtils.collectAndOpenItems(selList, strataDoc);
//		return strataDoc;
//	}
	
	public static StrataRootDoc getStrataDoc() {
		return getStrataDoc(null);
	}
	
	public static StrataRootDoc getStrataDoc(IPath repoPath) {
		ReloRdfRepository repo = null;
		if (repoPath == null)
			repo = StoreUtil.getDefaultStoreRepository();
		else
			repo = StoreUtil.getStoreRepository(repoPath);

		StrataFactory objFactory = new StrataFactory(repo);
		
		PckgDirRepo pdr = PckgDirRepo.getPDR(repo, RJCore.pckgDirContains);
		
		StrataRootDoc strataDoc = new StrataRootDoc(new DepNdx(new RepositoryMgr(repo), pdr), pdr, objFactory);
		strataDoc.getArtifact(strataDoc);
		
    	BrowseModel bm = new ClassStrucBrowseModel();
		strataDoc.setBrowseModel(bm);
		bm.setRootArt(strataDoc);
		bm.setRepo(strataDoc.getRepo());

		return strataDoc;
	}

//	public static void getLoadCommand(CompoundCommand loadCmd, StrataRootDoc strataDoc, SelectionCollector sel) {
//		List<ArtifactFragment> docModuleItems = sel.getArtFragList();
//		
//		loadCmd.add(new AddChildrenCommand(docModuleItems, strataDoc, strataDoc));
//		ClosedContainerDPolicy.showChildren(loadCmd, strataDoc, strataDoc);
//	}

	private static void openItems(final StrataRootDoc strataDoc, final Command cmdToExecOnStart, StrataView view, final Map<IDocument, IDocument> lToRDocMap, final Map<IDocument, String> lToPathMap, final StringBuffer docBuff, final IPath repoPath, final RunWithObjectParam runWithEditor) {
		DbgRes.restartIds();
		//clear children count cache
		try {
			if (view != null)
				view.getRootController().execute(cmdToExecOnStart);
			else {
				new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, false, new IRunnableWithProgress(){

					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Creating Layered Diagram...", IProgressMonitor.UNKNOWN);

						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable(){

							public void run() {
								try {
									IWorkbenchPage page = StrataPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
									StrataEditor editor = null;
									RSEMultiPageEditor mpe = null;
									RSEShareableDiagramEditorInput edInput = null;
									if (lToRDocMap == null || lToPathMap == null)
										edInput = new StrataEditorInput(strataDoc);
									else
										edInput = new StrataEditorInput(strataDoc, lToRDocMap, lToPathMap, docBuff);
									
									// The exploration server uses this to dynamically get the repo for a diagram from a code element
		        					if (repoPath != null)
		        						edInput.wkspcPath = repoPath;
		        					
		        					mpe = (RSEMultiPageEditor) page.openEditor(edInput, RSEMultiPageEditor.editorId);
									editor = (StrataEditor) mpe.getRseEditor();
		        					if (runWithEditor != null) runWithEditor.run(mpe);

		        					editor.getRootController().execute(cmdToExecOnStart);
//									editor.getRootController().refresh();
//									editor.getRootController().refreshHeirarchyInUI(true);
								} catch (PartInitException e) {
									logger.error("Unexpected Exception.", e);
								}
							}
						});
					}
				});
			}
		} catch (InvocationTargetException e) {
			logger.error("Unexpected Error", e);
		} catch (InterruptedException e) {
			logger.error("Unexpected Error", e);
		}
	}

}
