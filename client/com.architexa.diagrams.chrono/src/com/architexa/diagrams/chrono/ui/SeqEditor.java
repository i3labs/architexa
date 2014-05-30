package com.architexa.diagrams.chrono.ui;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.EventObject;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.ActionFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.collab.UIUtils;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.editparts.InstanceEditPart;
import com.architexa.diagrams.chrono.editparts.SeqNodeEditPart;
import com.architexa.diagrams.chrono.figures.InstanceFigure;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.editors.RSEMultiPageEditor;
import com.architexa.diagrams.jdt.compat.ASTUtil;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.relo.jdt.browse.ClassStrucBrowseModel;
import com.architexa.diagrams.ui.RSECommandStack;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.ui.RSEShareableDiagramEditorInput;
import com.architexa.diagrams.ui.RSETransferDropTargetListener;
import com.architexa.org.eclipse.draw2d.FigureCanvas;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.DefaultEditDomain;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.GraphicalViewer;
import com.architexa.org.eclipse.gef.KeyHandler;
import com.architexa.org.eclipse.gef.KeyStroke;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.editparts.LayerManager;
import com.architexa.org.eclipse.gef.editparts.ZoomManager;
import com.architexa.org.eclipse.gef.palette.PaletteRoot;
import com.architexa.org.eclipse.gef.print.PrintGraphicalViewerOperation;
import com.architexa.org.eclipse.gef.requests.SimpleFactory;
import com.architexa.org.eclipse.gef.ui.actions.ActionRegistry;
import com.architexa.org.eclipse.gef.ui.actions.PrintAction;
import com.architexa.org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import com.architexa.rse.BuildStatus;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.RepositoryMgr;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqEditor extends RSEEditor {
	private static final Logger logger = SeqPlugin.getLogger(SeqEditor.class);

	final public static String editorId = "com.architexa.diagrams.chrono.editor";

	static {
		registerEditorID(editorId);
	}

	public static String COMMENT_LAYER ="Comment Layer";
	public static String CONDITIONAL_LAYER = "Conditional Layer";
	public static String INSTANCE_PANEL_LAYER = "Instance Panel Layer";
	public static String LIFE_LINE_IN_INSTANCE_PANEL_LAYER = "Life Line In Instance Panel Layer";
	public static String LIFE_LINE_LAYER = "Life Line Layer";


	private KeyHandler sharedKeyHandler;
	private List<?> selections;

	public SeqEditor() {
		DefaultEditDomain defaultEditDomain = new DefaultEditDomain(this);
		defaultEditDomain.setCommandStack(new RSECommandStack("Chrono"));
		setEditDomain(defaultEditDomain);
//		setEditDomain(new DefaultEditDomain(this));
		sharedKeyHandler = null;
		setDiagramCreator(RSEMultiPageEditor.getDiagramCreator());
		BuildStatus.updateDiagramActionMap("Opened Diagram Created By", RSEMultiPageEditor.getDiagramCreator());
	}

	
	@Override
	public String getEditorId() {
		return editorId;
	}
	
	private DiagramModel rootModel = new DiagramModel();

	@Override
	public RootArtifact getRootArt() {
		return rootModel;
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public Object getAdapter(Class type){
		if (type == ZoomManager.class)
			return getGraphicalViewer().getProperty(ZoomManager.class.toString());
		else if (type == EditDomain.class)
		    return getEditDomain();

		return super.getAdapter(type);
	}
	
	@Override
	public void initializeGraphicalViewer() {	
		super.initializeGraphicalViewer();
		getGraphicalViewer().setContents(rootModel);

		RSETransferDropTargetListener dragListener = new RSETransferDropTargetListener(this, getGraphicalViewer());
		getGraphicalViewer().addDropTargetListener(dragListener);

		// for debugging: 
		getGraphicalViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection sel = event.getSelection();
				if (sel instanceof IStructuredSelection) {
					selections = ((IStructuredSelection)sel).toList();
					for (Object item : ((IStructuredSelection)sel).toList()) {
						if (item instanceof AbstractGraphicalEditPart) {
							logger.info("Selection " + item.toString() + "(" + ((AbstractGraphicalEditPart)item).getFigure() + "), pos: " + ((AbstractGraphicalEditPart)item).getFigure().getBounds());
						} else
							logger.info("selItem: " + item.getClass());
					}
				} else {
					logger.info(sel.getClass());
				}
			}});
	}

	@Override
	public boolean canDropOnEditor(IStructuredSelection sel) {
		return SeqViewEditorCommon.canDropOnEditor(sel);
	}


	@Override
	public void configureGraphicalViewer() {
		super.configureGraphicalViewer();

		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer)getGraphicalViewer();
		SeqViewEditorCommon.configureGraphicalViewer(this, viewer, getActionRegistry(), getCommonKeyHandler(), rootModel);
	}

	@Override
	protected void createActions() {
		super.createActions();

		ActionRegistry registry = getActionRegistry();
		registry.registerAction(new PrintAction(this) {
			@Override
			public void run() {
				GraphicalViewer viewer = (GraphicalViewer)getWorkbenchPart().getAdapter(GraphicalViewer.class);
				PrintDialog dialog = new PrintDialog(viewer.getControl().getShell(), SWT.NULL);
				PrinterData data = dialog.open();
				if (data != null) {
					SeqPrintGraphicalViewerOperation op =  new SeqPrintGraphicalViewerOperation(new Printer(data), viewer);
					op.run(getWorkbenchPart().getTitle());
				}
			}
		});
	}

	public class SeqPrintGraphicalViewerOperation extends PrintGraphicalViewerOperation {
		private GraphicalViewer viewer;
		public SeqPrintGraphicalViewerOperation(Printer p, GraphicalViewer g) {
			super(p,g);
			viewer = g;
			LayerManager lm = (LayerManager)viewer.getEditPartRegistry().get(LayerManager.ID);
			IFigure fullDiagram = lm.getLayer(LayerConstants.PRINTABLE_LAYERS);
			Rectangle instancePane=(rootModel.getInstancePanel()).getBounds();
			fullDiagram.getBounds().y=instancePane.y;
			fullDiagram.getBounds().height-=instancePane.y;
			setPrintSource(fullDiagram);
		}
	}

	private static PaletteRoot paletteRoot = null;
	
	@Override
	protected PaletteRoot getPaletteRoot() {
		if (paletteRoot == null)
			paletteRoot = SeqEditorPaletteFactory.createPalette(this);
		return paletteRoot;
	}

	@Override
	public void commandStackChanged(EventObject event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		super.commandStackChanged(event);
	}

	public DiagramModel getModel() {
		return rootModel;
	}

	public DiagramEditPart getDiagramController() {
		return (DiagramEditPart) getGraphicalViewer().getEditPartRegistry().get(rootModel);
	}

	private IAction deleteAction = new Action() {
		@Override
		public void run() {
			for(Object obj : selections) {
				if(!(obj instanceof SeqNodeEditPart)) continue;
				//	SeqNodeEditPart part = (SeqNodeEditPart) obj;
				//	part.delete();
			}
			getActionRegistry().getAction(ActionFactory.DELETE.getId()).run();
		}
	};

	private KeyHandler getCommonKeyHandler() {
		if (sharedKeyHandler == null) {
			sharedKeyHandler = new KeyHandler();
			sharedKeyHandler.put(
					KeyStroke.getPressed(SWT.DEL, 127, 0), deleteAction);
			sharedKeyHandler.put(
					KeyStroke.getPressed(26, SWT.CTRL),
					getActionRegistry().getAction(ActionFactory.UNDO.getId()));
			sharedKeyHandler.put(
					KeyStroke.getPressed(25, SWT.CTRL),
					getActionRegistry().getAction(ActionFactory.REDO.getId()));
		}

		return sharedKeyHandler;
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		rootModel.setRepo(StoreUtil.getDefaultStoreRepository());

		//TODO Should only receive RSEShareableEditorInput. Remove others
		if (input instanceof IFileEditorInput) {
//			setFile(((IFileEditorInput)input).getFile());
//			ReloRdfRepository memRepo = StoreUtil.getMemRepository();
//
//			try {
//				readFile(getFile().getName(), getFile().getContents(/*force*/true), memRepo);
//			} catch (CoreException e) {
//				logger.error("Problem loading. ", e);
//			}
//
//			RSEShareableDiagramEditorInput sharedInput = LoadUtils.isLocalFileShared(getFile(), memRepo, RSECore.chronoFile);
//			if (sharedInput != null) {
//				this.setInput(sharedInput);
//				return;
//			}
//			setupRoot(memRepo);
		} else if (input instanceof RSEShareableDiagramEditorInput) { 
			RSEShareableDiagramEditorInput rseInput = (RSEShareableDiagramEditorInput)input;

			//ReloRdfRepository memRepo = StoreUtil.getMemRepository();
			ReloRdfRepository memRepo = rseInput.getMemRepo();

			//readFile(rseInput.getName(), rseInput.getInputStream(), memRepo);
			setupRoot(memRepo, rseInput);
		} else {
			checkEditorInputForOutsideWorkspace(input);
		}
	}

	public static final URI browseModel = RSECore.createRseUri("core#browseModel");

	private BrowseModel bm = null;

	@Override
	protected void setupRoot() {
		setupRoot(memRepo, null);

		super.setupRoot();
	}
	
	protected void setupRoot(ReloRdfRepository memRepo, RSEShareableDiagramEditorInput rseInput) {

		if (bm == null) {
			bm = new ClassStrucBrowseModel() {
				@Override
				public boolean artifactLoadable(Artifact art, Resource artType) {
					if(CodeUnit.isPackage(getRepo(), artType)) return false;
					return super.artifactLoadable(art, artType);
				}
			};
			rootModel.setBrowseModel(bm);
			bm.setRootArt(rootModel);
		}
		Resource diagramRes = memRepo.getStatement((Resource)null, memRepo.rdfType, RSECore.chronoFile).getSubject();
		if (diagramRes == null && rseInput != null && rseInput.isSavedFile()) {
			//TODO check this?
			logger.error("Diagram Resource not found.");
			UIUtils.errorPromptDialog("Error Opening Diagram", "Could not open the saved diagram");
		}
		((RepositoryMgr)rootModel.getRepo()).setFileRepo(memRepo);
		rootModel.setSavedDiagramResource(diagramRes);
	
	}
	
	@Override
	public IFile getNewSaveFile() {
		return SeqViewEditorCommon.getNewSaveFile(this);
	}

	@Override
	public void writeFile(RdfDocumentWriter rdfWriter, Resource fileRes) throws IOException {
		rootModel = SeqViewEditorCommon.writeFile(this, rootModel, rdfWriter, fileRes);
	}


	@Override
	public void dispose() {
		super.dispose();
		ASTUtil.emptyCache();
	}

	@Override
	public Rectangle getDiagramsBoundsForExport() {
		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer)getGraphicalViewer();
		viewer.reveal(getRootEditPart());
		return super.getDiagramsBoundsForExport();
	}			

	@Override
	public Point getDiagramsOriginalHeight() {
		Rectangle instancePane=(rootModel.getInstancePanel()).getBounds();
		return new Point(0, instancePane.y);
	}			
	
	@Override
	public void returnToOrigLoc(Point oldLoc) {
		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer)getGraphicalViewer();
		((FigureCanvas) viewer.getControl()).scrollSmoothTo(oldLoc.x, oldLoc.y);
	}
	
	@Override
	public Rectangle getBoundsOfDiagramChild(GraphicalEditPart childEP) {
		if(childEP instanceof InstanceEditPart) 
			return super.getBoundsOfDiagramChild(childEP).union(
					((InstanceFigure)childEP.getFigure()).getChildrenContainer().getBounds().getCopy());
		return super.getBoundsOfDiagramChild(childEP);
	}
	// Only used by strata
	// TODO make all 'get root' methods the same
	@Deprecated
	public AbstractGraphicalEditPart getRootController() {
		return getDiagramController();
	}

	// Used to make the connection labels show full name before exporting the diagram
	public void addRemoveImageExportCustomizations(boolean status) {
		MemberUtil.updateFullOrAbbrvConnectionLabels(getDiagramController(), status);
	}

	// TODO combine with ReloEditor.CreationWithParamsFactory and move to RSEEditor
	public CreationWithParamsFactory getCreationFactory(Class aClass) {
		return new CreationWithParamsFactory(aClass);
	}

	class CreationWithParamsFactory extends SimpleFactory {

		public CreationWithParamsFactory(Class<?> aClass) {
			super(aClass);
		}

		@Override
		public Object getNewObject() {
			try {
				Constructor<?> constructor = ((Class<?>)getObjectType()).getDeclaredConstructor(DiagramModel.class);
				constructor.setAccessible(true);
				return constructor.newInstance(rootModel);
			} catch (Exception exc) {
				logger.error("Unable to create new instance of " + getObjectType(), exc);
				return null;
			}
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return SeqPlugin.getImageDescriptor("icons/chrono-document.png");
	}
}
