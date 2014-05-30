package com.architexa.diagrams.draw2d;

import org.eclipse.swt.graphics.Color;

public class ColorUtilities {

	public static Color darken(Color base) {
		return darken(base, 0.9);
	}

	public static Color darken(Color base, double ratio) {
	    float hsb[] = ColorToHSB(base);
	    hsb[2] = (float) Math.min(1.0, (float) (hsb[2]*ratio));
	    return HSBtoColor(hsb);
	}
	
	public static float[] ColorToHSB(Color srcColor) {
	    float hsb[] = new float[3];
	    java.awt.Color.RGBtoHSB(srcColor.getRed(), srcColor.getGreen(), srcColor.getBlue(), hsb);
	    return hsb;
	}
	
	public static Color HSBtoColor(float[] srcHSB) {
	    java.awt.Color javaAwtColor = new java.awt.Color(java.awt.Color.HSBtoRGB(srcHSB[0], srcHSB[1], srcHSB[2]));
	    return new Color(null, javaAwtColor.getRed(), javaAwtColor.getGreen(), javaAwtColor.getBlue());
	}

}
