package com.architexa.diagrams.parts;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Text;

import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.tools.CellEditorLocator;

public class AnnoLabelCellEditorLocator implements CellEditorLocator{
	 	protected IFigure fig;

	    public AnnoLabelCellEditorLocator(IFigure fig) {
	        this.fig = fig;
	    }

	    public void relocate(CellEditor celleditor) {
	    	if (celleditor == null) return;
	        Text text = (Text)celleditor.getControl();

	        Rectangle rect = fig.getClientArea(Rectangle.SINGLETON);
	        if (fig instanceof Label)
	            rect = ((Label)fig).getTextBounds().intersect(rect);
	        fig.translateToAbsolute(rect);

	        org.eclipse.swt.graphics.Rectangle trim = text.computeTrim(0, 0, 0, 0);
	        rect.translate(trim.x, trim.y);
	        rect.width += trim.width;
	        rect.height += trim.height;
	        
	        text.setBounds(rect.x, rect.y, rect.width, rect.height);
	    }
}
