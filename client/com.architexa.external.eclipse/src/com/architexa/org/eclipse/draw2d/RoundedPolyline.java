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
 * Created on Oct 27, 2004
 */
package com.architexa.org.eclipse.draw2d;

import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.Polyline;
import com.architexa.org.eclipse.draw2d.Shape;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.PointList;

/**
 * @author vineet
 *
 */
public class RoundedPolyline extends Polyline {

	protected int cornerLen = 10;

	/**
	 * Sets the length of the corner on each edge.
	 *
	 * @param d the dimensions of the corner
	 */
	public void setCornerLength(int len) {
		cornerLen = len;
	}
	
	private Dimension getCornerDimension(Point pt1, Point pt2, boolean twoCorners) {
		/*
		 * scale the distance between the two points such that the distance is 
		 * equal to the cornerLen
		 */
		double scale = cornerLen / pt1.getDistance(pt2);
		if (twoCorners)
			scale = Math.min(0.5, scale);
		else
			scale = Math.min(1, scale);
		return pt2.getDifference(pt1).scale(scale);
	}

	/**
	 * @see Shape#outlineShape(Graphics)
	 */
	@Override
    protected void outlineShape(Graphics g) {
		PointList pointList = getPoints();

		int sz = pointList.size();
		Point prevPt = pointList.getPoint(0);
		Point prevPtBendBeg = null;
		for (int i = 1; i < sz; i++) {
			Point currPt = pointList.getPoint(i);
			
			boolean corner = (prevPtBendBeg != null);
			Dimension cornerDimension = getCornerDimension(prevPt, currPt, corner);
			
			// draw corner if there is a corner beginning
			if (corner) {
				Point prevPtBendEnd = prevPt.getTranslated(cornerDimension);
				if (cornerLen > 2) {
					drawBezier(g, prevPtBendBeg, prevPt, prevPtBendEnd, prevPt, 1.0/cornerLen);
				} else {
					g.drawLine(prevPtBendBeg, prevPtBendEnd);
				}
				prevPt = prevPtBendEnd;
			}
			
			Point curPtLineEnd = currPt;	// default to currPt
			if (i < sz) {
				curPtLineEnd = currPt.getTranslated(cornerDimension.negate());
			}
			g.drawLine(prevPt, curPtLineEnd);
			
			prevPt = currPt;
			prevPtBendBeg = curPtLineEnd;
		}

	}
	
	static class BezierDimension {
		double a, b, c, d;
		/* based on: http://www.moshplant.com/direct-or/bezier/math.html
		 * 
		 * given (in dimension): 
		 *  p0 - start point
		 *  p1 - start control point
		 *  p2 - end control point
		 *  p3 - end point 
		 * 
		 * based on the spec:
		 *  value(t) = a.t.t.t + b.t.t + c.t + d
		 *  p0 = d
		 *  p1 = p0+c/3
		 *  p2 = p1+(c+b)/3
		 *  p3 = p0+c+b+a
		 */
		public BezierDimension(int p0, int p1, int p2, int p3) {
			d = p0;
			c = 3 * (p1 - p0);
			b = 3 * (p2 - p1) - c;
			a = p3 - p0 - c - b;
		}
		public int getValue(double t) {
			// added 0.5 so that floor is actually rounding
			return (int) (a*t*t*t + b*t*t + c*t + d + 0.5);
		}
		
	};

	static private void drawBezier(Graphics g, 
			Point startPt,
			Point startCtrlPt, 
			Point endPt, 
			Point endCtrlPt, 
			double step) {
		BezierDimension x = new BezierDimension(startPt.x, startCtrlPt.x, endCtrlPt.x, endPt.x);
		BezierDimension y = new BezierDimension(startPt.y, startCtrlPt.y, endCtrlPt.y, endPt.y);
		double t = 0;
		Point midPt = startPt;
		while (t < 1) {
			Point nextPt = new Point();
			nextPt.setLocation(x.getValue(t), y.getValue(t));
			g.drawLine(midPt, nextPt);
			t += step;
			midPt = nextPt;
		}
		g.drawLine(midPt, endPt);
	}
	
}
