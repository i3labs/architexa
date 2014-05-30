package com.architexa.diagrams.parts;


import com.architexa.diagrams.commands.DeleteCommentCommand;
import com.architexa.diagrams.model.Comment;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editpolicies.ComponentEditPolicy;
import com.architexa.org.eclipse.gef.requests.GroupRequest;

public class CommentComponentEditPolicy extends ComponentEditPolicy{
	@Override
	public Command createDeleteCommand(GroupRequest request) {
		Object child = getHost().getModel();
		if(child instanceof Comment){
			return new DeleteCommentCommand(((Comment) child).getParentArt(),(Comment) child);
		}
		return super.createDeleteCommand(request);
	}
}
