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
package com.architexa.org.eclipse.gef.ui.palette.customize;

import com.architexa.org.eclipse.gef.palette.PaletteEntry;
import com.architexa.org.eclipse.gef.palette.PaletteGroup;
import com.architexa.org.eclipse.gef.ui.palette.PaletteMessages;

import org.eclipse.swt.widgets.Shell;


/**
 * Factory to create {@link com.architexa.org.eclipse.gef.palette.PaletteGroup groups}
 * 
 * @author Pratik Shah
 */
public class PaletteGroupFactory 
	extends PaletteContainerFactory
{
	
/**
 * Constructor
 */
public PaletteGroupFactory() {
	setLabel(PaletteMessages.MODEL_TYPE_GROUP);
}

/**
 * @see com.architexa.org.eclipse.gef.ui.palette.customize.PaletteEntryFactory#createNewEntry(Shell)
 */
protected PaletteEntry createNewEntry(Shell shell) {
	PaletteGroup group = new PaletteGroup(PaletteMessages.NEW_GROUP_LABEL);
	group.setUserModificationPermission(PaletteEntry.PERMISSION_FULL_MODIFICATION);
	return group;
}

}
