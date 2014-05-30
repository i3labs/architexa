package com.architexa.diagrams.chrono.util;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.INavigationHistory;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.TextSelectionNavigationLocation;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.MethodInvocationModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.models.UserCreatedInstanceModel;
import com.architexa.diagrams.chrono.models.UserCreatedMethodBoxModel;
import com.architexa.diagrams.chrono.models.UserCreatedMethodInvocationModel;
import com.architexa.diagrams.chrono.sequence.FieldRead;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.chrono.services.PluggableInterfaceProvider;
import com.architexa.diagrams.jdt.AnonymousClassConstructor;
import com.architexa.diagrams.jdt.ImplicitConstructorCallLocation;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.compat.ASTUtil;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.utils.MethodInvocationFinder;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.intro.AtxaIntroPlugin;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqUtil {
	public static PluggableInterfaceProvider pip = new PluggableInterfaceProvider();
	public static boolean debugHighlightingOn = false;
	private static final Logger logger = SeqPlugin.getLogger(SeqUtil.class);
	/**
	 * Creates a model for the given Resource based on 
	 * the given repository, which contains saved data
	 * @param instanceRes a saved diagram component's instanceRes
	 * @param parentModel the parent that will contain the 
	 * created model. NOTE: This method does not add the 
	 * created model to the parent
	 * @param repo the repository that contains the saved 
	 * data (component type, name, partner, and model 
	 * resource) that will be used to create the new model
	 * @return a new model for the given instanceRes 
	 */
	public static ArtifactFragment createChronoModelForResource(Resource instanceRes, ArtifactFragment parentModel, ReloRdfRepository repo, DiagramEditPart parentPart) {
		if(instanceRes==null) return null;

		Resource modelRes = (Resource)repo.getStatement(instanceRes, RSECore.model, null).getObject();
		Resource detailsRes = modelRes;

		Artifact tempDetailsRes = new Artifact(detailsRes);
		Resource type = tempDetailsRes.queryType(repo);
		if (type == null) {
			logger.error("Null type encountered for resource : "+ modelRes);
			return null;
		}
		if (RJCore.classType.equals(type) || RJCore.interfaceType.equals(type)) {

			Statement instanceNameStmt = repo.getStatement(instanceRes, RSECore.instanceName, null);
			String instanceName = (instanceNameStmt!=null && instanceNameStmt.getObject()!=null) ? instanceNameStmt.getObject().toString() : "";
			String className = InstanceUtil.getClassName(modelRes, repo);

			InstanceModel instanceModel = new InstanceModel(instanceName, className, modelRes);
			if(instanceModel!=null)
				instanceModel.setInstanceRes(instanceRes);
			else
				logger.error("Could not create Instance model for resource: " + detailsRes.toString());
			return instanceModel;
		}
		if(RJCore.methodType.equals(type)) {

			InstanceModel instanceModel = null;
			if(parentModel instanceof InstanceModel) instanceModel = (InstanceModel) parentModel;
			else if(parentModel instanceof MethodBoxModel) instanceModel = ((MethodBoxModel)parentModel).getInstanceModel();
			if(instanceModel==null) return null;

			boolean isAnInvocation = false;
			StatementIterator connIter = repo.getStatements(null, repo.rdfSubject, instanceRes);
			while(connIter.hasNext()) {
				Resource conn = connIter.next().getSubject();
				if(conn==null) continue;
				Value connType = repo.getStatement(conn, RSECore.model, null).getObject();
				if(RJCore.calls.equals(connType)) {
					isAnInvocation = true;
					break;
				}
			}
			if (isAnInvocation) {
				Invocation invoc = getASTNodeForInvocation(modelRes, parentModel);
				MethodBoxModel invocModel;
				if (invoc == null) { 
					logger.warn("Could not create Invocation for resource: "+detailsRes.toString());
					invocModel = new MethodInvocationModel(instanceModel, modelRes, MethodBoxModel.access);
				} else
					invocModel = MethodUtil.createInvocationModel(invoc, instanceModel);
				
				if (invocModel != null)
					invocModel.setInstanceRes(instanceRes);
				else
					logger.error("Could not create Invocation Model for resource: "+detailsRes.toString());
				return invocModel;
			}

			MethodBoxModel declModel = new MethodBoxModel(instanceModel, modelRes, MethodBoxModel.declaration);
			if(declModel != null)
				declModel.setInstanceRes(instanceRes);
			else
				logger.error("Could not create Declaration model for resource: "+detailsRes.toString());
			return declModel;
		}
		if(RJCore.fieldType.equals(type)) {
			InstanceModel instanceModel = null;
			if(parentModel instanceof InstanceModel) instanceModel = (InstanceModel) parentModel;
			else if(parentModel instanceof MethodBoxModel) instanceModel = ((MethodBoxModel)parentModel).getInstanceModel();
			if(instanceModel==null) return null;

			boolean isInvocation = false;
			StatementIterator connIter = repo.getStatements(null, repo.rdfSubject, instanceRes);
			while(connIter.hasNext()) {
				Resource conn = connIter.next().getSubject();
				if(conn==null) continue;
				Value connType = repo.getStatement(conn, RSECore.model, null).getObject();
				if(ConnectionModel.FIELD_READ.equals(connType) || ConnectionModel.FIELD_WRITE.equals(connType)) {
					isInvocation = true;
					break;
				}
			}

			if (repo.getStatements(detailsRes, RSECore.link, null).hasNext()) {
				FieldRead read = getASTNodeForFieldRead(modelRes, parentModel);
				if (read != null){
					FieldModel readModel = null;
					if (isInvocation)
						readModel = FieldUtil.createFieldReadModel(read, (IField)read.resolveFieldBinding().getJavaElement(), instanceModel);
					else { // is Declaration
						IField fieldElt = (IField) read.resolveFieldBinding().getJavaElement();
						readModel = new FieldModel((InstanceModel) parentModel.getParentArt(), fieldElt, RJCore.getCorrespondingASTNode(fieldElt), MemberModel.declaration);
					}

					if (readModel != null)
						readModel.setInstanceRes(instanceRes);
					else
						logger.error("Could not create field model for resource: "+detailsRes.toString());
					return readModel;
				}
			}

			IField field = (IField) RJCore.resourceToJDTElement(repo, modelRes);
			
			FieldModel declModel;
			if (isInvocation)
				declModel = new FieldModel(instanceModel, field, null, MemberModel.access);
			else	
				declModel = new FieldModel(instanceModel, field, null, MemberModel.declaration);
			if(declModel != null)
				declModel.setInstanceRes(instanceRes);
			else
				logger.error("Could not create field declaration for resource: "+detailsRes.toString());
			return declModel;
		}
		logger.error("Unrecognizable type for the resource: "+detailsRes.toString());
		return null;
	}

	public static ArtifactFragment createAFForSavedResources(ReloRdfRepository repo, 
			Resource modelRes, Resource instanceRes, ArtifactFragment parent, DiagramModel diagram) {
		Resource nodeType = (Resource) repo.getStatement(modelRes, repo.rdfType, null).getObject();
		if (RJCore.classType.equals(nodeType)) {
			String instanceName = repo.getStatement(modelRes, RSECore.instanceName, null).getObject().toString();
			UserCreatedInstanceModel userInstance = new UserCreatedInstanceModel(instanceName, diagram);
			userInstance.setInstanceRes(instanceRes);
			return userInstance;
			
		} else if (RJCore.methodType.equals(nodeType)) {
			Value nameStmt = repo.getStatement(modelRes, RSECore.instanceName, null).getObject();
			MethodBoxModel userModel;
			if ((parent instanceof InstanceModel)) {
				String methodName = nameStmt != null? nameStmt.toString() : "";
				userModel = new UserCreatedMethodBoxModel(methodName, (InstanceModel) parent);
			} else
				userModel = new UserCreatedMethodInvocationModel(((MethodBoxModel)parent).getInstanceModel(), MemberModel.access); 
			
			userModel.setInstanceRes(instanceRes);
			return userModel;
		}
		
		return null;
	}
	
	private static Invocation getASTNodeForInvocation(Resource res,	ArtifactFragment parentOfInvoc) {
		if (!(parentOfInvoc instanceof MethodBoxModel))
			return null;
		if(((MethodBoxModel)parentOfInvoc).getMethod() instanceof AnonymousClassConstructor) {
			// Anonymous class definition makes a single call: an 
			// implicit call to the superclass constructor
			AnonymousClassConstructor anonConstr = (AnonymousClassConstructor) ((MethodBoxModel)parentOfInvoc).getMethod();
			ImplicitConstructorCallLocation callLoc = anonConstr.getImplicitCallToSuperConstructor();
			Invocation invocation = MethodUtil.getInvocation(callLoc, anonConstr);
			return invocation;
		}
		ASTNode parentOfInvocASTNode = ((MethodBoxModel) parentOfInvoc).getASTNode();
		if (parentOfInvocASTNode == null) return null;
		MethodInvocationFinder invocFinder = null;
		invocFinder = new MethodInvocationFinder(parentOfInvocASTNode, true);
		
		if (invocFinder == null) return null;
		
		for (Invocation invocation : invocFinder.getAllInvocations()) {
			Resource invocRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), invocation.getMethodElement());
			if (res.equals(invocRes))
				return invocation;
		}
		return null;
	}

	private static FieldRead getASTNodeForFieldRead(Resource res, ArtifactFragment parentOfRead) {
		if(!(parentOfRead instanceof MethodBoxModel)) return null;
		ASTNode parentOfReadASTNode = ((MethodBoxModel)parentOfRead).getASTNode();
		if(!(parentOfReadASTNode instanceof MethodDeclaration)) return null;
		FieldReadFinder readFinder = new FieldReadFinder((MethodDeclaration) parentOfReadASTNode);
		for(FieldRead read : readFinder.getAllReads()) {
			Resource readRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), read.resolveFieldBinding().getJavaElement());
			if(res.equals(readRes)) return read;
		}
		return null;
	}

	/**
	 * Converts a model for a class or method into the appropriate Chrono model
	 * @param cu the model of the code element to be converted
	 * @param parent the parent of the code element to be converted
	 * @param artFragToNodeModelMap a mapping from the original ArtFrag representation 
	 * to the Chrono model of the code unit
	 * @return a Chrono model of the given code element
	 */
	public static NodeModel convertToChronoModels(CodeUnit cu, ArtifactFragment parent, DiagramModel diagram, Map<ArtifactFragment, NodeModel> artFragToNodeModelMap) {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		Resource artType = cu.queryType(repo);
		if(RJCore.classType.equals(artType) || RJCore.interfaceType.equals(artType)) {
			Resource classRes = cu.getArt().elementRes;
			InstanceModel classOrInterfaceModel = new InstanceModel(cu.getInstanceName(), InstanceUtil.getClassName(classRes, repo), classRes);
			for(Object child : cu.getShownChildren()) {
				if(!(child instanceof CodeUnit)) continue;

				NodeModel childNode = convertToChronoModels((CodeUnit)child, classOrInterfaceModel, diagram, artFragToNodeModelMap);
				if(childNode==null) continue;
				if(childNode instanceof InstanceModel) {
					// It's a nested class. Relo shows a nested class as a child of 
					// its container class; Chrono shows a nested class as a child of 
					// the diagram (the same way that it shows non-nested classes)
					diagram.appendShownChild(childNode);
				} else classOrInterfaceModel.addChild(childNode);
			}
			artFragToNodeModelMap.put(cu, classOrInterfaceModel);
			return classOrInterfaceModel;
		} else if(RJCore.methodType.equals(artType)) {
			InstanceModel instance;
			if(parent instanceof InstanceModel)	instance = (InstanceModel)parent;
			else if(parent instanceof MethodBoxModel) instance = ((MethodBoxModel)parent).getInstanceModel();
			else return null;

			MethodBoxModel methodModel = cu instanceof MethodBoxModel ? new MethodBoxModel(instance, ((MethodBoxModel)cu).getMethod(), MethodBoxModel.declaration) : new MethodBoxModel(instance, cu.getArt().elementRes, MethodBoxModel.declaration);
			artFragToNodeModelMap.put(cu, methodModel);
			return methodModel;
		} else if(RJCore.fieldType.equals(artType)) {
		}
		return null;
	}

	public static String filterNone = "noFilter";
	public static String filterByClasses = "classesFilter";
	public static String filterByMethods = "methodsFilter";

	/**
	 * Returns the last n entries in the workbench's navigation history
	 * 
	 */
	public static LinkedHashMap<IType, List<Invocation>> getNavigationHistory(int n) {
		LinkedHashMap<IType, List<Invocation>> viewedMap = new LinkedHashMap<IType, List<Invocation>>();

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		INavigationHistory history = page.getNavigationHistory();
		INavigationLocation locationsArray[] = history.getLocations();

		List<INavigationLocation> mostRecentToOldestLocations = new ArrayList<INavigationLocation>();
		for(INavigationLocation loc : locationsArray) mostRecentToOldestLocations.add(loc);
		Collections.reverse(mostRecentToOldestLocations);

		Resource nextNavigatedClassRes = null;
		for(INavigationLocation loc : mostRecentToOldestLocations) {
			if(n<=0 || loc==null ||  !(loc instanceof TextSelectionNavigationLocation) || !(loc.getInput() instanceof FileEditorInput)) continue;

			FileEditorInput input = (FileEditorInput) loc.getInput();
			IFile file = input.getFile();

			TextSelectionNavigationLocation textSelectNavLoc = (TextSelectionNavigationLocation) loc;
			Position position = lookupfPosition(textSelectNavLoc);
			if(position==null) continue;

			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);
			IJavaElement currentNavElt = null;;
			try {
				currentNavElt = icu.getElementAt(position.getOffset());
			} catch (JavaModelException e) {
				e.printStackTrace();
			}

			// Find the type most directly containing the navigated element (i.e. if
			// the navigated element is in a nested class, we want that nested class 
			// rather than the class that corresponds to the source file containing
			// the nested class)
			IJavaElement currentNavEltCopy = currentNavElt;
			while(currentNavEltCopy!=null && !(currentNavEltCopy instanceof IType)) {
				currentNavEltCopy = currentNavEltCopy.getParent();
			}
			IType currentNavType = (IType) currentNavEltCopy;
			if(currentNavType==null) continue;
			Resource currentNavigatedClassRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), currentNavType);

			// If a superclass is the next class navigated after a subclass, 
			// add both the subclass and superclass to the viewedMap now so
			// that an inheritance connection can be shown between them
			if(nextNavigatedClassRes!=null) {
				List<Resource> superClasses = InstanceUtil.getSuperClasses(currentNavigatedClassRes);
				if(superClasses.contains(nextNavigatedClassRes)) {
					// Adding the superclass first since we are iterating through
					// the navigations from most recent to oldest, so the superclass
					// navigation would presumably be more recent (i.e. navigating
					// a super.foo() call from the subclass to the superclass)
					boolean putInMap = false;
					IJavaElement superClassElt = RJCore.resourceToJDTElement(StoreUtil.getDefaultStoreRepository(), nextNavigatedClassRes);
					if(!viewedMap.containsKey(superClassElt)) {
						viewedMap.put((IType)superClassElt, new ArrayList<Invocation>());
						putInMap = true;
					}
					if(!viewedMap.containsKey(currentNavType)) {
						viewedMap.put(currentNavType, new ArrayList<Invocation>());
						putInMap = true;
					}
					if(putInMap) n--;
				}
			}
			nextNavigatedClassRes = currentNavigatedClassRes;

			// If the position of the navigation corresponds to an invocation,
			// add the type containing the invocation and the invocation to 
			// viewedMap so that the method call can be added to the diagram
			CompilationUnit cu = ASTUtil.getAST(file);
			MethodInvocationFinder finder = new MethodInvocationFinder(cu);
			Invocation invocation = finder.findInvocation(position.getOffset());
			if(invocation==null) continue;

			if(viewedMap.containsKey(currentNavType) && !viewedMap.get(currentNavType).contains(invocation)) {
				viewedMap.get(currentNavType).add(invocation);
				n--;
			} else if(!viewedMap.containsKey(currentNavType)) {
				List<Invocation> invocations = new ArrayList<Invocation>();
				invocations.add(invocation);
				viewedMap.put(currentNavType, invocations);
				n--;
			}
		}

		LinkedHashMap<IType, List<Invocation>> viewedMapCorrectOrder = new LinkedHashMap<IType, List<Invocation>>();
		List<IType> typesCorrectOrder = new ArrayList<IType>(viewedMap.keySet());
		Collections.reverse(typesCorrectOrder);
		for(IType type : typesCorrectOrder) {
			List<Invocation> invocationsCorrectOrder = viewedMap.get(type);
			Collections.reverse(invocationsCorrectOrder);
			viewedMapCorrectOrder.put(type, invocationsCorrectOrder);
		}
		return viewedMapCorrectOrder;
	}

	public static ImageDescriptor getImageDescriptorFromKey(String key) {
		return CodeUnit.getImageDescriptorFromKey(key);
	}

	public static IPreferenceStore getPreferenceStore() {
		return AtxaIntroPlugin.getDefault().getPreferenceStore();
	}

	private static Position lookupfPosition(TextSelectionNavigationLocation loc) {
		Field[] fields = TextSelectionNavigationLocation.class.getDeclaredFields();
		for (Field f : fields) {
			if (!f.getName().equals("fPosition")) continue;
			f.setAccessible(true);
			try {
				Object retVal = f.get(loc);
				return (Position) retVal;
			} catch (Exception e) {}

		}
		return null;
	}

}
