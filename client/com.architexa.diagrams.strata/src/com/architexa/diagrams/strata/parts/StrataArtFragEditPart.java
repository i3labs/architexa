/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.InstanceofPredicate;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.draw2d.ColorUtilities;
import com.architexa.diagrams.draw2d.FigureUtilities;
import com.architexa.diagrams.draw2d.RoundedShadowBorder;
import com.architexa.diagrams.eclipse.gef.MenuButton;
import com.architexa.diagrams.eclipse.gef.UndoableLabelSource;
import com.architexa.diagrams.jdt.IJavaElementContainer;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.RJMapToId;
import com.architexa.diagrams.jdt.model.CUSupport;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.model.UserCreatedFragment;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.ColorDPolicy;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.parts.AnnoLabelCellEditorLocator;
import com.architexa.diagrams.parts.AnnoLabelDirectEditPolicy;
import com.architexa.diagrams.parts.BasicRootController;
import com.architexa.diagrams.parts.NavAidsEditPart;
import com.architexa.diagrams.parts.NavAidsEditPolicy;
import com.architexa.diagrams.parts.NavAidsSpec;
import com.architexa.diagrams.relo.jdt.services.PluggableEditPartSupport;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.cache.Partitioner;
import com.architexa.diagrams.strata.commands.BreakCommand;
import com.architexa.diagrams.strata.commands.DeleteCommand;
import com.architexa.diagrams.strata.commands.ModelUtils;
import com.architexa.diagrams.strata.commands.ShowAllDirectRelationCommand;
import com.architexa.diagrams.strata.commands.UndoableAutoDelLayerCommand;
import com.architexa.diagrams.strata.figures.ContainerFigure;
import com.architexa.diagrams.strata.figures.LinkUtils;
import com.architexa.diagrams.strata.model.CompositeLayer;
import com.architexa.diagrams.strata.model.EmbeddedFrag;
import com.architexa.diagrams.strata.model.Layer;
import com.architexa.diagrams.strata.model.StrataFactory;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.ContainedClassSizeCacheDPolicy;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.diagrams.strata.ui.BreakAction;
import com.architexa.diagrams.strata.ui.ColorScheme;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.diagrams.ui.MultiAddCommandAction;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.utils.DbgRes;
import com.architexa.diagrams.utils.MoreButtonUtils;
import com.architexa.org.eclipse.draw2d.AbstractBorder;
import com.architexa.org.eclipse.draw2d.ChopboxAnchor;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.ConnectionAnchor;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.LayoutManager;
import com.architexa.org.eclipse.draw2d.LineBorder;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.RoundedBorder;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.DefaultEditDomain;
import com.architexa.org.eclipse.gef.EditDomain;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartListener;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.editpolicies.ComponentEditPolicy;
import com.architexa.org.eclipse.gef.requests.CreateConnectionRequest;
import com.architexa.org.eclipse.gef.requests.GroupRequest;
import com.architexa.org.eclipse.gef.tools.CellEditorLocator;
import com.architexa.org.eclipse.gef.tools.LabelDirectEditManager;
import com.architexa.org.eclipse.gef.tools.LabelSource;
import com.architexa.store.ReloRdfRepository;


public class StrataArtFragEditPart extends TitledArtifactEditPart implements NavAidsEditPart, IJavaElementContainer, LabelSource, UndoableLabelSource {

	public static final Logger logger = StrataPlugin.getLogger(StrataArtFragEditPart.class);
	
	public StrataArtFragEditPart() {}
	
	public final Iterator<?> dbg_getEPL() {
		return super.getEventListeners(EditPartListener.class);
	}
	
	// needed so that labels center correctly on figures when there are no contents
	@Override
	protected void addChildVisual(EditPart childEditPart, int index) {
		if (StrataRootDoc.stretchFigures && !getFigure().getChildren().contains(getContentPane()))
			getFigure().add(getContentPane());
		super.addChildVisual(childEditPart, index);
	}
	
	// removing content pane becomes a problem
	// TODO: figure out why we removed the content pane in the first place
	//@Override
	//protected void removeChildVisual(EditPart childEditPart) {
	//	if (StrataRootDoc.stretchFigures && getFigure().getChildren().contains(getContentPane()))
	//		getFigure().remove(getContentPane());
	//	super.removeChildVisual(childEditPart);
	//}
	
	
	protected DbgRes tag = new DbgRes(ArtifactFragment.class, this, "EP");
	private static String MORE_BUTTON_TEXT = " Member";
	private static String MORE_BUTTON_TOOLTIP_TEXT = " Member";

	@Override
    public String toString() {
		return super.toString() + tag;
	}
	
	public boolean isPckgEP() {
		return CodeUnit.isPckgDirType(this.getRepo(), this.getArtFrag().getArt());
	}

	private IJavaElement cachedIJE = null; 
	public IJavaElement getJaveElement() {
		if (cachedIJE == null)
			cachedIJE = RJCore.resourceToJDTElement(this.getRepo(), getContainedArtifact().elementRes);
		return cachedIJE;
	}
	public Artifact getContainedArtifact() {
		return this.getArtFrag().getArt();
	}

	public Image getIcon(ReloRdfRepository repo) {
		// if we are user created make sure we are initiallized so we show the
		// right icon
		if (getArtFrag() instanceof UserCreatedFragment) {
			if (!getArtFrag().getArt().isInitialized(repo)) {
				repo.startTransaction();
				repo.addStatement(getArtFrag().getArt().elementRes, RSECore.initialized, true);
				repo.commitTransaction();
			}
			return CodeUnit.getIcon(repo, getArtFrag().getArt(), getArtFrag().queryType(repo));
		}

		if (getArtFrag() instanceof EmbeddedFrag) {
			// TODO: Make sure cached image for embedded frags works as expected
			// URL url = StrataPlugin.getDefault().getBundle().getEntry("icons/moduleColl.png");
	        // return ImageCache.calcImageFromDescriptor(ImageDescriptor.createFromURL(url));
	        return ImageCache.moduleColl;
		} else
			return ImageCache.calcImageFromDescriptor(PluggableEditPartSupport.getIconDescriptor(getRepo(), getArtFrag().getArt(), getArtFrag().queryType(repo)));
	}


	@Override
	protected IFigure createContainerFigure() {
		String sizeTxt = "";
		int containedSz = ContainedClassSizeCacheDPolicy.get(getArtFrag(), getRepo());
		if (containedSz != -1) {
			sizeTxt = "( " + containedSz + " items )";
		}
		
		String label = getLabel();
		String toolTip = label + " " + sizeTxt;
		Color highlightColor = ColorDPolicy.getColor(getArtFrag());
		
		Image icon = null;
		if (com.architexa.diagrams.ColorScheme.SchemeV1) {
			icon = getIcon(getRepo());
		} 
		ContainerFigure fig = new ContainerFigure(icon, label, toolTip, getNestingLevel(), highlightColor);
		refreshVisuals(fig);
		return fig;
	}
	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		refreshVisuals((ContainerFigure) this.getFigure());
		
		
		ContainerFigure containerFig = (ContainerFigure) getFigure();
		Color myBack = ColorScheme.containerColors[getNestingLevel()%10];

