package com.architexa.diagrams.relo.jdt.parts;

import org.eclipse.swt.graphics.Color;

import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.PolygonDecoration;
import com.architexa.org.eclipse.draw2d.RoundedPolylineConnection;


public class OverridesRelationPart extends InheritanceRelationPart {

	@Override
    protected IFigure createFigure() {
		// mostly copied from InheritanceRelationPart

		RoundedPolylineConnection conn = new RoundedPolylineConnection();
		conn.setLineStyle(Graphics.LINE_DASHDOT);
		conn.setConnectionRouter(new InheritanceConnectionRouter(15));
		
		PolygonDecoration dec = new PolygonDecoration();
		dec.setScale(7, 5);
		dec.setBackgroundColor(new Color(null, 255, 255, 255));
		conn.setTargetDecoration(dec);
		conn.setToolTip(new Label(" " + getRelationLabel() + " "));
		return conn;
	}

}
