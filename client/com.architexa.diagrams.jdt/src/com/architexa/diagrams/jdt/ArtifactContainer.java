package com.architexa.diagrams.jdt;

import com.architexa.diagrams.model.Artifact;


/**
 * Selectable items that implement this interface will be adaptable to
 * Artifact and get those actions in their context menu
 */
public interface ArtifactContainer {
	public Artifact getContainedArtifact();
}
