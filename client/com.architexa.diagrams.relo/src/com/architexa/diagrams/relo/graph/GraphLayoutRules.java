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
 * Created on Apr 11, 2006
 */
package com.architexa.diagrams.relo.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jiggle.Cell;
import jiggle.Edge;
import jiggle.Graph;
import jiggle.PointedEdge;
import jiggle.Vertex;

import org.apache.log4j.Logger;

import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.RootEditPart;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.store.ReloRdfRepository;


public class GraphLayoutRules {
    static final Logger logger = ReloPlugin.getLogger(GraphLayoutRules.class);

    /**
     * This currently does nothing
     * @param graph
     * @param partsToCellMap
     * @param anchoredParts
     */
    public static void assertRules(Graph graph, Map<AbstractGraphicalEditPart,Object> partsToCellMap, Set<AbstractGraphicalEditPart> oldParts) {
        if (true) return;
        
        Set<AbstractGraphicalEditPart> newNodes = new HashSet<AbstractGraphicalEditPart> ();
        newNodes.addAll(partsToCellMap.keySet());
        newNodes.removeAll(oldParts);
        
        //System.err.println("adjusting graph: " + newNodes.size() + " done: " + oldParts.size());
        Iterator<AbstractGraphicalEditPart> it = newNodes.iterator();
        int adjustedCount = oldParts.size();
        while (it.hasNext()) {
            Cell v = (Cell) partsToCellMap.get(it.next());
            if (v==null) continue;
            Rectangle bounds = v.getBounds();
            bounds.x += 50 * adjustedCount;
            bounds.y += 50 * adjustedCount;
            v.setBounds(bounds);
            //System.err.println("adjusting: " + v + " new: " + bounds);
            
            adjustedCount++;
        }

        // done
    }

    private IGraphLayoutAgeMgr ageMgr;
	private Set<AbstractGraphicalEditPart> positionedParts = null;
	private Map<AbstractGraphicalEditPart, Object> partsToCellMap = null;
	private GraphLayoutManager graphLayoutManager;
	private RootEditPart rootEditPart;
    public GraphLayoutRules(IGraphLayoutAgeMgr _ageMgr, Map<AbstractGraphicalEditPart, Object> _partsToCellMap, GraphLayoutManager _graphLayoutManager, RootEditPart _rootEditPart) {
		ageMgr = _ageMgr;
		partsToCellMap = _partsToCellMap;
		positionedParts = new HashSet<AbstractGraphicalEditPart>();
		graphLayoutManager = _graphLayoutManager;
		rootEditPart = _rootEditPart;
	}

    public static void assertRulesForNewParts(Graph graph,
			Set<AbstractGraphicalEditPart> newParts,
			Set<AbstractGraphicalEditPart> newEdges,
			Map<AbstractGraphicalEditPart, Object> partsToCellMap,
			IGraphLayoutAgeMgr ageMgr,
			Point defaultPos, 
			GraphLayoutManager graphLayoutManager, 
			RootEditPart rootEditPart) {
    	GraphLayoutRules glr = new GraphLayoutRules(ageMgr, partsToCellMap, graphLayoutManager, rootEditPart);
    	glr.assertRulesForNewParts(graph, newParts, newEdges, defaultPos);
    }

