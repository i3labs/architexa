package com.architexa.diagrams.chrono.animation;

import java.util.Map;


import com.architexa.diagrams.chrono.commands.InstanceCreateCommand;
import com.architexa.diagrams.chrono.figures.InstanceFigure;
import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.util.CommandUtil;
import com.architexa.diagrams.commands.AddResCommand;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.ui.AnimationCommand;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.RectangleFigure;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class AnimateOverrideCommand extends AnimationCommand implements AddResCommand {

	InstanceModel instance;
	MethodBoxModel overrider;
	MethodBoxModel overridden;

	MethodBoxModel newMethod = null;

	public void setInstance(InstanceModel instance) {
		this.instance = instance;
	}

	public InstanceFigure getInstanceFigure() {
		return instance.getFigure();
	}

	public boolean isInstanceNew() {
		if(newMethod==null) return false;
		for(Object command : CommandUtil.getAllContainedCommands(this)) {
			if(!(command instanceof InstanceCreateCommand)) continue;
			return newMethod.getInstanceModel().equals(((InstanceCreateCommand)command).getChild());
		}
		return false;
	}

	public void setOverrider(MethodBoxModel overrider) {
		this.overrider = overrider;
	}

	public void setOverridden(MethodBoxModel overridden) {
		this.overridden = overridden;
	}

	public MethodBoxFigure getNewFigure() {
		return newMethod==null ? null : newMethod.getFigure();
	}

	public Artifact getNewArt() {
		if(newMethod!=null) return newMethod.getArt();
		return null;
	}

	public void setNewMethod(MethodBoxModel newMethod) {
		this.newMethod = newMethod;
	}

	public boolean isMethodNew() {
		return newMethod!=null;
	}

	@Override
	public void setAnimationStates(IFigure figure, Rectangle rect1, Rectangle rect2) {
		if(figure.equals(getInstanceFigure()) 
				|| figure.equals(overrider.getFigure()) 
				|| figure.equals(overridden.getFigure())) {
			rect1.x = rect2.x;
			rect1.y = rect2.y;
			rect1.width = rect2.width;
			rect1.height = rect2.height;
		}
	}

	@Override
	public void makeAdjustmentsForAnimationPlayback(IFigure figure,
			Rectangle rect1, Rectangle rect2, Map<Object, Object> finalStates) {}


	public AnimateOverrideConnectionCommand createAnimateOverrideConnectionCommand() {
		return new AnimateOverrideConnectionCommand(overrider, overridden);
	}

	public class AnimateOverrideConnectionCommand extends CompoundCommand {

		MethodBoxModel overrider;
		MethodBoxModel overridden;

		Rectangle start;
		Rectangle end;

		@Override
		public void execute() {
			this.start = setIndicatorAnimateStart();
			this.end = setIndicatorAnimateEnd();
			super.execute();
		}

		public AnimateOverrideConnectionCommand(MethodBoxModel overrider, MethodBoxModel overridden) {
			this.overrider = overrider;
			this.overridden = overridden;
		}

		public MethodBoxFigure getOverriderFigure() {
			return overrider.getFigure();
		}

		public MethodBoxFigure getOverriddenFigure() {
			return overridden.getFigure();
		}

		public ConnectionModel getOverrideConnection() {
			return overrider.getOverridesConnection();
		}

		public Rectangle getIndicatorAnimateStart() {
			return start;
		}

		public Rectangle getIndicatorAnimateEnd() {
			return end;	
		}

		private Rectangle setIndicatorAnimateStart() {
			RectangleFigure indicatorOfOverrider = getOverriderFigure().getOverridesIndicator();	
			Rectangle overriderBounds = getOverriderFigure().getBounds().getCopy();
			Rectangle start = new Rectangle(overriderBounds.getTopRight().x, overriderBounds.getTopRight().y, indicatorOfOverrider.getSize().width, indicatorOfOverrider.getSize().height);

			getOverriderFigure().translateToAbsolute(start);
			indicatorOfOverrider.translateToRelative(start);

			return start;
		}

		private Rectangle setIndicatorAnimateEnd() {
			RectangleFigure indicatorOfOverridden = getOverriddenFigure().getOverriddenIndicator();	
			Rectangle overriddenBounds = getOverriddenFigure().getBounds().getCopy();
			Rectangle end = new Rectangle(overriddenBounds.getBottomLeft().x, overriddenBounds.getBottomLeft().y, indicatorOfOverridden.getSize().width, indicatorOfOverridden.getSize().height);

			getOverriddenFigure().translateToAbsolute(end);
			indicatorOfOverridden.translateToRelative(end);

			return end;
		}

	}

}