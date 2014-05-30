/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.architexa.org.eclipse.draw2d;

import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Orientable;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.Shape;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.PointList;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

/**
 * A triangular graphical figure.
 * This class is identical to Triangle with the following differences:
 * 1> Triangle does not have to be closed (for arrow heads)
 * 2> Access method to get the current tip position
 * 3> Access method to get the current triangle base
 */
public final class Triangle2
	extends Shape
	implements Orientable
{
// Added by Vineet
protected boolean closed = true;
public void setClosed(boolean close) {
	closed = close;
}
public boolean isClosed() {
	return closed;
}

public Point getTip() {
    if (triangle.size() == 0) return null;
    return triangle.getPoint(1);
}
public Point getBase() {
    if (triangle.size() == 0) return null;
    if (!closed) return getTip();
    Point p1 = triangle.getPoint(0);
    Point p2 = triangle.getPoint(2);
    return new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
}
    
    
    
/** The direction this triangle will face. Possible values are 
 * {@link PositionConstants#NORTH}, {@link PositionConstants#SOUTH}, 
 * {@link PositionConstants#EAST} and {@link PositionConstants#WEST}.
 */
protected int direction = NORTH;
/** The orientation of this triangle.  Possible values are {@link Orientable#VERTICAL} 
 * and {@link Orientable#HORIZONTAL}.
 */
protected int orientation = VERTICAL;

/** The points of the triangle. */
protected PointList triangle = new PointList(3);

/**
 * @see Shape#fillShape(Graphics)
 */
@Override
protected void fillShape(Graphics g) {
	g.fillPolygon(triangle);
}


/**
 * @see Shape#outlineShape(Graphics)
 */
@Override
protected void outlineShape(Graphics g) {
	g.setForegroundColor(g.getBackgroundColor());
	if (closed)
	    g.drawPolygon(triangle);
	else
	    g.drawPolyline(triangle);
}

/**
 * @see Figure#primTranslate(int, int)
 */
@Override
public void primTranslate(int dx, int dy) {
	super.primTranslate(dx, dy);
	triangle.translate(dx, dy);
}

/**
 * @see Orientable#setDirection(int)
 */
public void setDirection(int value) {
	if ((value & (NORTH | SOUTH)) != 0)
		orientation = VERTICAL;
	else
		orientation = HORIZONTAL;
	direction = value;
	revalidate();
	repaint();
}

/**
 * @see Orientable#setOrientation(int)
 */
public void setOrientation(int value) {
	if (orientation == VERTICAL && value == HORIZONTAL) {
		if (direction == NORTH) setDirection(WEST);
		else setDirection(EAST);
	}
	if (orientation == HORIZONTAL && value == VERTICAL) {
		if (direction == WEST) setDirection(NORTH);
		else setDirection(SOUTH);
	}
}

/**
 * @see IFigure#validate()
 */
@Override
public void validate() {
	super.validate();
	Rectangle r = new Rectangle();
	r.setBounds(getBounds());
	r.crop(getInsets());
	r.resize(-1, -1);
	int size;
	switch (direction & (NORTH | SOUTH)) {
		case 0: //East or west.
			size = Math.min(r.height / 2, r.width);
			r.x += (r.width - size) / 2;
			break;
		default: //North or south
			size = Math.min(r.height, r.width / 2);
			r.y += (r.height - size) / 2;
			break;
	}

	size = Math.max(size, 1); //Size cannot be negative

	Point head, p2, p3;

	switch (direction) {
		case NORTH:
			head = new Point(r.x + r.width / 2, r.y);
			p2   = new Point (head.x - size, head.y + size);
			p3   = new Point (head.x + size, head.y + size);
			break;
		case SOUTH:
			head = new Point (r.x + r.width / 2, r.y + size);
			p2   = new Point (head.x - size, head.y - size);
			p3   = new Point (head.x + size, head.y - size);
			break;
		case WEST:
			head = new Point (r.x, r.y + r.height / 2);
			p2   = new Point (head.x + size, head.y - size);
			p3   = new Point (head.x + size, head.y + size);
			break;
		default:
			head = new Point(r.x + size, r.y + r.height / 2);
			p2   = new Point(head.x - size, head.y - size);
			p3   = new Point(head.x - size, head.y + size);

	}
	triangle.removeAllPoints();
	triangle.addPoint(p2);
	triangle.addPoint(head);
	triangle.addPoint(p3);
}

}