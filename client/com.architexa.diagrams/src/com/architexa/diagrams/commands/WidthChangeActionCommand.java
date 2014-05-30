package com.architexa.diagrams.commands;

import org.apache.log4j.Logger;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.model.WidthDPolicy;
import com.architexa.org.eclipse.gef.commands.Command;




public class WidthChangeActionCommand extends Command {

	static final Logger logger = Activator.getLogger(WidthChangeActionCommand.class);
	
	private NamedRel af;
	private int newWidth;

	private int prevWidth;

	private int CHANGE_INCREMENT = 2;
	private int DEFAULT_WIDTH = 2;

	private String actnText;

	public WidthChangeActionCommand(String actnText, NamedRel model) {
		super(actnText);
		af = model;
		this.actnText = actnText;
	}
	
	@Override
	public void execute() {
		WidthDPolicy pol = (WidthDPolicy) af.getDiagramPolicy(WidthDPolicy.DefaultKey);
		if (pol ==null) {
			logger.error("Width policy not installed on " + af.toString());
			return;
		}
		prevWidth = pol.getWidth();
		if (actnText.contains("Increase"))
			newWidth = prevWidth +CHANGE_INCREMENT;
		else if (actnText.contains("Decrease"))
			newWidth = prevWidth - CHANGE_INCREMENT;
		else
			newWidth = DEFAULT_WIDTH;
		if (newWidth < 1) newWidth = 1;
		updateWidth(newWidth);
	}
	
	private void updateWidth(int width) {
		WidthDPolicy.setWidth(width, af);
	}
	
	@Override
	public void undo() {
		updateWidth(prevWidth);
	}
	
	@Override
	public void redo() {
		updateWidth(newWidth);
	}
}
