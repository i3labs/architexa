package com.architexa.diagrams.strata.cache;

import java.io.PrintStream;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.architexa.diagrams.model.ArtifactFragment;

public class DepSourceUtils {

	public static final boolean dependencyDebugMode = false; 
	
	public static void toStream(PrintStream outStream, DepSource depSrc, Map<Integer, ArtifactFragment> depNdxToArtFrag, String msg) {
		if (!dependencyDebugMode) return;
		
		outStream.println();
		outStream.println(msg);

		// header row
		outStream.printf("|%20s|%3s|", "container", "num");
		for (int i = 0; i < depSrc.getSize(); i++) {
			outStream.printf("%3d|", i);
		}
		outStream.println();

		// for every row
		for (int row = 0; row < depSrc.getSize(); row++) {
			String str;
			if (depNdxToArtFrag.containsKey(row))
				str = truncate(depNdxToArtFrag.get(row).toString(), 20);
			else
				str = "{err:" + row + "}";
			outStream.printf("|%20s|%3d|", str, row);
			for (int col = 0; col < depSrc.getSize(); col++) {
				if (depSrc == null)
					outStream.printf("%3s|", "  ?");
				else if (row == col)
					outStream.printf("%3s|", "  .");
				else
					outStream.printf("%3d|", depSrc.getDep(depNdxToArtFrag.get(col), depNdxToArtFrag.get(row), new NullProgressMonitor(), null));	// for lower triangular matrices we print them (col,row)
			}
			outStream.println();
		}
	}


	private static String truncate(String str, int len) {
		if (str.length() > len)
			return str.substring(str.length() - len);
		else
			return str;
	}

}
