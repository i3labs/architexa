/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.parts;


import com.architexa.diagrams.strata.parts.AbstractConnectionEditPart2.SrcTgtNotificationEditPolicy;
import com.architexa.org.eclipse.draw2d.Connection;
import com.architexa.org.eclipse.draw2d.FigureListener;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.ConnectionEditPart;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.NodeListener;
import com.architexa.org.eclipse.gef.editparts.AbstractConnectionEditPart;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.editpolicies.AbstractEditPolicy;

// @tag move-into-sgef
public class AbstractConnectionEditPolicy extends AbstractEditPolicy implements SrcTgtNotificationEditPolicy {

	// helper methods
	
	protected AbstractConnectionEditPart getConnEP() {
		return (AbstractConnectionEditPart) this.getHost();
	}

	private AbstractGraphicalEditPart srcEP = null;
	private AbstractGraphicalEditPart tgtEP = null;

	protected AbstractGraphicalEditPart getSrcEP() {
		if (srcEP == null)
			srcEP = (AbstractGraphicalEditPart) getConnEP().getSource();
		return srcEP;
	}

	protected AbstractGraphicalEditPart getTgtEP() {
		if (tgtEP == null)
			tgtEP = (AbstractGraphicalEditPart) getConnEP().getTarget();
		return tgtEP;
	}

	public void setSource(EditPart ep) {
		srcEP = (AbstractGraphicalEditPart) ep;
	}

	public void setTarget(EditPart ep) {
		tgtEP = (AbstractGraphicalEditPart) ep;
	}

	protected IFigure getSrcFig() {
		if (getSrcEP() == null) 
			return null;
		else
			return getSrcEP().getFigure();
	}

	protected IFigure getTgtFig() {
		if (getTgtEP() == null) 
			return null;
		else
			return getTgtEP().getFigure();
	}

	protected Connection getConnFig() {
		return getConnEP().getConnectionFigure();
	}

	// this are called after both the src and the tgt EP's are set (not a
	// guarantee by activate)
	// maybe called multiple times, but only after clean is called
	public void init() {
		if (getSrcEP() != null)
			getSrcEP().addNodeListener(initNodeListener);
		if (getTgtEP() != null)
			getTgtEP().addNodeListener(initNodeListener);
		initialized = true;
	}

	// really called when deactivate is called
	public void clean() {
		if (getSrcEP() != null)
			getSrcEP().removeNodeListener(initNodeListener);
		if (getTgtEP() != null)
			getTgtEP().removeNodeListener(initNodeListener);
		srcEP = null;
		tgtEP = null;
		initialized = false;
	}
	
	// init comes from when the figure moves (as will also happen when connected to a new node)
	// clean comes from removingConnection being fired
	private boolean initialized = false;
	public boolean isIniitalized() {
		return initialized;
	}
	private FigureListener connFigMoveListener = new FigureListener() {
		public void figureMoved(IFigure source) {
			if (initialized) return;
			if (getSrcEP() == null) return;
			if (getTgtEP() == null) return;
			init();
		}
	};
	private NodeListener initNodeListener = new NodeListener() {
		public void removingSourceConnection(ConnectionEditPart connection, int index) {
			removingConnection(connection);
		}
		public void removingTargetConnection(ConnectionEditPart connection, int index) {
			removingConnection(connection);
		}
		public void sourceConnectionAdded(ConnectionEditPart connection, int index) {}
		public void targetConnectionAdded(ConnectionEditPart connection, int index) {}
		private void removingConnection(ConnectionEditPart connection) {
			if (connection == AbstractConnectionEditPolicy.this.getConnEP() && initialized) {
				clean();
			}
		}
	};

	@Override
	public void activate() {
		super.activate();
		this.getConnFig().addFigureListener(connFigMoveListener);
	}

	@Override
	public void deactivate() {
		super.deactivate();
		this.getConnFig().removeFigureListener(connFigMoveListener);
		if (initialized) {
			clean();
		}
	}
}
