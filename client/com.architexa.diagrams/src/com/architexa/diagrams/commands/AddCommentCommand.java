package com.architexa.diagrams.commands;


import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.commands.Command;

/**
 * @author Abhishek Rakshit
 *
 */
public class AddCommentCommand extends Command{

	ArtifactFragment parent;
	Comment comment;
	Point location;

	public AddCommentCommand(ArtifactFragment parent, Comment comment ,Point loc){
		super("New Comment");
		if(parent==null || comment==null || loc == null)
			throw new IllegalArgumentException();
		this.parent = parent;
		this.comment = comment;
		this.location = loc;
	}

	@Override
	public void execute() {
		addComment();
	}

	private void addComment(){
		((RootArtifact)parent).addComment(comment);
		PointPositionedDiagramPolicy.setLoc(comment, location);
	}

	@Override
	public void undo() {
		if (((RootArtifact)parent).getCommentChildren().contains(comment))
			((RootArtifact)parent).removeShownChild(comment);
	}

	@Override
	public void redo() {
		addComment();
	}
}
