package com.architexa.diagrams.chrono.animation;

import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class AnimationLayoutManager extends ToolbarLayout {

	public AnimationLayoutManager(boolean isHorizontal) {
		super(isHorizontal);
	}	

	@Override
	public void layout(IFigure container) {
		Animator.recordInitialState(container);
		if(!Animator.playbackState(container)) super.layout(container);		
	}

}
