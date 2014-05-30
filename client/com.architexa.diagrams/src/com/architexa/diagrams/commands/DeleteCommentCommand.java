package com.architexa.diagrams.commands;

import java.util.ArrayList;


import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.org.eclipse.gef.commands.Command;

public class DeleteCommentCommand extends Command {

	ArtifactFragment parent;
	Comment comment;
	private ArrayList<ArtifactRel> conns;

	public DeleteCommentCommand(ArtifactFragment parent, Comment comment) {
		this.parent = parent;
		this.comment = comment;
	}

	@Override
	public void execute() {
		((RootArtifact)parent).removeShownChild(comment);
		conns = new ArrayList<ArtifactRel>(comment.getTargetConnections());
		conns.addAll(new ArrayList(comment.getSourceConnections()));
		for (ArtifactRel conn : conns) {
			RootArtifact.hideRel(conn);
		}
	}

	@Override
	public void undo() {
		if (comment != null && !parent.containsChild(comment)) {
			((RootArtifact)parent).addComment(comment);
		}
		for (ArtifactRel conn : conns) {
			ArtifactFragment srcAF = (ArtifactFragment)conn.getSrc(); 
	    	ArtifactFragment tgtAF = (ArtifactFragment)conn.getDest();
	    	conn.init(srcAF, tgtAF, conn.relationRes);
	    	conn.connect(srcAF, tgtAF);
		}
	}
}
