package com.architexa.diagrams.editors;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.ParseException;
import org.openrdf.rio.Parser;
import org.openrdf.rio.RdfDocumentWriter;
import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.StatementHandlerException;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.diagrams.ui.PluggableDiagramsSupport;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.ui.RSEEditorViewCommon.IRSEEditorViewCommon;
import com.architexa.diagrams.ui.RSEShareableDiagramEditorInput;
import com.architexa.diagrams.utils.LoadUtils;
import com.architexa.diagrams.utils.RecentlyCreatedDiagramUtils;
import com.architexa.external.eclipse.IExportableEditorPart;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.commands.CommandStackListener;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

/**
 * Implementation of RSE multi-page editor.
 * This currently has 2 pages:
 * <ul>
 * <li>page 0 contains a nested RSE editor.
 * <li>page 1 shows you the diff if a revision diagram is opened. Otherwise this page is not created.
 * </ul>
 */
public class RSEMultiPageEditor extends MultiPageEditorPart implements IResourceChangeListener, ISelectionListener, CommandStackListener, IExportableEditorPart, IRSEEditorViewCommon{

	
	final public static String editorId = "com.architexa.diagrams.editors.RSEMultiPageEditor";
	public static Logger logger = Activator.getLogger(RSEMultiPageEditor.class);
	private CompareEditor compEditor;
	private RSEEditor rseEditor;
	
//	private String lDoc = "";
//	private String rDoc = "";
	List<IDocument> lDocList;
	List<IDocument> rDocList;
	private boolean diff = false;
	
