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
package com.architexa.org.eclipse.gef;

import com.architexa.org.eclipse.gef.commands.CommandStack;
import com.architexa.org.eclipse.gef.internal.InternalGEFPlugin;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.properties.IPropertySheetEntry;


/**
 * @deprecated The GEF plugin class must not be referenced by clients.
 */
public final class GEFPlugin
	extends AbstractUIPlugin
{

private static GEFPlugin singleton;

/**
 * This method will be deleted.
 * @deprecated use org.eclipse.gef.ui.properties.UndoablePropertySheetEntry
 * @param stack a command stack
 * @return the implementation for the entry
 */
public static IPropertySheetEntry createUndoablePropertySheetEntry(CommandStack stack) {
	return new com.architexa.org.eclipse.gef.ui.properties.UndoablePropertySheetEntry(stack);
}

/**
 * Gets the plugin singleton.
 *
 * @return the default GEFPlugin singleton
 */
public static GEFPlugin getDefault() {
	if (singleton == null)
		singleton = new GEFPlugin();
	return singleton;
}

GEFPlugin() {
	try {
		start(InternalGEFPlugin.getContext());
	} catch (Exception exc) {
		throw new RuntimeException(exc);
	}
}


}
