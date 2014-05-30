/******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 *    
 *    
 *    Modified from GMF original to allow stretching/aligning in both major and minor axis - SR
 ****************************************************************************/

package com.architexa.diagrams.strata.figures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.architexa.org.eclipse.draw2d.FlowLayout;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Insets;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;


/**
 * An extended toolbar layout that supports the following additional features:
 * 1- The ability to stretch the major axis
 * 2- The ability to reverse the children in layout
 * 3- The ability to ignore invisible children
 * 4- The ability to set ratio constraints on children (in major axis)
 * 
 * @author melaasar
 */
public class ConstrainedToolbarLayout extends ToolbarLayout {

	/**
	 * Whether to stretch the major axis
	 */
	private boolean stretchMajorAxis = true;
	/**
	 * Whether to reverse children for layout
	 */
	private boolean reversed = false;
	/**
	 * Whether to ignore invisible children
	 */
	private boolean ignoreInvisibleChildren = true;
	/**
	 * The constrains map
	 */
	private Map<IFigure, Object> constraints;

	/**
	 * Creates a new vertical ConstrainedToolbarLayout
	 */
	public ConstrainedToolbarLayout() {
		this(false);
	}

	/**
	 * Creates a new ConstrainedToolbarLayout with a given orientation
	 * 
	 * @param isHorizontal Whether the layout is horizontal
	 */
	public ConstrainedToolbarLayout(boolean isHorizontal) {
		super(isHorizontal);
		setStretchMinorAxis(true);
		setStretchMajorAxis(true);
		setMinorAlignment(ALIGN_CENTER);
		setMajorAlignment(ALIGN_CENTER);
	}

	/**
	 * @see org.eclipse.draw2d.AbstractLayout#calculatePreferredSize(org.eclipse.draw2d.IFigure, int, int)
	 */
	@Override
	protected Dimension calculatePreferredSize(
		IFigure container,
		int wHint,
		int hHint) {
		Insets insets = container.getInsets();
		if (!container.isVisible())
			return new Dimension(insets.getWidth(),insets.getHeight());
		if (isHorizontal()) {
			wHint = -1;
			if (hHint >= 0)
				hHint = Math.max(0, hHint - insets.getHeight());
		} else {
			hHint = -1;
			if (wHint >= 0)
				wHint = Math.max(0, wHint - insets.getWidth());
		}

		List<IFigure> children = getChildren(container);
		Dimension prefSize =
			calculateChildrenSize(children, wHint, hHint, true);
		// Do a second pass, if necessary
		if (wHint >= 0 && prefSize.width > wHint) {
			prefSize =
				calculateChildrenSize(children, prefSize.width, hHint, true);
		} else if (hHint >= 0 && prefSize.width > hHint) {
			prefSize =
				calculateChildrenSize(children, wHint, prefSize.width, true);
		}

		prefSize.height += Math.max(0, children.size() - 1) * spacing;
		return transposer
			.t(prefSize)
			.expand(insets.getWidth(), insets.getHeight())
			.union(getBorderPreferredSize(container));
	}

	/**
	 * @see org.eclipse.draw2d.AbstractHintLayout#calculateMinimumSize(org.eclipse.draw2d.IFigure, int, int)
	 */
	@Override
	public Dimension calculateMinimumSize(
		IFigure container,
		int wHint,
		int hHint) {
		Insets insets = container.getInsets();
		if (!container.isVisible())
			return new Dimension(insets.getWidth(),insets.getHeight());
		
		if (isHorizontal()) {
			wHint = -1;
			if (hHint >= 0)
				hHint = Math.max(0, hHint - insets.getHeight());
		} else {
			hHint = -1;
			if (wHint >= 0)
				wHint = Math.max(0, wHint - insets.getWidth());
		}

		List<IFigure> children = getChildren(container);
		Dimension minSize =
			calculateChildrenSize(children, wHint, hHint, false);
		// Do a second pass, if necessary
		if (wHint >= 0 && minSize.width > wHint) {
			minSize =
				calculateChildrenSize(children, minSize.width, hHint, false);
		} else if (hHint >= 0 && minSize.width > hHint) {
			minSize =
				calculateChildrenSize(children, wHint, minSize.width, false);
		}

		minSize.height += Math.max(0, children.size() - 1) * spacing;
		return transposer
			.t(minSize)
			.expand(insets.getWidth(), insets.getHeight())
			.union(getBorderPreferredSize(container));
	}

