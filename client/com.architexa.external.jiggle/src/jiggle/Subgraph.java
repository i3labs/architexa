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
 * Created on Sep 6, 2004
 *
 */
package jiggle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import com.architexa.org.eclipse.draw2d.geometry.Insets;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author vineet
 *  
 */
public class Subgraph extends Graph {

    public Insets subgraphBorder = new Insets(10);

    /**
     * @param g
     */
    public Subgraph(Graph g) {
        setGraph(g);
    }

    private void getMinMaxCoords(int[] minCoord, int[] maxCoord) {
        int sgnv = getNumberOfVertices();
        
        if (sgnv == 0) {
            minCoord[0] = 0;
            minCoord[1] = 0;

            maxCoord[0] = 0;
            maxCoord[1] = 0;
            
            return;
        }
        
        int[] vertexCoords = getBorderCoords(vertices.get(0));
        int[] vertexSize = getBorderSize(vertices.get(0));
        
        minCoord[0] = vertexCoords[0] - (vertexSize[0] / 2);
        minCoord[1] = vertexCoords[1] - (vertexSize[1] / 2);

        maxCoord[0] = vertexCoords[0] + (vertexSize[0] / 2);
        maxCoord[1] = vertexCoords[1] + (vertexSize[1] / 2);

        for (int sv = 0; sv < sgnv; sv++) {
            vertexCoords = getBorderCoords(vertices.get(sv));
            vertexSize = getBorderSize(vertices.get(sv));

            minCoord[0] = Math.min(minCoord[0], vertexCoords[0] - (vertexSize[0] / 2));
            minCoord[1] = Math.min(minCoord[1], vertexCoords[1] - (vertexSize[1] / 2));

            maxCoord[0] = Math.max(maxCoord[0], vertexCoords[0] + (vertexSize[0] / 2));
            maxCoord[1] = Math.max(maxCoord[1], vertexCoords[1] + (vertexSize[1] / 2));
        }
    }
    
    public int[] getBorderSize() {
        int minCoord[] = new int[2];
        int maxCoord[] = new int[2];

        getMinMaxCoords(minCoord, maxCoord);

        int retSize[] = new int[2];
        retSize[0] = subgraphBorder.getWidth() + maxCoord[0] - minCoord[0];
        retSize[1] = subgraphBorder.getHeight() + maxCoord[1] - minCoord[1];
        return retSize;
    }

    public void dmpInfo() {
        int minCoord[] = new int[2];
        int maxCoord[] = new int[2];

        getMinMaxCoords(minCoord, maxCoord);

        int retSize[] = new int[2];
        retSize[0] = maxCoord[0] - minCoord[0];
        retSize[1] = maxCoord[1] - minCoord[1];
        
        System.err.println("Dumping...");
        System.err.println("Min: " + minCoord[0] + "," + minCoord[1]);
        System.err.println("Max: " + maxCoord[0] + "," + maxCoord[1]);
        System.err.println("Sz : " + retSize[0] + "," + retSize[1]);
		//if (Double.isNaN(minCoord[0])) {
		//    System.err.println("V0C: " + vertices.get(0).getCoords()[0] + "," + vertices.get(0).getCoords()[1]);
		//    System.err.println("V0S: " + vertices.get(0).getSize()[0] + "," + vertices.get(0).getSize()[1]);
		//} else {
		//}
    }

    @SuppressWarnings("unused")
	private void dmp(int[] d) {
        for (int i = 0; i < d.length; i++) {
            System.err.print(d[i] + " ");
        }
        System.err.println();
    }

    public int[] getBorderCoords() {
        int minCoord[] = new int[2];
        int maxCoord[] = new int[2];

        getMinMaxCoords(minCoord, maxCoord);

        int retCoords[] = new int[2];
        retCoords[0] = (maxCoord[0] + minCoord[0])/2;
        retCoords[1] = (maxCoord[1] + minCoord[1])/2;
        return retCoords;
    }
    
    // border family reaches in and gets the real size/coords
    private int[] getBorderCoords(Cell v) {
        if (v instanceof Subgraph) {
            return ((Subgraph)v).getBorderCoords();
        } else {
            return v.getCenter();
        }
    }
    private int[] getBorderSize(Cell v) {
        if (v instanceof Subgraph) {
            return ((Subgraph)v).getBorderSize();
        } else {
            return v.getSize();
        }
    }

    @Override
    public Rectangle getBounds() {
        int minCoord[] = new int[2];
        int maxCoord[] = new int[2];

        getMinMaxCoords(minCoord, maxCoord);
        
        Rectangle containedRect = new Rectangle((int) minCoord[0], (int) minCoord[1],
                (int) (maxCoord[0] - minCoord[0]), 
                (int) (maxCoord[1] - minCoord[1]));
        return containedRect.expand(subgraphBorder);
    }

	@Override
    public void recomputeBoundaries (int[] center, int[] size) {
	    //(new RuntimeException("tst")).printStackTrace();
        int[] min = getMin();
        int[] max = getMax();
        // access private members
        // basically Cell.recomputeBoundaries
        min [0] = center [0] - size [0] / 2;
		max [0] = center [0] + size [0] / 2;

		min [1] = center [1] - size [1] / 2;
		max [1] = center [1] + size [1] / 2;
	}
	
	public void getChildrenRecursively(Set<Cell> s) {
        for (int i = 0; i < vertices.size(); i++) {
            Cell v = vertices.get(i);
            if (v instanceof Subgraph)
                ((Subgraph)v).getChildrenRecursively(s);
            else
                s.add(v);
        }
	    
	}
	
	public Map<Vertex,Set<Vertex>> mapCellsToCC = null;
	public Set<Vertex>[] arrCC = null;
	public int largestCC = -1;
	
	public void initConnectedMap() {
		mapCellsToCC = new HashMap<Vertex,Set<Vertex>>();

		//Set children = new HashSet(Arrays.asList(vertices));
        for (int i = 0; i < vertices.size(); i++) {
            Cell c = vertices.get(i);
            if (!(c instanceof Vertex)) continue;
            
            // already has a component attached to it
            if (mapCellsToCC.containsKey(c)) continue;
            
            Vertex v = (Vertex) c;

            Set<Vertex> connectedComponent = new HashSet<Vertex>();
            connectedComponent.add(v);
            mapCellsToCC.put(v, connectedComponent);
            
            Iterator<Vertex> neighbors = v.getAllNeighbors().iterator();
            while (neighbors.hasNext()) {
                Vertex neighbor = neighbors.next();
                if (mapCellsToCC.containsKey(neighbor)) {
                    connectedComponent = mergeComponents( mapCellsToCC.get(neighbor), connectedComponent);
                }
            }
            
        }
        
        Set<Set<Vertex>> setCC = new HashSet<Set<Vertex>> (mapCellsToCC.values());
        arrCC = setCC.toArray(new Set[] {});
        
        largestCC = 0;
        for (int i=1; i<arrCC.length; i++) {
            if (arrCC[largestCC].size() < arrCC[i].size()) {
                largestCC = i;
            }
        }
	}

    private Set<Vertex> mergeComponents(Set<Vertex> connectedComponent1, Set<Vertex> connectedComponent2) {
        // do connectedComponent2 => connectedComponent1
        
        // remap
        Iterator<Vertex> connectedComponent2It = connectedComponent2.iterator();
        while (connectedComponent2It.hasNext()) {
            Vertex cc2Vertex = connectedComponent2It.next();
            mapCellsToCC.put(cc2Vertex, connectedComponent1);
        }
        
        // add
        connectedComponent1.addAll(connectedComponent2);
        return connectedComponent1;
    }

}