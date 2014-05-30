/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.parts;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.draw2d.NonEmptyFigure;
import com.architexa.diagrams.draw2d.NonEmptyFigureSupport;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.parts.BasicRootController;
import com.architexa.diagrams.parts.IRSERootEditPart;
import com.architexa.diagrams.parts.NavAidsEditPart;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;
import com.architexa.diagrams.relo.jdt.browse.AbstractJDTBrowseModel;
import com.architexa.diagrams.relo.jdt.services.PluggableEditPartSupport;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.cache.DepNdx;
import com.architexa.diagrams.strata.commands.ModelUtils;
import com.architexa.diagrams.strata.commands.MultiBreakCommand;
import com.architexa.diagrams.strata.figures.LinkUtils;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.ContainedClassSizeCacheDPolicy;
import com.architexa.diagrams.strata.ui.StrataEditor;
import com.architexa.diagrams.strata.ui.StrataView;
import com.architexa.diagrams.ui.RSEShareableDiagramEditorInput;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.draw2d.Animation;
import com.architexa.org.eclipse.draw2d.ConnectionLayer;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FigureListener;
import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.FreeformLayer;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.MarginBorder;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Insets;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.DefaultEditDomain;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.LayerConstants;
import com.architexa.org.eclipse.gef.commands.CommandStack;
import com.architexa.org.eclipse.gef.commands.CommandStackEvent;
import com.architexa.org.eclipse.gef.commands.CommandStackEventListener;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.ui.parts.GraphicalView.ViewEditDomain;
import com.architexa.rse.BuildStatus;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.RepositoryMgr;
import com.architexa.store.StoreUtil;


public class StrataRootEditPart extends TitledArtifactEditPart implements /*IPropertySource,*/ BasicRootController, IRSERootEditPart {

	public static final Logger logger = StrataPlugin.getLogger(StrataRootEditPart.class);
	private static double jdtUIVer = com.architexa.collab.proxy.PluginUtils.getPluginVer("org.eclipse.jdt.ui");
	
	Figure figHeading = new Figure();
	public NonEmptyFigure nonEmptyFigure;

	Figure contentFig = null;
	Figure rootFig;

