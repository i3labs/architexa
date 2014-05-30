package com.architexa.diagrams.chrono.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.commands.InstanceCreateCommand;
import com.architexa.diagrams.chrono.commands.InstanceDeleteCommand;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.UserCreatedInstanceModel;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.jdt.IJEUtils;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.compat.ASTUtil;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class InstanceUtil {

	private static final Logger logger = SeqPlugin.getLogger(InstanceUtil.class);
	public static List<InstanceModel> instancesAdded;

	public static ImageDescriptor getInstanceIconDescriptor(InstanceModel model) {
		if (model instanceof UserCreatedInstanceModel) return null;

		// If have an IJavaElement for the instance model, use it to get the icon
		// from eclipse, which will handle adding appropriate decorations like
		// final, static, abstract, etc. 
		if(model.getInstanceElem()!=null) {
			Image img = new JavaUILabelProvider().getImage(model.getInstanceElem());
			if(img!=null) return ImageDescriptor.createFromImage(img);
		}

		String iconKey = null;
		Resource type = model.getType();
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		Resource access = InstanceModel.getAccess(repo, model.getArt());

		if (isAnonymousClass(model))
			iconKey = ISharedImages.IMG_OBJS_CLASS_DEFAULT;
		else if (isEnum(model))
			// TODO access will be wrong for protected and private enums. See 
			// ticket #739: protected enums have an access stmt in repo that says they are 
			// public, and private enums have an access stmt that says they have no access.
			iconKey = getEnumIconKey(access);
		else if (type == null || !RSECore.isInitialized(repo, model.getArt().elementRes)) 
			iconKey = ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE;
		else if (type.equals(RJCore.classType))
			iconKey = ISharedImages.IMG_OBJS_CLASS;
		else  if (type.equals(RJCore.interfaceType)) 
			iconKey = ISharedImages.IMG_OBJS_INTERFACE;

		return SeqUtil.getImageDescriptorFromKey(iconKey);
	}

	public static boolean isAnonymousClass(InstanceModel instance) {
		IJavaElement instanceElt = instance.getInstanceElem();
		if (instanceElt == null) return false;
		if (!(instanceElt instanceof SourceType)) return false;
		return ((SourceType)instanceElt).isAnonymous();
	}

	public static boolean isEnum(InstanceModel instance) {
		if (instance.getInstanceElem()==null) return false;
		if (!(instance.getInstanceElem() instanceof IType)) return false;
		try {
			return ((IType)instance.getInstanceElem()).isEnum();
		} catch (JavaModelException e) {
			logger.error("Unexpected exception while testing whether " +
					"instance " + instance + " represents an enum.", e);
		}
		return false;
	}

	public static boolean isStatic(InstanceModel instance) {
		boolean isStatic = false;
		try {
			isStatic = instance.getInstanceElem() instanceof IType &&
			(((IType)instance.getInstanceElem()).getFlags()&Flags.AccStatic)!=0;
		} catch(Exception e) {
			logger.error("Unexpected exception while testing " +
					"whether instance " + instance + " is static. ", e);
		}
		return isStatic;
	}

	private static String getEnumIconKey(Resource access) {
		if (RJCore.publicAccess.equals(access)) return ISharedImages.IMG_OBJS_ENUM;
		if (RJCore.protectedAccess.equals(access)) return ISharedImages.IMG_OBJS_ENUM_PROTECTED;
		if (RJCore.privateAccess.equals(access)) return ISharedImages.IMG_OBJS_ENUM_PRIVATE;
		return ISharedImages.IMG_OBJS_ENUM;
	}

	public static String getClassName(Resource classRes, ReloRdfRepository repo) {
		String className = classRes.toString();
		IJavaElement classElt = RJCore.resourceToJDTElement(repo, classRes);
		String nameStr = repo.queryName(classRes);

		if (classElt == null) {
			if(nameStr!=null) className = nameStr;
			return className;
		}
		
		if (classElt.getElementName()!=null && !classElt.getElementName().equals("")) className = classElt.getElementName().replace(".java", "");
		else if (nameStr!=null) className = nameStr;
		
		// anonymous class - displaying name the way it is shown in the
		// Eclipse Outline view, "new AnonClass() {..}"
		if (classElt instanceof SourceType && ((SourceType)classElt).isAnonymous()) {
			Value superClass = repo.getStatement(classRes, RJCore.inherits, null).getObject();
			if (superClass!=null) {
				String name = getClassName((Resource)superClass, repo);
				String arguments = CodeUnit.getAnonClassArgumentTypes(repo, classRes);
				return "new "+name+"("+arguments+") {..}";
			}
		}

		// If class is an inner class, append name of outer class before class name  
		IJavaElement parentElt = classElt.getParent();
		while (parentElt instanceof IType) {
			className = parentElt.getElementName() + "." + className;
			parentElt = parentElt.getParent();
		}
		return className;
	}

	/**
	 * 
	 * @return all classes that the given class extends or implements. If the given
	 * class does not extend or implement anything, an empty list is returned.
	 */
	public static List<Resource> getSuperClasses(Resource subclass) {
		List<Resource> superClasses = new ArrayList<Resource>();
		StatementIterator inheritsIter = StoreUtil.getDefaultStoreRepository().getStatements(subclass, RJCore.inherits, null);
		if (inheritsIter==null) return superClasses;

		while(inheritsIter.hasNext()) {
			Statement stmt = inheritsIter.next();
			if (stmt==null || !(stmt.getObject() instanceof Resource)) continue;
			superClasses.add((Resource)stmt.getObject());
		}
		return superClasses;
	}

	/**
	 * Adds to the given list all classes in the given class's super 
	 * type hierarchy, i.e. all super classes of the given class, 
	 * all super classes of those classes, and so on
	 */
	public static void getAllSuperClassesInTypeHierarchy(Resource subclass, List<Resource> allSuperClasses) {
		List<Resource> directSuperClasses = getSuperClasses(subclass);
		for(Resource superClass : directSuperClasses) {
			allSuperClasses.add(superClass);
			getAllSuperClassesInTypeHierarchy(superClass, allSuperClasses);
		}
	}

	/**
	 * Returns true if subClass and superClass represent two different classes and the
	 * class represented by subClass extends or implements the class represented by superClass
	 * @return true if subClass is a subclass of superClass, false otherwise
	 */
	public static boolean isASubClass(InstanceModel subClass, InstanceModel superClass) {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		StatementIterator iter = repo.getStatements(subClass.getResource(), RJCore.inherits, superClass.getResource());
		if (iter.hasNext()) return true;
		return false;
	}

	/**
	 * @return If the diagram already contains an instance model with given name, 
	 * class name, and Resource returns that instance, otherwise returns null
	 */
	public static InstanceModel findInstanceModel(String instanceName, String className, Resource classRes, DiagramModel diagram) {
		for (ArtifactFragment af : diagram.getChildren()) {
			if (!(af instanceof InstanceModel)) continue;
			InstanceModel im = (InstanceModel)af;
			boolean sameInstanceName = (instanceName==null && im.getInstanceName()==null) || (instanceName!=null && instanceName.equals(im.getInstanceName()));
			if (className.equals(im.getClassName()) && sameInstanceName && classRes.equals(im.getResource())) {
				return im;
			}
		}
		
		// instancesAdded will be null unless we are inside an addAll cmd
		if (instancesAdded==null) return null;
		for (InstanceModel im : instancesAdded) {
			if (im.getResource().toString().equals(classRes.toString())) {
				if (im.getInstanceName()!=null ) {
					if (im.getInstanceName().equals(instanceName)) // if we have an instanceName make sure it matches
						return im;
					else
						continue;
				}
				return im;
			}
		}
		return null;
	}

	/**
	 * Returns an InstanceModel representing the instance whose name, class name, 
	 * and Resource are given. If the diagram already contains an InstanceModel 
	 * representing the instance, that InstanceModel is returned. Otherwise, a 
	 * new InstanceModel is created, added to the diagram at the given index, 
	 * and then returned.
	 * 
	 */
	public static InstanceModel findOrCreateContainerInstanceModel(String instanceName, String className, Resource classRes, DiagramModel diagram, int indexToAddAt, CompoundCommand cmd, IJavaElement instanceElm) {
		InstanceModel instance = findInstanceModel(instanceName, className, classRes, diagram);
		if (instance!=null) return instance;

		for (Object o : CommandUtil.getAllContainedCommands(cmd)) {
			if (!(o instanceof InstanceCreateCommand)) continue;
			InstanceModel im = ((InstanceCreateCommand)o).getChild();
			boolean sameInstanceName = (instanceName==null && im.getInstanceName()==null) || (instanceName!=null && instanceName.equals(im.getInstanceName()));
			if (className.equals(im.getClassName()) && sameInstanceName && classRes.equals(im.getResource())) {
				return im;
			}
		}

		// Model for the instance isn't already present in the diagram,
		// so make a model for it and add it to the diagram
		if (instanceElm == null)
			instance = new InstanceModel(instanceName, className, classRes);
		else
			instance = new InstanceModel(instanceName, className, classRes, instanceElm);
		if (instancesAdded!=null)
			instancesAdded.add(instance);
		InstanceCreateCommand newInstanceCmd = new InstanceCreateCommand(instance, diagram, indexToAddAt, true, true);
		cmd.add(newInstanceCmd);

		return instance;
	}

	public static InstanceModel findOrCreateContainerInstanceModel(String instanceName, String className, Resource classRes, DiagramModel diagram, int indexToAddAt, CompoundCommand cmd) {
		return findOrCreateContainerInstanceModel(instanceName, className, classRes, diagram, indexToAddAt, cmd, null);
	}
	/**
	 * Returns the line number corresponding to the given source character position 
	 * in the original source string. Returns -1 if no corresponding source line
	 * could be found or -2 if no line number information is available
	 * 
	 * @see CompilationUnit#getLineNumber(int)
	 */
	public static int getLineNumber(InstanceModel instance, int position) {
		CompilationUnit cu = getCompilationUnitForInstance(instance);
		if (cu==null) return -1;
		return cu.getLineNumber(position);
	}

	/**
	 * Returns the declaration that corresponds to the given class or interface
	 */
	public static AbstractTypeDeclaration getTypeDeclaration(InstanceModel instance) {
		IJavaElement instanceElt = instance.getInstanceElem();
		return getTypeDeclaration(instanceElt);
	}

	/**
	 * Returns the declaration that corresponds to the given class or interface
	 */
	public static AbstractTypeDeclaration getTypeDeclaration(IJavaElement instanceElt) {
		CompilationUnit cu = getCompilationUnitForInstance(instanceElt);
		if (instanceElt==null || cu==null) return null;

		for (Object obj : cu.types()) {
			if (!(obj instanceof TypeDeclaration)) continue;
			TypeDeclaration typeDecl = (TypeDeclaration) obj;

			if (typeDecl.resolveBinding()!=null && 
					instanceElt.equals(typeDecl.resolveBinding().getJavaElement())) 
				return typeDecl;

			List<BodyDeclaration> nestedDecls = new ArrayList<BodyDeclaration>();
			for (Object bodyDecl : typeDecl.bodyDeclarations()) nestedDecls.add((BodyDeclaration)bodyDecl); 
			AbstractTypeDeclaration nestedType = findNestedType(nestedDecls, instanceElt);
			if (nestedType!=null) return nestedType;
		}
		return null;
	}

	private static AbstractTypeDeclaration findNestedType(List<BodyDeclaration> bodyDecls, IJavaElement instanceElt) {
		for (BodyDeclaration bodyDecl : bodyDecls) {
			if (bodyDecl instanceof TypeDeclaration || 
					bodyDecl instanceof EnumDeclaration) {
				AbstractTypeDeclaration typeOrEnumDecl = (AbstractTypeDeclaration) bodyDecl;
				if (typeOrEnumDecl.resolveBinding()!=null && 
						instanceElt.equals(typeOrEnumDecl.resolveBinding().getJavaElement()))
					return typeOrEnumDecl;

				List<BodyDeclaration> nestedDecls = new ArrayList<BodyDeclaration>();
				for (Object obj : typeOrEnumDecl.bodyDeclarations()) nestedDecls.add((BodyDeclaration)obj); 
				AbstractTypeDeclaration nestedType = findNestedType(nestedDecls, instanceElt);
				if (nestedType!=null) return nestedType;
			}
		}
		return null;
	}

	private static CompilationUnit getCompilationUnitForInstance(InstanceModel instance) {
		IJavaElement instanceElt = RJCore.resourceToJDTElement(StoreUtil.getDefaultStoreRepository(), instance.getResource());
		return getCompilationUnitForInstance(instanceElt);
	}

	private static CompilationUnit getCompilationUnitForInstance(IJavaElement instanceElt) {
		IJavaElement typeRoot = instanceElt;
		while (typeRoot!=null && !(IJEUtils.isTypeRoot(typeRoot))) typeRoot = typeRoot.getParent();
		if (IJEUtils.isTypeRoot(typeRoot))
			return ASTUtil.getAst(typeRoot);
		return null;
	}

	public static void getInstanceDeleteCommand(DiagramModel diagram, InstanceModel instance , List<EditPart> selectedParts, CompoundCommand command){
		for (MemberModel model:instance.getMemberChildren()){
			if (model instanceof MethodBoxModel)
				MemberUtil.getDeclarationDeleteCommand(model, instance, selectedParts, command, true);
			else if (model instanceof FieldModel)
				MemberUtil.deleteFieldDeclaration((FieldModel) model, instance, command, true);
		}
		command.add(new InstanceDeleteCommand(diagram,instance,diagram.getChildren().indexOf(instance)));
	}
}
