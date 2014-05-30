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
 * Created on Jun 14, 2004
 */
package com.architexa.diagrams.relo.figures;

import org.eclipse.swt.graphics.Color;

import com.architexa.org.eclipse.draw2d.AbstractBorder;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Insets;

public class PartialLineBorder extends AbstractBorder {
	boolean top = false;
	boolean left = false;
	boolean bottom = false;
	boolean right = false;
	
	public int width = 1;
	public Color lineColor = ColorConstants.black;

	public PartialLineBorder(boolean top, boolean left, boolean bottom, boolean right) {

		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;

		this.lineColor = ColorConstants.black;
	}
	public PartialLineBorder(Color color, int width, 
			boolean top, boolean left, boolean bottom, boolean right) {
		this(top, left, bottom, right);
		this.lineColor = color;
		this.width = width;
	}

	public Insets getInsets(IFigure figure) {
		return new Insets(
			top ? width : 0,
			left ? width : 0,
			bottom ? width : 0,
			right ? width : 0);
	}
	public void paint(IFigure figure, Graphics graphics, Insets insets) {
		tempRect.setBounds(getPaintRectangle(figure, insets));
		if (width % 2 == 1) {
			tempRect.width--;
			tempRect.height--;
		}
		tempRect.shrink(width / 2, width / 2);
		graphics.setLineWidth(width);
		if (lineColor != null) {
			graphics.setForegroundColor(lineColor);
		}
		if (top) {
			graphics.drawLine(tempRect.getTopLeft(), tempRect.getTopRight());
		}
		if (left) {
			graphics.drawLine(tempRect.getTopLeft(), tempRect.getBottomLeft());
		}
		if (bottom) {
			graphics.drawLine(tempRect.getBottomLeft(), tempRect.getBottomRight());
		}
		if (right) {
			graphics.drawLine(tempRect.getTopRight(), tempRect.getBottomRight());
		}
	}
}