	@Override
	protected IFigure createContainerFigure() {

		/** 
		 * Code showing Strata without centering
		 */
		
//		rootFig = new FreeformLayer() {
//			@Override
//			protected void fireFigureMoved() {
//				super.fireFigureMoved();
//				System.err.println("Fig move fire");
//			}
//		};
//
//		rootFig.addFigureListener(new FigureListener() {
//			public void figureMoved(IFigure source) {
//				System.err.println("Figmoved");
//			}
//		});
//		rootFig.setOpaque(true);
////		rootFig.setVisible(true);
////		rootFig.setBackgroundColor(ColorConstants.blue);
//		FlowLayout rootLayout = new FlowLayout(false);
//		rootLayout.setStretchMinorAxis(true);
//
//		rootFig.setLayoutManager(rootLayout);
//		rootFig.setBorder(new MarginBorder(new Insets(8)));
//
//		IFigure label = new Label(" \n Packages and Classes can be Dragged from the package explorer to create a diagram.");
//		nonEmptyFigure = new NonEmptyFigure(label);
//		NonEmptyFigureSupport.instructionHighlight(label, 10);
//		rootFig.add(nonEmptyFigure);
//		if (warning == null)
//			warning = new Label("");
//		rootFig.add(this.warning);
//		contentFig = new Figure();
////		FlowLayout layout = new FlowLayout( /* isHorizontal */false);
////		layout.setMajorSpacing(20);
////		layout.setStretchMinorAxis(true);
////		rootFig.setLayoutManager(layout);
//		
//		FlowLayout layout = new FlowLayout( /*isHorizontal*/false);
//		layout.setMajorSpacing(20);
//		layout.setStretchMinorAxis(true);
//		contentFig.setLayoutManager(layout);
//		rootFig.add(contentFig);
//		
////		LinkUtils.convertToLabelLinks(figHeading, getCommonContextLabel(), "",
////				getRootModel(), getRepo(), this);
////		rootFig.add(figHeading, 0);
//		return rootFig;
			
		
		/**
		 * CODE to center strata diagram
		 */
		
		/**
		 * Strata has two root figures (horizRootFigBase and rootFig) to properly show the diagram in the center.
		 * The rootFigBase uses a horizontal ToolbarLayout with vertically center aligned children.
		 * The rootFig is the child of the rootFigBase and uses a vertical ToolBarLayout with
		 * horizontally center aligned children. To make sure that the rootFig stays at the center of
		 * the screen its width has be always set to the rootFigBase's width. 
		 */
		final Figure horizRootFigBase = new FreeformLayer();
		
		FreeformLayer verticalRootFigBase = new FreeformLayer() {
			@Override
			public void setBounds(Rectangle rect) {
				super.setBounds(rect);
				getBounds().width = horizRootFigBase.getBounds().width;
			}
		};
		
		ToolbarLayout rootLayout1 = new ToolbarLayout(/*isHoriz*/true);
		rootLayout1.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		rootLayout1.setStretchMinorAxis(false);
		horizRootFigBase.setLayoutManager(rootLayout1);
		horizRootFigBase.setOpaque(true);
		
		
		horizRootFigBase.add(verticalRootFigBase);
		verticalRootFigBase.setOpaque(true);
		ToolbarLayout rootLayout = new ToolbarLayout(/*isHoriz*/false);
		rootLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		rootLayout.setStretchMinorAxis(false);
		verticalRootFigBase.setLayoutManager(rootLayout);
		verticalRootFigBase.setBorder(new MarginBorder(new Insets(8)));

		rootFig = new Figure();
		rootFig.setLayoutManager(rootLayout);
		rootFig.setOpaque(true);
		
		verticalRootFigBase.add(rootFig);
		
		IFigure label = new Label(" \n Packages and Classes can be Dragged from the package explorer to create a diagram.");
		nonEmptyFigure = new NonEmptyFigure(label);
		NonEmptyFigureSupport.instructionHighlight(label, 10);
		rootFig.add(nonEmptyFigure);

		warning = new Label("");
		warning.setLayoutManager(new ToolbarLayout());
		NonEmptyFigureSupport.instructionHighlight(warning, 10);
		rootFig.add(this.warning);
		
		contentFig = new Figure();
		FlowLayout layout = new FlowLayout( /*isHorizontal*/false);
		layout.setMajorSpacing(20);
		layout.setStretchMinorAxis(true);
		contentFig.setLayoutManager(layout);
		rootFig.add(contentFig);
		
		figHeading.setOpaque(true);
		FlowLayout flowLayout = new FlowLayout(true);
		figHeading.setLayoutManager(flowLayout);
		flowLayout.setMajorAlignment(FlowLayout.ALIGN_CENTER);
		flowLayout.setStretchMinorAxis(false);
		figHeading.setToolTip(new Label("Select a parent to see package"));

		// add links to Figure 
		LinkUtils.convertToLabelLinks(figHeading, getCommonContextLabel(), "", getRepo(), this);
		rootFig.add(figHeading,0);
		
		
		/*
		 * Making sure that the comments reposition themselves
		 * when the root figure changes its bounds
		 */
		rootFig.addFigureListener(new FigureListener() {
			public void figureMoved(IFigure source) {
				updateCommentsPositioning();
			}
		});
		return horizRootFigBase;
	}
	
	
	/**Code centering strata but when a element is moved outside the bottom bounds the canvas starts resizing itself
	 */
//	Figure rootFigBase;
////	Figure rootFig;
//	@Override
//	protected IFigure createContainerFigure() {
//		/*
//		 * Strata has two root figures (rootFigBase and rootFig) to properly show the diagram in the center.
//		 * The rootFigBase uses a horizontal ToolbarLayout with vertically center aligned children.
//		 * The rootFig is the child of the rootFigBase and uses a vertical ToolBarLayout with
//		 * horizontally center aligned children. To make sure that the rootFig stays at the center of
//		 * the screen its width has be always set to the rootFigBase's width. 
//		 */
////		final Figure rootFigBase = new FreeformLayer();
//		rootFigBase = new FreeformLayer();
//		
//		rootFig = new FreeformLayer() {
//			@Override
//			public void setBounds(Rectangle rect) {
//				super.setBounds(rect);
//				getBounds().width = rootFigBase.getBounds().width;
//			}
//		};
//		
//		rootFigBase.addFigureListener(new FigureListener() {
//			public void figureMoved(IFigure source) {
////				System.err.println("Figmoved");
////				if (source.getBounds() != rootFig.getBounds())
////					source.setBounds(rootFig.getBounds());
//			}
//		});
//		ToolbarLayout rootLayout1 = new ToolbarLayout(/*isHoriz*/true);
//		rootLayout1.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
//		rootLayout1.setStretchMinorAxis(false);
//		rootFigBase.setLayoutManager(rootLayout1);
//		rootFigBase.setOpaque(true);
//		
//		
//		rootFigBase.add(rootFig);
//		rootFig.setOpaque(true);
//		ToolbarLayout rootLayout = new ToolbarLayout(/*isHoriz*/false);
//		rootLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
//		rootLayout.setStretchMinorAxis(false);
//		rootFig.setLayoutManager(rootLayout);
//		rootFig.setBorder(new MarginBorder(new Insets(8)));
//
//		IFigure label = new Label(" \n Packages and Classes can be Dragged from the package explorer to create a diagram.");
//		nonEmptyFigure = new NonEmptyFigure(label);
//		NonEmptyFigureSupport.instructionHighlight(label, 10);
//		rootFig.add(nonEmptyFigure);
//
//		warning = new Label("");
//		warning.setLayoutManager(new ToolbarLayout());
//		NonEmptyFigureSupport.instructionHighlight(warning, 10);
//		rootFig.add(this.warning);
//		
//		contentFig = new Figure();
//		FlowLayout layout = new FlowLayout( /*isHorizontal*/false);
//		layout.setMajorSpacing(20);
//		layout.setStretchMinorAxis(true);
//		contentFig.setLayoutManager(layout);
//		rootFig.add(contentFig);
//		
//		figHeading.setOpaque(true);
//		FlowLayout flowLayout = new FlowLayout(true);
//		figHeading.setLayoutManager(flowLayout);
//		flowLayout.setMajorAlignment(FlowLayout.ALIGN_CENTER);
//		flowLayout.setStretchMinorAxis(false);
//		figHeading.setToolTip(new Label("Select a parent to see package"));
//		// add links to Figure 
//		LinkUtils.convertToLabelLinks(figHeading, getCommonContextLabel(), "", getRootModel(), getRepo(), this);
//		rootFig.add(figHeading,0);
//		
//		return rootFigBase;
//	}
	
