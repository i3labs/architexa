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
 * Created on Jun 27, 2004
 *
 */
package com.architexa.diagrams.relo.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import jiggle.Cell;


import com.architexa.diagrams.relo.figures.CodeUnitFigure;
import com.architexa.diagrams.relo.parts.AbstractReloEditPart;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.draw2d.graph.Node;


/**
 * @author vineet
 *  
 */
public class SVGExport {

    PrintStream f;
    
    public SVGExport(PrintStream f) {
        this.f = f;
    }
    
    public SVGExport(String fileName) throws FileNotFoundException {
        this.f = new PrintStream(new FileOutputStream(fileName));
    }
    
	public void dumpHeader() {
		f.println("<?xml version='1.0' encoding='ISO-8859-1' standalone='no'?>");
		f.println("<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 20010904//EN'");
		f.println("\t'http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd'>");
		f.println("<svg xmlns='http://www.w3.org/2000/svg'");
		f.println("\txmlns:xlink='http://www.w3.org/1999/xlink'");
		f.println("\twidth='18cm' height='18cm' viewBox='-5 -5 1010 1010'>");
		f.println("\t<style type='text/css'>");
		f.println("\t<![CDATA[");
		f.println("\trect {fill:none;stroke:red;stroke-width:.4}");
		f.println("\tline {fill:none;stroke:red;stroke-width:.4}");
		f.println("\tpath {fill:none;stroke:red;stroke-width:.4;stroke-dasharray:2,2}");
		f.println("\ttext {fill:blue;font-family:Verdana;font-size:9}");
		f.println("\t]]>");
		f.println("\t</style>");
		//f.println(
		//	"\t<rect x='0' y='0' width='700' height='700'
		// style='stroke-width:.4'/>");
	}

	public void dumpFooter() {
		f.println("</svg>");
	}

	public void dumpRectangle(String lbl, String comment, int x, int y, int width, int height) {
		f.println("");

		if (comment != null) {
			f.println("\t" + getSectionIndent() + "<!--" + comment + "-->");
		}

		f.println("\t" + getSectionIndent() + "<rect " + "x='" + x + "' y='"
				+ y + "' width='" + width + "' height='" + height + "'/>");

		if (lbl != null) {
			f.println("\t" + getSectionIndent() + "<text " + "x='" + (x + 2)
					+ "' y='" + (y + 10) + "'>" + lbl + "</text>");
		}

	}

	public void dumpRectangle(String lbl, String comment, Rectangle r) {
		dumpRectangle(lbl, comment, r.x, r.y, r.width, r.height);
	}

	public void dumpLine(int x1, int y1, int x2, int y2) {
		f.println("\t<line " + "x1='" + x1 + "' y1='" + y1 + "' x2='" + x2
				+ "' y2='" + y2 + "'/>");
	}

	public void dumpLine(Node n1, Node n2) {
		int x1 = n1.x + n1.height / 2;
		int y1 = n1.y + n1.width / 2;
		int x2 = n2.x + n2.height / 2;
		int y2 = n2.y + n2.width / 2;

		f.println("\t<line " + "x1='" + x1 + "' y1='" + y1 + "' x2='" + x2
				+ "' y2='" + y2 + "'/>");
	}

	public void dumpLine(Cell n1, Cell n2) {
	    int[] coords1 = n1.getCenter();
	    int[] coords2 = n2.getCenter();

		f.println("\t<line " + "x1='" + coords1[0] + "' y1='" + coords1[1] + "' x2='" + coords2[0]
				+ "' y2='" + coords2[1] + "'/>");
	}

	int sectionIndent = 0;

	public void beginSection() {
		sectionIndent++;
	}

	public void endSection() {
		sectionIndent--;
	}

	private String getSectionIndent() {
		String retVal = "";
		for (int i = 0; i < sectionIndent; i++) {
			retVal += "   ";
		}
		return retVal;
	}

    public void exportNode(Cell c, String hdr) {
        if (c.data instanceof AbstractReloEditPart) {
            AbstractReloEditPart vep = (AbstractReloEditPart) c.data;
            Object model = vep.getModel();
            Rectangle n = c.getBounds();
            dumpRectangle(
                hdr + " " + model.toString(),
                null,
                n.x,
                n.y,
                n.width,
                n.height);
        } else {
            //Rectangle n = c.getBounds();
            //dumpRectangle(
            //  hdr+" " + n.toString(),
            //    null,
            //  n.x,
            //  n.y,
            //  n.width,
            //  n.height);
        }
        //if (c.getParent() != null) {
        //  exportNode(f, n.getParent(), "");
        //}
    }

    public void exportFigure(IFigure fig) {
        beginSection();
        
        List lst = fig.getChildren();
        if (fig instanceof Label) {
            dumpRectangle(((Label)fig).getText(), fig.getClass().getName(), fig.getBounds());
        } else if (fig instanceof CodeUnitFigure) {
            //SVGExport.dumpRectangle(f, fig.toString(), fig.getClass().getName(), fig.getBounds());
        } else {
            dumpRectangle(null, fig.getClass().getName(), fig.getBounds());
        }
        if (lst.size() == 0) {} else {
            Iterator lstIt = lst.iterator();
            while (lstIt.hasNext()) {
                IFigure childFig = (IFigure) lstIt.next();
                exportFigure(childFig);
            }
        }

        endSection();
    }

}