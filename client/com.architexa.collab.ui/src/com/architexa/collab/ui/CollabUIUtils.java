package com.architexa.collab.ui;

import java.util.Comparator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.openrdf.model.Resource;

import com.architexa.diagrams.jdt.RJCore;
import com.architexa.store.ReloRdfRepository;

public class CollabUIUtils {

	private static Comparator<String> alphabeticalComparator = new Comparator<String>() {
		public int compare(String s1, String s2) {
			return s1.compareToIgnoreCase(s2);
		}
	};

	/**
	 * @return a Comparator that uses String.compareToIgnoreCase()
	 * since the natural ordering will not ignore case, which places
	 * capital letters before lower case
	 */ 
	public static Comparator<String> getAlphabeticalIgnoringCaseComparator() {
		return alphabeticalComparator;
	}
	
	public static Comparator<String> getMethodNameComparator() {
		return new Comparator<String>() {
			public int compare(String s1, String s2) {
				String s1Meth = s1.substring(s1.lastIndexOf("."));
				String s2Meth = s2.substring(s2.lastIndexOf("."));
				return s1Meth.compareToIgnoreCase(s2Meth);
			}
		};
	}

	private static String classDiagramType = "Class Diagram";
	private static String layeredDiagramType = "Layered Diagram";
	private static String sequenceDiagramType = "Sequence Diagram";

	public static ImageDescriptor getActionIcon(String type) {
		if(classDiagramType.equals(type))
			return Activator.getImageDescriptor("icons/relo-document.png");

		if(layeredDiagramType.equals(type))
			return Activator.getImageDescriptor("icons/office-document.png");

		if(sequenceDiagramType.equals(type))
			return Activator.getImageDescriptor("icons/chrono-document.png");

		return null;
	}

	/**
	 * 
	 * @return a String with the format PackageName.ClassName.MemberName
	 * (If model is a package, the returned String will only include the
	 * PackageName portion; if model is a class, the returned String will 
	 * only include the PackageName.ClassName portion)
	 */
	public static String getResourceStringRepresentation(Resource model) {
		String s = model.toString();

		return getStringRepresentationFromResourceString(s);
//		// Remove the rdf namespace prefix
//		String commonPfx = ReloRdfRepository.atxaRdfNamespace + RJCore.jdtWkspcNS;
//		s = s.replace(commonPfx, "");
//
//		// Replace the $ClassName separator with a '.'
//		s = s.replace("$", ".");
//
//		return s;
	}
	
	
	public static String getStringRepresentationFromResourceString(String s) {
//		String s = model.toString();

		// Remove the rdf namespace prefix
//		String commonPfx = ReloRdfRepository.atxaRdfNamespace + RJCore.jdtWkspcNS;
//		s = s.replace(commonPfx, "");

		s = cleanAtxaNameSpaces(s);
		// Replace the $ClassName separator with a '.'
		s = s.replace("$", ".");

		return s;
	}
	
	public static String getClassPathStringFromMethodResourceString(String s) {
		// Remove the rdf namespace prefix
//		String commonPfx = ReloRdfRepository.atxaRdfNamespace + RJCore.jdtWkspcNS;
//		s = s.replace(commonPfx, "");
		s = cleanAtxaNameSpaces(s);
		s = s.substring(0, s.lastIndexOf("."));
		// Replace the $ClassName separator with a '.'
		s = s.replace("$", ".");

		return s;
	}
	
	public static String cleanAtxaNameSpaces(String s) {
		String commonPfx = ReloRdfRepository.atxaRdfNamespace + RJCore.jdtWkspcNS;
		return s.replace(commonPfx, "");
	}

}
