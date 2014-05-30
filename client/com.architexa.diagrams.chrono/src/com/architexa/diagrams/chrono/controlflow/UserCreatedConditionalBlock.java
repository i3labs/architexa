package com.architexa.diagrams.chrono.controlflow;

import java.util.List;

import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

public class UserCreatedConditionalBlock extends ControlFlowBlock {

	private List<MemberModel> containedStatements;

	public UserCreatedConditionalBlock(String condition, DiagramModel diagram) {
		super(condition, diagram);
		setType("", "");
		add(getConditionLabel());
	}

	@Override
	public List<MemberModel> getOrigStatements() {
		return containedStatements;
	}

	@Override
	public List<MemberModel> getStatements() {
		return containedStatements;
	}

	@Override
	public void figureMoved(IFigure source) {
		
		IFigure topLeftFig = getTopLeftFigureListeningTo();
		IFigure bottomMostFig = getBottomFigureListeningTo();
		IFigure rightMostFig = getRightFigureListeningTo();
		
		
		if(topLeftFig==null || bottomMostFig==null || rightMostFig==null) return;

		int typeLabelWidth = getTypeLabel().getSize().width;
		Point topLeft = new Point(topLeftFig.getBounds().getTopLeft().x - MARGIN - typeLabelWidth/2, topLeftFig.getBounds().getTop().y - getTypeLabel().getSize().height - getConditionLabel().getSize().height/2 + DiagramModel.TOP_MARGIN);
		Point bottomRight = new Point(rightMostFig.getBounds().getRight().x + MARGIN + typeLabelWidth/2, bottomMostFig.getBounds().getBottom().y + MARGIN + DiagramModel.TOP_MARGIN);

		removeAnyOverlap(topLeft, bottomRight);

		Rectangle boundsRect = new Rectangle(topLeft, bottomRight);
		setBounds(boundsRect);

		updateOuterBlocks(source);
//		updateLoopStmts(source);
	}

	public void setStmts(List<MemberModel> statements) {
		containedStatements = statements;
	}

}
