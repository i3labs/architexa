package com.architexa.diagrams.chrono.figures;

import org.eclipse.swt.graphics.Image;

import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.ToolbarLayout;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class FieldFigure extends MemberFigure {

	public FieldFigure(String fieldName, Image accessLevel, boolean isADeclaration) {

		setToolTip(new Label(fieldName));

		ToolbarLayout layout = new ToolbarLayout(true);
		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		setLayoutManager(layout);

		IFigure fieldFig = new Label(accessLevel);
		fieldFig.setVisible(isADeclaration);
		add(fieldFig);

		getGap().setSize(getGap().getSize().width, FIELD_GAP);
	}

	@Override
	public int getType() {
		if(isVisible()) return MemberModel.declaration;
		return MemberModel.access;
	}

	@Override
	public FieldFigure getPartner() {
		return (FieldFigure) partner;
	}

	@Override
	public void setPartner(MemberFigure partner) {
		if(!(partner instanceof FieldFigure)) this.partner = null;
		else super.setPartner(partner);
	}

}
