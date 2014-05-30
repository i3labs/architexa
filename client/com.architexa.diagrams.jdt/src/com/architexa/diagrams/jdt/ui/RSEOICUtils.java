package com.architexa.diagrams.jdt.ui;

import org.eclipse.swt.widgets.Composite;

public class RSEOICUtils {
	private static int savedHeight = 0;
	private static int savedWidth = 0;
	public static void loadSavedWidthAndHeight(Composite composite) {
			if (savedWidth != 0 && savedHeight!=0)
				composite.setBounds(composite.getBounds().x, composite.getBounds().y, savedWidth, savedHeight);
	}
	public static void setSavedWidthAndHeight(int w, int h) {
		savedWidth = w;
		savedHeight = h;
	}
	public static int getSavedWidth() {
		return savedWidth;
	}
	public static int getSavedHeight() {
		return savedHeight;
	}
}
