package com.architexa.diagrams.generate.compat;

/* file purpose: Compatibility issue with LocalFileRevision object
 * in accessing private member "file"
*/

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IFile;
import org.eclipse.team.internal.core.history.LocalFileRevision;

import com.architexa.collab.proxy.PluginUtils;



public class LFRUtil {
	
	private static double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
	
	//Version 3.3+ includes getFile() method in LocalFileRevision class
	//Version 3.2 No such access method, desired accessed variable named "file"
	public static IFile getFile(LocalFileRevision fi){
		
		//3.3 and above supports
		if (jdtUIVer >= 3.3 ){
			try {
				Method mth = LocalFileRevision.class.getMethod("getFile");
				return (IFile) mth.invoke(fi);
			} catch (Exception e) {
				System.err.println("Issue stemming from method getFile in LocalFileRevision: " + e);
			} 
		}
		
		//below suited for eclipse 3.2
		IFile file = null;
		try{
			Field theFile = LocalFileRevision.class.getDeclaredField("file");
			theFile.setAccessible(true);
			file = (IFile) theFile.get(fi);
		}catch(Exception NoSuchFieldException){
			System.err.println("Issue stemming from variable file in LocalFileRevision: " + NoSuchFieldException);
		}
		
		return file;
	}
}