package com.architexa.extensions.entjava.spring;

import org.eclipse.core.runtime.IAdaptable;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.core.model.IModelElement;

public class AdaptedBean extends Bean implements IAdaptable {

	public AdaptedBean(IModelElement parent, BeanDefinitionHolder bdHolder) {
		super(parent, bdHolder);
	}

	@Override
	public Object getAdapter(Class adapter) {
//		selJDTElelements.add(JDTSpringUtils.getJDTElementForSelectedSpringBean(sel));
		return JDTSpringUtils.getJDTElementForSelectedSpringBean(this);
	}

}
