package com.architexa.diagrams.commands;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ColorDPolicy;
import com.architexa.diagrams.ui.BaseColorScheme;
import com.architexa.org.eclipse.gef.commands.Command;

public class ColorActionCommand extends Command{
	private ArtifactFragment af;
	private Color newColor;
	private Color prevColor;

	static final Logger logger = Activator.getLogger(BreakableCommand.class);
	
	public ColorActionCommand(String actnText, ArtifactFragment model) {
		super("Color Action: " + actnText);
		af = model;
		newColor = BaseColorScheme.getColorFromMap(actnText);
	}
	
	@Override
	public void execute() {
		updateColor(newColor);
	}
	
	private void updateColor(Color color) {
		ColorDPolicy pol = (ColorDPolicy) af.getDiagramPolicy(ColorDPolicy.DefaultKey);
		if (pol ==null) {
			logger.error("Color policy not installed on " + af.toString());
			return;
		}
		prevColor = pol.getColor();
		ColorDPolicy.setColor(color, af);
	}
	
	@Override
	public void undo() {
		updateColor(prevColor);
	}
	
	@Override
	public void redo() {
		updateColor(newColor);
	}
}
