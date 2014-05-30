package com.architexa.diagrams.jdt;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.core.IJavaElement;

/**
 * This allows actions that are application to IJE's to be applicable to our
 * Edit Parts.
 * 
 * TODO: We should remove the need for implementing IJavaElementContainer in the
 * edit part - we should work on any 'ArtifactFragmentEditPart' and query the
 * artifact to get the jdt element. We would also need to cache the most recent
 * jdt elements since this method might be called multiple times for each
 * action.
 */
public class IJaveElementContainerToIJavaElementAdapterFactory implements IAdapterFactory {
    private static final Logger logger = Activator.getLogger(IJaveElementContainerToIJavaElementAdapterFactory.class);

	@SuppressWarnings("unchecked")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (!(adaptableObject instanceof IJavaElementContainer)) {
			logger.error("Adapter Requested for unsupported type: " + adaptableObject.getClass());
			return null;
		}
		if (adapterType == IJavaElement.class)
			return ((IJavaElementContainer)adaptableObject).getJaveElement();

		return null;
	}

	@SuppressWarnings("unchecked")
	public Class[] getAdapterList() {
		return new Class[] {IJavaElement.class};
	}

}
