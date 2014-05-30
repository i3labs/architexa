/**
 * 
 */
package com.architexa.diagrams.strata.cache;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import com.architexa.diagrams.model.ArtifactFragment;

public interface DepSource {
	public int getSize();
	public int getDep(ArtifactFragment srcAF, ArtifactFragment dstAF, IProgressMonitor monitor, List<ArtifactFragment> artFragsToAdd);
}