package com.architexa.diagrams.jdt.utils;

/*
 * Class Purpose: Compatibility issue with ITextFileBufferManager method connect(...) 
 * 
 */


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import com.architexa.collab.proxy.PluginUtils;
import com.architexa.diagrams.jdt.compat.ASTUtil;


public class ITFBMUtil {

	private static double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
	
	//connect(...) in IFileBufferManager class, Eclipse 3.3+ use LocationKind argument
	//Eclipse 3.2: LocationKind is undefined, method
	@SuppressWarnings("deprecation")
	public static void connectIFILE(ITextFileBufferManager manage, IPath location, IProgressMonitor monitor) {
		
		if(jdtUIVer >= 3.3){
			//Location class exists in this version, so need it as well as to call the better available connect
			//method using it
			try{
				Class<?> locClass = Class.forName("org.eclipse.core.filebuffers.LocationKind");
				Method mth = manage.getClass().getMethod("connect", IPath.class, locClass, IProgressMonitor.class);
				Field theLocType = locClass.getDeclaredField("IFILE");
				mth.invoke(manage, location, theLocType.get(null), monitor);
				
			}catch(Throwable e){
				System.err.println("Reflection based issue occured" + e);
			}
			return;
		}
		//eclipse 3.2 method still included as of eclipse 3.5
		try {
			manage.connect(location, monitor);
		} catch (CoreException e) {
			System.err.println("Issue: " + e);
		}
	}
	
	//Compatibility issue calling getTextFileBuffer from ITextFileBufferManager Class
	//eclipse 3.2 LocationKind is undefined
	@SuppressWarnings("deprecation")
	public static ITextFileBuffer getTextFileBufferIFILE(ITextFileBufferManager manage, IPath location) {
		if(jdtUIVer >= 3.3){
			//Location class exists in this version, so need to cast locKind to it for method invocation
			try{
				Class<?> locClass = Class.forName("org.eclipse.core.filebuffers.LocationKind");
				Method mth = manage.getClass().getMethod("getTextFileBuffer", IPath.class, locClass);
				Field theLocType = locClass.getDeclaredField("IFILE");
				theLocType.setAccessible(true);
				return (ITextFileBuffer) mth.invoke(manage, location, theLocType.get(null));
				
			}catch(Throwable e){
				System.err.println("Issue with reflection: " + e);
			}
		}
		//eclipse 3.2 method still included as of eclipse 3.5
		return manage.getTextFileBuffer(location);
	}
	
	static public IDocument getCurrentFileDocument(IFile file) {
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		ITFBMUtil.connectIFILE(bufferManager, file.getFullPath(), null);
		final ITextFileBuffer currentFileBuffer = ITFBMUtil.getTextFileBufferIFILE(bufferManager, file.getFullPath());
		IDocument currentFileDoc = currentFileBuffer.getDocument();
		if(currentFileDoc==null) {
			CompilationUnit currentCU = ASTUtil.getAST(file);
			currentFileDoc = new Document(currentCU.toString());
		}
		return currentFileDoc;
	}
	
}
