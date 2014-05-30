/* 
 * Copyright (c) 2004-2006 Massachusetts Institute of Technology. This code was
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
 * Created on Aug 23, 2007
 */
package com.architexa.diagrams.strata.figures;

import com.architexa.org.eclipse.draw2d.ArrowLocator;
import com.architexa.org.eclipse.draw2d.Connection;
import com.architexa.org.eclipse.draw2d.ConnectionLocator;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.PolylineConnection;
import com.architexa.org.eclipse.draw2d.RotatableDecoration;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.PointList;

// basically a lineConnection, but has a gap in between, we really not support
// routers, etc. The interface therefore needs to be cleaned up, but we are
// getting a lot of functionality for free
public class BrokenPolylineConnection extends PolylineConnection {

    private final int segLength;
    private final boolean breakLine;
    
    public BrokenPolylineConnection(int segLength, boolean breakLine) {
        this.segLength = segLength;
        this.breakLine = breakLine;
    }

    // cached variables
    private double length;
    private double ratio;

    protected Point srcSegPt1, srcSegPt2;
    private PointList srcSeg;

    protected Point tgtSegPt1, tgtSegPt2;
    private PointList tgtSeg;


    @Override
    public void setPoint(Point pt, int index) {
        super.setPoint(pt, index);
        updateCache();
    }
    @Override
    public void setPoints(PointList points) {
        super.setPoints(points);
        updateCache();
    }

    private void updateCache() {
        PointList curPoints = getPoints();
        srcSegPt1 = curPoints.getFirstPoint();
        tgtSegPt1 = curPoints.getLastPoint();
        length = tgtSegPt1.getDistance(srcSegPt1);
        ratio = 1.0*segLength / length;

        double x, y;
        x = (1-ratio)*srcSegPt1.x + ratio*tgtSegPt1.x;
        y = (1-ratio)*srcSegPt1.y + ratio*tgtSegPt1.y;
        srcSegPt2 = new Point(x,y);

        srcSeg = new PointList();
        srcSeg.addPoint(srcSegPt1);
        srcSeg.addPoint(srcSegPt2);
        
        x = (1-ratio)*tgtSegPt1.x + ratio*srcSegPt1.x;
        y = (1-ratio)*tgtSegPt1.y + ratio*srcSegPt1.y;
        tgtSegPt2 = new Point(x,y);

        tgtSeg = new PointList();
        tgtSeg.addPoint(tgtSegPt1);
        tgtSeg.addPoint(tgtSegPt2);
    }

    // we implement this to ensure that the primary layer does get the mouse
	// events when we are hidden.
    @Override
	public boolean containsPoint(int x, int y) {
    	if (isVisible() && isEnabled())
    		return super.containsPoint(x, y);
    	else
    		return false;
    }

    public void setTargetDecorations(RotatableDecoration dec1, RotatableDecoration dec2) {
        this.setTargetDecoration(dec1);
        if (breakLine)
            add(dec2, new BrokenSegmentLocator(this, ConnectionLocator.TARGET, /*srcSeg*/false));
    }
    public void setSourceDecorations(RotatableDecoration dec1, RotatableDecoration dec2) {
        this.setSourceDecoration(dec1);
        if (breakLine)
            add(dec2, new BrokenSegmentLocator(this, ConnectionLocator.SOURCE, /*srcSeg*/true));
    }

    @Override
    protected void outlineShape(Graphics g) {
        if (breakLine) {
            g.drawPolyline(srcSeg);
            g.drawPolyline(tgtSeg);
        } else {
            super.outlineShape(g);
        }
    }

}

class BrokenSegmentLocator extends ArrowLocator {
    private final boolean srcSeg;
    public BrokenSegmentLocator(Connection connection, int align, boolean srcSeg) {
        super(connection, align);
        this.srcSeg = srcSeg;
    }
    @Override
    protected Point getLocation(PointList points) {
        if (srcSeg)
            return ((BrokenPolylineConnection)this.getConnection()).tgtSegPt2;
        else
            return ((BrokenPolylineConnection)this.getConnection()).srcSegPt2;
    }
}