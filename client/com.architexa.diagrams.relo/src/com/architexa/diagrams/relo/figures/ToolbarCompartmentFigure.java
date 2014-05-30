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

import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;

/**
 * @author vineet
 * 
 * Essentially a figure grouping with a line on top
 *  
 * Based on CompartmentFigure from 
 * http://www.eclipse.org/articles/Article-GEF-Draw2d/GEF-Draw2d.html
 */
public class ToolbarCompartmentFigure extends Figure {

	public ToolbarCompartmentFigure() {
	    this(null);
	}
	
	public ToolbarCompartmentFigure(IFigure defaultFig) {
		ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		//layout.setStretchMinorAxis(false);
		//layout.setSpacing(2);
		setLayoutManager(layout);
		//setBorder(new PartialLineBorder(true, false, false, false));
		
		if (defaultFig != null) {
		    add(defaultFig);
		}
	}
	

}