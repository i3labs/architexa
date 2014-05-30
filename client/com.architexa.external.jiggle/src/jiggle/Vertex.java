package jiggle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* Class for vertices of a graph. */

public class Vertex extends Cell {

	// TODO: Remove from the below that we are not using
	ArrayList<Edge> undirectedEdges = new ArrayList<Edge>();
	ArrayList<Edge>         inEdges = new ArrayList<Edge>();
	ArrayList<Edge>        outEdges = new ArrayList<Edge>();
	ArrayList<Vertex> undirectedNeighbors = new ArrayList<Vertex>();
	ArrayList<Vertex>         inNeighbors = new ArrayList<Vertex>();
	ArrayList<Vertex>        outNeighbors = new ArrayList<Vertex>();

	/* NOTE: the above are made package-accessible for reasons of
	efficiency.  They should NOT, however, be modified except by
	insertNeighbor and deleteNeighbor methods below. */


	public Vertex(Graph g) {
        super();
        setGraph(g);
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public Vertex clone() {
		// really needed only for bounds, so we do shallow copying of everything else
		Vertex retVal = new Vertex(this.getGraph());
		retVal.undirectedEdges  = (ArrayList<Edge>) this.undirectedEdges.clone();
		retVal.inEdges = (ArrayList<Edge>) this.inEdges.clone();
		retVal.outEdges = (ArrayList<Edge>) this.outEdges.clone();
		retVal.undirectedNeighbors = (ArrayList<Vertex>) this.undirectedNeighbors.clone();
		retVal.inNeighbors = (ArrayList<Vertex>) this.inNeighbors.clone();
		retVal.outNeighbors = (ArrayList<Vertex>) this.outNeighbors.clone();
		retVal.setBounds(this.getBounds());
		retVal.data = this.data;
		return retVal;
		
	}

	void insertNeighbor (Edge e) {
		Vertex from = e.getFrom (), to = e.getTo ();
		Vertex v = null;
		if (this == from) v = to; else if (this == to) v = from;
		else throw new Error (e + " not incident to " + this);
		if (! e.getDirected ()) {
			undirectedEdges.add(e);
            undirectedNeighbors.add(v);
		}
		else if (this == from) {
			outEdges.add(e);
            outNeighbors.add(to);
		}
		else {
			inEdges.add(e);
            inNeighbors.add(from);
		}
	}

	void deleteNeighbor(Edge e) {
        Vertex from = e.getFrom(), to = e.getTo();
        Vertex v = null;
        if (this == from)			v = to;
        else if (this == to)		v = from;
        else throw new Error(e + " not incident to " + this);

        boolean edgeExisted = true;
        boolean vertExisted = true;
        if (!e.getDirected()) {
            edgeExisted = undirectedEdges.remove(e);
            vertExisted = undirectedNeighbors.remove(v);
        } else if (this == from) {
            edgeExisted = outEdges.remove(e);
            vertExisted = outNeighbors.remove(to);
        } else {
            edgeExisted = inEdges.remove(e);
            vertExisted = inNeighbors.remove(from);
        }

        if (!edgeExisted || !vertExisted) {
            throw new Error(e + " not incident to " + this);
        }
	}
	
	public Set<Vertex> getAllNeighbors() {
	    Set<Vertex> neighbors = new HashSet<Vertex> (undirectedNeighbors.size() + inNeighbors.size() + outNeighbors.size());
	    neighbors.addAll(undirectedNeighbors);
	    neighbors.addAll(inNeighbors);
	    neighbors.addAll(outNeighbors);
        return neighbors;
	}

    public Set<Edge> getInEdges() {
        return new HashSet<Edge>(inEdges);
    }

    public Set<Edge> getOutEdges() {
        return new HashSet<Edge>(outEdges);
    }

	//public String toString () {return "(Vertex: " + name + ")";}
	@Override
    public String toString () {
	    return "(Vertex: " + data + " )";
	}
}