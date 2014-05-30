package com.architexa.diagrams.chrono.figures;

import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LineBorder;
import com.architexa.org.eclipse.draw2d.geometry.Insets;
import com.architexa.org.eclipse.draw2d.geometry.Point;

/**
 * A line border on the specified sides. A figure with a ParticularSidesBorder can have 
 * a border on any single side or on any combination of sides.
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class ParticularSidesBorder extends LineBorder {
	
	boolean top = true;
	boolean bottom = true;
	boolean left = true;
	boolean right = true;
	
	public ParticularSidesBorder(boolean top, boolean bottom, boolean left, boolean right) {
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}
	
	@Override
	public void paint(IFigure figure, Graphics graphics, Insets insets) {
		tempRect.setBounds(getPaintRectangle(figure, insets));
		if (getWidth() % 2 == 1) {
			tempRect.width--;
			tempRect.height--;
		} 
		tempRect.shrink(getWidth() / 2, getWidth() / 2);

		graphics.setLineWidth(getWidth());
		if (getColor() != null) graphics.setForegroundColor(getColor());
		
		if(top) {
			Point topStart = new Point(tempRect.getTopLeft().x, tempRect.getTopLeft().y);
			Point topEnd = new Point(tempRect.getTopRight().x, tempRect.getTopRight().y);
			graphics.drawLine(topStart, topEnd);
		}
		
		if(bottom) {
			Point bottomStart = new Point(tempRect.getBottomLeft().x, tempRect.getBottomLeft().y);
			Point bottomEnd = new Point(tempRect.getBottomRight().x, tempRect.getBottomRight().y);
			graphics.drawLine(bottomStart, bottomEnd);
		}
		
		if(left) {
			Point leftStart = new Point(tempRect.getTopLeft().x, tempRect.getTopLeft().y);
			Point leftEnd = new Point(tempRect.getBottomLeft().x, tempRect.getBottomLeft().y);
			graphics.drawLine(leftStart, leftEnd);
		}
		
		if(right) {
			Point rightStart = new Point(tempRect.getTopRight().x, tempRect.getTopRight().y);
			Point rightEnd = new Point(tempRect.getBottomRight().x, tempRect.getBottomRight().y);
			graphics.drawLine(rightStart, rightEnd);
		}
	}

}
