package com.architexa.diagrams.chrono.editparts;


import org.eclipse.swt.graphics.Image;

import com.architexa.diagrams.chrono.figures.FieldFigure;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.util.FieldUtil;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.org.eclipse.draw2d.IFigure;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class FieldEditPart extends MemberEditPart {

	@Override
	protected IFigure createFigure() {
		FieldModel model = (FieldModel) getModel();
		String figureTooltip = model.getFigureToolTip();
		Image accessLevel = ImageCache.calcImageFromDescriptor(FieldUtil.getFieldIconDescriptor(model.getFieldAccess()));
		FieldFigure fieldFigure = new FieldFigure(figureTooltip, accessLevel, model.isDeclaration());
		fieldFigure.setPartner(getPartnerEP()==null?null:(FieldFigure)getPartnerEP().getFigure());
		model.setFigure(fieldFigure);
		fieldFigure.addPropertyChangeListener(this);
		fieldFigure.addMouseListener(this);
		fieldFigure.addFigureListener(this);
		return fieldFigure;
	}

}
