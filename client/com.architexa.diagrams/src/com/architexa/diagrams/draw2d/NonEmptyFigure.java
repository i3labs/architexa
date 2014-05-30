/**
 * 
 */
package com.architexa.diagrams.draw2d;

import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;

public class NonEmptyFigure extends Figure {
	private IFigure emptyContent = null;

	public NonEmptyFigure(IFigure _emptyContent) {
		this.setLayoutManager(new ToolbarLayout());
		emptyContent = _emptyContent;
		add(emptyContent);
	}
	// These are called by the listeners in CompartmentedCodeUnitEditPart 
	public void figureAdded() {
		if (this.getChildren().contains(emptyContent))
			this.remove(emptyContent);
	}
	public void figureRemoved() {
		if (!this.getChildren().contains(emptyContent))
			this.add(emptyContent);
	}
}