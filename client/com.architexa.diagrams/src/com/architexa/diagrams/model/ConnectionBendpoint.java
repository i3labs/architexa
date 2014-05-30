package com.architexa.diagrams.model;

import com.architexa.org.eclipse.draw2d.Bendpoint;
import com.architexa.org.eclipse.draw2d.geometry.Point;

public class ConnectionBendpoint extends DerivedArtifact implements Bendpoint{

	Point location;
	
	public ConnectionBendpoint() {
		super(null);
	}

	public ConnectionBendpoint(Point p) {
		super(null);
		this.location = p;
	}
	
	public Point getLocation() {
		return new Point(location);
	}

	@Override
	public String toString() {
		return super.toString() + "(" + location + ")";
	}

}
