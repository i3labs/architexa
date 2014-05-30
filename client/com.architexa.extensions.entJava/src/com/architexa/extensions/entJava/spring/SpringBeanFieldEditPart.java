package com.architexa.extensions.entJava.spring;

import org.eclipse.jface.resource.ImageDescriptor;
import org.openrdf.model.Resource;

import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.relo.jdt.parts.FieldEditPart;
import com.architexa.diagrams.relo.jdt.services.PluggableEditPartSupport;
import com.architexa.diagrams.services.PluggableTypes.ImageDescriptorProvider;
import com.architexa.extensions.entJava.Activator;
import com.architexa.store.ReloRdfRepository;

public class SpringBeanFieldEditPart extends FieldEditPart implements ImageDescriptorProvider{
    
	@Override
    public ImageDescriptor getIconDescriptor(Artifact art, Resource resType) {
    	return PluggableEditPartSupport.getIconDescriptor(getRepo(), art, resType);
    }

	public ImageDescriptor getImageDescriptor(Artifact art, Resource typeRes, ReloRdfRepository repo) {
		return Activator.getImageDescriptor("/icons/beanProperty.png");
	}
}
