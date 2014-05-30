package com.architexa.diagrams.relo.ui;

import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.ui.BaseColorScheme;

public class ColorScheme extends BaseColorScheme{
	public static Color classColor;
	public static Color classShadow;
	public static Color classHdrBottom; 
	public static Color classBorder;
	
	public static Color packageColor;
	public static Color packageBorder;
	public static Color ghostBorder;
	public static Color ghostBackground;
	public static Color ghostShadow;
	
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
	
	public static void init() { 
		colorMap.put(RED, new Color(null, 242, 119, 119));
		colorMap.put(GREEN, new Color(null, 118, 245, 120));
		colorMap.put(BLUE, new Color(null, 116, 170, 232));
		
		ghostBorder = new Color(null, 200,200,200);
		ghostBackground = new Color(null, 220,220,220);
		ghostShadow = new Color(null, 240,240,240);
		if (com.architexa.diagrams.ColorScheme.SchemeV1) {
			classColor = new Color(null, 211, 220, 243);
			classShadow = new Color(null, 204, 221, 204);
			classHdrBottom  = new Color(null, 210, 219, 242);
			classBorder = new Color(null, 162, 181, 230);
		
			packageColor = new Color(null, 236, 253, 236);
			packageBorder = new Color(null, 192, 233, 192);

			highlightBackground = new Color(null, 122, 214, 208);
		} else if (com.architexa.diagrams.ColorScheme.SchemeUML) {
			classColor = new Color(null, 255, 255, 255);
			classShadow = new Color(null, 255, 255, 255);
			classHdrBottom  = new Color(null, 0, 0, 0);
			classBorder = new Color(null, 0, 0, 0);
			
			packageColor = new Color(null, 220, 220, 220);
			packageBorder = new Color(null, 0, 0, 0);
			
			ghostBackground = new Color(null, 220,220,220);
			ghostShadow = new Color(null, 240,240,240);
			
			highlightBackground = new Color(null, 122, 214, 208);
	
		}
	}
}
