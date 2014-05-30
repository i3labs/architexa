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
 * Created on Oct 23, 2004
 *
 */
package com.architexa.diagrams.relo.parts;

import com.architexa.org.eclipse.draw2d.ChopboxAnchor;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author vineet
 *
 */
public class SideRestrictedChopboxAnchor extends ChopboxAnchor {
    public static class Side {
        protected Side() {}
    }
    final static Side top = new Side();
    final static Side left = new Side();
    final static Side bottom = new Side();
    final static Side right = new Side();

    protected Side allowedSide;
    public SideRestrictedChopboxAnchor(IFigure owner, Side allowedSide) {
    	super(owner);
    	this.allowedSide = allowedSide;
    }

    /* (non-Javadoc)
     * @see com.architexa.diagrams.relo.eclipse.gef.ConnectionAnchor#getLocation(com.architexa.diagrams.relo.eclipse.gef.geometry.Point)
     */
    @Override
    public Point getLocation(Point reference) {
        Point chopBoxLocation = super.getLocation(reference);
        Rectangle box = new Rectangle(getBox());
    	getOwner().translateToAbsolute(box);
        Side chopBoxLocationSide = getSide(box, chopBoxLocation);
        
        // common case
        if (chopBoxLocationSide == allowedSide) return chopBoxLocation;
        
        if (chopBoxLocationSide == SideRestrictedChopboxAnchor.right) {
            if (allowedSide == top) return box.getTopRight();
            if (allowedSide == bottom) return box.getBottomRight();
        }
        if (chopBoxLocationSide == SideRestrictedChopboxAnchor.left) {
            if (allowedSide == top) return box.getTopLeft();
            if (allowedSide == bottom) return box.getBottomLeft();
        }
        if (chopBoxLocationSide == SideRestrictedChopboxAnchor.top) {
            if (allowedSide == left) return box.getTopLeft();
            if (allowedSide == right) return box.getTopRight();
        }
        if (chopBoxLocationSide == SideRestrictedChopboxAnchor.bottom) {
            if (allowedSide == left) return box.getBottomLeft();
            if (allowedSide == right) return box.getBottomRight();
        }
        
        // in this case - just give something that meets the criteria (we will invert the coords)
        System.err.print("Bad Layout!! " + chopBoxLocationSide + " " + allowedSide);
        
        Point boxCenter  = box.getCenter();
    	getOwner().translateToAbsolute(boxCenter);
        Dimension dstToCenter = boxCenter.getDifference(chopBoxLocation);
        return chopBoxLocation.translate(dstToCenter).translate(dstToCenter);
    }

    private static Side getSide(Rectangle box, Point chopBoxLocation) {
        // chopBox does a translate(-1,-1).grow(1,1)
        if (chopBoxLocation.x == box.x-1) {
            return SideRestrictedChopboxAnchor.left;
        } else if (chopBoxLocation.y == box.y-1) {
            return SideRestrictedChopboxAnchor.top;
        } else if (chopBoxLocation.x == box.right()) {
            return SideRestrictedChopboxAnchor.right;
        } else if (chopBoxLocation.y == box.bottom()) {
            return SideRestrictedChopboxAnchor.bottom;
        }
        System.err.println("Failed to getSide: " + chopBoxLocation + " in box: " + box);
        return null;
    }
}
