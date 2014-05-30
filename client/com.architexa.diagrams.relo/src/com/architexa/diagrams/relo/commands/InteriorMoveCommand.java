package com.architexa.diagrams.relo.commands;


import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.org.eclipse.gef.commands.Command;

public class InteriorMoveCommand extends Command{
	private RootArtifact rootArt;
	private ArtifactFragment parentAF;
	private ArtifactFragment childAF;
	private int oldIndex;
	private int newIndex;
	private ArtifactFragment oldParentAF;
	
	public InteriorMoveCommand(RootArtifact rootArt, ArtifactFragment parentAF, ArtifactFragment childAF, int newIndex, int oldIndex, boolean move) {
		super("Move Element");
		this.rootArt = rootArt;
		this.parentAF = parentAF;
		this.childAF = childAF;
		this.newIndex = newIndex;
		this.oldIndex = oldIndex;
	}
	
	@Override
	public void execute() {
		oldParentAF = childAF.getParentArt();
		if (childAF != null && childAF.getParentArt() != null)
			rootArt.removeVisibleArt(childAF);

		if (parentAF != null) { 
			if (newIndex == -1) parentAF.appendShownChild(childAF);
			else parentAF.appendShownChild(childAF, newIndex);
		}
	}

	@Override
	public void undo(){
		if (childAF != null && childAF.getParentArt() != null)
			rootArt.removeVisibleArt(childAF);
		
		if (oldParentAF != null)
			oldParentAF.appendShownChild(childAF, oldIndex);
	}
	
}