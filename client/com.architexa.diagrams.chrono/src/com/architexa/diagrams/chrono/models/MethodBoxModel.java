package com.architexa.diagrams.chrono.models;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.internal.corext.callhierarchy.CallLocation;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.chrono.controlflow.CollapseExpandButton;
import com.architexa.diagrams.chrono.controlflow.ControlFlowBlock;
import com.architexa.diagrams.chrono.controlflow.IfBlock;
import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.sequence.FieldRead;
import com.architexa.diagrams.chrono.util.FieldReadFinder;
import com.architexa.diagrams.chrono.util.InstanceUtil;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.jdt.AnonymousClassConstructor;
import com.architexa.diagrams.jdt.CompilerGeneratedDefaultConstructor;
import com.architexa.diagrams.jdt.ImplicitConstructorCallLocation;
import com.architexa.diagrams.jdt.InitializerWrapper;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.utils.MethodInvocationFinder;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.draw2d.Label;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class MethodBoxModel extends MemberModel {

	private MethodBoxFigure figure;

	ConnectionModel overridesConnection; // method that this overrides
	List<ConnectionModel> overriderConnections = new ArrayList<ConnectionModel>(); // methods that override this

	private IMethod method;
	private Resource methodRes;

	private List<Invocation> invocationsMade;
	private List<CallLocation> invocationsOf;
	private List<IMethod> overridingMethods;

	private List<FieldRead> fieldReads;

	private boolean isAChainedCall = true;

	public MethodBoxModel(InstanceModel instance, IMethod method, int type) {
		super(RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), method));
		this.instance = instance;
		this.method = method;
		this.methodRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), method);
		this.type = type;
	}
	
	// Should be called by only user created models
	public MethodBoxModel() {
		super();
		this.type = MemberModel.declaration;
		setUserCreated(true);
	}
	
	public MethodBoxModel(Artifact art, InstanceModel instance) {
		this();
		if (art != null)
			this.methodRes = art.elementRes;
		this.instance = instance;
	}

	public MethodBoxModel(InstanceModel instance, Resource res, int type) {
		super(res);
		this.instance = instance;
		this.methodRes = res;
		this.type = type;

		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		IJavaElement methodElement = RJCore.resourceToJDTElement(repo, methodRes);
		if(methodElement instanceof IInitializer)
			methodElement = new InitializerWrapper((IInitializer)methodElement);
		if(methodElement instanceof IMethod)
			this.method = (IMethod) methodElement;
	}

	public MethodBoxModel(InstanceModel instance, int type) {
		this.instance = instance;
		this.type = type;
	}

	/**
	 * Get the Innermost ifblock which contains this model
	 * @return Innermost ifblock
	 */
	public IfBlock getInnerMostIfBlock(){
		for(ControlFlowBlock block:this.getConditionalBlocksContainedIn()){
			for(CollapseExpandButton button:block.getCollapseExpandButtons()){
				if(button.getStatements().contains(this))
					return (IfBlock) block;
			}
		}
		return null;
	}

	public void setOverridesConnection(ConnectionModel conn) {
		overridesConnection = conn;
	}

	public ConnectionModel getOverridesConnection() {
		return overridesConnection;
	}

	public void addOrRemoveOverriderConnection(ConnectionModel conn, boolean add) {
		if(add) overriderConnections.add(conn);
		else overriderConnections.remove(conn);
	}

	public List<ConnectionModel> getOverriderConnections() {
		return overriderConnections;
	}

	@Override
	public MethodBoxModel getPartner() {
		MemberModel partner = super.getPartner();
		if(partner==null || !(partner instanceof MethodBoxModel)) return null;
		return (MethodBoxModel) partner;
	}

	public void setFigure(MethodBoxFigure figure) {
		this.figure = figure;
	}

	@Override
	public MethodBoxFigure getFigure() {
		return figure;
	}

	public Rectangle getMethodBoxBounds() {
		return figure.getBounds();
	}

	public String getMethodName() {
		if(method instanceof InitializerWrapper) 
			return CodeUnit.initializerStringRep;
		if(method!=null) return method.getElementName();
		if(methodRes==null) return "";
		return MethodUtil.getMethodName(methodRes, super.getASTNode());
	}
	
	// Overriden by User Created method
	public void setMethodName(String methodName) {
	}

	@Override
	public String getName() {
		if(method instanceof InitializerWrapper) 
			return CodeUnit.initializerStringRep;
		if(method==null) return getMethodName();
		return MemberUtil.getFullName(method);
	}

	public Resource getMethodAccess() {
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		return (Resource) repo.getStatement(methodRes, RJCore.access, null).getObject();
	}

	public Resource getMethodRes() {
		return methodRes;
	}

	public IMethod getMethod() {
		return method;
	}

	@Override
	public IMethod getMember() {
		return method;
	}

	public void changeMethodRepresented(IMethod method, ASTNode node) {
		this.method = method;
		this.methodRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), method);

		if(type==access) {
			int newStart = node==null ? -1 : node.getStartPosition();
			int newEnd = node==null ? -1 : node.getStartPosition() + node.getLength();
			setCharStart(newStart);
			setCharEnd(newEnd);
		}

		if(figure!=null && (figure.getContainer().getToolTip() instanceof Label)) {
			String newFullName = Invocation.nodeIsATypeOfInvocation(node) ? Invocation.getStringRepresentationOfInvocation(node) : getName();
			((Label)figure.getContainer().getToolTip()).setText(newFullName);
		}
	}

	public boolean isInvocationListInitialized() {
		return invocationsMade!=null;
	}

	/**
	 * Returns all of the method calls made inside this method declaration. If this 
	 * model does not correspond to a method declaration (i.e., it is a method 
	 * invocation) or if the necessary information about its AST is not available, 
	 * an empty list will be returned
	 * @param scheduleOnCompletion If non-null, this job will be scheduled once
	 * the calls are determined and progress monitor closed
	 * @return a list of all the method invocations made inside this method declaration
	 */
	public List<Invocation> getCallsMade(Job scheduleOnCompletion) {
		if(invocationsMade==null) invocationsMade = findInvocationsMade();
		if(scheduleOnCompletion!=null) {
			// give the job high priority so it starts as quickly
			// as possible once the invocations are determined
			scheduleOnCompletion.setUser(true);
			scheduleOnCompletion.setPriority(Job.INTERACTIVE);
			scheduleOnCompletion.schedule();
		}
		return invocationsMade;

	}

	/**
	 * @return a list of all the locations of invocations of this method 
	 */
	public List<CallLocation> getCallsOf() {
		if(invocationsOf==null) invocationsOf = findInvocationsOf();
		return invocationsOf;
	}

	/**
	 * 
	 * @return a list of all the locations of invocations of this method
	 * that are not already shown in the diagram 
	 */
	public List<CallLocation> getCallsOfNotInDiagram() {
		List<CallLocation> allCalls = new ArrayList<CallLocation>(getCallsOf());
		List<CallLocation> allCallsCopy = new ArrayList<CallLocation>(allCalls);

		// Remove any call already made to this, and remove any calls already 
		// made to siblings (or children of siblings in the case of calls to
		// the same class) of this that represent the same method
		removeShownCallsToMethod(allCalls, allCallsCopy, getInstanceModel().getMethodChildren());
		return allCalls;
	}

	private void removeShownCallsToMethod(List<CallLocation> allCalls, List<CallLocation> allCallsCopy, List<MethodBoxModel> methodChildren) {
		for(MethodBoxModel siblingOrChildOfSibling : methodChildren) {

			// method representing the same method as this could be a child of a sibling
			// (or a child of that child etc) if it is invoked as a call to the same class
			removeShownCallsToMethod(allCalls, allCallsCopy, siblingOrChildOfSibling.getMethodChildren());

			// if method represents the same method as this does, remove the invocation 
			// represented by its partner from the list of all calls made to this
			if(!siblingOrChildOfSibling.getMethod().equals(getMethod())) continue;
			if(siblingOrChildOfSibling.getPartner()==null) continue;
			for(CallLocation call : new ArrayList<CallLocation>(allCallsCopy)) {
				if(siblingOrChildOfSibling.getPartner().getCharStart()==call.getStart() &&
						siblingOrChildOfSibling.getPartner().getCharEnd()==call.getEnd()) {
					allCalls.remove(call);
					break;
				}
			}
		}
	}

	/**
	 * 
	 * @return a list of all the method declarations that override this method. 
	 * If this does not correspond to a method declaration (i.e., it 
	 * is a method invocation) or if the necessary information about its  
	 * AST is not available, an empty list will be returned
	 */
	public List<IMethod> getMethodsThatOverride() {
		if(overridingMethods==null) overridingMethods = findMethodsThatOverride();
		return overridingMethods;
	}

	/**
	 * Returns all of the field reads made inside this method declaration. If this
	 * model does not correspond to a method declaration (i.e., it is a method
	 * invocation) or if the necessary information about its AST is not available,
	 * an empty list will be returned
	 * @return a list of all the field reads made inside this method declaration
	 */
	public List<FieldRead> getFieldReadsMade() {
		if(fieldReads==null) fieldReads = findFieldReadsMade();
		return fieldReads;
	}

	public void setIsAChainedCall(boolean chained) {
		isAChainedCall = chained;
	}

	public boolean isAChainedCall() {
		return isAChainedCall;
	}

	/**
	 * 
	 * @return the method model representing the method declaration that 
	 * contains this invocation method model or null if this model does
	 * not represent an invocation
	 */
	public MethodBoxModel getDeclarationContainer() {
		if(type!=access || !(getParent() instanceof MethodBoxModel)) return null;

		return (MethodBoxModel) getParent();
	}

	@Override
	public String toString() {
		String methodName = getName()==null ? "nullMethodName" : getName();
		return methodName + " contained in " + getInstanceModel();
	}

	/**
	 * Removes only this method and any other models that cannot exist without it.
	 * 
	 * If this model represents an invocation, only it and the connections to its
	 * partner are deleted. 
	 * If this model represents an invocation whose partner declaration is a method
	 * in the same class (and therefore is shown as a child of this invocation), 
	 * that partner is not deleted and is made a child of the instance directly.
	 * If this model represents a declaration, it and the connections to its partner
	 * invocation are deleted. The partner is also deleted since an invocation cannot 
	 * exist without a declaration. For the same reason any invocations made 
	 * inside this declaration are also deleted, but their targets are not.
	 */
	@Override
	public void deleteBasic() {
		deleteAndHangingConnectionsAndInvocations();
		super.deleteBasic();
		this.removeFromConditionalBlocks();
	}

	/* Collapse the direct invocations
	 */
	public void basicCollapse(){
		List<MemberModel> memberChild=getMemberChildren();
		List<MemberModel> partners=new ArrayList<MemberModel>();
		for(MemberModel model: memberChild){
			partners.add(model.getPartner());
			model.deleteBasic();
		}

		for(MemberModel part: partners){
			if(part.getParent()!=null && part.getChildren().size()==0)
			{
				(part.getParent()).removeChild(part);
			}	
		}
	}

	/* Removes all the links originating from this method
	 */
	public void extendedCollapse(){
		List<MemberModel> memberChild=getMemberChildren();
		for(MemberModel model: memberChild){
			if(model instanceof MethodBoxModel)
				((MethodBoxModel)model).removeContaineesAndPartner(new ArrayList<MethodBoxModel>(), new ArrayList<MethodBoxModel>());
			else
				model.deleteBasic();
		}

	}
	/**
	 * Removes this method and all methods that are somehow connected through any
	 * chain of method calls to it, one of its children, one of its partner's children,
	 * one of its children's partners, one of its children's children, and so on.
	 * 
	 */
	public void deleteExtended() {
		removeContaineesAndPartner(new ArrayList<MethodBoxModel>(), new ArrayList<MethodBoxModel>());
		if(getParent()!=null) {
			getParent().removeChild(this);
			this.removeFromConditionalBlocks();
		}
	}

	private void deleteAndHangingConnectionsAndInvocations() {
		if(access==getType()) {
			MethodBoxModel partner = getPartner();

			if(getIncomingConnection()!=null)
				getIncomingConnection().disconnect();

			if(getOutgoingConnection()!=null)
				getOutgoingConnection().disconnect();
			//call to same class
			if(getChildren().contains(partner)) {
				removeChild(partner);
				getInstanceModel().addChild(partner);
			}
		} else {
			if(getOverridesConnection()!=null) getOverridesConnection().disconnect();
			for(ConnectionModel overrider : new ArrayList<ConnectionModel>(getOverriderConnections())) overrider.disconnect();

			List<ArtifactFragment> children=new ArrayList<ArtifactFragment>();
			children.addAll(getChildren());
			for(ArtifactFragment child : children) {
				if(child instanceof MemberModel)
					((MemberModel) child).deleteBasic();
				if(child instanceof HiddenNodeModel){
					for(MemberModel model:((HiddenNodeModel) child).getControlFlowMethodsHiding()){
						model.removeFromConditionalBlocks();
					}
					((NodeModel)child.getParentArt()).removeChild(child);
				}
			}

			if(getPartner()!=null) getPartner().deleteBasic();
		}
	}

	/*
	 * @param visited the list of method boxes that have already been examined in order to 
	 * find their partners and children
	 * @param removed the list of method boxes that have been removed from their parents
	 */
	private void removeContaineesAndPartner(List<MethodBoxModel> visited, List<MethodBoxModel> removed) {
		visited.add(this);
		MethodBoxModel partner = getPartner();
		List<ArtifactFragment> children=new ArrayList<ArtifactFragment>();
		List<ControlFlowBlock> listBlocks=new ArrayList<ControlFlowBlock>();
		children.addAll(getChildren());
		//remove hidden models
		for(MemberModel collapsedMethod : getCollapsedMethodChildren()) {
			collapsedMethod.removeFromConditionalBlocks();
		}
		for(ArtifactFragment containee : children) {
			if(containee instanceof FieldModel) ((FieldModel)containee).deleteBasic();
			else if(containee instanceof MethodBoxModel) 
				((MethodBoxModel)containee).removeContaineesAndPartner(visited, removed);

			else if(containee instanceof HiddenNodeModel) {
				HiddenNodeModel hiddenPartner=((HiddenNodeModel) containee).getPartner();
				if(hiddenPartner!=null && hiddenPartner.getParentArt()!=null){
					((NodeModel)hiddenPartner.getParentArt()).removeChild(containee);
					listBlocks.addAll(hiddenPartner.getConditionalBlocksContainedIn());
					for(ControlFlowBlock block:listBlocks){
						hiddenPartner.removeConditionalBlock(block);
					}
				}
				listBlocks.clear();
				listBlocks.addAll(((HiddenNodeModel) containee).getConditionalBlocksContainedIn());
				((NodeModel)((HiddenNodeModel) containee).getParentArt()).removeChild(containee);
				for(ControlFlowBlock block:listBlocks){
					((HiddenNodeModel) containee).removeConditionalBlock(block);
				}
			}
		}

		if(getOverridesConnection()!=null) getOverridesConnection().disconnect();
		for(ConnectionModel overrider : new ArrayList<ConnectionModel>(getOverriderConnections())) overrider.disconnect();

		if(partner==null) return;

		NodeModel partnerParent = partner.getParent();
		if(!visited.contains(partner)) partner.removeContaineesAndPartner(visited, removed);
		if(!removed.contains(partner)) {
			removed.add(partner);
			if(partnerParent!=null) {
				partnerParent.removeChild(partner);
				partner.removeFromConditionalBlocks();
			}
		}
	}

	private List<Invocation> findInvocationsMade() {

		final List<Invocation> allCalls = new ArrayList<Invocation>();

		if(getType()!=MethodBoxModel.declaration || 
				getMethod()==null)
			return allCalls;

		if(getMethod() instanceof AnonymousClassConstructor) {
			ImplicitConstructorCallLocation callLoc =
				((AnonymousClassConstructor)getMethod()).getImplicitCallToSuperConstructor();
			Invocation invoc = MethodUtil.getInvocation(callLoc, getMember());
			allCalls.add(invoc);
			return allCalls;
		}

		if(getASTNode()==null) return allCalls;

		try {
			new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, false, new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Finding calls...", 100);
					// Using MethodInvocationFinder because it finds calls to 
					// create anonymous classes and calleeRoot.getCalls() does not
					MethodInvocationFinder invocationFinder = new MethodInvocationFinder(getASTNode());
					List<Invocation> invoc=invocationFinder.getAllInvocations();
					for(Invocation invocation : invoc) {
						if (monitor.isCanceled()) continue;
						monitor.worked(100/invoc.size());
						if(invocation.getMethodElement()==null) continue;

						// Determine whether the invocation should be included in the list of 
						// invoked methods by testing whether the declaration containing the 
						// invocation is actually the declaration represented by this model. 
						// If it isn't, not including the invocation. For example, bar() should 
						// not be included in the list of invocations made by foo() in the case
						// foo() { anon = new AnonClass() { baz() { bar() } } }
						ASTNode parent = invocation.getInvocation().getParent();
						while(parent!=null 
								&& !(parent instanceof MethodDeclaration)
								&& !(parent instanceof Initializer)) {
							parent = parent.getParent();
						}
						if(parent==null) continue;
						if(!parent.equals(getASTNode())) {
							// Can't just continue here. getASTNode() may return the
							// node enclosing this node if it can't find the exact node 
							// for this method, so we need to make sure that parent 
							// really doesn't correspond to this method by comparing 
							// the IJavaElements
							if(!(parent instanceof MethodDeclaration)) continue;
							IJavaElement containingDecl = ((MethodDeclaration)parent).resolveBinding().getJavaElement();
							if(!getMember().equals(containingDecl)) continue;
						}
						allCalls.add(invocation);
					}
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Collections.sort(allCalls, new Comparator<Invocation>() {
			public int compare(Invocation loc1, Invocation loc2) {
				return loc1.getStartPosition()-loc2.getStartPosition();
			}
		});

		// A subclass constructor must always call a superclass constructor. If the subclass
		// does not explicitly call a superclass constructor, the compiler calls the
		// default no-argument superclass constructor
		// TODO: Add any implicit constructor call to the list

		return allCalls;
	}

	private boolean hasInvocationsOfUsingRepository() {
		if (getType() != MethodBoxModel.declaration ||
				getMethod() == null) return false;

		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		StatementIterator calledByIter = repo.getStatements(null, RJCore.calls, getMethodRes());
		int numberOfCalls = 0;
		while(calledByIter.hasNext()){
			Statement calledByStmt = calledByIter.next();
			boolean isPresentInDiagram = false;
			if(calledByStmt==null || !(calledByStmt.getSubject() instanceof URI)) continue;
			numberOfCalls++;
			for (MethodBoxModel sibling : getInstanceModel().getMethodChildren()){
				if (sibling.getPartner() != null) {
					Resource parentRes = ((MethodBoxModel) sibling.getPartner().getParent()).getMethodRes();
					if (parentRes != null && parentRes.equals(calledByStmt.getSubject())){
						isPresentInDiagram = true;
						break;
					}
				}

				//this is a self invocation
				if(sibling.getInstanceModel().equals(this.getInstanceModel()) && sibling.getMethodRes().equals(calledByStmt.getSubject())){
					isPresentInDiagram = true;
					break;
				}
			}

			if(!isPresentInDiagram)
				return true;
		}

		//case where there is exactly one call to the method which is present in the diagram
		if(numberOfCalls == 1) return false;

		//Called only in a specific case where the same method calls this method more than once.
		if(getCallsOfNotInDiagram().size() > 0) return true;
		return false;
	}

	public boolean hasInvocationsOf(){
		if(getType()!=MethodBoxModel.declaration ||
				getMethod()==null) return false;

		if(invocationsOf == null) 
			return hasInvocationsOfUsingRepository();
		else if(getCallsOfNotInDiagram().size() > 0) 
			return true; //has some call not in diagram

		return false;
	}

	private List<CallLocation> findInvocationsOf() {
		if(getType()!=MethodBoxModel.declaration ||
				getMethod()==null) return new ArrayList<CallLocation>();

		if(getMethod() instanceof AnonymousClassConstructor ||
				getMethod() instanceof CompilerGeneratedDefaultConstructor) {
			// MethodWrapper.getCalls() below returns no callers for an
			// anonymous class constructor and results in a
			// JavaElement.newNotPresentException() for compiler generated
			// constructors, so use repo and MethodInvocationFinder
			return findInvocationsOfAnonClassOrCompilerGenConstructor();
		}

		final List<CallLocation> allCallLocations = new ArrayList<CallLocation>();

		try {
			new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, false, new IRunnableWithProgress(){
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					MethodWrapper callerRoot = MethodUtil.getCallerRoot(getMethod());
					MethodWrapper[] wrappersOfCalls = callerRoot.getCalls(monitor);

					for(MethodWrapper wrapper : wrappersOfCalls) {

						if(wrapper.getMember() instanceof IInitializer) {
							// wrapper.getMethodCall().getCallLocations() will simply return
							// foo() as the invocation text rather than f.foo(). In order to
							// include the instance the method is called on (the "f" in "f.foo()"),
							// need to get the call using an invocation finder and create a 
							// CallLocation for it
							ASTNode initializerNode = RJCore.getCorrespondingASTNode(wrapper.getMember());
							if(initializerNode==null) continue;
							MethodInvocationFinder invocFinder = new MethodInvocationFinder(initializerNode);
							for(Invocation invoc : invocFinder.getAllInvocations()) {
								if(!getMember().equals(invoc.getMethodElement())) continue;
								InitializerWrapper wrappedCaller = new InitializerWrapper((IInitializer)wrapper.getMember());
								CallLocation callLoc = new CallLocation(
										wrappedCaller, getMember(), 
										invoc.getStartPosition(), invoc.getStartPosition()+invoc.getLength(), InstanceUtil.getLineNumber(getInstanceModel(), invoc.getStartPosition()));
								allCallLocations.add(callLoc);
							}
						}

						// If wrapper.getMember() is an IField, it means the call is made
						// from a class field (for example class C { private Bar b = new Bar(); }
						// and not from within a method declaration. Since we don't have any support
						// for showing calls made from a field (TODO #774), simply skip this case 
						// for now. (Relo displays such a call as C() -> Bar()).
						if(!(wrapper.getMember() instanceof IMethod)) continue;

						List<CallLocation> callsByWrapper = new ArrayList<CallLocation>();
						// In order to get the CallLocation that has the full invocation text 
						// (including instance invoked on), need to use the CallLocation
						// retrieved from the method call of the declaration containing the call
						// For example, calling getCallText() on the CallLocations in 
						// wrapper.getMethodCall().getCallLocations() will give you only "bar()"
						// for both a.bar() and b.bar() for the case where bar() is the method
						// represented by this model and foo(){a.bar(); b.bar();}. To get CallLocations 
						// that will return "a.bar()" and "b.bar()" as the call text, we need to 
						// look at the CallLocations of the calls made by the container of the 
						// call (foo()) and use the ones that correspond to calls to this
						// method (by comparing IMembers) since calling getCallText() on *these*
						// CallLocations *will* return "a.bar()" and "b.bar()"
						MethodWrapper caller = MethodUtil.getCalleeRoot((IMethod)wrapper.getMember());
						MethodWrapper[] allCallsMadeByContainerOfCall = caller.getCalls(new NullProgressMonitor());
						for(MethodWrapper callMadeByContainerOfCallToThis : allCallsMadeByContainerOfCall) {
							for(Object callLoc : callMadeByContainerOfCallToThis.getMethodCall().getCallLocations()) {
								if(getMethod().equals(((CallLocation)callLoc).getCalledMember())) {
									callsByWrapper.add(((CallLocation)callLoc));
								}
							}
						}
						int totalCallsToMethod = wrapper.getMethodCall().getCallLocations().size();
						if(callsByWrapper.size()!=totalCallsToMethod) {
							// Missed some calls to this method. Using MethodCall.getCallLocations()
							// to try to find what MethodWrapper.getCalls() did not.
							for(Object o : wrapper.getMethodCall().getCallLocations()) {
								CallLocation callLoc = (CallLocation) o;
								if(callsByWrapper.contains(callLoc)) continue; // already got this call

								Invocation invoc = MethodUtil.getInvocation(callLoc, (IMethod)callLoc.getMember());
								if(invoc==null) continue;

								IMethod invocElmt = invoc.getMethodElement();
								if(getMethod().equals(invocElmt)) {
									callsByWrapper.add(callLoc);
								} else if(invocElmt instanceof AnonymousClassConstructor &&
										getMethod().equals(((AnonymousClassConstructor)invocElmt).getSuperclassConstructor())) {
									// Handling the implicit call to the super class
									// constructor (this) used when defining an anonymous class.
									// callLoc's caller is the the declaration containing the anon 
									// class definition. However, we want the CallLocation to have
									// the anonymous class itself as the caller so it can represent the
									// implicit call made by it to this, the constructor defined in 
									// the class the anon class extends. So, creating a new 
									// ImplicitConstructorCallLocation that takes the anon class
									// as the caller
									ImplicitConstructorCallLocation implicitCallLoc = 
										new ImplicitConstructorCallLocation(
												invocElmt, getMethod(), ((AnonymousClassConstructor)invocElmt).getSuperclassConstructorBinding());
									callsByWrapper.add(implicitCallLoc);
								}
							}
						}
						allCallLocations.addAll(callsByWrapper);
					}
				}
			});

		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return allCallLocations;
	}

	// Use repo info and MethodInvocationFinder to find calls that 
	// create an anonymous class or invoke a compiler generated constructor
	private List<CallLocation> findInvocationsOfAnonClassOrCompilerGenConstructor() {
		List<CallLocation> allCallLocations = new ArrayList<CallLocation>();
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		StatementIterator calledByIter = repo.getStatements(null, RJCore.calls, getMethodRes());
		while(calledByIter.hasNext()) {
			Statement calledByStmt = calledByIter.next();
			if(calledByStmt==null || !(calledByStmt.getSubject() instanceof URI)) continue;

			IJavaElement callerElmt = RJCore.resourceToJDTElement(repo, calledByStmt.getSubject());
			if(!(callerElmt instanceof IMethod)) continue;

			if(callerElmt instanceof AnonymousClassConstructor
					&& getMethod().equals(((AnonymousClassConstructor)callerElmt).getSuperclassConstructor())) {
				// Handling the implicit call to the super class constructor (this)
				// from an anonymous class constructor (callerElmt). (Note that 
				// callerNode below will be null for callerElmt since it is an 
				// anon class constructor).
				ImplicitConstructorCallLocation implicitCallLoc = 
					new ImplicitConstructorCallLocation(
							(IMethod)callerElmt, getMethod(), ((AnonymousClassConstructor)callerElmt).getSuperclassConstructorBinding());
				allCallLocations.add(implicitCallLoc);
				continue;
			}

			ASTNode callerNode = RJCore.getCorrespondingASTNode((IMethod)callerElmt);
			if(callerNode == null || !(callerNode instanceof MethodDeclaration)) continue;

			MethodDeclaration declaration = (MethodDeclaration) callerNode;
			MethodInvocationFinder invocationFinder = new MethodInvocationFinder(declaration);
			for(Invocation invocation : invocationFinder.getAllInvocations()) {
				if(getMethod().equals(invocation.getMethodElement())) {
					// found a call to this
					CallLocation callLoc = new CallLocation((IMethod)callerElmt, getMethod(), invocation.getStartPosition(), invocation.getStartPosition()+invocation.getLength(), InstanceUtil.getLineNumber(getInstanceModel(), invocation.getStartPosition()));
					allCallLocations.add(callLoc);
				}
			}
		}
		return allCallLocations;
	}

	private List<IMethod> findMethodsThatOverride() {
		List<IMethod> declarationsThatOverride = new ArrayList<IMethod>();

		if(getType()!=MethodBoxModel.declaration ||
				getMethod()==null) return declarationsThatOverride;

		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		StatementIterator overridenByIter = repo.getStatements(null, RJCore.overrides, getMethodRes());
		while(overridenByIter.hasNext()) {
			Statement overridenByStmt = overridenByIter.next();
			if(overridenByStmt==null || !(overridenByStmt.getSubject() instanceof URI)) continue;

			IJavaElement overriderElmt = RJCore.resourceToJDTElement(repo, overridenByStmt.getSubject());
			if(!(overriderElmt instanceof IMethod)) continue;

			declarationsThatOverride.add((IMethod)overriderElmt);
		}
		return declarationsThatOverride;
	}

	private List<FieldRead> findFieldReadsMade() {
		ASTNode astNode = getASTNode();
		if(getType()!=declaration ||
				astNode==null ||
				!(astNode instanceof MethodDeclaration)) return new ArrayList<FieldRead>();

		FieldReadFinder readFinder = new FieldReadFinder((MethodDeclaration)astNode);
		return readFinder.getAllReads();
	}

}
