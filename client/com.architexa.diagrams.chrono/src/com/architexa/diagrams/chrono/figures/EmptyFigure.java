package com.architexa.diagrams.chrono.figures;


import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.org.eclipse.draw2d.ColorConstants;



/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class EmptyFigure extends GapFigure {

	private int correctY = 0;
	private String name;

	public EmptyFigure(String name) {
		super("empty");
		this.name = name;

		if(SeqUtil.debugHighlightingOn) {
			setBackgroundColor(ColorConstants.red);
			setOpaque(true);
		}
	}

	public int getCorrectY() {
		return correctY;
	}

	public void setCorrectY(int y) {
		correctY = y;
	}

	public String getName() {
		return name;
	}

}
