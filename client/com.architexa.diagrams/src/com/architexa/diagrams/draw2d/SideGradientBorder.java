package com.architexa.diagrams.draw2d;

import org.eclipse.swt.graphics.Color;

import com.architexa.org.eclipse.draw2d.AbstractBorder;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.geometry.Insets;

public class SideGradientBorder extends AbstractBorder {

	protected Insets insets;
	private final int direction;
	private final int width;
	private final Color endColor;
	private final Color begColor;

	public SideGradientBorder(int direction, int width, Color begColor, Color endColor) {
		this.direction = direction;
		this.width = width;
		this.begColor = begColor;
		this.endColor = endColor;
		
    	switch (direction) {
        case PositionConstants.LEFT:
        	insets = new Insets(0, width, 0, 0);
            break;
        case PositionConstants.RIGHT:
        	insets = new Insets(0, 0, 0, width);
            break;
        case PositionConstants.TOP:
        	insets = new Insets(width, 0, 0, 0);
            break;
        case PositionConstants.BOTTOM:
        	insets = new Insets(0, 0, width, 0);
            break;
        default:
        	throw new IllegalArgumentException("Expect left, right, top, or bottom for direction");
        };
	}
	
	public Insets getInsets(IFigure figure) {
		return insets;
	}

	@Override
	public boolean isOpaque() {
		return true;
	}

	public void paint(IFigure figure, Graphics graphics, Insets insets) {
		boolean vert = false;
		
		tempRect.setBounds(getPaintRectangle(figure, insets));

    	switch (direction) {
        case PositionConstants.LEFT:
        	tempRect.width = width;
            break;
        case PositionConstants.RIGHT:
        	tempRect.x  += tempRect.width - width;
        	tempRect.width = width;
            break;
        case PositionConstants.TOP:
        	tempRect.height = width;
        	vert = true;
            break;
        case PositionConstants.BOTTOM:
        	tempRect.y  += tempRect.height - width;
        	tempRect.height = width;
        	vert = true;
            break;
        };
		graphics.setForegroundColor(this.begColor);
		graphics.setBackgroundColor(this.endColor);
		graphics.fillGradient(tempRect, vert);
	}

}
