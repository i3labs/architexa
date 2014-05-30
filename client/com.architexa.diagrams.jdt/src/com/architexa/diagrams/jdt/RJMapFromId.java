package com.architexa.diagrams.jdt;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.openrdf.model.Resource;

import com.architexa.diagrams.jdt.compat.ASTUtil;
import com.architexa.diagrams.jdt.utils.MethodInvocationFinder;
import com.architexa.store.StoreUtil;

public class RJMapFromId {
    private static final Logger logger = Activator.getLogger(RJMapFromId.class);

    private static List<String> nullJavaElemList = new ArrayList<String>();
    public static IJavaElement idToJdtElement(String id) throws JavaModelException {
    	return idToJdtElement(id, null);
    }
    
    
    /**
     * @param id
     * @param srcFldr
     * 	srcFldr is needed when preping the buildQueue since 
     * we may have to create IJE for specific src fldrs. Typically this will be null
     * @return
     * @throws JavaModelException
     */
    public static IJavaElement idToJdtElement(String id, String srcFldr) throws JavaModelException {
    	if (srcFldr==null && nullJavaElemList.contains(id))
    		return null;
    	
        int pckgEndSep = id.indexOf("$");
        
        // get package
        String pkgName;
        if (pckgEndSep != -1)
            pkgName = id.substring(0, pckgEndSep);
        else if (id.endsWith(".*"))  // account for Parent packages 
        	pkgName = id.replace(".*", "");
        else
            pkgName = id;
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = root.getProjects();
        for (int i=0;i<projects.length; i++) {
            IJavaProject project = null;
            try {
                project = JavaCore.create(projects[i]);
            } catch (IllegalArgumentException iae) {}
            if (project == null || !project.exists()) continue;

            IPackageFragmentRoot[] pfrs = project.getAllPackageFragmentRoots();
            for (int j=0; j<pfrs.length; j++) {
                IPackageFragment pckgFgmt = pfrs[j].getPackageFragment(pkgName);
                if (!pckgFgmt.exists()) continue;
                
                
                // only happens during build
                if (srcFldr!=null) { //check for a specific srcFolder
					String packageFolderRootName = pfrs[j].getPath().toString().replace("/"+project.getElementName()+"/", "");
					if (srcFldr.equals(packageFolderRootName)) {
						IJavaElement pckgChild = getPackageChildJavaElement(pckgFgmt, id, pckgEndSep);
	                    if (pckgChild != null)
	                        return pckgChild;
					}
					continue;
                }
                
                if (pckgEndSep == -1) {
                    // requesting package
                    //logger.info("Converting: " + id + " --> " + tgtPckgFgmt.getElementName());
                    return pckgFgmt;
                } else {
                    IJavaElement pckgChild = getPackageChildJavaElement(pckgFgmt, id, pckgEndSep);
                    if (pckgChild != null)
                        return pckgChild;
                    else
                        pckgFgmt = null; // get next tgtPckgFgmt
                }
            }
        }
        
        nullJavaElemList.add(id);
        // not found
        logger.warn("Could not find java element for id: " + id);
        return null;
    }
    
    public static void emptyCache() {
    	nullJavaElemList.clear();
    }
    
    private static IJavaElement getPackageChildJavaElement(IPackageFragment tgtPckgFgmt, String id, int typesBegSep) throws JavaModelException {
        //logger.info("Searching for: " + id + " in: " + tgtPckgFgmt.getElementName());
        String idTypeNames = null;
        IType tgtType = null;
        int typesEndSep = id.indexOf(".", id.lastIndexOf("$"));
        if (typesEndSep == -1)
            idTypeNames = id.substring(typesBegSep);
        else
            idTypeNames = id.substring(typesBegSep, typesEndSep);

        StringTokenizer typeTokenizer = new StringTokenizer(idTypeNames, "$");
        while (typeTokenizer.hasMoreTokens()) {
            String typeName = typeTokenizer.nextToken();
            if (tgtType == null) {
                tgtType = tgtPckgFgmt.getCompilationUnit(typeName + ".java").findPrimaryType();
            	if (tgtType == null) {
            		// type is likely nested class somewhere in this package, go through them all
            		for (ICompilationUnit srchCU : tgtPckgFgmt.getCompilationUnits()) {
            			for (IType potType : srchCU.getTypes()) {
							if (potType.getElementName().equals(typeName)) tgtType = potType;
	            			if (tgtType != null) break;
						}
            			if (tgtType != null) break;
					}
            	}
            } else {
            	// inner classes can be anonymous
                if (Character.isDigit(typeName.charAt(0)) && !typeName.contains("."))
                	tgtType = getAnonType(tgtType, Integer.parseInt(typeName.substring(0, 1)));	
                else
                	tgtType = tgtType.getType(typeName);
            }
        }

        // tgtType == null also when in the wrong project (that depends on the right one)
		//if (tgtType == null) {
		//	logger.error("Cannot find class: " + id + " :: Package: " + tgtPckgFgmt.getHandleIdentifier() + " // type names: " + idTypeNames);
		//	tgtType = tgtPckgFgmt.getClassFile(idTypeNames.substring(1) + ".class").getType();
		//}

        if (tgtType == null || typesEndSep == -1) {
            // logger.info("Converting: " + id + " --> " + tgtPckgFgmt.getElementName() + "//" + tgtType.getElementName());
            return tgtType;
        }
        
        // logger.info("Converting: " + id + " --> " + tgtPckgFgmt.getElementName() + "//" + tgtType.getElementName() + " ...");

        if (id.contains("<clinit>") && tgtType.getInitializers().length>0) {
        	// static or instance initializer
        	// TODO Currently we only show a single initializer item
        	// in a diagram, whose nav aid contains all calls made
        	// by all initializers in the class. Until we show
        	// multiple initializers separately, simply returning
        	// the initializer that appears first in the source.
        	return tgtType.getInitializers()[0];
        }
        
        // get member
        int methSep = id.indexOf("(");
        if (methSep == -1) {
            // field
            return tgtType.getField(id.substring(typesEndSep + 1, id.length()));
        } else {
            // method
            return getMethodJavaElement(tgtType, id, typesEndSep, methSep);
        }

        //return null;
    }

