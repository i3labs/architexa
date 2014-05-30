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

import com.architexa.org.eclipse.draw2d.AbstractConnectionAnchor;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author vineet
 *
 */
public class SideCenteredAnchor extends AbstractConnectionAnchor {
    // TODO we need to move this code somewhere more general
    public static class Side {
        protected Side() {}
    }
    public final static Side top = new Side();
    public final static Side left = new Side();
    public final static Side bottom = new Side();
    public final static Side right = new Side();

    protected Side anchoredSide;
    
    public SideCenteredAnchor(IFigure owner, Side anchoredSide) {
    	super(owner);
    	this.anchoredSide = anchoredSide;
    }

    public Point getLocation(Point reference) {
        Rectangle box = Rectangle.SINGLETON;
        box.setBounds(getOwner().getBounds());
    	getOwner().translateToAbsolute(box);

        if (anchoredSide == SideCenteredAnchor.left) {
            return box.getTopLeft().translate(0, box.height/2);
        }
        if (anchoredSide == SideCenteredAnchor.right) {
            return box.getTopRight().translate(0, box.height/2);
        }
        if (anchoredSide == SideCenteredAnchor.top) {
            return box.getTopLeft().translate(box.width/2, 0);
        }
        if (anchoredSide == SideCenteredAnchor.bottom) {
            return box.getBottomLeft().translate(box.width/2, 0);
        }

        throw new RuntimeException("Invalid argument exception!");
    }


}
