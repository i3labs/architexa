package com.architexa.diagrams.jdt.builder.asm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.openrdf.model.Resource;

import com.architexa.store.ReloRdfRepository;

public class PluggableAsmAnnotationVisitorSupport {
	
	public interface IASMAnnotationFactory {
		public AnnotationVisitor getClassVisitor(ReloRdfRepository rdfRepo, Resource classRes, String descriptor);
		public AnnotationVisitor getMethdVisitor(ReloRdfRepository rdfRepo, Resource methdRes, String descriptor);
		public void doneVisitingAnnotations(ReloRdfRepository rdfRepo, Resource projectRes, Resource res);
	};

	private static Set<IASMAnnotationFactory> registeredFactories = new HashSet<IASMAnnotationFactory>();
	public static void registerAnnotationFactory(IASMAnnotationFactory factory) {
		registeredFactories.add(factory);
	}
	public static Set<IASMAnnotationFactory> getRegisteredFactories() {
		return registeredFactories;
	}
	private static boolean vistingAnnotations = false;
	public static AnnotationVisitor getClassVisitors(ReloRdfRepository rdfRepo, Resource classRes, String descriptor) {
		vistingAnnotations = true;
		MultiAnnotationVisitorFactory mavf = new MultiAnnotationVisitorFactory();
		for (IASMAnnotationFactory factory : registeredFactories) {
			mavf.addAnnoVisitor(factory.getClassVisitor(rdfRepo, classRes, descriptor));
		}
		return mavf.getMultiAnnoVisitor();
	}
	public static AnnotationVisitor getMethdVisitors(ReloRdfRepository rdfRepo, Resource methodRes, String descriptor) {
		vistingAnnotations = true;
		MultiAnnotationVisitorFactory mavf = new MultiAnnotationVisitorFactory();
		for (IASMAnnotationFactory factory : registeredFactories) {
			mavf.addAnnoVisitor(factory.getMethdVisitor(rdfRepo, methodRes, descriptor));
		}
		return mavf.getMultiAnnoVisitor();
	}
	public static void doneVisitingAnnotations(ReloRdfRepository rdfRepo, Resource projectRes, Resource res) {
		if (!vistingAnnotations) {
			return;
		}
		vistingAnnotations = false;
		for (IASMAnnotationFactory factory : registeredFactories) {
			factory.doneVisitingAnnotations(rdfRepo, projectRes, res);
		}
	}

	
	private static class MultiAnnotationVisitor implements AnnotationVisitor {
		private final List<AnnotationVisitor> visitors;
		public MultiAnnotationVisitor(List<AnnotationVisitor> _visitors) {
			this.visitors = _visitors;
		}

		public void visit(String name, Object value) {
			for (AnnotationVisitor visitor : visitors) {
				visitor.visit(name, value);
			}
		}

		public AnnotationVisitor visitAnnotation(String name, String desc) {
			MultiAnnotationVisitorFactory mavf = new MultiAnnotationVisitorFactory();
			for (AnnotationVisitor visitor : visitors) {
				mavf.addAnnoVisitor(visitor.visitAnnotation(name, desc));
			}
			return mavf.getMultiAnnoVisitor();
		}

		public AnnotationVisitor visitArray(String name) {
			MultiAnnotationVisitorFactory mavf = new MultiAnnotationVisitorFactory();
			for (AnnotationVisitor visitor : visitors) {
				mavf.addAnnoVisitor(visitor.visitArray(name));
			}
			return mavf.getMultiAnnoVisitor();
		}

		public void visitEnd() {
			for (AnnotationVisitor visitor : visitors) {
				visitor.visitEnd();
			}
		}

		public void visitEnum(String name, String desc, String value) {
			for (AnnotationVisitor visitor : visitors) {
				visitor.visitEnum(name, desc, value);
			}
		}
		
	}
	public static class MultiAnnotationVisitorFactory {
		List<AnnotationVisitor> visitors = new ArrayList<AnnotationVisitor>();
		public void addAnnoVisitor(AnnotationVisitor annoVisitor) {
			if (annoVisitor != null) visitors.add(annoVisitor);
		}
		public AnnotationVisitor getMultiAnnoVisitor() {
			if (visitors.isEmpty()) 
				return null;
			else
				return new MultiAnnotationVisitor(visitors);
		}
		
	}

}
