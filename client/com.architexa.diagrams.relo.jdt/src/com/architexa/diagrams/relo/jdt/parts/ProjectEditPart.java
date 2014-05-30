package com.architexa.diagrams.relo.jdt.parts;

import org.eclipse.jface.resource.ImageDescriptor;
import org.openrdf.model.Resource;

import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.diagrams.services.PluggableTypes.ImageDescriptorProvider;
import com.architexa.store.ReloRdfRepository;

public class ProjectEditPart implements ImageDescriptorProvider {

	public ImageDescriptor getImageDescriptor(Artifact art, Resource typeRes, ReloRdfRepository repo) {
		return ReloJDTPlugin.getImageDescriptor("/icons/prj_obj.gif");
	}

}
