package com.architexa.diagrams.chrono.commands;

import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.commands.Command;

public class ResizeInstanceCommand extends Command {

	private InstanceModel instance;
	private Dimension sizeChange;
	private int nameAndIconWidth;

	public ResizeInstanceCommand(InstanceModel model, Dimension dimension) {
		super("Resize Instance");
		this.instance = model;
		this.sizeChange = dimension;
	}
	
	@Override
	public void execute() {
		nameAndIconWidth = instance.getFigure().nameAndIconContainer.getBounds().getCopy().width;
		instance.setInstanceBoxWidth(sizeChange.width+nameAndIconWidth);
	}
	 @Override
	public void undo() {
		 instance.setInstanceBoxWidth(nameAndIconWidth);
	}
}
