package com.architexa.diagrams.draw2d;

import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;

/**
 * An interface used on figures to so that they can tell which of their
 * potentially many children is the main content (note that this can be
 * considered an edit part responsibility and eventually might be best at home
 * their)
 * 
 * @author vineet
 */
public interface IFigureWithContents {
	
	public IFigure getContentFig();
	
	public class Impl extends Figure implements IFigureWithContents {
		public IFigure contentFig;
		
		public Impl() {
			// by default we point to ourselves as the content figure
			contentFig = this;
		}

		public IFigure getContentFig() {
			return contentFig;
		}

		public void addContentFig(IFigure givenFig) {
			contentFig = givenFig;
			add(givenFig);
		}
	}
}
