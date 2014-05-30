package com.architexa.diagrams.chrono.figures;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import com.architexa.diagrams.chrono.ui.ColorScheme;
/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class GroupedInstanceFigure extends InstanceFigure {

	public static int redValue = ColorScheme.groupedInstanceFigureBackground.getRed();
	public static int greenValue = ColorScheme.groupedInstanceFigureBackground.getGreen();
	public static int blueValue = ColorScheme.groupedInstanceFigureBackground.getBlue();
	public static int redDecrement = 10;
	public static int greenDecrement = 70;
	public static int blueDecrement = 10;

	public GroupedInstanceFigure(String instanceName, String className, Image icon) {
		super(instanceName, className, null, icon);
	}

	@Override
	public void highlightGroupedInstance(){
		int r = redValue - redDecrement;
		int g = greenValue - greenDecrement;
		int b = blueValue - blueDecrement;
		Color newBackgroundColor = new Color(null,r,g,b);
		iconLabel.setBackgroundColor(newBackgroundColor);
		classNameLabel.setBackgroundColor(newBackgroundColor);
		nameAndIconContainer.setBackgroundColor(newBackgroundColor);
		isGrouped = true;
	}

	@Override
	public void unHighlight(){
		iconLabel.setBackgroundColor(ColorScheme.groupedInstanceFigureBackground);
		classNameLabel.setBackgroundColor(ColorScheme.groupedInstanceFigureBackground);
		nameAndIconContainer.setBackgroundColor(ColorScheme.groupedInstanceFigureBackground);
		isGrouped = false;
	}
	
	@Override
	public void setInstanceName(String text) {
		if (text == null || text.length()==0) return;
		instanceName = text;
		instanceNameLabel.setText(instanceName);
		if (classNameLabel != null && classNameLabel.getParent() != null) {
			classNameLabel.setText("");
		}
		
	}
}