	public RSEMultiPageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		StoreUtil.trackDiagram(this);
	}
	
	void createPage1() {
		try {
			RSEShareableDiagramEditorInput RSEinp= (RSEShareableDiagramEditorInput) getEditorInput();
			if (RSEinp.getlToRDocMap() == null || RSEinp.getlToRDocMap().isEmpty())
				return;
			compEditor = new CompareEditor(){
				@Override
				public IEditorSite getEditorSite() {
					return RSEMultiPageEditor.this.getEditorSite();
				};
			};
			int index = addPage(compEditor, new CompareInput());
			setPageText(index, "Diff");
			setDiff(true);
		} catch (PartInitException e) {
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating nested text editor",
				null,
				e.getStatus());
		}
	}

	void createPage0() {
		String type = ((RSEShareableDiagramEditorInput)getEditorInput()).getDiagramType();
		for (IRSEDiagramEngine engine : PluggableDiagramsSupport.getRegisteredDiagramEngines()) {
			if (engine.editorId().equals(type))
				try {
					this.rseEditor = engine.getEditorClass().newInstance();
					// if loading a local file: set file so future saves will not require a new file
					if (getEditorInput() instanceof IFileEditorInput) {
						IFile file = ((IFileEditorInput) getEditorInput()).getFile();
						if (file != null)
							getRseEditor().setFile(file);
					}
					if (getEditorInput() instanceof RSEShareableDiagramEditorInput) {
						IFile file = ((RSEShareableDiagramEditorInput) getEditorInput()).getFile();
						if (file != null)
							getRseEditor().setFile(file);
					}
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
		}
		int index = 0;
		try {
			index = addPage((IEditorPart) getRseEditor(), getEditorInput());
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		setPageText(index, "Diagram");
		setPartName(((RSEShareableDiagramEditorInput)rseEditor.getEditorInput()).getName());
		setTitleImage(ImageCache.calcImageFromDescriptor(getImageDescriptor()));
		rseEditor.setParentEditor(this);
//		RSEEditor.setParentEditor(this);
	}
	
	public void setShownName(String name){
		setPartName(((RSEShareableDiagramEditorInput)rseEditor.getEditorInput()).getName());
	}
	
	private ImageDescriptor getImageDescriptor() {
		ImageDescriptor desc = rseEditor.getEditorInput().getImageDescriptor();
		if (desc == null)
			desc = rseEditor.getImageDescriptor();
		return  desc;
	}

	/**
	 * Creates the pages of the multi-page editor.
	 */
	@Override
	protected void createPages() {
		createPage0();
		createPage1();
		
		// Do not create second page if no diff
		if (getPageCount() == 1) {
	        Composite container = getContainer();
	        if (container instanceof CTabFolder) {
	            ((CTabFolder) container).setTabHeight(0);
	        }
	    }
	}
	
	/**
	 * The <code>MultiPageEditorPart</code> implementation of this 
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		if (rseEditor != null)
			rseEditor.dispose();
		if (compEditor != null)
			compEditor.dispose();
		
		// This was causing the editor to be disposed twice resulting in
		// multiple recently closed diagrams being saved
		// super.dispose();
		StoreUtil.removeDiagram(this);
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
	}
	
	/**
	 * Saves the multi-page editor's document.
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}
	
	@Override
	public boolean isSaveOnCloseNeeded() {
		if (((RSEEditor)getEditor(0)).isSavedFile())
			return isDirty();
		else
			return false;
	}

	@Override
	public boolean isDirty() {
		return getEditor(0).isDirty();
	}
	/**
	 * Saves the multi-page editor's document as another file.
	 */
	@Override
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
	}
	/* (non-Javadoc)
	 * Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		if (editorInput instanceof IFileEditorInput) { // file saved in workspace
			IFile file = ((IFileEditorInput)editorInput).getFile();
			ReloRdfRepository memRepo = StoreUtil.getMemRepository();

			try {
				readFile(file.getName(), file.getContents(/*force*/true), memRepo);
			} catch (CoreException e) {
				logger.error("Problem loading. ", e);
			}

			RSEShareableDiagramEditorInput sharedInput = LoadUtils.isLocalFileShared(file, memRepo, getFileURI(file.getName(), memRepo), getEditorType(file.getName(), memRepo));
			if (sharedInput != null)
				editorInput = sharedInput;
			else {
				try {
					editorInput = new RSEShareableDiagramEditorInput(file.getName(), getEditorType(file.getName(), memRepo), memRepo, true);
				} catch (Throwable t) {
					logger.error("Error creating editor input from saved file. ", t);
				}
			}
			if (editorInput instanceof RSEShareableDiagramEditorInput)
				((RSEShareableDiagramEditorInput) editorInput).setFile(file);
			
		} else if (editorInput instanceof FileStoreEditorInput) { // diagram from file outside workspace
			ReloRdfRepository memRepo = StoreUtil.getMemRepository();
			java.net.URI uri = ((FileStoreEditorInput)editorInput).getURI();
			File file = new File(uri);
			try {
				InputStream inputStream =  new FileInputStream(file );
				
				readFile(uri.toString(), inputStream, memRepo);
			} catch (Throwable e) {
				logger.error("Problem loading. ", e);
			}
			String fileName ="";
			if (file.getName().contains("NAME~"))
				fileName  = RecentlyCreatedDiagramUtils.getDisplayName(file.getName());
			else
				fileName = file.getName();
			
			try {
				editorInput = new RSEShareableDiagramEditorInput(fileName, getEditorType(file.getName(), memRepo), memRepo, true);
			} catch (Throwable t) {
				logger.error("Error creating editor input from saved file. ", t);
			}
		} else if (editorInput instanceof RSEShareableDiagramEditorInput) { // diagram from my.architexa/codemaps 
			RSEShareableDiagramEditorInput inp = (RSEShareableDiagramEditorInput) editorInput;
			ReloRdfRepository memRepo = StoreUtil.getMemRepository();
			readFile(editorInput.getName(), inp.getInputStream(), memRepo);
			inp.setMemRepo(memRepo);
		} else
			throw new PartInitException("Invalid Input: Must be RSEShareableDiagramEditorInput\n" + editorInput);
		
		super.init(site, editorInput);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
	}
	
	
	// TODO combine this with getFileURI
	public static String getEditorType(String name, ReloRdfRepository memRepo) {
		String ext = name.substring(name.lastIndexOf("."));
		if (ext.equalsIgnoreCase(".chrono"))
			return RSEEditor.seqEditorId;
		if (ext.equalsIgnoreCase(".relo"))
			return RSEEditor.reloEditorId;
		if (ext.equalsIgnoreCase(".strata"))
			return RSEEditor.strataEditorId;
		if (ext.equalsIgnoreCase(".atxa")) {
			if (memRepo.contains(null, memRepo.rdfType, RSECore.chronoFile))
				return RSEEditor.seqEditorId;
			if (memRepo.contains(null, memRepo.rdfType, RSECore.reloFile))
				return RSEEditor.reloEditorId;
			if (memRepo.contains(null, memRepo.rdfType, RSECore.strataFile))
				return RSEEditor.strataEditorId;
		}
//		throw new IllegalArgumentException("Unsupported File format:" + name);
		return "";
	}
	
	private URI getFileURI(String name, ReloRdfRepository memRepo) {
		String ext = name.substring(name.lastIndexOf("."));
		if (ext.equalsIgnoreCase(".chrono"))
			return RSECore.chronoFile;
		if (ext.equalsIgnoreCase(".relo"))
			return RSECore.reloFile;
		if (ext.equalsIgnoreCase(".strata"))
			return RSECore.strataFile;
		if (ext.equalsIgnoreCase(".atxa")) {
			if (memRepo.contains(null, memRepo.rdfType, RSECore.chronoFile))
				return RSECore.chronoFile;
			if (memRepo.contains(null, memRepo.rdfType, RSECore.reloFile))
				return RSECore.reloFile;
			if (memRepo.contains(null, memRepo.rdfType, RSECore.strataFile))
				return RSECore.strataFile;
		}
		throw new IllegalArgumentException("Unsupported File format:" + name);
	}

	private static String diagramCreator = "";
	
	public void readFile(String partName, InputStream inputStream, final ReloRdfRepository dstRDFRepo) {
		if (inputStream == null || dstRDFRepo == null) return;
		setPartName(partName);
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			Parser parser = StoreUtil.getRDFParser(dstRDFRepo);
			parser.setStatementHandler(new StatementHandler() {
				public void handleStatement(Resource subj, URI pred, Value obj) throws StatementHandlerException {
					dstRDFRepo.addStatement(subj, pred, obj);
				}});
			dstRDFRepo.startTransaction();
			parser.parse(in, ReloRdfRepository.atxaRdfNamespace);
			dstRDFRepo.commitTransaction();
			in.close();
			inputStream.close();
		} catch (IOException e) {
			logger.error("Problem loading.", e);
		} catch (ParseException e) {
			logger.error("Problem loading.", e);
		} catch (StatementHandlerException e) {
			logger.error("Problem loading.", e);
		}
		
		Statement creatorStmt = dstRDFRepo.getStatement((Resource)null, RSECore.rdfCreator, StoreUtil.createMemLiteral("true"));
		Resource creator = creatorStmt.getSubject();
		if (creator!=null) {
			String creatorEmail = creator.toString();
			setDiagramCreator(creatorEmail.substring(creatorEmail.lastIndexOf("/")+1));
			System.err.println(getDiagramCreator());
		} else {
			setDiagramCreator("");
		}
			
	}
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}
	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i<pages.length; i++){
						if (((FileEditorInput)compEditor.getEditorInput()).getFile().getProject().equals(event.getResource())){
							IEditorPart editorPart = pages[i].findEditor(compEditor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}            
			});
		}
	}

	public RSEEditor getRseEditor() {
		return rseEditor;
	}
	
	
	class CompareInput extends CompareEditorInput {
		public CompareInput() {
			super(new CompareConfiguration());
		}

		@Override
		protected Object prepareInput(IProgressMonitor pm) {
			return diffTree();
		}
	}
	
	Map<IDocument, IDocument> lToRDocmap = new LinkedHashMap<IDocument, IDocument>();
	Map<IDocument, String> docToFullPathMap = new LinkedHashMap<IDocument, String>();
	Map<String, IDocument> pathToDocMap = new LinkedHashMap<String, IDocument>();
	public Object diffTree() {
		reInitializeMaps();
		lToRDocmap = ((RSEShareableDiagramEditorInput)getEditorInput()).getlToRDocMap();
		docToFullPathMap = ((RSEShareableDiagramEditorInput)getEditorInput()).getlToPathMap();
		DiffNode parent = new DiffNode(null, Differencer.CHANGE, null, new CompareItem("Folder", "Parent Folder", ITypedElement.FOLDER_TYPE), null);
		List<String> pathList = new ArrayList<String>();
		
		for (IDocument doc : docToFullPathMap.keySet()) {
			String path = docToFullPathMap.get(doc);
			pathList.add(path);
			pathToDocMap.put(path.substring(path.lastIndexOf("/") + 1), doc);
		}
			
		createTree(parent, pathList, true);
		return parent;
	}
	
	private void reInitializeMaps() {
		lToRDocmap.clear();
		docToFullPathMap.clear();
		pathToDocMap.clear();
	}
	
	private void createTree(DiffNode parent, List<String> pathList, boolean isParentCall) {
		// add leaf node and return
		if (pathList.size() == 1) {
			String path = pathList.get(0);
			IDocument doc;
			
			// leaf node could be util/SeqUtil.java
			if (path.contains("/"))
				doc = pathToDocMap.get(path.substring(path.lastIndexOf("/") + 1));
			else	
				doc = pathToDocMap.get(path);
			IDocument remDoc = lToRDocmap.get(doc);
			CompareItem left = new CompareItem(path, doc.get());
			CompareItem right = new CompareItem(null, remDoc.get()); 
			new DiffNode(parent, Differencer.CHANGE, null, left, right);
			return;
		}
		
		List<String> delList = new ArrayList<String>(pathList);
		List<String> travList;
		while (!delList.isEmpty()) {
			List<String> paramList = new ArrayList<String>();
			String currPath = delList.get(0);
			if (!currPath.contains("/")) {
				paramList.add(currPath);
				createTree(parent, paramList, false);
				delList.remove(currPath);
				continue;
			}
			String[] spCurPath = currPath.split("/");
			travList = new ArrayList<String>(delList);
			String curRoot = "";
			for (String path : travList) {
				if (path.equals(currPath)) continue;
				// find first level of mismatch, 
				String tempStr = "";
				String[] spPath = path.split("/");
				for (int i = 0 ; i < Math.min(spCurPath.length, spPath.length); i++ ) {
					if (spCurPath[0].equals("commands"))
						"".toCharArray();
					if (!spCurPath[i].equals(spPath[i])) 
						break;

					tempStr = tempStr + spCurPath[i] + "/";
				}
				
				if (curRoot.length() == 0)
					curRoot = tempStr;
				else if (tempStr.length() != 0)
					curRoot = curRoot.length() > tempStr.length() ? tempStr : curRoot;
			}
			
			for (String path : travList) {
				if (path.equals(currPath)) continue;
				
				if (curRoot.length() != 0 && path.contains(curRoot)) {
					// create a list for all children paths from there
					paramList.add(path.substring(curRoot.length()));
					// delete those from the delList
					delList.remove(path);
				}
			}

			// create a list for all children paths from there
			paramList.add(currPath.substring(curRoot.length()));
			// delete those from the delList
			delList.remove(currPath);
			
			// add to treeMap / create parent
			DiffNode newParent;
			String parentName = currPath;
			
			if (curRoot.length() != 0)
				parentName = currPath.substring(0, curRoot.length() - 1);
			
			// e.g "a/b/c/d/" we want to get "d" as name
			if (isParentCall)
				parentName = parentName.substring(parentName.lastIndexOf("/") + 1);
			
			//If only one in list dont create new parent
			if (paramList.size() != 1)
				newParent = new DiffNode(parent, Differencer.CHANGE, null, new CompareItem(parentName, "" , ITypedElement.FOLDER_TYPE), null);
			else
				newParent = parent;
			
			// call this method with the new node and list
			createTree(newParent, paramList, false);
		}
	}
	
	
	//***********
	class CompareItem implements IStructureComparator, IStreamContentAccessor, ITypedElement,
			IModificationDate {
		private String contents, name;
		private long time;
		private String type = "java";

		CompareItem(String name, String contents) {
			this.name = name;
			this.contents = contents;
		}
		
		CompareItem(String name, String contents, long time) {
			this.name = name;
			this.contents = contents;
			this.time = time;
		}
		
		CompareItem(String name, String contents, String type) {
			this.name = name;
			this.contents = contents;
			this.type = type;
		}

		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(contents.getBytes());
		}

		public Image getImage() {
			if (getType().equals(ITypedElement.FOLDER_TYPE))
				return ImageCache.calcImageFromDescriptor(Activator.getImageDescriptor("/icons/folder.gif"));
			return ImageCache.calcImageFromDescriptor(Activator.getImageDescriptor("/icons/java_file.gif"));
		}

		public long getModificationDate() {
			return time;
		}

		public String getName() {
			return name;
		}

		public String getString() {
			return contents;
		}

		public String getType() {
			return type;
		}

		public Object[] getChildren() {
			return null;
		}

	}


	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (this.equals(getSite().getPage().getActiveEditor())) {
			if (rseEditor.equals(getActiveEditor()))
				rseEditor.selectionChanged(getActiveEditor(), selection);
		}

	}

	public void commandStackChanged(EventObject event) {
		if (this.equals(getSite().getPage().getActiveEditor())) {
			if (rseEditor.equals(getActiveEditor()))
				rseEditor.commandStackChanged(event);
		}
	}

	public Rectangle getDiagramsBoundsForExport() {
		return rseEditor.getDiagramsBoundsForExport();
	}

	public void returnToOrigLoc(Point oldLoc) {
		rseEditor.returnToOrigLoc(oldLoc);
	}

	public Point getDiagramsOriginalHeight() {
		return rseEditor.getDiagramsOriginalHeight();
	}

	public void addRemoveImageExportCustomizations(boolean status) {
		rseEditor.addRemoveImageExportCustomizations(status);
	}

	public void setDiff(boolean diff) {
		this.diff = diff;
	}

	public boolean isDiff() {
		return diff;
	}

	public IFile getNewSaveFile() {
		return getRseEditor().getNewSaveFile();
	}

	public void writeFile(RdfDocumentWriter rdfWriter, Resource fileRes) throws IOException {
		getRseEditor().writeFile(rdfWriter, fileRes);
	}

	public void setName(String partName, boolean isLocalSave) {
		getRseEditor().setName(partName, isLocalSave);		
	}

	public void clearDirtyFlag() {
		getRseEditor().clearDirtyFlag();
	}

	public void checkError() {
		getRseEditor().checkError();		
	}

	public AbstractGraphicalEditPart getRootController() {
		return getRseEditor().getRootController();
	}

	public void showLocalSaveDialog() {
		getRseEditor().showLocalSaveDialog();		
	}

	public boolean canDropOnEditor(IStructuredSelection sel) {
		return getRseEditor().canDropOnEditor(sel);		
	}

	public static String getDiagramCreator() {
		return diagramCreator;
	}

	public static void setDiagramCreator(String _diagramCreator) {
		diagramCreator = _diagramCreator;
	}
}