    public void assertRulesForNewParts(Graph graph,
			Set<AbstractGraphicalEditPart> newParts,
			Set<AbstractGraphicalEditPart> newEdges,
			Point defaultPos) {

    	//logger.error("Applying rules... "
    	//		+ " Default Pos: " + defaultPos
    	//		+ " New parts cnt: " + newParts.size() 
    	//		+ " New edges cnt: " + newEdges.size());

        
        
        // Rule I: Set all newParts to have their centers to defaultPos (which
		// is likely the most recent focused item)
        for (AbstractGraphicalEditPart nppAGEP : newParts) {
            Object c = (Cell) partsToCellMap.get(nppAGEP);
            if (!(c instanceof Vertex)) continue;
            Vertex v = (Vertex) c;
            v.setCenter(defaultPos);
        }
        
        
        // New Rule II: align all nodes with no connections in a table with width sqrt(n)
        // this prevents long horizontal layouts of many unrelated nodes
        // this grid will be modified by the following connection/overlapping rules        
        List<Vertex> lonelyNodes = new ArrayList<Vertex>();
        for (AbstractGraphicalEditPart part1 : newParts) {
            Object c1 = partsToCellMap.get(part1);
            if (!(c1 instanceof Vertex)) continue;
            Vertex v1 = (Vertex) c1;	
			if(v1.getOutEdges().isEmpty()) {
				lonelyNodes.add(v1);
			}
        }
        if (lonelyNodes.size() > 4 ) {
        	layoutLonelyNodes(lonelyNodes);
        }
        
        // Rule III
        // when adding a method to a class, expand all elements relative to classes expansion
        Map<AbstractGraphicalEditPart, Rectangle> partToOrgSizeMap = new HashMap<AbstractGraphicalEditPart, Rectangle>();
        Map<AbstractGraphicalEditPart, Rectangle> partToNewSizeMap = new HashMap<AbstractGraphicalEditPart, Rectangle>();
        // create map of orginal sizes
        for (AbstractGraphicalEditPart part : partsToCellMap.keySet()) {
        	Object c = partsToCellMap.get(part);
            if (!(c instanceof Vertex)) continue;
        	partToOrgSizeMap.put(part, new Rectangle(part.getFigure().getBounds()));
        }
        // refresh sizes
        graphLayoutManager.calculatePreferredSize(((AbstractGraphicalEditPart) rootEditPart).getFigure(),0,0); 
    	
        // create map of new sizes (if they have changed)
        for (AbstractGraphicalEditPart part : partsToCellMap.keySet()) {
    		Object c = partsToCellMap.get(part);
            if (!(c instanceof Vertex)) continue;
        	if (!partToOrgSizeMap.get(part).equals(new Rectangle(part.getFigure().getBounds())))
        		partToNewSizeMap.put(part, new Rectangle(part.getFigure().getBounds()));
        	
        }
        
        // move other nodes relative to the changed nodes
        for (AbstractGraphicalEditPart newPart : partToNewSizeMap.keySet()) {
        	int xDiff = partToNewSizeMap.get(newPart).width - partToOrgSizeMap.get(newPart).width;
        	int yDiff = partToNewSizeMap.get(newPart).height - partToOrgSizeMap.get(newPart).height;
			if (newParts.contains(newPart)) continue; // do not move for new nodes
        	for (AbstractGraphicalEditPart part : partsToCellMap.keySet()) {
        		Object c1 = partsToCellMap.get(part);
    			Object c2 = partsToCellMap.get(newPart);
    			if (!(c1 instanceof Vertex) || !(c2 instanceof Vertex)) continue; // only move vertexes
        		if (part.getFigure().getBounds().x > partToOrgSizeMap.get(newPart).x) 
        			smartMove((Vertex)c1, (Vertex)c2, new Dimension(xDiff,0), false);
        		if (part.getFigure().getBounds().y > partToOrgSizeMap.get(newPart).y) 
        			smartMove((Vertex)c1, (Vertex)c2, new Dimension(0 ,yDiff), false);
        	}
        }
        
        // Rule IV: Enforce New Edges (ignore the one with the older age)
        // new relationships should result in corresponding new nodes to be placed in the correct posistion
        // Use a copy so that removal of parts does not cause problems with overlapping prevention
        enforceNewEdges(newEdges, new HashSet<AbstractGraphicalEditPart>(newParts));
        
        // do we need a rule for compacting?
        
        // Rule V: Prevent overlapping of parts (we only move items that are very new, say age<2)
        // we basically have two loops
        for (AbstractGraphicalEditPart part1 : partsToCellMap.keySet()) {
            Object c1 = partsToCellMap.get(part1);
            if (!(c1 instanceof Vertex)) continue;
            Vertex v1 = (Vertex) c1;
            for (AbstractGraphicalEditPart part2 : partsToCellMap.keySet()) {
                Object c2 = partsToCellMap.get(part2);
                if (!(c2 instanceof Vertex)) continue;
                Vertex v2 = (Vertex) c2;
                if ( (v1 != v2) && v1.getBounds().intersects(v2.getBounds()))
                	// make sure atleast one node is new to de-overlap 
                	if ((newParts.contains(v1.data) || newParts.contains(v2.data))) 
                		enforceNonOverlapping(part1, part2, ageMgr, partsToCellMap);
            }
        }
    }

