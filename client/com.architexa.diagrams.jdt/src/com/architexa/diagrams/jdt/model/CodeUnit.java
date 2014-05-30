/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
/*
 * Created on Mar 3, 2003
 *
 */
package com.architexa.diagrams.jdt.model;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.Filters;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.asm.AsmMethodSupport;
import com.architexa.diagrams.jdt.utils.MethodParametersSupport;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DerivedArtifact;
import com.architexa.diagrams.ui.EditorImageDecorator;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.utils.ErrorUtils;
import com.architexa.intro.preferences.LibraryPreferences;
import com.architexa.intro.preferences.PreferenceConstants;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.RepositoryMgr;
import com.architexa.store.StoreUtil;


/**
 * @author vineet
 * 
 * Consists of mostly static methods that should only be called when known to
 *  have an object of CodeUnit type (eventually may move to use Jena Resource
 *  heirarchy)
 */
// To be renamed CodeFrag
public class CodeUnit extends ArtifactFragment {
	
	// the way initializers look in the eclipse Outline view
	public static String initializerStringRep = "{...}";

    static final Logger logger = Activator.getLogger(CodeUnit.class);

	public static int detailLevel = 0;

    // For User created nodes 
    public CodeUnit() {
		super(StoreUtil.createBNode());
	}
    
    public CodeUnit(Resource _elementRes) {
        super(_elementRes);
    }

    public CodeUnit(Artifact art) {
	    super(art.elementRes);
	}

    public CodeUnit(ArtifactFragment af) {
    	super((Artifact)null);
    	clone(af);
	}
	
	public IJavaElement getJDTElement(ReloRdfRepository repo) {
	    return RJCore.resourceToJDTElement(repo, getArt().elementRes);
	}

	public static CodeUnit getCodeUnit(ReloRdfRepository repo, IJavaElement element) {
//		System.out.println("asking here");
//		if(element instanceof Initializer || element instanceof InitializerWrapper) {
//			((Initializer)element).get
//		}
		return new CodeUnit( RJCore.jdtElementToResource(repo, element) );
	}

	public static Artifact getArtifact(ReloRdfRepository repo, IJavaElement element) {
		return new Artifact( RJCore.jdtElementToResource(repo, element) );
	}
	
	
	public String getTypeStr(ReloRdfRepository repo) {
        return repo.getRequiredProperty(getArt().queryType(repo), ReloRdfRepository.rdfsLabel).getObject().toString();
	}

	public static boolean isJavaElementType(ReloRdfRepository repo, Resource typeRes) {
	    return (typeRes.equals(RJCore.classType)
	            || typeRes.equals(RJCore.interfaceType)
	            || typeRes.equals(RJCore.fieldType)
	            || typeRes.equals(RJCore.methodType)
	            || typeRes.equals(RJCore.packageType) );
	}
	public boolean isType(ReloRdfRepository repo) {
	    return isType(this, repo);
	}
	public static boolean isType(ArtifactFragment artFrag, ReloRdfRepository repo) {
	    return isType(repo, artFrag);
	}
	public static boolean isType(ReloRdfRepository repo, ArtifactFragment artFrag) {
	    return isType(repo, artFrag.getArt().queryType(repo));
	}

	public static boolean isType(ReloRdfRepository repo, Resource typeRes) {
	    return CUSupport.isType(typeRes);
	}

	public boolean isField(ReloRdfRepository repo) {
	    return isField(repo, getArt().queryType(repo));
	}

	public static boolean isField(ReloRdfRepository repo, Resource typeRes) {
	    return typeRes.equals(RJCore.fieldType);
	}

	public boolean isMethod(ReloRdfRepository repo) {
	    return isMethod(repo, getArt().queryType(repo));
	}

	public static boolean isMethod(ReloRdfRepository repo, Resource typeRes) {
	    return typeRes.equals(RJCore.methodType);
	}

	public boolean isPackage(ReloRdfRepository repo) {
	    return isPackage(repo, getArt().queryType(repo));
	}

