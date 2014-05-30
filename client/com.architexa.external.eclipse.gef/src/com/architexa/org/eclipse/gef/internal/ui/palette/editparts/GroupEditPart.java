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

import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LayoutManager;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.gef.palette.PaletteContainer;
import com.architexa.org.eclipse.gef.ui.palette.PaletteViewerPreferences;



public class GroupEditPart 
	extends PaletteEditPart 
{

private int cachedLayout = -1;

public GroupEditPart(PaletteContainer group) {
	super(group);
}

public IFigure createFigure() {
	return new GroupFigure();
}

/**
 * @see com.architexa.org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
 */
protected void refreshVisuals() {
	int layout = getPreferenceSource().getLayoutSetting();
	if (cachedLayout == layout)
		return;
	cachedLayout = layout;
	LayoutManager manager;
	if (layout == PaletteViewerPreferences.LAYOUT_COLUMNS) {
		manager = new ColumnsLayout();
	} else if (layout == PaletteViewerPreferences.LAYOUT_ICONS) {
		FlowLayout flow = new FlowLayout();
		flow.setMajorSpacing(0);
		flow.setMinorSpacing(0);
		manager = flow;
	} else {
		manager = new ToolbarLayout();
	}
	getContentPane().setLayoutManager(manager);
}

}
