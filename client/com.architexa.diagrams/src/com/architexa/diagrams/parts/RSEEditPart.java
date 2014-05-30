package com.architexa.diagrams.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.architexa.diagrams.eclipse.gef.AbstractGraphicalEditPart2;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.EditPart;

public abstract class RSEEditPart extends AbstractGraphicalEditPart2 implements PropertyChangeListener {

	@Override
	protected abstract IFigure createFigure();

	@Override
	protected abstract void createEditPolicies();

	public void propertyChange(PropertyChangeEvent evt) {
	}

	@Override
	public EditPart createChild(Object model) {
		return super.createChild(model);
	}
	@Override
	public IFigure getLayer(Object layer) {
		return super.getLayer(layer);
	}
	@Override
	public void fireRemovingChild(EditPart child, int index) {
		super.fireRemovingChild(child, index);
	}
	@Override
	public void fireChildAdded(EditPart child, int index) {
		super.fireChildAdded(child, index);
	}

	public IPropertyDescriptor[] getProperties() {
		return new IPropertyDescriptor[]{};
	}
	public Object getPropertyValue(Object id) {
		return null;
	}
	public void setPropertyValue(Object id, Object value) {
	}

}
