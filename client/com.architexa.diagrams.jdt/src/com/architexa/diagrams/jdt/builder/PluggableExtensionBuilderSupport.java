package com.architexa.diagrams.jdt.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipException;

import org.eclipse.core.resources.IProject;
import org.openrdf.model.Resource;

public class PluggableExtensionBuilderSupport {

	public interface IAtxaExtensionBuilder {
		public void processProject(AtxaBuildVisitor builder, IProject resource);
		public List<Resource> processExtensionResource(AtxaBuildVisitor builder, ResourceToProcess rtp);
		// This is only used by the ClassExtensionBuilder currently
		public void autoBuildJar(String string) throws ZipException, IOException;
	}
	
	private static Map<String, Collection<IAtxaExtensionBuilder>> extToBuilders = new HashMap<String, Collection<IAtxaExtensionBuilder>>();
	
	public static void registerExtensionBuilder(String ext, IAtxaExtensionBuilder extBuilder) {
		Collection<IAtxaExtensionBuilder> currBuilders = extToBuilders.get(ext);
		if (currBuilders == null)
			currBuilders = new ArrayList<IAtxaExtensionBuilder>();
		currBuilders.add(extBuilder);
		extToBuilders.put(ext, currBuilders);
		
    	ResourceQueueManager.addExtensionForScheduling(ext);
	}

	public static Collection<IAtxaExtensionBuilder> getBuilderForExt(String ext) {
		if (ext == null)
			return null;
		else
			return extToBuilders.get(ext.toLowerCase());
	}

	public static Collection<IAtxaExtensionBuilder> getAllBuilders() {
		Collection<Collection<IAtxaExtensionBuilder>> allBuilders = extToBuilders.values();
		List<IAtxaExtensionBuilder> retBuilders = new ArrayList<IAtxaExtensionBuilder>(10);
		for (Collection<IAtxaExtensionBuilder> extBuilders : allBuilders) {
			retBuilders.addAll(extBuilders);
		}
		return retBuilders;
	}

}
