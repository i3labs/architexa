package com.architexa.extensions.entJava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;

public class Struts2AnnotationCollector  implements AnnotationVisitor {
	
	private String currUrl;
	Set<String> resultJspSet = new LinkedHashSet<String>();
	Map<String, List<String>> methodToJspMap = new HashMap<String, List<String>>();
	String annotation = "";
	
	public Struts2AnnotationCollector(Map<String, List<String>> _methodToJspMap) {
		methodToJspMap = _methodToJspMap;
	}

	public void visit(String name, Object value) {
//		System.err.println("Collector: " + this);
		if (name == null || value == null) return;
		if (value.toString().contains("User"))
			"".toCharArray();
		if (name.equals("value")) {
			setCurrUrl((String) value);
		} else if (name.equals("location")) {
			resultJspSet.add((String) value);
		}
	}

	public AnnotationVisitor visitAnnotation(String name, String desc) {
//		System.err.println("Name: " + name + "\tDesc: " + desc);
		return this; 
	}
	
	public AnnotationVisitor visitArray(String name) {
//		System.err.println("Arr --- Name: " + name);
		return this; 
		}
	
	public void visitEnd() {
		methodToJspMap.put(getCurrUrl(), new ArrayList<String>(resultJspSet));
//		System.err.println("In visit End: " + methodToJspMap);
	}
	
	public void visitEnum(String name, String desc, String value) {
//		System.err.println("Name: " + name + "\tDesc: " + desc + "\n Value: " + value);
	}

	public void setCurrUrl(String currUrl) {
		this.currUrl = currUrl;
	}

	public String getCurrUrl() {
		return currUrl;
	}

}
