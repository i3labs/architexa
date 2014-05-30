/**
 * 
 */
package com.architexa.diagrams.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import com.architexa.diagrams.Activator;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.store.ReloRdfRepository;



public class PluggableTypes {
    static final Logger logger = Activator.getLogger(PluggableTypes.class);

    public interface ImageDescriptorProvider {
    	public ImageDescriptor getImageDescriptor(Artifact art, Resource typeRes, ReloRdfRepository repo);
    }
    
    public static class PluggableTypeInfo {
		public final Resource type;
		public final String label;
		public final Class<?> model;
		public final Class<? extends EditPart> reloController;

		// not in controller because we don't want to instantiate the whole controller just to get the icon
		public ImageDescriptorProvider iconProvider = null;

		// Graph Node and Container from the perspective of Relo
		public boolean isGraphContainer = false;
		public boolean isGraphNode = false;
		
		// Used for rels, etc that do not need icons
		public PluggableTypeInfo(Resource type, String label, Class<?> model, Class<? extends EditPart> reloController) {
			this.type = type;
			this.label = label;
			this.model = model;
			this.reloController = reloController;
		}
		
		public PluggableTypeInfo(Resource type, String label, Class<?> model, Class<? extends EditPart> reloController, ImageDescriptorProvider iconProvider) {
			this(type, label, model,reloController);
			this.iconProvider = iconProvider;
		}
	}
	
    public static class PluggableFilterInfo extends PluggableTypeInfo {
		public final URI property;
		public final Value value;
		public PluggableFilterInfo(Resource type, String label, Class<?> model, Class<? extends EditPart> reloController, URI property, URI truestatement, ImageDescriptorProvider iconProvider) {
			super(type, label, model, reloController, iconProvider);
			this.property = property;
			this.value = truestatement;
		}
	}
    
    
	private static Map<Resource, List<PluggableTypeInfo>> registeredTypeToPluggableInfo = new HashMap<Resource, List<PluggableTypeInfo>>();
	
	public static PluggableTypeInfo registerType(PluggableTypeInfo pti) {
		List<PluggableTypeInfo> ptiList = registeredTypeToPluggableInfo.get(pti.type);
		if (ptiList!=null && !ptiList.isEmpty())
			ptiList.add(pti);
		else {
			List<PluggableTypeInfo> newPTIList = new ArrayList<PluggableTypeInfo>();
			newPTIList.add(pti);
			registeredTypeToPluggableInfo.put(pti.type, newPTIList);
		}
		return pti;
	}
	public static List<PluggableTypeInfo> getRegisteredTypeInfo(Resource type) {
		return registeredTypeToPluggableInfo.get(type);
	}
	
	public static boolean isPluggableAF(Resource type) {
		List<PluggableTypeInfo> ptiList = registeredTypeToPluggableInfo.get(type);
		for (PluggableTypeInfo pti : ptiList) {
			if (pti == null || pti.model == null) return false;
			if (ArtifactFragment.class.isAssignableFrom(pti.model))
				return true;
		}
		return false;
	}
	public static ArtifactFragment getAF(Resource type) {
		List<PluggableTypeInfo> ptiList = registeredTypeToPluggableInfo.get(type);
		if (ptiList == null) return null;
		for (PluggableTypeInfo pti : ptiList) {
			if (pti == null || pti.model == null) return null;
			try {
				if (ArtifactFragment.class.isAssignableFrom(pti.model)) return (ArtifactFragment) pti.model.newInstance();
			} catch (Throwable e) {
				logger.error("Unexpected Error", e);
			}
		}
		return null;
	}
	public static EditPart getController(ReloRdfRepository repo, Resource res, Resource type) {
		Class<?> defaultController = null;
		try {
			PluggableTypeInfo pti = getRegisteredTypeInfo(res, repo, (URI) type);
			defaultController = pti.reloController;
			if (defaultController != null)
				return (EditPart) defaultController.newInstance();
		} catch (Throwable e) {
			logger.error("Unexpected Error", e);
		}
		return null;
	}
	public static ArtifactRel getAR(Resource type) {
		List<PluggableTypeInfo> ptiList = registeredTypeToPluggableInfo.get(type);
		if (ptiList == null) return null;
		for (PluggableTypeInfo pti : ptiList) {
			if (pti == null || pti.model == null) return null;
			try {
				if (ArtifactRel.class.isAssignableFrom(pti.model)) return (ArtifactRel) pti.model.newInstance();
			} catch (Throwable e) {
				logger.error("Unexpected Error", e);
			}
		}
		return null;
	}

	public static PluggableTypeInfo getRegisteredTypeInfo(Resource res, ReloRdfRepository repo, URI relType) {
    	PluggableTypeInfo retVal = null;
    	List<PluggableTypeInfo> ptis = PluggableTypes.getRegisteredTypeInfo(relType);
    	if (ptis == null) return null;
    	for (PluggableTypeInfo pti : ptis) {
    		if (!(pti instanceof PluggableFilterInfo)) {
    			// not a filter type - use this as the default
				retVal = pti;
				continue;
    		}
    		
    		// check if this is a valid filter, if yes, return it
    		PluggableFilterInfo pfi = (PluggableFilterInfo) pti;
    		if (repo.hasStatement(res, pfi.property, pfi.value))
    			return pti;
    	}
    	return retVal;
    }
}