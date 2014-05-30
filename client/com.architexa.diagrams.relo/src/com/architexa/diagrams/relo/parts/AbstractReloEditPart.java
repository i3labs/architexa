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
 * Created on Jun 13, 2004
 *
 */
package com.architexa.diagrams.relo.parts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jiggle.Cell;
import jiggle.Graph;
import jiggle.Subgraph;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.MenuManager;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.parts.RSEEditPart;
import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.relo.agent.ReloBrowseModel;
import com.architexa.diagrams.relo.commands.CreateCommand;
import com.architexa.diagrams.relo.commands.InteriorMoveCommand;
import com.architexa.diagrams.relo.figures.SubgraphFigure;
import com.architexa.diagrams.relo.graph.GraphLayoutDiagram;
import com.architexa.diagrams.relo.graph.GraphLayoutEditPolicy;
import com.architexa.diagrams.relo.graph.GraphLayoutManager;
import com.architexa.diagrams.relo.modelBridge.ReloDoc;
import com.architexa.diagrams.utils.BuildPreferenceUtils;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.draw2d.GraphAnimation;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LayoutManager;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Insets;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.RequestConstants;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.commands.UnexecutableCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.editpolicies.ComponentEditPolicy;
import com.architexa.org.eclipse.gef.requests.ChangeBoundsRequest;
import com.architexa.org.eclipse.gef.requests.CreateRequest;
import com.architexa.store.ReloRdfRepository;


/**
 * Functionality needed for both ReloController and CodeUnitEditPart, i.e.
 * 1] functionality needed to be a container for CodeUnitEditPart's
 * 
 * @author vineet
 */