		if (ColorDPolicy.isDefaultColor(getModel(), myBack)) { 
			containerFig.setBackgroundColor(myBack);
			containerFig.setMyBack(myBack);
		}
		Image icon = null;
		AbstractBorder abstractBorder = containerFig.getAbstractBorder();
		if (com.architexa.diagrams.ColorScheme.SchemeV1 ) {
			icon = getIcon(getRepo());
			containerFig.setBorder(null);
			abstractBorder = new RoundedBorder(myBack, 4);
			containerFig.setAbstractBorder(abstractBorder);
			FigureUtilities.insertBorder(containerFig, abstractBorder);
			FigureUtilities.insertBorder(containerFig, new RoundedShadowBorder(ColorUtilities.darken(myBack, 0.5), 2));
		} else { 
			if (abstractBorder instanceof RoundedBorder) {
				containerFig.setBorder(null);
				abstractBorder = new LineBorder(ColorConstants.black, 1);
				containerFig.setAbstractBorder(abstractBorder);
				LineBorder padding = new LineBorder(ColorConstants.white, 5);
				containerFig.setBorder(padding);
				FigureUtilities.insertBorder(containerFig, abstractBorder);

			}
		}
		containerFig.icnLbl.setIcon(icon);
	}

	private final void refreshVisuals(ContainerFigure myFig) {
		TitledArtifactEditPart parentTAFEP = this.getParentTAFEP();
		String contextLbl = null;
		if (parentTAFEP != null) 
			contextLbl = getCntxLabel(parentTAFEP);
		if (contextLbl == null)
			contextLbl = getContextualizedLabel();
		
		
		Font font = JFaceResources.getDialogFont();
		int containedSz = ContainedClassSizeCacheDPolicy.get(getArtFrag(), getRepo());
		if (ClosedContainerDPolicy.isShowingChildren(this.getArtFrag()))
			font = myFig.setSizeRatio(1.0);
		else
			font = myFig.setSizeRatio(1.0 + 1.0*containedSz/this.getRootModel().maxSize);
		
		if (!myFig.getNameFig().getChildren().isEmpty() && (getArtFrag() instanceof UserCreatedFragment)) {
			Label annoLabelFig = (Label) getAnnoLabelFigure(myFig);
			myFig.getNameFig().removeAll();
			myFig.getNameFig().add(new Label(annoLabelFig.getText()));
		} else
			LinkUtils.convertToLabelLinks(myFig.getNameFig(), getContextualizedLabel(), contextLbl, getRepo(), this, font);
	}

	@Override
	public Command getCommand(Request request) {
		if (request.getType() == BreakAction.BreakRequest) {
			if (!ClosedContainerDPolicy.isShowingChildren(this.getArtFrag())) return null;
			// try catch here to double check that no SWT handle errors occur here
			try {
				if (!isPckgEP()) return null;
			} catch (NullPointerException e) {
				return null;
			}

			return createBreakCommand(StrataArtFragEditPart.this);
		}
        if (request.getType().equals(RequestConstants.REQ_DELETE)) {
            if (!(request instanceof GroupRequest)) {
                GroupRequest newReq = new GroupRequest(request.getType());
                newReq.setEditParts(this);
                request = newReq;
            }
        }
        if (request.getType() == RequestConstants.REQ_OPEN) {
            final StrataArtFragEditPart mySAFEP = this;
			// do NOT OPEN user created frags, they can only be opened by adding
			// items to them. Double clicking will edit the text
            if (getArtFrag() instanceof UserCreatedFragment) 
            	return getDirectEditCommand();
            
			// do not select opened package/class by default: this will cause
			// too many rels to show and not be useful
            
        	return getTogglePackageCommand(mySAFEP);
        }
		return super.getCommand(request);
	}
	
	// used for opening / closing a package. Either by double clicking
	// (getCommand above) or by context menu action (StrataViewEditorCommon)
    public Command getTogglePackageCommand(StrataArtFragEditPart mySAFEP) {
    	if (ClosedContainerDPolicy.isShowingChildren(mySAFEP.getArtFrag())){
        	CompoundCommand hideAFEPChildrenCmd = new CompoundCommand("Hide Children");
    		ClosedContainerDPolicy.hideChildren(hideAFEPChildrenCmd, mySAFEP.getArtFrag());
        	return hideAFEPChildrenCmd;
        }
        else {
			CompoundCommand showAFEPChildrenCmd = new CompoundCommand("Show Children");
			setSelected(SELECTED_NONE);
			ClosedContainerDPolicy.queryAndShowChildren(showAFEPChildrenCmd, mySAFEP.getArtFrag());
	        ClosedContainerDPolicy.showAllSingleChildren(showAFEPChildrenCmd, mySAFEP.getArtFrag());
	        return showAFEPChildrenCmd;
        }
	}

	protected static Command createBreakCommand(StrataArtFragEditPart strataAFEPToBreak) {
    	TitledArtifactEditPart parentAFEP = strataAFEPToBreak.getParentTAFEP();
    	if (parentAFEP == null) return null;
        return createBreakCommand(strataAFEPToBreak.getArtFrag(), parentAFEP.getArtFrag());
    }

	protected static Command createBreakCommand(final ArtifactFragment strataAFToBreak, final ArtifactFragment parentAF) {
        logger.info("createBreakCommand: " + strataAFToBreak + " parentAF: " + parentAF);
		return new BreakCommand(parentAF, strataAFToBreak);
	}
	
	@SuppressWarnings("unchecked")
	protected static void moveChild(GraphicalEditPart oldParentEP, GraphicalEditPart newParentEP, GraphicalEditPart childEP, int newEPNdx) {
		// Save the constraint of the child so that it does not
		// get lost during the remove and re-add.
		IFigure childFigure = childEP.getFigure();
		LayoutManager layout = oldParentEP.getContentPane().getLayoutManager();
		Object constraint = null;
		if (layout != null)
			constraint = layout.getConstraint(childFigure);

		// // // super.reorderChild(child, index);

		//oldParentEP.removeChildVisual(childEP);	
		oldParentEP.getContentPane().remove(childFigure);

		oldParentEP.getChildren().remove(childEP);
		newParentEP.getChildren().add(newEPNdx, childEP);

		//newParentEP.addChildVisual(childEP, index);
		newParentEP.getContentPane().add(childFigure);
		
		newParentEP.setLayoutConstraint(childEP, childFigure, constraint);
	}
    
    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(NavAidsEditPolicy.HANDLES_ROLE, new NavAidsEditPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentEditPolicy() {
			@Override
			protected Command createDeleteCommand(GroupRequest deleteRequest) {
                try {
    				if (StrataArtFragEditPart.this.getParentTAFEP() == null) return null;
    				
    				ArtifactFragment parentAF = StrataArtFragEditPart.this.getParentTAFEP().getArtFrag();
					
    				List<Layer> orgLayers = LayersDPolicy.getLayersCopy(parentAF, getRepo());
					int layerNdx = Partitioner.getLayerNdx(orgLayers, StrataArtFragEditPart.this.getArtFrag());
					
    				ArtifactFragment parentLayer = orgLayers.get(layerNdx);
    				ArtifactFragment af = StrataArtFragEditPart.this.getArtFrag();

    				CompoundCommand cc = new CompoundCommand("Delete");
					cc.add(StrataArtFragEditPart.this.createDeleteCommand(parentAF,af));
					// Always autoDelete Layers incase we are deleting all the contents of a layer and need to remove it in an undoable way
					// remove deleted afs from org layer so undo adds Afs to an empty layer instead of adding AFs twice
					if (layerNdx >= 0)
						parentLayer.removeShownChild(af);
					cc.add(new UndoableAutoDelLayerCommand(getRootController(), orgLayers, parentAF));
					
    				return cc;
    				
                } catch (Throwable t) {
                    logger.error("Unexpected error. Working with: " + StrataArtFragEditPart.this, t);
                    return null;
                }
			}
		});
		// make sure policies are installed, even for user created frags
		installDirectEditPolicy();
		if (getArtFrag() instanceof UserCreatedFragment) {
			ArtifactFragment.ensureInstalledPolicy(getArtFrag(), ClosedContainerDPolicy.DefaultKey, ClosedContainerDPolicy.class);
			ClosedContainerDPolicy.setBreakable(getArtFrag(), false);
		}
    }

	// this is useful for making sure general connections go to the title of the
	// open packages, but this can cause problems when moving and does not
	// always look better
