package com.architexa.diagrams.chrono.animation;

import java.util.Map;


import com.architexa.diagrams.chrono.commands.InstanceCreateCommand;
import com.architexa.diagrams.chrono.commands.MemberCreateCommand;
import com.architexa.diagrams.chrono.figures.InstanceFigure;
import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.util.CommandUtil;
import com.architexa.diagrams.ui.AnimationCommand;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class AnimateCalledByCommand extends AnimationCommand {

	MethodBoxModel declMakingInvoc;
	MethodBoxModel invocation;
	MethodBoxModel declaration;

	public AnimateCalledByCommand() {
		setLabel("adding invocation of method");
	}

	public InstanceFigure getInstanceFigure() {
		return declMakingInvoc.getInstanceModel().getFigure();
	}

	public boolean isInstanceNew() {
		for(Object command : CommandUtil.getAllContainedCommands(this)) {
			if(!(command instanceof InstanceCreateCommand)) continue;
			return declMakingInvoc.getInstanceModel().equals(((InstanceCreateCommand)command).getChild());
		}
		return false;
	}

	public void setDeclMakingInvoc(MethodBoxModel declaration) {
		declMakingInvoc = declaration;
	}

	public MethodBoxFigure getDeclMakingInvocFigure() {
		return declMakingInvoc.getFigure();
	}

	public boolean isDeclMakingInvocNew() {
		return isMethodNew(declMakingInvoc);
	}

	public void setInvocation(MethodBoxModel invocation) {
		this.invocation = invocation;
	}

	private MethodBoxFigure getInvocationFigure() {
		if(invocation==null) return null;
		return invocation.getFigure();
	}

	public void setDeclaration(MethodBoxModel declaration) {
		this.declaration = declaration;
	}

	public boolean isDeclarationNew() {
		return isMethodNew(declaration);
	}

	private boolean isMethodNew(MethodBoxModel method) {
		boolean isNew = false;
		for(Object command : CommandUtil.getAllContainedCommands(this)) {
			if(!(command instanceof MemberCreateCommand)) continue;
			if(method.equals(((MemberCreateCommand)command).getChild())) {
				isNew = true;
				break;
			}
		}
		return isNew;
	}

	public MethodBoxFigure getDeclarationFigure() {
		if(declaration==null) return null;
		return declaration.getFigure();
	}

	@Override
	public void setAnimationStates(IFigure figure, Rectangle rect1, Rectangle rect2) {
		if(figure.equals(getInstanceFigure())) {
			setAnimationStatesForSourceOfCallInstance(rect1, rect2);
		} else if(figure.equals(getInstanceFigure().getChildrenContainer()) && isDeclMakingInvocNew() && !isInstanceNew() && !isDeclarationNew()) {
			setAnimationStatesForSourceOfCallInstanceChildrenContainer(rect1, rect2);
		} else if(figure.equals(getDeclMakingInvocFigure())) {
			setAnimationStatesForContainerDecl(rect1, rect2);
		} else if(figure.equals(getInvocationFigure())) {
			setAnimationStatesForInvocation(rect1, rect2);
		} else if(figure.equals(getDeclarationFigure())) {
			setAnimationStatesForDeclaration(figure, rect1, rect2);
		} else if(figure.equals(declaration.getInstanceModel().getFigure().getChildrenContainer()) && !isDeclarationNew()) {
			setAnimationStatesForTargetOfCallInstanceChildrenContainer(rect1, rect2);
		}
	}

	@Override
	public void makeAdjustmentsForAnimationPlayback(IFigure figure, Rectangle rect1, Rectangle rect2, Map<Object, Object> finalStates) {
	}

	private void setAnimationStatesForSourceOfCallInstance(Rectangle rect1, Rectangle rect2) {
		rect1.x = rect2.x;
		rect1.y = rect2.y;
		rect1.width = rect2.width;
		rect1.height = rect2.height;
	}

	private void setAnimationStatesForSourceOfCallInstanceChildrenContainer(Rectangle rect1, Rectangle rect2) {
		Rectangle declarationFigBounds = getDeclarationFigure().getBounds().getCopy();
		if(rect1.getRight().x < declarationFigBounds.getRight().x) {
			rect1.width = rect1.width + declarationFigBounds.getRight().x - rect1.getRight().x;
			rect2.width = rect1.width;
		}
	}

	private void setAnimationStatesForContainerDecl(Rectangle rect1, Rectangle rect2) {
		if(isDeclMakingInvocNew() && !isInstanceNew() && !isDeclarationNew()) {
			Rectangle declarationFigBounds = getDeclarationFigure().getBounds().getCopy();

			rect1.x = declarationFigBounds.x;
		} else {
			rect1.x = rect2.x;
		}
		rect1.y = rect2.y;
		rect1.width = rect2.width;
		rect1.height = rect2.height;
	}

	private void setAnimationStatesForInvocation(Rectangle rect1, Rectangle rect2) {
		if(!isDeclMakingInvocNew()) {
			rect1.x = getDeclarationFigure().getBounds().getCopy().x;
		} else {
			rect1.x = rect2.x;
		}
		rect1.y = rect2.y;
		rect1.width = rect2.width;
		rect1.height = rect2.height;
	}

	private void setAnimationStatesForDeclaration(IFigure figure, Rectangle rect1, Rectangle rect2) {
		Rectangle invocationFigBounds = getInvocationFigure().getBounds().getCopy();

		if(isInstanceNew())	rect1.x = invocationFigBounds.x;
		else				rect1.x = rect2.x - 1; // kluge to avoid the rect1==rect2 continue in animation playback

		rect1.y = invocationFigBounds.y;
		rect2.y = invocationFigBounds.y;

		rect1.width = rect2.width;
		rect1.height = rect2.height;

		figure.setLocation(new Point(figure.getBounds().x, rect1.y));
	}

	private void setAnimationStatesForTargetOfCallInstanceChildrenContainer(Rectangle rect1, Rectangle rect2) {
		int invocationY = getInvocationFigure().getBounds().getCopy().y;
		int declarationY = getDeclarationFigure().getBounds().getCopy().y;
		int difference = invocationY - declarationY;
		rect2.height = rect2.height + difference;
		rect1.height = rect2.height;
	}

}