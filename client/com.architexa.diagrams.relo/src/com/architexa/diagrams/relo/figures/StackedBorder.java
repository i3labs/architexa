package com.architexa.diagrams.relo.figures;

import java.util.ArrayList;
import java.util.List;

import com.architexa.org.eclipse.draw2d.AbstractBorder;
import com.architexa.org.eclipse.draw2d.Border;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Insets;

public class StackedBorder extends AbstractBorder {
	
	List<Border> borders = new ArrayList<Border> (5);
	
	public StackedBorder() {}
	
	public StackedBorder(Border b1, Border b2) {
		borders.add(b1);
		borders.add(b2);
	}
	
	public void addBorder(Border b) {
		borders.add(b);
	}

	public Insets getInsets(IFigure figure) {
		Insets inset = new Insets();
		for (Border border : borders) {
			Insets borderInset = border.getInsets(figure);
			inset.top = Math.max(inset.top, borderInset.top);
			inset.left = Math.max(inset.top, borderInset.top);
			inset.right = Math.max(inset.top, borderInset.top);
			inset.bottom = Math.max(inset.top, borderInset.top);
		}
		return inset;
	}

	public void paint(IFigure figure, Graphics graphics, Insets insets) {
		for (Border border : borders) {
			border.paint(figure, graphics, insets);
		}
	}

}
