package com.architexa.diagrams.relo.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.WorkbenchPart;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.relo.actions.ReloContextMenuProvider;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.ui.RSEEditorViewCommon;
import com.architexa.diagrams.ui.RSESaveAction;
import com.architexa.diagrams.utils.LoadUtils;
import com.architexa.diagrams.utils.NamespaceDeclaratorWriter;
import com.architexa.diagrams.utils.OpenItemUtils;
import com.architexa.org.eclipse.draw2d.FreeformLayer;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LayeredPane;
import com.architexa.org.eclipse.draw2d.ScalableFreeformLayeredPane;
import com.architexa.org.eclipse.gef.KeyHandler;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import com.architexa.org.eclipse.gef.editparts.ZoomManager;
import com.architexa.org.eclipse.gef.extensions.GraphicalViewManager;
import com.architexa.org.eclipse.gef.extensions.GraphicalViewManager.NullImpl;
import com.architexa.org.eclipse.gef.ui.actions.ActionRegistry;
import com.architexa.org.eclipse.gef.ui.actions.ZoomInAction;
import com.architexa.org.eclipse.gef.ui.actions.ZoomOutAction;
import com.architexa.org.eclipse.gef.ui.parts.GraphicalEditor;
import com.architexa.org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import com.architexa.store.StoreUtil;



/**
 * Static class to essentially provide shared code for ReloView and ReloEditor
 * 
 * @author vineet
 */
public class ReloViewEditorCommon extends RSEEditorViewCommon {

	@SuppressWarnings("deprecation")	// non-deprecated method are only available for Eclipse 3.2 +
	public static void configureGraphicalViewer(
			IWorkbenchPart wbPart, 
			ScrollingGraphicalViewer viewer, 
			ActionRegistry actionReg,
			IWorkbenchPartSite site, 
			BrowseModel bm, 
			KeyHandler keyHandler, final GraphicalViewManager graphicalViewManager) {
		ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart() {
			@Override
			protected ScalableFreeformLayeredPane createScaledLayers() {
				// overriding to set a map mode
				ScalableFreeformLayeredPane layers = new ScalableFreeformLayeredPane();
				addGeneralConnectionLayer(getPrintableLayers());
				layers.add(createGridLayer(), GRID_LAYER);
				layers.add(getPrintableLayers(), PRINTABLE_LAYERS);
				FreeformLayer sfl = new FreeformLayer();
				sfl.setEnabled(false);
				layers.add(sfl, SCALED_FEEDBACK_LAYER);
				return layers;
			}
            @Override
			public void createLayers(LayeredPane layeredPane) {
                super.createLayers(layeredPane);
                // enabled because needed by: ArtifactNavAidsEditPolicy
                getLayer(LayerConstants.SCALED_FEEDBACK_LAYER).setEnabled(true);
            }
            @Override
			public IFigure createFigure() {
            	if (!(graphicalViewManager instanceof NullImpl) && graphicalViewManager!=null )
        			return graphicalViewManager.createGEFScaleableFigure(this);
            	return super.createFigure();
            }
            
        };
        

	
		
		
		//ScalableFreeformRootEditPart root = new ScalableRootEditPart();

		List<String> zoomLevels = new ArrayList<String> (3);
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		root.getZoomManager().setZoomLevelContributions(zoomLevels);

		IAction zoomIn = new ZoomInAction(root.getZoomManager());
		IAction zoomOut = new ZoomOutAction(root.getZoomManager());
		IAction save = new RSESaveAction(wbPart);

		actionReg.registerAction(zoomIn);
		actionReg.registerAction(zoomOut);
		actionReg.registerAction(save);

		site.getKeyBindingService().registerAction(zoomIn);
		site.getKeyBindingService().registerAction(zoomOut);
		
		viewer.setRootEditPart(root);

		/*
		// this does not really work - should instead consider moving the origin
		GraphicalViewer gv = getGraphicalViewer();
		System.err.println(gv.getClass());
		Viewport vp = (Viewport) ((AbstractGraphicalEditPart)gv.getRootEditPart()).getFigure();
		System.err.println(vp.getHorizontalRangeModel().toString());
		System.err.println(vp.getVerticalRangeModel().toString());
		System.err.println(vp.getClass());
		System.err.println(vp.getViewLocation());
		vp.setViewLocation(50,50);
		System.err.println(vp.getViewLocation());
		*/

		RSEContextMenuProvider provider = new ReloContextMenuProvider(viewer, actionReg);

		if (wbPart instanceof RSEEditor) {
			RSEEditor.graphicalViewManager.updateMenuMap((GraphicalEditor) wbPart, provider, provider);
		}
		
		viewer.setContextMenu(provider);
		site.registerContextMenu("relo.editor.contextmenu", provider, viewer);

		if (keyHandler != null ) viewer.setKeyHandler(keyHandler);
		
        /*
        // TODO: hook up the handler to call into OpenTypeAction
        ReloEditor editorPart = (ReloEditor) ((DefaultEditDomain)viewer.getEditDomain()).getEditorPart();
        Action act = new Action("blah\tCtrl+Shift+T") {
            @Override
            public void run() {
                System.err.println("running");
            }
        };
        
        IMenuManager imm = ((ReloEditorContributor)editorPart.getEditorSite().getActionBarContributor()).getActionBars().getMenuManager();
        IMenuManager imm2 = (IMenuManager) imm.find("Relo");
        imm2.insertAfter("model", act);
        editorPart.getEditorSite().getActionBars().setGlobalActionHandler("org.eclipse.jdt.ui.navigate.open.type", act);
        editorPart.getEditorSite().getActionBars().setGlobalActionHandler("org.eclipse.jdt.ui.navigate.openType", act);
        */
	}


