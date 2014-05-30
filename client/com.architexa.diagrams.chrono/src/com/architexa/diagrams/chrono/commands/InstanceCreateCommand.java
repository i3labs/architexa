package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.commands.AddNodeCommand;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class InstanceCreateCommand extends AddNodeCommand {

	private InstanceModel newInstanceModel;
	private DiagramModel diagram;
	private int index = -1;
	private boolean fadeInCreation = true;
	private boolean animate = false;

	public InstanceCreateCommand(InstanceModel newElement, DiagramModel parent, int index, boolean fadeInCreation, boolean animateCreation) {
		super(parent, parent, newElement);
		this.newInstanceModel = newElement;
		this.diagram = parent;
		this.index = index;
		this.fadeInCreation = fadeInCreation;
		this.animate = animateCreation;
		setLabel("instance creation");
	}

	@Override
	public boolean canExecute() {
		return newInstanceModel != null && diagram != null;
	}

	@Override
	public void execute() {
		addChild();
	}

	public void addChild(){
		if(index >= diagram.getChildren().size()) index = -1;
		diagram.addChild(newInstanceModel, index);
	}
	public void setParent(DiagramModel parent) {
		this.diagram = parent;
	}

	public DiagramModel getParent(){
		return diagram;
	}

	public void setChild(InstanceModel child) {
		this.newInstanceModel = child;
	}

	public InstanceModel getChild() {
		return newInstanceModel;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public boolean getFadeInCreation() {
		return fadeInCreation;
	}

	public boolean getAnimate() {
		return animate;
	}

	@Override
	public void undo() {
		if(diagram.getChildren().contains(newInstanceModel))
			diagram.removeChild(newInstanceModel);
	}

	@Override
	public void redo() {
		addChild();
	}
}
