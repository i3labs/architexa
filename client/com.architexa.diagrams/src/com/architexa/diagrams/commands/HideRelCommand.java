package com.architexa.diagrams.commands;


import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.utils.RelUtils;
import com.architexa.org.eclipse.gef.commands.Command;

public class HideRelCommand extends Command {

	private ArtifactRel rel;
	private ArtifactFragment orgDest;
	private ArtifactFragment orgSrc;

	public HideRelCommand(ArtifactRel rel) {
		super("Hide Relationship");
		this.rel = rel;
	}
	
	@Override
	public void execute(){
		orgSrc = rel.getSrc(); 
		orgDest = rel.getDest();
		hideRel((ArtifactRel) rel);
		if (orgSrc instanceof Comment)
			((Comment) orgSrc).setAnchored(false);
	}

	private void hideRel(ArtifactRel rel2) {
		RelUtils.removeModelSourceConnections(rel.getSrc(), rel);
		RelUtils.removeModelTargetConnections(rel.getDest(), rel);
	}

	@Override
	public void undo(){
		rel.init(orgSrc, orgDest, rel.relationRes);
		rel.connect(orgSrc, orgDest);
		if (orgSrc instanceof Comment)
			((Comment) orgSrc).setAnchored(true);
	}
}
