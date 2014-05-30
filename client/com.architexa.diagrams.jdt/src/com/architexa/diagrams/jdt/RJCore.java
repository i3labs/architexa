package com.architexa.diagrams.jdt;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.internal.corext.dom.NodeFinder;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.compat.ASTUtil;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.store.ReloRdfRepository;


/**
 * @author vineet
 *
 */
public class RJCore {
    private static final Logger logger = Activator.getLogger(RJCore.class);

    public static final String PLUGIN_ID = Activator.PLUGIN_ID;
   
    public final static String atxaRdfNamespace = ReloRdfRepository.atxaRdfNamespace;
    
	/*
     * Note: properties are not associated with any single model, since there 
     * could be multiple models that they apply to
     */

    private static final URI createRseUri(String str) {
    	return RSECore.createRseUri(str);
    }
    
    public static final URI srcResource = createRseUri("jdt#source-resource");
    public static final URI srcStart = createRseUri("jdt#source-start");
    public static final URI srcLength = createRseUri("jdt#source-length");

    
    // global relo-jdt properties
    public static final URI javaType = createRseUri("jdt#javaType");

    // properties
    public static final URI access = createRseUri("jdt#access");
    public static final URI parameter = createRseUri("jdt#parameter");
    public static final URI returnType = createRseUri("jdt#returnType");
    
	// diagram properties
	public static final URI index = createRseUri("jdt#index");
	public static final URI child = createRseUri("jdt#child");

    // relations
	public static final URI calls = createRseUri("jdt#calls");
	public static final URI returns = createRseUri("jdt#returns");
	public static final URI inherits = createRseUri("jdt#inherits");
	public static final URI refType = createRseUri("jdt#refType");
	public static final URI overrides = createRseUri("jdt#overrides");
	
    // object types
	public static final URI classType = createRseUri("jdt#class");
	public static final URI interfaceType = createRseUri("jdt#interface");
	public static final URI fieldType = createRseUri("jdt#field");
	public static final URI methodType = createRseUri("jdt#method");
	public static final URI packageType = createRseUri("jdt#package");
	public static final URI projectType = createRseUri("jdt#project");
	
	public static final URI isInterface = createRseUri("jdt#isInterface");
	
	// comment types and string literals
	public static final URI blockComment = createRseUri("jdt#blockComment");
    public static final URI lineComment = createRseUri("jdt#lineComment");
    public static final URI javaDoc = createRseUri("jdt#javaDoc");
    public static final URI stringLiteral = createRseUri("jdt#stringLiteral");

	public static final URI publicAccess = createRseUri("jdt#access-public");
	public static final URI protectedAccess = createRseUri("jdt#access-protected");
	public static final URI privateAccess = createRseUri("jdt#access-private");
	public static final URI noAccess = createRseUri("jdt#access-none");

    // support for directory based package exploration
    public static final URI pckgDirType = createRseUri("jdt#pckgDirType");	// all directories + dirs with '*' 
    public static final URI indirectPckgDirType = createRseUri("jdt#indirectPckgDirType"); // the ones with the '*' on them
    public static final URI pckgDirContains = RSECore.pckgDirContains;
    
    // support for a containment cache
    public static final URI containmentBasedDepStrength = createRseUri("jdt#depStrength");
    public static final URI containmentCacheValid = createRseUri("jdt#virtualCacheValid");
    public static final URI containmentBasedCalls = createRseUri("jdt#containmentBasedCalls");
    public static final URI containmentBasedInherits = createRseUri("jdt#containmentBasedInherits");
    public static final URI containmentBasedRefType = createRseUri("jdt#containmentBasedRefType");
    
    public static final URI containmentBasedClassesInside = createRseUri("jdt#inClasses");

	// above properties as directed relationships
    public static final DirectedRel fwdPckgDirContains = DirectedRel.getFwd(pckgDirContains);

    

    public final static String jdtWkspcNS = "jdt-wkspc#";
    
