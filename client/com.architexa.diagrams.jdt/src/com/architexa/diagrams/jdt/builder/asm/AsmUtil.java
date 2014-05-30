package com.architexa.diagrams.jdt.builder.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openrdf.model.Resource;

import com.architexa.diagrams.jdt.RJCore;
import com.architexa.store.ReloRdfRepository;


public class AsmUtil {

	public static Resource createAccessModifierResource(int flags) {
		return getAccessModifierResource(flags);
	}

	/**
	 * Returns access modifier (public, private, protected, none) resource.
	 * TODO: We should also be able to references to other modifiers, especially
	 * &quot;static&quot; and &quot;synchronized&quot;.
	 * 
	 * @param flags
	 * 
	 * @return Resource
	 */
	public static Resource getAccessModifierResource(final int flags) {
		if ((flags & Opcodes.ACC_PUBLIC) != 0) {
			return RJCore.publicAccess;
		} else if ((flags & Opcodes.ACC_ENUM) != 0) {
			return RJCore.privateAccess;
		} else if ((flags & Opcodes.ACC_PROTECTED) != 0) {
			return RJCore.protectedAccess;
		} else if ((flags & Opcodes.ACC_PRIVATE) != 0) {
			return RJCore.privateAccess;
		} else {
			return RJCore.noAccess;
		}
	}

	public static String getMethodSignature(String methodName, String desc) {
		boolean isShort = false;
		StringBuilder buff = new StringBuilder(100);

		if (methodName == null) {
			isShort = true;
		} else if (methodName.equals("<clinit>")) {
			buff.append(methodName);
		} else if (methodName.endsWith("<init>")) {
		} else {
			buff.append(methodName);
		}

		buff.append("(");

		int startLen = buff.length();

		for (Type t : Type.getArgumentTypes(desc)) {
			buff.append(getShortClassname(t)).append(',');
		}

		if (isShort && (buff.length() > startLen)) {
			buff.deleteCharAt(buff.length() - 1);
		}

		buff.append(")");

		if (isShort) {
			buff.append(": ").append(Type.getReturnType(desc).getClassName());
		}

		return buff.toString();
	}

	public static String getRdfClassName(String s) {
		StringBuilder buff = quickReplace(s, "/", ".");

		// if(buff.indexOf(JAVA_LANG) == 0) {buff.delete(0,
		// JAVA_LANG.length());}

		int idx = buff.lastIndexOf(".");

		if (idx < 0) {
			buff.insert(0, "$");
		} else {
			buff.replace(idx, idx + 1, "$");
		}

		return buff.toString();
	}

	public static String getShortClassname(Type type) {
		int sort = type.getSort();

		if (sort == Type.OBJECT) {
			String s = type.getInternalName();
			int idx = s.lastIndexOf('/');
			if (idx >= 0)
				s = s.substring(idx + 1);
			
			idx = s.indexOf("$");
			if (idx >= 0)
				s = s.substring(idx + 1);

			return s;
		}

		if (sort == Type.ARRAY) {
			Type elemType = type.getElementType();
			String elem = null;

			if (elemType.getSort() == Type.OBJECT) {
				elem = getShortClassname(elemType);
			} else {
				elem = elemType.getClassName();
			}

			StringBuilder buff = new StringBuilder(elem);

			for (int i = type.getDimensions(); i > 0; i--) {
				buff.append("[]");
			}

			return buff.toString();
		}

		return type.getClassName();
	}

	public static String getShortClassname(String classname) {
		return getShortClassname(internalNameToType(classname));
	}
	
	public static Resource getClassRes(String className, ReloRdfRepository reloRdf) {
		Type classType = AsmUtil.internalNameToType(className);
		Resource classRes;
		if(AsmUtil.isPrimitive(classType)) {
			classRes = AsmUtil.toReloClassResource(reloRdf, getCleanName(classType));
		} else {
			classRes = AsmUtil.toWkspcResource(reloRdf, AsmUtil.getRdfClassName(getCleanName(classType)));
		}
		return classRes;
	}

	/**
	 * 
	 * @param methodString the method declaration's name, including parameters
	 * @param containingClass the class containing the method's declaration
	 * @param reloRdf
	 * @return an rdf Resource for the method with 
	 * the given name, declared in the given class
	 */
	public static Resource getMethodRes(String methodString, Resource containingClass,
			ReloRdfRepository reloRdf) {
		// A class's Resource.toString() will give a URI like the following,
		// where com.architexa.utils.log4j is a package, MetadataFileAppender 
		// is a top level class, and InnerMeta is an inner class
		// http://www.architexa.com/rdf/jdt-wkspc#com.architexa.utils.log4j.MetadataFileAppender$InnerMeta

		// Remove jdt workspace namespace
		String classURI = containingClass.toString();
		int packageStart = classURI.indexOf("#");
		classURI = classURI.substring(packageStart+1);

		// Now class URI should look like 
		// com.architexa.utils.log4j.MetadataFileAppender$InnerMeta
		// Append the method name to the end of it and get a Resource from it
		Resource methodRes = AsmUtil.toWkspcResource(reloRdf, 
				classURI+"."+methodString);

		return methodRes;
	}

	// If the name has a . as the last character it is removed because it
	// creates problem when trying to find the contained package name
	private static String getCleanName(Type classType) {
		String str = classType.getClassName();
		int len = str.length();
		if (str.lastIndexOf(".") != len - 1)
			return str;

		return str.substring(0, len - 1);
	}

	/**
	 * 
	 * @param internalName - the fully qualified name of an IType (source type in a compilation unit or binary type in a class file)
	 * @return the Java type corresponding to the given type descriptor
	 */
	public static Type internalNameToType(String internalName) {
		StringBuilder buff = new StringBuilder(internalName);

		// Classes starting with L in the default package already start with an
		// L so they are removed when getting the name from the type. We should
		// add the extra 'L' indicating the type in all cases 
//		if (!(internalName.startsWith("L"))) {
			buff.insert(0, 'L');
//		}

		if (!(internalName.endsWith(";"))) {
			buff.append(';');
		}

		return Type.getType(buff.toString());
	}

	public static boolean isPrimitive(Type type) {
		int sort = type.getSort();

		if (sort == Type.ARRAY) {
			sort = type.getElementType().getSort();
		}

		switch (sort) {
		case (Type.OBJECT): {
			return false;
		}

		default: {
			return true;
		}
		}
	}

	public static Resource toReloClassResource(ReloRdfRepository rdfRepo, Type type) {
		return toReloClassResource(rdfRepo, type.getClassName());
	}

	public static Resource toReloClassResource(ReloRdfRepository rdfRepo, String s) {
		return toWkspcResource(rdfRepo, getRdfClassName(s));
	}

	public static Resource toWkspcResource(ReloRdfRepository rdfRepo, String s) {
		return rdfRepo.getDefaultURI(RJCore.jdtWkspcNS, s);
	}

	public static StringBuilder quickReplace(String str, String from, String to) {
		StringBuilder sb = new StringBuilder(str);
		int idx = 0;
		while ((idx = sb.indexOf(from)) >= 0) {
			sb.replace(idx, idx + from.length(), to);
		}
		return sb;
	}
}
