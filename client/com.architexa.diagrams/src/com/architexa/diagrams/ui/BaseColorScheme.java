package com.architexa.diagrams.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;

public class BaseColorScheme {
	public static final String RED = "Red";
	public static final String GREEN = "Green";
	public static final String BLUE = "Blue";
	protected static final Map<String, Color> colorMap = new HashMap<String, Color>();
	public static List<BaseColorScheme> registeredSchemes = new ArrayList<BaseColorScheme>();
	
	
	// used by each diagram to initialize colors when the theme changes
	public void reInit() {}
	
	public static Color getColorFromMap (String color) {
		return colorMap.get(color);
	}
	
	public static void registerScheme(BaseColorScheme scheme) {
		registeredSchemes.add(scheme);
	}
}
