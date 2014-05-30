package com.architexa.diagrams.jdt.utils;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.openrdf.model.Resource;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.asm.AsmUtil;
import com.architexa.store.StoreUtil;

/**
 * Searches the source of a method invocation in order to provide 
 * an rdf Resource of the target class of that invocation.
 *
 */
public class TargetTypeFinder {

	private static final Logger logger = Activator.getLogger(TargetTypeFinder.class);

	private MethodDeclaration sourceDeclaration;
	private String targetExpression;

	private Resource targetClassRes = null;

	public TargetTypeFinder(MethodDeclaration sourceDeclaration,  String targetExpression) {
		this.sourceDeclaration = sourceDeclaration;
		this.targetExpression = targetExpression;
	}

	/**
	 * Determines the type of the expression an invocation is called
	 * on and returns an rdf Resource for that type. As examples:
	 * - If the invocation is user.getId(), where user is of type User, 
	 * this method will determine that user is of type User and return
	 * an rdf Resource for User.
	 * - If the invocation is Srvr.staticCall(), this method will
	 * determine that the call is a static call on the class Srvr 
	 * and return an rdf Resource for Srvr.
	 * - If the invocation is myCall(), callers should determine
	 * for themselves that it is an invocation to the same class
	 * and not bother using TargetTypeFinder.
	 * 
	 * @return an rdf Resource of the class that the invocation 
	 * expression (provided in the constructor as targetExpression)
	 * is a type of
	 */
	public Resource search() {
		// Try to find local variable in method declaration whose name matches target name
		targetClassRes = searchMethodDeclaration(sourceDeclaration);
		if(targetClassRes!=null) return targetClassRes;

		// Couldn't find target type in method declaration, so maybe
		// it's a field in the class that contains the method declaration
		TypeDeclaration typeContainingSourceDecl = null;
		ASTNode parent = sourceDeclaration.getParent();
		while(parent!=null && !(parent instanceof TypeDeclaration)) 
			parent = parent.getParent();
		if(parent instanceof TypeDeclaration) {
			typeContainingSourceDecl = (TypeDeclaration)parent;
			targetClassRes = searchTypeDeclaration(typeContainingSourceDecl);
		}
		if(targetClassRes!=null) return targetClassRes;

		// Invoked method could be static, in which case it will 
		// not be invoked on a variable but rather on a class.
		// (Note that if the invocation is on a static method of
		// an inner class, the binding should have been available,
		// so we don't worry about that case here)

		// Class in a different package ie an imported class
		CompilationUnit compilationUnitContainingSourceDecl = null;
		parent = typeContainingSourceDecl!=null ? 
				typeContainingSourceDecl.getParent() : sourceDeclaration.getParent();
		while(parent!=null && !(parent instanceof CompilationUnit))
			parent = parent.getParent();
		if(parent instanceof CompilationUnit) {
			compilationUnitContainingSourceDecl = (CompilationUnit) parent;
			targetClassRes = searchCompilationUnit(compilationUnitContainingSourceDecl);
		}
		if(targetClassRes!=null) return targetClassRes;

		// Class in the same package
		PackageDeclaration packageContainingSourceDecl = compilationUnitContainingSourceDecl.getPackage();
		targetClassRes = searchPackage(packageContainingSourceDecl);

		return targetClassRes;
	}

	/**
	 * Search the declaration that makes the invocation for a variable
	 * (such as an argument of the declaration, a variable created in 
	 * the declaration, a variable initialized in a for loop in the 
	 * declaration) that matches the expression the invocation is called on.
	 * 
	 * @param methodDecl the declaration that makes the invocation on 
	 * the variable we want to find the type of 
	 * @return a RDF Resource of the class that the variable is a type of
	 */
	private Resource searchMethodDeclaration(MethodDeclaration methodDecl) {
		methodDecl.accept(new ASTVisitor() {

			// Local variable declarations and for loop initializers are VariableDeclarationFragments
			@Override
			public boolean visit(VariableDeclarationFragment node) {

				// Already found the target type Resource,
				// so no need to look at any more variables
				if(targetClassRes!=null) return false;

				// Variable's name doesn't match target 
				// instance's name, so keep searching
				SimpleName varName = node.getName();
				if(!namesMatch(targetExpression, varName)) return true;

				// Name of this var decl matches the name of the instance
				// the invocation was made on, so find the type.
				// VariableDeclarationFragments are missing the modifiers and
				// the type; these are located in the fragment's parent node
				Type varType = null;
				ASTNode varParent = node.getParent();
				while(varType==null && varParent!=null) {
					try {
						Method getTypeMthd = varParent.getClass().getMethod("getType");
						varType = (Type) getTypeMthd.invoke(varParent);
					} catch (Exception e) {
						logger.warn("Cannot call getType() on variable declaration's parent,  " +
								"a "+varParent.getClass());
					}
				}
				if(varType!=null)
					targetClassRes = typeToResource(varType);
				else
					targetClassRes = AsmUtil.getClassRes(varName.getIdentifier(), StoreUtil.getDefaultStoreRepository());
				return false;
			}

			// Method arguments and catch clause variables are SingleVariableDeclarations
			@Override
			public boolean visit(SingleVariableDeclaration node) {

				// Already found the target type Resource,
				// so no need to look at any more variables
				if(targetClassRes!=null) return false;

				// Variable's name doesn't match target 
				// instance's name, so keep searching
				if(!namesMatch(targetExpression, node.getName())) return true;

				// Name of this var decl matches the name of the instance
				// the invocation was made on, so find the type
				Type varType = node.getType();
				targetClassRes = typeToResource(varType);
				return false;
			}
		});
		return targetClassRes;
	}

