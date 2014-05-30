/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
/*
 * Created on Feb 4, 2004
 *
 */
package com.architexa.diagrams.relo.ui;


import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.editors.RSEMultiPageEditor;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.relo.actions.ListEditorInput;
import com.architexa.diagrams.relo.actions.SelectAllUIAction;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.diagrams.relo.parts.ReloPartFactory;
import com.architexa.diagrams.ui.DefaultAfterConnectionCreationTool;
import com.architexa.diagrams.ui.EditorImageDecorator;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.diagrams.ui.RSECommandStack;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.ui.RSEShareableDiagramEditorInput;
import com.architexa.diagrams.ui.RSETransferDropTargetListener;
import com.architexa.org.eclipse.gef.DefaultEditDomain;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.editparts.ZoomManager;
import com.architexa.org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import com.architexa.org.eclipse.gef.palette.ConnectionCreationToolEntry;
import com.architexa.org.eclipse.gef.palette.PaletteGroup;
import com.architexa.org.eclipse.gef.palette.PaletteRoot;
import com.architexa.org.eclipse.gef.palette.PaletteSeparator;
import com.architexa.org.eclipse.gef.palette.SelectionToolEntry;
import com.architexa.org.eclipse.gef.palette.ToolEntry;
import com.architexa.org.eclipse.gef.requests.SimpleFactory;
import com.architexa.org.eclipse.gef.tools.SelectionTool;
import com.architexa.org.eclipse.gef.ui.actions.ActionRegistry;
import com.architexa.org.eclipse.gef.ui.palette.PaletteViewerProvider;
import com.architexa.org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import com.architexa.rse.BuildStatus;
import com.architexa.store.StoreUtil;


/**
 * @author vineet
 *
 */
public class ReloEditor extends RSEEditor {
	static final Logger logger = ReloPlugin.getLogger(ReloEditor.class);
    
    final public static String editorId = "com.architexa.diagrams.relo.editor";

    static {
    	registerEditorID(editorId);
    }

	public ReloEditor() {
		DefaultEditDomain defaultEditDomain = new DefaultEditDomain(this);
		//defaultEditDomain.setActiveTool(new ConnectionCreationTool());
		defaultEditDomain.setActiveTool(new SelectionTool());
		// Removed QueueableCommandStack: Its functionality is obsolete
		defaultEditDomain.setCommandStack(new RSECommandStack("Relo"));
		setEditDomain(defaultEditDomain);
		BuildStatus.updateDiagramActionMap("Opened Diagram Created By", RSEMultiPageEditor.getDiagramCreator());
		setDiagramCreator(RSEMultiPageEditor.getDiagramCreator());
	}

	@Override
	public String getEditorId() {
		return editorId;
	}
	
	private ReloDoc rootModel = new ReloDoc();
	
	public ReloDoc getRootModel() {
		return rootModel;
	}
	@Override
	public RootArtifact getRootArt() {
		return rootModel;
	}
	
    // BUG-FIX: Overriding base implementation [backported from cvs]
    @Override
    protected PaletteViewerProvider createPaletteViewerProvider() {
        return new PaletteViewerProvider(getEditDomain());
    }


