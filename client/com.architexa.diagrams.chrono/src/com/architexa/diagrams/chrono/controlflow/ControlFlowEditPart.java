package com.architexa.diagrams.chrono.controlflow;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.architexa.diagrams.chrono.commands.CollapseExpandButtonCommand;
import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.editpolicies.SeqComponentEditPolicy;
import com.architexa.diagrams.chrono.editpolicies.SeqOrderedLayoutEditPolicy;
import com.architexa.diagrams.chrono.editpolicies.SeqSelectionEditPolicy;
import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.draw2d.ActionEvent;
import com.architexa.org.eclipse.draw2d.ActionListener;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class ControlFlowEditPart extends AbstractGraphicalEditPart implements ActionListener, PropertyChangeListener {

	private Figure cfContentPane;

	@Override
	public List<ArtifactFragment> getModelChildren() {
		return ((ControlFlowModel)getModel()).getInnerConditionalModels();
	}
	
	@Override
	public void refresh() {
		super.refresh();
		for (Object cfep : getChildren()) {
			((ControlFlowEditPart) cfep).refresh();
		}
		((ControlFlowBlock) getFigure()).updateBlock();
	}
	
	@Override
	protected void addChildVisual(EditPart childEditPart, int index) {
		IFigure figure = ((AbstractGraphicalEditPart)childEditPart).getFigure();
		if (figure instanceof ControlFlowBlock) {
			DiagramEditPart rootEP =  (DiagramEditPart) getRoot().getChildren().get(0);
			SeqOrderedLayoutEditPolicy policy = (SeqOrderedLayoutEditPolicy) rootEP.getEditPolicy(EditPolicy.LAYOUT_ROLE);
			policy.addConditionalFigure(figure);
		} else {
			super.addChildVisual(childEditPart, index);
		}
	}
	
	@Override
	protected void removeChildVisual(EditPart childEditPart) {
		IFigure figure = ((AbstractGraphicalEditPart)childEditPart).getFigure();
		if (figure instanceof ControlFlowBlock) {
			DiagramEditPart rootEP =  (DiagramEditPart) getRoot().getChildren().get(0);
			SeqOrderedLayoutEditPolicy policy = (SeqOrderedLayoutEditPolicy) rootEP.getEditPolicy(EditPolicy.LAYOUT_ROLE);
			policy.removeConditionalFigure(figure);
		} else {
			super.removeChildVisual(childEditPart);
		}
	}
	
	@Override
	public IFigure getContentPane() {
		return cfContentPane;
	}
	
	@Override
	protected IFigure createFigure() {
		ControlFlowModel model = (ControlFlowModel) getModel();
		ControlFlowBlock fig = null;
		cfContentPane = new Figure();
		
		if (model instanceof IfBlockModel){
			fig = new IfBlock(model.getConditionalLabel(), model.getDiagram());
			((IfBlock) fig).setElseStmts(((IfBlockModel) model).getElseStmts());
			((IfBlock) fig).setThenStmts(((IfBlockModel) model).getThenStmts());
			((IfBlock) fig).addElseIfStmts(model.getConditionalLabel(), ((IfBlockModel) model).getIfStmts());
		} else if (model instanceof LoopBlockModel) {
			fig = new LoopBlock(model.getConditionalLabel(), model.getDiagram());
			((LoopBlock) fig).setLoopStmts(model.getStatements());
		} else {
			fig = new UserCreatedConditionalBlock(model.getConditionalLabel(), model.getDiagram());
			((UserCreatedConditionalBlock) fig).setStmts(model.getStatements());
		}
		fig.setModel(model);
		
		// TODO, these figs should come from the EPs
		IFigure bottomMostFig = getFigureOfModel(model.getBottomMostModel());//fig.getTopMostFigure();
		IFigure topLeftFig = getFigureOfModel(model.getTopMostModel());//fig.getBottomMostFigure();
		IFigure rightMostFig = getFigureOfModel(model.getRightMostModel());//fig.getRightMostFigure();
		
		EditPart parentEP = getParent();
		if (parentEP instanceof ControlFlowEditPart) {
			ControlFlowEditPart parentCFEP = (ControlFlowEditPart) parentEP;

			((ControlFlowBlock) parentCFEP.getFigure()).addInnerConditionalBlock(fig);
			fig.setOuterConditionalBlock((ControlFlowBlock) parentCFEP.getFigure());
		}
		
//		List<ControlFlowEditPart> conditionalEPs =((DiagramEditPart) getParent()).getConditionalChildren();
//		
//		for (ControlFlowModel innerModel : model.getInnerConditionalModels()) {
//			for (ControlFlowEditPart cfEP : conditionalEPs) {
//				if (cfEP.getModel().equals(innerModel)) {
//					fig.addInnerConditionalBlock((ControlFlowBlock) cfEP.getFigure());
//					((ControlFlowBlock) cfEP.getFigure()).setOuterConditionalBlock(fig);
//				}
//			}
//		}
//		if (model.getOuterConditionalModel()!=null) {
//			
//			for (ControlFlowEditPart cfEP : conditionalEPs) {
//				if (cfEP.getModel().equals(model.getOuterConditionalModel())) {
//					fig.setOuterConditionalBlock((ControlFlowBlock) cfEP.getFigure());
//					((ControlFlowBlock) cfEP.getFigure()).addInnerConditionalBlock(fig);
//				}
//			}
//		}
		
		if(topLeftFig != null && bottomMostFig != null && rightMostFig != null) {
			// add figure listener to the parent method declaration of the invocation stored.
			// first getParent() gives the cut corner and the second gives the actual declaration figure
			topLeftFig.getParent().getParent().addFigureListener(fig);
			
			rightMostFig.addFigureListener(fig);
	
			fig.setBottomFigureListeningTo(bottomMostFig);
			fig.setTopLeftFigureListeningTo(topLeftFig);
			fig.setRightFigureListeningTo(rightMostFig);
		} 
		fig.updateBlock();
		fig.addPropertyChangeListener(this);
		model.addPropertyChangeListener(this);
		for(CollapseExpandButton button : fig.getCollapseExpandButtons()) 
			button.addActionListener(this);
		return fig;
	}

	public static IFigure getFigureOfModel(ArtifactFragment model) {
		if(model instanceof MemberModel) return ((MemberModel)model).getFigure();
		if(model instanceof HiddenNodeModel) return ((HiddenNodeModel)model).getFigure();
		return null;
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new SeqComponentEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new SeqOrderedLayoutEditPolicy());
		installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new SeqSelectionEditPolicy());
	}

	public void actionPerformed(ActionEvent event) {
		if(event.getSource() instanceof CollapseExpandButton){
			CollapseExpandButtonCommand command = new CollapseExpandButtonCommand((CollapseExpandButton) event.getSource());
			handleCollapseExpandButtonPress((CollapseExpandButton)event.getSource(), command);
		}
	}

	private void handleCollapseExpandButtonPress(CollapseExpandButton button, CollapseExpandButtonCommand command) {
//		try {
//			button.collapseOrExpand(true, command);
//
//			// had to execute here otherwise the inner/outer blocks were getting wrong Top/Bottom/Righmost figures
//			getViewer().getEditDomain().getCommandStack().execute(command);
//			ControlFlowBlock block = ((ControlFlowModel)getModel()).getControlFlowBlock();
//
//			for(ControlFlowBlock innerBlock : block.buttonToContainedInnerBlocks.get(button)) {
//				innerBlock.setOuterIsCollapsed();
//				innerBlock.showOrHide(!innerBlock.outerIsCollapsed());
//			}
//
//			block.setTopLeftFigureListeningTo(block.getTopMostFigure());
//			block.setBottomFigureListeningTo(block.getBottomMostFigure());
//			block.setRightFigureListeningTo(block.getRightMostFigure());
//
//			ControlFlowBlock outerBlock = block.getOuterConditionalBlock();
//			while(outerBlock!=null) {
//				outerBlock.setTopLeftFigureListeningTo(outerBlock.getTopMostFigure());
//				outerBlock.setBottomFigureListeningTo(outerBlock.getBottomMostFigure());
//				outerBlock.setRightFigureListeningTo(outerBlock.getRightMostFigure());
//				outerBlock = outerBlock.getOuterConditionalBlock();
//			}
//
//			List<ControlFlowBlock> innerBlocks = block.getInnerConditionalBlocks();
//			for(ControlFlowBlock innerBlock : innerBlocks) 	
//				setFiguresListeningToForInnerBlocks(innerBlock);
//
//		} catch (Throwable t) {
//			t.printStackTrace();
//		}
	}

//	private void setFiguresListeningToForInnerBlocks(ControlFlowBlock block) {
//		block.setTopLeftFigureListeningTo(block.getTopMostFigure());
//		block.setBottomFigureListeningTo(block.getBottomMostFigure());
//		block.setRightFigureListeningTo(block.getRightMostFigure());
//		if(block.getInnerConditionalBlocks().size()==0) block.figureMoved(null);
//		for(ControlFlowBlock innerBlock : block.getInnerConditionalBlocks()) {
//			setFiguresListeningToForInnerBlocks(innerBlock);
//		}
//	}

	public void propertyChange(PropertyChangeEvent evt) {
		String evtName = evt.getPropertyName();
		if(ControlFlowModel.PROPERTY_DELETE.equals(evtName)) {
			removeChildVisual(this);
		} else if("parent".equals(evt.getPropertyName()))
			refreshChildren();
	}

}
