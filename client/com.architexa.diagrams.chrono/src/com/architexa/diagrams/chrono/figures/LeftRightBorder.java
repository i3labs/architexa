package com.architexa.diagrams.chrono.figures;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LineBorder;
import com.architexa.org.eclipse.draw2d.geometry.Insets;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class LeftRightBorder extends LineBorder {


	public LeftRightBorder(Color color, int width, int lineStyle) {
		super(color, width);
	}

	@Override
	public void paint(IFigure figure, Graphics graphics, Insets insets) {

		tempRect.setBounds(getPaintRectangle(figure, insets));
		if (getWidth() % 2 == 1) {
			tempRect.width--;
			tempRect.height--;
		} 
		tempRect.shrink(getWidth() / 2, getWidth() / 2);

		Point rightSideStart = new Point(tempRect.getTopRight().x, tempRect.getTopRight().y - getWidth()/2);
		Point rightSideEnd = new Point(tempRect.getBottomRight().x, tempRect.getBottomRight().y + getWidth()/2);
		graphics.setForegroundColor(ColorScheme.diagramBackground);
		graphics.setLineWidth(getWidth());
		graphics.drawLine(rightSideStart, rightSideEnd);

		graphics.setLineWidth(1);

		Point leftSideTopLeft = new Point(tempRect.getTopLeft().x - getWidth()/2, tempRect.getTopLeft().y - getWidth()/2);
		Point leftSideBottomRight = new Point(tempRect.getBottomLeft().x + getWidth()/2 - 1, tempRect.getBottomLeft().y + getWidth()/2);

		IFigure parentFig = figure.getParent().getParent().getParent();
		if(!(parentFig instanceof MethodBoxFigure)) { 
			// No overlapping due to calls to the same class that needs to be handled
			Rectangle leftSideBorder = new Rectangle(leftSideTopLeft, leftSideBottomRight);
			graphics.setBackgroundColor(ColorScheme.diagramBackground);
			graphics.fillRectangle(leftSideBorder);
		} else {

			// Need to handle the overlapping caused by calls to the same class

			IFigure parentBoxWithBorder = ((MethodBoxFigure)parentFig).getMethodBox();
			int parentLeft = parentBoxWithBorder.getBounds().getCopy().x + ((LeftRightBorder)parentBoxWithBorder.getBorder()).getWidth();

			// Map each MethodBoxFigure parent to the location of the left side of its method box
			Map<MethodBoxFigure, Integer> overlappedParents = new LinkedHashMap<MethodBoxFigure, Integer>();
			overlappedParents.put((MethodBoxFigure)parentFig, parentLeft);
			IFigure parentBoxWithBorderCopy = parentBoxWithBorder;
			int parentLeftCopy = parentLeft;
			while(leftSideTopLeft.x <= parentLeft) {
				parentBoxWithBorderCopy = parentBoxWithBorder;
				overlappedParents.put((MethodBoxFigure)parentFig, parentLeft);
				if(!(parentFig.getParent().getParent() instanceof MethodBoxFigure)) break;

				parentBoxWithBorder = ((MethodBoxFigure)parentFig.getParent().getParent()).getMethodBox();
				parentLeft = parentBoxWithBorder.getBounds().getLeft().x + ((LeftRightBorder)parentBoxWithBorder.getBorder()).getWidth();
				parentFig = parentFig.getParent().getParent();
			}

			// Draw each section of the border in the color of the parent it overlaps
			if(leftSideTopLeft.x <= parentLeftCopy) {
				Color parentColor = ((LeftRightBorder)parentBoxWithBorderCopy.getBorder()).getColor();
				graphics.setForegroundColor(parentColor);
				graphics.setBackgroundColor(parentColor);

				MethodBoxFigure nextParent = null;
				Point right = new Point(leftSideBottomRight.x-1, leftSideBottomRight.y);
				Iterator<MethodBoxFigure> overlappedParentsIter = overlappedParents.keySet().iterator();
				while(overlappedParentsIter.hasNext()) {
					nextParent = overlappedParentsIter.next();
					int nextParentLeft = overlappedParents.get(nextParent);
					Point left = new Point(nextParentLeft, leftSideTopLeft.y);
					Color c = nextParent.getMethodBox().getBackgroundColor();
					graphics.setForegroundColor(c);
					graphics.setBackgroundColor(c);

					Rectangle rect = new Rectangle(left, right);
					graphics.drawRectangle(rect);
					graphics.fillRectangle(rect);

					right = new Point(nextParentLeft-2, leftSideBottomRight.y);
				}

				// Draw the leftmost section of the border that overlaps no method box
				if(nextParent!=null && leftSideTopLeft.x<right.x) {
					graphics.setForegroundColor(ColorScheme.diagramBackground);
					graphics.setBackgroundColor(ColorScheme.diagramBackground);
					Rectangle rect = new Rectangle(leftSideTopLeft, right);
					graphics.drawRectangle(rect);
					graphics.fillRectangle(rect);
				}

				// Draw the left side line border of each parent
				Color leftLineColor = ColorScheme.diagramBackground;
				overlappedParentsIter = overlappedParents.keySet().iterator();
				while(overlappedParentsIter.hasNext()) {
					nextParent = overlappedParentsIter.next();
					int nextParentLeft = overlappedParents.get(nextParent);
					if(nextParent.getParent()!=null && nextParent.getParent().getParent() instanceof MethodBoxFigure) {
						leftLineColor = ((MethodBoxFigure)nextParent.getParent().getParent()).getMethodBox().getBackgroundColor();
					} else {
						leftLineColor = ColorScheme.diagramBackground;
					}
					graphics.setForegroundColor(leftLineColor);
					graphics.drawLine(new Point(nextParentLeft, nextParent.getMethodBox().getBounds().getTop().y), new Point(nextParentLeft, nextParent.getMethodBox().getBounds().getBottom().y));
				}
			}
		}
	}

}