	public static boolean isPackage(ReloRdfRepository repo, Resource typeRes) {
	    return CUSupport.isPackage(typeRes);
	}
	public static boolean isPckgDirType(ReloRdfRepository repo, Artifact node) {
	    if (repo.contains(node.elementRes, RJCore.pckgDirType, true))
	    	return true;
		else
			return false;
	}

	public static boolean isJavaProject(ReloRdfRepository repo, Resource typeRes) {
	    return typeRes.equals(RJCore.projectType);
	}
    public static int getContextDepth(ReloRdfRepository repo, Artifact art) {
        PreferenceConstants.loadPrefs();
        Resource artType = art.queryType(repo);
        Artifact parentArt = art.queryParentArtifact(repo);
        int myDepth = 1;
        
        if (isJavaProject(repo, artType)) {
            myDepth = 0;
        } else  if (isPackage(repo, artType)) {
            myDepth += getCount(getLabelWithContext(repo, art), ".", 0);
        }

        if (parentArt == null)
            return myDepth;
        else
            return myDepth + getContextDepth(repo, parentArt);
    }
    private static int getCount(String lbl, String findStr, int fromNdx) {
        int ndx = lbl.indexOf(findStr, fromNdx);
        if (ndx == -1) 
            return 0;
        else
            return 1 + getCount(lbl, findStr, ndx + 1);
    }


    public int getContextDepth(ReloRdfRepository repo) {
        return CodeUnit.getContextDepth(repo, this.getArt());
    }

