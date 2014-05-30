package com.architexa.diagrams.jdt.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;

import com.architexa.collab.proxy.PluginUtils;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.IJEUtils;
import com.architexa.diagrams.jdt.RJMapToId;
import com.architexa.diagrams.utils.LRUCache;

@SuppressWarnings("restriction")
public class ASTUtil {
    private static final Logger logger = Activator.getLogger(ASTUtil.class);
    
    private static double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");

    private static LRUCache<IJavaElement, CompilationUnit> astCacheTypeRootToCU = new LRUCache<IJavaElement, CompilationUnit>(10);
    
	public static CompilationUnit getAst(IJavaElement inp) {
		if (!IJEUtils.isTypeRoot(inp)) return null;

		if (!astCacheTypeRootToCU.containsKey(inp))
			astCacheTypeRootToCU.put(inp, request_getAst(inp));
		
		return astCacheTypeRootToCU.get(inp);
	}

	public static CompilationUnit getAST(IFile file) {
		try {
			return getAst(JavaCore.createCompilationUnitFrom(file));
		} catch(Throwable t) {
			logger.warn("Problem getting compilation unit for file " + file.getName() + ". " + t.getClass());
			return null;
		}
	}
	
	public static void removeFileFromCache(IResource resource) {
		if (astCacheTypeRootToCU.isEmpty()) return;
		Set<IJavaElement> keySet = new HashSet<IJavaElement>(astCacheTypeRootToCU.keySet());
		for (IJavaElement key : keySet) {
			String keyPath = key.getResource().getName().replace(key.getResource().getFileExtension(), "");
			String rePath = resource.getName().replace(resource.getFileExtension(), "");
			if (rePath.equals(keyPath) && 
					resource.getProject().equals(key.getResource().getProject()))
				astCacheTypeRootToCU.remove(key);
		}
	}
	
	/**
	 * Calling this will reduce memory footprint. Large requesters of ASTNode's
	 * might want to call this method after their operation is finished, so that
	 * other operations can also have their caches. 
	 */
	public static void emptyCache() {
		astCacheTypeRootToCU.clear();
	}
	
	public static void emptyCache(IMember javaElem) {
		ICompilationUnit inp = javaElem.getCompilationUnit();
		emptyCache(inp);
	}

	public static void emptyCache(ICompilationUnit inp) {
		if (!IJEUtils.isTypeRoot(inp )) return;

		if (astCacheTypeRootToCU.containsKey(inp))
			astCacheTypeRootToCU.remove(inp);
	}
	
	private static CompilationUnit request_getAst(IJavaElement typeRoot) {
		logger.info("Getting AST For: " + RJMapToId.getId(typeRoot));
		
		// does: ASTProvider.getASTProvider().getAST(typeRoot, ASTProvider.WAIT_YES, null);
		// first param is IJavaElement in 3.2/3.3 and ITypeRoot in 3.4/3.5
		// 3.2 does not define ITypeRoot - so we need to Class.forName it
		// ASTProvider.WAIT_YES comes from different classes in 3.2/3.3 and 3.4/3.5
		// [also: the first param is IJavaElement in 3.3 and ITypeRoot in Eclipse 3.4]
		ASTProvider astProvider = ASTProvider.getASTProvider();
		
		// astProvider.getAST(inp, ASTProvider.WAIT_YES, null);
		
		try {
			// Part I: do a ASTProvider.WAIT_YES
			Object waitYes = getWaitYes();
			
			// Part II: call getAST
			Class<?> param1Class = IJavaElement.class;
			if (jdtUIVer >= 3.4)
				param1Class = Class.forName("org.eclipse.jdt.core.ITypeRoot");
			Method mth = astProvider.getClass().getMethod("getAST", param1Class, waitYes.getClass(), IProgressMonitor.class);
			Object result = mth.invoke(astProvider, typeRoot, waitYes, null);
			return (CompilationUnit) result;
		} catch (Throwable e) {
			logger.error("Could not get AST", e);
			return null;
		}
	}

	private static Object getWaitYes() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		// does: return ASTProvider.WAIT_YES;
		Field fld = ASTProvider.class.getField("WAIT_YES");
		return fld.get(null);
	}
}
