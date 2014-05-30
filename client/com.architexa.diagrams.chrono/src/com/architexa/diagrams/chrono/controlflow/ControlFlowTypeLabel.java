package com.architexa.diagrams.chrono.controlflow;


import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.MarginBorder;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.PointList;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class ControlFlowTypeLabel extends Figure {

	public static String IF = "if";
	public static String LOOP = "loop";
	private String controlFlowType = "";

	private static int DEFAULT_CORNER_SIZE = 5;
	private int cornerSize = DEFAULT_CORNER_SIZE;

	public ControlFlowTypeLabel(String type, String condition) {
		setBackgroundColor(ColorScheme.controlFlowTypeLabelBackground);
		setForegroundColor(ColorScheme.controlFlowTypeLabelText);
		setCornerSize(DEFAULT_CORNER_SIZE);
		setControlFlowType(type);

		setOpaque(true);
		setLayoutManager(new ToolbarLayout(true));
		setBorder(new MarginBorder(ControlFlowBlock.labelMargin, ControlFlowBlock.labelMargin, 5, 5));
		add(new Label(type));// + "  [ "+condition+" ]"));
	}

	public String getControlFlowType() {
		return controlFlowType;
	}

	public void setControlFlowType(String type) {
		controlFlowType = type;
	}

	public int getCornerSize() {
		return cornerSize;
	}

	public void setCornerSize(int newSize) {
		cornerSize = newSize;
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rect = getBounds().getCopy();

		graphics.translate(getLocation());

		PointList outline = new PointList();
		outline.addPoint(0, 0);
		outline.addPoint(rect.width - 1, 0);
		outline.addPoint(rect.width - 1, rect.height - cornerSize - 1);
		outline.addPoint(rect.width - cornerSize - 1, rect.height - 1);
		outline.addPoint(0, rect.height - 1);

		graphics.drawPolygon(outline);
		graphics.fillPolygon(outline); 

		graphics.translate(getLocation().getNegated());
	}

}