	public static String getLabel(ReloRdfRepository repo, ArtifactFragment af) {
		if (repo == null)
			return af.toString();
		else
			return getLabel(repo, af.getArt(), null, false);
	}
	public static String getLabelWithContext(ReloRdfRepository repo, ArtifactFragment af) {
		if (af instanceof DerivedArtifact)
			return "DervivedArtifact-" + af.getClass().getName();

		if (repo == null)
			return af.toString();

		return getLabelWithContext(repo, af.getArt());
	}
	public static String getLabelWithContext(ReloRdfRepository repo, Artifact art) {
		return getLabel(repo, art, null, true);
	}
	public static String getLabelWithContext(ReloRdfRepository repo, Artifact art, Artifact contextArt) {
		return getLabel(repo, art, contextArt, true);
	}
	public static String getLabel(ReloRdfRepository repo, ArtifactFragment af, Artifact contextArt, boolean labelWithContext) {
		return getLabel(repo, af.getArt(), contextArt, labelWithContext);
	}
	public static String getLabel(ReloRdfRepository repo, Artifact art, Artifact contextArt, boolean highDetailForContext) {
		if (art.elementRes == null) {
			return "{null}";
		}
		
		int currentDetailLevel = detailLevel;
		if (highDetailForContext)
			currentDetailLevel  = 0;
		
		String retVal = null;
		
		String name = repo.queryName(art.elementRes);
		if (name==null || name.equals("")) {
			retVal = art.toString();
			retVal = retVal.substring(retVal.lastIndexOf('#') + 1);
			return retVal;
		}
		else
			retVal = name;
		
		

		Object sigObj = repo.getStatement(art.elementRes, RSECore.classSig, null).getObject();
		if(sigObj != null) {
			retVal = retVal + " " + sigObj.toString();
		} 
		
		
//		Object obj = repo.getNameStatement(art.elementRes, RSECore.name, null).getObject();
//		if(obj == null) {
//			// In the case of class files
//			//@tag post-rearch-verify
//			//retVal = art.getNonDerivedBaseArtifact().toString();
//			retVal = art.toString();
//			retVal = retVal.substring(retVal.lastIndexOf('#') + 1);
//			return retVal;
//		} else {
//			retVal = obj.toString();
//		}
//		String retVal = null;
//		String obj = repo.queryName(art.elementRes);//getNameStatement(art.elementRes, RSECore.name, null).getObject();
//		if(obj == null || obj.equals("")) {
//			// In the case of class files
//			//@tag post-rearch-verify
//			//retVal = art.getNonDerivedBaseArtifact().toString();
//			retVal = art.toString();
//			retVal = retVal.substring(retVal.lastIndexOf('#') + 1);
//			return retVal;
//		} else {
//			retVal = obj;
//		}
		if ("<clinit>".equals(retVal)) {
			return initializerStringRep;
		}


		// do not add CNtx info to Anon classes: it is added at the end
		IJavaElement anonClassElt = null;
		boolean isAnonConstructor = false;//isAnonConstructor(art, repo);
		boolean isAnonymousClass = false;//IsArtAnonClass(repo, art);
		if (art.toString().indexOf("$") != art.toString().lastIndexOf("$")) {
			// Don't need to test whether art is an anonymous class if 
			// art is not even a class. This will require the Resource 
			// to be converted to an IJavaElement less often, which will 
			// save time, especially for a Resource the IJavaElement can't 
			// be found for, for example when art represents a built in 
			// enum method.
			anonClassElt = RJCore.resourceToJDTElement(repo, art.elementRes);
			isAnonymousClass =  (anonClassElt instanceof SourceType) && ((SourceType)anonClassElt).isAnonymous();
			isAnonConstructor = isAnonConstructor(art, repo);
		}
			
		// add contextual information
		if (highDetailForContext && !isAnonymousClass && !isAnonConstructor) {
			Artifact curElemArt = queryParentArt(repo, art);//art.queryParentArtifact(repo);
			while (curElemArt != null && !curElemArt.equals(contextArt)) {
			    Resource curElemTypeRes = curElemArt.queryType(repo);
				if (CodeUnit.isType(repo, curElemTypeRes) || CodeUnit.isPackage(repo, curElemTypeRes)) {
					retVal = curElemArt.queryName(repo) + "." + retVal;
				}
				curElemArt = queryParentArt(repo, curElemArt);//curElemArt.queryParentArtifact(repo);
			}
		} else {
			// for packages lets strip out everything in the beginning
			if (isPackage(repo, art.queryType(repo))) {
				retVal = retVal.substring(retVal.lastIndexOf(".")+1);
			}
		}
		
		
        // check for null types to account for layers/multiLayers 
        Resource resType = art.queryType(repo);
        if (resType==null) return "";
		
		if (isField(repo, resType ) ) {
			Resource res = (Resource) repo.getStatement(art.elementRes, RJCore.refType, null).getObject();
			if (res != null)
				retVal += ": " + getLabel(repo, new Artifact(res), contextArt);
        }
        
      	if (isMethod(repo, art.queryType(repo))) {
      		Statement stmt = repo.getStatement(art.elementRes, MethodParametersSupport.parameterCachedLabel, null);	
      		if(stmt==ReloRdfRepository.nullStatement) // maybe wasn't built because it's user created
      			stmt = repo.getStatement(art.elementRes, RSECore.userCreated, StoreUtil.createMemLiteral("true"));
      		if (stmt != ReloRdfRepository.nullStatement) {
	      		
	      		if(isAnonConstructor) {
					// anonymous class constructor - displaying as "new [class name](arg types)"
					Artifact anonClass = art.queryParentArtifact(repo);
					Resource obj = (Resource) repo.getStatement(anonClass.elementRes, RJCore.inherits, null).getObject();
					if(obj==null) return art.queryName(repo);
					String implementedClass = new Artifact(obj).queryName(repo);
					String args = getAnonConstructorArgs(repo, art);
					return implementedClass+"("+args+")"+" ##"+retVal;
				}
	            retVal += MethodParametersSupport.getInOutSig(art, repo, currentDetailLevel);
      		}
		}

		// anonymous class - displaying name as "new [class name](arg types)"
		if(isAnonymousClass && anonClassElt!=null) {
			try {
				String superClassName = ((SourceType)anonClassElt).getSuperclassName();
				String arguments = getAnonClassArgumentTypes(repo, art.elementRes);
				return "new "+superClassName+"("+arguments+") #"+retVal;
			} catch (JavaModelException e) {
				logger.error("Unexpected exception while getting label of anonymous class.", e);
			}
		}
		
		return retVal + art.getTrailer();
	}
	