//    @Override
//    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
//    	// Anchor General Connection at the label so it originates at top or center of this
//    	if(isGeneralConn(connection))
//    		return new ChopboxAnchor(getAnnoLabelFigure());
//    	return super.getSourceConnectionAnchor(connection);
//    }
//    @Override
//    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
//    	// Anchor General Connection at the label so it terminates at top or center of this
//    	if(isGeneralConn(connection))
//    		return new ChopboxAnchor(getAnnoLabelFigure());
//    	return super.getTargetConnectionAnchor(connection);
//    }
//    private boolean isGeneralConn(ConnectionEditPart connection) {
//    	return connection.getModel() instanceof NamedRel && 
//    	RSECore.namedRel.equals(((NamedRel)connection.getModel()).relationRes);
//    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(CreateConnectionRequest request) {
    	// Anchor feedback line at the label so it originates at the top or center of this
    	return new ChopboxAnchor(getAnnoLabelFigure());
    }
    @Override
    public ConnectionAnchor getTargetConnectionAnchor(CreateConnectionRequest request) {
    	if(!validTarget(request)) return null;
    	// Anchor feedback line at the label so it terminates at the top or center of this
    	return new ChopboxAnchor(getAnnoLabelFigure());
    }

    @SuppressWarnings("unchecked")
    public List<NavAidsSpec> getMultiSelectHandlesSpecList(NavAidsEditPolicy policy) {
        return Collections.EMPTY_LIST;
    }
    public String getRelModelLabel(Object model) {
        // if(!(model instanceof Artifact)) return model.toString();
        // return CodeUnit.getLabel(getRepo(), (Artifact)model, this.getArtFrag().getArt());
    	// show context always in strata until better solution is found
    	return CodeUnit.getLabel(getRepo(), (Artifact)model, this.getArtFrag().getArt(), true);
    }
    @SuppressWarnings("unchecked")
    public List<AbstractGraphicalEditPart> getSelectedAGEP() {
        List<Object> allChildren = new ArrayList<Object> (getViewer().getEditPartRegistry().values());
        CollectionUtils.filter(allChildren, new InstanceofPredicate(StrataArtFragEditPart.class));
        List<AbstractGraphicalEditPart> selectedParts = new ArrayList<AbstractGraphicalEditPart>();       
        for(Object child : allChildren) {
            if (!(child instanceof AbstractGraphicalEditPart)) continue;
            AbstractGraphicalEditPart part = (AbstractGraphicalEditPart) child;
            if (part.getSelected() == EditPart.SELECTED_NONE) continue;
            selectedParts.add(part);
        }    
        return selectedParts;
    }

    public List<Artifact> listModel(ReloRdfRepository repo, DirectedRel rel, Predicate filter) {
    	if (rel instanceof DirectedRel && ((DirectedRel)rel).res.equals(RJCore.containmentBasedRefType)) {
    		return this.getRootModel().getDepNdx().queryContainedList(this.getArtFrag().getArt(), rel, filter);
    	}
    	return this.getArtFrag().getArt().queryArtList(repo, rel, filter);
    }
    public List<Artifact> showableListModel(ReloRdfRepository repo, DirectedRel rel, Predicate filter) {
    	Resource aepType = getArtFrag().getArt().queryType(getRepo());    
    	BasicRootController rc = getRootController();
    	List<Artifact> filteredList = new ArrayList<Artifact>();
    	//do not filter here using Filters.getTypeFilter(repo, aepType) since type filtering happens below and we do not want to remove interfaces from clas navaids
    	Set<Artifact> artList = getRootModel().getDepNdx().queryContainedSet(getArtFrag().getArt() , rel , null);
    	
    	for (Artifact relArt : artList) {
    		if (!rc.canAddRel(getArtFrag(), rel, relArt)) continue;
    		
    		boolean aepIsInit = getArtFrag().getArt().isInitialized(repo);
    		boolean relArtIsInit = relArt.isInitialized(repo);
    		
			// allow classes to add interface connections and visversa.
			// Otherwise make sure connections are of the same type as the added
    		if (!CUSupport.isGraphNode(relArt.queryType(getRepo()), relArt, getRepo()) || !CUSupport.isGraphNode(aepType,  getArtFrag().getArt(), getRepo())) 
				if (aepIsInit && relArtIsInit && !aepType.equals(relArt.queryType(getRepo()))) continue; // different type
    		
    		List<ArtifactFragment> shownChild = rc.getRootArtifact().getMatchingNestedShownChildren(relArt);
    		if (!shownChild.isEmpty()) {
    			if (ClosedContainerDPolicy.isShowingChildren(shownChild.get(0).getParentArt()))
    				continue; // already in diagram
    		}

    		// do not add library code
    		if (relArt.queryParentArtifact(getRepo())==null) continue;
    		if (getRelModelLabel(relArt).contains("$")) continue;
    		
    		filteredList.add(relArt);
    	}

    	Collections.sort(filteredList, new Comparator<Artifact>() {
			public int compare(Artifact art1, Artifact art2) {
				if (art1 == null || art2 == null) return 0;
				return getRelModelLabel(art1).compareTo(getRelModelLabel(art2));
			}
		});
    	return filteredList;
    }
    public Resource getElementRes() {
        return this.getArtFrag().getArt().elementRes;
    }
    
    public MultiAddCommandAction getShowRelAction(final BasicRootController rc, final DirectedRel rel, final Object relArt, final String relArtLbl) {
		MultiAddCommandAction action = new MultiAddCommandAction(relArtLbl, getRootController()) {
			@SuppressWarnings("unchecked")
			@Override
			public Command getCommand(Map<Artifact, ArtifactFragment> addedArtToAFMap) {
				List<Artifact> relArts = new ArrayList<Artifact>();
				CompoundCommand cmd = new CompoundCommand();
				if (relArt instanceof Artifact) //not List
						relArts.add((Artifact)relArt);	
				else if (relArt instanceof List<?>) 
					relArts = (List<Artifact>) relArt;
				
				for (Artifact artToAdd : relArts) {
					addArtAndContainers(cmd, addedArtToAFMap, (StrataRootEditPart) rc, getRepo(), artToAdd);
				}
				return cmd;
			}
		};
		
    	return action;
    }

	/**
	 * Adds relArt and its containers, adds them to the model if they are not
	 * there and makes sure that they are shown.
	 * 
	 * Checks all existing root elements to see if they belong in any of the new containers and moves them if necessary
	 * @param breakable - lets ClosedContainerDPolicy know whether or not to remove packages if this parent only has one child. 
	 *                    This is set to false when adding from the 'more' button
	 */
    
    public static void addArtAndContainers(CompoundCommand cc, Map<Artifact, ArtifactFragment> addedArtToAFMap, StrataRootEditPart rc, ReloRdfRepository repo, Artifact art) {
		addArtAndContainers(cc, addedArtToAFMap, art, true, rc.getRootModel(), rc, null);
	}

    public static void addArtAndContainers(CompoundCommand compoundAddCmd, Map<Artifact, ArtifactFragment> addedArtToAFMap, Artifact relArt, boolean showChildren, StrataRootDoc rootModel, StrataRootEditPart rc, Map<Resource, Resource> mapRDFtoProjRes) {
    	if (addedArtToAFMap == null) addedArtToAFMap = new HashMap<Artifact, ArtifactFragment>();
		addedArtToAFMap.put(rootModel.getArt(), rootModel);
		
		LinkedHashMap<Artifact, Artifact> toAddParentToChildMap = new LinkedHashMap<Artifact, Artifact>();
    	List<Artifact> parentList = new ArrayList<Artifact>();
    	
    	ReloRdfRepository repo = rootModel.getRepo();
    	if (relArt.queryWarnedType(repo).equals(RJCore.methodType) || relArt.queryWarnedType(repo).equals(RJCore.fieldType) )
    		relArt = relArt.queryParentArtifact(repo);
    	
    	if (rc!=null)
			getArtsToAdd(rc, repo, relArt, toAddParentToChildMap, parentList);
		else
			getArtsToAdd(relArt, toAddParentToChildMap, parentList, rootModel, showChildren, mapRDFtoProjRes);
    	// want to move backwards through the map so that we add the top most container first and relArt last
		for (int i = parentList.size() - 1; i >= 0; i--) {
    		final Artifact parentArt = (Artifact) parentList.get(i);
    		final Artifact childArt = toAddParentToChildMap.get(parentArt);
    		
    		// find parentAF - since we are starting from containers first, the
			// parent should have already been added - so we need to just find it
    		ArtifactFragment parentAF = null;
			if (addedArtToAFMap.containsKey(parentArt)) {
				// if parent was just added then use that
				parentAF = addedArtToAFMap.get(parentArt);
			} else 
        		// search for parent in model - it should have already been added
    			if(!rootModel.getMatchingNestedShownChildren(parentArt).isEmpty()) parentAF = rootModel.getMatchingNestedShownChildren(parentArt).get(0);

			if (parentAF==null) return; //should never happen
    		
    		// find orphan children of added parent in existing model and move as child of new parent
    		List<ArtifactFragment> movedChildren = new ArrayList<ArtifactFragment>();
    		ArrayList<Artifact> newParentList = new ArrayList<Artifact>(parentList);
    		newParentList.add(relArt);
    		findAndReparentExistingChildren(compoundAddCmd, rootModel, newParentList, parentAF, movedChildren);

    		List<Artifact> modelChildren = new ArrayList<Artifact>();
    		for (ArtifactFragment modelChild : parentAF.getShownChildren()) {
				modelChildren.add(modelChild.getArt());
    		}
    		// if child is already in the model just show
    		if(!rootModel.getMatchingNestedShownChildren(childArt).isEmpty() && isInCorrectSpot(childArt, rootModel)) {      
    			ClosedContainerDPolicy.showChildren(compoundAddCmd, parentAF, (StrataRootDoc) rootModel);
    		} else if (modelChildren.contains(childArt)) {
    			ClosedContainerDPolicy.showChildren(compoundAddCmd, parentAF, (StrataRootDoc) rootModel);
    		} else {
        		//otherwise create child, add to map, add to model, then show
    			
    			//Only create child if not already just added
        		ArtifactFragment childAF = null;
        		if(addedArtToAFMap.containsKey(childArt))
        			childAF = addedArtToAFMap.get(childArt); 
        		else {
        			childAF = ((StrataRootDoc) rootModel).createArtFrag(childArt);
            		addedArtToAFMap.put(childArt, childAF);
        		}
        
        		Resource type = childAF.getArt().queryType(repo);
        		boolean isBreakable = (CUSupport.isPackage(type) || CUSupport.isPackageFolder(type) );
    			ClosedContainerDPolicy.addAndShowChild(compoundAddCmd, parentAF, childAF, (StrataRootDoc) rootModel, showChildren);
    			ClosedContainerDPolicy.setBreakable(parentAF, isBreakable);
    		}
    	}
    }
	
