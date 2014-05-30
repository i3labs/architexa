package com.architexa.diagrams.relo.modelBridge;

import java.util.Collection;

import com.architexa.diagrams.model.Artifact;

/**
 * This class tries to support the fact that ArtFrag's are not id'd by their
 * rdf resource (because we want to support collections of children, etc).
 * 
 * @author vineet
 */
public class ArtifactFragmentUtil {
	
	public static boolean removeByRDFResourceFromSet(Collection<? extends Artifact> collArt, Artifact artToRemove) {
		for (Artifact collElement : collArt) {
			if (collElement.elementRes.equals(artToRemove.elementRes))
				return collArt.remove(collElement);
		}
		return false;
	}

	
}
