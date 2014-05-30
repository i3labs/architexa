package jiggle;

import java.util.ArrayList;


// Class for graphs. */


public class Graph extends Cell {
	
	// for debugging purposes
	public static int cnt = 0;
	public int id = cnt++;
  
	public ArrayList<Cell> vertices = new ArrayList<Cell> (1);
	public ArrayList<Edge> edges = new ArrayList<Edge>();
	public int getNumberOfVertices() {
		return vertices.size();
	}
	

	/* NOTE: the above are made publicly accessible for reasons of
	efficiency.  They should NOT, however, be modified except by
	insertVertex, deleteVertex, insertEdge, and deleteEdge methods
	below. */
    
	/*
	 * sets up a 2-dimensional graph
	 */
	public Graph () {}

	public Vertex insertVertex() {
        Vertex v = new Vertex(this);
        insertVertex(v);
        return v;
    }

	public Cell insertVertex(Cell v) {
        vertices.add(v);
        v.setGraph(this);
        return v;
    }

	public Edge insertEdge (Vertex from, Vertex to) {
		return insertEdge (from, to, false);
	}

	/*
    private static int cnt = 0;
    private int id = cnt++;
    private String getId() {
        return "/" + id + "/";
    }
    */
	
    public static String getEdgeAtStr(Edge e) {
        String retVal = "";
        retVal += e.getFrom();
        retVal += " --> ";
        retVal += e.getTo();
        return retVal;
    }

	public Edge insertEdge (Vertex from, Vertex to, boolean dir) {
		return insertEdge(new Edge (this, from, to, dir));
	}

	public Edge insertEdge (Edge e) {
        //System.err.println("Inserting EDGE: " + getId() + " " + getEdgeAtStr(e));
		e.getFrom().insertNeighbor (e); 
		e.getTo().insertNeighbor (e);
        edges.add(e);
        //System.err.println("EDGE Cnt: " + edges.size());
        //for (Edge edge : edges) {
		//	System.err.println("EDGE Cnt - Edge:" + getEdgeAtStr(edge));
		//} 
        //System.err.println("EDGE Cnt: " + edges.size());
		return e;
	}
	
	public void deleteVertex (Vertex v) {
		for (int i = 0; i < v.undirectedEdges.size(); i++) {
			Edge e = v.undirectedEdges.get(i);
			(v.undirectedNeighbors.get(i)).deleteNeighbor (e);
            edges.remove(e);
		}
		for (int i = 0; i < v.inEdges.size(); i++) {
			Edge e = v.inEdges.get(i);
			(v.inNeighbors.get(i)).deleteNeighbor (e);
            edges.remove(e);
		}
		for (int i = 0; i < v.outEdges.size(); i++) {
			Edge e = v.outEdges.get(i);
			(v.outNeighbors.get(i)).deleteNeighbor (e);
            edges.remove(e);
		}
		if (!vertices.remove(v))
			throw new Error (v + " not found");
	}

	public void deleteEdge (Edge e) {
		e.getFrom ().deleteNeighbor (e); e.getTo ().deleteNeighbor (e);
	}
	
	@Override
    public void recomputeBoundaries (int[] center, int[] size) {
		int lo [] = getMin ();
		int hi [] = getMax ();

		lo [0] = Integer.MAX_VALUE; 
		lo [1] = Integer.MAX_VALUE; 

		hi [0] = -Integer.MAX_VALUE;
		hi [1] = -Integer.MAX_VALUE;

		for (int i = 0; i < vertices.size(); i++) {
			Cell v = vertices.get(i); 
			int c [] = v.getCenter ();
			lo [0] = Math.min (lo [0], c [0]);
			lo [1] = Math.min (lo [1], c [1]);

			hi [0] = Math.max (hi [0], c [0]);
			hi [1] = Math.max (hi [1], c [1]);
		}
		recomputeSize (lo, hi);
	}

}
