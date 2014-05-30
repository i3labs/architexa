package com.architexa.diagrams.chrono.figures;

import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.RectangleFigure;
import com.architexa.org.eclipse.draw2d.geometry.PointList;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class CutCornerFigure extends RectangleFigure {

	MethodBoxFigure parent;

	public CutCornerFigure(MethodBoxFigure parent) {
		this.parent = parent;
	}

	private static int cornerSize = 3;

	@Override
	public void paintFigure(Graphics graphics) {

		Rectangle rect = getBounds().getCopy();

		graphics.translate(getLocation());

		int corner = parent.getPartner()==null ? 0 : cornerSize;
		int borderWidth = getBorder()==null ? 0 : getBorder().getInsets(this).left+1;
		int cornerBeyondBorder = corner + borderWidth;

		PointList outline = new PointList();
		outline.addPoint(cornerBeyondBorder, 0);
		outline.addPoint(rect.width, 0);
		outline.addPoint(rect.width, rect.height);
		outline.addPoint(cornerBeyondBorder, rect.height);
		outline.addPoint(borderWidth, rect.height - corner - 1);
		outline.addPoint(borderWidth, corner);

		graphics.fillPolygon(outline); 

		graphics.translate(getLocation().getNegated());
	}

}