public abstract class AbstractReloEditPart extends RSEEditPart
		implements GraphLayoutDiagram {

	static final Logger logger = ReloPlugin.getLogger(AbstractReloEditPart.class);
    
	public ReloBrowseModel getBrowseModel() {
		return ((ReloController) getRoot().getContents()).bm;
	}
	// utility methods - these should not change (which is why they are final)
	public ReloController getRootController() {
	    return (ReloController) getRoot().getContents();
	}
	public final ReloRdfRepository getRepo() {
		return ((ReloController) getRoot().getContents()).bm.getRepo();
	}

	@Deprecated
    public void executeWithoutLayout(Command cmd) {
        getViewer().getEditDomain().getCommandStack().execute(cmd);
    }
    @Override
    public void execute(Command cmd) {
        if (cmd == UnexecutableCommand.INSTANCE) return;    // don't add these to the queue

        ReloController rc = getRootController();
        Command cmdWithLayout = null;
        // do layout after every command
        //unless flag has been set by cmd
        if (cmd.getLabel() != null && !cmd.getLabel().contains("Hide Package")) {
        	cmdWithLayout = cmd.chain(getRootController().getLayoutCmd());
        	cmdWithLayout .setLabel(cmd.getLabel());
        } else
        	cmdWithLayout = cmd;
		
    	getViewer().getEditDomain().getCommandStack().execute(cmdWithLayout);
        
        
        // and then updateVisibility
        rc.updateVisibility();
    }
	
	// TODO Invesitgate if the more appropriate place for these agents are policies

	Object agentManagerToken = null;
	/* (non-Javadoc)
	 * @see org.eclipse.gef.EditPart#activate()
	 */
	@Override
    public void activate() {
		//System.err.println(this.getClass() + " activiting!");
		super.activate();
		agentManagerToken = getBrowseModel().initializePart(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.EditPart#deactivate()
	 */
	@Override
    public void deactivate() {
		//System.err.println(this.getClass() + " deactiviting!");
		super.deactivate();
		if (agentManagerToken != null)
			getBrowseModel().cleanPart(this, agentManagerToken);
		else
			logger.error("Unexpected: agentManagerToken == null");
	}

	public void buildModelContextMenu(MenuManager subMenu, String groupName) {
        if (ReloPlugin.getDefault().isDebugging())
            getBrowseModel().buildModelContextMenu(subMenu, groupName, this, agentManagerToken);
    }

	
	// Graph layout support
	protected boolean subGraphContainer() {
        //if (getChildren().size() == 0) return false;

        LayoutManager lm = getContentPane().getLayoutManager();
		if (lm instanceof GraphLayoutManager.SubgraphLayout) return true;
        if (lm instanceof GraphLayoutManager) return true;

        return false;
	}

    public void contributeEdgesToGraph(Graph graph, Map<AbstractGraphicalEditPart,Object> partsToCellMap) {
		List<?> outgoing = getSourceConnections();
		for (int i = 0; i < outgoing.size(); i++) {
			// ignore non-relo connections
			if (!(outgoing.get(i) instanceof AbstractReloRelationPart)) continue;
		    AbstractReloRelationPart relPart = (AbstractReloRelationPart) outgoing.get(i);
		    relPart.contributeEdgeToGraph(graph, partsToCellMap);
		}
		for (int i = 0; i < getChildren().size(); i++) {
			if(!(children.get(i) instanceof AbstractReloEditPart)) continue;
			AbstractReloEditPart child = (AbstractReloEditPart) children.get(i);
			child.contributeEdgesToGraph(graph, partsToCellMap);
		}
    }

	public void contributeNodesToGraph(Graph graph, Subgraph sg, Map<AbstractGraphicalEditPart,Object> partsToNodesMap) {
		if (subGraphContainer()) {
			Subgraph mySG = contributeSubgraphToGraph(graph, sg, partsToNodesMap);
			contributeChildrenToGraph(graph, mySG, partsToNodesMap);
		} else {
			contributeNodeToGraph(graph, sg, partsToNodesMap);
		}
	}
	
    protected void contributeNodeToGraph(Graph graph, Subgraph sg, Map<AbstractGraphicalEditPart,Object> partsToNodesMap) {
	    Cell node = graph.insertVertex();
	    Dimension prefSize = getFigure().getPreferredSize();
	    Rectangle nodeBounds = getFigure().getBounds().getCopy();
	    nodeBounds.setSize(prefSize);
	    node.setBounds(nodeBounds);

	    node.data = this;
		partsToNodesMap.put(this, node);
		//tag.consolePrintln("CONTRIBUTION: Vertex - " + this);

		if (sg != null) {
            sg.insertVertex(node);
        }
    }

	final int subgraphWidth = 10;
	final int subgraphHeight = 10;
	protected Subgraph contributeSubgraphToGraph(Graph graph, Subgraph sg, Map<AbstractGraphicalEditPart,Object> partsToNodesMap) {
        GraphAnimation.recordInitialState(getContentPane());

        Subgraph mySG = new Subgraph(graph);
		graph.insertVertex(mySG);
		if (sg != null) {
            sg.insertVertex(mySG);
        }
		
	    //Dimension prefSize = getFigure().getPreferredSize(); 
		//mySG.setBounds(getFigure().getBounds());
		Rectangle currBounds = getFigure().getBounds();
		mySG.setBounds(new Rectangle(currBounds.x, currBounds.y, subgraphWidth, subgraphHeight));
		//mySG.setSize(prefSize.width, prefSize.height);

		mySG.data = this;
		partsToNodesMap.put(this, mySG);
		
        mySG.subgraphBorder = contributeSubgraghInsets();
        
        return mySG;
    }
    
    protected Insets contributeSubgraghInsets() {
        Insets insets = new Insets(GraphLayoutManager.PADDING);
        IFigure fig = getFigure();
        if (fig instanceof SubgraphFigure) {
            insets.top += ((SubgraphFigure)fig).getHeader().getSize().height;
            //insets.bottom += ((SubgraphFigure)fig).getFooter().getSize().height;
        }
        return insets; 
    }
	
    protected void contributeChildrenToGraph(Graph graph, Subgraph mySG, Map<AbstractGraphicalEditPart,Object> partsToNodesMap) {
		for (int i = 0; i < getChildren().size(); i++) {
			if(!(children.get(i) instanceof AbstractReloEditPart)) continue;
            AbstractReloEditPart cuep = (AbstractReloEditPart) getChildren().get(i);
            cuep.contributeNodesToGraph(graph, mySG, partsToNodesMap);
        }
    }

    public void applyGraphResults(Graph graph, Map<AbstractGraphicalEditPart,Object> partsToCellMap) {
		applyOwnResults(graph, partsToCellMap);
		applyChildrenResults(graph, partsToCellMap);
    }

	protected void applyOwnResults(Graph graph, Map<AbstractGraphicalEditPart,Object> partsToCellMap) {
	    Cell n = (Cell) partsToCellMap.get(this);
        if (n==null) return;
        //if (!(this instanceof ReloController) && getFigure().getBounds().getTopLeft().getDifference(n.getBounds().getTopLeft()).getArea() > 25)
        //	System.err.println("AREP.applyOwnResults " + this + " " + getFigure().getBounds().getTopLeft() + " --> " + n.getBounds().getTopLeft() + " id: " + ((Graph)n.getContext()).id);
		getFigure().setBounds(n.getBounds());
		
		
	    //Rectangle r = n.getBounds();
		//Dimension size = getFigure().getPreferredSize(r.width,r.height);
		//Rectangle prefBounds = new Rectangle(new Point(r.x, r.y), size);
		////Rectangle prefBounds = new Rectangle(new Point(0, 0), size);
		//getFigure().setBounds(prefBounds);
		//System.err.println("Setting bounds for: " + this.getClass() + "/" + this + " to: " + prefBounds);
		////Dimension size = getFigure().getPreferredSize(n.width,n.height);
		////Rectangle prefBounds = new Rectangle(new Point(n.x, n.y), size);
		////part.getFigure().setBounds(prefBounds);
	
		for (int i = 0; i < getSourceConnections().size(); i++) {
		    if (!(getSourceConnections().get(i) instanceof AbstractReloRelationPart)) continue;
			AbstractReloRelationPart rel = (AbstractReloRelationPart) getSourceConnections().get(i);
			rel.applyGraphResults(graph, partsToCellMap);
		}
	}

	protected void applyChildrenResults(Graph graph, Map<AbstractGraphicalEditPart,Object> partsToCellMap) {
		if (subGraphContainer()) {
			for (int i = 0; i < getChildren().size(); i++) {
				if(!(children.get(i) instanceof AbstractReloEditPart)) continue;
				AbstractReloEditPart part = (AbstractReloEditPart)getChildren().get(i);
				part.applyGraphResults(graph, partsToCellMap);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.architexa.diagrams.relo.graph.GraphLayoutManager.IDiagram#getDiagram()
	 */
	public GraphicalEditPart getDiagram() {
		return this;
	}

	@Override
    protected void createEditPolicies() {
	    //super.createEditPolicies();
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new GraphLayoutEditPolicy());
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentEditPolicy() {
            @Override
            public Command getCommand(Request request) {
                if (REQ_ORPHAN.equals(request.getType())) {
                    return getOrphanCommand((ChangeBoundsRequest) request);
                }
                return super.getCommand(request);
            }
            // TODO: we need to figure out the right place to do this (since
            // ComponendEditPolicy is not supposed to be a GraphicalEditPolicy
            // Code below based on NonResizableEditPolicy.getMoveCommand
            protected Command getOrphanCommand(ChangeBoundsRequest request) {
                // we create a ChangeBoundsRequest to have location provided
                ChangeBoundsRequest req = new ChangeBoundsRequest(REQ_ORPHAN_CHILDREN);
                req.setEditParts(getHost());
                
                req.setMoveDelta(request.getMoveDelta());
                req.setSizeDelta(request.getSizeDelta());
                req.setLocation(request.getLocation());
                req.setExtendedData(request.getExtendedData());
                return getHost().getParent().getCommand(req);
            }
        });
        installEditPolicy(ZOrderEditPolicy.ZORDER_ROLE, new ZOrderEditPolicy());
	}
	
	@Override
    protected abstract IFigure createFigure();

	
	public IFigure getLabelFigure() {
	    return getFigure();
	}
	
	@Override
    public Command getCommand(Request request) {
	    if (request.getType().equals(RequestConstants.REQ_CREATE)) {
            // we assume that the requests are for (possibly single item) lists
            return getEditPartCreationCommand((CreateRequest) request);
	    }
	    if (request.getType().equals(REQ_ADD)) {
	    	@SuppressWarnings("unchecked")
			List<EditPart> selectedEPs = ((ChangeBoundsRequest)request).getEditParts();
	    	CompoundCommand compoundMoveCmd = new CompoundCommand("Move Item");
	    	for (EditPart ep : selectedEPs) {
			    final ArtifactFragment reqChildAF = (ArtifactFragment) ep.getModel();
			    ArtifactFragment closestParent = CreateCommand.findClosestParent(reqChildAF, getRootController(), ((ChangeBoundsRequest) request).getLocation());
			    
			    EditPart srcParentEP = ((EditPart) ((ChangeBoundsRequest)request).getEditParts().get(0)).getParent().getParent();
			    if (!(srcParentEP.getModel() instanceof ArtifactFragment) || srcParentEP.getModel() instanceof ReloDoc) return null;
			    int oldIndex = ((ArtifactFragment) srcParentEP.getModel()).getShownChildren().indexOf(reqChildAF);
			    
			    if (closestParent != null)
			    	compoundMoveCmd.add(new InteriorMoveCommand(getRootController().getRootArtifact(), closestParent, reqChildAF, -1, oldIndex, false));
	    	}
	    	return compoundMoveCmd;
	    }  
	    if (request.getType().equals(REQ_RESIZE_CHILDREN)) {
	    	Command cmd = RootEditPartUtils.getResizeCommand((ChangeBoundsRequest)request);
	    	if(cmd!=null) return cmd;
	    }
		return super.getCommand(request);
	}
	
	
    @SuppressWarnings("unchecked")
	private Command getEditPartCreationCommand(final CreateRequest req) {

    	// Set enclosing parent of user created frags
    	List<Object> reqList;
    	Object newObj = req.getNewObject();
		if ( newObj instanceof List<?>) {
    		reqList = (List<Object>)newObj;
    	} else {
    		reqList = new ArrayList<Object>();
    		reqList.add(newObj);
    	}
		if (!BuildPreferenceUtils.selectionInBuild(reqList)) return null;

    	if(newObj instanceof ArtifactFragment &&
    			((ArtifactFragment)newObj).getArt()!=null &&
    			(RSECore.isUserCreated(getRepo(), ((ArtifactFragment)newObj).getArt().elementRes) || getBrowseModel().isUserCreated((ArtifactFragment) newObj)))
    		setPaletteFragParent((ArtifactFragment)newObj, req.getLocation());

		CompoundCommand cc = new CompoundCommand();
		CreateCommand createCmd = new CreateCommand(getRootController(), req);
		
//		if (createCmd.doLayout) {
//			Command cmdWithLayout = createCmd.chain(getRootController().getLayoutCmd());
//	    	cmdWithLayout.setLabel(createCmd.getLabel());
//	    	cc.add(cmdWithLayout);
//		} else
//			cc.add(createCmd);
		
		cc.add(createCmd);
		((ReloDoc) getRootController().getRootArtifact()).showIncludedRelationships(cc, createCmd.getAddedAFs());

		// this.getRootController().execute(cc);
		// do not execute here since we do not want to layout if one item is being dragged in
		if (createCmd.doLayout)
			cc.add(getRootController().getLayoutCmd());
		return cc;
    }

    private void setPaletteFragParent(ArtifactFragment paletteFrag, Point dropLocation) {
    	// Check children to make sure we find 
    	// the tightest enclosing parent frag
    	for (Object childEP : getChildren()) {
    		if(!(childEP instanceof AbstractReloEditPart)) continue;

    		// If a child is the target, let it handle setting the enclosing frag
    		if(((AbstractReloEditPart)childEP).getFigure().containsPoint(dropLocation))
    			return;
    	}

    	// This contains the drop location and none of its children do, 
    	// so this must be the tightest enclosing parent frag.
    	// If this is the root, setting the parent to null
    	ArtifactFragment parentFrag = (this instanceof ReloController) ? null : (ArtifactFragment)getModel();
    	
    	// Make sure that the parent is a class/interface or package and not a field or method 
    	if (parentFrag != null)
    		parentFrag = getBrowseModel().findContainerParent(parentFrag);

    	getBrowseModel().setUserCreatedEnclosingFrag(paletteFrag, parentFrag);
    }
    
    /*
	 * Utility methods 
	 */
	
	public boolean isArtifactVisible(Artifact cu) {
		return (findEditPart(cu) != null);
	}
	
	@SuppressWarnings("unchecked")
	public void bringToFront() {
		if (!(this.getParent() instanceof AbstractReloEditPart)) return;

		// bring parent front first (if possible)
		AbstractReloEditPart parentEP = (AbstractReloEditPart) this.getParent();
		parentEP.bringToFront();
		
		// bring me front (not possible in tool bar)
		if (!((AbstractReloEditPart)this.getParent()).subGraphContainer()) return; 

		List<Object> siblings = parentEP.getModelChildren();
		boolean success = siblings.remove(this.getModel());
		if (!success) {
			logger.error("Could not find me in my parent model");
			return;
		}
		siblings.add(this.getModel());
		
		parentEP.refreshChildren();
	}
	
}
