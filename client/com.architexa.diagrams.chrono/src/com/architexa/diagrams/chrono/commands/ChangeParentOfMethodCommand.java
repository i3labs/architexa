package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.org.eclipse.gef.commands.Command;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class ChangeParentOfMethodCommand extends Command {

	private MethodBoxModel child;
	private NodeModel oldParent;
	private NodeModel newParent;
	private int oldIndex = -1;
	private int newIndex = -1;

	public ChangeParentOfMethodCommand(MethodBoxModel child, NodeModel newParent, int newIndex) {
		this.child = child;
		this.oldParent = child.getParent();
		this.newParent = newParent;
		this.oldIndex = child.getParent().getChildren().indexOf(child);
		this.newIndex = newIndex;
		setLabel("changing method's parent");
	}

	@Override
	public void execute() {
		move();
	}

	@Override
	public void undo() {
		newParent.removeChild(child);
		oldParent.addChild(child, oldIndex);
	}

	@Override
	public void redo() {
		move();
	}

	private void move() {
		oldParent.removeChild(child);
		newParent.addChild(child, newIndex);
	}

}
