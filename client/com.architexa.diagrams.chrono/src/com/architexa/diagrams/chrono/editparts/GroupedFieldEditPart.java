package com.architexa.diagrams.chrono.editparts;

import org.eclipse.swt.graphics.Image;

import com.architexa.diagrams.chrono.figures.FieldFigure;
import com.architexa.diagrams.chrono.models.GroupedFieldModel;
import com.architexa.diagrams.chrono.util.FieldUtil;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.org.eclipse.draw2d.IFigure;

public class GroupedFieldEditPart extends FieldEditPart{
	@Override
	protected IFigure createFigure() {
		GroupedFieldModel model = (GroupedFieldModel) getModel();
		String figureTooltip = model.getFigureToolTip();
		Image accessLevel = ImageCache.calcImageFromDescriptor(FieldUtil.getFieldIconDescriptor(model.getFieldAccess()));
		FieldFigure fieldFigure = new FieldFigure(figureTooltip, accessLevel, model.isDeclaration());
		fieldFigure.setPartner(getPartnerEP()==null?null:(FieldFigure)getPartnerEP().getFigure());
		fieldFigure.addPropertyChangeListener(this);
		fieldFigure.addMouseListener(this);
		fieldFigure.addFigureListener(this);
		return fieldFigure;
	}
}
