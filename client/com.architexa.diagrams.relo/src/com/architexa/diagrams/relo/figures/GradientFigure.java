package com.architexa.diagrams.relo.figures;

import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.draw2d.IFigureWithContents;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.handles.HandleBounds;

public class GradientFigure extends Figure implements IFigureWithContents, HandleBounds {
	private Color begColor = null;
	private boolean vertical = true;
	protected final IFigure contentFigure;

	public GradientFigure(IFigure contentFigure) {
		this.setLayoutManager(new ToolbarLayout());
		this.contentFigure = contentFigure;
		this.add(contentFigure);
	}
	public GradientFigure(IFigure contentFigure, boolean vert, Color begColor, Color endColor) {
		this(contentFigure);
		this.vertical = vert;
		this.begColor = begColor;
		this.setBackgroundColor(endColor);
	}

	public static GradientFigure wrap(IFigure contentFigure, boolean vert, Color begColor, Color endColor) {
		return new GradientFigure(contentFigure, vert, begColor, endColor);
	}
	
	public void setVertBegGradient(Color startColor) {
		this.begColor = startColor;
		this.vertical  = true;
	}
	public void setHorzBegGradient(Color startColor) {
		this.begColor = startColor;
		this.vertical  = false;
	}
	
	public Color getBegColor() {
		return begColor;
	}

	@Override
	public void paintFigure(Graphics g) {
		super.paintFigure(g);
		if (this.begColor != null) {
			Rectangle r = this.getBounds().getCopy();
			r.crop(this.getInsets());					// @tag why-needed?
			//int newWidth = Math.min(50, r.width);
			//r.x += r.width - newWidth;
			//r.width = newWidth;
			g.setForegroundColor(this.begColor);
			g.setBackgroundColor(this.getBackgroundColor());
			g.fillGradient(r, vertical);
		}
	}

	public IFigure getContentFig() {
		return this.contentFigure;
	}
	public Rectangle getHandleBounds() {
		if (contentFigure instanceof HandleBounds)
			return ((HandleBounds)contentFigure).getHandleBounds();
		return getBounds();
	}

}
