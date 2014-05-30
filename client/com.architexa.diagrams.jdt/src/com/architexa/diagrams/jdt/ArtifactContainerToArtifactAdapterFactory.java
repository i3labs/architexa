package com.architexa.diagrams.jdt;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdapterFactory;

import com.architexa.diagrams.model.Artifact;

public class ArtifactContainerToArtifactAdapterFactory implements IAdapterFactory {
	    private static final Logger logger = Activator.getLogger(IJaveElementContainerToIJavaElementAdapterFactory.class);

		@SuppressWarnings("unchecked")
		public Object getAdapter(Object adaptableObject, Class adapterType) {
			if (!(adaptableObject instanceof ArtifactContainer)) {
				logger.error("Adapter Requested for unsupported type: " + adaptableObject.getClass());
				return null;
			}
			if (adapterType == Artifact.class)
				return ((ArtifactContainer)adaptableObject).getContainedArtifact();

			return null;
		}

		@SuppressWarnings("unchecked")
		public Class[] getAdapterList() {
			return new Class[] {Artifact.class};
		}

	}