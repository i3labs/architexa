package com.architexa.diagrams.commands;


import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.commands.Command;

public class MoveCommentCommand extends Command{
	CommentEditPart editPart;
	Comment comment;
	Point origTopLeft;
	Point newTopLeft;
	private Point rootTopLeft;
	
	public MoveCommentCommand(Comment comment, Rectangle origBounds, Rectangle newBounds){
		if(comment == null || origBounds == null || newBounds == null)
			throw new IllegalArgumentException();
		this.comment = comment;
		origTopLeft = origBounds.getTopLeft();
		newTopLeft = newBounds.getTopLeft();
	}

	public MoveCommentCommand(Comment model, Rectangle origBounds,
			Rectangle newBounds, Point point) {
		this(model, origBounds, newBounds);
		this.rootTopLeft = point;
	}

	@Override
	public void execute(){
		moveEP();
	}

	@Override
	public void undo(){
		PointPositionedDiagramPolicy.setLoc(comment, origTopLeft);
	}

	@Override
	public void redo(){
		moveEP();
	}

	private void moveEP(){
		if (rootTopLeft != null)
			comment.setRelDistFromDiagTopLeft(new Point(rootTopLeft.x - newTopLeft.x, rootTopLeft.y - newTopLeft.y));
		PointPositionedDiagramPolicy.setLoc(comment, newTopLeft);
	}
}
