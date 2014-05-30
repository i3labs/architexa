package com.architexa.diagrams.utils;


import com.architexa.diagrams.draw2d.UnderlinableLabel;
import com.architexa.org.eclipse.draw2d.Label;

public class UILinkUtils {
	
	public static void setUnderline (Label label, boolean doUnderline) {
		if (label instanceof UnderlinableLabel) 
			((UnderlinableLabel) label).setUnderline(doUnderline);
	}
}
