package com.architexa.diagrams.chrono.commands;

import com.architexa.org.eclipse.gef.commands.Command;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqCommand extends Command {

	boolean canAnimate = false;

	public void setAnimatable(boolean canAnimate) {
		this.canAnimate = canAnimate;
	}

	public boolean isAnimatable() {
		return canAnimate==true;
	}

}