//    // Version used for adding arts to an existing diagram
//	public static void addArtAndContainers(CompoundCommand compoundAddCmd, Map<Artifact, ArtifactFragment> addedArtToAFMap, final BasicRootController rc, ReloRdfRepository repo, Artifact relArt) {
//    	if (addedArtToAFMap == null) addedArtToAFMap = new HashMap<Artifact, ArtifactFragment>();
//		addedArtToAFMap.put(rc.getRootArtifact().getArt(), rc.getRootArtifact());
//		
//		LinkedHashMap<Artifact, Artifact> toAddParentToChildMap = new LinkedHashMap<Artifact, Artifact>();
//    	List<Artifact> parentList = new ArrayList<Artifact>();
//    	
//    	if (relArt.queryWarnedType(repo).equals(RJCore.methodType) || relArt.queryWarnedType(repo).equals(RJCore.fieldType) )
//    		relArt = relArt.queryParentArtifact(repo);
//    	
//    	getArtsToAdd(rc, repo, relArt, toAddParentToChildMap, parentList);
//    	
//    	// want to move backwards through the map so that we add the top most container first and relArt last
//		for (int i = parentList.size() - 1; i >= 0; i--) {
//    		final Artifact parentArt = (Artifact) parentList.get(i);
//    		final Artifact childArt = toAddParentToChildMap.get(parentArt);
//    		
//    		// find parentAF - since we are starting from containers first, the
//			// parent should have already been added - so we need to just find it
//    		ArtifactFragment parentAF = null;
//			if (addedArtToAFMap.containsKey(parentArt)) {
//				// if parent was just added then use that
//				parentAF = addedArtToAFMap.get(parentArt);
//			} else 
//        		// search for parent in model - it should have already been added
//    			if(!rc.getRootArtifact().getMatchingNestedShownChildren(parentArt).isEmpty()) parentAF = rc.getRootArtifact().getMatchingNestedShownChildren(parentArt).get(0);
//
//			if (parentAF==null) return; //should never happen
//    		
//    		// find orphan children of added parent in existing model and move as child of new parent
//    		StrataRootDoc strataRC = (StrataRootDoc) rc.getRootArtifact();
//    		List<ArtifactFragment> movedChildren = new ArrayList<ArtifactFragment>();
//    		ArrayList<Artifact> newParentList = new ArrayList<Artifact>(parentList);
//    		newParentList.add(relArt);
////    		findAndReparentExistingChildren(compoundAddCmd, strataRC, rc, repo, newParentList, parentAF, movedChildren);
//    		findAndReparentExistingChildren(compoundAddCmd, strataRC, newParentList, parentAF, movedChildren);
//
//    		// if child is already in the model just show
//    		if(!rc.getRootArtifact().getMatchingNestedShownChildren(childArt).isEmpty() && isInCorrectSpot(childArt, (StrataRootDoc) rc.getRootArtifact())) {      
//    			ClosedContainerDPolicy.showChildren(compoundAddCmd, parentAF, (StrataRootDoc) rc.getRootArtifact());
//    		} else {
//        		//otherwise create child, add to map, add to model, then show
//    			
//    			//Only create child if not already just added
//        		ArtifactFragment childAF = null;
//        		if(addedArtToAFMap.containsKey(childArt))
//        			childAF = addedArtToAFMap.get(childArt); 
//        		else {
//        			childAF = ((StrataRootDoc) rc.getRootArtifact()).createArtFrag(childArt);
//            		addedArtToAFMap.put(childArt, childAF);
//        		}
//        
//        		Resource type = childAF.getArt().queryType(repo);
//        		boolean isBreakable = (CUSupport.isPackage(type) || CUSupport.isPackageFolder(type) );
//    			ClosedContainerDPolicy.addAndShowChild(compoundAddCmd, parentAF, childAF, (StrataRootDoc) rc.getRootArtifact());
//    			ClosedContainerDPolicy.setBreakable(parentAF, isBreakable);
//    		}
//    	}
//    }
    
    public static void addSingleArt(CompoundCommand compoundAddCmd, Point dropPoint, ContainableEditPart targetEP, ArtifactFragment artifactFragment, StrataRootDoc rootModel) {
    	Artifact childArt = artifactFragment.getArt();
    	ArtifactFragment childAF = null;
		if (artifactFragment instanceof UserCreatedFragment) {
			childAF = StrataFactory.initAF(artifactFragment);//((StrataRootDoc) rootModel).createArtFrag(childArt);
		} else
			childAF = ((StrataRootDoc) rootModel).createArtFrag(childArt);
		ArtifactFragment parentAF = targetEP.getArtFrag();
		
    	if (targetEP instanceof LayerEditPart) {
    		// set parentAF as root model if it is not a SAFEP, this should only happen when adding to a root layer
    		if (!(targetEP.getParent() instanceof StrataArtFragEditPart)) parentAF = rootModel;
    		else 
    			parentAF = ((StrataArtFragEditPart) targetEP.getParent()).getArtFrag();
    		// if adding to a layer check if its a multiLayer and get special
			// indices, otherwise create standard move command for layers
    		Layer layer = ((LayerEditPart) targetEP).getLayer();
    		if ((layer instanceof CompositeLayer) && ((CompositeLayer) layer).getMultiLayer()) {
				
				// edge case:
				// if adding to the multiLayer below child layers then add at
				// end of the last layer (do not create new layer)
				int newLayerChildNdx = -1;
				int newLayerNdx = ((LayerEditPart) targetEP).getLayer().getShownChildren().size()-1;

				LayerEditPart child = ContainableEditPart.getLayerChild(dropPoint, targetEP.getChildren());
				if (child != null) {
					newLayerNdx = targetEP.getChildren().indexOf(getLayerChild(dropPoint, targetEP.getChildren()))+1;
					newLayerChildNdx = child.getChildren().indexOf(getEPChild(dropPoint, child));
				}
				ClosedContainerDPolicy.addAndShowChild(compoundAddCmd, parentAF, childAF, (StrataRootDoc) rootModel,
						((Layer) ((LayerEditPart) targetEP).getLayer().getShownChildren().get(newLayerNdx)), newLayerChildNdx);
				
				return;
			}
			int newLayerChildNdx = ((LayerEditPart) targetEP).getLayer().getShownChildren().size();
			if(getEPChild(dropPoint, targetEP) != null)
				newLayerChildNdx = targetEP.getChildren().indexOf(getEPChild(dropPoint, targetEP));
			logger.info("moving to Layer " + targetEP.getClass() + newLayerChildNdx);
			ClosedContainerDPolicy.addAndShowChild(compoundAddCmd, parentAF, childAF, (StrataRootDoc) rootModel,
					((LayerEditPart) targetEP).getLayer(), newLayerChildNdx);
			return;
    	}
    	if (parentAF instanceof StrataRootDoc)
    		ClosedContainerDPolicy.addAndShowChild(compoundAddCmd, parentAF, childAF, (StrataRootDoc) rootModel,
					new Layer(rootModel.getDepNdx()), -1);
			
        ClosedContainerDPolicy.addAndShowChild(compoundAddCmd, parentAF, childAF, (StrataRootDoc) rootModel);	
	}
    
    public static void addSingleUserCreatedArt(CompoundCommand compoundAddCmd, Point dropPoint, ContainableEditPart targetEP, UserCreatedFragment artifactFragment, StrataRootDoc rootModel) {
    	ArtifactFragment childAF = StrataFactory.initAF(artifactFragment);//((StrataRootDoc) rootModel).createArtFrag(childArt);
    	ArtifactFragment parentAF = targetEP.getArtFrag();
    	
    	if (targetEP instanceof LayerEditPart) {
    		// set parentAF as root model if it is not a SAFEP, this should only happen when adding to a root layer
    		if (!(targetEP.getParent() instanceof StrataArtFragEditPart)) parentAF = rootModel;
    		else 
    			parentAF = ((StrataArtFragEditPart) targetEP.getParent()).getArtFrag();
    		// if adding to a layer check if its a multiLayer and get special
			// indices, otherwise create standard move command for layers
    		Layer layer = ((LayerEditPart) targetEP).getLayer();
    		if ((layer instanceof CompositeLayer) && ((CompositeLayer) layer).getMultiLayer()) {
				
				// edge case:
				// if adding to the multiLayer below child layers then add at
				// end of the last layer (do not create new layer)
				int newLayerChildNdx = -1;
				int newLayerNdx = ((LayerEditPart) targetEP).getLayer().getShownChildren().size()-1;

				LayerEditPart child = ContainableEditPart.getLayerChild(dropPoint, targetEP.getChildren());
				if (child != null) {
					newLayerNdx = targetEP.getChildren().indexOf(getLayerChild(dropPoint, targetEP.getChildren()))+1;
					newLayerChildNdx = child.getChildren().indexOf(getEPChild(dropPoint, child));
				}
				ClosedContainerDPolicy.addAndShowChild(compoundAddCmd, parentAF, childAF, (StrataRootDoc) rootModel,
						((Layer) ((LayerEditPart) targetEP).getLayer().getShownChildren().get(newLayerNdx)), newLayerChildNdx);
				
				return;
			}
			int newLayerChildNdx = ((LayerEditPart) targetEP).getLayer().getShownChildren().size();
			if(getEPChild(dropPoint, targetEP) != null)
				newLayerChildNdx = targetEP.getChildren().indexOf(getEPChild(dropPoint, targetEP));
			logger.info("moving to Layer " + targetEP.getClass() + newLayerChildNdx);
			ClosedContainerDPolicy.addAndShowChild(compoundAddCmd, parentAF, childAF, (StrataRootDoc) rootModel,
					((LayerEditPart) targetEP).getLayer(), newLayerChildNdx);
			return;
    	}
    	if (parentAF instanceof StrataRootDoc)
    		ClosedContainerDPolicy.addAndShowChild(compoundAddCmd, parentAF, childAF, (StrataRootDoc) rootModel,
					new Layer(rootModel.getDepNdx()), -1);
			
        ClosedContainerDPolicy.addAndShowChild(compoundAddCmd, parentAF, childAF, (StrataRootDoc) rootModel);	
	}
    
    
    
    
    
    
    
    
    
	// for each existing element in the model find its possible parents and
	// compare it to the parent being added. If they are the same move the
	// child. (existing model may have children attached to grandparents and may
	// need to have the parent inserted)
    // Examine recursion and possibly using getArtsToAdd instead of getParentPackages
	private static void findAndReparentExistingChildren(CompoundCommand addOrphansToNewParent, ArtifactFragment existingParent, List<Artifact> newParentList, ArtifactFragment parentAF, List<ArtifactFragment> movedChildren) {
		StrataRootDoc rootModel = (StrataRootDoc) existingParent.getRootArt();
		ReloRdfRepository repo = rootModel .getRepo();
		// Find Existing children in root and add them to this parent 
    	for (ArtifactFragment orphanChild : new ArrayList<ArtifactFragment>(existingParent.getShownChildren())) {
    		// do not worry about user created elements, they have no children
    		if (orphanChild instanceof UserCreatedFragment) continue;
    		
    		// find art for immediate parent package / package folder
    		List<Artifact> allParents = getParentPackages(orphanChild.getArt(), repo, rootModel);
    	
//    		LinkedHashMap<Artifact, Artifact> toAddParentToChildMap = new LinkedHashMap<Artifact, Artifact>();
//        	List<Artifact> allParents = new ArrayList<Artifact>();
//    		getArtsToAdd((BasicRootController) rc, repo, orphanChild.getArt(), toAddParentToChildMap, allParents);
    		
    		for (Artifact pckgDirArt : allParents ) {
	    		// make sure parent art is in the list of arts being added that they are identical and that it has not already been moved
	    		if (pckgDirArt != null && newParentList.contains(pckgDirArt) 
	    				&& parentAF.getArt().elementRes.equals(pckgDirArt.elementRes)
	    				&& !movedChildren.contains(orphanChild)) {
	    			// add orphan to the new parent
	    			ClosedContainerDPolicy.addAndShowChild(addOrphansToNewParent, parentAF, orphanChild, (StrataRootDoc) rootModel);
	    			// make sure children of moved items are not moved twice
	        		movedChildren.addAll(getAllArtFrags(orphanChild));
	    		}
    		}
    		findAndReparentExistingChildren(addOrphansToNewParent, orphanChild, newParentList, parentAF, movedChildren);
    	}	
    }

    private static List<ArtifactFragment> getAllArtFrags(ArtifactFragment afParent) {
    	List<ArtifactFragment> childrenArts = new ArrayList<ArtifactFragment>();
    	if (afParent.getArt().elementRes == null) return null;
    	childrenArts.add(afParent);
    	for (ArtifactFragment afChild : afParent.getShownChildren()) {
			childrenArts.addAll(getAllArtFrags(afChild));
    	}
    	return childrenArts;
	}

	
	private static List<Artifact> getParentPackages(Artifact orphanChild, ReloRdfRepository repo, RootArtifact rootArtifact) {
		List<Artifact> allParents = new ArrayList<Artifact>();
		Resource pckgDirContainer = null;
		String packageString = RSECore.resourceToId(repo, orphanChild.elementRes, true);  
		Resource type = orphanChild.queryOptionalType(repo);
		if (type == null)
			return allParents;
		else if (type.equals(RJCore.indirectPckgDirType) ) 
    		pckgDirContainer = repo.getStatement((Resource)null, RJCore.pckgDirContains, orphanChild.elementRes ).getSubject();
		else if (type.equals(RJCore.classType) || type.equals(RJCore.interfaceType)) { 
			Artifact orphanParentArt = orphanChild.queryParentArtifact(repo);
			if (orphanParentArt != null) pckgDirContainer = orphanParentArt.elementRes;
		} else if (type.toString().contains("jdt-web#webPath") ) // TODO abstract into EJCore 
			return allParents;
		else {
			if (RSECore.ResourceIsEclipseProject(orphanChild.elementRes)) 
				return allParents;
			pckgDirContainer = RJCore.idToResource(repo, RJMapToId.pckgIDToPckgFldrID(packageString));	
		}
			
		if (pckgDirContainer == null) 
    		return allParents;

    	Artifact parentArt = new Artifact(pckgDirContainer);
    	if (rootArtifact.getMatchingNestedShownChildren(parentArt).isEmpty())
    		allParents.add(parentArt);
    	
    	allParents.addAll(getParentPackages(parentArt, repo, rootArtifact));
    	
    	return allParents;
	}

	// For each component that needs to be added to the diagram, adds a mapping
    // from the parent that will contain that container to the component
    private static void getArtsToAdd(Artifact childArt, LinkedHashMap<Artifact, Artifact> toAddParentToChildMap, List<Artifact> parentList, StrataRootDoc rootModel, boolean showChildren, Map<Resource, Resource> mapRDFtoProjRes) {
    	List<ArtifactFragment> childAFList = rootModel.getMatchingNestedShownChildren(childArt);
    	ReloRdfRepository repo = rootModel.getRepo();
    	// if child is already in the diagram and it is also in the correct spot in the diagram
    	// do not add
    	// use showChildren flag so that we open closed packages
		if (!childAFList.isEmpty() && isInCorrectSpot(childArt, rootModel) && !showChildren) {
    		return; // already in diagram, so don't need to add it
		}
    	
    	Artifact parentArt = getPckgDirParent(childArt, rootModel);
    	
    	if (parentArt == null)
    		parentArt = childArt.queryParentArtifact(repo);
    	
    	if (parentArt==null) {
    		parentArt = rootModel.getArt();
    		toAddParentToChildMap.put(parentArt, childArt);
        	parentList.add(parentArt);
    		return;
    	}
    	
    	if (parentArt.queryType(repo).equals(RJCore.projectType) && mapRDFtoProjRes !=null) {
    		List<Resource> parents = rootModel.getPckgDirRepo().getParents((Resource)null, RJCore.pckgDirContains, (Value)childArt.elementRes);
    		Resource selParentProjRes = mapRDFtoProjRes.get(childArt.elementRes);
    		String selProjString = "";
    		if (selParentProjRes != null) { // will be null if opening from other than package explorer
    			selProjString = selParentProjRes.toString();
        		selProjString = selProjString.substring(selProjString.indexOf("#"));
    		}
    		for (Resource parentProjRes : parents) {
    			String parentProjString = parentProjRes.toString();
    			parentProjString = parentProjString.substring(parentProjString.indexOf("#"));
    			
				if (parentProjString.equals(selProjString)) {
    				parentArt = new Artifact(parentProjRes);
    				break;
    			}
    		}
    	}
    	
    	toAddParentToChildMap.put(parentArt, childArt);
    	parentList.add(parentArt);
    	getArtsToAdd(parentArt, toAddParentToChildMap, parentList, rootModel, showChildren, mapRDFtoProjRes); // parent must be added too if it isn't in the diagram yet
    }
	
    // Version Used when adding to an existing diagram (we do not need to add all the parents)
    private static void getArtsToAdd(BasicRootController rc, ReloRdfRepository repo, Artifact childArt, LinkedHashMap<Artifact, Artifact> toAddParentToChildMap, List<Artifact> parentList) {
    	List<ArtifactFragment> childAFList = rc.getRootArtifact().getMatchingNestedShownChildren(childArt);
    	
    	// if child is already in the diagram and it is also in the correct spot in the diagram
    	// do not add
		if (!childAFList.isEmpty() && rc.findEditPart(childAFList.get(0)) != null && isInCorrectSpot(childArt, (StrataRootDoc) rc.getRootArtifact())) {
    		return; // already in diagram, so don't need to add it
		}
    	
//    	Artifact parentArt = getPckgDirParent(rc, repo, childArt);
		Artifact parentArt = getPckgDirParent(childArt, (StrataRootDoc) rc.getRootArtifact());
    	
    	if (parentArt == null)
    		parentArt = childArt.queryParentArtifact(repo);
    	
    	if (parentArt==null) {
    		parentArt = rc.getRootArtifact().getArt();
    		toAddParentToChildMap.put(parentArt, childArt);
        	parentList.add(parentArt);
    		return;
    	}
    	
    	toAddParentToChildMap.put(parentArt, childArt);
    	parentList.add(parentArt);
    	getArtsToAdd(rc, repo, parentArt, toAddParentToChildMap, parentList); // parent must be added too if it isn't in the diagram yet
    }

    private static boolean isInCorrectSpot(Artifact childArt, StrataRootDoc rootModel) {
    	ReloRdfRepository repo = rootModel.getRepo();
		Artifact parentArt = childArt.queryParentArtifact(repo);
		List<ArtifactFragment> matchingAFList = new ArrayList<ArtifactFragment>();
		ArtifactFragment actualParentAF  = null;
		// find queried parent in diagram 
		if (parentArt != null ) 
			matchingAFList = rootModel.getMatchingNestedShownChildren(parentArt);
		if (!matchingAFList.isEmpty() && matchingAFList.get(0) != null) 
			actualParentAF  = rootModel.getMatchingNestedShownChildren(parentArt).get(0);
		
		// return true (ignore) if parent is not in diagram
		if (actualParentAF == null)
			return true;

		// if child is already in found parent return true
		for (ArtifactFragment childAF : actualParentAF.getShownChildren()) {
			if (childAF.getArt().elementRes.equals(childArt.elementRes)) 
				return true;
		}
		// child must not be in diagram with the correct parent so return false
		return false;
	}

	private static Artifact getPckgDirParent(Artifact childArt, StrataRootDoc rootModel) {
    	//ReloRdfRepository repo = rootModel.getRepo();
		Resource pckgDirContainer = rootModel.getPckgDirRepo().getStatementSubj( (Resource)null, RJCore.pckgDirContains, (Value)childArt.elementRes);
    	if(pckgDirContainer==null) return null;

    	Artifact pckgDirArt = new Artifact(pckgDirContainer);

		// Used to check for EP type here. Not sure why we need to get the EP
		// from the model and then the model from the EP
    	// Artifact found from model/controller should be the same as Artifact created by Resource
    	//List<ArtifactFragment> pckgDirAFList = rootModel.getMatchingNestedShownChildren(pckgDirArt);
    	//if(!pckgDirAFList.isEmpty()) {
    	//	ArtifactFragment pckgDirAF = pckgDirAFList.get(0);
    	//	Object pckgDirEP = rc.findEditPart(pckgDirAF);
    	//	if (pckgDirEP instanceof StrataArtFragEditPart) return ((StrataArtFragEditPart) pckgDirEP).getArtFrag().getArt();
    	//	return pckgDirAF.getArt();
    	//} 
    	return pckgDirArt;
    }
    
	//TODO Still need this??? nobody calls??
    public void showAllDirectRelation(CompoundCommand btnExecCmd, final DirectedRel rel, final Predicate filter) {
    	List<Artifact> filteredListModel = showableListModel(getRepo(), rel, filter);
    	if(filteredListModel.size()==0) return;
    	try {
            //logger.info("Exectuing for: " + myCU + " myDepth: " + myDepth + " myType: " + myType);
  
            btnExecCmd.add(new ShowAllDirectRelationCommand(this, filter, rel));
        } catch (Throwable t) {
            logger.error("Unexpected exception", t);
        }
    }
    
    //TODO nobody calls?
    public void showAllDirectRelation(DirectedRel rel, Predicate filter) {
        StrataRootEditPart rc = getRootController();
        ArtifactFragment rootAF = rc.getArtFrag();

        ArtifactFragment myAF = getArtFrag();
        Artifact myArt = myAF.getArt();
        Resource myType = myArt.queryType(getRepo());
        int myDepth = CodeUnit.getContextDepth(getRepo(), myArt); 

        // @tag thesis: change this so that it does this heirarchically, and shows the fewest possible items
        // how do we deal with 'byte', etc. --> (lets ignore all built-in)
        StrataRootDoc rootDoc = getRootModel();
        List<Artifact> relArts = myAF.getArt().queryArtList(getRepo(), rel, filter);
		List<ArtifactFragment> itemsToAdd = new ArrayList<ArtifactFragment> (relArts.size());
        for (Artifact relCU : relArts) {
            if (rc.findEditPart(relCU) != null) continue;
            if (!rc.modelCreatable(relCU)) continue;
            
            int relDepth = CodeUnit.getContextDepth(getRepo(), relCU);
            Resource relType = relCU.queryType(getRepo());
            if (relDepth > myDepth || !myType.equals(relType)) {
                //logger.info("Skipping: " + relCU + " depth: " + relDepth);
                continue;
            } else {
                //logger.info("Registering: " + relCU + " depth: " + CodeUnit.getContextDepth(getRepo(), relCU) + " Type: " + relType);
            }
            
            ArtifactFragment strataAF = rootDoc.createArtFrag(relCU);
            if (!ClosedContainerDPolicy.getShownChildren(rootAF).contains(strataAF))
            	itemsToAdd.add(strataAF);

            //tgtCmd.add(new AddNodeAndRelCmd(rc, ArtifactEditPart.this, rel.res, relCU, rel.isFwd));
        }
        rootAF.appendShownChildren(itemsToAdd);  // flush layers and update...
    }

    //public final static String REQ_REDUCE = "minimize";
    //public final static String REQ_EXPAND = "expand";

    // tooltips
    //public final static String COLLAPSE = "collapse";
    //public final static String EXPAND = "expand";
    public final static String HIDE = "hide";

    public List<NavAidsSpec> getSingleSelectHandlesSpecList(NavAidsEditPolicy bdec) {
        // @tag work-here-first
        final List<NavAidsSpec> decorations = new ArrayList<NavAidsSpec> (5);
        /*
        decorations.add(new NavAidsSpec() {
            @Override
            public void buildHandles() {
                IFigure btn;
                
                //btn = getReqButton(StrataArtFragEditPart.this, "collapse.gif", REQ_REDUCE, COLLAPSE);
                //if (btn != null) decorationFig.add(btn);
                
                //btn = getReqButton(StrataArtFragEditPart.this, "expand.gif", REQ_EXPAND, EXPAND);
                //if (btn != null) decorationFig.add(btn);
                
                btn = getReqButton(StrataArtFragEditPart.this, "remove.gif", RequestConstants.REQ_DELETE, HIDE);
                if (btn != null) decorationFig.add(btn);
            }
            @Override
            public Point getHandlesPosition(IFigure containerFig) {
                if (containerFig instanceof CodeUnitFigure)
                    containerFig = ((CodeUnitFigure)containerFig).getLabel();
                
                return containerFig.getBounds().getTopRight(); 
            }
        });
        */
        
        decorations.add(new NavAidsSpec() {
            @Override
            public void buildHandles() {
                DirectedRel rel = DirectedRel.getFwd(RJCore.containmentBasedRefType);
				//Artifact art = StrataArtFragEditPart.this.getArtFrag().getArt();
//				int relCnt = showableListModel(getRepo(), rel, null).size();
				List<Artifact> childList = showableListModel(getRepo(), rel, null);
				int relCnt = childList.size();
				// we force the handles to always be shown
				IFigure btn = null;
				if (relCnt>0)
					btn = getRelation(StrataArtFragEditPart.this, rel, relCnt, childList);
                
                if (btn != null) decorationFig.add(btn);
            }
            @Override
            public Point getHandlesPosition(IFigure containerFig) {
                Dimension prefSize = decorationFig.getPreferredSize();
                Rectangle bounds = containerFig.getBounds();
                int x = bounds.x + (bounds.width - prefSize.width)/2;
                int y = bounds.y + bounds.height;
                return new Point(x, y);
            }
        });
        decorations.add(new NavAidsSpec() {
            @Override
            public void buildHandles() {
                DirectedRel rel = DirectedRel.getRev(RJCore.containmentBasedRefType);
				//Artifact art = StrataArtFragEditPart.this.getArtFrag().getArt();
//				int relCnt = showableListModel(getRepo(), rel, null).size();
				List<Artifact> childList = showableListModel(getRepo(), rel, null);
				int relCnt = childList.size();
				IFigure btn = null;
				if (relCnt>0)
					btn = getRelation(StrataArtFragEditPart.this, rel, relCnt, childList);
                
                if (btn != null) decorationFig.add(btn);
            }
            @Override
            public Point getHandlesPosition(IFigure containerFig) {
                Dimension prefSize = decorationFig.getPreferredSize();
                Rectangle bounds = containerFig.getBounds();
                int x = bounds.x + (bounds.width - prefSize.width)/2;
                int y = bounds.y - prefSize.height;
                return new Point(x, y);
            }
        });
        decorations.add(new NavAidsSpec() {
        	@Override
        	public void buildHandles() {
        		IFigure btn = RSEContextMenuProvider.getContextMenuNavAid(getViewer());
        		if (btn!=null) decorationFig.add(btn);	
        	}
        	@Override
        	public Point getHandlesPosition(IFigure containerFig) {
        		// Put button outside upper right corner
        		Rectangle bounds = containerFig.getBounds();
        		return new Point(bounds.x+bounds.width, bounds.y);
        	}
        });

        return decorations;
    }

    
    ///////////////////////// CREATING MORE BUTTON /////////////////////
    public IFigure getMoreButton() {
		// add text
    	Label moreLabel = new Label();
		moreLabel.setLabelAlignment(PositionConstants.CENTER);
//		int numMembers = getMoreChildrenList(getArtFrag().getArt(), new ArrayList<Artifact>()).size();
		final List<Artifact> childrenList = getMoreChildrenList(getArtFrag().getArt());
		int numMembers = childrenList.size();
		if (numMembers < 1) return new Figure();
		String plural = (numMembers != 1) ? "s " : " ";
		moreLabel.setText(numMembers + MORE_BUTTON_TEXT  + plural);

		// add layout
		IFigure moreFig = new Figure();
		ToolbarLayout tb = new ToolbarLayout(true);
		tb.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		moreFig.setLayoutManager(tb);
		moreFig.add(moreLabel);

		//add arrow
		moreFig.add(MoreButtonUtils.getMoreButtonTriangle());

		IFigure moreContainer = new Figure();
		FlowLayout layout = new FlowLayout(true);
		layout.setMajorAlignment(ToolbarLayout.ALIGN_BOTTOMRIGHT);
		moreContainer.setLayoutManager(layout);
		moreContainer.add(moreFig);
		
		// add button
		MenuButton menuButton = new MenuButton(moreContainer, getViewer()) {
			@Override
			public void buildMenu(IMenuManager menu) {
				menu.add(new Separator("main"));
//				buildMoreChildrenContextMenu(menu);
				
				// actions to send to server/build menu with 
				List<MultiAddCommandAction> actions = buildMoreChildrenContextMenu(menu, childrenList); 
				if (menu.getItems().length <= 2) 
					return;
				MoreButtonUtils.addShowAllItem(menu, getRootController());
				
				// if we are sending actions to the server we dont need to actually build the menu
				if (actions == null) menu.removeAll();
		}};

		// set properties, set bounds and add
		Figure moreButton = MoreButtonUtils.setProperties(menuButton, getFigure().getFont(), MORE_BUTTON_TOOLTIP_TEXT);
		MoreButtonUtils.setMoreButtonBounds(moreButton, getFigure().getBounds());
		return moreButton;
	}

	private List<MultiAddCommandAction> buildMoreChildrenContextMenu(final IMenuManager menu, List<Artifact> allMoreChildren) {
		final StrataRootEditPart rc = getRootController();
		DirectedRel rel = DirectedRel.getRev(RJCore.containmentBasedRefType);
//		List<Artifact> artsInMenu = new ArrayList<Artifact>();
		
//		List<Artifact> allMoreChildren = getMoreChildrenList(getArtFrag().getArt(), artsInMenu);
		return createMenu(menu, new HashSet<Artifact>(allMoreChildren), rc, rel);
	}

	public void buildNavAidMenu(List<MultiAddCommandAction> menuActions, NavAidsSpec spec, IMenuManager defaultMenu, DirectedRel rel) {
		spec.createMenu(defaultMenu, new HashSet<Artifact>(showableListModel(getRepo(), rel, null)), this, rel, getRootController());
	}

	public void sortArtifactListByName(List<Artifact> allMoreChildren) {
		Collections.sort(allMoreChildren, new Comparator<Artifact>() {
			public int compare(Artifact art1, Artifact art2) {
				if (art1 == null || art2 == null) return 0;
				return getRelModelLabel(art1).compareTo(getRelModelLabel(art2));
			}
		});
	}
	
	int MAX_LENGTH = 15;
	private List<MultiAddCommandAction> createMenu(IMenuManager menu, Set<Artifact> allMoreChildren, StrataRootEditPart rc, DirectedRel rel) {
		List<Artifact> childList = new ArrayList<Artifact>(allMoreChildren);
		sortArtifactListByName(childList);
		List<Artifact> tempList = new ArrayList<Artifact>(childList);
		boolean createMultipleMenus = childList.size() > MAX_LENGTH;
		Iterator<Artifact> itr = tempList.iterator();
		
		// actions to send to server/build menu with 
		List<MultiAddCommandAction> actions = new ArrayList<MultiAddCommandAction>();
				
		while (!childList.isEmpty()) {
			String menuStrStart = getRelModelLabel(childList.get(0));
 			MenuManager subMenu;
 			
 			if (createMultipleMenus) {
	 			subMenu = new MenuManager(menuStrStart+ " - ...");
				subMenu.add(new Separator("main"));
				menu.add(subMenu);
 			} else
 				subMenu = (MenuManager) menu;
 			
 			int i = 0;
 			while (i < MAX_LENGTH && itr.hasNext()) {
 				Artifact childArt = itr.next();
	 			childList.remove(childArt);
	 			MultiAddCommandAction action = null;
	            String relArtLbl = getRelModelLabel(childArt);

	            // if still a packageFolder at this point then we have multiple children
				// they all must be added via this action
				// the children packages must not show in the menu either (?)
	            List<Artifact> arts = new ArrayList<Artifact>();
	            if (childArt.queryType(getRepo()).equals(RJCore.indirectPckgDirType) ){
					for (Artifact art : ModelUtils.getAllPackageChildrenFromRepo(childArt, getRepo())) {
						arts.add(art);
					}
				} else
					arts.add(childArt);
	            
				action = getShowRelAction(rc, rel, arts, relArtLbl);
				actions.add(action);
	            if (action == null) continue;
	            try {
	                ImageDescriptor des = rc.getIconDescriptor(this, childArt);
	                if (des != null) action.setImageDescriptor(des);
	            } catch (Throwable t) {
	                logger.error("Unexpected error while getting icon for: " + childArt, t);
	            }
	            subMenu.appendToGroup("main", action);
	            i++;
 			}
		}
		EditDomain editDomain = getRoot().getViewer().getEditDomain();
		
		// do not need to check editor when using a view
		if (!(editDomain instanceof DefaultEditDomain)) 
			return actions;
		
		RSEEditor editor = (RSEEditor) ((DefaultEditDomain)editDomain).getEditorPart();
		if (editor.rseInjectableOICMenuController.createMenu(actions, this)) return null; // return if actions are being sent to server
		return actions;
		
	}

	private List<Artifact> getMoreChildrenList(Artifact art) {
		List<Artifact> artsInMenu = new ArrayList<Artifact>();
		List<Artifact> allMoreChildren = new ArrayList<Artifact>();
		for (Artifact childArt : ModelUtils.getAllPackageChildrenFromRepo(art, getRepo())) {
			// ignore fields and methods
			if (!childArt.isInitialized(getRepo()) || childArt.queryType(getRepo()).equals(RJCore.methodType) || childArt.queryType(getRepo()).equals(RJCore.fieldType)) continue;  
          	// ignore if already added
			if (artsInMenu.contains(childArt)) continue; // already in menu
          	// ignore if it is a packageFolder with just one child
			if (ModelUtils.queryChldren(childArt, getRepo()).size() <=1 && (childArt.queryType(getRepo()).equals(RJCore.indirectPckgDirType))) continue; 
			
			boolean skip = false;
          	// dont add items already in model
          	for (ArtifactFragment childAF : getAllArtFrags(getArtFrag())) {
          		if (childArt.equals(childAF.getArt()) && ClosedContainerDPolicy.isShowingChildren(childAF.getParentArt())) 
          			skip  = true; // already in diagram          		
          	}
          	if (skip) continue;
          	
          	if (childArt.queryType(getRepo()).equals(RJCore.indirectPckgDirType) ){
				for (Artifact artChild : ModelUtils.getAllPackageChildrenFromRepo(childArt, getRepo())) {
					artsInMenu.add(artChild );
				}
			}
          	allMoreChildren.add(childArt);
		}
		return allMoreChildren;
	}
    
    
    ///////////////////////////////////////////////////////////////////////////

    protected Command createDeleteCommand(final ArtifactFragment parentAF, 
			final ArtifactFragment strataAFToRemove) {
		
		final StrataRootDoc rootDoc = this.getRootModel();
		
		return new DeleteCommand("Delete Node", strataAFToRemove, rootDoc, parentAF);
	}
    
    @Override
    protected List<ArtifactRel> getModelTargetConnections() {
    	List<ArtifactRel> targetRels = new ArrayList<ArtifactRel>(getArtFrag().getShownTargetConnections());
    	for(ArtifactRel afRel : getArtFrag().getShownTargetConnections()){
    		if(!beingShown(afRel.getDest().getParentArt()) || !beingShown(afRel.getSrc().getParentArt()))
    			targetRels.remove(afRel);
    	}
    	return targetRels;
    }

	@Override
    protected List<ArtifactRel> getModelSourceConnections() {
    	List<ArtifactRel> sourceRels = new ArrayList<ArtifactRel>(getArtFrag().getShownSourceConnections());
    	for(ArtifactRel afRel : getArtFrag().getShownSourceConnections()){
    		if(!beingShown(afRel.getDest().getParentArt()) || !beingShown(afRel.getSrc().getParentArt()))
        			sourceRels.remove(afRel);
    	}
    	return sourceRels;
    }
	
	private boolean beingShown(ArtifactFragment af) {
		if (af == null) return false;
		if (af == af.getRootArt()) return true;
		if (ClosedContainerDPolicy.isShowingChildren(af) && beingShown(af.getParentArt()))
			return true;
	    return false;
	}

	
	// *********************  DIRECT EDIT METHODS FOR USER CREATED FRAGS ******************
	
	protected SAFEPLabelDirectEditManager manager;
	protected String oldName;

	protected void installDirectEditPolicy() {
		if(!(this instanceof UndoableLabelSource)) return;
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, 
				new AnnoLabelDirectEditPolicy((UndoableLabelSource)this, getTextChangeCmdName()));
	}
	protected String getTextChangeCmdName() {
		return "Edit Name";
	}
	
	public class SAFEPLabelDirectEditManager extends LabelDirectEditManager {
		public SAFEPLabelDirectEditManager(GraphicalEditPart source, Class<TextCellEditor> editorType,
				CellEditorLocator locator, IFigure directEditFigure) {
			super(source, editorType, locator, directEditFigure);
		}
		@Override
		protected void commit() {
			super.commit();
		}
	}
	
	protected Command getDirectEditCommand() {
		if(!(this instanceof LabelSource)) return null;
		final StrataArtFragEditPart taep = StrataArtFragEditPart.this;
		return new Command() {
			@Override
			public void execute() {
				if (oldName == null)
					oldName = getArtFrag().getArt().queryName(getRepo());
				//IFigure me = getAnnoLabelFigure();
				if (manager == null)
					manager = new SAFEPLabelDirectEditManager(taep, TextCellEditor.class,
							new AnnoLabelCellEditorLocator(getAnnoLabelFigure()),
							getAnnoLabelFigure()) {
					@Override
					protected void commit() {
						super.commit();
						// Typing finished, so update Resource and repo with new name
						getArtFrag().setInstanceName(getAnnoLabelText());
//						updateResource();
					}
				};
				manager.show();
			}
		};
	}
	
