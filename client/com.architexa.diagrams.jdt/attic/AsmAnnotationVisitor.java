package com.architexa.diagrams.jdt.builder.asm;


import org.objectweb.asm.AnnotationVisitor;

import com.architexa.store.ReloRdfRepository;

/**
 * This is an empty AnnotationVisitor
 */
public class AsmAnnotationVisitor implements AnnotationVisitor {

	public AsmAnnotationVisitor(ReloRdfRepository rrr) {
	}


	public void visit(String arg0, Object arg1) {
	}

	public AnnotationVisitor visitAnnotation(String arg0, String arg1) {
		return this;
	}

	public AnnotationVisitor visitArray(String arg0) {
		return this;
	}

	public void visitEnd() {
	}

	public void visitEnum(String arg0, String arg1, String arg2) {
	}
}
