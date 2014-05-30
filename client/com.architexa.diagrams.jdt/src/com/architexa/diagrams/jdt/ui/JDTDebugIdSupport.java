package com.architexa.diagrams.jdt.ui;

import org.apache.log4j.Logger;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaType;

import com.architexa.diagrams.jdt.Activator;


public class JDTDebugIdSupport {
    static final Logger logger = Activator.getLogger(JDTDebugIdSupport.class);


	public static String getId(IJavaFieldVariable fieldVar) {
		try {
			return getId(fieldVar.getDeclaringType()) + "." + fieldVar.getName();
		} catch (DebugException e) {
			logger.error("Debug Exception", e);
			return "";
		}
	}
	
	public static String getId(IJavaType javaType) {
		try {
			String typeName = javaType.getName();
			// change the last '.' to a $ (our format has a '$' before every type name)
			typeName = typeName.substring(0, typeName.lastIndexOf(".")) + "$" + 
						typeName.substring(typeName.lastIndexOf(".")+1); 
			return typeName;
		} catch (DebugException e) {
			logger.error("Debug Exception", e);
			return "";
		}
	};
}
