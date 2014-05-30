package com.architexa.diagrams.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.ParseException;
import org.openrdf.rio.Parser;
import org.openrdf.rio.RdfDocumentWriter;
import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.StatementHandlerException;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.ErrorBuildListeners;
import com.architexa.diagrams.editors.RSEMultiPageEditor;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.Entity;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.parts.RSEInjectableCommentEditor;
import com.architexa.diagrams.utils.RSEInjectableOICMenuController;
import com.architexa.diagrams.utils.RecentlyCreatedDiagramUtils;
import com.architexa.external.eclipse.IExportableEditorPart;
import com.architexa.org.eclipse.draw2d.FigureCanvas;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Polyline;
import com.architexa.org.eclipse.draw2d.Viewport;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.ContextMenuProvider;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.GraphicalViewer;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.RootEditPart;
import com.architexa.org.eclipse.gef.editparts.LayerManager;
import com.architexa.org.eclipse.gef.extensions.GraphicalViewManager;
import com.architexa.org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import com.architexa.org.eclipse.gef.palette.ConnectionCreationToolEntry;
import com.architexa.org.eclipse.gef.palette.PaletteEntry;
import com.architexa.org.eclipse.gef.palette.ToolEntry;
import com.architexa.org.eclipse.gef.requests.SimpleFactory;
import com.architexa.org.eclipse.gef.ui.actions.ActionRegistry;
import com.architexa.org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

/**
 * Helpful/common support for all RSE Editors
 * 
 * @author Vineet Sinha
 * @author Elizabeth L. Murnane
 *
 */
public abstract class RSEEditor extends GraphicalEditorWithFlyoutPalette implements IExportableEditorPart, RSEEditorViewCommon.IRSEEditorViewCommon {
	static final Logger logger = Activator.getLogger(RSEEditor.class);
	
	final public static String seqEditorId = "com.architexa.diagrams.chrono.editor";
	final public static String reloEditorId = "com.architexa.diagrams.relo.editor";
	final public static String strataEditorId = "com.architexa.diagrams.strata.editor";
	public static String GENERAL_CONNECTION_LAYER = "User Connection Layer";
	private RSEMultiPageEditor parentEditor;

	private String diagramCreator = "";
	
	public ContextMenuProvider provider = null;
	
	static Set<String> editorIDs = new HashSet<String> (5);
	public static void registerEditorID(String id) {
		editorIDs.add(id);
	}
	public static boolean isEditorID(String id) {
		return editorIDs.contains(id);
	}
	
	public abstract String getEditorId();

	public RSEEditor(){
		StoreUtil.trackDiagram(this);
	}

	@Override
	public ActionRegistry getActionRegistry() {
		return super.getActionRegistry();
	}
	
	public void setName(String partName, boolean isLocalSave) {
    	setPartName(partName);
    	((RSEShareableDiagramEditorInput)getEditorInput()).setLocal(isLocalSave);
    	((RSEShareableDiagramEditorInput)getEditorInput()).setName(partName, isLocalSave);
    	getParentEditor().setShownName(partName);
    }
	
