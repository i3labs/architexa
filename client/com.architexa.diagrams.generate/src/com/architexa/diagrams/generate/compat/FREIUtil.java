package com.architexa.diagrams.generate.compat;

/* File purpose: Deal with Compatibility issues with
 * org.eclipse.team.internal.ui.history.FileRevisionEditorIn
 * Issue: CreateEditInputEditor method non-existent in 
 * eclipse 3.2
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ui.history.FileRevisionEditorInput;

import com.architexa.collab.proxy.PluginUtils;


public class FREIUtil {

	private static double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
	
	// eclipse 3.3+ createEditorInputFor included in FileRevisionEditorInput class
	// eclipse 3.2 functionality here
	public static FileRevisionEditorInput createEditorInputFor(IFileRevision revision, IProgressMonitor monitor) throws CoreException {
		
		//3.3+ call existing method
		if (jdtUIVer >= 3.3 )
		{	
			try {
				Method mth = FileRevisionEditorInput.class.getMethod("createEditorInputFor", IFileRevision.class, IProgressMonitor.class);
				return (FileRevisionEditorInput) mth.invoke(null, revision, monitor);
			} catch (Exception e) {
				System.err.println("Issue stemming from method createEditorInputFor in FileRevisionEditorIn: " + e);
			} 
		}
		
		//3.2 method duplicated here
		IStorage storage = revision.getStorage(monitor);
		//Have constructor for one parameter, force access to the private variable storage to set the other
		
		//Constructor has different parameter type outside of 3.2 
		Method mth;
		FileRevisionEditorInput input = null;
		try {
			mth = FileRevisionEditorInput.class.getMethod("FileRevisionEditorInput", IFileRevision.class);
			input = (FileRevisionEditorInput) mth.invoke(null, revision);
		} catch (Exception e) {
			System.err.println("Issue in calling constructor (FileRevisionEditorInput(IFileRevision)) " + e);
		}
		
		try{
			Field theStorage = FileRevisionEditorInput.class.getDeclaredField("storage");
			theStorage.setAccessible(true);
			theStorage.set(input, storage);
		}catch(Exception e){
			System.err.println("Issue in forcefully setting the storage variable of FileRevisionEditorInput: " + e);
		}
		
		return input; 
		
	}
}
