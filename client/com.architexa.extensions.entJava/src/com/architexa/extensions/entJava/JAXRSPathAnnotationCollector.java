package com.architexa.extensions.entJava;

import java.util.List;

import org.objectweb.asm.AnnotationVisitor;

public class JAXRSPathAnnotationCollector implements AnnotationVisitor {
	
	private List<String> idToProcess;

	public JAXRSPathAnnotationCollector(List<String> _idToProcess) {
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
		String jaxrsId = EJCore.getId(path.replaceAll("/", "."));
		if (value.equals("/"))
			jaxrsId = path.replace("/", "$.*");
		
		idToProcess.add(jaxrsId);
	}

	public AnnotationVisitor visitAnnotation(String name, String desc) { return null; }
	public AnnotationVisitor visitArray(String name) { return null; }
	public void visitEnd() {}
	public void visitEnum(String name, String desc, String value) {}
	
}