	/**
	 * Search the class containing the declaration that makes the 
	 * invocation for a field declaration that matches the expression 
	 * the invocation is called on.
	 * @param typeDeclaration the class containing the declaration that 
	 * makes the invocation on the variable we want to find the type of 
	 * @return a RDF Resource of the class that the variable is a type of
	 */
	private Resource searchTypeDeclaration(TypeDeclaration typeDeclaration) {
		typeDeclaration.accept(new ASTVisitor() {

			// A FieldDeclaration contains VariableDeclarationFragments
			@Override
			public boolean visit(VariableDeclarationFragment node) {

				// Already found the target type Resource, so no 
				// need to look at any more field declarations
				if(targetClassRes!=null) return false;

				// Field's name doesn't match target 
				// instance's name, so keep searching
				if(!namesMatch(targetExpression, node.getName())) return true;

				// Name of this var decl matches the name of the instance
				// the invocation was made on, so find the field declaration
				// and the type it declares
				ASTNode fieldDecl = node.getParent();
				while(fieldDecl!=null && !(fieldDecl instanceof FieldDeclaration)) {
					fieldDecl = fieldDecl.getParent();
				}
				if(fieldDecl==null) return true;

				Type varType = ((FieldDeclaration)fieldDecl).getType();
				targetClassRes = typeToResource(varType);
				return false;
			}
		});
		return targetClassRes;
	}

	/**
	 * Search the compilation unit containing the declaration that makes the 
	 * invocation for an imported class that matches the expression 
	 * the static invocation is called on.
	 * @param compilationUnit the compilation unit containing the declaration 
	 * that makes a static invocation on a class we want to find the type of 
	 * @return a RDF Resource of the class that the static method is invoked on
	 */
	private Resource searchCompilationUnit(CompilationUnit compilationUnit) {
		compilationUnit.accept(new ASTVisitor() {

			// A FieldDeclaration contains VariableDeclarationFragments
			@Override
			public boolean visit(ImportDeclaration node) {

				// Already found the target type Resource, so no 
				// need to look at any more field declarations
				if(targetClassRes!=null) return false;

				// If a static on-demand import (1) or a regular
				// single-type import (2), ImportDeclaration.getName() 
				// is the qualified name of a the imported type 
				boolean importIsType = 
					(node.isOnDemand() && node.isStatic()) /*(1)*/
						|| (!node.isOnDemand() && !node.isStatic()) /*(2)*/;
				if(!importIsType) return true; // Not the import we want, so keep searching

				Name qualifiedImportName = node.getName();
				SimpleName importTypeName = qualifiedImportName.isQualifiedName() ? 
						((QualifiedName)qualifiedImportName).getName() : (SimpleName)qualifiedImportName;

				// Imported class's name doesn't match 
				// target class's name, so keep searching
				if(!namesMatch(targetExpression, importTypeName)) return true;

				// Name of this imported class matches the name of the class
				// the invocation was made on, so make a Resource from the
				// import's qualified name
				targetClassRes = AsmUtil.getClassRes(qualifiedImportName.getFullyQualifiedName(), 
						StoreUtil.getDefaultStoreRepository());
				return false;
			}
		});
		return targetClassRes;
	}

	/**
	 * Search the package containing the declaration that makes the 
	 * invocation for a class that matches the expression the static 
	 * invocation is called on.
	 * @param packageDecl the package containing the class that contains the
	 * declaration making a static invocation on a class we want to find the 
	 * type of 
	 * @return a RDF Resource of the class that the static method is invoked on
	 */
	private Resource searchPackage(PackageDeclaration packageDecl) {

		if(packageDecl.resolveBinding()==null ||
				!(packageDecl.resolveBinding().getJavaElement() instanceof PackageFragment)) 
			return null;

		PackageFragment packageElmt = (PackageFragment) packageDecl.resolveBinding().getJavaElement();
		IJavaElement matchingClassElmt = null;
		try {
			for(ICompilationUnit topLevelClass : packageElmt.getCompilationUnits()) {

				// Compare target name to top level class and any inner classes
				for(IType innerClass : topLevelClass.getAllTypes()) {
					if(namesMatch(targetExpression, innerClass)) {
						matchingClassElmt = innerClass;
						break;
					}
				}
				if(matchingClassElmt != null) break; // Found matching type so can stop looking
			}
		} catch (JavaModelException e) {
			logger.error("Unexpected exception getting compilation " +
					"units in package "+packageElmt.getElementName());
		}

		if(matchingClassElmt==null) return null;
		targetClassRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), matchingClassElmt);
		return targetClassRes;
	}

	private boolean namesMatch(String targetExpression, SimpleName nodeName) {
		return targetExpression.equals(nodeName.getIdentifier());
	}

	private boolean namesMatch(String targetExpression, IJavaElement potentialMatchingClass) {
		return targetExpression.equals(potentialMatchingClass.getElementName());
	}

	private Resource typeToResource(Type type) {
		ITypeBinding typeBinding = type.resolveBinding();
		return RJCore.bindingToResource(StoreUtil.getDefaultStoreRepository(), typeBinding);
	}

}
