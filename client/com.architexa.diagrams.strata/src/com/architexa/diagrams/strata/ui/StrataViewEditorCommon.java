package com.architexa.diagrams.strata.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.WorkbenchPart;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.commands.ColorActionCommand;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.model.WidthDPolicy;
import com.architexa.diagrams.relo.jdt.actions.AddJavaDocAction;
import com.architexa.diagrams.relo.jdt.actions.EditJavaDocDialogAction;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.doc.StrataDocWizard;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.parts.LayerEditPart;
import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.strata.parts.TitledArtifactEditPart;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.diagrams.ui.RSEEditorViewCommon;
import com.architexa.diagrams.ui.RSESaveAction;
import com.architexa.diagrams.utils.LoadUtils;
import com.architexa.diagrams.utils.NamespaceDeclaratorWriter;
import com.architexa.diagrams.utils.OpenItemUtils;
import com.architexa.org.eclipse.draw2d.FreeformLayer;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LayeredPane;
import com.architexa.org.eclipse.draw2d.Viewport;
import com.architexa.org.eclipse.gef.ContextMenuProvider;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.GraphicalViewer;
import com.architexa.org.eclipse.gef.KeyHandler;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.editparts.ScalableRootEditPart;
import com.architexa.org.eclipse.gef.editparts.ZoomManager;
import com.architexa.org.eclipse.gef.extensions.GraphicalViewManager;
import com.architexa.org.eclipse.gef.extensions.GraphicalViewManager.NullImpl;
import com.architexa.org.eclipse.gef.ui.actions.ActionRegistry;
import com.architexa.org.eclipse.gef.ui.actions.ZoomInAction;
import com.architexa.org.eclipse.gef.ui.actions.ZoomOutAction;
import com.architexa.rse.AccountSettings;
import com.architexa.rse.BuildStatus;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class StrataViewEditorCommon extends RSEEditorViewCommon {

	private static Logger logger = StrataPlugin.getLogger(StrataViewEditorCommon.class);
	public static void configureGraphicalViewer(final IWorkbenchPart wbPart, final GraphicalViewer graphicalViewer, final ActionRegistry actionRegistry, KeyHandler keyHandler, final GraphicalViewManager graphicalViewManager) {
		try {
		ScalableRootEditPart root = new ScalableRootEditPart() {
            @Override
			public IFigure createFigure() {
                IFigure scalableViewport = super.createFigure();
                if (!(graphicalViewManager instanceof NullImpl) && graphicalViewManager!=null)
                	scalableViewport = graphicalViewManager.createGEFScalableViewPort(this);
                // enabled because needed by NavAidsEditPolicy
                getLayer(LayerConstants.SCALED_FEEDBACK_LAYER).setEnabled(true);
                return scalableViewport;
            }
            @Override
			public void createLayers(LayeredPane layeredPane){
            	super.createLayers(layeredPane);
            	layeredPane.add(new FreeformLayer(), StrataEditor.COMMENT_LAYER);
            	getLayer(StrataEditor.COMMENT_LAYER).setEnabled(true);
            }
            
            @Override
            protected LayeredPane createPrintableLayers() {
            	LayeredPane pane = super.createPrintableLayers();
            	addGeneralConnectionLayer(pane);
            	pane.add(new FreeformLayer(), StrataEditor.COMMENT_LAYER);
            	return pane;
            	
            }
            
        };
        
        IFigure fig = ((Viewport)root.getFigure()).getContents();
        fig.setLayoutManager(new FreeformStackLayout(root));
		
		graphicalViewer.setRootEditPart(root);
		graphicalViewer.setEditPartFactory(new StrataEditPartFactory());
		
		List<String> zoomLevels = new ArrayList<String> (3);
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		root.getZoomManager().setZoomLevelContributions(zoomLevels);

		IAction zoomIn = new ZoomInAction(root.getZoomManager());
		IAction zoomOut = new ZoomOutAction(root.getZoomManager());
		IAction save = new RSESaveAction(wbPart);
		
		actionRegistry.registerAction(zoomIn);
		actionRegistry.registerAction(zoomOut);
		actionRegistry.registerAction(save);

		ContextMenuProvider menuProvider = new RSEContextMenuProvider(graphicalViewer, actionRegistry) {
			@SuppressWarnings("restriction")
			@Override
			public void buildContextMenu(IMenuManager menu) {
				try {
					super.buildContextMenu(menu);
	
					List<?> sel = getViewer().getSelectedEditParts();
					if (!sel.isEmpty()) {
					
						final EditPart ep = (EditPart)sel.get(0);
	
						// actions to move to vertical layer
						MenuManager subMenu = new MenuManager("Move to Vertical Layer");
						subMenu.add(new CreateVertLayerAndMoveAction(wbPart, sel));
						subMenu.add(new CreateLeftVertLayerAndMoveAction(wbPart, sel));
						menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, subMenu);
						String state = "Open";
						if (ep instanceof TitledArtifactEditPart) {
							final ArtifactFragment af = ((TitledArtifactEditPart) ep).getArtFrag();
							if (ClosedContainerDPolicy.isShowingChildren(af))
								state = "Close";
							IAction toggleOpenAction = new Action(state + " Package") {
								@Override
								public void run() {
									Command cmd = ((StrataArtFragEditPart)ep).getTogglePackageCommand((StrataArtFragEditPart) ep);
									((StrataArtFragEditPart)ep).getRootController().execute(cmd);
								};
							};
							menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, toggleOpenAction);
						}
	
						// if experimental actions enabled, put its 
						// submenu below the move to vertical layer submenu
						if (AccountSettings.EXPERIMENTAL_MODE) {
							MenuManager experimentalActionSubMenu = new MenuManager("Experimental Actions");
							experimentalActionSubMenu.add(actionRegistry.getAction("collapseAll"));
							experimentalActionSubMenu.add(actionRegistry.getAction("reduce"));
							experimentalActionSubMenu.add(actionRegistry.getAction("sdd"));
							experimentalActionSubMenu.add(actionRegistry.getAction("showInteractions"));
							menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, experimentalActionSubMenu);
						}
	
						// add java doc to diagram and edit java doc in source
						IAction act = new AddJavaDocAction((AbstractGraphicalEditPart) ep);
						if (!((AddJavaDocAction) act).canRun(ep.getModel())) 
							act.setEnabled(false);
						// place separator before java doc action(s)
						menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, new Separator());
						menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, act);
						if (ep instanceof StrataArtFragEditPart && ep.getModel() instanceof ArtifactFragment && !(ep.getModel() instanceof Layer)) {
							IJavaElement jElem = ((StrataArtFragEditPart)ep).getJaveElement();
							act = new EditJavaDocDialogAction((AbstractGraphicalEditPart) ep, ((ArtifactFragment) ep.getModel()).getRootArt().getRepo());
							menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, act);
							if (!(jElem instanceof org.eclipse.jdt.internal.core.SourceType))
								act.setEnabled(false);
						}
	
						//action to highlight layers/members					
						addColorAction(sel, menu, graphicalViewer);
						//action to change connection width
						WidthDPolicy.addConnectionWidthChangeAction(sel, menu, graphicalViewer);
					}
				} catch (Throwable t) {
					logger.error("Error creating menu: ", t);
				}
			}
		};
		
		graphicalViewer.setContextMenu(menuProvider);
		wbPart.getSite().registerContextMenu(menuProvider, graphicalViewer);

		if (keyHandler != null ) graphicalViewer.setKeyHandler(keyHandler);
		} catch (Throwable t) {
			logger.error("Error creating menu: ", t);
		}
	}

	public static String DEFAULT = "Default";
	protected static void addColorAction(final List<?> sel, IMenuManager menu, 
			GraphicalViewer graphicalViewer) {
		for (Object item : sel) {
			// TODO support coloring of NamedRelParts
			if (!(item instanceof TitledArtifactEditPart) /*&& !(item instanceof NamedRelationPart)*/) return;
		}
		
		MenuManager subMenu = new MenuManager("Highlight");
    	subMenu.add(getColorAction(ColorScheme.RED, sel, graphicalViewer));
		subMenu.add(getColorAction(ColorScheme.BLUE, sel, graphicalViewer));
		subMenu.add(getColorAction(ColorScheme.GREEN, sel, graphicalViewer));
		subMenu.add(new Separator());
		subMenu.add(getColorAction(DEFAULT, sel, graphicalViewer));

		// Highlight menu goes in the edit appearance section
		menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_EDIT_APPEARANCE, subMenu);
	}

	private static IAction getColorAction(final String actnText, final List<?> sel, final GraphicalViewer graphicalViewer) {
		return new Action(actnText) {
			@Override
			public void run() {
				CompoundCommand cmd = new CompoundCommand("Coloring: " + actnText);
				BuildStatus.addUsage("Strata > " + cmd.getLabel());
				
					for (Object part : sel) {
						cmd.add(new ColorActionCommand(actnText, (ArtifactFragment) ((EditPart) part).getModel()));
						if (part instanceof LayerEditPart) {
							for (Object multiLayeredChild : ((LayerEditPart) part).getChildren()) {
								if (multiLayeredChild instanceof LayerEditPart)
									cmd.add(new ColorActionCommand(actnText, (ArtifactFragment) multiLayeredChild));
							}
						}
					}
					
				graphicalViewer.getEditDomain().getCommandStack().execute(cmd);
			}
		};
	}

	public static IFile getNewSaveFile(WorkbenchPart strataEditorView, StrataRootEditPart scep) {
		StrataDocWizard wizard = new StrataDocWizard();

		IWorkbenchWindow ww = strataEditorView.getSite().getWorkbenchWindow();
		IStructuredSelection ss = StructuredSelection.EMPTY;

		List<ArtifactFragment> curChildren = ClosedContainerDPolicy.getShownChildren(scep.getArtFrag());
		IProject defaultProject = null;
		if (curChildren.size() > 0) {
			ArtifactFragment firstElement = curChildren.get(0);
			IJavaElement elemInProj = strataAFToJavaElement(scep.getRepo(), firstElement);
			if (elemInProj != null && elemInProj.getResource()!=null) {
				defaultProject = elemInProj.getResource().getProject();
			}
		}
		if (defaultProject == null)
			defaultProject = getCurrentProject(strataEditorView);
		if (defaultProject != null)
			ss = new StructuredSelection(defaultProject);

		wizard.init(ww.getWorkbench(), ss);
		new WizardDialog(ww.getShell(), wizard).open();

		return wizard.getFile();

	}
	
	private static IJavaElement strataAFToJavaElement(ReloRdfRepository repo, ArtifactFragment tgtElement) {
		// @tag post-test-cleanup
		//if (tgtElement instanceof CUArt) {
		//	return ((CUArt)tgtElement).cu.getJDTElement(repo);
		//} else if (tgtElement instanceof StrataArtFrag) {
		//	return strataAFToJavaElement(repo, ((StrataArtFrag)tgtElement).getCUArt());
		//}
		//return null;
		return new CodeUnit(tgtElement.getArt()).getJDTElement(repo);
	}

	public static Resource writeFile(WorkbenchPart strataEditorView, StrataRootEditPart rc, BrowseModel bm, URI browsemodel, StrataRootDoc rootContent, RdfDocumentWriter rdfWriter, Resource rootAFRes) throws IOException {
		// let's do this in two passes, the first to get the namespaces
		RdfDocumentWriter namespacesDeclarator = new NamespaceDeclaratorWriter(rdfWriter);
		
		List<ArtifactFragment> savedNodes = new ArrayList<ArtifactFragment>();
    	List<ArtifactRel> savedRels = new ArrayList<ArtifactRel>();

    	rootAFRes = writeView(strataEditorView, namespacesDeclarator, bm, browsemodel, rootContent, rootAFRes, savedNodes, savedRels);
    	rootAFRes = writeView(strataEditorView, rdfWriter, bm, browsemodel, rootContent, rootAFRes, savedNodes, savedRels);
		
		
    	rdfWriter.startDocument();
		rdfWriter.writeStatement(rootAFRes, rc.getRepo().rdfType, RSECore.strataFile);
		for(ArtifactFragment af : savedNodes) {
			rdfWriter.writeStatement(rootAFRes, RSECore.contains, af.getInstanceRes());
		}
		for(ArtifactRel ar : savedRels) {
			rdfWriter.writeStatement(rootAFRes, RSECore.contains, ar.getInstanceRes());
		}
		rdfWriter.endDocument();
		return rootAFRes;
	}

	private static Resource writeView(WorkbenchPart strataEditorView, RdfDocumentWriter rdfWriter, BrowseModel bm, URI browseModel, StrataRootDoc rootContent, Resource rootAFRes, List<ArtifactFragment> savedNodes,
			List<ArtifactRel> savedRels) throws IOException {
		    rdfWriter.startDocument();
			//Add a statement to the repository indicating 
			//that this file is a .strata 
	        rdfWriter.writeStatement(rootAFRes, StoreUtil.getDefaultStoreRepository().rdfType, RSECore.strataFile);
	        
	        // if we are saving a loaded shared diagram locally, saved diagram infor to the file
			LoadUtils.checkForSavingSharedFile(strataEditorView, rdfWriter, rootAFRes);
			
			// save detail level
			rdfWriter.writeStatement(RSECore.createRseUri("DetailNode"), RSECore.detailLevelURI, StoreUtil.createMemLiteral(Integer.toString(rootContent.getDetailLevel())));
			
			Literal classLoc = StoreUtil.createMemLiteral(StoreUtil.getClassLoc(bm.getBundleSymbolicName(), bm.getClass()));

			// allows for different browseModel's to be stored in the file
	        rdfWriter.writeStatement(RSECore.docRoot, browseModel, classLoc);		
	        
	        rootContent.writeRDF(rdfWriter, RSECore.docRoot, savedNodes, savedRels);
	        writeCommentChildren(rootAFRes, rootContent.getCommentChildren(), rdfWriter, savedRels);
			rdfWriter.endDocument();
			return rootAFRes;
	}

	public static IProject getCurrentProject(WorkbenchPart strataEditorView) {
		ISelection sel = strataEditorView.getSite().getSelectionProvider().getSelection();
		if (sel instanceof IStructuredSelection) {
			Object selItem = ((IStructuredSelection)sel).getFirstElement();
			if (selItem instanceof IResource) return ((IResource)selItem).getProject();
		}
		return null;		
	}
	
	protected static boolean canDropOnEditor(IStructuredSelection sel) {
		// Don't allow methods or fields to be dragged into strata
		if(OpenItemUtils.containsMethodOrField(sel)) return false;
		
		return true;
	}
}
