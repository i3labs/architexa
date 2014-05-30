package com.architexa.diagrams.chrono.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.CallLocation;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.OverrideIndicatorLabelDecorator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.collab.proxy.PluginUtils;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.animation.AnimateChainExpandCommand;
import com.architexa.diagrams.chrono.commands.ConnectionCreateCommand;
import com.architexa.diagrams.chrono.commands.ConnectionDeleteCommand;
import com.architexa.diagrams.chrono.commands.MemberCreateCommand;
import com.architexa.diagrams.chrono.commands.MemberDeleteCommand;
import com.architexa.diagrams.chrono.controlflow.AddIfBlockCommand;
import com.architexa.diagrams.chrono.controlflow.AddLoopBlockCommand;
import com.architexa.diagrams.chrono.controlflow.ControlFlowModel;
import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.MethodInvocationModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.chrono.sequence.StatementHandler;
import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.AnonymousClassConstructor;
import com.architexa.diagrams.jdt.CompilerGeneratedDefaultConstructor;
import com.architexa.diagrams.jdt.ImplicitConstructorCallLocation;
import com.architexa.diagrams.jdt.InitializerWrapper;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.compat.ASTUtil;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.utils.MethodInvocationFinder;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.ui.ImageCache;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class MethodUtil {
	private static final Logger logger = SeqPlugin.getLogger(MethodUtil.class);

	public static int indexNotSpecified = -287;

	public static IMethodBinding findMethodBindingInType(Type type, IMethod method) {
		for(IMethodBinding methodBinding : type.resolveBinding().getDeclaredMethods()) {
			if(method instanceof CompilerGeneratedDefaultConstructor &&
					methodBinding.isDefaultConstructor()) return methodBinding;
			if(method.equals(methodBinding.getJavaElement())) return methodBinding;
		}
		return null;
	}

	/**
	 * Returns a MethodBoxModel representing the given method declaration. If
	 * the given instance already contains a MethodBoxModel representing the
	 * method, that MethodBoxModel is returned. Otherwise, a new MethodBoxModel
	 * is created, added to the instance at the given index, and then returned.
	 * 
	 */
	public static MethodBoxModel findOrCreateMethodModel(IMethod method, Resource methodRes, InstanceModel instance, int indexToAddAt, CompoundCommand cmd) {
		for(ArtifactFragment af : instance.getChildren()) {
			if(!(af instanceof MethodBoxModel)) continue;
			MethodBoxModel mm = (MethodBoxModel)af;
			if(methodRes.equals(mm.getArt().elementRes)) return mm;
		}
		for(Object o : CommandUtil.getAllContainedCommands(cmd)) {
			if(!(o instanceof MemberCreateCommand)) continue;
			MethodBoxModel mm = (MethodBoxModel) ((MemberCreateCommand)o).getChild();
			if(methodRes.equals(mm.getArt().elementRes)) return mm;
		}
		// Model for the method isn't already present in the instance,
		// so make a model for it and add it to the instance
		MethodBoxModel methodModel = new MethodBoxModel(instance, method, MethodBoxModel.declaration);

		MemberCreateCommand createMethodCmd = new MemberCreateCommand(methodModel, instance, MemberCreateCommand.FROM_INSTANCE);
		cmd.add(createMethodCmd);

		return methodModel;
	}

	/**
	 * 
	 * @return the name, including return type and parameter types,
	 * of the given method declaration
	 */
	public static String getMethodName(MethodDeclaration declaration) {
		if(declaration.resolveBinding()==null || 
				!(declaration.resolveBinding().getJavaElement() instanceof IMethod)) return "";
		IMethod method = (IMethod)declaration.resolveBinding().getJavaElement();
		Resource methodRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), method);
		return getMethodName(methodRes, declaration, true);
	}

	/**
	 * 
	 * @param fromChainedCall 
	 * @return the name, including return type and parameter types,
	 * of the given method declaration
	 */
	public static String getMethodName(IMethod declaration, Invocation invocation, boolean fromChainedCall) {
		if (declaration instanceof AnonymousClassConstructor)
			return ((AnonymousClassConstructor) declaration).getName();
		if (declaration instanceof InitializerWrapper)
			return CodeUnit.initializerStringRep;

		Resource methodRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), declaration);
		if (invocation != null && !fromChainedCall)
			return getMethodName(methodRes, invocation);

		return getMethodName(methodRes, null, true);
	}

	public static String getMethodName(Value method, Invocation invocation) {

		Invocation expressionInvoc = null;
		if(invocation.getExpression() instanceof MethodInvocation)
			expressionInvoc = new Invocation((MethodInvocation)invocation.getExpression());
		else if(invocation.getExpression() instanceof SuperMethodInvocation)
			expressionInvoc = new Invocation((SuperMethodInvocation)invocation.getExpression());
		if(expressionInvoc!=null) {
			Resource expressionRes = RJCore.bindingToResource(StoreUtil.getDefaultStoreRepository(), expressionInvoc.resolveMethodBinding());
			return getMethodName(expressionRes, expressionInvoc)+"."+getMethodName(method, invocation.getInvocation());
		}

		return getMethodName(method, invocation.getInvocation());
	}

	/**
	 * 
	 * @return the name of the method that corresponds to the 
	 * given Value, including return type and parameter types 
	 */
	public static String getMethodName(Value method, ASTNode node) {
		return getMethodName(method, node, true);
	}

	// TODO: in order to return actual parameter values and not types 
	// (for example foo(5) instead of foo(int)), use information from 
	// the given ASTNode instead of the given Value
	/**
	 * @return the name of the method that corresponds to the given Value, including 
	 * return type. Parameter types are only included if includeArguments is true
	 */
	public static String getMethodName(Value method, ASTNode node, boolean includeArguments) {
		if(method == null) return "void";

		String message = "";
		String nameStr;
		if((nameStr=StoreUtil.getDefaultStoreRepository().queryName((Resource) method))!=null && nameStr!=null){
			message = nameStr;
			if("<clinit>".equals(message)) return CodeUnit.initializerStringRep;
			if(!includeArguments) return message;
			int argumentsIndex = method.toString().indexOf("(");
			if(argumentsIndex >= 0) {
				String arguments = method.toString().substring(argumentsIndex);
				message = message.concat(arguments);
			}
		} else {
			message = method.toString();
			int packageStartIndex = message.indexOf("#");
			if(packageStartIndex >= 0) {
				message = message.substring(packageStartIndex).replace("#", "");
			}
			int classStartIndex = message.indexOf("$");
			if(classStartIndex >= 0) {
				message = message.substring(classStartIndex).replace("$", "");
			}
			int methodStartIndex = message.indexOf(".");
			if(methodStartIndex >= 0) {
				message = message.substring(methodStartIndex).replace("\\.", "");
			}
		}
		message = message.replace(",)", ")");
		if(message.indexOf(".")==0) message = message.replaceFirst("\\.", "");
		if(node instanceof SuperMethodInvocation) message = "super." + message;
		return message;
	}

	public static ImageDescriptor getMethodIconDescriptor(MethodBoxModel method, boolean isInherited) {

		// If have an IJavaElement for the method model, use it to get the icon
		// from eclipse, which will handle adding appropriate decorations like
		// final, static, abstract, etc.
		if(method.getMember()!=null) {

			// Handle anon class constructor ourselves because it has
			// no flags and eclipse will return an "object unknown" image
			if (method.getMember() instanceof AnonymousClassConstructor) {
				ImageDescriptor desc = SeqUtil.getImageDescriptorFromKey(ISharedImages.IMG_OBJS_CLASS_DEFAULT);
				if(desc!=null) return desc;
			}

			// Create label provider and add overrides/implements decorator
			JavaUILabelProvider lblProv = new JavaUILabelProvider();
			lblProv.addLabelDecorator(new OverrideIndicatorLabelDecorator());

			Image img = lblProv.getImage(method.getMember());
			if(img!=null) return ImageDescriptor.createFromImage(img);
		}

		ImageDescriptor desc = null;

		if ("<clinit>".equals(method.getArt().queryName(StoreUtil.getDefaultStoreRepository()))) {
			// give static and instance initializers same icons
			// as eclipse uses for them in the package explorer
			if (RJCore.isStatic(method.getMethod())) {
				desc = Activator.getImageDescriptor("icons/static_initializer.png");
				if (desc == null)
					logger.error("Null image descriptor for static_initializer method: " + method.getMethod() + "\nresource" + method.getMethodRes());
				return desc;	
//				return Activator.getImageDescriptor("icons/static_initializer.png");
			}
			desc = SeqUtil.getImageDescriptorFromKey(ISharedImages.IMG_OBJS_PRIVATE);
			if (desc == null)
				logger.error("Null image descriptor for IMG_OBJS_PRIVATE method: " + method.getMethod() + "\nresource" + method.getMethodRes());
			return desc;
//			return SeqUtil.getImageDescriptorFromKey(ISharedImages.IMG_OBJS_PRIVATE);
		}

		Resource access = method.getMethodAccess();
		if (isInherited) {
			desc = getMethodIconDescriptor(access);
			if (desc == null)
				logger.error("Null image descriptor for access method: " + method.getMethod() + "\nresource" + method.getMethodRes() + "\n Access: " + access);
			return desc;
//			return getMethodIconDescriptor(access);
		}

		if (MethodUtil.isConstructor(method)) {
			desc = SeqPlugin.getImageDescriptor("icons/constructor.png");
			if (desc == null)
				logger.error("Null image descriptor for icons/constructor.png method: " + method.getMethod() + "\nresource" + method.getMethodRes());
			return desc;
//			return SeqPlugin.getImageDescriptor("icons/constructor.png");
		}

		desc = MethodUtil.getMethodIconDescriptor(access);

		if (MethodUtil.isOverrider(method)) {
			OverrideIndicatorLabelDecorator decorator = new OverrideIndicatorLabelDecorator();
			Image decoratedImg = decorator.decorateImage(ImageCache.calcImageFromDescriptor(desc), method.getMember());
			return ImageDescriptor.createFromImage(decoratedImg);
		}

		return desc;
//		return MethodUtil.getMethodIconDescriptor(access);
	}

	public static ImageDescriptor getMethodIconDescriptor(Resource access) {
		String iconKey = null;
		if(access==null) 
			iconKey = ISharedImages.IMG_OBJS_DEFAULT;
		else if (access.equals(RJCore.publicAccess))
			iconKey = ISharedImages.IMG_OBJS_PUBLIC;
		else if (access.equals(RJCore.protectedAccess))
			iconKey = ISharedImages.IMG_OBJS_PROTECTED;
		else if (access.equals(RJCore.privateAccess))
			iconKey = ISharedImages.IMG_OBJS_PRIVATE;
		else iconKey =  ISharedImages.IMG_OBJS_DEFAULT;

		return SeqUtil.getImageDescriptorFromKey(iconKey);
	}

	/**
	 * A subclass constructor must always call a superclass constructor. If the subclass
	 * does not explicitly call a superclass constructor, the compiler calls the
	 * default no-argument superclass constructor
	 * @param declarationModel 
	 * @param explicitInvocations a list of the invocations explicitly made
	 * in declarationModel
	 * @return the default no-argument superclass constructor if declarationModel is 
	 * the default constructor and does not invoke a superclass constructor explicitly. 
	 * Otherwise returns null
	 */
	public static Invocation getImplicitConstructorCall(MethodBoxModel declarationModel, List<Invocation> explicitInvocations) {

		for(Invocation invocation : explicitInvocations) {
			if(invocation.getInvocation() instanceof SuperConstructorInvocation) {
				// already calls a superclass constructor
				return null;
			}
		}

		ASTNode declAST = declarationModel.getASTNode();
		// Using MethodDeclaration.isConstructor() instead of 
		// ASTNode.getStructuralProperty(MethodDeclaration.CONSTRUCTOR_PROPERTY)
		// since some nodes may not have that property
		boolean isAConstructor = (declAST instanceof MethodDeclaration) ? ((MethodDeclaration)declAST).isConstructor() : false;
		if(!isAConstructor || !(declAST.getParent() instanceof TypeDeclaration)) return null;

		Type superType = ((TypeDeclaration)declAST.getParent()).getSuperclassType();
		if(superType==null) return null;

		for(IMethodBinding declaredMethod : superType.resolveBinding().getDeclaredMethods()) {
			if(!declaredMethod.isConstructor()) continue; // not a constructor
			IMethod constructor = (IMethod) declaredMethod.getJavaElement();
			if(constructor==null || // is the default constructor of a source class
					constructor.getParameterTypes().length != 0) continue; // is not the default constructor

			AST ast = ((MethodDeclaration)declAST).getAST();
			SuperConstructorInvocation superConstructorInvocation = ast.newSuperConstructorInvocation();
			return new Invocation(superConstructorInvocation, declaredMethod);
		}

		return null;
	}

	/**
	 * Finds all of the method calls made inside the given method declaration,
	 * including implicit calls to the super class's no argument constructor if
	 * the declaration is a constructor, and adds to the diagram the ones made 
	 * to the given instance. If instance is null, all of the calls are added
	 * 
	 * @param methodModel the model of the method declaration
	 * @param decl the method declaration
	 * @param instance the instance that a call must have as its target in order for 
	 * that call to be added to the diagram or null if all calls should be added
	 * @param contents the controller for the whole diagram
	 * @param monitor 
	 * @return true if any calls were added to the diagram, false otherwise
	 */
	public static boolean displayCallsMadeByMethodDeclToInstance(MethodBoxModel methodModel, MethodDeclaration decl, InstanceModel instance, final DiagramEditPart contents, CompoundCommand addMultipleCallsCmd, IProgressMonitor monitor) {
		if(decl.getBody()==null) return false;

		StatementHandler statementHandler = new StatementHandler(methodModel, (DiagramModel)contents.getModel(), instance, addMultipleCallsCmd);
		for(Object obj : decl.getBody().statements()) {
			if (monitor.isCanceled()) return true;
			statementHandler.handleStatement((org.eclipse.jdt.core.dom.Statement)obj);
		}

		Invocation implicitConstructorCall = getImplicitConstructorCall(methodModel, statementHandler.getCallsList());

		// If an implicit constructor call is made, only add it if either 
		// no target instance has been specified or if the specified instance 
		// is the super class to which the implicit constructor belongs
		if(implicitConstructorCall!=null && 
				(instance==null || instance.getResource().equals(RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), implicitConstructorCall.resolveMethodBinding().getDeclaringClass().getJavaElement())))) {
			statementHandler.getCallsList().add(0, implicitConstructorCall);
			statementHandler.handleInvocation(implicitConstructorCall);
		}

		if(statementHandler.getCallsList().size()==0) return false;
		
		List<ControlFlowModel> ifBlockModels = statementHandler.getIfBlockModels();
		List<ControlFlowModel> loopBlockModels = statementHandler.getLoopBlockModels();

		DiagramModel dm = (DiagramModel) contents.getModel();
		for(final ControlFlowModel ifModel : ifBlockModels) {
			if (monitor.isCanceled()) return true;
//			addMultipleCallsCmd.addBreakPlace(AnimateCallMadeCommand.class);
			addMultipleCallsCmd.add(new AddIfBlockCommand(dm, ifModel));
		} 

		for(final ControlFlowModel loopModel : loopBlockModels) {
			if (monitor.isCanceled()) return true;
//			addMultipleCallsCmd.addBreakPlace(AnimateCallMadeCommand.class);
			addMultipleCallsCmd.add(new AddLoopBlockCommand(dm, loopModel));
		}
		return true;
	}

	
	public static Resource findInjectedBeanClass(Invocation invocation, IMember member) {
		// Check for Spring Injection
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		Resource memRes = RJCore.jdtElementToResource(repo, member);
		String str = memRes.toString();
		Resource fieldParentRes = null;
		String instance = MethodUtil.getInstanceCalledOn(invocation);
		if (instance != null) {
			String fieldStr = str.substring(str.indexOf("#") + 1, str.lastIndexOf(".") + 1);
			fieldStr = fieldStr + instance;
			Resource fieldRes = RJCore.idToResource(repo, fieldStr);
			fieldParentRes = SeqUtil.pip.interfaceGuesser(fieldRes);
		}
		return fieldParentRes;
	}
	
	/**
	 * Creates the method boxes and call and connection messages between them 
	 * for the method being invoked and adds the boxes to the correct instances
	 * 
	 * @param invocation the method being invoked
	 * @param model the model of the method declaration where the method 
	 * corresponding to Resource method is invoked
	 * @param diagram the model of the whole diagram
	 * @param containerClass the class containing the declaration of the invoked method
	 * @param containerModel the parent to which the created called method box model will be added
	 * @param conditionalBlockStmts is the list of stmts to be added to the conditional block calling to create this model
	 * @param classOfInstanceIsBean is the boolean if the class is injected from Spring
	 * @return the created method box corresponding to the method invocation or null if the 
	 * parent for the created declaration box model could not be determined
	 */
	public static MethodBoxModel createModelsForMethodRes(Invocation invocation, MethodBoxModel model, DiagramModel diagram, Resource containerClass, NodeModel containerModel, boolean isFirstCall, CompoundCommand command, List<MemberModel> conditionalBlockStmts, boolean classOfInstanceIsBean) {
		if(containerModel==null) containerModel = getContainerModel(invocation, model, diagram, containerClass, command);
		if(containerModel==null) return null;
		return createModelsForMethodRes(invocation, model, diagram, containerClass, containerModel, true, new LinkedHashMap<String, Boolean>(), isFirstCall, command, conditionalBlockStmts, classOfInstanceIsBean);
	}

	/**
	 * Creates the method boxes and call and connection messages between them 
	 * for the method being invoked and adds the boxes to the correct instances
	 * at the given indices
	 * 
	 * @param invocation the method being invoked
	 * @param model the model of the method declaration where the method 
	 * corresponding to Resource method is invoked
	 * @param diagram the model of the whole diagram
	 * @param containerClass the class containing the declaration of the invoked method
	 * @param containerModel the parent to which the created called method box model will be added
	 * @param isChained true if the message should be displayed as a set of chained invocations
	 * @param conditionalBlockStmts is the list of stmts to be added to the conditional block calling to create this model
	 * @param classOfInstanceIsBean 
	 * @return the created method box corresponding to the method invocation or null if the 
	 * parent for the created declaration box model could not be determined
	 */
	public static MethodBoxModel createModelsForMethodRes(Invocation invocation, MethodBoxModel model, DiagramModel diagram, Resource containerClass, NodeModel containerModel, boolean isChained, LinkedHashMap<String, Boolean> argLabelToIsExpandableMap, boolean isFirstCall, CompoundCommand command, List<MemberModel> conditionalBlockStmts, boolean classOfInstanceIsBean) {
		if(containerModel==null) containerModel = getContainerModel(invocation, model, diagram, containerClass, command);
		if(containerModel==null) return null;

		if (containerModel instanceof InstanceModel && classOfInstanceIsBean)
			((InstanceModel)containerModel).setIsBean(true);
		
		MethodBoxModel callerMethodBox = createInvocationModel(invocation, model.getInstanceModel());
		
		callerMethodBox.setIsAChainedCall(isChained);
		InstanceModel calledBoxInstance = (containerModel instanceof InstanceModel) ? (InstanceModel)containerModel : ((MethodBoxModel)containerModel).getInstanceModel();
		IMethod method = callerMethodBox.getMethod();
		if (classOfInstanceIsBean) {
			for (Artifact artChild : containerModel.queryChildrenArtifacts(StoreUtil.getDefaultStoreRepository())) {
				Resource callerMethodBoxRes = callerMethodBox.getArt().elementRes;
				Resource artRes = artChild.elementRes;
				if (getMethodFromRes(callerMethodBoxRes).equals(getMethodFromRes(artRes)))
					method = (IMethod) RJCore.resourceToJDTElement(StoreUtil.getDefaultStoreRepository(), artChild.elementRes);
			}
		}
		MethodBoxModel calledMethodBox = new MethodBoxModel(calledBoxInstance, method, MethodBoxModel.declaration);
		
		NodeModel declarationModelParent;
		if(containerModel.equals(model)) declarationModelParent = callerMethodBox;
		else declarationModelParent = containerModel;

		MemberCreateCommand createInvocationCmd = new MemberCreateCommand(callerMethodBox, model, MemberCreateCommand.NONE);
		MemberCreateCommand createDeclarationCmd = new MemberCreateCommand(calledMethodBox, declarationModelParent, "");
		createDeclarationCmd.setAccessPartner(callerMethodBox, model);

		ConnectionCreateCommand callConn = new ConnectionCreateCommand(callerMethodBox, calledMethodBox, MethodUtil.getMethodName(method, invocation, false), ConnectionModel.CALL, argLabelToIsExpandableMap);							
		ConnectionCreateCommand returnConn = new ConnectionCreateCommand(calledMethodBox, callerMethodBox, MethodUtil.getReturnMessage(invocation), ConnectionModel.RETURN);

		command.add(createInvocationCmd);
		command.add(createDeclarationCmd);
		command.add(callConn);
		command.add(returnConn);

		FieldUtil.handleFieldReadOrWrite(invocation, model, model.getChildren().indexOf(callerMethodBox), diagram, command, conditionalBlockStmts);

		if(command instanceof AnimateChainExpandCommand){
			if(isFirstCall){
				((AnimateChainExpandCommand) command).setFirstCallInvocation(callerMethodBox);
				((AnimateChainExpandCommand) command).setFirstCallDeclaration(calledMethodBox);
				((AnimateChainExpandCommand) command).setFirstCallCallMessage(MethodUtil.getMethodName(method, invocation, false));
				((AnimateChainExpandCommand) command).setFirstCallReturnMessage(MethodUtil.getReturnMessage(invocation));

				((AnimateChainExpandCommand) command).setNewDeclaration(calledMethodBox);
				if(declarationModelParent instanceof InstanceModel)
					((AnimateChainExpandCommand) command).setInstance((InstanceModel) declarationModelParent);
				else if(declarationModelParent instanceof MemberModel)
					((AnimateChainExpandCommand) command).setInstance(((MemberModel)declarationModelParent).getInstanceModel());
			}else{
				((AnimateChainExpandCommand) command).setSecondCallInvocation(callerMethodBox);
				((AnimateChainExpandCommand) command).setSecondCallDeclaration(calledMethodBox);
				((AnimateChainExpandCommand) command).setSecondCallCallMessage(MethodUtil.getMethodName(callerMethodBox.getMethodRes(), invocation));
				((AnimateChainExpandCommand) command).setSecondCallReturnMessage(MethodUtil.getReturnMessage(invocation));
			}
		}
		// add to conditional stmts
		if (conditionalBlockStmts != null)
			conditionalBlockStmts.add(callerMethodBox);
		return callerMethodBox;
	}

	private static String getMethodFromRes(Resource res) {
		int lastDot = res.toString().lastIndexOf(".");
		if (lastDot == -1) return null;
		return res.toString().substring(lastDot);
	}

	public static Invocation getInvocation(CallLocation callLoc, IMethod declarationMakingCall) {
		if(callLoc instanceof ImplicitConstructorCallLocation) {
			CompilationUnit cu = ASTUtil.getAst(declarationMakingCall.getCompilationUnit());
			AST ast = cu.getAST();
			SuperConstructorInvocation superConstructorInvocation = ast.newSuperConstructorInvocation();

			// Add any arguments to super constructor invocation
			// Only need to worry about arguments if the implicit call is the
			// one made when an anonymous class is defined. The other type of
			// implicit constructor call is due to the compiler implicitly 
			// assuming a constructor body begins with "super()", which takes 
			// no arguments
			if(declarationMakingCall instanceof AnonymousClassConstructor) {
				// Implicit call to the superclass constructor that is made 
				// when an anonymous class is defined. Give the super
				// constructor invocation any passed arguments (the actual
				// values, not the types)
				List<Expression> passedArgs = ((AnonymousClassConstructor)declarationMakingCall).getPassedArguments();
				for(Expression passedArg : passedArgs) {
					ASTNode passedArgCopy = Expression.copySubtree(ast, passedArg);
					superConstructorInvocation.arguments().add((Expression)passedArgCopy);
				}
			}

			IMethodBinding superclassConstructorBinding = ((ImplicitConstructorCallLocation)callLoc).getSuperclassConstructorBinding();
			return new Invocation(superConstructorInvocation, superclassConstructorBinding);
		}
		return getInvocation(callLoc.getStart(), declarationMakingCall);
	}

	private static Invocation getInvocation(int callStart, IMethod declarationMakingCall) {
		MethodInvocationFinder invocationFinder = new MethodInvocationFinder(RJCore.getCorrespondingASTNode(declarationMakingCall)); 
		// TODO: Sometimes this ASTNode corresponding to
		// declarationMakingCall is not a MethodDeclaration. Why?
		Invocation invocation = invocationFinder.findInvocation(callStart);
		return invocation;
	}

	public static MethodBoxModel createInvocationModel(Invocation invocation, InstanceModel instance) {
		MethodBoxModel callerMethodBox = new MethodInvocationModel(instance, invocation.getMethodElement(), invocation.getInvocation(), MethodBoxModel.access);
		callerMethodBox.setCharStart(invocation.getStartPosition());
		callerMethodBox.setCharEnd(invocation.getStartPosition() + invocation.getLength());

		return callerMethodBox;
	}

	/**
	 * Returns the index at which an invocation should be added to its declaration 
	 * container. Indices correspond to the order in which the calls are actually 
	 * made. Note that if the invocation is the parameter of another invocation,  
	 * for example foo(bar()), bar() is actually executed before foo(), so the
	 * index returned for bar() will be before foo()'s index  
	 */
	public static int getInvocationIndex(int invocationStart, int invocationEnd, MethodBoxModel declarationContainer) {
		int invocationIndex = 0;
		Boolean addVisibleHiddenIndex=false;
		for (ArtifactFragment child : declarationContainer.getChildren()) {
			if (child instanceof MethodBoxModel){
				if (((MemberModel) child).getCharStart() < invocationStart && invocationStart < ((MemberModel) child).getCharEnd()) {
					// child contains invocation (i.e. invocation is a parameter of child)
					return invocationIndex;
				}
				if ((invocationStart < ((MemberModel) child).getCharStart() && ((MemberModel) child).getCharEnd() < invocationEnd) // invocation contains child (i.e. child is a parameter of invocation)
						|| invocationStart > ((MemberModel) child).getCharStart() 
						//case for a().b();
						|| (invocationStart == ((MemberModel) child).getCharStart() && invocationEnd > ((MemberModel) child).getCharEnd())) {
					invocationIndex = declarationContainer.getChildren().indexOf(child) + 1;
				}	
			}	

			if (child instanceof FieldModel) { // if a field call is a parameter
				int fieldIdx = 1;
				// if self field call also add a count for the partner which is the next index
				if (((FieldModel) child).getParent().equals(
						((FieldModel) child).getPartner().getParent()))
					fieldIdx++;

				if (invocationStart > ((MemberModel) child).getCharStart()
						&& invocationEnd > ((MemberModel) child).getCharStart())
					invocationIndex++;

				if (invocationStart < ((MemberModel) child).getCharStart()
						&& ((MemberModel) child).getCharStart() < invocationEnd)
					return declarationContainer.getChildren().indexOf(child) + fieldIdx;
			}

			if (child instanceof HiddenNodeModel){
				HiddenNodeModel hiddenModel=(HiddenNodeModel) child;
				if (hiddenModel.getControlFlowMethodsHiding().size()>0) {
					for (MemberModel model: hiddenModel.getControlFlowMethodsHiding()) {
						if (invocationStart>model.getCharStart() && invocationEnd>model.getCharEnd()) {
							invocationIndex++;
							//increment index for the collapsed hidden model only if the model in inside or later of this conditional block
							if (addVisibleHiddenIndex) {addVisibleHiddenIndex=false; invocationIndex++;} 
						}  else 
							return invocationIndex;
					}
				} else addVisibleHiddenIndex=true; //set increment for collapsed hidden model
			}
		}
		return invocationIndex;
	}

	public static String getReturnMessage(Invocation invocation) {
		String returnMessage = "";
		if(invocation.getInvocation() instanceof ClassInstanceCreation || 
				invocation.getInvocation() instanceof ConstructorInvocation ||
				invocation.getInvocation() instanceof SuperConstructorInvocation) {
			// The return message for a call to a constructor is
			// the type of the object being created instead of void
			if(invocation.getInvocation() instanceof ClassInstanceCreation 
					&& ((ClassInstanceCreation)invocation.getInvocation()).getAnonymousClassDeclaration()!=null) {
				AnonymousClassDeclaration acd = ((ClassInstanceCreation)invocation.getInvocation()).getAnonymousClassDeclaration();
				try {
					returnMessage = ((IType)acd.resolveBinding().getJavaElement()).getSuperclassName();
				} catch (JavaModelException e) {
					returnMessage = acd.resolveBinding().getSuperclass().getName();
					logger.error("Unexpected Exception while getting superclass name of "+acd, e);
				}
			} else returnMessage = invocation.resolveMethodBinding().getDeclaringClass().getName();
		} else {
			IMethod method = invocation.getMethodElement();
			if (method != null) {
				try {
					// Get the unresolved return type signature ie "V", "I", 
					// "QFoo", "QList<QRecord;>;" etc.
					String typeSig = Signature.getReturnType(method.getSignature());
					// Get the resolved form of the type sig ie "void", "int", 
					// "Foo", List<Record> etc. Note that Signature.getSignatureSimpleName()
					// can handle parameterized return types like List<Record>
					returnMessage = Signature.getSignatureSimpleName(typeSig);
				} catch (JavaModelException e) {
					logger.error("Exception while determining return " +
							"type for " + MemberUtil.getFullName(method), e);
				}
			}
		}
		return returnMessage;
	}

	/**
	 * 
	 * @param invocation the method call that is made
	 * @param instance the instance of the class on which the invocation is made
	 * @return the class of which the instance the method is invoked on is a type
	 */
	public static Resource getClassOfInstanceCalledOn(Invocation invocation, InstanceModel instance) {
		if(invocation.getMethodElement() instanceof AnonymousClassConstructor) {
			// For the following case where the anonymous class extends/implements
			// a nested inner class within a different class, invocation.getExpression() 
			// will not be null (it will be "outer", which has a type binding of 
			// OuterClass). However, the invocation represents a class instance creation 
			// of an anonymous instance of InnerClass, so InnerClass is the class we want 
			// to return
			/*public class C {
				public void method() {
					OuterClass outer = new OuterClass();
					OuterClass.InnerClass anonInner = outer.new InnerClass() {};
				}
			}*/
			IType anonClassDecl = ((AnonymousClassConstructor)invocation.getMethodElement()).getAnonymousClassDeclaration();
			return RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), anonClassDecl);
		}
		if(invocation.getExpression()!=null) {
			ITypeBinding classBinding = invocation.getExpression().resolveTypeBinding();
			return RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), classBinding.getJavaElement());
		}
		return getContainerClass(invocation, instance);
	}

	/**
	 * 
	 * @param invocation
	 * @param instance the instance of the class in which the invocation is made
	 * @return the Resource corresponding to the class containing the declaration of 
	 * the given method invocation or null if no containing class can be found
	 */
	public static Resource getContainerClass(Invocation invocation, InstanceModel instance) {

		// Below code makes a call like super.foo() into a self call. It is likely more
		// intuitive to have the target of the invocation be the actual super class, 
		// especially if there are multiple super calls to methods in different super 
		// classes, so commenting this code out. (See ticket #472)
//		if(invocation.getInvocation() instanceof SuperMethodInvocation) {
//			return instance.getResource();
//		}

		Resource containerClass = null;
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();

		if(invocation.getMethodElement() instanceof AnonymousClassConstructor) {
			IType anonClassDecl = ((AnonymousClassConstructor)invocation.getMethodElement()).getAnonymousClassDeclaration();
			return RJCore.jdtElementToResource(repo, anonClassDecl);
		}

		if(invocation.getMethodElement()!=null) {
			IMethod method = invocation.getMethodElement();
			containerClass = RJCore.jdtElementToResource(repo, method.getParent());
			if(containerClass!=null) return containerClass;
		}

		Resource methodRes = RJCore.bindingToResource(repo, invocation.resolveMethodBinding());
		return getContainerClass(methodRes, repo);
	}

	public static boolean isOverridenBy(MethodBoxModel overriden, MethodBoxModel overrides) {
		return isOverridenBy(overriden.getMethodRes(), overrides.getMethodRes());
	}

	public static boolean isOverridenBy(Resource overriden, Resource overrides) {
		StatementIterator overridenByIter = StoreUtil.getDefaultStoreRepository().getStatements(overrides, RJCore.overrides, overriden);
		return overridenByIter.hasNext();
	}

	/**
	 * 
	 * @param methodRes
	 * @return the Resource corresponding to the class containing the declaration of 
	 * the given method or null if no containing class can be found
	 */
	public static Resource getContainerClass(Resource methodRes, ReloRdfRepository repo) {
		StatementIterator containsIter = repo.getStatements(null, RSECore.contains, methodRes);
		while(containsIter.hasNext()) {
			Resource container = containsIter.next().getSubject();
			Artifact containerArt = new Artifact(container);
			Resource containerType = containerArt.queryType(repo);
			if(RJCore.classType.equals(containerType) 
					|| RJCore.interfaceType.equals(containerType)) 
				return container;
		}
		return null;
	}

	/**
	 * Returns all subclasses of a given class
	 *
	 */
	public static List<Resource> getContainerClassSubclasses(Resource superClass) {
		StatementIterator subIter = StoreUtil.getDefaultStoreRepository().getStatements(null, RJCore.inherits, superClass);
		if(subIter==null) return new ArrayList<Resource>();

		List<Resource> subclasses = new ArrayList<Resource>();
		while(subIter.hasNext()) {
			Resource subclass = subIter.next().getSubject();
			if(subclass!=null) subclasses.add(subclass);
		}
		return subclasses;
	}

	/**
	 * 
	 * @param invocation
	 * @return the instance on which the method is being invoked
	 */
	public static String getInstanceCalledOn(Invocation invocation) {

		if(invocation.getInvocation() instanceof SuperMethodInvocation) return "super";
		if((invocation.getInvocation() instanceof ClassInstanceCreation) && 
				(invocation.getInvocation().getParent() instanceof VariableDeclarationFragment)) {
			VariableDeclarationFragment variableDecl = (VariableDeclarationFragment) invocation.getInvocation().getParent();
			return variableDecl.getName().getIdentifier();
		}

		Expression expression = invocation.getExpression();
		if(expression==null || (
				!(expression instanceof Name) && 
				!(expression instanceof FieldAccess) &&
				!(expression instanceof SuperFieldAccess))) return null;

		// Test whether the method is static
		if(invocation.resolveMethodBinding()!=null) {
			IMethodBinding declaration = invocation.resolveMethodBinding().getMethodDeclaration();
			if((declaration.getModifiers() & ClassFileConstants.AccStatic) != 0) return null;
		}

		if(expression instanceof SimpleName)       return ((SimpleName)expression).getIdentifier();
		if(expression instanceof QualifiedName)    return ((QualifiedName)expression).getName().getIdentifier();
		if(expression instanceof FieldAccess)      return ((FieldAccess)expression).getName().getIdentifier();
		if(expression instanceof SuperFieldAccess) return ((SuperFieldAccess)expression).getName().getIdentifier();

		return null;
	}

	/**
	 * 
	 * @param invocation
	 * @return the instance on which the first method in a chain of method 
	 * calls is being invoked (for example foo in the chain foo.m1().m2())
	 */
	public static String getInstanceInvocationChainStartsOn(Invocation invocation) {
		Expression expression = invocation.getExpression();
		if(expression instanceof SuperMethodInvocation) return "super";
		if(expression instanceof MethodInvocation) return getInstanceInvocationChainStartsOn(new Invocation((MethodInvocation)expression));
		return getInstanceCalledOn(invocation);
	}

	/**
	 * 
	 * @return true if the given super class method is overridden by one of the 
	 * methods in the given list of a subclass's declared methods
	 */
	public static boolean isOverriden(MethodDeclaration superDecl, List<IMethodBinding> subclassDecls) {
		IMethodBinding superDeclBinding = superDecl.resolveBinding();
		for(IMethodBinding subclassDecl : subclassDecls) {
			if(subclassDecl.isSubsignature(superDeclBinding)) return true;
			
			// isSubsignature() works for regular overriders and for
			// methods implementing a method in an interface, but it will
			// always return false for methods implementing an abstract
			// superclass method, so need the following additional test.
			ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
			Resource subclassDeclRes = RJCore.bindingToResource(repo, subclassDecl);
			Resource superclassDeclRes = RJCore.bindingToResource(repo, superDeclBinding);
			if(isOverridenBy(superclassDeclRes, subclassDeclRes)) return true;

			//TODO: why does the following line not work?
			//if(subclassDecl.resolveBinding().overrides(superDeclBinding)) return true;
		}
		return false;
	}

	public static boolean isOverridenByAnonymousClass(MethodDeclaration declInSuper, IJavaElement anonClassElt) {
		if(!(anonClassElt instanceof IType)) return false;
		try {
			if(!((IType)anonClassElt).isAnonymous()) return false;
			for(IMethod subclassMethod : ((IType)anonClassElt).getMethods()) {
				ASTNode node = RJCore.getCorrespondingASTNode(subclassMethod);
				if(node instanceof MethodDeclaration &&
						equals(((MethodDeclaration)node).resolveBinding().getParameterTypes(), 
								declInSuper.resolveBinding().getParameterTypes())) {
					return true;
				}
			}
		} catch(JavaModelException e) {
			e.printStackTrace();
		}
		return false;
	}

	// Taken from Arrays.equals(Object[], Object[]). Only difference is
	// that we are using IBinding.isEqualTo(IBinding) to compare the two
	// bindings instead of Object.equals(Object) since Object.equals(Object)
	// will not necessarily return the correct value for ITypeBindings
	private static boolean equals(ITypeBinding[] a, ITypeBinding[] a2) {
		if (a==a2)
			return true;
		if (a==null || a2==null)
			return false;

		int length = a.length;
		if (a2.length != length)
			return false;

		for (int i=0; i<length; i++) {
			ITypeBinding o1 = a[i];
			ITypeBinding o2 = a2[i];
			if (!(o1==null ? o2==null : o1.isEqualTo(o2)))
				return false;
		}

		return true;
	}

	public static boolean isOverrider(MethodBoxModel method) {
		StatementIterator overridesIter = StoreUtil.getDefaultStoreRepository().getStatements(method.getMethodRes(), RJCore.overrides, null);
		return overridesIter.hasNext();
	}

	public static boolean isOverriden(MethodBoxModel method) {
		StatementIterator overridenByIter = StoreUtil.getDefaultStoreRepository().getStatements(null, RJCore.overrides, method.getMethodRes());
		return overridenByIter.hasNext();
	}

	public static int getNumOverriders(MethodBoxModel method) {
		StatementIterator overridenByIter = StoreUtil.getDefaultStoreRepository().getStatements(null, RJCore.overrides, method.getMethodRes());
		int num = 0;
		while(overridenByIter.hasNext()) {
			num = num + 1;
			overridenByIter.next();
		}
		return num;
	}

	public static boolean isInvoked(MethodBoxModel method) {
		StatementIterator calledByIter = StoreUtil.getDefaultStoreRepository().getStatements(null, RJCore.calls, method.getMethodRes());
		return calledByIter.hasNext();
	}

	public static boolean makesACall(MethodBoxModel method) {
		StatementIterator callsIter = StoreUtil.getDefaultStoreRepository().getStatements(method.getMethodRes(), RJCore.calls, null);
		return callsIter.hasNext();
	}

	/**
	 * Returns a MethodBoxModel representing the given declaration or null if
	 * no such model representing the declaration already exists. 
	 * 
	 * If the given InstanceModel already contains a MethodBoxModel corresponding 
	 * to the declaration, that MethodBoxModel is returned; or if a command exists 
	 * that will add a MethodBoxModel corresponding to the declaration to the 
	 * given InstanceModel, that MethodBoxModel is returned.
	 * 
	 * @param declaration the declaration for which a MethodBoxModel will be returned
	 * @param instance the instance of the class in which the declaration is present
	 * @return a MethodBoxModel representing the given declaration or null if no 
	 * such model representing the declaration already exists
	 */
	public static MethodBoxModel findDeclarationHolder(IMethod declaration, InstanceModel instance, CompoundCommand cmd) {
		MethodBoxModel declarationHolder = null;
		if(declaration!=null) {
			for(ArtifactFragment child : instance.getChildren()) {
				if(!(child instanceof MethodBoxModel) || !((MethodBoxModel)child).isDeclaration()) continue;
				MethodBoxModel methodChild = (MethodBoxModel) child;
				if(methodChild.getMethod()!=null && methodChild.getMethod().equals(declaration)) {
					declarationHolder = methodChild;
					break;
				}
			}
		}
		if(declarationHolder==null) {
			for(Object o : CommandUtil.getAllContainedCommands(cmd)) {
				if(!(o instanceof MemberCreateCommand)) continue;
				MethodBoxModel methodChild = (MethodBoxModel) ((MemberCreateCommand)o).getChild();
				if(!methodChild.isDeclaration()) continue;
				if(methodChild.getMethod()!=null && methodChild.getMethod().equals(declaration)) {
					declarationHolder = methodChild;
					break;
				}
			}
		}
		return declarationHolder;
	}

	/**
	 * 
	 * @return true if the given method model is the source or target of any
	 * messages or if it contains any children
	 */
	public static boolean methodHasConnectionsOrChildren(MethodBoxModel methodModel) {
		if(methodModel.getChildren().size()!=0) return true;
		if(methodModel.getIncomingConnection()!=null) return true;
		if(methodModel.getOutgoingConnection()!=null) return true;
		return false;
	}

	/**
	 * 
	 * @param source the invocation that results in other methods being called (the 
	 * right most of which will be returned)
	 * @param diagram
	 * @return the method model that represents the declaration of a method that was 
	 * called as a result of source being called and whose instance model is the 
	 * farthest to the right in the diagram
	 */
	public static MemberModel getRightMostResultingCall(MemberModel source, DiagramModel diagram) {
		if(source.getType()!=MemberModel.access || source.getOutgoingConnection()==null) return source;

		List<MemberModel> declarationsOfCalls = new ArrayList<MemberModel>();

		MemberModel start = (MemberModel) source.getOutgoingConnection().getTarget();
		declarationsOfCalls.add(start);
		getContainedCalls(start, declarationsOfCalls);

		MemberModel rightMost = start;
		int rightMostIndex = -1;
		for(MemberModel declaration : declarationsOfCalls) {
			if(declaration.getType()!=MethodBoxModel.declaration) continue;
			int index = diagram.getChildren().indexOf(declaration.getInstanceModel());
			if(index > rightMostIndex) {
				rightMostIndex = index;
				rightMost = declaration;
			}
		}
		return rightMost;
	}

	private static void getContainedCalls(MemberModel declaration, List<MemberModel> calls) {
		for(MethodBoxModel invocation : declaration.getMethodChildren()) {
			if(invocation.getOutgoingConnection()==null) continue;
			MethodBoxModel target = (MethodBoxModel) invocation.getOutgoingConnection().getTarget();
			calls.add(target);
			getContainedCalls(target, calls);
		}
	}

	public static NodeModel getContainerModel(Invocation invocation, MethodBoxModel model, DiagramModel diagram, Resource containerClass, CompoundCommand command) {

		if(containerClass.equals(model.getInstanceModel().getResource())) return model;

		String className = InstanceUtil.getClassName(containerClass, StoreUtil.getDefaultStoreRepository());
		String instanceName = (invocation.getInvocation() instanceof SuperMethodInvocation) ? model.getInstanceModel().getInstanceName() : getInstanceCalledOn(invocation);

		IJavaElement instanceElm = null;
		if (invocation.getInvocation() instanceof ClassInstanceCreation){
			instanceElm = ((ClassInstanceCreation)invocation.getInvocation()).resolveTypeBinding().getJavaElement();
		}

		NodeModel containerModel = InstanceUtil.findOrCreateContainerInstanceModel(instanceName, className, containerClass, diagram, -1, command, instanceElm);

		if(diagram.getChildren().indexOf(containerModel) >= diagram.getChildren().indexOf(model.getInstanceModel())) return containerModel;

		// Container of target is before the container of the source. Reorder to avoid
		// the backward message if container doesn't have any children. (If it had 
		// children, reordering could create a backward message).
		if(containerModel.getChildren().size()==0) {
			diagram.reorderChild(containerModel, diagram.getChildren().indexOf(model.getInstanceModel()));
			return containerModel;
		} 
		//TODO
		// This is what we want; uncomment when updating bounds is fixed because
		// currently this makes it enter an infinite loop
		/*	boolean canReorder = true;
			for(NodeModel instanceChild : im.getChildren()) {
				if(!(instanceChild instanceof MethodBoxModel)) continue;

				for(NodeModel methodChild : instanceChild.getChildren()) {
					if(!(methodChild instanceof MethodBoxModel) || ((MethodBoxModel)methodChild).getType()!=MethodBoxModel.invocation) continue;
					MethodBoxModel invocationModel = (MethodBoxModel) methodChild;

					if(invocationModel.getOutgoingConnection()==null) continue;
					MethodBoxModel target = (MethodBoxModel) invocationModel.getOutgoingConnection().getTarget();
					if(target==null) continue;
					int indexOfTarget = diagram.getChildren().indexOf(target.getInstanceModel());
					if(indexOfTarget > diagram.getChildren().indexOf(model.getInstanceModel())) continue;
					// TODO can reorder in some cases here. implement handling to determine which ones.
				}
			}
			if(canReorder) {
				ReorderNodeCommand reorderCmd = new ReorderNodeCommand(im, diagram, diagram.getChildren().indexOf(model.getInstanceModel()));
				reorderCmd.execute();
				return containerModel;
			}
		 */
		return containerModel;
	}

	public static MethodWrapper getCalleeRoot(IMethod method) {
		try {
			return getCalleeOrCallerRoot(method, true);
		} catch (Throwable e) {
			logger.error("Could not get callee root", e);
			return null;
		}
	}

	public static MethodWrapper getCallerRoot(IMethod method) {
		try {
			return getCalleeOrCallerRoot(method, false);
		} catch (Throwable e) {
			logger.error("Could not get caller root", e);
			return null;
		}
	}

	// callee should be true if want to get callee root, false if want caller root
	private static MethodWrapper getCalleeOrCallerRoot(IMethod method, boolean callee) throws Throwable {
		double jdtUIVer = PluginUtils.getPluginVer("org.eclipse.jdt.ui");
		CallHierarchy callHierarchy = CallHierarchy.getDefault();

		Method mth;
		if(jdtUIVer < 3.4)
			mth = callHierarchy.getClass().getMethod(callee?"getCalleeRoot":"getCallerRoot", IMethod.class);
		else
			mth = callHierarchy.getClass().getMethod(callee?"getCalleeRoots":"getCallerRoots", IMember[].class);

		Object param;
		if(jdtUIVer < 3.4) param = method;
		else param = new IMember[]{method};

		Object result = mth.invoke(callHierarchy, param);
		if(jdtUIVer < 3.4) return (MethodWrapper) result;
		else return ((MethodWrapper[])result)[0];
	}

	public static void Collapse(MemberModel model, CompoundCommand command){
		for(ArtifactFragment child: model.getChildren()){
			ArtifactFragment partner = null;
			if(child instanceof MethodBoxModel){
				partner = ((MethodBoxModel) child).getPartner();
				if(partner != null){
					if(((MemberModel) partner).getInstanceModel().equals(((MethodBoxModel) child).getInstanceModel())){
						// remove nested self calls
						Collapse((MemberModel) partner, command);
					}
					else{// Remove connection and invocation 
						command.add(new ConnectionDeleteCommand(((MemberModel) child).getOutgoingConnection()));
						command.add(new ConnectionDeleteCommand(((MemberModel)child).getIncomingConnection()));
						command.add(new MemberDeleteCommand(((MemberModel) child).getParent(),(MemberModel) child, true));
					}
				}
			}
		}
		MemberModel invocPartner = model.getPartner();
		//Connections
		if(invocPartner != null){
			command.add(new ConnectionDeleteCommand(model.getIncomingConnection()));
			command.add(new ConnectionDeleteCommand(model.getOutgoingConnection()));
		}
		//Declaration
		command.add(new MemberDeleteCommand(model.getParent(),model, true));
		//Invocation
		if(invocPartner != null){
			command.add(new MemberDeleteCommand(invocPartner.getParent(),invocPartner, true));
		}
	}

	public static void getBasicCollapseCommand(MemberModel model,
			CompoundCommand command) {
		for (ArtifactFragment child : model.getChildren()) {
			ArtifactFragment partner = null;
			if (child instanceof MethodBoxModel) {
				partner = ((MemberModel) child).getPartner();
				if (partner != null)
					Collapse((MemberModel) partner, command);
			} else if (child instanceof FieldModel
					&& ((FieldModel) child).getType() == MemberModel.access) {
				partner = ((FieldModel) child).getPartner();
				if (partner != null) {
					command.add(new ConnectionDeleteCommand(((FieldModel) child).getOutgoingConnection()));
					command.add(new MemberDeleteCommand(((FieldModel) partner).getParent(), (FieldModel) partner, true));
					command.add(new MemberDeleteCommand(((FieldModel) child).getParent(), (FieldModel) child, true));
				}
			}
		}
	}

	// For collapse the calling method will remain in the diagram.
	public static void getExtendedCollapseCommand(MemberModel model, CompoundCommand command){
		deletePartnerDeclarationAndChildren(model, command, false);
	}

	// deletePartnerDeclarationAndChildren is called with the declaration 
	public static void getExtendedDeleteCommand(MemberModel model, CompoundCommand command){
		if(model instanceof MethodInvocationModel){
			MemberModel partner = model.getPartner();
			if(partner != null) deletePartnerDeclarationAndChildren(partner, command, true);
		}else	deletePartnerDeclarationAndChildren(model, command, true);
	}

	private static void deletePartnerDeclarationAndChildren(MemberModel declaration,
			CompoundCommand command, boolean isDelete) {
		for(ArtifactFragment child: declaration.getChildren()){
			ArtifactFragment partner = null;
			if (child instanceof MethodBoxModel) {
				partner = ((MethodBoxModel) child).getPartner();
				if (partner != null)
					deletePartnerDeclarationAndChildren((MemberModel) partner, command, true);
			} else if (child instanceof FieldModel
					&& ((FieldModel) child).getType() == MemberModel.access) {
				partner = ((FieldModel) child).getPartner();
				command.add(new ConnectionDeleteCommand(((FieldModel) child).getOutgoingConnection()));
				command.add(new MemberDeleteCommand((NodeModel) partner.getParentArt(),(FieldModel)partner, true));
				command.add(new MemberDeleteCommand(((FieldModel) child).getParent(),(FieldModel) child, true));
			}
		}

		if(isDelete){ // Do not delete if called from extended collapse
			MemberModel invocPartner = declaration.getPartner();
			//Connections
			if(invocPartner != null){
				command.add(new ConnectionDeleteCommand(declaration.getIncomingConnection()));
				command.add(new ConnectionDeleteCommand(declaration.getOutgoingConnection()));
			}
			//Declaration
			command.add(new MemberDeleteCommand(declaration.getParent(),declaration, true));
			//Invocation
			if(invocPartner != null){
				command.add(new MemberDeleteCommand(invocPartner.getParent(),invocPartner, true));
			}
		}
	}

	public static boolean isConstructor(MethodBoxModel methodBox) {
		ASTNode astNode = methodBox.getASTNode();
		return (astNode instanceof MethodDeclaration) ? ((MethodDeclaration)astNode).isConstructor() : false;
	}
}
