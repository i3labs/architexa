package com.architexa.org.eclipse.draw2d;

import org.eclipse.swt.graphics.Color;

import com.architexa.org.eclipse.draw2d.AbstractBorder;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Insets;

// based on lineBorder
public class RoundedBorder extends AbstractBorder {

	private int width;
	private Color color;
	private Dimension corner;

	public RoundedBorder(Color _color, int _width) {
		this.color = _color;
		this.width = _width;
		this.corner = new Dimension(_width * 2, _width * 2);
	}
	public RoundedBorder(Color _color) {
		this(_color, 4);
	}

	public void setCornerDimensions(Dimension d) {
		corner.width = d.width;
		corner.height = d.height;
	}


	public Insets getInsets(IFigure figure) {
		return new Insets(this.width);
	}

	@Override
	public boolean isOpaque() {
		return true;
	}


	public void setColor(Color color) {
		this.color = color;
	}
	public Color getColor() {
		return this.color;
	}

	public void setWidth(int width) {
		this.width = width;
	}
	public int getWidth() {
		return this.width;
	}

	public void paint(IFigure figure, Graphics graphics, Insets insets) {
		tempRect.setBounds(getPaintRectangle(figure, insets));
		if (getWidth() % 2 == 1) {
			tempRect.width--;
			tempRect.height--;
		}
		tempRect.shrink(getWidth() / 2, getWidth() / 2);
		graphics.setLineWidth(getWidth());
		if (getColor() != null)
			graphics.setForegroundColor(getColor());
		graphics.drawRoundRectangle(tempRect, corner.width, corner.height);
	}

}
