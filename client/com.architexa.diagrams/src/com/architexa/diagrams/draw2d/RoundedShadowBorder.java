package com.architexa.diagrams.draw2d;

import org.eclipse.swt.graphics.Color;

import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Insets;

public class RoundedShadowBorder extends ShadowBorder {

	private final Dimension corner = new Dimension(8, 8);

	public RoundedShadowBorder(Color _color, int _displacement) {
		this(_color, new Dimension(_displacement, _displacement));
	}
	
	public RoundedShadowBorder(Color _color, Dimension _displacement) {
		super(_color, _displacement);
	}
	
	public void setCorner(int width, int height) {
		corner.height = height;
		corner.width = width;
	}

	@Override
	public void paintBackground(IFigure figure, Graphics graphics, Insets insets) {
		tempRect.setBounds(getPaintRectangle(figure, this.insets));
		tempRect.translate(displacement.width, displacement.height);

		// note: this draws on the whole background - but we need that - to
		// ensure that we don't have holes
		if (color != null)
			graphics.setBackgroundColor(color);
		graphics.fillRoundRectangle(tempRect, corner.width, corner.height);
	}

}
