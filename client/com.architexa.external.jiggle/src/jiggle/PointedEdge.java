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


/**
 * This class represents a specific kind of directed edge in a graph, these 
 * edges try to point in a particular direction. 
 * 
 * @author vineet
 *
 */
public class PointedEdge extends Edge {
	private boolean isVert;
	private boolean isAsc;

    /**
     * For top-to-bottom use dim=0,ascPoint=true
     * For left-to-right use dim=1,ascPoint=true
     */
    public PointedEdge(Graph g, Vertex fr, Vertex to, int dim, boolean ascPoint) {
        super(g, fr, to, true);
        this.isAsc = true;
    }

    /**
     * For setting the common left-to-right or top-to-bottom. Same as asc=true
     */
    public PointedEdge(Graph g, Vertex fr, Vertex to, boolean vert) {
        super(g, fr, to, true);
        this.isVert = vert;
        this.isAsc = true;
    }

    public PointedEdge(Graph g, Vertex fr, Vertex to, boolean vert, boolean asc) {
        super(g, fr, to, true);
        this.isVert = vert;
        this.isAsc = asc;
    }

    public boolean isVertical() {
    	return this.isVert;
    }
    
    public boolean isAscending() {
    	return this.isAsc;
    }
    
    public int getDim() {
    	return (this.isVert ? 1 : 0);
    }

    public int getIncrFactor() {
    	return (this.isAsc ? 1 : -1);
    }

}
