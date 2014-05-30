/**
 * 
 */
package com.architexa.diagrams.parts;

import org.eclipse.jface.resource.ImageDescriptor;
import org.openrdf.model.URI;

import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.org.eclipse.gef.commands.Command;

public interface BasicRootController {
    Object findEditPart(Object model);
    boolean modelCreatable(Object model);
    ImageDescriptor getIconDescriptor(NavAidsEditPart aep, Object relArt);
    void execute(Command cmd);
	RootArtifact getRootArtifact();
	boolean canAddRel(ArtifactFragment srcAF, DirectedRel rel, Artifact relArt);
    ArtifactRel addRel(ArtifactFragment srcArtFrag, URI relationRes, ArtifactFragment dstArtFrag);
	void hideRel(ArtifactRel rel);
}