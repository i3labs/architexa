/**
 * 
 */
package com.architexa.diagrams.strata.commands;


import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CommandStack;

public class OpenCommand extends Command {
	private final ArtifactFragment artFrag;
	private final CommandStack cmdStack;

	
	public OpenCommand(ArtifactFragment artFrag, CommandStack cmdStack) {
		this.artFrag=artFrag;
		this.cmdStack = cmdStack;
	}

	@Override
	public void execute() { 
//		IFigure myFig = mySAFEP.getFigure();
//		if (myFig instanceof ContainerFigure)
//			((ContainerFigure)myFig).highlight(false);
	    ModelUtils.toggleOpenArtFrag(artFrag, cmdStack);
	}
	
	@Override
	public void undo() {
//		TODO this may need to be looked at in the near future (SR)
//		This is currently not needed since toggleOpenArtFragEP creates a CompoundCommand
//		using queryAndShowChildren that contains addChildren and showChildren which are both undoable	
//		mySAFEP.toggleOpenArtFragEP();
	}

}