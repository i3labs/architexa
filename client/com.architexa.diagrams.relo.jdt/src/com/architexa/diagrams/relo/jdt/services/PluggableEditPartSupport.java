package com.architexa.diagrams.relo.jdt.services;

import org.apache.log4j.Logger;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;
import com.architexa.diagrams.relo.parts.MoreItemsEditPart;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.diagrams.services.PluggableTypes.ImageDescriptorProvider;
import com.architexa.diagrams.services.PluggableTypes.PluggableTypeInfo;
import com.architexa.store.ReloRdfRepository;

public class PluggableEditPartSupport {
	static final Logger logger = ReloJDTPlugin.getLogger(PluggableEditPartSupport.class);
	
	public static class DefaultImageDescriptorProvider implements ImageDescriptorProvider {

		public ImageDescriptor getImageDescriptor(Artifact art, Resource typeRes, ReloRdfRepository repo) {
			ImageDescriptor desc = CodeUnit.getIconDescriptor(repo, art, typeRes);
			
			if (desc == null) {
		    	// see if there is another EP to render it
		        MoreItemsEditPart cuepInst = (MoreItemsEditPart) PluggableTypes.getController(repo, art.elementRes, typeRes);
		        if (cuepInst != null) return cuepInst.getIconDescriptor(art, typeRes);
		    	
			    logger.error("IconDesc requested for unknown type: " + typeRes, new Exception());
			    desc = CodeUnit.getImageDescriptorFromKey(ISharedImages.IMG_FIELD_PRIVATE);
			}
			return desc;
		}
		
	}
	public static DefaultImageDescriptorProvider defaultCodeUnitIconProvider = new DefaultImageDescriptorProvider();
	
	
	public static ImageDescriptor getIconDescriptor(ReloRdfRepository repo, Artifact art, Resource typeRes) {
		if("<clinit>".equals(art.queryName(repo))) {
			// Wrap in a JavaElementImageDescriptor since it sets the
			// size, which will prevent ugly stretching of images in menus
			ImageDescriptor desc = Activator.getImageDescriptor("icons/static_initializer.png");
			return new JavaElementImageDescriptor(desc, 0, JavaElementImageProvider.BIG_SIZE);
		}

		ImageDescriptor pluggableIcon = guessFromPluggableTypes(art, typeRes, repo);
		
		// check for icons for items without a pluggable type
		if (pluggableIcon == null)
			pluggableIcon = defaultCodeUnitIconProvider.getImageDescriptor(art, typeRes, repo);
		
		return pluggableIcon;
			
	}
	
	private static ImageDescriptor guessFromPluggableTypes(Artifact art, Resource typeRes, ReloRdfRepository repo) {
		PluggableTypeInfo pti = PluggableTypes.getRegisteredTypeInfo(art.elementRes, repo, (URI) typeRes);
		if (pti != null)
			return pti.iconProvider.getImageDescriptor(art, typeRes, repo);
		else
			return null;
	}

}
