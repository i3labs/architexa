package com.architexa.diagrams.chrono.figures;

import com.architexa.org.eclipse.draw2d.ChopboxAnchor;
import com.architexa.org.eclipse.draw2d.ConnectionAnchor;
import com.architexa.org.eclipse.draw2d.Figure;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public abstract class AbstractSeqFigure extends Figure {

	public static int MEMBER_GAP = 40;
	public static int METHOD_BOX_GAP = 40;
	public static int FIELD_GAP = 40;

	protected ConnectionAnchor anchor;

	public AbstractSeqFigure() {
		super();
		anchor = new ChopboxAnchor(this);
	}

	public ConnectionAnchor getConnectionAnchor() {
		return anchor;
	}

}