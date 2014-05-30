package com.architexa.extensions.entJava.spring;

import java.util.List;

import org.objectweb.asm.AnnotationVisitor;

import com.architexa.extensions.entJava.EJCore;

public class SWFAnnotationController implements AnnotationVisitor{
	private List<String> idToProcess;

	public SWFAnnotationController(List<String> _idToProcess) {
		this.idToProcess = _idToProcess;
	}

	public void visit(String name, Object value) {
		String path = EJCore.WEBROOT;

		if (!((String)value).startsWith("/")) path += "/";
		path += value;

		// remove variable parens
		path = path.replace("{", "");
		path = path.replace("}", "");
		
		// if we are the session controller
		path = path.replaceAll("\\.", "\\*");
		String jaxrsId = EJCore.getId(path.replaceAll("/", "."));
		jaxrsId = jaxrsId.replaceAll("\\*", "\\.");
		if (value.equals("/"))
			jaxrsId = path.replace("/", "$.*");
		
		idToProcess.add(jaxrsId);
	}

	public AnnotationVisitor visitAnnotation(String name, String desc) {
		return this; 
	}
	
	public AnnotationVisitor visitArray(String name) {
		return this; 
		}
	
	public void visitEnd() {}
	public void visitEnum(String name, String desc, String value) {}
}
