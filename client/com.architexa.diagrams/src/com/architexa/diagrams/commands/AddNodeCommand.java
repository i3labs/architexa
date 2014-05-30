package com.architexa.diagrams.commands;


import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.org.eclipse.gef.commands.Command;

public class AddNodeCommand extends Command {

	private RootArtifact rootArt;
	private ArtifactFragment parentAF;
	private ArtifactFragment childAF;
	
	public AddNodeCommand(RootArtifact rootArt, ArtifactFragment parentAF, ArtifactFragment childAF) {
		this.rootArt = rootArt;
		this.parentAF = parentAF;
		this.childAF = childAF;
	}
	
	
	@Override
	public void execute() {
		if (parentAF != null)
			parentAF.appendShownChild(childAF);
	}

	@Override
	public void undo(){
		if (childAF != null && childAF.getParentArt() != null)
			rootArt.removeVisibleArt(childAF);
	}
	
	public ArtifactFragment getNewArtFrag() {
		return childAF;
	}
	public ArtifactFragment getNewParentArtFrag() {
		return parentAF;
	}
}