	public static Resource idToResource(ReloRdfRepository reloRepo, String id) {
		return RSECore.idToResource(reloRepo, jdtWkspcNS, id);
	}
	public static Resource bindingToResource(ReloRdfRepository reloRepo, IBinding binding) {
		return RSECore.idToResource(reloRepo, jdtWkspcNS, RJMapToId.getId(binding));
	}

	public static Resource jdtElementToResource(ReloRdfRepository reloRepo, IJavaElement element) {
		return RSECore.idToResource(reloRepo, jdtWkspcNS, RJMapToId.getId(element));
	}

	public static IJavaElement bindingToJDTElement(ReloRdfRepository repo, IBinding binding) {
		return resourceToJDTElement(repo, bindingToResource(repo, binding));
	}

    /**
	 * Note: returns null for project rdf resources
	 */
	public static IJavaElement resourceToJDTElement(ReloRdfRepository repo, Resource res) {
        if (!(res instanceof URI)) {
            logger.error(resFindErrStr(repo, res));
            return null;
        }
        
		// If the resource is a int, boolean, or un indexed library code then we
		// did not initialize it so there is no need to attempt to find an
		// IJavaElement
//        if (!RSECore.isInitialized(repo, res))
//        	return null;
        
        try {
            return RJMapFromId.idToJdtElement(((URI)res).getLocalName());
        } catch (JavaModelException e) {
            logger.error(resFindErrStr(repo, res), e);
        }
        return null;
	}
	private static String resFindErrStr(ReloRdfRepository repo, Resource res) {
		Value type = repo.getStatement(res, repo.rdfType, null).getObject();
		return "Cannot find element for Resource: "
				+ res
				+ " [type: "
				+ (type != null ? type.toString() : "null") 
				+ "]";
	}
	
	
	/**
	 * 
	 * @param member the member for which the corresponding ASTNode will be returned
	 * @return the ASTNode corresponding to the given member or null if the
	 * member is null, the compilation unit in which the member is declared 
	 * is null, or if the search to find the ASTNode fails
	 */
	public static ASTNode getCorrespondingASTNode(IMember member) {
		if(member==null || member.getCompilationUnit()==null) return null;

		// No actual corresponding code for anon constructor or compiler
		// generated constructor, so member.getSourceRange() will 
		// result in a Java Model NotPresentException in these cases
		if(member instanceof AnonymousClassConstructor || 
				member instanceof CompilerGeneratedDefaultConstructor) return null;

		CompilationUnit cu = ASTUtil.getAst(member.getCompilationUnit());
		try {
			ISourceRange memberSourceRange = member.getSourceRange();
			ASTNode node = NodeFinder.perform(cu, memberSourceRange);
			// Want to convert IInitializer -> core.dom.Initializer, but
			// for instance initializers, Node.perform() returns the
			// Block that the Initializer contains since the start position
			// and length of the Block are the same as the start position
			// and length of the Initializer. So, for this case return the 
			// Block's parent, which is the Initializer. This isn't an issue 
			// for a static initializer; NodeFinder.perform() will return 
			// an Initializer for it.
			if(member instanceof InitializerWrapper 
					&& node instanceof Block 
					&& node.getParent() instanceof Initializer)
				return node.getParent();
			return node;
		} catch (JavaModelException e) {
			logger.error("Unexpected Exception.", e);
		}
		return null;
	}
	
	/**
	 * Returns true if given member is a static field, a static method, 
	 * or a type declared within a static field or method (for example,
	 * private static void foo() {
	 *      Bar b = new Bar() { };
	 * }
	 * Returns false otherwise
	 */
	public static boolean isStatic(IMember member) {
		try {
			if(Flags.isStatic(member.getFlags())) return true;
			IJavaElement parent = member.getParent();
			while(parent!=null) {
				if(parent instanceof IMember 
						&& Flags.isStatic(((IMember)parent).getFlags())) return true;
				parent = parent.getParent();
			}
		} catch(JavaModelException e) {
			logger.error("Exception while testing whether element is static", e);
		}
		return false;
	}
	public static boolean isJDTWksp(Resource elementRes) {
		if (elementRes.toString().contains(jdtWkspcNS))
			return true;
		return false;
	}

}
