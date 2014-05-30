package com.architexa.diagrams.chrono.figures;


import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Insets;
import com.architexa.org.eclipse.draw2d.geometry.Point;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class BottomShadowBorder extends ParticularSidesBorder {

	public BottomShadowBorder() {
		super(false, true, false, false);
	}

	@Override
	public void paint(IFigure figure, Graphics graphics, Insets insets) {
		super.paint(figure, graphics, insets);

		Point bottomStart = new Point(tempRect.getBottomLeft().x, tempRect.getBottomLeft().y-1);
		Point bottomEnd = new Point(tempRect.getBottomRight().x, tempRect.getBottomRight().y-1);
		graphics.setForegroundColor(ColorScheme.borderShadow);
		graphics.drawLine(bottomStart, bottomEnd);
	}

}