	// this is currently only optimized for classes
	private static Artifact queryParentArt(ReloRdfRepository repo, Artifact art) {
		String artStr = art.toString();
		String retArtStr = "";
		Resource resType = art.queryType(repo);
		if (artStr.contains("$") && !isField(repo, resType ) && !isMethod(repo, resType )) {
			retArtStr = artStr.substring(0,artStr.indexOf("$"));
			return new Artifact(repo.createURI(retArtStr));
		} else {
			return art.queryParentArtifact(repo);
		}
		
		
	}

	private static boolean isAnonConstructor(Artifact art, ReloRdfRepository repo) {

    	if (repo.hasStatement(art.elementRes, AsmMethodSupport.anonymousMethodType, true)) return true;
    	
    	String name = art.queryName(repo);
    	if(name!=null && !"".equals(name.trim()) && 
    			Character.isDigit(name.charAt(0))) return true;
		
		Artifact parentArt = art.queryParentArtifact(repo);
		if (!ErrorUtils.isValidType(parentArt, repo, true)) return false;
		
		if(!CodeUnit.isType(repo, parentArt.queryType(repo))) return false;
		
		String methodName = art.queryName(repo);
		String parentName = parentArt.queryName(repo);
		if(methodName==null || !methodName.equals(parentName)) return false;

		// should never reach this point and have to rely on IJavaElements
		IJavaElement parentClassElt = RJCore.resourceToJDTElement(repo, parentArt.elementRes);
		boolean isInAnonClass = (parentClassElt instanceof SourceType) && ((SourceType)parentClassElt).isAnonymous();
		if(!isInAnonClass)  return false;

		IJavaElement anonMethodElt = RJCore.resourceToJDTElement(repo, art.elementRes);
		try {
			return ((IMethod)anonMethodElt).isConstructor();
		} catch (JavaModelException e) {
				logger.error("Unexpected exception while testing whether method "+art.elementRes+" is constructor.");
		}
		return false;
	}
	public static String getAnonClassArgumentTypes(ReloRdfRepository repo, Resource anonClassRes) {
		String className = new Artifact(anonClassRes).queryName(repo);
		StatementIterator containsIter = repo.getStatements(anonClassRes, RSECore.contains, null);
		while(containsIter.hasNext()) {
			Value containee = containsIter.next().getObject();
			if(!(containee instanceof Resource)) continue;

			Artifact tempContaineeArt = new Artifact((Resource) containee);
			Resource containeeType = tempContaineeArt.queryType(repo);
			if(!RJCore.methodType.equals(containeeType)) continue; // not a method
			
			String methodName = tempContaineeArt.queryName(repo);
			if(!methodName.equals(className)) continue; // not the constructor

			return getAnonConstructorArgs(repo, tempContaineeArt);
		}
		return "";
	}
	private static String getAnonConstructorArgs(ReloRdfRepository repo, Artifact constructor) {
		String argTypes = MethodParametersSupport.getParamString(constructor, repo);
		
        // As long as the anon class is not declared in a static method,
		// for constructors of an inner class, the first token (in the byte
		// code and our format) is a hidden reference to the parent class.
        	
		int endOfFirstArg = argTypes.indexOf(",");
		// if 0 or 1 param, it would only be the reference to the parent
		// class, so we can just return an empty string of no params
		if(endOfFirstArg==-1) return "";
		// when at least 2 params, remove the first one and the comma that follows
		return argTypes.substring(endOfFirstArg+1);
	}
    public static String getLabel(ReloRdfRepository repo, Artifact art, Artifact contextArt) {
//        PreferenceConstants.loadPrefs();
//        boolean showCntx = AtxaIntroPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.LabelItemsWithContextKey);
        return getLabel(repo, art, contextArt, detailLevel == 0);
    }
    public static String getLabel(ReloRdfRepository repo, Artifact art, ArtifactFragment contextAF) {
    	return getLabel(repo, art, contextAF.getArt());
    }

	public String getLabel(ReloRdfRepository repo, Artifact contextArt) {
		return getLabel(repo, this.getArt(), contextArt);
	}

    public String getLabel(ReloRdfRepository repo) {
        return getLabel(repo, (Artifact)null);
    }
    
