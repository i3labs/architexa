package jiggle;

/* Class for edges of a graph.  NOTE: the only mutable characteristics
of an edge are its label, directedness, and preferred length. */

public class Edge {

	private Vertex from, to; /* endpoints of the edge */
	private boolean directed = false; /* is the edge directed? */
	
	public Edge(Graph g, Vertex f, Vertex t) {
        from = f;
        to = t;
        // setContext(g);
    }

	public Edge (Graph g, Vertex f, Vertex t, boolean dir) {
		from = f;
        to = t;
        // setContext(g);
        directed = dir;
	}

	public Vertex getFrom () {return from;}
	public Vertex getTo () {return to;}

	boolean getDirected () {return directed;}
	void setDirected (boolean d) {directed = d;}

	@Override
    public String toString () {
		return "(Edge: " + from + ", " + to + ", " +
			 (directed ? "directed" : "undirected") + ")";
	}
}