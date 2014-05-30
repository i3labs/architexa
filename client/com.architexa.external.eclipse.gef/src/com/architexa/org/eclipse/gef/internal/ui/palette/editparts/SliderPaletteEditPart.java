/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.architexa.org.eclipse.gef.internal.ui.palette.editparts;

import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.gef.palette.PaletteRoot;
import com.architexa.org.eclipse.gef.ui.palette.PaletteViewer;



public class SliderPaletteEditPart 
	extends PaletteEditPart
{

private PaletteAnimator controller;

public SliderPaletteEditPart(PaletteRoot paletteRoot) {
	super(paletteRoot);
}

public IFigure createFigure() {
	Figure figure = new Figure();
	figure.setOpaque(true);
	figure.setForegroundColor(ColorConstants.listForeground);
	figure.setBackgroundColor(ColorConstants.button);
	return figure;
}

/**
 * This method overrides super's functionality to do nothing.
 * 
 * @see PaletteEditPart#refreshVisuals()
 */
protected void refreshVisuals() {
}

/**
 * @see com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart#registerVisuals()
 */
protected void registerVisuals() {
	super.registerVisuals();
	controller = new PaletteAnimator(
		((PaletteViewer)getViewer()).getPaletteViewerPreferences());
	getViewer().getEditPartRegistry().put(PaletteAnimator.class, controller);
	ToolbarLayout layout = new PaletteToolbarLayout();
	getFigure().setLayoutManager(layout);
	getFigure().addLayoutListener(controller);
}

}