	public static String getLabels(ReloRdfRepository repo, List<ArtifactFragment> afList, boolean wContext) {
		StringBuffer sb = new StringBuffer("[");
		boolean first = true;
		for (ArtifactFragment artFrag : afList) {
			if (!first) sb.append(", ");
			sb.append(CodeUnit.getLabel(repo, artFrag, null, wContext));
			first = false;
		}
		sb.append("]");
		return sb.toString();
	}
	
	public static String getLabels(ReloRdfRepository repo, List<ArtifactFragment> afList) {
		return getLabels(repo, afList, true);
	}

	public static String getLabelss(ReloRdfRepository repo, List<List<ArtifactFragment>> afListList, boolean wContext) {
		StringBuffer sb = new StringBuffer("[");
		boolean first = true;
		for (List<ArtifactFragment> afList : afListList) {
			if (!first) sb.append(", ");
			sb.append(getLabels(repo, afList, wContext));
			first = false;
		}
		sb.append("]");
		return sb.toString();
	}

	public static String getLabelss(ReloRdfRepository repo, List<List<ArtifactFragment>> afList) {
		return getLabelss(repo, afList, true);
	}


    /**
     * Note the input collection is changed by the method
     */
    @SuppressWarnings("unchecked")
    private static List<CodeUnit> transformResourcesToCodeUnits(List<Resource> in) {
        CollectionUtils.transform(in, new Transformer() {
            public Object transform(Object arg0) {
                return new CodeUnit((Resource)arg0);
            }});
        return (List) in;
    }
    

	public List<CodeUnit> listFwdCU(ReloRdfRepository repo, URI relationRes, Predicate filter) {
		List<Resource> retValCU = new LinkedList<Resource>();
		repo.getResourcesFor(retValCU, getArt().elementRes, relationRes, null);
		CollectionUtils.filter(retValCU, filter);
		return transformResourcesToCodeUnits(retValCU);
	}
	
	public List<CodeUnit> listRevCU(ReloRdfRepository repo, URI relationRes, Predicate filter) {
		List<Resource> retValCU = new LinkedList<Resource>();
		repo.getResourcesFor(retValCU, null, relationRes, getArt().elementRes);
		CollectionUtils.filter(retValCU, filter);
		return transformResourcesToCodeUnits(retValCU);
	}
	
	public List<CodeUnit> listFwdCU(ReloRdfRepository repo, URI relationRes) {
		return listFwdCU(repo, relationRes, null);
	}

	public List<CodeUnit> listRevCU(ReloRdfRepository repo, URI relationRes) {
		return listRevCU(repo, relationRes, null);
	}

	public List<CodeUnit> listSupertypesCU(ReloRdfRepository repo) {
	    return listFwdCU(repo, RJCore.inherits);
	}

	public List<CodeUnit> listImplementedCU(ReloRdfRepository repo) {
	    return listFwdCU(repo, RJCore.inherits, Filters.getTypeFilter(repo, RJCore.interfaceType));
	}

	public CodeUnit getExtendedTypeCU(ReloRdfRepository repo) {
		List<CodeUnit> extendedTypeCU = listFwdCU(repo, RJCore.inherits, Filters.getTypeFilter(repo, RJCore.classType));
		if (extendedTypeCU.isEmpty())
			return null;
		else
			return extendedTypeCU.get(0);
	}
	public CodeUnit getInheritedTypeCU(ReloRdfRepository repo) {
		List<CodeUnit> inheritedTypeCU = listRevCU(repo, RJCore.inherits, Filters.getTypeFilter(repo, RJCore.classType));
		if (inheritedTypeCU.isEmpty())
			return null;
		else
			return inheritedTypeCU.get(0);
	}
	public List<CodeUnit> getExtendedTypeList(ReloRdfRepository repo) {
		return listFwdCU(repo, RJCore.inherits, Filters.getTypeFilter(repo, RJCore.classType));
	}
	public List<CodeUnit> getInheritedTypeList(ReloRdfRepository repo) {
		return listRevCU(repo, RJCore.inherits, Filters.getTypeFilter(repo, RJCore.classType));
	}