	@Override
	public void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		BrowseModel bm = rootModel.getBrowseModel();
		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer)getGraphicalViewer();
		ReloViewEditorCommon.configureGraphicalViewer(this, viewer, getActionRegistry(), getSite(), bm, null, graphicalViewManager);
		viewer.setEditPartFactory(new ReloPartFactory(bm));
	}

	@Override
	public void initializeGraphicalViewer() {
    	super.initializeGraphicalViewer();
		getGraphicalViewer().setContents(rootModel);
		
		TransferDropTargetListener dragSrc = new RSETransferDropTargetListener(this, getGraphicalViewer());
		getGraphicalViewer().addDropTargetListener(dragSrc);

	}

	@Override
	public boolean canDropOnEditor(IStructuredSelection sel) {
		return ReloViewEditorCommon.canDropOnEditor(sel);
	}
	
	public ReloController getReloController() {
		return (ReloController) this.getGraphicalViewer().getEditPartRegistry().get(this.rootModel);
	}

	IProject defaultProject = null;
	
	@Override
	public IFile getNewSaveFile() {
		return ReloViewEditorCommon.getNewSaveFile(this, defaultProject);
	}

	private IResource getSelectedResource() {
		ISelection sel = getSite().getWorkbenchWindow().getSelectionService().getSelection();
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)sel;
			Object first = ss.getFirstElement();
			if (first instanceof IResource) {
				return (IResource) first;
			} else if (first instanceof IAdaptable) {
				Object res = ((IAdaptable)first).getAdapter(IResource.class);
				if (res != null)
					return (IResource) res;
			}
		}
		
		return null;
	}

	/**
	 * @see edu.mit.csail.pdeConsole.part.EditorPart#setInput(edu.mit.csail.pdeConsole.IEditorInput)
	 */
	@Override
    protected void setInput(IEditorInput input) {
		super.setInput(input);
		
		IResource selRes = getSelectedResource();
		if (selRes != null)
			defaultProject = selRes.getProject();
		
		//TODO Should only receive RSEShareableEditorInput. Remove others
		if (input instanceof ListEditorInput) {
			rootModel.setInputItems(((ListEditorInput) input).list);
			BrowseModel bm = ((ListEditorInput) input).browseModel;
			rootModel.setBrowseModel(bm);
			bm.setRootArt(rootModel);
//			bm.setRepo(rootModel.getRepo());
			super.setupRoot();
			return;
		} else if (input instanceof IFileEditorInput) {
//			setFile(((IFileEditorInput)input).getFile());
//			
//			try {
//				readFile(getFile().getName(), getFile().getContents(/*force*/true), memRepo);
//			} catch (CoreException e) {
//				logger.error("Unexpected Error", e);
//			}
//			
//			RSEShareableDiagramEditorInput sharedInput = LoadUtils.isLocalFileShared(getFile(), memRepo, RSECore.reloFile);
//			if (sharedInput != null) {
//				this.setInput(sharedInput);
//				return;
//			}
//			
//			setupRoot();
		}
		
		if (input instanceof RSEShareableDiagramEditorInput) {
			//readFile(rseInput.getName(), rseInput.getInputStream(), memRepo);
			memRepo = ((RSEShareableDiagramEditorInput) input).getMemRepo();
			setupRoot();
			return;
		}

		checkEditorInputForOutsideWorkspace(input);
	}
	

	@Override
	protected void setupRoot() {
		rootModel.setInputRepo(memRepo);
		
		BrowseModel bm = (BrowseModel) StoreUtil.loadClass(memRepo, RSECore.docRoot, browseModel);
		if (bm == null)
			System.err.println();
		rootModel.setBrowseModel(bm);
		bm.setRootArt(rootModel);
		
		super.setupRoot();
	}
	
    public static final URI browseModel = RSECore.createRseUri("core#browseModel");

	@Override
	public void writeFile(RdfDocumentWriter rdfWriter, Resource diagramRes) throws IOException {
		ReloController rc = (ReloController) getGraphicalViewer().getRootEditPart().getContents();
		diagramRes = ReloViewEditorCommon.writeFile(this, rc, rootModel, browseModel, rootModel.getBrowseModel(), rdfWriter, diagramRes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#createActions()
	 */
	@Override
    protected void createActions() {
		super.createActions();
		ActionRegistry registry = getActionRegistry();
		IAction action;

		// do not need fisheye or rename options in context menu
		
		//action = new FisheyeSemanticZoomInAction((IWorkbenchPart)this);
		//registry.registerAction(action);
		//getSelectionActions().add(action.getId());

		// action = new DirectEditAction((IWorkbenchPart)this);
		// registry.registerAction(action);
		// addSelectionAction(action);
		
		action = new SelectAllUIAction((IWorkbenchPart)this);
		registry.registerAction(action);
		addSelectionAction(action);
	}

    @SuppressWarnings("unchecked")
    private final void addSelectionAction(IAction action) {
        getSelectionActions().add(action.getId());
    }


	@SuppressWarnings("rawtypes")
	@Override
    public Object getAdapter(Class type){
		if (type == ZoomManager.class)
			return getGraphicalViewer().getProperty(ZoomManager.class.toString());
		else if (type == EditDomain.class)
		    return getEditDomain();

		return super.getAdapter(type);
	}

	/**
	 * @see com.architexa.org.eclipse.gef.ui.parts.GraphicalEditorWithPalette#initializePaletteViewer()
	 */
	/*
	protected void initializePaletteViewer() {
		super.initializePaletteViewer();
		//getPaletteViewer().addDragSourceListener(
		//	new TemplateTransferDragSourceListener(getPaletteViewer()));
	}
	*/

	///* (non-Javadoc)
	// * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPalettePreferences()
	// */
	//@Override
	//protected FlyoutPreferences getPalettePreferences() {
	//    return new FlyoutPreferences() {
	//
	//        int dockLocation = -1;
	//        public int getDockLocation() {
	//            return dockLocation;
	//        }
	//        public void setDockLocation(int location) {
	//            dockLocation = location;
	//        }
	//
	//        int paletteState = -1;
	//        public int getPaletteState() {
	//            return paletteState;
	//        }
	//        public void setPaletteState(int state) {
	//            paletteState = state;
	//        }
	//
	//        int paletteWidth = 125;
	//        public int getPaletteWidth() {
	//            return paletteWidth;
	//        }
	//
	//        public void setPaletteWidth(int width) {
	//            paletteWidth = width;                
	//        }};
	//}

	private static PaletteRoot paletteRoot;

	public static ImageDescriptor getImageDescriptor(String key) {
		return ImageDescriptor.createFromFile(ReloEditor.class, key);
	}
	@SuppressWarnings("unused")	// used primarily for debugging
	private final String getObjType(Object obj) {
	    return (obj==null) ? "null" : obj.getClass().toString();
	}
	
	@Override
	public void checkError() {
		try {
			IMarker[] markers = getFile().findMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
			if(markers.length > 0) {
				addErrorDecoration();
			} else {
				removeErrorDecoration();
			}
			Shell shell = getEditorSite().getShell();
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					getReloController().refresh();
				}}); 
		} catch (CoreException e) {
			logger.error("Could not find associated file's error markers. ", e);
		}
	}
	
	/**
	 * Decorates the title image of this editor with an error decoration.
	 * 
	 */
	public void addErrorDecoration() {
		ImageDescriptor editorImage= ImageDescriptor.createFromImage(getTitleImage());
		final ImageDescriptor errorImage = ImageDescriptor.createFromFile(ReloEditor.class, "error_co.gif");
		EditorImageDecorator decorator = new EditorImageDecorator(editorImage, errorImage, EditorImageDecorator.BOTTOM_LEFT);
		final Image decoratedImage = ImageCache.calcImageFromDescriptor(decorator);
		Shell shell = getEditorSite().getShell();
		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				setTitleImage(decoratedImage);
			}});
	}
	
	/**
	 * Removes any error decoration on the title image of this editor
	 *
	 */
	public void removeErrorDecoration() {
		URL url = ReloPlugin.getDefault().getBundle().getEntry("icons/relo-document.png");
		try {
			final Image errorFreeImage = new Image(Display.getDefault(), url.openStream());
			Shell shell = getEditorSite().getShell();
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					setTitleImage(errorFreeImage);
				}});
		} catch (IOException e) {
			logger.error("Could not remove error decoration. ", e);
		}
	}
	
    /* (non-Javadoc)
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithPalette#getPaletteRoot()
	 */
	@Override
    protected PaletteRoot getPaletteRoot() {
		if (paletteRoot == null) {
			paletteRoot = new PaletteRoot();

			PaletteGroup controlGroup = new PaletteGroup("Control Group");

			ToolEntry tool = new SelectionToolEntry();
			controlGroup.add(tool);
			paletteRoot.setDefaultEntry(tool);

			//tool = new MarqueeToolEntry();
			//controlGroup.add(tool);
			
			controlGroup.add(new PaletteSeparator("Entities"));
			
			controlGroup.add(getImportImagePaletteEntry());
			controlGroup.add(getActorPaletteEntry());
			controlGroup.add(getDatabasePaletteEntry());
			controlGroup.add(getCommentPaletteEntry());
			
			controlGroup.add(new PaletteSeparator("Code Components"));

			String packageName = "newPackage";
			String className = "NewClass";
			String methodName = "newMethod()";
			String fieldName = "newField";
			
//			final BrowseModel bm = rootModel.getBrowseModel();

			SimpleFactory factory = new CreationWithParamsFactory(packageName+"$"+className);
			ImageDescriptor img = ReloPlugin.getImageDescriptor("icons_palette/palette_relo_class.PNG");
			controlGroup.add(new CombinedTemplateCreationEntry(
					"Class", 
					"Press here, then click in the diagram to add a class",
					factory,
					factory,
					img, 
					img));

			factory = new CreationWithParamsFactory(packageName+"$"+className+"."+methodName);
			img = ReloPlugin.getImageDescriptor("icons_palette/palette_relo_method.PNG");
			controlGroup.add(new CombinedTemplateCreationEntry(
					"Method", 
					"Press here, then click on a class in the diagram to add a method to it",
					factory,
					factory,
					img, 
					img));

			factory = new CreationWithParamsFactory(packageName+"$"+className+"."+fieldName);
			img = ReloPlugin.getImageDescriptor("icons_palette/palette_relo_field.PNG");
			controlGroup.add(new CombinedTemplateCreationEntry(
					"Field", 
					"Press here, then click on a class in the diagram to add a field to it",
					factory,
					factory,
					img, 
					img));

			controlGroup.add(new PaletteSeparator("Relationships"));

			String relDesc = "Press here, then click any item in the diagram and then " +
			"click another item in the diagram to draw this connection between them";

			img = ReloPlugin.getImageDescriptor("icons_palette/palette_relo_inheritance.PNG");
			ToolEntry inheritanceEntry = new ConnectionCreationToolEntry(
					"Inheritance", 
					relDesc,
					new SimpleFactory(NamedRel.class) {
						@Override
						public Object getNewObject() {
							NamedRel rel = (NamedRel) super.getNewObject();
							BrowseModel bm = rootModel.getBrowseModel();
							bm.setUserCreatedRelTypeToInheritance(rel);
							rel.setUserCreated(true);
							return rel;
						}
					}, 
					img, img);
			inheritanceEntry.setToolClass(DefaultAfterConnectionCreationTool.class);
			controlGroup.add(inheritanceEntry);

			img = ReloPlugin.getImageDescriptor("icons_palette/palette_relo_call.PNG");
			ToolEntry methodCallEntry = new ConnectionCreationToolEntry(
					"Method Call", 
					relDesc,
					new SimpleFactory(NamedRel.class) {
						@Override
						public Object getNewObject() {
							NamedRel rel = (NamedRel) super.getNewObject();
							BrowseModel bm = rootModel.getBrowseModel();
							bm.setUserCreatedRelTypeToCall(rel);
							rel.setUserCreated(true);
							return rel;
						}
					}, 
					img, img);
			methodCallEntry.setToolClass(DefaultAfterConnectionCreationTool.class);
			controlGroup.add(methodCallEntry);

			img = ReloPlugin.getImageDescriptor("icons_palette/palette_relo_overrides.png");
			ToolEntry overrideEntry = new ConnectionCreationToolEntry(
					"Method Override", 
					relDesc,
					new SimpleFactory(NamedRel.class) {
						@Override
						public Object getNewObject() {
							NamedRel rel = (NamedRel) super.getNewObject();
							BrowseModel bm = rootModel.getBrowseModel();
							bm.setUserCreatedRelTypeToOverride(rel);
							rel.setUserCreated(true);
							return rel;
						}
					}, 
					img, img);
			overrideEntry.setToolClass(DefaultAfterConnectionCreationTool.class);
			controlGroup.add(overrideEntry);

			controlGroup.add(getGeneralConnectionPaletteEntry());

			//controlGroup.add(new PaletteSeparator());
//			img = getImageDescriptor("comment.gif");
//			controlGroup.add(new CombinedTemplateCreationEntry(
//                    "Add Package", 
//					"Can be used to add packages to the diagram", 
//                    new SimpleFactory(EmptyCodeUnit.class),
//                    new SimpleFactory(EmptyCodeUnit.class), 
//                    img, 
//                    img));
			
			paletteRoot.add(controlGroup);
		}
		return paletteRoot;
	}

	// Only used by strata
	// TODO make all 'get root' methods the same
	@Deprecated
	public AbstractGraphicalEditPart getRootController() {
		return getReloController();
	}

	//Currently only for Chrono
	public void addRemoveImageExportCustomizations(boolean status) {}

	// TODO combine with SeqEditor.CreationWithParamsFactory and move to RSEEditor
	public class CreationWithParamsFactory extends SimpleFactory {

		String fragName;

		public CreationWithParamsFactory(String fragName) {
			super(null);
			this.fragName = fragName;
		}

		@Override
		public Object getNewObject() {
			BrowseModel bm = rootModel.getBrowseModel();
			try {
				Class<?> type = bm.getUserCreatedFragmentClass();
				Constructor<?> constructor = type.getDeclaredConstructor(BrowseModel.class, String.class);
				constructor.setAccessible(true);
				return constructor.newInstance(bm, fragName);
			} catch (Exception exc) {
				logger.error("Unable to create new instance of " + bm.getUserCreatedFragmentClass(), exc);
				return null;
			}
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ReloPlugin.getImageDescriptor("icons/relo-document.png"); 
	}

}
