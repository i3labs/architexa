package com.architexa.diagrams.commands;


import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.org.eclipse.gef.commands.Command;

public class AddNamedRelCommand extends Command{


	private ArtifactFragment dstArt;
	private ArtifactFragment srcArt;
	private ArtifactRel artifactRel;
	   
	public AddNamedRelCommand(ArtifactFragment srcArt, ArtifactRel artifactRel, ArtifactFragment dstArt) {
		this.srcArt =srcArt;
		this.dstArt = dstArt;
		this.artifactRel = artifactRel;
	}

	@Override
	public void execute(){
		//artifactRel.init(srcArt, dstArt, artifactRel.relationRes);
		//artifactRel.connect(srcArt, dstArt);
		
		NamedRel rel = new NamedRel();
		rel.init(srcArt, dstArt, artifactRel.getType());
		rel.connect(srcArt, dstArt);
		if (artifactRel instanceof NamedRel && ((NamedRel) artifactRel).getAnnoLabelText() !=null) {
			rel.setAnnoLabelText(((NamedRel) artifactRel).getAnnoLabelText());
		}
		artifactRel = rel;
		
	}

	@Override
	public void undo(){
		if (artifactRel!= null)
			RootArtifact.hideRel(artifactRel);          
	}
}
		