	public static IFile getNewSaveFile(WorkbenchPart reloEditorView, IProject defaultProject) {
		ReloDocWizard wizard = new ReloDocWizard();
		IWorkbenchWindow ww = reloEditorView.getSite().getWorkbenchWindow();
		IStructuredSelection ss = StructuredSelection.EMPTY;
		if (defaultProject != null)
			ss = new StructuredSelection(defaultProject);

		wizard.init(ww.getWorkbench(), ss);
		new WizardDialog(ww.getShell(), wizard).open();

		return wizard.getFile();
	}

	public static Resource writeFile(WorkbenchPart reloEditorView, ReloController rc, ReloDoc rootReloModel, URI browseModel, BrowseModel bm, RdfDocumentWriter rdfWriter, Resource diagramRes) throws IOException {
		// let's do this in two passes, the first to get the namespaces
	    RdfDocumentWriter namespacesDeclarator = new NamespaceDeclaratorWriter(rdfWriter);
	    
    	List<ArtifactFragment> savedNodes = new ArrayList<ArtifactFragment>();
    	List<ArtifactRel> savedRels = new ArrayList<ArtifactRel>();
    	
    	//ReloRdfRepository repoCopy = new ReloRdfRepository(rc.getRepo());
    	
    	diagramRes = writeView(reloEditorView, rootReloModel, browseModel, bm, namespacesDeclarator, diagramRes, savedNodes, savedRels);
    	diagramRes = writeView(reloEditorView, rootReloModel, browseModel, bm, rdfWriter, diagramRes, savedNodes, savedRels);

		
		//Add a statement to the repository indicating that this file is a .relo file,
		//and add a statement indicating each statement the file contains
//
//		rc.getRepo().startTransaction();
//		rc.getRepo().addStatement(diagramRes, rc.getRepo().rdfType, RSECore.reloFile);
//		for(ArtifactFragment af : savedNodes) {
//			rc.getRepo().addStatement(diagramRes, RSECore.contains, af.getInstanceRes());
//		}
//		for(ArtifactRel ar : savedRels) {
//			rc.getRepo().addStatement(diagramRes, RSECore.contains, ar.getInstanceRes());
//		}
//    	rc.getRepo().commitTransaction();
    	rdfWriter.startDocument();
    	rdfWriter.writeStatement(diagramRes, rc.getRepo().rdfType, RSECore.reloFile);
    	for(ArtifactFragment af : savedNodes) {
    		rdfWriter.writeStatement(diagramRes, RSECore.contains, af.getInstanceRes());
		}
		for(ArtifactRel ar : savedRels) {
			rdfWriter.writeStatement(diagramRes, RSECore.contains, ar.getInstanceRes());
		}
    	rdfWriter.endDocument();
    	return diagramRes;
	}
	
	private static Resource writeView(WorkbenchPart reloEditorView, ReloDoc rootReloModel, URI browseModel, BrowseModel bm, final RdfDocumentWriter rdfWriter, Resource res, List<ArtifactFragment> savedNodes, List<ArtifactRel> savedRels) throws IOException {
		rdfWriter.startDocument();

		// Add a statement to the repository indicating
		// that this file is a .relo file
		rdfWriter
				.writeStatement(res,
						StoreUtil.getDefaultStoreRepository().rdfType,
						RSECore.reloFile);

		// save detail level
		rdfWriter.writeStatement(RSECore.createRseUri("DetailNode"),
				RSECore.detailLevelURI, StoreUtil.createMemLiteral(Integer
						.toString(rootReloModel.getDetailLevel())));

		// if we are saving a loaded shared diagram locally, saved diagram infor
		// to the file
		LoadUtils.checkForSavingSharedFile(reloEditorView, rdfWriter, res);

		Literal classLoc = StoreUtil.createMemLiteral(StoreUtil.getClassLoc(bm
				.getBundleSymbolicName(), bm.getClass()));

		// allows for different browseModel's to be stored in the file
		rdfWriter.writeStatement(RSECore.docRoot, browseModel, classLoc);

		rootReloModel.writeRDF(rdfWriter, RSECore.docRoot, savedNodes,
				savedRels);
		writeCommentChildren(res, rootReloModel.getCommentChildren(),
				rdfWriter, savedRels);
		rdfWriter.endDocument();
		return res;
	}
	  
	public static boolean canDropOnEditor(IStructuredSelection sel) {
		// Don't allow projects to be dragged into relo
		if(OpenItemUtils.containsProject(sel)) return false;
		
		return true;
	}
	
}
