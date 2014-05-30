package com.architexa.diagrams.parts;


import com.architexa.diagrams.model.DirectedRel;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.EditPart;

public abstract class RelNavAidsSpec extends NavAidsSpec {

	private final DirectedRel rel;
	private final EditPart hostEP;

	public RelNavAidsSpec(EditPart hostEP, DirectedRel rel) {
		this.hostEP = hostEP;
		this.rel = rel;
	}

	public DirectedRel getRel() {
		return rel;
	}

	@Override
	public void buildHandles() {
        IFigure btn = getRelation(hostEP, rel);
        if (btn != null) decorationFig.add(btn);
	}

	@Override
	public abstract Point getHandlesPosition(IFigure containerFig);

}
