/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */

/*
 * Created on Jun 13, 2004
 */
package com.architexa.diagrams.relo.figures;

import org.eclipse.swt.graphics.Image;

import com.architexa.diagrams.relo.graph.GraphLayoutManager;
import com.architexa.diagrams.relo.ui.ColorScheme;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;


/**
 * @author vineet
 *
 */
public class PackageFigure extends CodeUnitFigure implements SubgraphFigure {

	static class UMLPackageHeaderFigure extends Figure {
		public UMLPackageHeaderFigure(String name, String fullname, Image pkgImg) {
			ToolbarLayout layout = new ToolbarLayout(true);
			setLayoutManager(layout);

			Label pckgNameLbl = new Label(name, pkgImg);
			add(pckgNameLbl);
			add(new Label("  "));

			pckgNameLbl.setBorder(new PartialLineBorder(ColorScheme.packageBorder, 1, true, true, false, true));
			pckgNameLbl.setBackgroundColor(ColorScheme.packageColor);
			pckgNameLbl.setOpaque(true);
			pckgNameLbl.setToolTip(new Label(fullname));
		}
	}

	public PackageFigure(String name, String fullname, Image pkgImg) {
		super(new UMLPackageHeaderFigure(name, fullname, pkgImg), null, null, false /*isClass*/);

		IFigure contentFigure = this.getContentFig();
		//contentFigure.setBorder(new LineBorder(ColorScheme.packageBorder, 1));
		StackedBorder packageBodyBorder = new StackedBorder();
		packageBodyBorder.addBorder(new PartialLineBorder(ColorScheme.packageBorder, 1, false, true, true, true));
		packageBodyBorder.addBorder(new IncompleteSideLineBorder(
				ColorScheme.packageBorder, 1, 
				PositionConstants.NORTH, this.getLabel(), 
				PositionConstants.NORTH_WEST));
		contentFigure.setBorder(packageBodyBorder);
		contentFigure.setBackgroundColor(ColorScheme.packageColor);
		contentFigure.setOpaque(true);
		contentFigure.setLayoutManager(new GraphLayoutManager.SubgraphLayout());
	}

	/* (non-Javadoc)
	 * @see com.architexa.diagrams.relo.figures.SubgraphFigure#getHeader()
	 */
	public IFigure getHeader() {
		return this.getHeaderFig();
	}

	@Override
    public void setBounds(Rectangle rect) {
		super.setBounds(rect);

		Dimension hdrSize = null;
		hdrSize = getHeader().getPreferredSize();
		getHeader().setSize(hdrSize);
		getHeader().setLocation(rect.getLocation());

		//Dimension ftrSize = null;
		//ftrSize = getFooter().getPreferredSize();
		//getFooter().setSize(ftrSize);
		//getFooter().setLocation(rect.getBottomRight().translate(ftrSize.negate()));

		rect = Rectangle.SINGLETON;
		getClientArea(rect);
		rect.height -= hdrSize.height;
		rect.y += hdrSize.height;
		getContentPane().setBounds(rect);
	}

}