	private void layoutLonelyNodes(List<Vertex> lonelyNodes) {
		int xIndex = lonelyNodes.get(0).getBounds().x;
        int yIndex = lonelyNodes.get(0).getBounds().y;
        int xDiff = 0;
        int yDiff = 0;
        int spacer = 10;
        int count = 0;
        for (Vertex node : lonelyNodes) {
        	xIndex += xDiff;
        	yIndex += yDiff;
        	node.setBounds(new Rectangle(xIndex, yIndex, node.getBounds().width, node.getBounds().height));
        	xDiff = node.getBounds().width + spacer;
        	count++;
        	if (count > Math.sqrt(lonelyNodes.size())) {
        		count = 0;
        		xDiff = 0;
        		xIndex = lonelyNodes.get(0).getBounds().x;
        		yIndex += node.getBounds().height + spacer;
        	}
        }
	}

	private void enforceNewEdges(Set<AbstractGraphicalEditPart> newEdgesAsParts, Set<AbstractGraphicalEditPart> newParts) {
        Set<Edge> newEdges = new HashSet<Edge>();
		Set<Vertex> edgeVtxSet = new HashSet<Vertex> ();
		for (AbstractGraphicalEditPart edgePart : newEdgesAsParts) {
        	Edge edge = (Edge) partsToCellMap.get(edgePart); 
    		if (edge.getFrom().data == null || edge.getTo().data == null) continue;
			edgeVtxSet.add(edge.getFrom());
			edgeVtxSet.add(edge.getTo());
    		newEdges.add(edge);
    	}
		
		// sort edgeVertices per ageMgr (oldest is dealt first first)
		Vertex[] edgeVertices = edgeVtxSet.toArray(new Vertex[] {});
		Arrays.sort(edgeVertices, new Comparator<Vertex>() {
			public int compare(Vertex o1, Vertex o2) {
				if (!(o1.data instanceof AbstractGraphicalEditPart) || !(o2.data instanceof AbstractGraphicalEditPart))
					return 0;
				AbstractGraphicalEditPart part1 = (AbstractGraphicalEditPart) o1.data;
				AbstractGraphicalEditPart part2 = (AbstractGraphicalEditPart) o2.data;
				int p1Age = ageMgr.getAge(part1); 
				int p2Age = ageMgr.getAge(part2); 
				if (p1Age==-1 && p2Age==-1)
					return 0;
				return -(p1Age-p2Age);
			}});
		
		// deal with multiple edges
		for (Vertex vtx : edgeVertices) {
			Set<Edge> vtxFromVertEdges = new HashSet<Edge> ();
			Set<Edge> vtxFromHorzEdges = new HashSet<Edge> ();
			Set<Edge> vtxFromUndrEdges = new HashSet<Edge> ();
			Set<Edge> vtxToVertEdges = new HashSet<Edge> ();
			Set<Edge> vtxToHorzEdges = new HashSet<Edge> ();
			Set<Edge> vtxToUndrEdges = new HashSet<Edge> ();
			for (Edge edge : newEdges) {
				if (edge.getFrom() == vtx) 
					addTypedEdge(edge, vtxFromVertEdges, vtxFromHorzEdges, vtxFromUndrEdges);
				if (edge.getTo() == vtx) 
					addTypedEdge(edge, vtxToVertEdges, vtxToHorzEdges, vtxToUndrEdges);
			}

			enforceTypedCommonNewEdges(true, vtx, vtxFromVertEdges, vtxFromHorzEdges, vtxFromUndrEdges, newEdges, newParts);
			enforceTypedCommonNewEdges(false, vtx, vtxToVertEdges, vtxToHorzEdges, vtxToUndrEdges, newEdges, newParts);
 		}
		
		// repopulate new edges with ONLY the edges connecting to NEW nodes
		// we do not want to move nodes already in the diagram
		newEdges.clear();
		for (AbstractGraphicalEditPart edgePart : newEdgesAsParts) {
        	Edge edge = (Edge) partsToCellMap.get(edgePart); 
    		if (edge.getFrom().data == null || edge.getTo().data == null) continue;
    		if (newParts.contains(((AbstractGraphicalEditPart) edge.getFrom().data)))
    			edgeVtxSet.add(edge.getFrom());
    		if (newParts.contains(((AbstractGraphicalEditPart) edge.getTo().data))) 
    			edgeVtxSet.add(edge.getTo());

    		// only add edges for each part once. (Adding multiple methods shouldnt cause mult moves)
    		if (newParts.contains(((AbstractGraphicalEditPart) edge.getFrom().data))) {
    			newEdges.add(edge);
    			newParts.remove(((AbstractGraphicalEditPart) edge.getFrom().data));
    		} 
    		if (newParts.contains(((AbstractGraphicalEditPart) edge.getTo().data))){
    			newEdges.add(edge);
    			newParts.remove(((AbstractGraphicalEditPart) edge.getTo().data));
    		}
        		
    	}
		
		// deal with single edges while keeping track of which have already been moved
		Set<Vertex> movedSingleVtxs = new HashSet<Vertex>();
		for (Edge edge : newEdges) {
			if (edge instanceof PointedEdge)
				enforceNewEdge((PointedEdge)edge, movedSingleVtxs);
		}
	}



