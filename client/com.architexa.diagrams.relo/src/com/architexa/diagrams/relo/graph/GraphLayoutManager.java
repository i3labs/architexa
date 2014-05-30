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
package com.architexa.diagrams.relo.graph;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jiggle.Cell;
import jiggle.Graph;
import jiggle.Vertex;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Control;

import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.diagrams.relo.agent.AgentManager;
import com.architexa.diagrams.relo.agent.ReloBrowseModel;
import com.architexa.diagrams.relo.commands.ServiceCommand;
import com.architexa.diagrams.relo.parts.MoreItemsEditPart;
import com.architexa.diagrams.relo.utils.SVGExport;
import com.architexa.org.eclipse.draw2d.AbstractLayout;
import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.GraphAnimation;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LayoutManager;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Insets;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.RootEditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editparts.AbstractConnectionEditPart;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;


public class GraphLayoutManager extends AbstractLayout {

    private static final Logger progressLogger = ReloPlugin.getLogger(GraphLayoutManager.class.getName() + ".Progress");
    private static final Logger logger = ReloPlugin.getLogger(GraphLayoutManager.class);
    
    public static final int prefEdgeLength = 35;
    public static final int minVertexEdgeDist = 5;
    public static final int prefVertexDist = (int) (prefEdgeLength * 1.25);


