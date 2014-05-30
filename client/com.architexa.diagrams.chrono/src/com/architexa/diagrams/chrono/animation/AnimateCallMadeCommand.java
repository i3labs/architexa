package com.architexa.diagrams.chrono.animation;

import java.util.Map;


import com.architexa.diagrams.chrono.commands.InstanceCreateCommand;
import com.architexa.diagrams.chrono.commands.MemberCreateCommand;
import com.architexa.diagrams.chrono.figures.InstanceFigure;
import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.util.CommandUtil;
import com.architexa.diagrams.chrono.util.LayoutUtil;
import com.architexa.diagrams.commands.AddResCommand;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.ui.AnimationCommand;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class AnimateCallMadeCommand extends AnimationCommand implements AddResCommand {

	private MethodBoxModel invocation;
	private MethodBoxModel declaration;

	private Artifact newArt;

	public AnimateCallMadeCommand() {
		this(null);
	}

	public AnimateCallMadeCommand(Artifact newArt) {
		setLabel("adding method call");
		this.newArt = newArt;
	}

	public Artifact getNewArt() {
		return newArt;
	}

	public boolean isCallToDifferentClass() {
		return !isCallToSameClass();
	}

	public boolean isCallToSameClass() {
		return invocation.equals(declaration.getParent());
	}

	public boolean isInstanceNew() {
		for(Object command : CommandUtil.getAllContainedCommands(this)) {
			if(!(command instanceof InstanceCreateCommand)) continue;
			return declaration.getInstanceModel().equals(((InstanceCreateCommand)command).getChild());
		}
		return false;
	}

	public void setMethodCall() {
		for(Object command : CommandUtil.getAllContainedCommands(this)) {
			if(!(command instanceof MemberCreateCommand) || !(((MemberCreateCommand)command).getChild() instanceof MethodBoxModel)) continue;
			MethodBoxModel newMethod = (MethodBoxModel) ((MemberCreateCommand)command).getChild();
			if(newMethod.isAccess()) this.invocation = newMethod;
			if(newMethod.isDeclaration()) this.declaration = newMethod;
		}
	}
	
	public MethodBoxModel getDeclaration() {
		return declaration;
	}

	public MethodBoxFigure getInvocationFigure() {
		return invocation.getFigure();
	}

	public MethodBoxFigure getDeclarationFigure() {
		return declaration.getFigure();
	}

	public InstanceFigure getInstanceOfDeclFigure() {
		return declaration.getInstanceModel().getFigure();
	}

	@Override
	public void execute() {
		if(invocation==null || declaration==null) setMethodCall();
		super.execute();
	}

	@Override
	public void setAnimationStates(IFigure figure, Rectangle rect1, Rectangle rect2) {
		if(figure.equals(getDeclarationFigure())) {
			setAnimationStatesForNewDecl(rect1, rect2);
		} else if(isCallToDifferentClass() && figure.equals(getInstanceOfDeclFigure().getChildrenContainer())) {
			setAnimationStatesForInstanceChildrenContainer(figure, rect1, rect2);
		} else if(isCallToDifferentClass() && figure instanceof MethodBoxFigure 
				&& MethodBoxModel.declaration==((MethodBoxFigure)figure).getType()
				&& ((MethodBoxFigure)figure).getPartner()!=null) {
			setAnimationStatesForOtherMethods((MethodBoxFigure)figure, rect1, rect2);
		} else {
			rect1.x = rect2.x;
			rect1.y = rect2.y;
			rect1.width = rect2.width;
			rect1.height = rect2.height;
		}
	}

	@Override
	public void makeAdjustmentsForAnimationPlayback(IFigure figure, Rectangle rect1, Rectangle rect2, Map<Object, Object> finalStates) {
		if(!isCallToDifferentClass() || !figure.equals(getDeclarationFigure())) return;

		Rectangle parentinstanceBounds = (Rectangle) finalStates.get(getInstanceOfDeclFigure().getChildrenContainer());
		rect2.x = (parentinstanceBounds.x + parentinstanceBounds.x + parentinstanceBounds.width)/2 - rect2.width/2 - 1;
	}

	private void setAnimationStatesForNewDecl(Rectangle rect1, Rectangle rect2) {
		if(isCallToDifferentClass()) {
			Rectangle invocationFigBounds = getInvocationFigure().getBounds().getCopy();

			rect1.x = invocationFigBounds.x;
			rect1.y = invocationFigBounds.y;
			rect1.width = rect2.width;
			rect1.height = rect2.height;

			rect2.y = invocationFigBounds.y;
		} else if(isCallToSameClass()) {
			rect1.x = rect2.x;
			rect1.y = rect2.height/2 + rect2.y;
			rect1.width = rect2.width;
			rect1.height = 0;
		} 
	}

	private void setAnimationStatesForInstanceChildrenContainer(IFigure figure, Rectangle rect1, Rectangle rect2) {
		Rectangle instanceFigBounds = getInstanceOfDeclFigure().getInstanceBox().getBounds().getCopy();

		if(rect2.x+rect2.width < instanceFigBounds.x+instanceFigBounds.width)
			rect2.width = instanceFigBounds.width;
		rect2.height = LayoutUtil.getHeightToContainAllChildren(figure);

		Rectangle invocationFigBounds = getInvocationFigure().getBounds().getCopy();
		rect1.x = invocationFigBounds.x;
		rect1.y = rect2.y;
		rect1.width = rect2.width + rect2.x - invocationFigBounds.x;
		rect1.height = rect2.height;
	}

	private void setAnimationStatesForOtherMethods(MethodBoxFigure figure, Rectangle rect1, Rectangle rect2) {
		Rectangle invocationBounds = figure.getPartner().getBounds().getCopy();
		rect1.y = invocationBounds.y;
		rect2.y = invocationBounds.y;

		rect1.x = rect2.x-1; // kluge to avoid the rect1==rect2 continue in animation playback
		rect1.height = rect2.height;
		rect1.width = rect2.width;
	}

}