	private void addTypedEdge(Edge edge, Set<Edge> vtxVertEdges, Set<Edge> vtxHorzEdges, Set<Edge> vtxUndrEdges) {
		if (edge instanceof PointedEdge) {
			if (((PointedEdge)edge).isVertical())
				vtxVertEdges.add(edge);
			else
				vtxHorzEdges.add(edge);
		} else {
			vtxUndrEdges.add(edge);
		}
	}

	private void enforceTypedCommonNewEdges(
			boolean isCommonVtxFrom, Vertex commonVtx, 
			Set<Edge> vtxVertEdges, Set<Edge> vtxHorzEdges, Set<Edge> vtxUndrEdges,
			Set<Edge> newEdges, 
			Set<AbstractGraphicalEditPart> newParts) {
		if (vtxVertEdges.size() > 1) {
			newEdges.removeAll(vtxVertEdges);
			enforceCommonNewVertEdges(isCommonVtxFrom, commonVtx, vtxVertEdges, newParts);
		}
		if (vtxHorzEdges.size() > 1) {
			newEdges.removeAll(vtxHorzEdges);
			enforceCommonNewHorzEdges(isCommonVtxFrom, commonVtx, vtxHorzEdges, newParts);
		}
		if (vtxUndrEdges.size() > 1) {
			newEdges.removeAll(vtxUndrEdges);
			enforceCommonNewUndrEdges(isCommonVtxFrom, commonVtx, vtxUndrEdges, newParts);
		}
	}
	