	public ReloRdfRepository getSupertypesHierarchyCU(ReloRdfRepository repo) {
	    ReloRdfRepository superTypesModel = StoreUtil.getMemRepository();
		StoreUtil.getObjectsRecursively(superTypesModel, getArt().elementRes, RJCore.inherits);
		return superTypesModel;
	}


	
	public static Resource getAccess(ReloRdfRepository repo, Artifact art) {
	    return (Resource) repo.getStatement(art.elementRes, RJCore.access, null).getObject();
	}

	public static ISharedImages isi = JavaUI.getSharedImages();
	public Image getIcon(ReloRdfRepository repo) {
	    return getIcon(repo, this.getArt(), getArt().queryType(repo));
	}
	public static ImageDescriptor getIconDescriptor(
			ReloRdfRepository repo, Artifact art, Resource typeRes) {
	    String iconKey = null;
	    if (typeRes == null) {
	    	logger.error("Type null for Artifact: " + art);
	        iconKey = ISharedImages.IMG_FIELD_PRIVATE;
	    } else if (typeRes.equals(RJCore.packageType)) {
	    	if (art.isInitialized(repo))
	    		iconKey = ISharedImages.IMG_OBJS_PACKAGE;
	    	else
		        iconKey = ISharedImages.IMG_OBJS_EMPTY_PACKAGE;
	    	//iconKey = ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE;
	    } else if (typeRes.equals(RJCore.indirectPckgDirType)) {
	        iconKey = ISharedImages.IMG_OBJS_PACKFRAG_ROOT;
	        // I prefer the idea behind the below one - but it looks a little unprofessional
	        // iconKey = ISharedImages.IMG_OBJS_LOGICAL_PACKAGE;
	    } else  if (typeRes.equals(RJCore.classType)) {
	    	try {
	    		IJavaElement elmt = (RSECore.isUserCreated(repo, art.elementRes)) ? null : 
	    			RJCore.resourceToJDTElement(repo, art.elementRes);
	    		if(elmt instanceof IType && ((IType)elmt).isAnonymous())
	    			iconKey = ISharedImages.IMG_OBJS_CLASS_DEFAULT;
	    		else if(elmt instanceof IType && ((IType)elmt).isEnum()) {
	    			// Use the eclipse label provider to get the correct public, 
	    			// protected, or private enum icon since (see ticket #739) protected
	    			// enums have an access stmt in repo that says they are public and 
	    			// private enums have an access stmt that says they have no access.
	    			Image img = new JavaUILabelProvider().getImage(elmt);
	    			if(img!=null) return ImageDescriptor.createFromImage(img);
	    			iconKey = ISharedImages.IMG_OBJS_ENUM;
	    		}
	    	} catch (JavaModelException e) {
	    		logger.error("Unexpected exception while testing whether " +
	    				"Resource " + art.elementRes + " represents an enum.", e);
	    	}
	    	if(iconKey==null) iconKey = ISharedImages.IMG_OBJS_CLASS;
	    } else  if (typeRes.equals(RJCore.interfaceType)) {
	        iconKey = ISharedImages.IMG_OBJS_INTERFACE;
	    } else if (typeRes.equals(RJCore.methodType)) {
	    	if(isAnonConstructor(art, repo)) {
	    		ImageDescriptor desc = getImageDescriptorFromKey(ISharedImages.IMG_OBJS_CLASS_DEFAULT);
	    		// Wrap in a JavaElementImageDescriptor since it sets the
	    		// size, which will prevent ugly stretching of images in menus
	    		return new JavaElementImageDescriptor(desc, 0, JavaElementImageProvider.BIG_SIZE);
	    	}
		    Resource cuAccess = getAccess(repo, art);
		    iconKey = getMethodAcessIconKey(cuAccess);
	    } else if (typeRes.equals(RJCore.fieldType)) {
		    Resource cuAccess = getAccess(repo, art);
		    if (cuAccess==null)
		    	iconKey = ISharedImages.IMG_FIELD_DEFAULT;
		    else if (cuAccess.equals(RJCore.publicAccess))
		        iconKey = ISharedImages.IMG_FIELD_PUBLIC;
		    else if (cuAccess.equals(RJCore.protectedAccess))
		        iconKey = ISharedImages.IMG_FIELD_PROTECTED;
		    else if (cuAccess.equals(RJCore.privateAccess))
		        iconKey = ISharedImages.IMG_FIELD_PRIVATE;
		    else
		        iconKey = ISharedImages.IMG_FIELD_DEFAULT;
	    } else if (typeRes.equals(RJCore.projectType)){
	    	iconKey = ISharedImages.IMG_OBJS_PACKFRAG_ROOT;
	    }
	    ImageDescriptor desc = getImageDescriptorFromKey(iconKey);
	    // Wrap in a JavaElementImageDescriptor since it sets the
	    // size, which will prevent ugly stretching of images in menus
	    return new JavaElementImageDescriptor(desc, 0, JavaElementImageProvider.BIG_SIZE);
	}
	public static String getMethodAcessIconKey(Resource cuAccess) {
		String iconKey;
		if (cuAccess==null) 
			iconKey = ISharedImages.IMG_OBJS_DEFAULT;
		else if (cuAccess.equals(RJCore.publicAccess))
			iconKey = ISharedImages.IMG_OBJS_PUBLIC;
		else if (cuAccess.equals(RJCore.protectedAccess))
			iconKey = ISharedImages.IMG_OBJS_PROTECTED;
		else if (cuAccess.equals(RJCore.privateAccess))
			iconKey = ISharedImages.IMG_OBJS_PRIVATE;
		else
			iconKey = ISharedImages.IMG_OBJS_DEFAULT;
		return iconKey;
	}
	public static String getTypeIconKey(Resource typeRes) {
		String iconKey = null;
		if (typeRes.equals(RJCore.packageType))
			iconKey = ISharedImages.IMG_OBJS_PACKAGE;
		else if (typeRes.equals(RJCore.indirectPckgDirType))
			iconKey = ISharedImages.IMG_OBJS_PACKFRAG_ROOT;
		else if (typeRes.equals(RJCore.classType))
			iconKey = ISharedImages.IMG_OBJS_CLASS;
		else if (typeRes.equals(RJCore.interfaceType))
			iconKey = ISharedImages.IMG_OBJS_INTERFACE;
		else if (typeRes.equals(RJCore.methodType))
			iconKey = ISharedImages.IMG_OBJS_PUBLIC;
		else if (typeRes.equals(RJCore.fieldType))
			iconKey = ISharedImages.IMG_FIELD_PUBLIC;
		else if (typeRes.equals(RJCore.projectType))
			iconKey = ISharedImages.IMG_OBJS_PACKFRAG_ROOT;
		return iconKey;
	}
	public static Image getIcon(ReloRdfRepository repo, Artifact art, Resource typeRes) {

		// Only static initializers are shown in relo and strata. Instance
		// initializers are represented by the default constructor; for example,
		// any calls made from an instance initializer will appear to be made
		// from the default constructor, even when the class doesn't explicitly
		// contain a default, no argument constructor.
		if("<clinit>".equals(art.queryName(repo))) {
			// Wrap in a JavaElementImageDescriptor since it sets the
			// size, which will prevent ugly stretching of images in menus
			ImageDescriptor desc = Activator.getImageDescriptor("icons/static_initializer.png");
			ImageDescriptor wrappedDesc = new JavaElementImageDescriptor(desc, 0, JavaElementImageProvider.BIG_SIZE);
			return ImageCache.calcImageFromDescriptor(wrappedDesc);
		}

		ImageDescriptor desc = getIconDescriptor(repo, art, typeRes);

		if (desc == null) {
	        logger.error("Icon requested for unknown type: " + typeRes, new Exception());
	        desc = isi.getImageDescriptor(ISharedImages.IMG_FIELD_PRIVATE);
	    }
		
		Image iconImage = ImageCache.calcImageFromDescriptor(desc);
		
    	ImageDescriptor di = getDecoratedIcon(repo, art, ImageDescriptor.createFromImage(iconImage));
    	
		return ImageCache.calcImageFromDescriptor(di);
	}
	public static ImageDescriptor getDecoratedIcon(ReloRdfRepository repo, Artifact art, ImageDescriptor icon) {

		//If the part is outdated, decorate it with an error image in the diagram
		ReloRdfRepository outDateCheckRepo = repo;
		if (repo instanceof RepositoryMgr) {
    		RepositoryMgr unionRepo = (RepositoryMgr)repo;
    		outDateCheckRepo = unionRepo.getStoreRepo();
    	}
		
		StatementIterator it = outDateCheckRepo.getStatements(art.elementRes, null, null);
		if (LibraryPreferences.isChronoLibCodeHidden() && !it.hasNext()) {
			ImageDescriptor errorImage = ImageDescriptor.createFromFile(RSEEditor.class, "error_co.gif");
			EditorImageDecorator decorator = new EditorImageDecorator(icon, errorImage, EditorImageDecorator.BOTTOM_RIGHT);
			Image decoratedImg = ImageCache.calcImageFromDescriptor(decorator);
			return ImageDescriptor.createFromImage(decoratedImg);
		}

		// If the part is created via the palette, decorate it with a design image in the diagram
		if(RSECore.isUserCreated(repo, art.elementRes)) {
			ImageDescriptor designImage = Activator.getImageDescriptor("icons/edtsrclkup_co.gif");
			EditorImageDecorator decorator = new EditorImageDecorator(icon, designImage, EditorImageDecorator.BOTTOM_RIGHT);
			Image decoratedImg = ImageCache.calcImageFromDescriptor(decorator);
			return ImageDescriptor.createFromImage(decoratedImg);
		}

		return icon;
	}
	public static ImageDescriptor getImageDescriptorFromKey(String iconKey) {
		if (iconKey==null) iconKey = JavaPluginImages.IMG_OBJS_UNKNOWN;

		if (ImageCache.getDescriptor(iconKey) != null) return ImageCache.getDescriptor(iconKey);

        // isi.getImageDescriptor does not support some of the icons (like fields)
        //  so we use internal methods
    	int NAME_PREFIX_LENGTH= "org.eclipse.jdt.ui.".length();
    	String bundleEntry = "/icons/full/obj16/" + iconKey.substring(NAME_PREFIX_LENGTH);
	    URL iconFileURL = Platform.getBundle("org.eclipse.jdt.ui").getEntry(bundleEntry);
	    ImageCache.add(iconKey, ImageDescriptor.createFromURL(iconFileURL));
		return ImageCache.getDescriptor(iconKey);
	}

