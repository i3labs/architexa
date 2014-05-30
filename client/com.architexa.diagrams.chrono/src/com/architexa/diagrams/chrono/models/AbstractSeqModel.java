package com.architexa.diagrams.chrono.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class AbstractSeqModel implements IPropertySource, PropertyChangeListener {
	
	public static String PROPERTY_SET_VISIBLE = "setVisible";

	protected PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	private static final IPropertyDescriptor[] EMPTY_ARRAY = new IPropertyDescriptor[0];

	public void propertyChange(PropertyChangeEvent evt) {}

	public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
		if (l != null) listeners.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
		if (l != null) listeners.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String property, Object oldValue, Object newValue) {
		if (listeners.hasListeners(property)) 
			listeners.firePropertyChange(property, oldValue, newValue);
	}

	protected void firePropertyChange(String property, Object child) {
		if (listeners.hasListeners(property)) 
			listeners.firePropertyChange(property, null, child);
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return EMPTY_ARRAY;
	}

	public Object getEditableValue() {
		return this;
	}

	/**
	 * Children should override this.
	 */
	public void resetPropertyValue(Object id) {
		// do nothing
	}

	/**
	 * Children should override this.
	 */
	public void setPropertyValue(Object id, Object value) {
		// do nothing
	}

	/**
	 * Children should override this.
	 */
	public Object getPropertyValue(Object id) {
		return null;
	}

	/**
	 * Children should override this.
	 */
	public boolean isPropertySet(Object id) {
		return false;
	}

}
