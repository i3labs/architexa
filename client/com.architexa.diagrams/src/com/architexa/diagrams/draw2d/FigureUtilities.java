package com.architexa.diagrams.draw2d;


import com.architexa.org.eclipse.draw2d.AbstractBackground;
import com.architexa.org.eclipse.draw2d.BackgroundableCompoundBorder;
import com.architexa.org.eclipse.draw2d.Border;
import com.architexa.org.eclipse.draw2d.CompoundBorder;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.MarginBorder;

public class FigureUtilities {
	
	public static IFigure addPadding(IFigure inFig, int vert, int horz) {
		return addPadding(inFig, vert, horz, vert, horz);
	}

	public static IFigure addPadding(IFigure inFig, int t, int l, int b, int r) {
		insertBorder(inFig, new MarginBorder(t,l,b,r));
		return inFig;
	}
	
	public static void removeBorder(IFigure inFig, Class<? extends Border> borderTypeToRemove) {
		Border curBorder = inFig.getBorder();
		if (curBorder == null) return;
		
		if (borderTypeToRemove.isInstance(curBorder)) {
			inFig.setBorder(null);
			return;
		}
		
		if (curBorder instanceof CompoundBorder) {
			curBorder = ((CompoundBorder)curBorder).getInnerBorder();
			inFig.setBorder(curBorder);
		}
	}

	public static void insertBorder(IFigure inFig, Border borderForInsertion) {
		if (inFig == null) return;
		Border oldBorder = inFig.getBorder();
		if (oldBorder == null) {
			inFig.setBorder(borderForInsertion);
			return;
		}
		
		if (borderForInsertion instanceof AbstractBackground || oldBorder instanceof AbstractBackground)
			inFig.setBorder(new BackgroundableCompoundBorder(borderForInsertion, oldBorder));
		else
			inFig.setBorder(new CompoundBorder(borderForInsertion, oldBorder));
	}

}