	public boolean isPublic(ReloRdfRepository repo) {
	    return repo.hasStatement(getArt().elementRes, RJCore.access, RJCore.publicAccess);
	}
	
	public List<Artifact> getMethods(ReloRdfRepository repo) {
	    return getArt().queryChildrenArtifacts(repo, PredicateUtils.andPredicate(
	    		Filters.getTypeFilter(repo, RJCore.methodType),
	            Filters.getAccessFilter(repo, RJCore.publicAccess)
	            ));
	}

	public List<Artifact> getMethods(ReloRdfRepository repo, Resource access) {
	    return getArt().queryChildrenArtifacts(repo, PredicateUtils.andPredicate(
	    		Filters.getTypeFilter(repo, RJCore.methodType),
	            Filters.getAccessFilter(repo, access)
	            ));
	}
	
	public List<Artifact> getFields(ReloRdfRepository repo) {
	    return getArt().queryChildrenArtifacts(repo, PredicateUtils.andPredicate(
	    		Filters.getTypeFilter(repo, RJCore.fieldType),
	            Filters.getAccessFilter(repo, RJCore.publicAccess)
	            ));
	}

	public List<Artifact> getFields(ReloRdfRepository repo, Resource access) {
	    return getArt().queryChildrenArtifacts(repo, PredicateUtils.andPredicate(
	    		Filters.getTypeFilter(repo, RJCore.fieldType),
	            Filters.getAccessFilter(repo, access)
	            ));
	}
	
	public static final Predicate getAccessFilter(ReloRdfRepository repo, Resource access) {
	    return StoreUtil.filterSubjectResPred(repo, RJCore.access, access);
	}
	
}