	/**
	 * @see org.eclipse.draw2d.LayoutManager#layout(IFigure)
	 */
	@Override
	public void layout(IFigure parent) {
		if (!parent.isVisible())
			return;
		List<IFigure> children = getChildren(parent);
		int numChildren = children.size();
		Rectangle clientArea = transposer.t(parent.getClientArea());
		int x = clientArea.x;
		int y = clientArea.y;
		int availableHeight = clientArea.height;

		Dimension prefSizes[] = new Dimension[numChildren];
		Dimension minSizes[] = new Dimension[numChildren];
		Dimension maxSizes[] = new Dimension[numChildren];

		// Calculate the width and height hints.  If it's a vertical ToolBarLayout,
		// then ignore the height hint (set it to -1); otherwise, ignore the 
		// width hint.  These hints will be passed to the children of the parent
		// figure when getting their preferred size. 
		int wHint = -1;
		int hHint = -1;
		if (isHorizontal()) {
			hHint = parent.getClientArea(Rectangle.SINGLETON).height;
		} else {
			wHint = parent.getClientArea(Rectangle.SINGLETON).width;
		}

		/*		
		 * Calculate sum of preferred heights of all children(totalHeight). 
		 * Calculate sum of minimum heights of all children(minHeight).
		 * Cache Preferred Sizes and Minimum Sizes of all children.
		 *
		 * totalHeight is the sum of the preferred heights of all children
		 * totalMinHeight is the sum of the minimum heights of all children
		 * prefMinSumHeight is the sum of the difference between all children's
		 * preferred heights and minimum heights. (This is used as a ratio to 
		 * calculate how much each child will shrink). 
		 */
		IFigure child;
		int totalHeight = 0;
		int totalMinHeight = 0;
		double totalMaxHeight = 0;
		int prefMinSumHeight = 0;
		double prefMaxSumHeight = 0;

		for (int i = 0; i < numChildren; i++) {
			child = children.get(i);

			prefSizes[i] = transposer.t(child.getPreferredSize(wHint, hHint));
			minSizes[i] = transposer.t(child.getMinimumSize(wHint, hHint));
			maxSizes[i] = transposer.t(child.getMaximumSize());

			if (getConstraint(child) != null) {
				double ratio = ((Double) getConstraint(child)).doubleValue();
				int prefHeight = (int) (ratio * availableHeight);
				prefHeight = Math.max(prefHeight, minSizes[i].height);
				prefHeight = Math.min(prefHeight, maxSizes[i].height);
				prefSizes[i].height = prefHeight;
			}

			totalHeight += prefSizes[i].height;
			totalMinHeight += minSizes[i].height;
			totalMaxHeight += maxSizes[i].height;
		}
		totalHeight += (numChildren - 1) * spacing;
		totalMinHeight += (numChildren - 1) * spacing;
		totalMaxHeight += (numChildren - 1) * spacing;
		prefMinSumHeight = totalHeight - totalMinHeight;
		prefMaxSumHeight = totalMaxHeight - totalHeight;

		/* 
		 * The total amount that the children must be shrunk is the 
		 * sum of the preferred Heights of the children minus  
		 * Max(the available area and the sum of the minimum heights of the children).
		 *
		 * amntShrinkHeight is the combined amount that the children must shrink
		 * amntShrinkCurrentHeight is the amount each child will shrink respectively  
		 */
		int amntShrinkHeight =
			totalHeight - Math.max(availableHeight, totalMinHeight);

		for (int i = 0; i < numChildren; i++) {
			int amntShrinkCurrentHeight = 0;
			int prefHeight = prefSizes[i].height;
			int minHeight = minSizes[i].height;
			int maxHeight = maxSizes[i].height;
			int prefWidth = prefSizes[i].width;
			int minWidth = minSizes[i].width;
			int maxWidth = maxSizes[i].width;
			Rectangle newBounds = new Rectangle(x, y, prefWidth, prefHeight);

			child = (IFigure) children.get(i);
			if (getStretchMajorAxis()) {
				if (amntShrinkHeight > 0 && prefMinSumHeight != 0)
                    amntShrinkCurrentHeight = (int) ((long) (prefHeight - minHeight)
                        * amntShrinkHeight / (prefMinSumHeight));
				else if (amntShrinkHeight < 0 && totalHeight != 0)
					amntShrinkCurrentHeight =
						(int) (((maxHeight - prefHeight) / prefMaxSumHeight)
							* amntShrinkHeight);
			}

			int width = Math.min(prefWidth, maxWidth);
			if (matchWidth)
				width = maxWidth;
			width = Math.max(minWidth, Math.min(clientArea.width, width));
			newBounds.width = width;

			int adjust = clientArea.width - width;
			switch (minorAlignment) {
				case ALIGN_TOPLEFT :
					adjust = 0;
					break;
				case ALIGN_CENTER :
					adjust /= 2;
					break;
				case ALIGN_BOTTOMRIGHT :
					break;
			}
			newBounds.x += adjust;
			if (newBounds.height - amntShrinkCurrentHeight > maxHeight)
				amntShrinkCurrentHeight = newBounds.height - maxHeight;
			newBounds.height -= amntShrinkCurrentHeight;
			child.setBounds(transposer.t(newBounds));

			amntShrinkHeight -= amntShrinkCurrentHeight;
			prefMinSumHeight -= (prefHeight - minHeight);
			prefMaxSumHeight -= (maxHeight - prefHeight);
			totalHeight -= prefHeight;
			y += newBounds.height + spacing;
		}
	}

