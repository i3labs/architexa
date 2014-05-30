package com.architexa.diagrams.chrono.controlflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.chrono.controlflow.IfBlock.ElseBlock;
import com.architexa.diagrams.chrono.figures.AbstractSeqFigure;
import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.FigureListener;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.LineBorder;
import com.architexa.org.eclipse.draw2d.MarginBorder;
import com.architexa.org.eclipse.draw2d.Polyline;
import com.architexa.org.eclipse.draw2d.RoundedRectangle;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public abstract class ControlFlowBlock extends AbstractSeqFigure implements FigureListener {

	protected DiagramModel diagram;
	private ControlFlowModel model;

	private String condition = "";
	private List<ControlFlowBlock> innerConditionalBlocks = new ArrayList<ControlFlowBlock>();
	private ControlFlowBlock outerConditionalBlock = null;
	private boolean outerIsCollapsed = false;

	private ControlFlowTypeLabel typeLabel = null;
	public Label conditionLabel = null;

	public static int MARGIN = 15;
	public static int labelMargin = 3;

	Highlight highlight;
	public static int redValue = ColorScheme.controlFlowHighlightBase.getRed();
	public static int greenValue = ColorScheme.controlFlowHighlightBase.getGreen();
	public static int blueValue = ColorScheme.controlFlowHighlightBase.getBlue();
	public static int redDecrement = 5;
	public static int greenDecrement = 15;
	public static int blueDecrement = 25;

	List<CollapseExpandButton> collapseExpandButtons = new ArrayList<CollapseExpandButton>();
	public static String SHOWING = "-";
	public static String HIDING = "+";
	Map<CollapseExpandButton, List<ControlFlowBlock>> buttonToContainedInnerBlocks = new HashMap<CollapseExpandButton, List<ControlFlowBlock>>(); 

	IFigure topLeftFigureListeningTo;
	IFigure bottomFigureListeningTo;
	IFigure rightFigureListeningTo;

	public ControlFlowBlock(String condition, DiagramModel diagram) {
		setDiagram(diagram);
		setCondition(condition);

		setOpaque(false);
		if(SeqUtil.debugHighlightingOn) setBorder(new LineBorder(ColorConstants.green));
		setLayoutManager(new ToolbarLayout(true));

		setHighlight(new Highlight());
		addFigureListener(getHighlight());
	}

	public abstract List<MemberModel> getOrigStatements();
	
	public void setModel(ControlFlowModel model){
		this.model = model;
	}

	public ControlFlowModel getModel(){
		return model;
	}

	public void setDiagram(DiagramModel diagram) {
		this.diagram = diagram;
	}

	public DiagramModel getDiagram() {
		return diagram;
	}

	public void setCondition(String condition) {
		this.condition = condition;
		conditionLabel = new Label("[ "+condition+" ]") {
			@Override
			public void setBounds(Rectangle rect) {
				Rectangle oldBounds = new Rectangle(conditionLabel.getBounds());
				super.setBounds(rect);
				labelBoundsSet(oldBounds, rect);
			}
		};
		conditionLabel.setBorder(new MarginBorder(labelMargin));
		conditionLabel.setBackgroundColor(ColorScheme.controlFlowTypeLabelBackground);
		conditionLabel.setOpaque(true);
	}

	public String getCondition() {
		return condition;
	}

	public Label getConditionLabel() {
		return conditionLabel;
	}

	public void setType(String type, String condition) {
		typeLabel = new ControlFlowTypeLabel(type, condition) {
			@Override
			public void setBounds(Rectangle rect) {
				Rectangle oldBounds = new Rectangle(typeLabel.getBounds());
				super.setBounds(rect);
				labelBoundsSet(oldBounds, rect);
			}
		};
		typeLabel.setOpaque(true);
	}

	public ControlFlowTypeLabel getTypeLabel() {
		return typeLabel;
	}

	public abstract List<MemberModel> getStatements();

	public void addInnerConditionalBlock(ControlFlowBlock block) {
		innerConditionalBlocks.add(block);
	}
	
	
	public List<ControlFlowBlock> getInnerConditionalBlocks() {
		List<ControlFlowBlock> innerBlocks = new ArrayList<ControlFlowBlock>();
		for(ControlFlowBlock innerBlock : innerConditionalBlocks) {
			innerBlocks.add(innerBlock);
		}
		return innerBlocks;
	}

	public void setOuterConditionalBlock(ControlFlowBlock block) {
		outerConditionalBlock = block;
	}

	public ControlFlowBlock getOuterConditionalBlock() {
		return outerConditionalBlock;
	}

	public void updateOuterBlocks(IFigure source) {
		if(outerConditionalBlock!=null) outerConditionalBlock.figureMoved(source);
	}

	public void setHighlight(Highlight highlight) {
		this.highlight = highlight;
	}

	public Highlight getHighlight() {
		return highlight;
	}

	public void addCollapseExpandButton(CollapseExpandButton button) {
		collapseExpandButtons.add(button);
	}

	public List<CollapseExpandButton> getCollapseExpandButtons() {
		return collapseExpandButtons;
	}

	public CollapseExpandButton getThenBlockCollapseExpandButton() {
		if (collapseExpandButtons.size()<=0) return null;
		return collapseExpandButtons.get(0);
	}

	public void removeAllCollapseExpandButtons(){
		collapseExpandButtons.clear();
	}

	public void setTopLeftFigureListeningTo(IFigure topLeftFigureListeningTo) {
		if(getTopLeftFigureListeningTo()!=null) 
			getTopLeftFigureListeningTo().removeFigureListener(this);

		if(topLeftFigureListeningTo!=null)
			topLeftFigureListeningTo.addFigureListener(this);

		this.topLeftFigureListeningTo = topLeftFigureListeningTo;
	}

	public IFigure getTopLeftFigureListeningTo() {
		return topLeftFigureListeningTo;
	}

	public void setBottomFigureListeningTo(IFigure bottomFigure) {
		if(getBottomFigureListeningTo()!=null)
			getBottomFigureListeningTo().removeFigureListener(this);

		if(bottomFigure!=null) 
			bottomFigure.addFigureListener(this);

		this.bottomFigureListeningTo = bottomFigure;
	}

	public IFigure getBottomFigureListeningTo() {
		return bottomFigureListeningTo;
	}

	public void setRightFigureListeningTo(IFigure rightFigure) {
		if(getRightFigureListeningTo()!=null)
			getRightFigureListeningTo().removeFigureListener(this);

		if(rightFigure!=null)
			rightFigure.addFigureListener(this);

		this.rightFigureListeningTo = rightFigure;
	}

	public IFigure getRightFigureListeningTo() {
		return rightFigureListeningTo;
	}

	public void delete() {
		getDiagram().removeChildFromConditionalLayer(getModel());
//		firePropertyChange(ControlFlowModel.PROPERTY_DELETE, null, null);
	}

	public abstract void figureMoved(IFigure source);

	protected void removeAnyOverlap(Point topLeft, Point bottomRight) {
		removeInnerConditionalOverlap(topLeft, bottomRight);
		removeNoConnectionsLabelOverlap(topLeft);
	}

	private void removeInnerConditionalOverlap(Point topLeft, Point bottomRight) {
		if(getInnerConditionalBlocks().size()==0) return;

		ControlFlowBlock topMostInnerBlock=null;
		for(ControlFlowBlock innerBlock : getInnerConditionalBlocks()) {
			if(topMostInnerBlock==null || 
					innerBlock.getBounds().y < topMostInnerBlock.getBounds().y) 
				topMostInnerBlock = innerBlock;
		}
		Rectangle innerBlockBounds = topMostInnerBlock.getBounds().getCopy();
		if(topLeft.y >= innerBlockBounds.y) {
			topLeft.y = innerBlockBounds.y - getConditionLabel().getBounds().getCopy().height;
		} else if(getConditionLabel().getBounds().getCopy().intersects(
				topMostInnerBlock.getConditionLabel().getBounds().getCopy())) {
			topLeft.y = topLeft.y - (innerBlockBounds.y - topLeft.y);
		}

		if(bottomRight.y <= innerBlockBounds.getBottom().y || bottomRight.y-innerBlockBounds.getBottom().y<labelMargin) {
			bottomRight.y = innerBlockBounds.getBottom().y + labelMargin;
		}

		if(topLeft.x >= innerBlockBounds.x || innerBlockBounds.x-topLeft.x<labelMargin) {
			topLeft.x = innerBlockBounds.x - labelMargin;
		}

		if(bottomRight.x <= innerBlockBounds.getRight().x || bottomRight.x-innerBlockBounds.getRight().x<labelMargin) {
			bottomRight.x = innerBlockBounds.getRight().x + labelMargin;
		}
	}

	private void removeNoConnectionsLabelOverlap(Point topLeft) {
		if(getTopMostFigure()==null || getTopMostFigure().getParent()==null || !(getTopMostFigure().getParent().getParent() instanceof MethodBoxFigure)) return;

		Label noConnectionsBoxLabel = ((MethodBoxFigure)getTopMostFigure().getParent().getParent()).getNoConnectionsBoxLabel();
		Rectangle noConnectionsLabelBounds = noConnectionsBoxLabel.getBounds().getCopy();

		Point topLeftCopy = new Point(topLeft);
		noConnectionsBoxLabel.translateToRelative(topLeftCopy);

		Point conditionLabelLoc = new Point(topLeftCopy.x + getTypeLabel().getBounds().width + labelMargin, topLeftCopy.y + labelMargin);
		Rectangle conditionLabelBounds = new Rectangle(conditionLabelLoc, new Dimension(getConditionLabel().getBounds().width, getConditionLabel().getBounds().height));

		if(noConnectionsLabelBounds.intersects(conditionLabelBounds)) {
			int differenceY = noConnectionsLabelBounds.y-conditionLabelBounds.y;
			int raiseY = differenceY>0 ? differenceY : differenceY+conditionLabelBounds.height;
			topLeft.y = topLeft.y - raiseY;
			if(noConnectionsLabelBounds.x < topLeftCopy.x) {
				int shiftX = topLeftCopy.x - noConnectionsLabelBounds.x;
				topLeft.x = topLeft.x - shiftX - 1;
			}
		}
	}

	public void statementDeleted(NodeModel deleted){
		updateBlock();
	}

	public void statementAdded(MemberModel model){
		updateBlock();
	}

	public void updateBlock() {
		setTopLeftFigureListeningTo(getTopMostFigure());
		setBottomFigureListeningTo(getBottomMostFigure());
		setRightFigureListeningTo(getRightMostFigure());
		figureMoved(null);
	}

	public ArtifactFragment getTopMostModel() {
		if(getStatements().size()==0) return null;

		MemberModel firstStmt =  getStatements().get(0);
		if(firstStmt.getFigure()!= null && firstStmt.getFigure().getParent()!=null) return firstStmt;

		CollapseExpandButton button = getCorrespondingCollapseExpandButton(firstStmt);
		if(button != null) return button.getMemberToHiddenMap().get(firstStmt);
		return null;
	}

	public ArtifactFragment getBottomMostModel() {
		if(getStatements().size()==0) return null;

		MemberModel lastStmt =  getStatements().get(getStatements().size()-1);
		if(lastStmt.getFigure()!= null && lastStmt.getFigure().getParent()!=null) return lastStmt;

		CollapseExpandButton button = getCorrespondingCollapseExpandButton(lastStmt);
		if(button != null) return button.getMemberToHiddenMap().get(lastStmt);
		return null;
	}

	public ArtifactFragment getRightMostModel() {
		MemberModel rightMostMethod = getRightMostMethodInBlock();
		if(rightMostMethod!=null) return rightMostMethod;

		if(getStatements().size()>0) {
			MemberModel firstStmt =  getStatements().get(0);
			CollapseExpandButton button = getCorrespondingCollapseExpandButton(firstStmt);
			if(button != null) return button.getHiddenChildList().get(0);
		}

		if(getCollapseExpandButtons().size()>0 && !getCollapseExpandButtons().get(0).getHiddenChildList().isEmpty()) {
			return getCollapseExpandButtons().get(0).getHiddenChildList().get(0);
		}

		return null;
	}

	protected MemberModel getRightMostMethodInBlock() {
		MemberModel rightMost = null;
		int rightMostIndex = -1;
		for(MemberModel stmt : getStatements()) {
			MemberModel currentRightMost = MethodUtil.getRightMostResultingCall(stmt, diagram);
			if(currentRightMost.getFigure()==null || currentRightMost.getFigure().getParent()==null) continue;
			int index = diagram.getChildren().indexOf(currentRightMost.getInstanceModel());
			if(index > rightMostIndex) {
				rightMostIndex = index;
				rightMost = currentRightMost;
			}
		}
		return rightMost;
	};

	public IFigure getTopMostFigure() {
		return getFigureOfModel(getTopMostModel());
	} 
	public IFigure getBottomMostFigure() {
		return getFigureOfModel(getBottomMostModel());
	}
	public IFigure getRightMostFigure() {
		return getFigureOfModel(getRightMostModel());
	}
	private IFigure getFigureOfModel(ArtifactFragment model) {
		if(model instanceof MemberModel) return ((MemberModel)model).getFigure();
		if(model instanceof HiddenNodeModel) return ((HiddenNodeModel)model).getFigure();
		return null;
	}

	public List<ControlFlowBlock> getBlocksTopModelContainedIn() {
		return getBlocksContainedInForModel(getTopMostModel());
	}
	public List<ControlFlowBlock> getBlocksBottomModelContainedIn() {
		return getBlocksContainedInForModel(getBottomMostModel());
	}
	private List<ControlFlowBlock> getBlocksContainedInForModel(ArtifactFragment model) {
		if(model instanceof MethodBoxModel) return ((MethodBoxModel)model).getConditionalBlocksContainedIn();
		if(model instanceof HiddenNodeModel) return ((HiddenNodeModel)model).getConditionalBlocksContainedIn();
		return null;
	}

	protected static CollapseExpandButton getCorrespondingCollapseExpandButton(MemberModel method) {
		for(ControlFlowBlock block : method.getConditionalBlocksContainedIn()) {
			if(!(block instanceof IfBlock)) continue;

			IfBlock ifBlock = (IfBlock) block;
			for(CollapseExpandButton button : ifBlock.getCollapseExpandButtons()) {
				if(!button.getMemberToHiddenMap().containsKey(method)) continue;
				if(button.getMemberToHiddenMap().get(method).getParentArt()==null) continue;
				return button;
			}
		}
		return null;
	}

	public void setOuterIsCollapsed() {
		boolean collapsed = false;
		ControlFlowBlock outerBlock = getOuterConditionalBlock();
		while(outerBlock!=null) {
			for(CollapseExpandButton button : outerBlock.getCollapseExpandButtons()) {
				if(ControlFlowBlock.HIDING.equals(button.getType())
						&& outerBlock.buttonToContainedInnerBlocks.containsKey(button) 
						&& outerBlock.buttonToContainedInnerBlocks.get(button).contains(this)) collapsed = true;
			}
			outerBlock = outerBlock.getOuterConditionalBlock();
		}
		outerIsCollapsed = collapsed;
	}

	public boolean outerIsCollapsed() {
		return outerIsCollapsed;
	}

	public void showOrHide(boolean show) {
		if(getHighlight()==null) return;

		if(outerIsCollapsed) show = false;

		getHighlight().setVisible(show);
		getTypeLabel().setVisible(show);
		if(!(this instanceof IfBlock && ((IfBlock)this).getThenStmts().size()==0)) { 
			getConditionLabel().setVisible(show);
		}

		if(this instanceof IfBlock) {

			for(ElseBlock elseBlock : ((IfBlock)this).getElseBlocks()) {
				elseBlock.getLabel().setVisible(show);
//				elseBlock.getButton().setVisible(show);
			}
			for(Polyline divisionLine : ((IfBlock)this).getElseDivisionLines()) {
				divisionLine.setVisible(show);
			}
			if(((IfBlock)this).getThenBlockCollapseExpandButton()!=null) ((IfBlock)this).getThenBlockCollapseExpandButton().setVisible(show);
		}

		for(IFigure innerBlock : getInnerConditionalBlocks()) {
			if (innerBlock instanceof ControlFlowBlock)
				((ControlFlowBlock) innerBlock).showOrHide(show);
		}
	}

	public void showOrHideOuterBlock(Point location) {
		if(getInnerConditionalBlocks().size()==0) return;

		boolean show = true;
		for(ControlFlowBlock innerBlock : getInnerConditionalBlocks()) {
			if(innerBlock.getBounds().contains(location)) show = false;
		}
		if(outerIsCollapsed) show = false;

		getHighlight().setVisible(show);
		getTypeLabel().setVisible(show);
		if(!(this instanceof IfBlock && ((IfBlock)this).getThenStmts().size()==0)) { 
			getConditionLabel().setVisible(show);
		}

		if(this instanceof IfBlock) {
			for(ElseBlock elseBlock : ((IfBlock)this).getElseBlocks()) {
				elseBlock.getLabel().setVisible(show);
//				elseBlock.getButton().setVisible(show);
			}
			for(Polyline divisionLine : ((IfBlock)this).getElseDivisionLines()) {
				divisionLine.setVisible(show);
			}
			if(((IfBlock)this).getThenBlockCollapseExpandButton()!=null) ((IfBlock)this).getThenBlockCollapseExpandButton().setVisible(show);
		}

		for(ControlFlowBlock innerBlock : getInnerConditionalBlocks()) {
			innerBlock.showOrHideOuterBlock(location);
		}
	}

	private void updateHighlightInnerBlock() {
		if(getHighlight()==null || getHighlight().getParent()==null) return;
		IFigure parent = getHighlight().getParent();
		parent.remove(getHighlight());
		parent.add(getHighlight());

		for(ControlFlowBlock innerBlock : getInnerConditionalBlocks()) {
			innerBlock.updateHighlightInnerBlock();
		}
	}

	private void labelBoundsSet(Rectangle oldBoundsOfUpdatedLabel, Rectangle newBoundsOfUpdatedLabel) {
		if(oldBoundsOfUpdatedLabel.height==newBoundsOfUpdatedLabel.height && 
				oldBoundsOfUpdatedLabel.width==newBoundsOfUpdatedLabel.width) return;

		if(this instanceof IfBlock) ((IfBlock)this).figureMoved(null);
		else if(this instanceof LoopBlock) ((LoopBlock)this).figureMoved(null);
	}

	public class Highlight extends RoundedRectangle implements FigureListener {

		public Highlight() {
			setOpaque(true);
			setVisible(false);
			setForegroundColor(ColorScheme.controlFlowHighlightBorder);
		}
//		
//		public ControlFlowBlock getOuterConditionalBlock() {
//			return outerConditionalBlock;
//		}

		public void figureMoved(IFigure source) {
			setBounds(source.getBounds());

			if(!(source instanceof ControlFlowBlock)) return;

			int nesting = 0;
			ControlFlowBlock outerBlock = getOuterConditionalBlock();
			while(outerBlock!=null) {
				nesting = nesting+1;
				outerBlock = outerBlock.getOuterConditionalBlock();
			}
			int r = redValue - redDecrement*nesting;
			int g = greenValue - greenDecrement*nesting;
			int b = blueValue - blueDecrement*nesting;
			setBackgroundColor(new Color(null, r, g, b));

			for(IFigure innerBlock : ((ControlFlowBlock)source).getInnerConditionalBlocks()) {
				if (innerBlock instanceof ControlFlowBlock)
					((ControlFlowBlock) innerBlock).updateHighlightInnerBlock();
			}
		}
	}
	
}
