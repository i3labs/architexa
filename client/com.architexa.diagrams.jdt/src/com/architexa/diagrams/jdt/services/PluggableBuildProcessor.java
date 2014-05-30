package com.architexa.diagrams.jdt.services;

import java.util.ArrayList;
import java.util.List;

import com.architexa.diagrams.jdt.builder.AtxaRDFBuildProcessor;

public class PluggableBuildProcessor {

	private static List<AtxaRDFBuildProcessor> registeredProcessors = new ArrayList<AtxaRDFBuildProcessor>();
	private static List<AtxaRDFBuildProcessor> registeredLastProcessors = new ArrayList<AtxaRDFBuildProcessor>();

	public static void register(AtxaRDFBuildProcessor processor) {
		if (!registeredProcessors.contains(processor)) registeredProcessors.add(processor);
	}
	public static List<AtxaRDFBuildProcessor> getRegisteredProcessors() {
		List <AtxaRDFBuildProcessor> allProcessors = new ArrayList<AtxaRDFBuildProcessor>();
		allProcessors.addAll(registeredProcessors);
		allProcessors.addAll(registeredLastProcessors);
		return allProcessors;
	}
	public static void registerLast(AtxaRDFBuildProcessor processor) {
		if (!registeredLastProcessors.contains(processor)) registeredLastProcessors.add(processor);
	}
}
