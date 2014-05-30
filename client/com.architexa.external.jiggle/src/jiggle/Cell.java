package jiggle;

import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

public abstract class Cell {
	// TODO: move this to Vertex
	public Object data = null;

	private Graph graph = null;

	private Rectangle bounds = new Rectangle();

	public Graph getGraph() {
		return graph;
	}

	protected void setGraph(Graph c) {
		graph = c;
	}

	protected Cell() {}

	public void recomputeBoundaries(int[] center, int[] size) {
		bounds.x = center[0] - size[0] / 2;
		bounds.y = center[1] - size[1] / 2;
	}

	public void recomputeSize(int[] min, int[] max) {
		bounds.width = max[0] - min[0];
		bounds.height = max[1] - min[1];
	}

	public int[] getMin() {
		return new int[] { bounds.x, bounds.y };
	}

	public int[] getMax() {
		return new int[] { bounds.x + bounds.width, bounds.y + bounds.height };
	}

	public int[] getSize() {
		return new int[] {bounds.width, bounds.height};
	}

	public int[] getCenter() {
		Point center = bounds.getCenter();
		return new int[] {center.x, center.y};
	}
	
	public void setCenter(Point newCenter) {
		// keep size constant
		recomputeBoundaries(new int[] {newCenter.x, newCenter.y}, getSize());
	}


	public Rectangle getBounds() {
		return bounds;
	}

	public void setBounds(Rectangle bounds) {
		this.bounds = bounds.getCopy();
	}

}