package com.architexa.diagrams.ui;

import java.util.Map;


import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public abstract class AnimationCommand extends CompoundCommand {

	public abstract void setAnimationStates(IFigure figure, Rectangle rect1, Rectangle rect2);
	public abstract void makeAdjustmentsForAnimationPlayback(IFigure figure, Rectangle rect1, Rectangle rect2, Map<Object, Object> finalStates);

}