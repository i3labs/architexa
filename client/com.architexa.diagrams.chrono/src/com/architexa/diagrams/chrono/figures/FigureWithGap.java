/**
 * 
 */
package com.architexa.diagrams.chrono.figures;


/**
 * @author Abhishek Rakshit
 *
 */
public class FigureWithGap extends AbstractSeqFigure {

	GapFigure gap;
	boolean usingContainerGap = false;


	public void setGap(GapFigure gap) {
		this.gap = gap;
	}

	public GapFigure getGap() {
		return gap;
	}

	public void setIsUsingContainerGap(boolean using) {
		usingContainerGap = using;
	}

	public boolean isUsingContainerGap() {
		return usingContainerGap;
	}
}