	private class PositioningBox {
		// these are 0 based, so top is usually negative
		int top = 0;
		int bot = 0;
		int maxHeight = Integer.MAX_VALUE;
		boolean vertOrientation = false;
		Dimension baseTranslation = null;
		public PositioningBox(boolean _vertOrientation, Dimension _baseTranslation) {
			vertOrientation = _vertOrientation;
			baseTranslation = _baseTranslation;
		}
		public PositioningBox(boolean _vertOrientation, Dimension _baseTranslation, int _maxHeight) {
			vertOrientation = _vertOrientation;
			baseTranslation = _baseTranslation;
			maxHeight = _maxHeight;
		}
		public boolean addToBot() {
			return -top > bot;
		}
		public boolean addToTop() {
			return -top < bot;
		}
		public void addVtxToBot(int vtxSize) {
			bot += GraphLayoutManager.prefEdgeLength + vtxSize;
		}
		public void addVtxToTop(int vtxSize) {
			top -= GraphLayoutManager.prefEdgeLength + vtxSize;
		}
		public void addVtxInMid(int vtxSize) {
			top -= vtxSize / 2;
			bot += vtxSize / 2 + 1;
			// we add 1 so that the next time items will be added to top
		}
		public int getTranslationSizeForBot() {
			return bot + GraphLayoutManager.prefEdgeLength;
		}
		public int getTranslationSizeForTop() {
			return top - GraphLayoutManager.prefEdgeLength;
		}
		private void addAndMoveVtxs(boolean isCommonVtxFrom, Set<Edge> vtxEdges, Vertex commonVtx, Set<AbstractGraphicalEditPart> newParts) {
			for (Edge currEdge : new HashSet<Edge>(vtxEdges)) {
//				Vertex movingVtx = isCommonVtxFrom ? currEdge.getTo() : currEdge.getFrom();
				Vertex movingVtx = isCommonVtxFrom ? currEdge.getTo() : currEdge.getFrom();
				if (newParts.contains((AbstractGraphicalEditPart) movingVtx.data))
					{}
				else if (newParts.contains(((AbstractGraphicalEditPart) currEdge.getTo().data)))
					movingVtx = currEdge.getTo();
				else if (newParts.contains(((AbstractGraphicalEditPart) currEdge.getFrom().data)))
					movingVtx = currEdge.getFrom();
				else {
					vtxEdges.remove(currEdge);
					continue;
				}
					
					
				AbstractGraphicalEditPart part1 = (AbstractGraphicalEditPart) movingVtx.data;
				AbstractGraphicalEditPart part2 = (AbstractGraphicalEditPart) commonVtx.data;
				if (part1 == part2) return;
				int p1Age = ageMgr.getAge(part1); 
				int p2Age = ageMgr.getAge(part2); 
				
				if (!canAddVtx(movingVtx)) return;
				// only move younger vertex
				if (p1Age <= p2Age ) 
					addAndMoveVtx(movingVtx, commonVtx);
				vtxEdges.remove(currEdge);
			}
		}
		private boolean canAddVtx(Vertex movingVtx) {
			int heightWithVtx = -top + bot + GraphLayoutManager.prefEdgeLength + getVtxSize(movingVtx, vertOrientation);
			if (maxHeight <= heightWithVtx)
				return false;
			else
				return true;
		}
		private void addAndMoveVtx(Vertex movingVtx, Vertex commonVtx) {
			Dimension edgeTranslation = baseTranslation.getCopy();
			
			int movingVtxSize = getVtxSize(movingVtx, vertOrientation);
			int translationSize = -1;
			
			if (addToBot()) {
				translationSize = this.getTranslationSizeForBot();
				this.addVtxToBot(movingVtxSize);
			} else if (addToTop()) {
				translationSize = this.getTranslationSizeForTop();
				addVtxToTop(movingVtxSize);
			} else {
				translationSize = 0;
				addVtxInMid(movingVtxSize);
			}
			
			if (vertOrientation)
				edgeTranslation.height = translationSize;
			else
				edgeTranslation.width = translationSize;
			ageMgr.setLayedOut((AbstractGraphicalEditPart) movingVtx.data);
			smartMove(movingVtx, commonVtx, edgeTranslation);
		}
	};
	private int getVtxSize(Vertex vtx, boolean vertOrientation) {
		if (vertOrientation)
			return vtx.getBounds().height;
		else
			return vtx.getBounds().width;
	}
	
	private Dimension getTranslation(boolean vert, boolean isAscending, int gap) {
		if (vert) {
			if (isAscending)
				return new Dimension(0, +gap);
			else
				return new Dimension(0, -gap);	// upwards
		} else {
			if (isAscending)
				return new Dimension(+gap, 0);
			else
				return new Dimension(-gap, 0);
		}
	}
	private Dimension getTranslation(boolean vert, boolean isAscending) {
		return getTranslation(vert, isAscending, GraphLayoutManager.prefEdgeLength);
	}
	private Dimension getMultiBoxedTranslation(boolean vert, boolean isAscending, int numBoxes, int boxSize) {
		int gap = GraphLayoutManager.prefEdgeLength * (numBoxes + 1);
		gap += boxSize * numBoxes;
		return getTranslation(vert, isAscending, gap);
	}

	private void enforceCommonNewVertEdges(boolean isCommonVtxFrom, Vertex commonVtx, Set<Edge> vtxEdges, Set<AbstractGraphicalEditPart> newParts) {
		int loops = 0;
		int p = GraphLayoutManager.prefEdgeLength;
		int w = getVtxSize(commonVtx, false);
		// incr p to fit atleast 3 items
		int minFit = Math.min(3, vtxEdges.size());
		int minP = (w+p)*minFit;
		w = Math.max(w, minP);
		//if (vtxEdges > 3)
		//logger.info(".");
		Edge tmpEdge = null;
		
		Set<Vertex> separateVtxsTo = new HashSet<Vertex>();
		Set<Vertex> separateVtxsFrom = new HashSet<Vertex>();
		for (Edge vtxEdge : vtxEdges) {
				separateVtxsFrom.add(vtxEdge.getFrom());
				separateVtxsTo.add(vtxEdge.getTo());
		}
		
		while (!vtxEdges.isEmpty()) {
			// assume common vertex is the source of the edge (from)
			Edge vtxEdge = vtxEdges.iterator().next();
			
			
			if (isCommonVtxFrom) {
				if (separateVtxsTo.contains(vtxEdge.getTo()))
					separateVtxsTo.remove(vtxEdge.getTo());
				else {
					vtxEdges.remove(vtxEdge);
					continue;
				}
			} 
			if (separateVtxsFrom.contains(vtxEdge.getFrom()))
				separateVtxsFrom.remove(vtxEdge.getFrom());
			else {
				vtxEdges.remove(vtxEdge);
				continue;
			}
			
			
			boolean ascending = ((PointedEdge)vtxEdge).isAscending();
			Dimension baseTranslation = getMultiBoxedTranslation(true, ascending, loops, getVtxSize(commonVtx, false));
			if (!isCommonVtxFrom) baseTranslation.negate();
			
			int boxSize = w + 2*p + (2*w+2*p)*loops;
			PositioningBox positioningBox = new PositioningBox(false, baseTranslation, boxSize);
			//logger.info("--> " + baseTranslation);
			//logger.info("--> " + boxSize);
			//logger.info("--> " + vtxEdges.size());
			//logger.info("", new Exception());

			positioningBox.addAndMoveVtxs(isCommonVtxFrom, vtxEdges, commonVtx, newParts);

			//logger.info("--> " + vtxEdges.size());
			loops++;
		}
	}
	

