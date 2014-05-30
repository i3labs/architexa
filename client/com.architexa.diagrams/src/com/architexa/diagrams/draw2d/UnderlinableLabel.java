package com.architexa.diagrams.draw2d;

import org.eclipse.swt.graphics.Point;

import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

public class UnderlinableLabel extends Label{

	
	private boolean setUnderlinable = false;
	public UnderlinableLabel(String methodName) {
		super(methodName);
	}

	public void setUnderline(boolean value) {
		setUnderlinable = value;
		//repaint
		getParent().repaint();
	}
	
	
	// This method is duplicate to the super paintFigure except the additional underlining functionality.
	@Override
	protected void paintFigure(Graphics graphics) {
		if (isOpaque())
			super.paintFigure(graphics);
		Rectangle bounds = getBounds();
		graphics.translate(bounds.x, bounds.y);
		if (getIcon() != null)
			graphics.drawImage(getIcon(), getIconLocation());
		if (!isEnabled()) {
			graphics.translate(1, 1);
			graphics.setForegroundColor(ColorConstants.buttonLightest);
			graphics.drawText(getSubStringText(), getTextLocation());
			graphics.translate(-1, -1);
			graphics.setForegroundColor(ColorConstants.buttonDarker);
		}
		if (setUnderlinable)
			drawUnderlinedText(graphics, getSubStringText(), getTextLocation().x, getTextLocation().y);
		else
			graphics.drawText(getSubStringText(), getTextLocation());
		graphics.translate(-bounds.x, -bounds.y);
	}
	
	public void drawUnderlinedText(Graphics gc, String string, int x, int y)
	{
		Point extent = new Point(getSubStringTextSize().width, getSubStringTextSize().height);
	    gc.drawText(string, x, y);
	    gc.drawLine(x - 1, y + extent.y - 1, x + extent.x - 1, y + extent.y - 1);
	}
}
