package com.architexa.diagrams;

import com.architexa.diagrams.ui.BaseColorScheme;

public class ColorScheme {
	// v0 - lost
	// v1 - current
	// v1a, v1b, v1c - proposals on top of v1
	public static boolean SchemeV1 = true;
	public static boolean SchemeUML = false;
	public static void setTheme(Integer theme) {
		if (theme == 0) {
			SchemeV1 = true;
			SchemeUML = false;
			for (BaseColorScheme colorScheme : BaseColorScheme.registeredSchemes ) {
				colorScheme.reInit();
			}
		} else {
			SchemeV1 = false;
			SchemeUML = true;
			for (BaseColorScheme colorScheme : BaseColorScheme.registeredSchemes ) {
				colorScheme.reInit();
			}
		}
	}

}
