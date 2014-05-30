package com.architexa.diagrams.ui;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.IFigure;

public class FontCache {
	
	private static String segoe = "Segoe UI";
	public static Font font10 = new Font(null, segoe, 10, 0);
	public static Font font12 = new Font(null, segoe, 12, 0);
	public static Font font14 = new Font(null, segoe, 14, 0);
	public static Font font16 = new Font(null, segoe, 16, 0);
	public static Font font18 = new Font(null, segoe, 18, 0);
	public static Font font20 = new Font(null, segoe, 20, 0);
	public static Font font22 = new Font(null, segoe, 22, 0);
	public static Font font24 = new Font(null, segoe, 24, 0);
	
	public static Font font10Bold = new Font(null, "", 10, SWT.BOLD);
	public static Font fontArial10Bold = new Font(null, "Arial", 10, SWT.BOLD);
	private static FontData[] dialogFontData = JFaceResources.getDialogFont().getFontData();
	public static Font dialogFont = new Font(null, dialogFontData);
	
	public static Font dialogFontBold = new Font(null, JFaceResources.getDialogFont().getFontData()[0].getName(), JFaceResources.getDialogFont().getFontData()[0].getHeight(), SWT.BOLD);
	
	public static Font getFontForRatio(double sizeRatio) {
		if (sizeRatio <= 1.1)
			return font10;
		else if (sizeRatio <= 1.3)
			return font12;
		else if (sizeRatio <= 1.4)
			return font14;
		else if (sizeRatio <= 1.6)
			return font16;
		else if (sizeRatio <= 1.7)
			return font18;
		else if (sizeRatio <= 1.9)
			return font20;
		else if (sizeRatio <= 2)
			return font22;
		else if (sizeRatio > 2)
			return font24;
		else
			return font12;
	}
	
	public static void instructionHighlight(IFigure _emptyContent, int fontMag) {
		_emptyContent.setForegroundColor(ColorConstants.darkGray);
		FontData fontData = new FontData();
		fontData.setHeight(fontMag);
		fontData.setStyle(SWT.BOLD);
		Font newFont = new Font(_emptyContent.getForegroundColor().getDevice(), fontData);
		_emptyContent.setFont(newFont);
	}
}
