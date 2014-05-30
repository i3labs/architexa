package com.architexa.diagrams.strata.ui;

import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.ui.BaseColorScheme;

public class ColorScheme extends BaseColorScheme{

	public static Color[] containerColors;
	public static Color highlightBackground;
	
//	public static final String RED = "Red";
//	public static final String GREEN = "Green";
//	public static final String BLUE = "Blue";
//	public static final Map<String, Color> colorMap = new HashMap<String, Color>();
//	
//	public static Color getColorFromMap (String color) {
//		return colorMap.get(color);
//	}
	static {
		init();
	}
	@Override
	public void reInit() {
		ColorScheme.init();
	}
	public static void init() { if (com.architexa.diagrams.ColorScheme.SchemeV1) {
		setupMap();
		containerColors = new Color[] {
			new Color(null, 255, 255, 255),
			new Color(null, 222, 222, 255),
			new Color(null, 230, 208, 187),
			new Color(null, 191, 193, 219),
			new Color(null, 214, 184, 203),
			new Color(null, 191, 222, 204),
			new Color(null, 233, 229, 211),
			new Color(null, 222, 222, 255),
			new Color(null, 230, 208, 187),
			new Color(null, 235, 193, 219)
			};
			highlightBackground = new Color(null, 122, 214, 208);
		} else {
			containerColors = new Color[] {
					new Color(null, 255, 255, 255),
					new Color(null, 255, 255, 255),
					new Color(null, 255, 255, 255),
					new Color(null, 255, 255, 255),
					new Color(null, 255, 255, 255),
					new Color(null, 255, 255, 255),
					new Color(null, 255, 255, 255),
					new Color(null, 255, 255, 255),
					new Color(null, 255, 255, 255),
					new Color(null, 255, 255, 255),
					};
			
		}
	}
	
	private static void setupMap() {
		colorMap.put(RED, new Color(null, 242, 119, 119));
		colorMap.put(GREEN, new Color(null, 118, 245, 120));
		colorMap.put(BLUE, new Color(null, 116, 170, 232));
	}
	//static {
	//	// colors originally from LDM
	//	// darken everything since we don't have borders
	//	for (int i = 1; i < containerColors.length; i++) {
	//		//containerColor[i] = darken(containerColor[i], 0.75);
	//		//containerColor[i] = darken(containerColor[i], 0.8);
	//        containerColors[i] = ColorUtilities.darken(ColorScheme.containerColors[i]);
	//	}
	//}

}
