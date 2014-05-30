package com.architexa.diagrams.commands;

import org.openrdf.model.URI;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.org.eclipse.gef.commands.Command;

public class AddRelCommand extends Command{


	private ArtifactFragment dstArt;
	private ArtifactFragment srcArt;
	private boolean isFwdRel;
	private URI property;
	private ArtifactRel rel = null;
	   
	public AddRelCommand(ArtifactFragment srcArt, DirectedRel property, ArtifactFragment dstArt) {
		this.isFwdRel = property.isFwd;
		this.srcArt =srcArt;
		this.dstArt = dstArt;
		this.property = property.res;
	}

	@Override
	public void execute(){
	  if (isFwdRel)
          rel = RootArtifact.addRel(srcArt, property, dstArt);
      else
          rel = RootArtifact.addRel(dstArt, property, srcArt);
	}

	@Override
	public void undo(){
		if (rel!= null)
			RootArtifact.hideRel(rel);          
	}
}
		
