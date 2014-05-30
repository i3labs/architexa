/**
 * 
 */
package com.architexa.diagrams.relo.commands.old;


import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.relo.graph.GraphLayoutManager;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;
import com.architexa.diagrams.relo.parts.ReloController;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

public final class CreateParentCommand extends Command {
	/**
	 * Used to create Package for classes after Package has been hidden
	 */
	private final ArtifactEditPart artifactEditPart;
	public Artifact parentArt;
	public ReloController rc;
	public CompoundCommand parentAssertionCommand;
	public ArtifactEditPart parentEP;
	public ArtifactFragment parentAF;

	public CreateParentCommand(ArtifactEditPart artifactEditPart, String label, Artifact parentArt,
			ReloController rc) {
		super(label);
		this.artifactEditPart = artifactEditPart;
		this.parentArt = parentArt;
		this.rc = rc;
	}

	@Override
	public void execute() { 
		parentAF = rc.getRootArtifact().addVisibleArt(parentArt);
	    parentEP = rc.createOrFindArtifactEditPart(parentAF.getArt());
	    
	    // @tag fixSetBounds
	    parentEP.getFigure().setBounds(this.artifactEditPart.getFigure().getBounds().getCopy());

	    GraphLayoutManager glm = (GraphLayoutManager) rc.getFigure().getLayoutManager();
	    if  (glm.isLayedOut(this.artifactEditPart)) {
	        glm.setLayedOut(parentEP);
	        if  (glm.isPartAnchored(this.artifactEditPart)) glm.anchorPart(parentEP);
	    }
	    parentAssertionCommand = new CompoundCommand();
	    parentEP.assertParenthood(parentAssertionCommand);
	    parentAssertionCommand.execute();
	}

	@Override
	public void undo() { 
	    parentAssertionCommand.undo();
	    parentAF.getParentArt().removeShownChild(parentAF);
	}
}