	private void enforceCommonNewHorzEdges(boolean isCommonVtxFrom, Vertex commonVtx, Set<Edge> vtxEdges, Set<AbstractGraphicalEditPart> newParts) {
		int loops = 0;
		
		// create set of individual Vertexs so that each is only added once
		Set<Vertex> separateVtxsTo = new HashSet<Vertex>();
		Set<Vertex> separateVtxsFrom = new HashSet<Vertex>();
		for (Edge vtxEdge : vtxEdges) {
				separateVtxsFrom.add(vtxEdge.getFrom());
				separateVtxsTo.add(vtxEdge.getTo());
		}
			
		while (!vtxEdges.isEmpty()) {
			// assume common vertex is the source of the edge (from)
			Edge vtxEdge = vtxEdges.iterator().next();
			
			// do not do moves/increment loop if this Vertex has already been added
			if (isCommonVtxFrom) {
				if (separateVtxsTo.contains(vtxEdge.getTo()))
					separateVtxsTo.remove(vtxEdge.getTo());
				else {
					vtxEdges.remove(vtxEdge);
					continue;
				}
			} 
			if (separateVtxsFrom.contains(vtxEdge.getFrom()))
				separateVtxsFrom.remove(vtxEdge.getFrom());
			else {
				vtxEdges.remove(vtxEdge);
				continue;
			}
			
			boolean ascending = ((PointedEdge)vtxEdge).isAscending();
			Dimension baseTranslation = getMultiBoxedTranslation(false, ascending, loops, getVtxSize(commonVtx, false));
			
			if (!isCommonVtxFrom) 
				baseTranslation.negate();
			
			int boxSize = getVtxSize(commonVtx, true) + GraphLayoutManager.prefEdgeLength*2*(1 + loops);
			PositioningBox positioningBox = new PositioningBox(true, baseTranslation, boxSize);

			positioningBox.addAndMoveVtxs(isCommonVtxFrom, vtxEdges, commonVtx, newParts);
			loops++;
		}
	}


	private void enforceCommonNewUndrEdges(boolean isCommonVtxFrom, Vertex commonVtx, Set<Edge> vtxEdges, Set<AbstractGraphicalEditPart> newParts) {
		int currSide = 0;
		int nextSide = 0;
		int loops = 0;
		while (!vtxEdges.isEmpty()) {
			boolean boxExpandVert = false;
			boolean boxPosAsc = false;
			switch (currSide) {
			case 0:
				boxExpandVert = true;
				boxPosAsc = false;
				nextSide = 1;
				break;
			case 1:
				boxExpandVert = false;
				boxPosAsc = true;
				nextSide = 2;
				break;
			case 2:
				boxExpandVert = true;
				boxPosAsc = true;
				nextSide = 3;
				break;
			case 3:
				boxExpandVert = false;
				boxPosAsc = false;
				nextSide = 0;
				loops ++;
				break;
			}
			
			Dimension baseTranslation = getTranslation(!boxExpandVert, boxPosAsc);
			baseTranslation.scale(1+loops);
			
			int boxSize = getVtxSize(commonVtx, boxExpandVert) + GraphLayoutManager.prefEdgeLength*2*(1 + loops);
			PositioningBox positioningBox = new PositioningBox(boxExpandVert, baseTranslation, boxSize);

			positioningBox.addAndMoveVtxs(isCommonVtxFrom, vtxEdges, commonVtx, newParts);

			currSide = nextSide;
		}
	}

