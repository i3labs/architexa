package com.architexa.diagrams.chrono.figures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.intro.preferences.PreferenceConstants;
import com.architexa.org.eclipse.draw2d.BendpointLocator;
import com.architexa.org.eclipse.draw2d.ConnectionLocator;
import com.architexa.org.eclipse.draw2d.Figure;
import com.architexa.org.eclipse.draw2d.Graphics;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.PolygonDecoration;
import com.architexa.org.eclipse.draw2d.PolylineConnection;
import com.architexa.org.eclipse.draw2d.PositionConstants;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;
import com.architexa.org.eclipse.draw2d.geometry.PointList;



/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class ConnectionFigure extends PolylineConnection {

	public static String PROPERTY_CONNECTION_LAYOUT = "ConnectionLayout";

	public static int CALL_LINE = Graphics.LINE_SOLID;
	public static int RETURN_LINE = Graphics.LINE_DASH;
	public static int OVERRIDES_LINE = Graphics.LINE_DOT;

	public static int maxNumOfCharsToDisplay = 16;

	Figure labelContainer;
	ConnectionLocator labelLocator;

	Map<Label, String> fullNameMap = new HashMap<Label, String>();
	Map<Label, String> abbreviatedNameMap = new HashMap<Label, String>();

	public ConnectionFigure(URI type, List<Label> messagePieces) {

		boolean accessType = RJCore.calls.equals(type)||ConnectionModel.FIELD_READ.equals(type)||ConnectionModel.FIELD_WRITE.equals(type);
		int lineStyle =  accessType?CALL_LINE : RJCore.overrides.equals(type)?OVERRIDES_LINE : RETURN_LINE;
		setLineStyle(lineStyle);

		setForegroundColor(ColorScheme.connectionLine);

		PolygonDecoration arrow = new PolygonDecoration();
		arrow.setTemplate(PolygonDecoration.TRIANGLE_TIP);
		arrow.setScale(5, 2.5);
		setTargetDecoration(arrow);

		labelContainer = new Figure();
		labelContainer.setLayoutManager(new ToolbarLayout(true));
		for(Label label : messagePieces) {
			label.setForegroundColor(RJCore.overrides.equals(type) ? ColorScheme.overridesText : ColorScheme.connectionText);
			labelContainer.add(label);
		}

		for(Label label : messagePieces) fullNameMap.put(label, label.getText());
		updateAbbreviatedNameMapping();
	}

	@Override
	public void layout() {
		super.layout();
		firePropertyChange(PROPERTY_CONNECTION_LAYOUT, null, null);
	}

	@Override
	public String toString() {
		String message = "";
		for(Object child : labelContainer.getChildren()) {
			if(!(child instanceof Label)) continue;
			message = message+((Label)child).getText();
		}
		return message;
	}

	@Override
	public void setPoints(PointList points) {
		super.setPoints(points);
		addLabel();
	}

	private void addLabel() {
		if(labelLocator!=null) return;
		if(getSourceAnchor()==null || getTargetAnchor()==null) return;

		labelLocator = getLabelLocator();
		if (lineStyle == OVERRIDES_LINE)
			labelLocator.setRelativePosition(PositionConstants.NORTH_WEST);
		else	
			labelLocator.setRelativePosition(PositionConstants.NORTH_EAST);
		labelLocator.setGap(2);
		add(labelContainer, labelLocator);
	}

	private ConnectionLocator getLabelLocator() {
		if(!(getSourceAnchor().getOwner() instanceof MethodBoxFigure) || 
				!(getTargetAnchor().getOwner() instanceof MethodBoxFigure) ||
				!MemberUtil.isAnAccessToTheSameClass((MethodBoxFigure)getSourceAnchor().getOwner(), 
						(MethodBoxFigure)getTargetAnchor().getOwner())) {
			int align = lineStyle==CALL_LINE ? ConnectionLocator.SOURCE : ConnectionLocator.TARGET;
			return new ConnectionLocator(this, align);
		}
		return new BendpointLocator(this, 1);
	}

	public Figure getLabelContainer(){
		return labelContainer;
	}

	public Label getFirstLabelChild() {
		if (labelContainer == null || labelContainer.getChildren().isEmpty()) 
			return new Label();
		
		return (Label) labelContainer.getChildren().get(0); 
	}
	
	public void setFirstLabelChild(String str) {
		if (labelContainer == null)
			labelContainer = new Figure();
		
		if (labelContainer.getChildren().isEmpty()) {
			labelContainer.add(new Label(str));
			return;
		}
		Label lbl = (Label) labelContainer.getChildren().get(0);
		lbl.setText(str);
		updateFullNameMapping(lbl);
	}
	
	public String removeLabelPiece(Label labelPiece, boolean removeDot, boolean before) {
		if(!labelContainer.getChildren().contains(labelPiece)) return null;

		// Remove this piece and the dot before or after it
		labelContainer.remove(labelPiece);
		if(removeDot && labelContainer.getChildren().size()>0) {
			int indexOfDot = before ? labelContainer.getChildren().size()-1 : 0;
			labelContainer.remove((IFigure)labelContainer.getChildren().get(indexOfDot));
		}

		fullNameMap.remove(labelPiece);
		updateAbbreviatedNameMapping(); 

		return toString();
	}

	public void showFullLabelText() {
		for(Label label : fullNameMap.keySet()) label.setText(fullNameMap.get(label));
	}

	public void showAbbreviatedLabelText() {
		boolean truncate = SeqUtil.getPreferenceStore().getBoolean(PreferenceConstants.TruncateLongMethods);
		if(!truncate) {
			// only truncate label text if preference is set that way, otherwise
			// keep full method name visible in accordance with pref instead
			showFullLabelText();
			return;
		}

		for(Label label : abbreviatedNameMap.keySet()) label.setText(abbreviatedNameMap.get(label));
	}

	public void updateFullNameMapping(Label label) {
		fullNameMap.remove(label);
		fullNameMap.put(label, label.getText());
		updateAbbreviatedNameMapping();
	}

	private void updateAbbreviatedNameMapping() {
		abbreviatedNameMap.clear();

		int start = 0;
		for(Object child : labelContainer.getChildren()) {
			if(!(child instanceof Label)) continue;

			Label label = (Label) child;

			String ellipsis = maxNumOfCharsToDisplay-start < label.getText().length() ? "..." : "";
			if(start > maxNumOfCharsToDisplay) abbreviatedNameMap.put(label, "");
			else abbreviatedNameMap.put(label, label.getText().substring(0, Math.min(maxNumOfCharsToDisplay-start, label.getText().length()))+ellipsis);

			start += label.getText().length();
		}
	}

}
