package com.architexa.diagrams.chrono.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.part.WorkbenchPart;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.rio.RdfDocumentWriter;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.controlflow.ControlFlowModel;
import com.architexa.diagrams.chrono.controlflow.IfBlockModel;
import com.architexa.diagrams.chrono.controlflow.LoopBlockModel;
import com.architexa.diagrams.chrono.controlflow.UserCreatedControlFlowModel;
import com.architexa.diagrams.chrono.editparts.SeqEditPartFactory;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.models.UserCreatedInstanceModel;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.DiagramPolicy;
import com.architexa.diagrams.relo.ui.ReloDocWizard;
import com.architexa.diagrams.ui.RSEEditorViewCommon;
import com.architexa.diagrams.utils.LoadUtils;
import com.architexa.diagrams.utils.NamespaceDeclaratorWriter;
import com.architexa.diagrams.utils.OpenItemUtils;
import com.architexa.org.eclipse.draw2d.ConnectionLayer;
import com.architexa.org.eclipse.draw2d.FreeformLayer;
import com.architexa.org.eclipse.draw2d.Layer;
import com.architexa.org.eclipse.draw2d.LayeredPane;
import com.architexa.org.eclipse.draw2d.StackLayout;
import com.architexa.org.eclipse.draw2d.Viewport;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.ContextMenuProvider;
import com.architexa.org.eclipse.gef.KeyHandler;
import com.architexa.org.eclipse.gef.editparts.ScalableRootEditPart;
import com.architexa.org.eclipse.gef.editparts.ZoomManager;
import com.architexa.org.eclipse.gef.ui.actions.ActionRegistry;
import com.architexa.org.eclipse.gef.ui.actions.ZoomInAction;
import com.architexa.org.eclipse.gef.ui.actions.ZoomOutAction;
import com.architexa.org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import com.architexa.org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class SeqViewEditorCommon extends RSEEditorViewCommon {
	final static Logger logger = SeqPlugin.getLogger(SeqViewEditorCommon.class);

	public static void configureGraphicalViewer(IWorkbenchPart workbenchPart, ScrollingGraphicalViewer viewer, ActionRegistry actionRegistry, KeyHandler keyHandler, final DiagramModel model) {
		final ScalableRootEditPart root = new ScalableRootEditPart() {

//TODO Check with Liz why we need this and the specific feedback layer	
//			@Override
//			protected void createLayers(LayeredPane layeredPane) {
				
//				layeredPane.add(new FreeformLayer(), SeqEditor.LIFE_LINE_LAYER);
//				layeredPane.add(new FreeformLayer(), SeqEditor.CONDITIONAL_LAYER);
//				super.createLayers(layeredPane);
				// Instance panel and the portion of the life line in it
				// added last so that they lie on top of anything
				// that is located under the panel due to scrolling
//				layeredPane.add(new FreeformLayer(), SeqEditor.INSTANCE_PANEL_LAYER);
//				layeredPane.add(new FreeformLayer(), SeqEditor.LIFE_LINE_IN_INSTANCE_PANEL_LAYER);

				// Then put the feedback layer on top of the instance panel 
				// layer so that when an instance figure is dragged, we can
				// see its drag shadow in the instance panel
//				Layer feedbackLayer = new Layer() {
//					@Override
//					public Dimension getPreferredSize(int wHint, int hHint) {
//						Rectangle rect = new Rectangle();
//						for (int i = 0; i < getChildren().size(); i++)
//							rect.union(((IFigure)getChildren().get(i)).getBounds());
//						return rect.getSize();
//					}
//				};
//				feedbackLayer.setEnabled(false);
//				layeredPane.add(feedbackLayer, FEEDBACK_LAYER);

//				layeredPane.add(new FreeformLayer(), SeqEditor.COMMENT_LAYER);
				
				//TODO Why does this not work????
				// Then put handle layer on top of all others so that instance
				// figures in instance panel can be selected
//				layeredPane.remove(getLayer(HANDLE_LAYER));
//				Layer layer = new Layer() {
//					@Override
//					public Dimension getPreferredSize(int wHint, int hHint) {
//						return new Dimension();
//					}
//				};
//				layer.setEnabled(true);
//				layeredPane.add(layer, HANDLE_LAYER);
				
//				getLayer(SeqEditor.LIFE_LINE_LAYER).setEnabled(true);
//				getLayer(SeqEditor.CONDITIONAL_LAYER).setEnabled(true);
//				getLayer(SeqEditor.INSTANCE_PANEL_LAYER).setEnabled(true);
//				getLayer(SeqEditor.LIFE_LINE_IN_INSTANCE_PANEL_LAYER).setEnabled(true);
//				getLayer(SeqEditor.COMMENT_LAYER).setEnabled(true);
				
//			}

			/**
			 * Creates a layered pane and the layers that should be printed.
			 * @see com.architexa.org.eclipse.gef.print.PrintGraphicalViewerOperation
			 * @return a new LayeredPane containing the printable layers
			 */
			@Override
			protected LayeredPane createPrintableLayers() {

				LayeredPane pane = new LayeredPane();

				//Life line should show up above the conditionals
				pane.add(new FreeformLayer(), SeqEditor.CONDITIONAL_LAYER);
				pane.add(new FreeformLayer(), SeqEditor.LIFE_LINE_LAYER);
				
				// From super.createPrintableLayers(). Needs to be done after
				// adding the life line layer so that the life line does not 
				// appear on top of components in the primary layer, and needs 
				// to be done after adding the conditional layer so that the 
				// conditional layer does not block the primary layer from receiving 
				// mouse over events, which is necessary to show conditional blocks
				Layer layer = new Layer();

				layer.setLayoutManager(new StackLayout());
				pane.add(layer, PRIMARY_LAYER);

				layer = new ConnectionLayer();
				layer.setPreferredSize(new Dimension(5, 5));
				pane.add(layer, CONNECTION_LAYER);

				// Adding the instance-panel and life-line-in-instance-panel 
				// layers last since the instance panel and the portion of the
				// life line within it should lie on top (need to be on top of 
				// anything that is located under the panel due to scrolling)
				pane.add(new FreeformLayer(), SeqEditor.INSTANCE_PANEL_LAYER);
				pane.add(new FreeformLayer(), SeqEditor.LIFE_LINE_IN_INSTANCE_PANEL_LAYER);
				addGeneralConnectionLayer(pane);
				pane.add(new FreeformLayer(), SeqEditor.COMMENT_LAYER);
				
				layer = new Layer() {
					//TODO Why this override?
					@Override
					public Dimension getPreferredSize(int wHint, int hHint) {
						return new Dimension();
					}
				};
				layer.setEnabled(true);
				pane.add(layer, HANDLE_LAYER);
				return pane;
			}

		};
		viewer.setRootEditPart(root);

		viewer.setEditPartFactory(new SeqEditPartFactory());
		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer).setParent(keyHandler));

		List<String> zoomLevels = new ArrayList<String> (3);
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		root.getZoomManager().setZoomLevelContributions(zoomLevels);

		IAction zoomIn = new ZoomInAction(root.getZoomManager());
		IAction zoomOut = new ZoomOutAction(root.getZoomManager());
		actionRegistry.registerAction(zoomIn);
		actionRegistry.registerAction(zoomOut);
		
		ContextMenuProvider cmProvider = new SeqEditorContextMenuProvider(viewer, actionRegistry, workbenchPart);
		viewer.setContextMenu(cmProvider);
		workbenchPart.getSite().registerContextMenu(cmProvider, viewer);

		((Viewport)root.getFigure()).addPropertyChangeListener(Viewport.PROPERTY_VIEW_LOCATION, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				int panelX = model.getInstancePanel().getBounds().x;
				int viewportY = ((Viewport)root.getFigure()).getViewLocation().y;
				ZoomManager zoomManager = root.getZoomManager();
				model.getInstancePanel().setLocation(new Point(panelX, viewportY*(1/zoomManager.getZoom())));
			}
		});
	}
	
	private static class SeqDocWizard extends ReloDocWizard {
		@Override
		public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
			super.init(workbench, currentSelection);
			setWindowTitle("New Chrono Document");
		}
		@Override
		public void addPages() {
			super.addPages();
			WizardNewFileCreationPage mainPage = (WizardNewFileCreationPage)getPage("newFilePage1");
			mainPage.setTitle("Chrono Document");
			mainPage.setDescription("Create a new Chrono Document.");
			mainPage.setFileName("Sequence.atxa");
		}
	}
	
	public static IFile getNewSaveFile(WorkbenchPart seqEditorView) {
		SeqDocWizard wizard = new SeqDocWizard();
		IWorkbenchWindow ww = seqEditorView.getSite().getWorkbenchWindow();
		IStructuredSelection ss = StructuredSelection.EMPTY;

		wizard.init(ww.getWorkbench(), ss);
		new WizardDialog(ww.getShell(), wizard).open();

		return wizard.getFile();
	}

	public static DiagramModel writeFile(WorkbenchPart seqEditorView, DiagramModel model, RdfDocumentWriter rdfWriter, Resource fileRes) throws IOException {
		// let's do this in two passes, the first to get the namespaces
		
		RdfDocumentWriter namespacesDeclarator = new NamespaceDeclaratorWriter(rdfWriter);
		model = writeView(seqEditorView, model, namespacesDeclarator, fileRes);
		model = writeView(seqEditorView, model, rdfWriter, fileRes);
		return model;
	}
	
	public static DiagramModel writeView(WorkbenchPart seqEditorView, DiagramModel diagramModel, RdfDocumentWriter rdfWriter, Resource fileRes) throws IOException {
		diagramModel.setSavedDiagramResource(fileRes);

		rdfWriter.startDocument();

		//Add a statement to the repository indicating 
		//that this file is a .chrono file
		rdfWriter.writeStatement(fileRes, StoreUtil.getDefaultStoreRepository().rdfType, RSECore.chronoFile);

		// if we are saving a loaded shared diagram locally, saved diagram infor to the file
		LoadUtils.checkForSavingSharedFile(seqEditorView, rdfWriter, fileRes);
		
		// save detail level
		rdfWriter.writeStatement(RSECore.createRseUri("DetailNode"), RSECore.detailLevelURI, StoreUtil.createMemLiteral(Integer.toString(diagramModel.getDetailLevel())));

		writeChildrenAndConnections(diagramModel, fileRes, diagramModel.getChildren(), rdfWriter);
		
		writeControlFlowChildren(diagramModel, fileRes, diagramModel.getConditionalChildren(), rdfWriter);
		
		writeCommentChildren(fileRes, diagramModel.getCommentChildren(), rdfWriter, new ArrayList<ArtifactRel>());

		rdfWriter.endDocument();
		return diagramModel;
	}

	
	
	public static void writeControlFlowChildren(ArtifactFragment parentModel, Resource parentRes, List<ControlFlowModel> cfModels,
			RdfDocumentWriter rdfWriter) throws IOException {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();

		for (ArtifactFragment child : cfModels) {

			Resource childSaveRes = child.getInstanceRes();

			rdfWriter.writeStatement(parentRes, RSECore.contains, child.getArt().elementRes);

			
			rdfWriter.writeStatement(child.getArt().elementRes, repo.rdfType, RSECore.controlFlowType);
			rdfWriter.writeStatement(child.getArt().elementRes, RSECore.model, child.getArt().elementRes);
//			rdfWriter.writeStatement(childSaveRes, RJCore.index, repo.getLiteral(Integer.toString(parentModel.getShownChildren().indexOf(child))));
			if (child instanceof ControlFlowModel) {
				if (child instanceof IfBlockModel) {
					rdfWriter.writeStatement(child.getArt().elementRes, RSECore.controlFlowName, RSECore.ifBlock);
					for (MemberModel ifStmtModel : ((IfBlockModel)child).getThenStmts()) {
						rdfWriter.writeStatement(child.getArt().elementRes, RSECore.thenStmt, repo.getLiteral(ifStmtModel.toString()));
					}
					for (MemberModel ifStmtModel : ((IfBlockModel)child).getElseStmts()) {
						rdfWriter.writeStatement(child.getArt().elementRes, RSECore.elseStmt, repo.getLiteral(ifStmtModel.toString()));
					}
					for (MemberModel ifStmtModel : ((IfBlockModel)child).getIfStmts()) {
						rdfWriter.writeStatement(child.getArt().elementRes, RSECore.ifStmt, repo.getLiteral(ifStmtModel.toString()));
					}
				}
				if (child instanceof LoopBlockModel) {
					rdfWriter.writeStatement(child.getArt().elementRes, RSECore.controlFlowName, RSECore.loopBlock);
					for (MemberModel loopStmtModel : ((LoopBlockModel)child).getStatements()) {
						rdfWriter.writeStatement(child.getArt().elementRes, RSECore.loopStmt, repo.getLiteral(loopStmtModel.toString()));
					}
				} 
				
				if (child instanceof UserCreatedControlFlowModel) {
					rdfWriter.writeStatement(child.getArt().elementRes, RSECore.controlFlowName, RSECore.userControlBlock);
					for (MemberModel userStmtModel : ((UserCreatedControlFlowModel)child).getStatements()) {
						rdfWriter.writeStatement(child.getArt().elementRes, RSECore.conditionalStmt, repo.getLiteral(userStmtModel.toString()));
					}
				} 
				
				String cfLabel = ((ControlFlowModel) child).getConditionalLabel();
				if (cfLabel != null)
					rdfWriter.writeStatement(child.getArt().elementRes, RSECore.name, repo.getLiteral(cfLabel));
				
			}
			
			Resource detailsNode =  child.getArt().elementRes;
			// we no longer need to save names or types since we calc this from the res
			// rdfWriter.writeStatement(detailsNode, repo.rdfType, child.queryType(repo));
			if (child instanceof MemberModel) {
				if (((MemberModel) child).isUserCreated()) {
						((MethodBoxModel)child).writeRDF(rdfWriter, repo, childSaveRes);
				} else	if (((MemberModel)child).isAccess() && ((MemberModel)child).getPartner()!=null) {
					rdfWriter.writeStatement(detailsNode, RSECore.link, ((MemberModel)child).getPartner().getInstanceRes());
				}
			}

			// for anonymous classes, write a statement indicating the superclass it 
			// implements or extends. This stmt is necessary when loading to display the
			// anon class's name as "new AnonClass() {..}" rather than as just a number
			if (child instanceof InstanceModel && ((InstanceModel)child).isAnonymousClass()) {
				Value superClass = repo.getStatement(detailsNode, RJCore.inherits, null).getObject();
				rdfWriter.writeStatement(detailsNode, RJCore.inherits, superClass);
			}

			List<ArtifactFragment> children = ((ControlFlowModel)child).getInnerConditionalModels();
			// write policies
	    	for (DiagramPolicy diagPolicy : child.getDiagramPolicies()) {
	    		try {
	    			diagPolicy.writeRDF(rdfWriter);
	    		} catch (Throwable t){
	    			logger.error("Error while Saving, some data may be lost",t);
	    		}
			}
	    	
	    	
	    	// switch types: TODO make this consistant
	    	ArrayList<ControlFlowModel> cfChildren = new ArrayList<ControlFlowModel>(); 
	    	for (ArtifactFragment afChild : children) {
	    		if (afChild instanceof ControlFlowModel)
	    			cfChildren.add((ControlFlowModel) afChild);
	    	}
	    	
	    	writeControlFlowChildren(child, child.getArt().elementRes, cfChildren, rdfWriter);
		}
	}

	
	public static void writeChildrenAndConnections(ArtifactFragment parentModel, Resource parentRes, List<ArtifactFragment> childrenList,
			RdfDocumentWriter rdfWriter) throws IOException {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();

		for (ArtifactFragment child : childrenList) {

			Resource childSaveRes = child.getInstanceRes();

			rdfWriter.writeStatement(parentRes, RSECore.contains, childSaveRes);

			rdfWriter.writeStatement(childSaveRes, repo.rdfType, RSECore.node);
			rdfWriter.writeStatement(childSaveRes, RSECore.model, child.getArt().elementRes);
			rdfWriter.writeStatement(childSaveRes, RJCore.index, repo.getLiteral(Integer.toString(parentModel.getShownChildren().indexOf(child))));
			if (child instanceof InstanceModel) {
				if (child instanceof UserCreatedInstanceModel) {
					((UserCreatedInstanceModel)child).writeRDF(rdfWriter, repo, childSaveRes);
				} else {
				InstanceModel instance = (InstanceModel)child;
				String instanceName = instance.getInstanceName() != null ? instance.getInstanceName() : "";
				rdfWriter.writeStatement(childSaveRes, RSECore.instanceName, repo.getLiteral(instanceName));
				}
			}
			
//			if (child instanceof UserCreatedNodeModel) {
//				((UserCreatedNodeModel)child).writeRDF(rdfWriter, repo, childSaveRes);
//			}
			
			Resource detailsNode =  child.getArt().elementRes;
			// we no longer need to save names or types since we calc this from the res
			// rdfWriter.writeStatement(detailsNode, repo.rdfType, child.queryType(repo));
			if (child instanceof MemberModel) {
				if (((MemberModel) child).isUserCreated()) {
						((MethodBoxModel)child).writeRDF(rdfWriter, repo, childSaveRes);
				} else	if (((MemberModel)child).isAccess() && ((MemberModel)child).getPartner()!=null) {
					rdfWriter.writeStatement(detailsNode, RSECore.link, ((MemberModel)child).getPartner().getInstanceRes());
				}
			}

			// for anonymous classes, write a statement indicating the superclass it 
			// implements or extends. This stmt is necessary when loading to display the
			// anon class's name as "new AnonClass() {..}" rather than as just a number
			if (child instanceof InstanceModel && ((InstanceModel)child).isAnonymousClass()) {
				Value superClass = repo.getStatement(detailsNode, RJCore.inherits, null).getObject();
				rdfWriter.writeStatement(detailsNode, RJCore.inherits, superClass);
			}

			for (ArtifactRel conn : child.getSourceConnections()) {
				Resource connSaveRes = conn.getInstanceRes();
				rdfWriter.writeStatement(connSaveRes, repo.rdfType, RSECore.link);
				rdfWriter.writeStatement(connSaveRes, RSECore.model, conn.relationRes);
				rdfWriter.writeStatement(connSaveRes, repo.rdfSubject, conn.getSrc().getInstanceRes());
				rdfWriter.writeStatement(connSaveRes, repo.rdfObject, conn.getDest().getInstanceRes());
				String message = (conn instanceof ConnectionModel) ? ((ConnectionModel)conn).getLabel() : "";
				// we no longer need to save names or types since we calc this from the res
				rdfWriter.writeStatement(connSaveRes, RSECore.name, repo.getLiteral(message));
			}

			List<ArtifactFragment> children = (child instanceof NodeModel) ? ((NodeModel)child).getChildren() : child.getShownChildren();
			// write policies
	    	for (DiagramPolicy diagPolicy : child.getDiagramPolicies()) {
	    		try {
	    			diagPolicy.writeRDF(rdfWriter);
	    		} catch (Throwable t){
	    			logger.error("Error while Saving, some data may be lost",t);
	    		}
			}
	    	
			writeChildrenAndConnections(child, childSaveRes, children, rdfWriter);
		}
	}

	public static boolean canDropOnEditor(IStructuredSelection sel) {
		// Don't allow projects or packages to be dragged into chrono
		if(OpenItemUtils.containsProject(sel)) return false;
		if(OpenItemUtils.containsPackage(sel)) return false;
		
		return true;
	}
}