	/**
	 * @see org.eclipse.draw2d.LayoutManager#getConstraint(org.eclipse.draw2d.IFigure)
	 */
	@Override
	public Object getConstraint(IFigure child) {
		if (constraints != null)
			return constraints.get(child);
		return null;
	}

	/**
	 * @see org.eclipse.draw2d.LayoutManager#setConstraint(org.eclipse.draw2d.IFigure, java.lang.Object)
	 */
	@Override
	public void setConstraint(IFigure child, Object constraint) {
		if (!(constraint instanceof Double))
			return;
		if (constraint instanceof Double) {
			Double c = (Double) constraint;
			super.setConstraint(child, constraint);
			if (constraints == null)
				constraints = new HashMap<IFigure, Object>();
			if (constraint == null || c.doubleValue() <= 0) {
				if (constraints.containsKey(child))
					constraints.remove(child);
			} else
				constraints.put(child, constraint);
		}
	}

	/**
	 * @see org.eclipse.draw2d.LayoutManager#remove(org.eclipse.draw2d.IFigure)
	 */
	@Override
	public void remove(IFigure child) {
		super.remove(child);
		setConstraint(child, null);
	}

	/**
	 * Sets whether to stretch the major axis or not
	 *  
	 * @param stretch Whether to stretch the major axis or not
	 */
	public void setStretchMajorAxis(boolean stretch) {
		stretchMajorAxis = stretch;
	}

	/**
	 * @return Whether the stretch major axis is on
	 */
	public boolean getStretchMajorAxis() {
		return stretchMajorAxis;
	}

	/**
	 * Sets whether to reverse children or not
	 * 
	 * @param reversed Whether to reverse children or not
	 */
	public void setReversed(boolean reversed) {
		this.reversed = reversed;
	}

	/**
	 * @return Whether the reverse children or not
	 */
	public boolean isReversed() {
		return reversed;
	}

	/**
	 * Sets whether to ignore invisible children or not
	 * 
	 * @param ignoreInvisibleChildren Whether to ignore invisible children or not
	 */
	public void setIgnoreInvisibleChildren(boolean ignoreInvisibleChildren) {
		this.ignoreInvisibleChildren = ignoreInvisibleChildren;
	}

	/**
	 * @return Whether to ignore invisible children or not
	 */
	public boolean getIgnoreInvisibleChildren() {
		return ignoreInvisibleChildren;
	}

