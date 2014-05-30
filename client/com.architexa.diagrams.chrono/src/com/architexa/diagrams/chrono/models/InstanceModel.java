package com.architexa.diagrams.chrono.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.SourceType;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.editparts.MethodBoxEditPart;
import com.architexa.diagrams.chrono.figures.InstanceFigure;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.chrono.util.InstanceUtil;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.jdt.InitializerWrapper;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class InstanceModel extends NodeModel {

	private static final Logger logger = SeqPlugin.getLogger(InstanceModel.class);

	public static int instanceBoxHeight = 30;
	private int instanceBoxWidth = -1;

	private String className = null;
	private Resource resource;
	public InstanceFigure figure;
	List<MethodBoxEditPart> methodBoxes = new ArrayList<MethodBoxEditPart>();
	private IJavaElement instanceElt;

	// JDT elements of this class's members ("members" = declared methods, inherited
	// methods, instance and static initializers, inner classes, and enums)
	private List<IMethodBinding> declaredAndInheritedMethods;
	private List<InitializerWrapper> initializers;
	private List<ITypeBinding> innerClassesAndEnums;

	// RSE models for all the "method" members (declared methods,
	// instance and static initializers, and inherited methods)
	private List<MethodBoxModel> modelsOfMethodMembers;

	// RSE models for the "type" members (inner classes and enums)
	private List<InstanceModel> modelsOfTypeMembers;


	// Models of the methods that are currently present in the Members menu list, 
	// ie methods not yet in the diagram, which removes them from the Members menu:

	// methods, including instance and static initializers, the class declares itself
	private List<MethodBoxModel> declaredMethodsCurrentlyInMenu = new ArrayList<MethodBoxModel>();
	// inherited methods, which appear together in a subtree 
	// in the menu to distinguish them from declared methods
	private List<MethodBoxModel> inheritedMethodsCurrentlyInMenu = new ArrayList<MethodBoxModel>();

	// (No such list is necessary for the inner classes and enums because they are 
	// never removed from the Members menu; we treat inner classes and enums differently 
	// because when one is selected from the menu and added to the diagram, it 
	// is not added as a child of the instance the same way a method member is).


	public InstanceModel (String instanceName, String className, Resource res, IJavaElement instanceElt) {
		super(res);
		setInstanceName(instanceName);
		setClassName(className);
		setResource(res);
		this.instanceElt = instanceElt;

		// Initialize cache of this class's members (methods declared in this class, 
		// inherited methods, instance and static initializers, inner classes, and 
		// enums) so that the members menu contains the correct member items and 
		// updates properly as this model's contained children change
		initializeMemberCache();
	}

	public InstanceModel(String instanceName, String className, Resource res) {
		this(instanceName, className, res, RJCore.resourceToJDTElement(StoreUtil.getDefaultStoreRepository(), res));
	}

	/**
	 * Should only be used by palette frags, which do not have resource/java element
	 */
	public InstanceModel() {
		super();
	}

	public IJavaElement getInstanceElem(){
		return instanceElt;
	}

	public boolean isAnonymousClass() {
		return (getInstanceElem() instanceof SourceType && 
				((SourceType)getInstanceElem()).isAnonymous());
	}

	public void setFigure(InstanceFigure figure) {
		this.figure = figure;
	}

	@Override
	public InstanceFigure getFigure() {
		return figure;
	}

	public void addMethodBox(MethodBoxEditPart box) {
		methodBoxes.add(box);
	}

	public void removeMethodBox(MethodBoxEditPart box) {
		methodBoxes.remove(box);
	}

	public List<MethodBoxEditPart> getMethodBoxes() {
		return methodBoxes;
	}

	public void removeMethodBoxFromChild(MethodBoxModel boxToRemove) {
		for(ArtifactFragment nm : getChildren()) {
			if(!(nm instanceof MethodBoxModel)) continue;
			MethodBoxModel childModel = (MethodBoxModel) nm;
			if(!childModel.getChildren().contains(boxToRemove)) continue;

			childModel.removeChild(boxToRemove);
			if(childModel.getChildren().size()==0) removeChild(childModel);
			break;
		}
	}

	public void setResource(Resource res) {
		this.resource = res;
	}

	public Resource getResource() {
		return resource;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public Resource getType() {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		return getArt().queryType(repo);
	}

	public boolean alreadyContains(Value method) {
		for(MethodBoxEditPart box : getMethodBoxes()) {
			if(method.equals(((MethodBoxModel)box.getModel()).getMethodRes())) return true;
		}
		return false;
	}

	@Override
	public String toString() {
		String instance = instanceName == null ? "" : instanceName;
		String clazz = className == null ? "" : className;
		return instance + " : " + clazz;
	}

	@Override
	public List<ArtifactFragment> getShownChildren() {
		return getChildren();
	}

	private boolean isBean = false;
	public void setIsBean(boolean b) {
		isBean = true;
	}
	
	public boolean isBean() {
		return isBean;
	}

	/** 
	 * @return a list of the IMethodBindings of all 
	 * this class's declared and inherited methods
	 */
	public List<IMethodBinding> getDeclaredAndInheritedMethods() {
		return declaredAndInheritedMethods;
	}

	/**
	 * @return a list of the jdt elements of all 
	 * instance and static initializers in this class
	 */
	public List<InitializerWrapper> getInitializers() {
		return initializers;
	}

	/**
	 * @return a list of RSE models of all the "method" members (declared 
	 * methods, instance and static initializers, and inherited methods)
	 */
	public List<MethodBoxModel> getModelsOfMethodMembers() {
		return modelsOfMethodMembers;
	}

	/**
	 * @return a list of RSE models of all the "type" members (inner classes and enums)
	 */
	public List<InstanceModel> getModelsOfTypeMembers() {
		return modelsOfTypeMembers;
	}

	/**
	 * @return a list of the methods, including instance and static initializers, 
	 * that the class declares itself and are currently present in the Members menu,
	 * ie are not yet in the diagram, which removes them from the Members menu
	 */
	public List<MethodBoxModel> getDeclaredMethodsCurrentlyInMenu() {
		return declaredMethodsCurrentlyInMenu;
	}

	/**
	 * @return a list of the inherited methods that are currently present in the Members
	 * menu, ie are not yet in the diagram, which removes them from the Members menu
	 */
	public List<MethodBoxModel> getInheritedMethodsCurrentlyInMenu() {
		return inheritedMethodsCurrentlyInMenu;
	}

	/**
	 * @return the number of members currently in the menu (all inner classes and
	 * enums + declared methods not in diagram + inherited methods not in diagram)
	 */
	public int getMemberMenuCount(){
		int numMembers = 
			modelsOfTypeMembers.size() + 
			declaredMethodsCurrentlyInMenu.size() +
			inheritedMethodsCurrentlyInMenu.size();
		return numMembers;
	}

	public void updateMethodLists() {
		// clear both the lists
		inheritedMethodsCurrentlyInMenu.clear();
		declaredMethodsCurrentlyInMenu.clear();
		for (MethodBoxModel methodModel : modelsOfMethodMembers) {
			boolean isInherited = false;

			// constructors for anon classes implementing
			// library code will have null IJavaElements
			if(methodModel.getMethod()==null) continue;

			IType declaringClass = methodModel.getMethod().getDeclaringType();
			Resource declaringClassRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), declaringClass);
			List<Resource> allSuperClasses = new ArrayList<Resource>();
			InstanceUtil.getAllSuperClassesInTypeHierarchy(getResource(), allSuperClasses);
			for (Resource superClassRes : allSuperClasses) {
				if (superClassRes.equals(declaringClassRes)) {
					isInherited = true;
					inheritedMethodsCurrentlyInMenu.add(methodModel);
					break;
				}
			}
			if (!isInherited)
				declaredMethodsCurrentlyInMenu.add(methodModel);
		}
		updateOverrides();
	}

	// method to remove overriden methods from the inherited method list
	private void updateOverrides() {
		for (MethodBoxModel containedModel : declaredMethodsCurrentlyInMenu) {
			for (MethodBoxModel inheritedModel : new ArrayList<MethodBoxModel>(inheritedMethodsCurrentlyInMenu)) {
				if (MethodUtil.isOverridenBy(inheritedModel, containedModel))
					inheritedMethodsCurrentlyInMenu.remove(inheritedModel);
			}
		}
	}

	// Finds the jdt elements of all this class's members (methods declared in this 
	// class, inherited methods, instance and static initializers, inner classes, and 
	// enums) and creates rse models for those members
	private void initializeMemberCache() {

		// The jdt elements of declared methods, inherited methods,
		// instance and static initializers, inner classes, and enums..
		declaredAndInheritedMethods = new ArrayList<IMethodBinding>();
		initializers = new ArrayList<InitializerWrapper>();
		innerClassesAndEnums = new ArrayList<ITypeBinding>();
		// ..and the models of all of these "method members"
		modelsOfMethodMembers = new ArrayList<MethodBoxModel>();
		// and "type members"
		modelsOfTypeMembers = new ArrayList<InstanceModel>();

		AbstractTypeDeclaration typeDecl = InstanceUtil.getTypeDeclaration(this);
		if(typeDecl!=null) findMembersUsingAST(typeDecl);
		else findMembersUsingRepo(); // No IJE or ASTNode available for this class, so use repo
	}

	private void findMembersUsingAST(AbstractTypeDeclaration typeDecl) {
		if(typeDecl==null) return;

		for(Object bodyDecl : typeDecl.bodyDeclarations()) {

			// find methods declared in this class
			if(bodyDecl instanceof MethodDeclaration)
				declaredAndInheritedMethods.add(((MethodDeclaration)bodyDecl).resolveBinding());

			// find inner classes and enums
			if(bodyDecl instanceof TypeDeclaration || bodyDecl instanceof EnumDeclaration)
				innerClassesAndEnums.add(((AbstractTypeDeclaration)bodyDecl).resolveBinding());
		}

		// find inherited methods
		addDeclarationsInSuper(getResource());

		// find static or instance initializers
		if(instanceElt instanceof IType) {
			try {
				for(IInitializer init : ((IType)instanceElt).getInitializers()) {
					InitializerWrapper initMethod = new InitializerWrapper(init);
					initializers.add(initMethod);
				}
			} catch(JavaModelException e) {
				logger.error("Unexpected exception while searching for " +
						"initializer blocks in class " + getClassName(), e);
			}
		}

		// Now that the members have been determined, create RSE models for them:

		for(IMethodBinding decl : declaredAndInheritedMethods) {
			if(!(decl.getJavaElement() instanceof IMethod)) continue;
			MethodBoxModel methodBox = new MethodBoxModel(this, (IMethod)decl.getJavaElement(), MethodBoxModel.declaration);
			addToMenuList(methodBox);
		}
		for(InitializerWrapper init : initializers) {
			MethodBoxModel methodModel = new MethodBoxModel(this, init, MethodBoxModel.declaration);
			modelsOfMethodMembers.add(methodModel);
			declaredMethodsCurrentlyInMenu.add(methodModel);
			// (Don't use addToMenuList here because the Resources of multiple 
			// initializers will test equal and only the first will end up being added)
		}
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		for (ITypeBinding innerClassDecl: innerClassesAndEnums){
			if(!(innerClassDecl.getJavaElement() instanceof IType)) continue;
			Resource classRes = RJCore.jdtElementToResource(repo, innerClassDecl.getJavaElement());
			InstanceModel innerClass = new InstanceModel(null, InstanceUtil.getClassName(classRes, repo), classRes);
			addClassToMenuList(innerClass);
		}

		Collections.sort(modelsOfMethodMembers, methodComparator);
		updateMethodLists();
	}

	private void findMembersUsingRepo() {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		
		// find methods declared in this class
		StatementIterator containsIter = repo.getStatements(getResource(), RSECore.contains, null);
		while(containsIter.hasNext()) {
			Value containee = containsIter.next().getObject();
			if(containee==null || !(containee instanceof Resource)) continue;

			Artifact tempContaineeArt = new Artifact((Resource)containee);
			Resource containeeType = tempContaineeArt.queryType(repo);
			if(!RJCore.methodType.equals(containeeType)) continue;
			
			// FYI, the AbstractTypeDeclaration for an anonymous class will be null,
			// so this code will be reached and use of the repo info should ensure that
			// the anon class's members button list contains methods that the anon class
			// has overridden, along with the constructor that created the anon class. 
			// (Important that constructor is included in the members list because then 
			// it is easy to add it to the diagram and use its called by or calls nav 
			// aids to easily connect to the call that created the anon class or 
			// the constructor of the class the anon class extends or implements).

			MethodBoxModel methodBox = new MethodBoxModel(this, (Resource)containee, MethodBoxModel.declaration);
			addToMenuList(methodBox);
			ASTNode methodBoxASTNode = methodBox.getASTNode();
			if(methodBoxASTNode instanceof MethodDeclaration) declaredAndInheritedMethods.add(((MethodDeclaration)methodBoxASTNode).resolveBinding());
		}

		// find inherited methods
		IJavaElement superElt = null;
		if(instanceElt instanceof IType) {
			for(Resource superRes : InstanceUtil.getSuperClasses(getResource())) {
				try {
					// Note: A customer error log reported a null value for 
					// ((IType)getJaveElement()).getSuperclassName() here. Not
					// exactly sure why this would be null if according to the
					// repo a super class exists (superRes and its name are non-null);
					// perhaps there are 2 resources in different projects with the
					// same name and one of them has a super class (causing the 
					// inherits stmt in the repo) and the other (this instance) doesn't?
					if(repo.queryName(superRes)!=null && 
							repo.queryName(superRes).equals(((IType)instanceElt).getSuperclassName())) {
						superElt = RJCore.resourceToJDTElement(repo, superRes);
						break;
					}
				} catch (JavaModelException e) {
					logger.error("Unexpected exception while finding element for " +
							"super class of " + instanceElt.getElementName(), e);
				}
			}
		}
		if(superElt!=null) {
			addDeclarationsInSuper(superElt);
			for(IMethodBinding decl : declaredAndInheritedMethods) {
				if(!(decl.getJavaElement() instanceof IMethod)) continue;
				MethodBoxModel methodBox = new MethodBoxModel(this, (IMethod)decl.getJavaElement(), MethodBoxModel.declaration);
				addToMenuList(methodBox);
			}
		}

		Collections.sort(modelsOfMethodMembers, methodComparator);
		updateMethodLists();
	}

	// Add all method declarations from any super classes in the
	// type hierarchy that could be invoked on an instance of the subclass 
	// (i.e., are not constructors, private, overridden, or static)
	private void addDeclarationsInSuper(Resource thisResource) {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		List<Resource> allSuperClasses = new ArrayList<Resource>();
		InstanceUtil.getAllSuperClassesInTypeHierarchy(thisResource, allSuperClasses);
		for (Resource superClassRes : allSuperClasses) {
			IJavaElement superElmt = RJCore.resourceToJDTElement(repo, superClassRes);
			addDeclarationsInSuper(superElmt);
		}
	}

	private void addDeclarationsInSuper(IJavaElement superElmt) {
		AbstractTypeDeclaration superTypeDecl =  InstanceUtil.getTypeDeclaration(superElmt);
		if(superTypeDecl==null || 
				!(superTypeDecl instanceof TypeDeclaration)) // a superclass must be a class (not an enum)
			return;

		for(MethodDeclaration declInSuper : ((TypeDeclaration)superTypeDecl).getMethods()) {

			// super class's constructor is not a method that 
			// can be invoked on an instance of the subclass
			if(declInSuper.isConstructor()) continue; 

			// cannot invoke a private superclass method  
			// on an instance of the subclass
			if(Modifier.isPrivate(declInSuper.resolveBinding().getModifiers())) continue;

			// the super class method could not be invoked on a subclass instance 
			// because only the method declared in the subclass that overrides it 
			// could be invoked
			if(MethodUtil.isOverriden(declInSuper, declaredAndInheritedMethods)) continue;
			if(MethodUtil.isOverridenByAnonymousClass(declInSuper, instanceElt)) continue;

			// concept of inherited methods is part of polymorphism, 
			// which static methods don't participate in
			if(Modifier.isStatic(declInSuper.resolveBinding().getModifiers())) continue;

			if(!declaredAndInheritedMethods.contains(declInSuper.resolveBinding())) declaredAndInheritedMethods.add(declInSuper.resolveBinding());
		}
	}

	private void addClassToMenuList(InstanceModel modelToAdd) {
		for(InstanceModel classInMenu : modelsOfTypeMembers) {
			if(classInMenu.getArt().elementRes.equals(modelToAdd.getArt().elementRes))
				return;
		}
		modelsOfTypeMembers.add(modelToAdd);
	}

	private void addToMenuList(MethodBoxModel modelToAdd) {
		for(MethodBoxModel methodInMenu : modelsOfMethodMembers) {
			if(methodInMenu.getArt().elementRes.equals(modelToAdd.getArt().elementRes))
				return;
		}
		modelsOfMethodMembers.add(modelToAdd);
		updateMethodLists();
	}

	public int getInstanceBoxWidth() {
		if (instanceBoxWidth == -1)
			return getFigure().getBounds().width;
		return instanceBoxWidth;
	}

	public void setInstanceBoxWidth(int instanceBoxWidth) {
		this.instanceBoxWidth = instanceBoxWidth;
		this.firePropChang(PROPERTY_WIDTH);
	}

	public static Comparator<MethodBoxModel> methodComparator = new Comparator<MethodBoxModel>() {
		public int compare(MethodBoxModel method1, MethodBoxModel method2) {

			// Constructors listed first in alphabetical order
			boolean method1isAConstructorCall = (method1.getASTNode() instanceof MethodDeclaration && ((MethodDeclaration)method1.getASTNode()).isConstructor());
			boolean method2isAConstructorCall = (method2.getASTNode() instanceof MethodDeclaration && ((MethodDeclaration)method2.getASTNode()).isConstructor());
			if(method1isAConstructorCall && !method2isAConstructorCall) return -1;
			if(method2isAConstructorCall && !method1isAConstructorCall) return 1;

			// Then initializers in the order they appear in the source
			boolean method1IsInitializer = (method1.getMethod() instanceof InitializerWrapper);
			boolean method2IsInitializer = (method2.getMethod() instanceof InitializerWrapper);
			if(method1IsInitializer && !method2IsInitializer) return -1;
			if(method2IsInitializer && !method1IsInitializer) return 1;
			if(method1IsInitializer && method2IsInitializer) 
				return new Integer(method1.getMethod().getOccurrenceCount()).
				compareTo(new Integer(method2.getMethod().getOccurrenceCount()));

			// Then remaining methods listed alphabetically
			String method1NameWithParams = MethodUtil.getMethodName(method1.getMethod(), null, false);
			String method2NameWithParams = MethodUtil.getMethodName(method2.getMethod(), null, false);
			return method1NameWithParams.toLowerCase().compareTo(method2NameWithParams.toLowerCase());
		}
	};

}
