package com.architexa.extensions.entjava.spring;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IStartup;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanProperty;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataNode;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;

import com.architexa.diagrams.jdt.JDTSelectionUtils;
import com.architexa.diagrams.jdt.JDTSelectionUtils.IConverter;
import com.architexa.diagrams.jdt.RJMapFromId;
import com.architexa.diagrams.jdt.builder.asm.AsmUtil;

public class JDTSpringUtils implements IStartup{

	public static IJavaElement getJDTElementForSelectedSpringBean(Bean sel) {
		return getIJEFromID(getBeanName(sel));
	}

	private static String getBeanName(Bean sel) {
		String selBeanTxt = sel.toString();
		selBeanTxt = selBeanTxt.substring(selBeanTxt.indexOf("[")+1, selBeanTxt.indexOf("]"));

		return AsmUtil.getRdfClassName(selBeanTxt);		
	}

	public static IJavaElement getJDTElementForSelectedSpringBeanNode(BeanMetadataNode sel) {
		String className ="";
		String label = sel.getLabel();
		if (label.contains("-> ")) {
			String[] parts = label.split("-> ");
			className = parts[1];
		} else
			className = label;
		String hi = sel.getHandleIdentifier();
		if (!hi.contains("<")) {
			hi = ((JavaModelSourceLocation) sel.getLocation()).getHandleIdentifier();
		}
		String packageName = hi.substring(hi.indexOf("<")+1, hi.indexOf("{"));
		int methodBeg = className.indexOf("(");
		String id;
		if (methodBeg == -1)
			id = packageName +"$" + className;
		else
			id = packageName +"$" + className.substring(0, methodBeg );
		return getIJEFromID(id);
	}

	private static IJavaElement getIJEFromID(String id) {
		try {
			IJavaElement ije = RJMapFromId.idToJdtElement(id);
			if (ije!=null)
				return ije;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static IJavaElement getJDTElementForSelectedSpringBeanProperty(BeanProperty sel) {
		IModelElement parent = sel.getElementParent();
		
		if (parent instanceof Bean) {
			String beanPropName = getBeanName((Bean) parent) + "." + sel.getElementName();
			return getIJEFromID(beanPropName);
		}
		return null;
	}

	public void earlyStartup() {
		JDTSelectionUtils.classToConverterMap.put(Bean.class, new BeanConverter());
		JDTSelectionUtils.classToConverterMap.put(BeanMetadataNode.class, new BeanNodeConverter());
		JDTSelectionUtils.classToConverterMap.put(BeanProperty.class, new BeanPropertyConverter());
	}
	
	private class BeanConverter implements IConverter {
		public IJavaElement getJE(Object in) {
			if (in instanceof Bean)
				return JDTSpringUtils.getJDTElementForSelectedSpringBean((Bean)in);
			return null;
		}
	}
	private class BeanNodeConverter implements IConverter {
		public IJavaElement getJE(Object in) {
			if (in instanceof BeanMetadataNode)
				return JDTSpringUtils.getJDTElementForSelectedSpringBeanNode((BeanMetadataNode)in);
			return null;
		}
	}
	private class BeanPropertyConverter implements IConverter {
		public IJavaElement getJE(Object in) {
			if (in instanceof BeanProperty)
				return JDTSpringUtils.getJDTElementForSelectedSpringBeanProperty((BeanProperty)in);
			return null;
		}
	}
}