	/**
	 * Calculates either the preferred or minimum children size
	 */
	private Dimension calculateChildrenSize(
		List<? extends IFigure> children,
		int wHint,
		int hHint,
		boolean preferred) {
		Dimension childSize;
		IFigure child;
		int height = 0, width = 0;
		for (int i = 0; i < children.size(); i++) {
			child = children.get(i);
			childSize =
				transposer.t(
					preferred
						? child.getPreferredSize(wHint, hHint)
						: child.getMinimumSize(wHint, hHint));
			height += childSize.height;
			width = Math.max(width, childSize.width);
		}
		return new Dimension(width, height);
	}

	/**
	 * Gets the list of children after applying the layout options of
	 * ignore invisible children & reverse children
	 */
	private List<IFigure> getChildren(IFigure container) {
		@SuppressWarnings("unchecked")
		ArrayList<IFigure> children = new ArrayList<IFigure>(container.getChildren());
		if (getIgnoreInvisibleChildren()) {
			Iterator<IFigure> iter = children.iterator();
			while (iter.hasNext()) {
				IFigure f = iter.next();
				if (!f.isVisible())
					iter.remove();
			}
		}
		if (isReversed())
			Collections.reverse(children);
		return children;
	}

	
	
	
	/** The alignment along the major axis. */
	protected int majorAlignment = FlowLayout.ALIGN_LEFTTOP;
	/** The alignment along the minor axis. */
	protected int minorAlignment = FlowLayout.ALIGN_LEFTTOP;
	/** The spacing along the minor axis. */
	protected int minorSpacing = 5;
	/** The spacing along the major axis. */
	protected int majorSpacing = 5;
	

/**
 * Sets the alignment for an entire row/column within the parent figure.
 * <P>
 * Possible values are :
 * <ul>
 *   <li>{@link #ALIGN_CENTER}
 * 	 <li>{@link #ALIGN_LEFTTOP}
 * 	 <li>{@link #ALIGN_RIGHTBOTTOM}
 * </ul>
 *
 * @param align the major alignment
 * @since 2.0
 */
public void setMajorAlignment(int align) {
	majorAlignment = align;
}

/**
 * Sets the spacing in pixels to be used between children in the direction parallel to the
 * layout's orientation.
 *
 * @param n the major spacing
 * @since 2.0
 */
public void setMajorSpacing(int n) {
	majorSpacing = n;
}

/**
 * Sets the alignment to be used within a row/column.
 * <P>
 * Possible values are :
 * <ul>
 *   <li>{@link #ALIGN_CENTER}
 * 	 <li>{@link #ALIGN_LEFTTOP}
 * 	 <li>{@link #ALIGN_RIGHTBOTTOM}
 * </ul>
 *
 * @param align the minor alignment
 * @since 2.0
 */
@Override
public void setMinorAlignment(int align) {
	minorAlignment = align;
}

/**
 * Sets the spacing to be used between children within a row/column.
 *
 * @param n the minor spacing
 * @since 2.0
 */
public void setMinorSpacing(int n) {
	minorSpacing = n;
}

/**
 * Returns the alignment used for an entire row/column.
 * <P>
 * Possible values are :
 * <ul>
 *   <li>{@link #ALIGN_CENTER}
 * 	 <li>{@link #ALIGN_LEFTTOP}
 * 	 <li>{@link #ALIGN_RIGHTBOTTOM}
 * </ul>
 *
 * @return the major alignment
 * @since 2.0
 */
public int getMajorAlignment() {
	return majorAlignment;
}

/**
 * Returns the spacing in pixels to be used between children in the direction parallel to
 * the layout's orientation.
 * @return the major spacing
 */
public int getMajorSpacing() {
	return majorSpacing;
}

/** 
 * Returns the alignment used for children within a row/column.
 * <P>
 * Possible values are :
 * <ul>
 *   <li>{@link #ALIGN_CENTER}
 * 	 <li>{@link #ALIGN_LEFTTOP}
 * 	 <li>{@link #ALIGN_RIGHTBOTTOM}
 * </ul>
 *
 * @return the minor alignment
 * @since 2.0
 */
@Override
public int getMinorAlignment() {
	return minorAlignment;
}

/**
 * Returns the spacing to be used between children within a row/column.
 * @return the minor spacing
 */
public int getMinorSpacing() {
	return minorSpacing;
}
}