	private static String commonPfx = ReloRdfRepository.atxaRdfNamespace + "jdt-wkspc#";
    private static String getDbgStr(Vertex v) {
    	if (v.data == null) return v.toString();
    	String uri = v.data.toString();
    	if (uri.startsWith(commonPfx)) return uri.substring(commonPfx.length());
    	return uri;
    }
    
	private void enforceNonOverlapping(
			AbstractGraphicalEditPart part1,
			AbstractGraphicalEditPart part2,
			IGraphLayoutAgeMgr ageMgr, 
			Map<AbstractGraphicalEditPart, Object> partsToCellMap) {
		int p1Age = ageMgr.getAge(part1); 
		int p2Age = ageMgr.getAge(part2); 
		if (p1Age>=2 && p2Age>=2) return;
		
		Vertex v1 = (Vertex) partsToCellMap.get(part1);
		Vertex v2 = (Vertex) partsToCellMap.get(part2);
		
		if (p1Age < p2Age) {
			logger.info("moving: " + getDbgStr(v1) + " / " + v1.getBounds() + 
					" bec of overlap with: " + getDbgStr(v2) + " / " + v2.getBounds());
			smartMove(v1, v2, getMinTranslation(v1, v2));	// move p1
			logger.info("after: moved: " + v1.getBounds() + " unmoved: " + v2.getBounds());
		} else {
			logger.info("moving: " + getDbgStr(v2) + " / " + v2.getBounds() + 
					" bec of overlap with: " + getDbgStr(v1) + " / " + v1.getBounds());
			smartMove(v2, v1, getMinTranslation(v2, v1));	// move v2
			logger.info("after: moved: " + v2.getBounds() + " unmoved: " + v1.getBounds());
		}
	}

	// TODO: Review - why not used
	@SuppressWarnings("unused")
	private void enforceEdgeForNewParts(Set<AbstractGraphicalEditPart> newParts, PointedEdge edge) {
		Vertex fr = edge.getFrom();
		Vertex to = edge.getTo();
		
		if (newParts.contains(fr.data)) {
    		smartMove(fr, to, getEdgeTranslation(edge).negate());
		}

		if (newParts.contains(to.data)) {
    		smartMove(to, fr, getEdgeTranslation(edge));
		}
	}
	
	private static class AgedVertex {
		Vertex youngVtx;
		Vertex oldVtx;
		public AgedVertex(IGraphLayoutAgeMgr ageMgr, Vertex v1, Vertex v2) {
			AbstractGraphicalEditPart part1 = (AbstractGraphicalEditPart)v1.data; 
			AbstractGraphicalEditPart part2 = (AbstractGraphicalEditPart)v2.data; 
			if (ageMgr.getAge(part1) < ageMgr.getAge(part2)){
				youngVtx = v1;
				oldVtx = v2;
			} else {
				youngVtx = v2;
				oldVtx = v1;
			}
		}
	}

	private void enforceNewEdge(PointedEdge edge, Set<Vertex> movedSingleVtxs) {
		AgedVertex agedParts = new AgedVertex(ageMgr, edge.getFrom(), edge.getTo());
		
		// do not enforce an edge on the same Vertex more than once
		if (movedSingleVtxs.contains(agedParts.youngVtx)) return;
		
		Dimension edgeTranslation = getEdgeTranslation(edge);
		if (agedParts.youngVtx == edge.getFrom()) edgeTranslation.negate();
		
		smartMove(agedParts.youngVtx, agedParts.oldVtx, edgeTranslation);
		movedSingleVtxs.add(agedParts.youngVtx);
	}

