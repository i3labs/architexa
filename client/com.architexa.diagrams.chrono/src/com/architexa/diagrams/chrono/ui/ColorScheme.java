package com.architexa.diagrams.chrono.ui;

import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.ui.BaseColorScheme;
import com.architexa.org.eclipse.draw2d.ColorConstants;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class ColorScheme extends BaseColorScheme{

	public static Color hyperlink;

	public static Color diagramBackground;

	public static Color instancePanelBackground;
	public static Color instancePanelBottomBorder;

	public static Color instanceFigureHighlightedBackground;
	public static Color instanceFigureBackground;
	public static Color instanceFigureBorder;
	public static Color instanceFigureText;
	public static Color instanceFigureMoreButtonArrows;
	public static Color lifeLine;
	
	public static Color groupedInstanceFigureBackground;
	public static Color groupedMethodDeclarationFigureBackground;

	public static Color methodInvocationFigureBackground;
	public static Color methodDeclarationFigureHighlightedBackground;
	public static Color methodDeclarationFigureBackground;
	public static Color backwardConnectionMethodBoxHighlight;
	public static Color overrideIndicatorBackground;
	public static Color noConnectionsBoxLabelText;

	public static Color connectionLine;
	public static Color connectionText;
	public static Color overridesText;
	public static Color chainedCallPieceHighlight;

	public static Color controlFlowHighlightBase;
	public static Color controlFlowHighlightBorder;
	public static Color controlFlowTypeLabelBackground;
	public static Color controlFlowTypeLabelText;
	
	public static Color debugHighlight;

	public static Color borderShadow;

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
		hyperlink = ColorConstants.blue;
		diagramBackground = ColorConstants.white;

		instancePanelBackground = new Color(null,240,240,240);
		instancePanelBottomBorder = new Color(null,240,240,240);

		instanceFigureHighlightedBackground = new Color(null, 227, 159, 41);
		
		instanceFigureBorder = new Color(null,208,208,0);
		instanceFigureText = ColorConstants.black;
		instanceFigureMoreButtonArrows = ColorConstants.listForeground;
		lifeLine = ColorConstants.gray;

		groupedInstanceFigureBackground = new Color(null, 223, 183, 103);
		groupedMethodDeclarationFigureBackground = /*FigureUtilities.darker(*/new Color(null, 96, 96, 224)/*)*/;

		methodInvocationFigureBackground = diagramBackground;
		methodDeclarationFigureHighlightedBackground = new Color(null, 127, 227, 225);
		backwardConnectionMethodBoxHighlight = new Color(null, 255, 99, 71);
		overrideIndicatorBackground = ColorConstants.lightGray;
		noConnectionsBoxLabelText = ColorConstants.black;

		connectionLine = ColorConstants.lightGray;
		connectionText = ColorConstants.black;
		overridesText = connectionLine;
		chainedCallPieceHighlight = hyperlink;

		controlFlowHighlightBase = new Color(null, 238, 216, 174);
		controlFlowHighlightBorder = ColorConstants.gray;
		controlFlowTypeLabelBackground = ColorConstants.tooltipBackground;
		controlFlowTypeLabelText = ColorConstants.black;
		
		debugHighlight = ColorConstants.yellow;

		borderShadow = new Color(null,180,180,180);
		if (com.architexa.diagrams.ColorScheme.SchemeV1) {
			methodDeclarationFigureBackground = ColorConstants.lightBlue;
			instanceFigureBackground = new Color(null, 255, 255, 166);
		} else {
			methodDeclarationFigureBackground = ColorConstants.lightGray;
			instanceFigureBackground = new Color(null, 255, 255, 255);
		}
	}
}
