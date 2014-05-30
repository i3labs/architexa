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
package com.architexa.org.eclipse.gef.ui.palette;

import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.palette.PaletteEntry;
import com.architexa.org.eclipse.gef.palette.PaletteRoot;
import com.architexa.org.eclipse.gef.ui.palette.customize.PaletteCustomizerDialog;

import java.util.List;

import org.eclipse.jface.action.Action;


/**
 * This action launches the PaletteCustomizerDialog for the given palette.
 * 
 * @author Pratik Shah
 */
public class CustomizeAction extends Action {

private PaletteViewer paletteViewer;

/**
 * Constructor
 * 
 * @param	palette		the palette which has to be customized when this action is run
 */
public CustomizeAction(PaletteViewer palette) {
	super();
	setText(PaletteMessages.MENU_OPEN_CUSTOMIZE_DIALOG);
	paletteViewer = palette;
}

/**
 * Opens the Customizer Dialog for the palette
 * 
 * @see org.eclipse.jface.action.IAction#run()
 */
public void run() {
	PaletteCustomizerDialog dialog = paletteViewer.getCustomizerDialog();
	List list = paletteViewer.getSelectedEditParts();
	if (!list.isEmpty()) {
		PaletteEntry selection = (PaletteEntry)((EditPart)list.get(0)).getModel();
		if (!(selection instanceof PaletteRoot)) {
			dialog.setDefaultSelection(selection);
		}
	}
	dialog.open();
}

}
