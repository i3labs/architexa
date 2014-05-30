/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.architexa.org.eclipse.draw2d.widgets;

import com.architexa.org.eclipse.draw2d.AbstractBorder;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Insets;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.swt.graphics.Image;


/**
 * @author Pratik Shah
 */
class ImageBorder 
	extends AbstractBorder 
{

/*
 * @TODO:Pratik	Need to test this class extensively
 * @TODO Test inside compound borders
 */

private Insets imgInsets;
private Image image;
private Dimension imageSize;

public ImageBorder(Image image) {
	setImage(image);
}

public Insets getInsets(IFigure figure) {
	return imgInsets;
}

public Image getImage() {
	return image;
}

/**
 * @see com.architexa.org.eclipse.draw2d.AbstractBorder#getPreferredSize(com.architexa.org.eclipse.draw2d.IFigure)
 */
public Dimension getPreferredSize(IFigure f) {
	return imageSize;
}

public void paint(IFigure figure, Graphics graphics, Insets insets) {
	if (image == null)
		return;
	Rectangle rect = getPaintRectangle(figure, insets);
	int x = rect.x;
	int y = rect.y + (rect.height - imageSize.height) / 2;
	graphics.drawImage(getImage(), x, y);
}

public void setImage(Image img) {
	image = img;
	imageSize = new Dimension(image);
	imgInsets = new Insets();
	imgInsets.left = imageSize.width;
}

}