	public abstract ImageDescriptor getImageDescriptor();

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub
		super.selectionChanged(part, selection);
		if (this.equals(part)) {
			updateActions(getSelectionActions());
		}
	}
	
	@Override
	public void commandStackChanged(EventObject event) {
		super.commandStackChanged(event);

		// notify that the editor could now be dirty
		getEditorSite().getWorkbenchWindow().getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				RSEEditor.this.firePropertyChange(IEditorPart.PROP_DIRTY);
			}});
	}

	/**
	 * @return true if this editor has the user-interface focus and false otherwise
	 */
	public boolean hasFocus() {
		return getGraphicalControl().isFocusControl();
	}


	private IFile file = null;


	/* (non-Javadoc)
	 * @see edu.mit.csail.pdeConsole.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	 @Override
	public void doSave(IProgressMonitor monitor) {
		// if we loaded from a local file, try to overwrite it
		RSEShareableDiagramEditorInput fileEditorInput = ((RSEShareableDiagramEditorInput)getEditorInput());
		if (fileEditorInput instanceof IFileEditorInput)
			setFile(((IFileEditorInput)fileEditorInput).getFile());
		
		setFile(RSEEditorViewCommon.doSave(this, monitor, getFile()));
	}
	
	public static GraphicalViewManager graphicalViewManager = new GraphicalViewManager.NullImpl();
	 
	 
	@Override
	protected void createGraphicalViewer(Composite parent) {
		graphicalViewManager.createGraphicalViewer(this, parent);
	}
	
	@Override
	public void configureGraphicalViewer() {
		graphicalViewManager.init(this, this.getGraphicalViewer());
		super.configureGraphicalViewer();
	}
	
	@Override
	public void initializeGraphicalViewer() {
//		Control ctrl = this.getGraphicalViewer().getControl();
		graphicalViewManager.initContextMenu(this, this.getGraphicalViewer());
		graphicalViewManager.initCanvas(this, this.getGraphicalViewer());
	}
	

	public void showLocalSaveDialog() {
		setFile(RSEEditorViewCommon.doSave(this, new NullProgressMonitor(), getFile()));
	}

	public void clearDirtyFlag() {
		getCommandStack().markSaveLocation();
		this.firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	/* (non-Javadoc)
	 * @see edu.mit.csail.pdeConsole.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		IFile curFile = getFile();
		setFile(null);
		doSave(new NullProgressMonitor());

		// if it fails for some reason reset to original
		if (getFile() == null) setFile(curFile);
	}

	/* (non-Javadoc)
	 * @see edu.mit.csail.pdeConsole.part.EditorPart#gotoMarker(org.eclipse.core.resources.IMarker)
	 */
	public void gotoMarker(IMarker marker) {}

	/* (non-Javadoc)
	 * @see edu.mit.csail.pdeConsole.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.mit.csail.pdeConsole.part.EditorPart#isDirty()
	 */
