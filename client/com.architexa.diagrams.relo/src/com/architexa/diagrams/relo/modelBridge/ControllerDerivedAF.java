package com.architexa.diagrams.relo.modelBridge;

import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.DerivedArtifact;
import com.architexa.diagrams.relo.parts.ArtifactEditPart;

public abstract class ControllerDerivedAF extends DerivedArtifact {

	public ControllerDerivedAF(Artifact parentArt) {
		super(parentArt);
	}

	public abstract ArtifactEditPart createController();

}
