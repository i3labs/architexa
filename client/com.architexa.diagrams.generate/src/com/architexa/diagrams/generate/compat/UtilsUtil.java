package com.architexa.diagrams.generate.compat;

/* 
 * Class Purpose deal with eclipse compatibility issue(s)
 *  stemming from org.eclipse.team.internal.ui.Utils
 */

import java.lang.reflect.Method;

import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.history.FileRevisionEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import com.architexa.collab.proxy.PluginUtils;


public class UtilsUtil {

	private static double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
	
	//openEditor(...) method part of org.eclipse.team.internal.ui.Utils in eclipse 3.3+
	//eclipse 3.2 no such openEditor method + internal calls to more methods not include
	//in eclipse 3.2 version 
	
	public static IEditorPart openEditor(IWorkbenchPage page, FileRevisionEditorInput revision) throws PartInitException{
		
		//for eclipse 3.3+ method exists
		if (jdtUIVer >= 3.3)
		{
			//get the method
			try {
				Method mth = Utils.class.getMethod("openEditor", IWorkbenchPage.class, FileRevisionEditorInput.class);
				return (IEditorPart) mth.invoke(null, page, revision);
			} catch (Exception e) {
				System.err.println("Issue stemming from method openEditor in Utils" + e);
			} 
		}
		
		//eclipse 3.2 unsupported -> return null
		System.err.println("openEditor(...) not available here");
		return null;
	}
}
