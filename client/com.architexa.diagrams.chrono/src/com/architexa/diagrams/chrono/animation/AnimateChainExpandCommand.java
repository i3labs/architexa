package com.architexa.diagrams.chrono.animation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;

import com.architexa.diagrams.chrono.figures.InstanceFigure;
import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.figures.InstanceFigure.InstanceChildrenContainer;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.util.ConnectionUtil;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.ui.AnimationCommand;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class AnimateChainExpandCommand extends AnimationCommand {

	private DiagramModel diagram;

	private MethodBoxModel chainedInvocation;
	private MethodBoxModel chainedDeclaration;
	private IMethod chainedMethod;
	private ASTNode chainedInvocationNode;
	private ASTNode chainedDeclarationNode;
	private String chainedMessage;
	private String chainedReturnMessage;
	private MethodBoxModel chainedInvocationParent;
	private NodeModel chainedDeclarationParent;
	private InstanceModel chainedInstance;

	private MethodBoxModel firstCallInvocation;
	private MethodBoxModel firstCallDeclaration;
	private String firstCallCallMessage;
	private String firstCallReturnMessage;
	private MethodBoxModel firstCallInvocationParent;
	private NodeModel firstCallDeclarationParent;

	private MethodBoxModel secondCallInvocation;
	private MethodBoxModel secondCallDeclaration;
	private String secondCallCallMessage;
	private String secondCallReturnMessage;
	private MethodBoxModel secondCallInvocationParent;
	private NodeModel secondCallDeclarationParent;

	MethodBoxModel newDeclaration;

	Rectangle originalChainDeclarationBounds;

	private InstanceModel instance;
	private boolean isInstanceNew = false;

	Map<IFigure, Integer> marginMap = new HashMap<IFigure, Integer>();

	public AnimateChainExpandCommand() {
		setLabel("expanding chained call");
	}

	@Override
	public void execute(){
		super.execute();
		addParents();
	}

	private void addParents(){
		firstCallInvocationParent = (MethodBoxModel) firstCallInvocation.getParent();
		firstCallDeclarationParent = firstCallDeclaration.getParent();
		secondCallInvocationParent = (MethodBoxModel)secondCallInvocation.getParent();
		secondCallDeclarationParent = secondCallDeclaration.getParent();
	}

	@Override
	public void undo() {

		int indexOfFirst = firstCallInvocationParent.getChildren().indexOf(firstCallInvocation);
		firstCallInvocation.deleteBasic();
		firstCallDeclaration.deleteBasic();
		secondCallInvocation.deleteBasic();
		secondCallDeclaration.deleteBasic();

		chainedInvocationParent.addChild(chainedInvocation, indexOfFirst);
		chainedDeclarationParent.addChild(chainedDeclaration, MemberUtil.getDeclarationIndex(chainedDeclarationParent, chainedInvocation, chainedInvocationParent));
		chainedInvocation.changeMethodRepresented(chainedMethod, chainedInvocationNode);
		chainedDeclaration.changeMethodRepresented(chainedMethod, chainedDeclarationNode);
		chainedDeclaration.setInstanceModel(chainedInstance);
		ConnectionUtil.createConnection(chainedMessage, chainedInvocation, chainedDeclaration, ConnectionModel.CALL);
		ConnectionUtil.createConnection(chainedReturnMessage, chainedDeclaration, chainedInvocation, ConnectionModel.RETURN);

		if(isInstanceNew) {
			diagram.removeChild(instance);
		}
	}

	@Override
	public void redo() {
		if(isInstanceNew) {
			diagram.addChild(instance, diagram.getChildren().indexOf(chainedInstance));
		}

		int indexOfChained = chainedInvocationParent.getChildren().indexOf(chainedInvocation);

		chainedInvocation.deleteBasic();
		chainedDeclaration.deleteBasic();

		firstCallInvocationParent.addChild(firstCallInvocation, indexOfChained);
		firstCallDeclarationParent.addChild(firstCallDeclaration, MemberUtil.getDeclarationIndex(firstCallDeclarationParent, firstCallInvocation, firstCallInvocationParent));
		ConnectionUtil.createConnection(firstCallCallMessage, firstCallInvocation, firstCallDeclaration, ConnectionModel.CALL);
		ConnectionUtil.createConnection(firstCallReturnMessage, firstCallDeclaration, firstCallInvocation, ConnectionModel.RETURN);

		secondCallInvocationParent.addChild(secondCallInvocation, indexOfChained+1);
		secondCallDeclarationParent.addChild(secondCallDeclaration, MemberUtil.getDeclarationIndex(secondCallDeclarationParent, secondCallInvocation, secondCallInvocationParent));
		ConnectionUtil.createConnection(secondCallCallMessage, secondCallInvocation, secondCallDeclaration, ConnectionModel.CALL);
		ConnectionUtil.createConnection(secondCallReturnMessage, secondCallDeclaration, secondCallInvocation, ConnectionModel.RETURN);
	}

	public void setDiagram(DiagramModel diagram) {
		this.diagram = diagram;
	}

	public void setChainedInvocation(MethodBoxModel chainedInvocation) {
		this.chainedInvocation = chainedInvocation;
		this.chainedMethod = chainedInvocation.getMethod();
		this.chainedInvocationNode = chainedInvocation.getASTNode();
		this.chainedInvocationParent = (MethodBoxModel) chainedInvocation.getParent();
	}

	public void setChainedDeclaration(MethodBoxModel chainedDeclaration) {
		this.chainedDeclaration = chainedDeclaration;
		this.chainedDeclarationNode = chainedDeclaration.getASTNode();
		this.chainedDeclarationParent = chainedDeclaration.getParent();
		this.chainedInstance = chainedDeclaration.getInstanceModel();
	}

	public void setChainedMessage(String chainedMessage) {
		this.chainedMessage = chainedMessage;
	}

	public void setChainedReturnMessage(String chainedReturnMessage) {
		this.chainedReturnMessage = chainedReturnMessage;
	}

	public void setFirstCallInvocation(MethodBoxModel invocation) {
		firstCallInvocation = invocation;
		firstCallInvocationParent = (MethodBoxModel) invocation.getParent();
	}

	public void setFirstCallDeclaration(MethodBoxModel declaration) {
		firstCallDeclaration = declaration;
		firstCallDeclarationParent = declaration.getParent();
	}

	public void setFirstCallCallMessage(String callMessage) {
		firstCallCallMessage = callMessage;
	}

	public void setFirstCallReturnMessage(String returnMessage) {
		firstCallReturnMessage = returnMessage;
	}

	public void setSecondCallInvocation(MethodBoxModel invocation) {
		this.secondCallInvocation = invocation;
		secondCallInvocationParent = (MethodBoxModel) invocation.getParent();
	}

	public void setSecondCallDeclaration(MethodBoxModel declaration) {
		this.secondCallDeclaration = declaration;
		secondCallDeclarationParent = declaration.getParent();
	}

	public void setSecondCallCallMessage(String callMessage) {
		secondCallCallMessage = callMessage;
	}

	public void setSecondCallReturnMessage(String returnMessage) {
		secondCallReturnMessage = returnMessage;
	}

	public void setNewDeclaration(MethodBoxModel newDeclaration) {
		this.newDeclaration = newDeclaration;
	}

	public MethodBoxFigure getNewDeclarationFigure() {
		return newDeclaration.getFigure();
	}

	public void setOriginalChainedDeclarationBounds(Rectangle bounds) {
		originalChainDeclarationBounds = bounds;
	}

	public void setInstance(InstanceModel instance) {
		this.instance = instance;
	}

	public InstanceFigure getInstanceFigure() {
		return instance.getFigure();
	}

	public void setIsInstanceNew(boolean isNew) {
		isInstanceNew = isNew;
	}

	public boolean isInstanceNew() {
		return isInstanceNew;
	}

	@Override
	public void setAnimationStates(IFigure figure, Rectangle rect1, Rectangle rect2) {
		if(figure.equals(firstCallInvocation.getFigure())) {
			setAnimationStatesForFirstCallInvocation(rect1, rect2);
		} else if(figure.equals(firstCallDeclaration.getFigure())) {
			setAnimationStatesForFirstCallDeclaration(rect1, rect2);
		} else if(figure.equals(secondCallInvocation.getFigure())) {
			setAnimationStatesForSecondCallInvocation(rect1, rect2);
		} else if(figure.equals(secondCallDeclaration.getFigure())) { 
			setAnimationStatesForSecondCallDeclaration(rect1, rect2);
		} else if(figure.equals(getInstanceFigure())) {
			setAnimationStatesForInstance(rect1, rect2);
		}  else if(figure instanceof MethodBoxFigure) {
			setAnimationStatesForOtherMethods((MethodBoxFigure)figure, rect1, rect2);
		} else if(figure instanceof Label) {
			rect1.x = rect2.x;
			rect1.y = rect2.y;
			rect1.width = rect2.width;
			rect1.height = rect2.height;
		}
	}

	@Override
	public void makeAdjustmentsForAnimationPlayback(IFigure figure,
			Rectangle rect1, Rectangle rect2, Map<Object, Object> finalStates) {

		if (figure instanceof MethodBoxFigure 
				&& ((MethodBoxFigure)figure).getType()==MethodBoxModel.declaration 
				&& ((MethodBoxFigure)figure).getPartner()!=null){
			Rectangle invocationBounds = ((MethodBoxFigure)figure).getPartner().getBounds().getCopy();
			rect1.y = invocationBounds.y;
			rect2.y = invocationBounds.y;
			rect1.width = rect2.width-1;
		} 

		if(!(figure.getParent() instanceof InstanceChildrenContainer)) return;
		IFigure parent = figure.getParent();
		Rectangle parentBounds = parent.getBounds().getCopy();
		if(marginMap.get(figure)!=null) {
			rect1.x = parentBounds.x + marginMap.get(figure);
			rect2.x = parentBounds.x + marginMap.get(figure);
		} else if(rect1.x > parentBounds.getRight().x) {
			int margin = ((Rectangle)finalStates.get(figure)).x - ((Rectangle)finalStates.get(parent)).x;
			rect1.x = parentBounds.x + margin;
			rect2.x = parentBounds.x + margin;
			marginMap.put(figure, margin);
		} else if(figure.getBounds().getCopy().x < parentBounds.x) {
			rect1.x = parentBounds.x;
			rect2.x = parentBounds.x;
		} else {
			marginMap.put(figure, figure.getBounds().getCopy().x - parentBounds.x);
		}
	}

	private void setAnimationStatesForFirstCallInvocation(Rectangle rect1, Rectangle rect2) {
		rect1.x = rect2.x;
		rect1.y = rect2.y;
		rect1.width = rect2.width;
		rect1.height = rect2.height;
	}

	private void setAnimationStatesForFirstCallDeclaration(Rectangle rect1, Rectangle rect2) {
		rect1.x = rect2.x;
		rect1.y = rect2.y;
		rect1.width = rect2.width;
		rect1.height = rect2.height;
	}

	private void setAnimationStatesForSecondCallInvocation(Rectangle rect1, Rectangle rect2) {
		rect1.x = firstCallInvocation.getFigure().getBounds().getCopy().x;
		rect1.y = firstCallInvocation.getFigure().getBounds().getCopy().y;
		rect1.width = rect2.width;
		rect1.height = rect2.height;
	}

	private void setAnimationStatesForSecondCallDeclaration(Rectangle rect1, Rectangle rect2) {
		rect1.x = firstCallDeclaration.getFigure().getBounds().getCopy().x;
		rect1.y = firstCallDeclaration.getFigure().getBounds().getCopy().y;
		rect1.width = rect2.width;
		rect1.height = rect2.height;
	}

	private void setAnimationStatesForInstance(Rectangle rect1, Rectangle rect2) {
		rect1.x = rect2.x;
		rect1.y = rect2.y;
		rect1.width = rect2.width;
		rect1.height = rect2.height;
	}

	private void setAnimationStatesForOtherMethods(MethodBoxFigure figure, Rectangle rect1, Rectangle rect2) {
		if(((MethodBoxFigure)figure).getType()==MethodBoxModel.declaration) {
			if(((MethodBoxFigure)figure).getPartner()==null) {
				rect1.x = rect2.x;
				rect1.y = rect2.y;
				rect1.width = rect2.width;
				rect1.height = rect2.height;
			} else {
				rect1.y = rect2.y;
				rect1.width = rect2.width-1;
				rect1.height = rect2.height;

				Rectangle parentinstanceBounds = figure.getParent().getBounds().getCopy();
				if(rect2.x < parentinstanceBounds.x) {
					rect2.x = parentinstanceBounds.x;
				}
			}
		} else if(((MethodBoxFigure)figure).getType()==MethodBoxModel.access) {
			if(!figure.getParent().getChildren().contains(firstCallInvocation.getFigure())) {
				rect1.x = rect2.x;
				rect1.y = rect2.y;
				rect1.width = rect2.width;
				rect1.height = rect2.height;
			}
		}
	}

}
