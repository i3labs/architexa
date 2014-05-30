package com.architexa.diagrams.draw2d;

import org.eclipse.swt.graphics.Color;

import com.architexa.org.eclipse.draw2d.AbstractBackground;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Insets;

public class ShadowBorder extends AbstractBackground {

	protected final Color color;
	protected final Dimension displacement;
	protected final Insets insets;

	public ShadowBorder(Color _color, int _displacement) {
		this(_color, new Dimension(_displacement, _displacement));
	}
	
	public ShadowBorder(Color _color, Dimension _displacement) {
		this.color = _color;
		this.displacement = _displacement;
		this.insets = new Insets();
		if (displacement.height > 0)
			this.insets.bottom = displacement.height;
		else
			this.insets.top = -displacement.height;
		if (displacement.width > 0)
			this.insets.right = displacement.width;
		else
			this.insets.left = -displacement.width;
	}

	@Override
	public Insets getInsets(IFigure figure) {
		return insets;
	}

	@Override
	public boolean isOpaque() {
		return false;
	}

	@Override
	public void paintBackground(IFigure figure, Graphics graphics, Insets insets) {
		tempRect.setBounds(getPaintRectangle(figure, this.insets));
		tempRect.translate(displacement.width, displacement.height);

		// note: this draws on the whole background - but we need that - to
		// ensure that we don't have holes
		if (color != null)
			graphics.setBackgroundColor(color);
		graphics.fillRectangle(tempRect);
	}

}