	private static Dimension getEdgeTranslation(PointedEdge edge) {
		if (edge.isVertical()) {
    		//logger.error("EdgeTranlation: Vert");
			if (edge.isAscending())
				return new Dimension(0, +GraphLayoutManager.prefEdgeLength);
			else
				return new Dimension(0, -GraphLayoutManager.prefEdgeLength);	// upwards
		} else {
    		//logger.error("EdgeTranlation: Horiz");
			if (edge.isAscending())
				return new Dimension(+GraphLayoutManager.prefEdgeLength, 0);
			else
				return new Dimension(-GraphLayoutManager.prefEdgeLength, 0);
		}
	}
	/**
	 * Try to eliminate overlaps. We use minVertexEdgeDist since we want to
	 * minimize change.
	 * 
	 * Works by creating the translation that would be returned as if there was
	 * an edge in between the two vertices, with distance minVertexEdgeDist
	 */
	private static Dimension getMinTranslation(Vertex v1, Vertex v2) {
		Rectangle p1Bounds = v1.getBounds();
		Point p1Center = p1Bounds.getCenter();
		Rectangle p2Bounds = v2.getBounds();

		// getting the slope right etc can be hard, so we will instead just do
		// the move on one axis (horiz or vert) which ever needs to be smaller
		Rectangle areaToClear = p1Bounds.getCopy().intersect(p2Bounds);
		Point areaToClearCenter = areaToClear.getCenter();
		if (areaToClear.getSize().getArea() == 0) return new Dimension(0,0);

		// we are moving v1
		if (areaToClear.height < areaToClear.width) {
    		//logger.error("MinTranslation: Vert");
//			if (p1Center.x > areaToClearCenter.x)  // move in closest direction
			if (v1.getInEdges().size() < v1.getOutEdges().size()) // move in direction of edge
				return new Dimension(0, +GraphLayoutManager.minVertexEdgeDist);
			else
				return new Dimension(0, -GraphLayoutManager.minVertexEdgeDist);	// upwards
		} else {
    		//logger.error("MinTranslation: Horiz");
//			if (p1Center.y > areaToClearCenter.y) // move in closest direction
			if (v1.getInEdges().size() >= v1.getOutEdges().size()) // move in direction of edge 
				return new Dimension(+GraphLayoutManager.minVertexEdgeDist, 0);
			else
				return new Dimension(-GraphLayoutManager.minVertexEdgeDist, 0);
		}
	}


	/**
	 * Smart Move notices the direction of translation and makes the given translation
	 * be a center to center translation
	 * @param getAvg 
	 * @param positionedParts 
	 */
	private void smartMove(Vertex tgtVertex, Vertex stationaryVertex, Dimension edgeTranslation) {
		smartMove(tgtVertex, stationaryVertex, edgeTranslation, true);
	}
	private void smartMove(Vertex tgtVertex, Vertex stationaryVertex, Dimension edgeTranslation, boolean getAvg) {
		Rectangle stationaryBounds = stationaryVertex.getBounds();
		Rectangle tgtBounds = tgtVertex.getBounds().getCopy();
		//logger.info(edgeTranslation, new Exception());

		// do the actual smartMoving
		if(getAvg) {
			if (edgeTranslation.height > 0) {
				edgeTranslation.height += +(stationaryBounds.height + tgtBounds.height) / 2;
			} else if (edgeTranslation.height < 0) {
				edgeTranslation.height += -(stationaryBounds.height + tgtBounds.height) / 2;
			}
			if (edgeTranslation.width > 0) {
				edgeTranslation.width += +(stationaryBounds.width + tgtBounds.width) / 2;
			} else if (edgeTranslation.width < 0) {
				edgeTranslation.width += -(stationaryBounds.width + tgtBounds.width) / 2;
			}
			
			// so now stationaryTranslation is a center to center translation

		}
			
		Point tgtCenter = new Point(tgtBounds.getCenter());
		tgtCenter.translate(edgeTranslation.width, edgeTranslation.height);
		Dimension tgtCenterDist = tgtBounds.getCenter().getDifference(tgtBounds.getTopLeft());
		Point tgtTopLeft = tgtCenter.translate(-tgtCenterDist.width, -tgtCenterDist.height);

		// now, ignore the axis which had no translation
		if (edgeTranslation.height == 0) tgtTopLeft.y = tgtBounds.y;
		if (edgeTranslation.width == 0) tgtTopLeft.x = tgtBounds.x;		

		tgtBounds.setLocation(tgtTopLeft);
		
		//logger.error("Moved: " + tgtVertex.getBounds() 
		//		+ " to: " + tgtBounds 
		//		+ " edgeTranslation: " + edgeTranslation 
		//		+ " tgtVertex: " + tgtVertex.data);
        tgtVertex.setBounds(tgtBounds);
		positionedParts.add((AbstractGraphicalEditPart) tgtVertex.data);
	}

	
}