//	public boolean isDirty() {
//		// when no file exists => we don't allow saving, i.e. nothing is dirty
//		if (getFile() == null) return false;
//		return super.isDirty();
//	}

	public boolean isSavedFile() {
		if (getFile() == null)
			return false;
		return true;
	}
	
	public RootEditPart getRootEditPart() {
		return getGraphicalViewer().getRootEditPart();
	}
	
	public void scrollDiagramToTopLeft() {
		FigureCanvas canvas = (FigureCanvas) getGraphicalControl();
		Viewport port = canvas.getViewport();
//		IFigure target = ((GraphicalEditPart)getRootEditPart()).getFigure();
		LayerManager rootEditPart =	(LayerManager) getGraphicalViewer().getEditPartRegistry().get(LayerManager.ID);
		IFigure target = rootEditPart.getLayer(LayerConstants.PRINTABLE_LAYERS);
		Rectangle exposeRegion = target.getBounds().getCopy();
		target = target.getParent();
		while (target != null && target != port) {
			target.translateToParent(exposeRegion);
			target = target.getParent();
		}
		exposeRegion.expand(5, 5);
		Point topLeft = exposeRegion.getTopLeft();
		canvas.scrollSmoothTo(topLeft.x, topLeft.y);
	}

	public Rectangle getDiagramsBoundsForExport() {
		final GraphicalViewer viewer = (GraphicalViewer) getAdapter(GraphicalViewer.class);
		LayerManager rootEditPart =
			(LayerManager) viewer.getEditPartRegistry().get(LayerManager.ID);
		IFigure rootFigure = rootEditPart.getLayer(LayerConstants.PRINTABLE_LAYERS);
		IFigure connLayer = rootEditPart.getLayer(LayerConstants.CONNECTION_LAYER);
		Rectangle bounds = null;
		try {
			RootEditPart root = viewer.getRootEditPart();
			EditPart diagramContents = (EditPart) root.getChildren().get(0);
			List<EditPart> childrenList = new ArrayList<EditPart>();
			childrenList.addAll(diagramContents.getChildren());
			
			// Handle comment children in Strata and Chrono
			if (diagramContents instanceof com.architexa.diagrams.parts.IRSERootEditPart) {
				childrenList.addAll(((com.architexa.diagrams.parts.IRSERootEditPart) diagramContents).getCommentEPChildren());
			}
			for (Iterator<?> iter = childrenList.iterator(); iter.hasNext();) {
				GraphicalEditPart childEP = (GraphicalEditPart) iter.next();
				Rectangle childBounds = getBoundsOfDiagramChild(childEP);
				if (bounds == null)
					bounds = childBounds;
				else
					bounds = bounds.union(childBounds);
			}

			for(Object connection : connLayer.getChildren()){
				if(connection instanceof Polyline)
					if(bounds == null)
						bounds = ((Polyline) connection).getBounds();
					else
						bounds = bounds.union(((Polyline) connection).getBounds());
			}
			bounds.expand(25, 25);
		} catch (Exception e) {
			// NPE, class cast exceptions or some other broken assumption above
			bounds = null;
		}

		if (bounds == null)
			bounds = rootFigure.getBounds();

		return bounds;
	}

	public Rectangle getBoundsOfDiagramChild(GraphicalEditPart childEP) {
		return childEP.getFigure().getBounds().getCopy();
	}

	/**
	 * Override IWorkbenchPart.dispose() so that when a RSEEditor is closed, it is
	 * removed as a listener on the build
	 * 
	 * also saving as a recent diagram
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (isDirty())
			RecentlyCreatedDiagramUtils.saveDiagram(this, null, getRootArt());
		ErrorBuildListeners.remove(this);
		StoreUtil.removeDiagram(this);
		graphicalViewManager.dispose(this);
	}


	public void writeCommentChildren(Resource parentRes, List<Comment> childrenList, RdfDocumentWriter rdfWriter) throws IOException{
		// not being called XXX ??
		// RSEEditorViewCommon.writeCommentChildren(parentRes, childrenList, rdfWriter);
	}

	public void readFile(String partName, InputStream inputStream, final ReloRdfRepository dstRDFRepo) {
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
		} catch (IOException e) {
			logger.error("Problem loading.", e);
		} catch (ParseException e) {
			logger.error("Problem loading.", e);
		} catch (StatementHandlerException e) {
			logger.error("Problem loading.", e);
		}
	}
	public Point getDiagramsOriginalHeight() {
		return null;
	}
	public void returnToOrigLoc(Point oldloc) {
	}

	public void checkError() {	
	}
	/**
	 * Shared support for writing the editor's output - this allows easily
	 * calling the functionality from additional tools - like in Collab
	 */
	public abstract void writeFile(RdfDocumentWriter rdfWriter, Resource diagramRes) throws IOException;

	/**
	 * just get a new file name (and open it for our use)
	 */
	public abstract IFile getNewSaveFile();

	public static PaletteEntry getActorPaletteEntry() {
		String name = "Actor";
		ImageDescriptor img = Activator.getImageDescriptor("icons_palette/palette_actor.png");
		CreationWithParamsFactory factory = new CreationWithParamsFactory(name, img);
		return new CombinedTemplateCreationEntry(
				name, 
				"Press here, then click in the diagram to add an actor",
				factory,
				factory,
				img, 
				img);
	}
	
	public static PaletteEntry getImportImagePaletteEntry() {
		String name = "Import Image";
		ImageDescriptor img = Activator.getImageDescriptor("icons_palette/palette_import.png");
		CreationWithParamsFactory factory = new CreationWithParamsFactory(name, null, true);
		return new CombinedTemplateCreationEntry(
				"Image", 
				"Press here, import an image and then click to place",
				factory,
				factory,
				img, 
				img);
	}
	

	public static PaletteEntry getDatabasePaletteEntry() {
		String name = "Database";
		ImageDescriptor img = Activator.getImageDescriptor("icons_palette/palette_database.png");
		CreationWithParamsFactory factory = new CreationWithParamsFactory(name, img);
		return new CombinedTemplateCreationEntry(
				name, 
				"Press here, then click in the diagram to add a database",
				factory,
				factory,
				img, 
				img);
	}

	public static PaletteEntry getCommentPaletteEntry() {
		ImageDescriptor img = Activator.getImageDescriptor("icons_palette/palette_comment.png");
		return new CombinedTemplateCreationEntry(
				"Comment", 
				"Press here, then click in the diagram to add a comment",
				new SimpleFactory(Comment.class),
				new SimpleFactory(Comment.class),
				img, 
				img);
	}

	public static PaletteEntry getGeneralConnectionPaletteEntry() {
		ImageDescriptor img = Activator.getImageDescriptor("icons_palette/palette_general_conn.PNG");
		ToolEntry ccte = new ConnectionCreationToolEntry(
				"General Connection", 
				"Press here, then click any item in the diagram and then " +
				"click another item in the diagram to draw a connection between them",
				new SimpleFactory(NamedRel.class),
				img, 
				img);
		ccte.setToolClass(DefaultAfterConnectionCreationTool.class);
		return ccte;
	}

	public void setFile(IFile file) {
		this.file = file;
	}
	public IFile getFile() {
		return file;
	}

	public void setParentEditor(RSEMultiPageEditor parentEditor) {
		this.parentEditor = parentEditor;
	}
	
	public RSEMultiPageEditor getParentEditor() {
		return parentEditor;
	}

	static public class CreationWithParamsFactory extends SimpleFactory {

		String entityName;
		ImageDescriptor entityIcon;
		private boolean localImg;

		public CreationWithParamsFactory(String entityName, ImageDescriptor entityIcon) {
			this(entityName, entityIcon, false);
		}
		
		public CreationWithParamsFactory(String entityName, ImageDescriptor entityIcon, boolean localImg) {
			super(null);
			this.localImg = localImg;
			this.entityName = entityName;
			this.entityIcon = entityIcon;
		}

		@Override
		public Object getNewObject() {
			if (localImg) {
				FileDialog fd = new FileDialog(Display.getDefault().getActiveShell());
				fd.setFilterExtensions(new String[] { "*.png;*.gif;*.jpg" });
				final String selectedImg = fd.open();
				entityIcon = new ImageDescriptor() {
					
					@Override
					public ImageData getImageData() {
						return new ImageData(selectedImg);
					}
				};
			}
			try {
				Constructor<Entity> constructor = Entity.class.getDeclaredConstructor(String.class, ImageDescriptor.class);
				constructor.setAccessible(true);
				return constructor.newInstance(entityName, entityIcon);
			} catch (Exception exc) {
				logger.error("Unable to create new instance of " + Entity.class, exc);
				return null;
			}
		}
	}
	
	public boolean isDiff() {
		return false;
	}

	protected ReloRdfRepository memRepo = StoreUtil.getMemRepository();

	public RSEInjectableOICMenuController rseInjectableOICMenuController = new RSEInjectableOICMenuController();

	public RSEInjectableCommentEditor rseInjectableCommentEditor = new RSEInjectableCommentEditor();	

	protected void checkEditorInputForOutsideWorkspace(IEditorInput input) {
		try {
			Object inpVal = null;
			Object inpName = null;
			FileInputStream inpFIS = null;
			inpVal = callObj(input, "org.eclipse.ui.ide.FileStoreEditorInput", "getURI");	// jdtUIVer >= 3.3

			if (inpVal instanceof java.net.URI) {
				inpFIS = new FileInputStream(new File((java.net.URI)inpVal));
				inpName = callObj(input, "org.eclipse.ui.ide.FileStoreEditorInput", "getName");
			}
			
			if (inpFIS == null) {
				inpVal = callObj(input, "org.eclipse.ui.internal.editors.text.JavaFileEditorInput", "getPath");	// jdtUIVer < 3.3
				if (inpVal instanceof Path) {
					inpFIS = new FileInputStream( ((Path)inpVal).toFile() );
					inpName = callObj(input, "org.eclipse.ui.internal.editors.text.JavaFileEditorInput", "getName");
				}
			}
			
			if (inpFIS != null) {
				readFile( (String) inpName, inpFIS, memRepo);
				setupRoot();
			}
		} catch (Exception e) {
			System.err.println("Issue with loading: " + e);
		}
	}
	
	private static Object callObj(IEditorInput obj, String inputTypeName, String inputMethName) throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (!obj.getClass().getName().equals(inputTypeName)) return null;
		Method meth = Class.forName(inputTypeName).getMethod(inputMethName);
		meth.setAccessible(true);
		return meth.invoke(obj);
	}
	
	public abstract boolean canDropOnEditor(IStructuredSelection sel);
	public abstract RootArtifact getRootArt();

	protected void setupRoot() {
		if (getEditorInput() instanceof RSEShareableDiagramEditorInput) {
			IPath path = ((RSEShareableDiagramEditorInput)getEditorInput()).wkspcPath;
			if (path != null) getRootArt().setRepo(StoreUtil.getStoreRepository(path));
		}
	}
	
	public String getDiagramCreator() {
		return diagramCreator;
	}
	public void setDiagramCreator(String diagramCreator) {
		this.diagramCreator = diagramCreator;
	}
	
	
}