	/**
	 * Given a parent class and the count associated with it, this method finds
	 * the appropriate anon inner class.
	 * 
	 * This is done by going through all the fields first and then the methods
	 * of the given parent class, and counting the anon classes till we get our
	 * count.
	 */
 	private static IType getAnonType(IType parentType, int parentAnonClassCnt) throws JavaModelException {
     	for (IField fld : parentType.getFields()) {
 			for (IJavaElement fldChild : fld.getChildren()) {
 				if (fldChild instanceof IType && ((IType)fldChild).isAnonymous()) {
 					parentAnonClassCnt--;
 					if (parentAnonClassCnt == 0) return (IType) fldChild;
 				}
 			}
 		}
     	for (IMethod meth : parentType.getMethods()) {
 			for (IJavaElement methChild : meth.getChildren()) {
 				if (methChild instanceof IType && ((IType)methChild).isAnonymous()) {
 					parentAnonClassCnt--;
 					if (parentAnonClassCnt == 0) return (IType) methChild;
 				}
 			}
 		}
     	for (IInitializer init : parentType.getInitializers()) {
     		for (IJavaElement initChild : init.getChildren()) {
     			if (initChild instanceof IType && ((IType)initChild).isAnonymous()) {
     				parentAnonClassCnt--;
     				if (parentAnonClassCnt == 0) return (IType) initChild;
     			}
     		}
     	}
     	logger.error("Error finding anonymous class. Looking for count: " + parentAnonClassCnt + " in type: " + parentType.getHandleIdentifier());
     	return null;
 	}

 	private static IJavaElement getMethodJavaElement(IType tgtType, String id, int typesEndSep, int methSep) throws JavaModelException {
        //logger.info("Searching for: " + id + " in: " + tgtType.getElementName());
        String methName = id.substring(typesEndSep + 1, methSep);
        IMethod[] meth = tgtType.getMethods();
        for (int i = 0; i < meth.length; i++) {
        	if (!meth[i].getElementName().equals(methName)) continue;
            
            String[] paramTypes = meth[i].getParameterTypes();
            int paramCnt = 0;
            StringTokenizer methParamTypesTokenizer = new StringTokenizer(id.substring(methSep), "(),");

            // for nested constructors not declared within a static member, the first token
            // (in the byte code, and our format) is a hidden reference to the parent class
            if (IJEUtils.isNestedType(tgtType) 
            		&& methName.equals(tgtType.getElementName())
            		&& !RJCore.isStatic(tgtType))
            	methParamTypesTokenizer.nextToken();

            while (methParamTypesTokenizer.hasMoreTokens()) {
                if (paramCnt>=paramTypes.length) break;
                String idTypeName = methParamTypesTokenizer.nextToken();
                String elemTypeName = Signature.getTypeErasure(Signature.getSignatureSimpleName(paramTypes[paramCnt]));
                if (!idTypeName.equals(elemTypeName)) break;
                paramCnt++;
            }
            if (paramCnt == paramTypes.length && !methParamTypesTokenizer.hasMoreTokens()) return meth[i];
        }

        // Also checking whether method is compiler supplied default constructor
        StringTokenizer methParamTypesTokenizer = new StringTokenizer(id.substring(methSep), "(),");
        // for nested constructors not declared within a static or enum member, the first 
        // token (in the byte code, and our format) is a hidden reference to the parent class
        if (IJEUtils.isNestedType(tgtType) && 
        		!RJCore.isStatic(tgtType) && !tgtType.isEnum() && 
        		methParamTypesTokenizer.hasMoreTokens()) // test for this to avoid nextToken()
        	// causing an exception if there are no more tokens, but shouldn't happen 
        	// unless there's case(s) besides static and enum that we're not considering
        	methParamTypesTokenizer.nextToken();
        boolean hasNoParameters = !methParamTypesTokenizer.hasMoreTokens();
        if(tgtType.getElementName().equals(methName) && hasNoParameters) {
        	return new CompilerGeneratedDefaultConstructor(methName, tgtType);
        }
        
        // Check for anonymous class constructor
        if (tgtType.isAnonymous()) {
			ICompilationUnit parentMethod = tgtType.getCompilationUnit();
			ASTNode parentNode = ASTUtil.getAst(parentMethod);
			MethodInvocationFinder invocFinder = new MethodInvocationFinder(parentNode, true);
			Resource res = RJCore.idToResource(StoreUtil.getDefaultStoreRepository(), id);
			for (Invocation invocation : invocFinder.getAllInvocations()) {
				Resource invocRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), invocation.getMethodElement());
				if (res.equals(invocRes)) 
					return invocation.getMethodElement();
			}
		}
        
        return null;
    }


     
}
