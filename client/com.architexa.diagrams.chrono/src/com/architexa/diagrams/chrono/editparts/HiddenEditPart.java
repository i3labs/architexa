package com.architexa.diagrams.chrono.editparts;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import com.architexa.diagrams.chrono.figures.HiddenFigure;
import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.org.eclipse.draw2d.IFigure;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class HiddenEditPart extends SeqNodeEditPart {

	@Override
	protected IFigure createFigure() {
		HiddenNodeModel model = (HiddenNodeModel)getModel();
		HiddenFigure fig = new HiddenFigure(model.getVisible(), model.getTooltip());
		model.setFigure(fig);
		return fig;
	}

	@Override
	public IAction getOpenInJavaEditorAction(String actionName, ImageDescriptor image) {
		return null;
	}

}