    /**
	 * The dummy layout class does nothing during normal layouts.  The Graph layout is
	 * entirely performed in one place: {@link GraphLayoutManager}, on the diagram's figure.
	 * During animation, THIS layout will playback the intermediate steps between the two
	 * invocations of the graph layout.
	 * @author hudsonr
	 */
	public static class SubgraphLayout extends AbstractLayout {
		/**
		 * @see com.architexa.diagrams.relo.eclipse.gef.AbstractLayout#calculatePreferredSize(com.architexa.diagrams.relo.eclipse.gef.IFigure, int, int)
		 */
		@Override
        protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
			return null;
		}
		/**
		 * @see com.architexa.diagrams.relo.eclipse.gef.LayoutManager#layout(com.architexa.diagrams.relo.eclipse.gef.IFigure)
		 */
		public void layout(IFigure container) {
			//	GraphAnimation.recordInitialState(container);
			GraphAnimation.playbackState(container);
		}

	}

    // note: even when disabling the layout manager, we do go through the whole
    //        process as usual, since the subgraphs then get resized 
    //        automatically
    private AgentManager.ViewAgent layoutManagerAgent = new AgentManager.ViewAgent() {
        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (enabled) {
                // force a relayout
                GraphLayoutManager.this.ageMgr.clearOldParts();
    			IFigure f = ((AbstractGraphicalEditPart)GraphLayoutManager.this.rootEditPart.getContents()).getFigure();
    			f.getLayoutManager().layout(f);
            }
        }
    };
    {
        ReloBrowseModel.connectToAllModels(layoutManagerAgent);
    }

    
    public RootEditPart rootEditPart;
    private GraphLayoutDiagram layoutDiag;
    private List<?> selectionTracker;

    public static final Insets PADDING = new Insets(8, 6, 8, 6);

    IGraphLayoutAgeMgr ageMgr = new GraphLayoutAgeMgr();

    // TODO: stubs into ageMgr - move methods or phase out?
	public void anchorPart(AbstractGraphicalEditPart agep, Point newLoc, Point oldLoc) {
		ageMgr.anchorPart(agep, newLoc, oldLoc);
	}
	public void anchorPart(AbstractGraphicalEditPart agep) {
		ageMgr.anchorPart(agep);
	}
	public boolean isPartAnchored(AbstractGraphicalEditPart agep) {
		return ageMgr.isPartAnchored(agep);
	}
    public boolean isLayedOut(AbstractGraphicalEditPart agep) {
    	return ageMgr.isLayedOut(agep);
    }
    public void setLayedOut(AbstractGraphicalEditPart agep) {
    	ageMgr.setLayedOut(agep);
    }
    

    
    public GraphLayoutManager(GraphLayoutDiagram diagram, RootEditPart rootEditPart, List<?> selectionTracker) {
		this.layoutDiag = diagram;
		this.selectionTracker = selectionTracker;
		this.rootEditPart = rootEditPart; 

        //jiggle.launchJiggle();
        
        // Do we want the below (relayout) working again?
		//  The code has the following problems:
		//  o Contribution needs to be added properly (to work for both editor and view)
		//  o Need to get relayout actually working (doesn't in new engine)
		/*
        EditPartViewer viewer = ((ReloController)diagram).getViewer();
        IEditorPart reloEditor = ((DefaultEditDomain) viewer.getEditDomain()).getEditorPart();
        Action layoutUpdaterAction = new Action("Update") {
            @Override
            public void run() {
                Object selectedEP = null;
                Cell selectedEPCell = null;
                for (Object selectedEPObj : GraphLayoutManager.this.selectionTracker) {
                    selectedEP = selectedEPObj;
                    selectedEPCell = (Cell) oldPartsToCellMap.get(selectedEP);
                }
                // make sure we have a focus
                oldPartsToCellMap.clear();
                if (selectedEPCell != null)
                    oldPartsToCellMap.put((AbstractGraphicalEditPart) selectedEP, selectedEPCell);

                undoableLayout();
            }
        };
        layoutUpdaterAction.setToolTipText("Update Layout");
        //trackerAction.setText("Layout");
        URL url = ReloPlugin.getDefault().getBundle().getEntry("icons/view-refresh.png");
        layoutUpdaterAction.setImageDescriptor(ImageDescriptor.createFromURL(url));
        IToolBarManager itbm = ((ReloEditorContributor)reloEditor.getEditorSite().getActionBarContributor()).getActionBars().getToolBarManager();
        itbm.add(layoutUpdaterAction);
        */
	}

	@Override
    protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
		// if (state == PLAYBACK)
		//		return container.getSize();
		container.validate();
		List<?> children = container.getChildren();
		Rectangle result =
			new Rectangle().setLocation(
				container.getClientArea().getLocation());

        for (int i = 0; i < children.size(); i++)
			result.union(((IFigure) children.get(i)).getBounds());

        result.resize(container.getInsets().getWidth(), container.getInsets().getHeight());

        return result.getSize();
	}

	void consoleLog(String str) {
		System.out.println(
			new Date().toString() + ":[" + this.getClass() + "] " + str);
	}

    public void layout(IFigure container) {
        GraphAnimation.recordInitialState(container);
        if (GraphAnimation.playbackState(container))
            return;

        try {
        	doRefresh();
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
        }
    }
    
    private void doRefresh() {
    	/* some properties: 
    	 * 1> we don't want this on the the command stack
    	 * 2> we are not really doing a layout, but containers might get resized
    	 * 3> we might want to (really) assert rules here
    	 * 
    	 * ideally want to eliminate doing anything here, but otherwise the top-level container has sizing issues  
    	 */ 
        //progressLogger.debug("Refresh - Starting!");
        
		Graph graph = new Graph();

		Map<AbstractGraphicalEditPart, Object> partsToCellMap = new HashMap<AbstractGraphicalEditPart, Object> ();
        layoutDiag.contributeNodesToGraph(graph, null, partsToCellMap);

        layoutDiag.contributeEdgesToGraph(graph, partsToCellMap);

        // by default all nodes are on top of each other, causing large forces
		// adjustNodeCoords(graph, partsToCellMap, oldPartsToCellMap);
		
        //GraphLayoutRules.assertRules(graph, partsToCellMap, anchoredParts);

        // [unfix if a cell has been forced to move ==> removed from refresh, esp. since we don't olden]

		//progressLogger.debug("Refresh Processing - Done! ");

        layoutDiag.applyGraphResults(graph, partsToCellMap);
		//progressLogger.debug("Refresh Manager - Layout Done! ");
    }


    /*
     * There are currently two types of Layout Commands:
     *  1> Instantiated: Done when the method is called (with commands providing undo and redo capabilities)
     *  2> Non-instantiated: This approach does the capture of the UI (to perform layout) when the command
     *     is executed (as opposed to when the command is created / method is called). This is more intuitive
     *     since the execute of another method that this method is chained to might want to do some layout,
     *     and therefore this is the default.
     *     
     *  TODO: see why instantiate layout even exists.
     */
    
    public Command getLayoutCmd() {
    	return getNonInstantiatedLayoutCmd();
    }
    public Command getInstantiatedLayoutCmd() {
        try {
            return getIncrementalLayoutCmd();
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
            return null;
        }
    }
    public Command getNonInstantiatedLayoutCmd() {
        try {
            return new ServiceCommand("Just-in-time Layout") {
                Command instantiatedLayoutCmd = null;
                @Override
                public void execute() {
                	instantiatedLayoutCmd = null;
                	if (instantiatedLayoutCmd == null) instantiatedLayoutCmd = getIncrementalLayoutCmd();
                    if (instantiatedLayoutCmd != null) instantiatedLayoutCmd.execute();
                }
                @Override
                public void undo() { 
                    if (instantiatedLayoutCmd != null) instantiatedLayoutCmd.undo();
                }
            };
            //return incrementalLayout();
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
            return null;
        }
    }
    public void undoableLayout() {
    	// XXX verify: we don't need to do this (supposedly) because this is being called from a command, and that will do the layout anyway
    	if (true) return;
        Command cmd = getInstantiatedLayoutCmd();
        //System.err.println("performing undoable layout");
        if (cmd != null)
            rootEditPart.getViewer().getEditDomain().getCommandStack().execute(cmd);
    }
	
    private Command getIncrementalLayoutCmd() {
    	// copy the original graph (needed for undo)
    	final Graph origGraph = new Graph();
        final Map<AbstractGraphicalEditPart, Object> origPartsToCellMap = new HashMap<AbstractGraphicalEditPart, Object> ();
        layoutDiag.contributeNodesToGraph(origGraph, null, origPartsToCellMap);
        layoutDiag.contributeEdgesToGraph(origGraph, origPartsToCellMap);

        
        progressLogger.debug("Layout - Starting!");
        
		Graph graph = new Graph();
		
		Map<AbstractGraphicalEditPart, Object> partsToCellMap = new HashMap<AbstractGraphicalEditPart, Object> ();
        layoutDiag.contributeNodesToGraph(graph, null, partsToCellMap);
		//printNodes(graph);

		// to keep positions
        AbstractGraphicalEditPart mainEP = getFocusedEP(partsToCellMap);
		Point mainEPPos = null;
		//AbstractGraphicalEditPart mainEP = null;
        if (mainEP != null) {
        	ageMgr.makeOld(mainEP);
        	mainEPPos = mainEP.getFigure().getBounds().getTopLeft();
            //mainEP.getFigure().translateToAbsolute(mainEPPos);
            //System.err.print("Storing position: " + mainEPPos + " - " + mainEPPos2 + " ");
            progressLogger.info("pos1 " + mainEPPos + " mainEP " + mainEP + " :: " + selectionTracker.size());
        }

        layoutDiag.contributeEdgesToGraph(graph, partsToCellMap);

        // grab old cells first (adjusting node coords 'oldens' all cells)

        // by default all nodes are on top of each other, causing large forces
        Point defaultPos = null;
        if (mainEP != null)
        	defaultPos = mainEP.getFigure().getBounds().getCenter();
        if (defaultPos == null) 
        	defaultPos = getCenter();
		adjustNodeCoords(graph, partsToCellMap, defaultPos);
		
        GraphLayoutRules.assertRules(graph, partsToCellMap, ageMgr.getOldParts());

        
		if (mainEP != null) {
            // TODO: why are we getting the position a second time (is it because of the adjusting?
		    mainEPPos = ((Cell) partsToCellMap.get(mainEP)).getBounds().getTopLeft();
		    progressLogger.info("pos2 " + mainEPPos + " mainEP " + mainEP + " :: " + selectionTracker.size());
		}
		




		
		//System.err.println("POST-VISIT-DUMP");
		//printNodes(graph);
		//System.err.println("");
		//printEdges(graph);
		//exportGraph(graph, myDesktop, 0);
		//log("visit graph <<<");

        
        GraphLayoutRules.assertRules(graph, partsToCellMap, ageMgr.getOldParts());


		//exportDiagram(layoutDiag);
		//consoleLog("layout <<<");

		progressLogger.debug("Layout Processing - Done! ");


        // create command

        //final Map<AbstractGraphicalEditPart, Object> newPartsToCellMap = new HashMap<AbstractGraphicalEditPart, Object> (partsToCellMap);
        ///*
        // do a deep copy of partsToCellMap
        final Map<AbstractGraphicalEditPart, Object> newPartsToCellMap = new HashMap<AbstractGraphicalEditPart, Object> (partsToCellMap.size());
        for (Map.Entry<AbstractGraphicalEditPart, Object> partsToCellEntry : partsToCellMap.entrySet()) {
        	if (partsToCellEntry.getValue() instanceof Vertex)
    			newPartsToCellMap.put(partsToCellEntry.getKey(), ((Vertex)partsToCellEntry.getValue()).clone());
        	else
        		newPartsToCellMap.put(partsToCellEntry.getKey(), partsToCellEntry.getValue());
		}
		//*/
        
        final Graph newGraph = graph;
        //printChanges(2, newGraph.vertices, origPartsToCellMap, newPartsToCellMap);
        //printChanges(2, newGraph.vertices, oldPartsToCellMap, origPartsToCellMap);
        //printChanges(3, newGraph.vertices, origPartsToCellMap, newPartsToCellMap);

        ageMgr.updateAges(partsToCellMap.keySet());

        return new Command("Instantiated Layout") {
            @Override
            public void execute() {
                layoutDiag.applyGraphResults(newGraph, newPartsToCellMap);
            }
            @Override
            public void undo() {
                layoutDiag.applyGraphResults(origGraph, origPartsToCellMap);
            }
        };
    }

	private org.eclipse.swt.graphics.Point getWindowSize() {
		Control rootCtrl = rootEditPart.getViewer().getControl();
        org.eclipse.swt.graphics.Point rootCtrlSize = rootCtrl.getSize();
        while (rootCtrlSize.x == 0 || rootCtrlSize.y == 0) {
            rootCtrl = rootCtrl.getParent();
            rootCtrlSize = rootCtrl.getSize();
        }
		return rootCtrlSize;
	}
	private Point getCenter() {
		org.eclipse.swt.graphics.Point wndSize = getWindowSize();
		return new Point(wndSize.x / 2, wndSize.y / 2);
	}
    
    @SuppressWarnings("unchecked")
	private AbstractGraphicalEditPart getFocusedEP(Map<AbstractGraphicalEditPart, Object> partsToCellMap) {
        for (Object selectedObj : new ArrayList(selectionTracker)) {
            EditPart selectedEP = (EditPart) selectedObj;
            while (selectedEP != null && !partsToCellMap.containsKey(selectedEP)) {
                // go up parent tree to node that is layed out by the layout manager
                selectedEP = selectedEP.getParent();
            }
            if (selectedEP != null) {
            	// remove stale data
            	if (!partsToCellMap.containsKey(selectedEP)) {
            		selectionTracker.remove(selectedEP);
            		continue;
            	}
            	// make sure that the selected item is not an items that has just been created
				if (ageMgr.isOldPart(selectedEP)) {
				    return (AbstractGraphicalEditPart) selectedEP;
				}
			}
        }
		return null;
	}

	private static int getGraphID(Object obj) {
    	Cell cell = (Cell) obj;
		return cell.getGraph().id;
    }
    
    @SuppressWarnings("unused")
	private static void printChanges(int hdr, Cell[] vertices, Map<AbstractGraphicalEditPart, Object> begPartsToCellMap, Map<AbstractGraphicalEditPart, Object> endPartsToCellMap) {
    	for (Cell cell : vertices) {
			if (!(cell instanceof Vertex)) continue;
			if (cell.data == null) continue;
			MoreItemsEditPart aep = (MoreItemsEditPart) cell.data;
			
			if (begPartsToCellMap==null || !begPartsToCellMap.containsKey(aep)) continue;
			
			Rectangle begBounds = ((Cell)begPartsToCellMap.get(aep)).getBounds();

			Rectangle endBounds = ((Cell)endPartsToCellMap.get(aep)).getBounds();

			printMove(hdr, aep, getGraphID(begPartsToCellMap.get(aep)), begBounds, getGraphID(endPartsToCellMap.get(aep)), endBounds);
		}
	}

	private static void printMove(int hdr, MoreItemsEditPart aep, int begGrID, Rectangle begBounds, int endGrID, Rectangle endBounds) {
		Point begTL = begBounds.getTopLeft();
		Point endTL = endBounds.getTopLeft();
		System.err.print( hdr + ": ");
		System.err.print(aep.getLabel(aep.getArtifact().getArt(), null) + ": ");
		if (endTL.getDifference(begTL).getExpanded(1,1).getArea() > 1) {
			System.err.print(begBounds.getTopLeft() + " --> " + endBounds.getTopLeft());
		} else {
			System.err.print("                       " + endBounds.getTopLeft());
		}
		System.err.println("  move: " + endTL.getDifference(begTL) + ": " + begGrID + "..." + endGrID);
		//(new Exception()).printStackTrace();
	}
    
    

    
    /*
	 * By default all nodes are on top of each other, causing large forces
     * Move new nodes 
     */
    private void adjustNodeCoords(Graph graph, Map<AbstractGraphicalEditPart, Object> partsToCellMap, Point defaultPos) {
        Set<AbstractGraphicalEditPart> newParts = new HashSet<AbstractGraphicalEditPart> ();
        newParts.addAll(partsToCellMap.keySet());
        newParts.removeAll(ageMgr.getOldParts());
        Set<AbstractGraphicalEditPart> newEdges = new HashSet<AbstractGraphicalEditPart> (newParts);
        CollectionUtils.filter(newParts, PredicateUtils.notPredicate(PredicateUtils.instanceofPredicate(AbstractConnectionEditPart.class)));
        newEdges.removeAll(newParts);
        
        for (AbstractGraphicalEditPart edge : new HashSet<AbstractGraphicalEditPart> (newEdges)) {
            if (nodeConstrainedConn((AbstractConnectionEditPart) edge)) newEdges.remove(edge);
        }
        
        //System.err.println("adjusting graph: " + newNodes.size() + " done: " + oldParts.size());
        GraphLayoutRules.assertRulesForNewParts(graph, newParts, newEdges, partsToCellMap, ageMgr, defaultPos, this, rootEditPart);
        
        // save for next time
        //fixedLocationParts.addAll(newNodes);
    }


    private boolean nodeConstrainedConn(AbstractConnectionEditPart conn) {
        AbstractGraphicalEditPart srcAGEP = (AbstractGraphicalEditPart) conn.getSource();
		EditPart rootEP = srcAGEP.getRoot().getContents();
        while (srcAGEP != null && srcAGEP != rootEP) {
            LayoutManager lm = srcAGEP.getContentPane().getLayoutManager();
            if (lm instanceof ToolbarLayout || lm instanceof FlowLayout) {
                if (nodeConstrainedConn(srcAGEP, conn)) return true;
            }
            srcAGEP = (AbstractGraphicalEditPart) srcAGEP.getParent();
        }
        return false;
    }
    private boolean nodeConstrainedConn(AbstractGraphicalEditPart srcAGEP, AbstractConnectionEditPart conn) {
        AbstractGraphicalEditPart tgtAGEP = (AbstractGraphicalEditPart) conn.getTarget();
    	EditPart rootEP = srcAGEP.getRoot().getContents();
        while (tgtAGEP != null && srcAGEP != rootEP) {
            if (tgtAGEP == srcAGEP) return true;
            tgtAGEP = (AbstractGraphicalEditPart) tgtAGEP.getParent();
        }
        return false;
    }
    
	@SuppressWarnings("unused")
	private void exportDiagram(GraphLayoutDiagram diagram) {
	    String fileName = System.getProperty("user.home") + "/Desktop/sgef/export.svg";

	    SVGExport svgFile = null;
		try {
            svgFile = new SVGExport(fileName);
		} catch (FileNotFoundException e) {
            logger.error("Unexpected exception", e);
			return;
		}
        
        svgFile.dumpHeader();

		IFigure fig = diagram.getDiagram().getFigure();
        svgFile.exportFigure(fig);

        svgFile.dumpFooter();
	}

    @SuppressWarnings("unused")
	private void exportGraph(Graph graph, String fileBase, int ndx) {
        SVGExport svgFile = null;
        try {
            svgFile =
                new SVGExport(fileBase + ndx +  ".svg");
        } catch (FileNotFoundException e) {
            logger.error("Unexpected exception", e);
            return;
        }
        svgFile.dumpHeader();

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            Cell n = graph.vertices.get(i);
            svgFile.exportNode(n, "");
        }

        for (jiggle.Edge edge : graph.edges) {
            svgFile.dumpLine(edge.getFrom(), edge.getTo());
        }

        svgFile.dumpFooter();
    }


    @SuppressWarnings("unused")
	private void printNodes(Graph graph) {
		System.out.println("Nodes");
		for (int i = 0; i < graph.getNumberOfVertices(); i++) {
			Cell n = graph.vertices.get(i);
			System.out.println(
				"["
					+ i
					+ " - "
					+ n.getMin()[0]
					+ ","
					+ n.getMin()[1]
					+ "x"
					+ n.getSize()[0]
					+ ","
					+ n.getSize()[1]
					+ "]: "
					+ n
					+ ":"
					+ n.getClass()
					+ "/"
					+ n.data
					+ ":"
					+ n.data.getClass()
					);
		}
    }

}