	@Override
	public void activate() {
		super.activate();
		NonEmptyFigureSupport.listenToModel(getRootModel(), nonEmptyFigure);

		Value detailLevel = ((RepositoryMgr)getRootArtifact().getRepo()).getStatement(RSECore.createRseUri("DetailNode"), RSECore.detailLevelURI, null).getObject();
		if (detailLevel != null) {
			getRootArtifact().setDetailLevel(Integer.valueOf(((Literal) detailLevel).getLabel()));
		}
		
		StrataEditor editor = getStrataEditor();
		// loadModel only applies to loaded Strata FILES
		Class<?> locClass = null;
		if (jdtUIVer >= 3.3) {
			try {
				locClass = Class.forName("org.eclipse.ui.ide.FileStoreEditorInput");
			} catch (Exception e) {
				System.err.println("Issue with loading: " + e);
			}
		}
		else if (jdtUIVer < 3.3) {
			try {
				locClass = Class.forName("org.eclipse.ui.internal.editors.text.JavaFileEditorInput");
			} catch (Exception e) {
				System.err.println("Issue with loading: " + e);
			}
		}
		if (editor != null && 
				(editor.getEditorInput() instanceof FileEditorInput || 
						(jdtUIVer >= 3.3 && editor.getEditorInput().getClass().equals(locClass))
						|| (jdtUIVer < 3.3 && editor.getEditorInput().getClass().equals(locClass)) 
						|| editor.getEditorInput() instanceof RSEShareableDiagramEditorInput)) { //should only run ONLOAD
			editor.clearDirtyFlag();
			if (editor.getMemRepo() == null)
				StrataEditor.loadModel(StoreUtil.getMemRepository(), getRootModel(), editor);
			else
				StrataEditor.loadModel(editor.getMemRepo(), getRootModel(), editor);
		}
		
		getViewer().getEditDomain().getCommandStack().addCommandStackEventListener(new CommandStackEventListener() {
			public void stackChanged(CommandStackEvent event) {
				switch (event.getDetail()) {
	            case CommandStack.PRE_EXECUTE:
	            case CommandStack.PRE_REDO:
	            case CommandStack.PRE_UNDO:
	                break;
	            case CommandStack.POST_UNDO:
					StrataRootEditPart.this.refreshHeirarchyInUI(true);
	            	break;
	            case CommandStack.POST_EXECUTE:
	            case CommandStack.POST_REDO:
					StrataRootEditPart.this.refreshHeirarchyInUI(false);
	            	break;
				}
			}});
	}
	
