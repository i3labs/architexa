/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.architexa.diagrams.figures;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.MarginBorder;
import com.architexa.org.eclipse.draw2d.StackLayout;
import com.architexa.org.eclipse.draw2d.examples.BentCornerFigure;
import com.architexa.org.eclipse.draw2d.geometry.PointList;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.draw2d.text.FlowPage;
import com.architexa.org.eclipse.draw2d.text.ParagraphTextLayout;
import com.architexa.org.eclipse.draw2d.text.TextFlow;


/**
 * A Figure with a bent corner and an embedded TextFlow within a FlowPage that contains
 * text.
 */
public class NoteFigure extends BentCornerFigure implements CommentFigure {

	/** The inner TextFlow **/
	private TextFlow textFlow;
	private Color begColor = new Color(getBackgroundColor().getDevice(), new RGB(250, 255, 189));
	private Color endColor = new Color(getBackgroundColor().getDevice(), new RGB(252, 252, 240));

	/**
	 *  Creates a new StickyNoteFigure with a default MarginBorder size of DEFAULT_CORNER_SIZE
	 *  - 3 and a FlowPage containing a TextFlow with the style WORD_WRAP_SOFT.
	 */
	public NoteFigure() {
		this(BentCornerFigure.DEFAULT_CORNER_SIZE);
	}

	/** 
	 * Creates a new StickyNoteFigure with a MarginBorder that is the given size and a
	 * FlowPage containing a TextFlow with the style WORD_WRAP_SOFT.
	 * 
	 * @param borderSize the size of the MarginBorder
	 */
	public NoteFigure(int borderSize) {
		setBorder(new MarginBorder(borderSize));
		FlowPage flowPage = new FlowPage();
		textFlow = new TextFlow();

		textFlow.setLayoutManager(new ParagraphTextLayout(textFlow, ParagraphTextLayout.WORD_WRAP_TRUNCATE));

		flowPage.add(textFlow);
		setLayoutManager(new StackLayout());
		add(flowPage);
	}


	@Override
	public void paintFigure(Graphics graphics) {
		Rectangle rect = getBounds().getCopy();

		Rectangle rect2 = rect.getCopy();
		rect2.y = rect.y+getCornerSize();

		graphics.setForegroundColor(begColor);
		graphics.setBackgroundColor(endColor);
		graphics.fillGradient(rect2, true);

		rect2 = new Rectangle(rect.x, rect.y, rect.width, getCornerSize());


		graphics.translate(getLocation());

		graphics.setBackgroundColor(begColor);
		graphics.setForegroundColor(begColor);
		PointList outline = new PointList();
		outline.addPoint(0,0);
		outline.addPoint(rect.width - getCornerSize(), 0);
		outline.addPoint(rect.width, getCornerSize());
		outline.addPoint(0, getCornerSize());

		graphics.fillPolygon(outline); 


		// draw the inner outline
		graphics.setForegroundColor(ColorConstants.black);
		PointList innerLine = new PointList();
		innerLine.addPoint(rect.width - getCornerSize() - 1, 0);
		innerLine.addPoint(rect.width - getCornerSize() - 1, getCornerSize());
		innerLine.addPoint(rect.width - 1, getCornerSize());
		innerLine.addPoint(rect.width - getCornerSize() - 1, 0);
		innerLine.addPoint(0, 0);
		innerLine.addPoint(0, rect.height - 1);
		innerLine.addPoint(rect.width - 1, rect.height - 1);
		innerLine.addPoint(rect.width - 1, getCornerSize());
		graphics.drawPolygon(innerLine);

		graphics.translate(getLocation().getNegated());
	}

	/**
	 * Returns the text inside the TextFlow.
	 * 
	 * @return the text flow inside the text.
	 */
	public String getText() {
		return textFlow.getText();
	}

	/**
	 * Sets the text of the TextFlow to the given value.
	 * 
	 * @param newText the new text value.
	 */
	public void setText(String newText) {
		textFlow.setText(newText);
	}

}