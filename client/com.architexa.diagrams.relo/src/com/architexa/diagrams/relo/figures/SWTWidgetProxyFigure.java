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
 * Created on Aug 5, 2004
 */
package com.architexa.diagrams.relo.figures;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import com.architexa.diagrams.relo.ReloPlugin;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;




/**
 * @author vineet
 *
 */
public class SWTWidgetProxyFigure extends Figure {
    static final Logger logger = ReloPlugin.getLogger(SWTWidgetProxyFigure.class);
    
	protected Control swtWidget = null;

	protected int defaultWidth = -1;
	protected int defaultHeight = -1;
	
	protected SWTWidgetProxyFigure() {
	}

	public SWTWidgetProxyFigure(Control swtWidget) {
	    this.swtWidget = swtWidget;
	}

	public SWTWidgetProxyFigure(Control swtWidget, int defaultWidth, int defaultHeight) {
	    this.swtWidget = swtWidget;
	    this.defaultHeight = defaultHeight;
	    this.defaultWidth = defaultWidth;
	}
	
	/*
	public static SWTWidgetProxyFigure getSWTButton(Composite parent, String txt) {
        Button button = new org.eclipse.swt.widgets.Button(parent, SWT.BORDER);
		button.setText(txt);
	    return new SWTWidgetProxyFigure(button);
	}
	*/

	@Override
    public Rectangle getBounds() {
        if (swtWidget != null) {
            Point pt = swtWidget.computeSize(defaultWidth, defaultHeight);
            return new Rectangle(0, 0, pt.x, pt.y);
        } else
            return super.getBounds();
    }
	
	@Override
    protected void primTranslate(int dx, int dy) {
		super.primTranslate(dx,dy);
		updateWidgetBounds();
	}

	/**
	 * Updates widgetBounds based on figure bounds
	 */
	public void updateWidgetBounds() {
        if (swtWidget != null) {
            Rectangle newBounds = new Rectangle(bounds);
            if (newBounds.height == 0 || newBounds.width == 0) {
                Point pt = swtWidget.computeSize(defaultWidth, defaultHeight);
                newBounds.width = pt.x;
                newBounds.height = pt.y;
            }
            translateToAbsolute(newBounds);
            swtWidget.setBounds(newBounds.x, newBounds.y, newBounds.width, newBounds.height);
        }
	}

    @Override
    public void removeNotify() {
        super.removeNotify();
        closeWidget();
    }
    @Override
    public void addNotify() {
        if (swtWidget == null) createWidget();
        super.addNotify();
    }

    protected void createWidget() {
        //logger.error("createWidget");
    }

    protected void closeWidget() {
        //logger.error("closeWidget");
		swtWidget.dispose();
        swtWidget = null;
    }
	
}