    public StrataEditor getStrataEditor() {
        EditDomain editDomain = getRoot().getViewer().getEditDomain();
        if (editDomain instanceof DefaultEditDomain) {
            return (StrataEditor) ((DefaultEditDomain)editDomain).getEditorPart();
        } else {
        	// caller needs to support when we are not opening in an editor
        	return null;
        }
    }

	private Snapshot animationSnapshot = new Snapshot();
	public void updateFigFromSnapshot(AbstractGraphicalEditPart editPart, IFigure fig) {
		animationSnapshot.updateFig(editPart, fig);
	}

	public void refreshHeirarchyInUI(final boolean undoSrc) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
	                try {
	                	StrataRootEditPart.this.animationSnapshot.clear();
	                	StrataRootEditPart.this.animationSnapshot.captureRoot(StrataRootEditPart.this);
	                	
	    				logger.info("Animation.markBegin: " + StrataRootEditPart.this.getArtFrag());
	    				Animation.markBegin();
	    				
	    				StrataRootEditPart.this.refreshHeirarchy();
	    				
	    				//System.err.println("CEP...figureAnimators.cnt: " + Animation.figureAnimators.size());
	    				logger.info("Animation.run");
	    				Animation.run(300);
	    				
	                	StrataRootEditPart.this.animationSnapshot.clear();
	                	
	                	if (!undoSrc) StrataRootEditPart.this.runAutoTasks();
//	                	StrataRootEditPart.this.refreshHeirarchy();
	    				
//	                	PartitionerSupport.partitionLayers(StrataRootEditPart.this.getRootModel(), StrataRootEditPart.this.getArtFrag());
	                	
						// set root to selected to prevent bugs with items still
						// being selected after move and delete commands
	                	setSelected(SELECTED);
	                } catch (Throwable t) {
	                    logger.error("Unexpected Error", t);
	                }
				}
			});
	}

	/*
	 * Whenever the content figure is changed this figure is called to 
	 * change the top left location of all the comments respective to the 
	 * root figure and then store the new top left location of the content figure.
	 */
	protected void updateCommentsPositioning() {
		StrataRootDoc doc = (StrataRootDoc) getModel();
		Point diagramTL = contentFig.getBounds().getTopLeft();
		for(Comment comment: doc.getCommentChildren()) {
//			if (comment.isAnchored()) continue;
			Point relDistFromRoot = comment.getRelDistFromDiagTopLeft();
			if (relDistFromRoot == null) continue;
			// Set Comment new relative location
			int xDiff = diagramTL.x - relDistFromRoot.x; 
			int yDiff = diagramTL.y - relDistFromRoot.y;
			PointPositionedDiagramPolicy.setLoc(comment, new Point(xDiff, yDiff));
		}		
	}

	@Override
	public void refresh(){
		super.refresh();
		RootEditPartUtils.refresh(getModel(), this, this);
	}
	
	@Override
	public void refreshHeirarchy() {
		//System.err.println("****** refreshHeirarchy ******");
		super.refreshHeirarchy();
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();

		if (getViewer().getControl() != null && (getViewer().getControl().getStyle() & SWT.MIRRORED) == 0)
			((ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER)).setAntialias(SWT.ON);

		// and add links
		// make sute to display names correctly when we are a parent added from
		// the context labels and only have a single child
		if (getArtFrag().getShownChildren().size()==1) {
			String commonCntxtLbl = getCommonContextLabel();
			
			// this may not be needed but was throwing errors when strata was in a strange state
			if (commonCntxtLbl == null) {
				commonCntxtLbl = "";
				return;
			}
			if (commonCntxtLbl.contains(".*"))
				commonCntxtLbl=commonCntxtLbl.replace(".*", "");
			if (commonCntxtLbl.contains("."))
				commonCntxtLbl = commonCntxtLbl.substring(0,  commonCntxtLbl.lastIndexOf("."));
			else 
				commonCntxtLbl = "";
			LinkUtils.convertToLabelLinks(figHeading, commonCntxtLbl, "", getRepo(), this);
		}
			
		else
			LinkUtils.convertToLabelLinks(figHeading, getCommonContextLabel(), "", getRepo(), this);
	}

	public Figure getRootFig() {
		return rootFig;
	}
	
	@Override
	public IFigure getContentPane() {
		return contentFig;
	}

    public RootArtifact getRootArtifact() {
    	return (RootArtifact) this.getModel();
    }

    @Override
	public StrataRootDoc getRootModel() {
		return (StrataRootDoc) getModel();
	}
	
	@Override
	public ReloRdfRepository getRepo() {
		return getRootModel().getDepNdx().getRepo();
	}
	
	public StrataRootEditPart() {
	}
	
	@Override
	public DepNdx getDepCalculator() {
		return getRootModel().getDepNdx();
	}

	@Override
    public boolean isSelectable() {
		return false;
	}


	public void runAutoTasks() {
		logger.info("SCEP.runAutoTasks");
		autoRemoveLayers();
		if (StrataEditor.doNotBreakLoadedAFs) return;
		List<ArtifactFragment> abTargets = getAutoBreakTargets();
		if (!abTargets.isEmpty()) 
			autoBreak(abTargets );
		if (checkForAutoShowChildren()) 
			autoOpen();
		
	}
	
	// auto remove Layers is not in the command stack because we want the
	// process of removing layers when moving items around to be invisible to
	// the user, unless they are specifically deleting stuff. 
	public void autoRemoveLayers() {
		@SuppressWarnings("unchecked")
		List<ContainableEditPart> epChildren = getChildren();
		for (ContainableEditPart containableEP : epChildren) {
			if (containableEP instanceof LayerEditPart)
				((LayerEditPart) containableEP).removeEmptyLayers();
			if (containableEP instanceof CompositeLayerEditPart)
				((CompositeLayerEditPart) containableEP).removeSingleCompositeLayers();
		}
		List<Layer> currLayers = new ArrayList<Layer>();
		for (ContainableEditPart containableEP : epChildren) {
			currLayers.add((Layer) containableEP.getModel());
		}
		refreshHeirarchy();
	}

	ArtifactFragment tmpAF = null;
	private boolean checkForAutoShowChildren() {
        // is there a need for open?
		ArtifactFragment rootAF = this.getArtFrag();
        if (ClosedContainerDPolicy.getShownChildren(rootAF).isEmpty()) return false;
        if (getVisibleSize(rootAF)-1 > 3) return false;    // -1 to remove the top level
        
        // will it do anything?
        ArtifactFragment maxAFChild =  getChildWithMaxAF();
        if (tmpAF == maxAFChild) return false;
        if (tmpAF==null) tmpAF = maxAFChild;
        //if (!maxAFChild.containsChildren()) return false;
        if (ContainedClassSizeCacheDPolicy.get(maxAFChild, getRepo()) >0) return true;
        
		return false;
	}
    private static int getVisibleSize(ArtifactFragment strataAF) {
        int openSize = 1;
        if (!(strataAF instanceof ArtifactFragment)) return openSize;
        if (ClosedContainerDPolicy.isShowingChildren(strataAF)) {
            List<ArtifactFragment> children = ClosedContainerDPolicy.getShownChildren(strataAF);
            for (ArtifactFragment obj : children) {
                openSize += getVisibleSize(obj);
            }
        }
        return openSize;
    }

	private void autoOpen() {
        ArtifactFragment openChildAF = getChildWithMaxAF();
        StrataArtFragEditPart openChildSAFEP = (StrataArtFragEditPart) this.findEditPart(openChildAF);
        if (openChildSAFEP != null && !ClosedContainerDPolicy.isShowingChildren(openChildAF) && !ClosedContainerDPolicy.isUserHidden(openChildAF))
            ModelUtils.showArtFragChildren(openChildAF, getCommandStack());
	}
    private ArtifactFragment getChildWithMaxAF() {
    	ArtifactFragment rootAF = this.getArtFrag();
        List<ArtifactFragment> rootChildren = ClosedContainerDPolicy.getShownChildren(rootAF);
        ArtifactFragment maxAF = rootChildren.get(0);
        int maxCCS = -1;
        for (ArtifactFragment strataAF : rootChildren) {
            int curSize = ContainedClassSizeCacheDPolicy.get(strataAF, getRepo());
            if (curSize > maxCCS) {
                maxCCS = curSize;
                maxAF = strataAF;
            }
        }
        return maxAF;
    }
    @SuppressWarnings("unused")
	private boolean checkForAutoBreak() {
        //logger.error("checkForAutoBreak");
        /*
        for (ArtFrag autoBreakAF : getAFTargets()) {
            if (autoBreakAF.isOpen()) return true;
            if (autoBreakAF.openable()) return true;
        }
        logger.error("checkForAutoBreak: " + true);
        return true;
        */
        return !getAutoBreakTargets().isEmpty();
    }
    private List<ArtifactFragment> getAutoBreakTargets() {
        List<ArtifactFragment> afTargets = new ArrayList<ArtifactFragment> ();
        for (ArtifactFragment strataAF : getAllArtFrag()) {
        	if (strataAF instanceof Layer) continue; // do not break layers

            if(!ClosedContainerDPolicy.isBreakable(strataAF)) continue; // do not break artFrags that were specifically shown by the user
            
            if (strataAF.getParentArt() == null) continue; // do not break items with a null parent (root, strange states)

            if(strataAF instanceof StrataRootDoc) continue; // do not break root <-- will likely never hit here (because of previous line)

            if (!ClosedContainerDPolicy.isShowingChildren(strataAF)) continue; // do not break closed items
            
            List<ArtifactFragment> strataAFChildren = ClosedContainerDPolicy.getShownChildren(strataAF);

            if (strataAFChildren.size() == 0) continue; // do not break if we have 0 children
            
			// if we are breaking something with 50+ children it might take some
			// time (30sec to 1 min). Do not break since this is not that
			// helpful.
            if (strataAFChildren.size() > 50) continue;

            if (ClosedContainerDPolicy.getShownChildren(strataAF.getParentArt()).size() > 1) { // if we have siblings
				if (strataAFChildren.size() > 1) continue; // ...and more than 1 child - do not break
			}

            afTargets.add(strataAF);
        }
        /*
        ArtifactFragment rootAF = this.getModuleArtFrag();
        if (rootAF.getChildren().size() != 1) 
            afTargets.add(rootAF.getChildren().get(0));
         */
        return afTargets;
    }

	private void autoBreak(List<ArtifactFragment> abTargets) {
		execute(new MultiBreakCommand(abTargets, getRepo(), getRootModel()));
	}

	@Override
	public void deactivate() {
		BuildStatus.updateDiagramItemMap(getViewer().getEditDomain()
				.getCommandStack().toString(), getAllArtFrag().size());
		super.deactivate();
	}
	
	public List<ArtifactFragment> getAllArtFrag() {
		Collection<?> allModelObjects = getRootController().getViewer().getEditPartRegistry().keySet();
		List<ArtifactFragment> allAF = new ArrayList<ArtifactFragment>();
		for (Object modelObj : allModelObjects) {
			if (modelObj instanceof ArtifactFragment)
				allAF.add((ArtifactFragment) modelObj);
		}
		return allAF;
	}

	public List<StrataArtFragEditPart> getAllArtFragEP() {
		Collection<?> allModelObjectEPs = getRootController().getViewer().getEditPartRegistry().values();
		List<StrataArtFragEditPart> allAFEP = new ArrayList<StrataArtFragEditPart>();
		for (Object modelObj : allModelObjectEPs) {
			if (modelObj instanceof StrataArtFragEditPart)
				allAFEP.add((StrataArtFragEditPart) modelObj);
		}
		return allAFEP;
	}

	public static final String DN_ARR_PROP = "DownwardArrowVisibility";
	public static final String UP_ARR_PROP = "UpwardArrowVisibility";

	public static final Integer ArrState_ShowAlways_Ndx = 0;
	public static final Integer ArrState_ShowOnHover_Ndx = 1;
	public static final Integer ArrState_DontShow_Ndx = 2;
	private static final String[] arrStateLabels = new String[] {
		"Show Always", 
		"Show on Hover", 
		"Don't Show" };

	private static IPropertyDescriptor[] descriptors = new IPropertyDescriptor[] { 
				new ComboBoxPropertyDescriptor(DN_ARR_PROP, "Downward Arrows", arrStateLabels),
				new ComboBoxPropertyDescriptor(UP_ARR_PROP, "Upward Arrows", arrStateLabels),
		};

	public Map<String, Integer> propertyState = new HashMap<String, Integer>();
	
	{
		initDefaultProperties();
	}

	private void initDefaultProperties() {
		propertyState.put(DN_ARR_PROP, ArrState_ShowOnHover_Ndx);
		propertyState.put(UP_ARR_PROP, ArrState_ShowOnHover_Ndx);
	}

	// not sure why this is needed
	public Object getEditableValue() {
		return this;
	}

	@Override
	public IPropertyDescriptor[] getProperties() {
		return descriptors;
	}

	@Override
	public Object getPropertyValue(Object id) {
		return propertyState.get(id);
	}

	public boolean isPropertySet(Object id) {
		return false;
	}

	public void resetPropertyValue(Object id) {
	}

    private PropertyChangeSupport pcSupport = new PropertyChangeSupport(this);
	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcSupport.addPropertyChangeListener(l);
	}
	public void removePropertyChangeListener(PropertyChangeListener l) {
		pcSupport.removePropertyChangeListener(l);
	}
	@Override
	public void setPropertyValue(Object id, Object value) {
		Integer oldValue = propertyState.get(id);
		propertyState.put((String) id, (Integer) value);
		pcSupport.firePropertyChange((String)id, oldValue, value);
	}

    public ImageDescriptor getIconDescriptor(NavAidsEditPart aep, Object relModel) {
        // @tag unify-core
        if (!(relModel instanceof Artifact) && !(relModel instanceof ArtifactFragment)) {
            logger.error("Don't know how to get icon for: " + relModel + " // " + relModel.getClass());
            return null;
        }
        Artifact art = relModel instanceof Artifact ? (Artifact)relModel : ((ArtifactFragment)relModel).getArt();
        return PluggableEditPartSupport.getIconDescriptor(getRepo(), art, art.queryType(aep.getRepo()));
    }

    // @tag unify-core: implementation basically same as from ReloController (all three methods below)
    
    public boolean modelCreatable(Object model) {
        if (!(model instanceof Artifact)) 
            return false;
        else {
            Artifact art = (Artifact) model;
            Resource artType = art.queryType(getRepo());
            if (artType==null) return false;
            if (!AbstractJDTBrowseModel.isLoadableJavaArtifact(art, artType, getRepo())) return false;
            return true;
        }
    }
	public boolean canAddRel(ArtifactFragment srcAF, DirectedRel rel, Artifact dstArt) {
        if (findEditPart(dstArt) != null) return true;
        if (!modelCreatable(dstArt)) return false;
        return true;
	}
	
	// is not used in strata
	public ArtifactRel addRel(ArtifactFragment srcArtFrag, URI relationRes, ArtifactFragment dstArtFrag) {
//		DependencyRelation depRel = this.getRootModel().getDepRel(srcArtFrag, dstArtFrag);
//
//		if (depRel == null) return null;
//
//		List<DependencyRelation> newRel = new ArrayList<DependencyRelation>(1);
//		newRel.add(depRel);
//		this.getRootModel().addRelationships(newRel);
//		return depRel;
		
		return null;
	}
	public void hideRel(ArtifactRel depRel) {
		List<ArtifactRel> relList = new ArrayList<ArtifactRel>(1);
		relList.add(depRel);
		this.getRootModel().removeRelationships(relList);
	}

    // we return the correct model
    public ArtifactFragment createOrFindQueriedArtifactEditPart(Artifact art) {
        ArtifactFragment rootAF = this.getArtFrag();
        ArtifactFragment strataAF = this.getRootModel().createArtFrag(art);
        if (!ClosedContainerDPolicy.getShownChildren(rootAF).contains(strataAF)) rootAF.appendShownChild(strataAF);
        
        return strataAF;
    }

    public WorkbenchPart getWorkbenchPart() {
        EditDomain editDomain = getRoot().getViewer().getEditDomain();
        if (editDomain instanceof DefaultEditDomain) {
            return (StrataEditor) ((DefaultEditDomain)editDomain).getEditorPart();
        } else if (editDomain instanceof ViewEditDomain) {
            return (StrataView) ((ViewEditDomain)editDomain).getViewPart();
        } else {
            logger.error("Unable to understand EditDomain - Type: " + editDomain.getClass(), new Exception());
            return null;
        }
    }

	public List<EditPart> commentChildren= new ArrayList<EditPart>();
	public List<EditPart> getCommentEPChildren(){
		return commentChildren;
	}

	public void setCommentEPChildren(List<EditPart> children) {
		commentChildren = children;
	}

	private Label warning;
	public void addUnbuiltWarning() {
		warning.setText(" \n  Some elements in this diagram may not have been indexed." +
				"\n  To update your index, go to the menu: Architexa->Update Indexes");
	}

   
}
