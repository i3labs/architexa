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
package com.architexa.diagrams.commands;


import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.commands.Command;

public class BendpointCommand extends Command {

	protected int index;
	protected Point location;
	protected ArtifactRel rel;

	protected int getIndex() {
		return index;
	}

	protected Point getLocation() {
		return location;
	}

	protected ArtifactRel getRel() {
		return rel;
	}

	@Override
	public void redo() {
		execute();
	}

	public void setIndex(int i) {
		index = i;
	}

	public void setLocation(Point p) {
		location = p;
	}

	public void setRel(ArtifactRel rel) {
		this.rel = rel;
	}

}
