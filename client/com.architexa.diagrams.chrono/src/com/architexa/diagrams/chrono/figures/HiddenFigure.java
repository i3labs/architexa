package com.architexa.diagrams.chrono.figures;


import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class HiddenFigure extends FigureWithGap {

	private boolean visible = true;

	public HiddenFigure(boolean visible, String tooltip) {
		this.visible = visible;

		ToolbarLayout layout = new ToolbarLayout(true);
		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		setLayoutManager(layout);

		gap = new GapFigure("hidden for " + tooltip);
		if(isCompletelyHidden()) {
			gap.setSize(3, 0);
		} else {
			gap.setSize(3, METHOD_BOX_GAP);
		}

		if(SeqUtil.debugHighlightingOn) {
			gap.setBackgroundColor(ColorConstants.green);
			gap.setOpaque(true);
		}

		IFigure figure = new Label(". . .");
		figure.setVisible(visible);
		add(figure);

		if(!tooltip.equals("")) setToolTip(new Label(tooltip));

		if(SeqUtil.debugHighlightingOn) {
			if(visible)
				setBackgroundColor(ColorConstants.cyan);
			else
				setBackgroundColor(ColorConstants.orange);
			setOpaque(true);
		}
	}

	public boolean isCompletelyHidden() {
		return !visible;
	}

	@Override
	public String toString() {
		String visibleString = visible ? "visible" : "invisible";
		return super.toString() + visibleString + ": " + getToolTip();
	}

}
