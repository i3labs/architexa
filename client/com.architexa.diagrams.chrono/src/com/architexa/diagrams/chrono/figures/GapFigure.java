package com.architexa.diagrams.chrono.figures;

import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.chrono.util.LayoutUtil;
import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class GapFigure extends Figure {

	private Dimension unhiddenGapSize = new Dimension(0, 0);
	private Dimension hiddenGapSize = new Dimension(0, 0);

	public GapFigure(String correspondingMethodName) {
		super();
		setSize(3, 3);

		if(SeqUtil.debugHighlightingOn) {
			setBackgroundColor(ColorConstants.darkGreen);
			setOpaque(true);
		}
		setToolTip(new Label(correspondingMethodName));
	}

	public GapFigure(int gapSize, String correspondingMethodName) {
		setSize(2, (gapSize));

		if(SeqUtil.debugHighlightingOn) {
			setBackgroundColor(ColorConstants.darkGreen);
			setOpaque(true);
		}
		setToolTip(new Label(correspondingMethodName));
	}

	@Override
	public void setSize(int w, int h) {
		super.setSize(w, h);
		unhiddenGapSize = getSize();
	}

	public void setHidden(boolean hidden) {
		if(hidden) {
			Dimension sizeBeforeHiding = getSize();
			setSize(hiddenGapSize);
			unhiddenGapSize = sizeBeforeHiding;

			if(SeqUtil.debugHighlightingOn) {
				setBackgroundColor(new Color(null, 119, 136, 153)); // slate gray
			}
		} else {
			setSize(unhiddenGapSize);

			if(SeqUtil.debugHighlightingOn) {
				setBackgroundColor(new Color(null, 25, 25, 112)); // midnight blue
			}
		}
		LayoutUtil.refresh(this);
	}

	@Override
	public String toString() {
		if(getToolTip()==null) return super.toString();
		return "GapFigure " + ((Label)getToolTip()).getText();
	}

}