//	private void updateResource() {
//		ReloRdfRepository repo = getRepo();
//
//		ArtifactFragment thisFrag = getArtFrag();
//		Resource oldRes = thisFrag.getArt().elementRes;
//		
//		String namePreEdit = getArtFrag().getArt().queryName(repo);
//		String newName = getAnnoLabelText();
//		
//		String newResString = oldRes.toString().replace(namePreEdit, newName);
//		newResString = newResString.substring(newResString.indexOf("#")+1); // removing namespace
//		Resource newRes = thisFrag.getRootArt().getBrowseModel().createResForUserCreatedFrag(newResString);
//		thisFrag.setArt(new Artifact(newRes));
//
//		repo.startTransaction();
//
//		StatementIterator iter = repo.getStatements(oldRes, null, null);
//		while(iter.hasNext()) {
//			Statement stmt = iter.next();
//			repo.addStatement(newRes, stmt.getPredicate(), stmt.getObject());
//		}
//		repo.removeStatements(oldRes, null, null);
//
//		iter = repo.getStatements(null, null, oldRes);
//		while(iter.hasNext()) {
//			Statement stmt = iter.next();
//			repo.addStatement(stmt.getSubject(), stmt.getPredicate(), newRes);
//		}
//		repo.removeStatements((Resource)null, null, oldRes);
//
//		repo.commitTransaction();
//	}

	public void setAnnoLabelText(String str) {
		if (str == null) return;
		if (str.contains("\n") || str.contains("\r") || str.contains("\t")) {
			// Not allowing multiple lines in text, so treating
			// user hitting Enter as the end of the edit
			str = str.replaceAll("\n", "");
			str = str.replaceAll("\r", "");
			str = str.replaceAll("\t", "");
			updateFigure(str);
			manager.commit();
			return;
		}
		updateFigure(str);
	}

	public IFigure getAnnoLabelFigure() { //		System.err.println(getFigure().getChildren().get(0));
		if (getFigure() instanceof ContainerFigure) {
			IFigure nameFig = ((ContainerFigure) getFigure()).getNameFig();
			if(nameFig.getChildren().size()>0 && 
					(IFigure) nameFig.getChildren().get(nameFig.getChildren().size()-1) instanceof Label)
				return (IFigure) nameFig.getChildren().get(nameFig.getChildren().size()-1);
			return null;
		}
		return null;
	}
	private IFigure getAnnoLabelFigure(ContainerFigure myFig) {
		IFigure nameFig = myFig.getNameFig();
		if (!nameFig.getChildren().isEmpty() && (IFigure) nameFig.getChildren().get(nameFig.getChildren().size()-1) instanceof Label)
			return (IFigure) nameFig.getChildren().get(nameFig.getChildren().size()-1);
		return null;
	}
	
	
	public String getAnnoLabelText() {
		return ((Label) getAnnoLabelFigure()).getText();
	}

	private void updateFigure(String newName) {
		((Label)getAnnoLabelFigure()).setText(newName);
		((Label)getAnnoLabelFigure()).setToolTip(new Label(newName));
	}

	public String getOldAnnoLabelText() {
		return oldName;
	}
	public void setOldAnnoLabelText(String oldName) {
		this.oldName = oldName;
	}
	
	public static Command getAddUserCreatedFrag(ContainableEditPart destEP, ContainableEditPart movedAFEP, StrataRootDoc strataRootDoc) {
		CompoundCommand showAFEPChildrenCmd = new CompoundCommand("Show Children");
		StrataArtFragEditPart.addSingleArt(showAFEPChildrenCmd, destEP.getFigure().getBounds().getCenter(), destEP, movedAFEP.getArtFrag(), strataRootDoc);
		DeleteCommand del = new DeleteCommand("", movedAFEP.getArtFrag(), strataRootDoc, movedAFEP.getArtFrag().getParentArt());
		showAFEPChildrenCmd.add(del);
		ClosedContainerDPolicy.setShowingChildren(destEP.getArtFrag(), true);
		return showAFEPChildrenCmd;
	}
}
