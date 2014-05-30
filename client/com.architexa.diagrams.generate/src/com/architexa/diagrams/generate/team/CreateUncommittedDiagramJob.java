package com.architexa.diagrams.generate.team;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.openrdf.model.Resource;

import com.architexa.diagrams.chrono.util.MethodDeclarationFinder;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.generate.GenerateUtil;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.builder.asm.AsmUtil;
import com.architexa.diagrams.jdt.compat.ASTUtil;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.utils.MethodInvocationFinder;
import com.architexa.diagrams.jdt.utils.UIUtils;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class CreateUncommittedDiagramJob extends Job{

	private static final Logger logger = GeneratePlugin.getLogger(CreateUncommittedDiagramJob.class);
	List<IFile> filesWithChanges; 
	Object revisionToCompareTo; 
	IRSEDiagramEngine diagramEngine;
	List<CodeUnit> createdFrags = new ArrayList<CodeUnit>();
	UncommittedChangesDiagramGenerator uncommittedChangesDiagramGenerator;
	
	public CreateUncommittedDiagramJob(UncommittedChangesDiagramGenerator uncommittedChangesDiagramGenerator, String name, List<IFile> filesWithChanges, Object revisionToCompareTo, IRSEDiagramEngine diagramEngine) {
		super(name);
		this.uncommittedChangesDiagramGenerator = uncommittedChangesDiagramGenerator;
		this.filesWithChanges = new ArrayList<IFile>(filesWithChanges);
		this.revisionToCompareTo = revisionToCompareTo;
		this.diagramEngine = diagramEngine;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		createdFrags = findChangesAndGetCreateFrags(filesWithChanges, revisionToCompareTo, monitor);
		return Status.OK_STATUS;
	}
	
	public List<CodeUnit> getCreatedFrags() {
		return createdFrags;
	}
	
	private List<CodeUnit> findChangesAndGetCreateFrags(List<IFile> filesWithChanges, Object revisionToCompareTo, IProgressMonitor monitor) {

		for(IFile fileWithChanges : filesWithChanges) {
			CompilationUnit fileCU = ASTUtil.getAST(fileWithChanges);
			if(fileCU==null) {
				logger.warn("Could not get CompilationUnit for file with changes "+fileWithChanges.getName());
				continue;
			}

			// Find positions of changes between the file and the revision
			Object revision = revisionToCompareTo!=null ? revisionToCompareTo : uncommittedChangesDiagramGenerator.getLatestRevision(fileWithChanges);
			IDocument lDoc = uncommittedChangesDiagramGenerator.getRevisionFileDocument(fileWithChanges, monitor);
			IDocument rDoc = uncommittedChangesDiagramGenerator.getRevisionFileDocument(revision, monitor);
			List<Position> positionsOfChanges = uncommittedChangesDiagramGenerator.getPositionsOfChangesInFile(lDoc, rDoc, monitor);								
			if(positionsOfChanges.size()==0) {
				logger.info("File "+fileWithChanges.getName()+" contains no changes");
				continue;
			}

			// Find uncommitted members (fields, methods,
			// inner classes) and calls at those positions
			final List<ASTNode> members = new ArrayList<ASTNode>();
			List<Invocation> invocations = new ArrayList<Invocation>();
			MethodInvocationFinder invocationFinder = new MethodInvocationFinder(fileCU);
			for(final Position position : positionsOfChanges) {

				final int positionStart = position.getOffset();
				final int positionEnd = positionStart+position.getLength();

				
//				CompilationUnit revCU = fileCUToRevisionCUMap.get(fileCU);
//				if (revCU != null)
//					addDeletedOrCommentedMembers(members, revCU, positionStart, positionEnd);
				// Find uncommitted fields, methods, and inner classes
				fileCU.accept(new ASTVisitor() {
					public void testNodeWithinChangedPos(ASTNode node) {
//						System.err.println(node);
						int nodeStart = node.getStartPosition();
						int nodeEnd = nodeStart+node.getLength();
						// node is a change if it's within position of a change
						if(positionStart<=nodeStart &&
								nodeEnd<=positionEnd) members.add(node);
						// add node if it contains a change, for example
						// the following test will return true for the 
						// change boolean b = true; -> boolean b = false;
						else if(nodeStart<=positionStart &&
								positionEnd<=nodeEnd) members.add(node);
					}
					@Override
					public boolean visit(FieldDeclaration node) {
						testNodeWithinChangedPos(node); return false;
					}
					@Override
					public boolean visit(MethodDeclaration node) {
						testNodeWithinChangedPos(node); return false;
					}
					@Override
					public boolean visit(TypeDeclaration node) {
						testNodeWithinChangedPos(node); return true;
						// return true because if node is an inner class that has
						// changed, want to also visit its members so that ones 
						// that have changed can also be added in the diagram
					}
				});

				// Find uncommitted method calls
				for(int i=position.getOffset(); i<=position.getOffset()+position.getLength(); i++) {
					Invocation invocation = invocationFinder.findInvocation(i); 
					if(invocation!=null && !invocations.contains(invocation)) {
						invocations.add(invocation);
					}
				}
			}

			// Create ArtifactFragments for the member and method call changes
			return getCreateFrags(fileWithChanges, fileCU, invocations, members);
		}
		return new ArrayList<CodeUnit>();
	}
	
	private List<CodeUnit> getCreateFrags(IFile fileWithChanges,
			CompilationUnit fileCU,
			List<Invocation> invocations,
			List<ASTNode> members) {

		// Create frag for this file
		// containing uncommitted changes
		List<CodeUnit> createdFrags = new ArrayList<CodeUnit>();
		try {
			Resource classWithChangesRes = AsmUtil.toWkspcResource(StoreUtil.getDefaultStoreRepository(), UIUtils.getName(JavaCore.create(fileWithChanges)));
			CodeUnit fileClassFrag = GenerateUtil.getCodeUnitForRes(classWithChangesRes, null, createdFrags, null);
			
			// Create the source, target, and call
			// frags for the uncommitted method calls
			createCallFrags(fileCU, fileClassFrag, invocations, createdFrags);
	
			// Create frags for new members
			createMemberFrags(members, fileClassFrag, createdFrags);
		} catch (Throwable t) {
			com.architexa.collab.UIUtils.openErrorPromptDialog("Error Creating Code Review Diagram", "There was a problem creating a diagram for your change set.");
		}
		return createdFrags;
	}

	private void createCallFrags(CompilationUnit fileCU, 
			CodeUnit fileClassFrag, 
			List<Invocation> invocations,
			List<CodeUnit> createdFrags) {
		MethodDeclarationFinder declarationFinder = new MethodDeclarationFinder(fileCU);
		for(Invocation invocation : invocations) {

			IJavaElement container = null;
			if(invocation.resolveMethodBinding()!=null) {
				container = invocation.resolveMethodBinding().getDeclaringClass().getJavaElement();
			}
			if(container==null) continue;

			ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();

			// source of call
			MethodDeclaration declaration = declarationFinder.findDeclaration(invocation.getStartPosition());
			if(declaration==null || declaration.resolveBinding()==null || declaration.resolveBinding().getJavaElement()==null) continue;
			Resource declarationRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), declaration.resolveBinding().getJavaElement());
			CodeUnit parentUnit = getDeclaringClassFrag(declaration.resolveBinding(), createdFrags, repo);
			CodeUnit declarationThatMakesInvocation = GenerateUtil.getCodeUnitForRes(declarationRes, null, createdFrags, parentUnit);

			// class containing target of call
			String instanceName = MethodUtil.getInstanceCalledOn(invocation);
			CodeUnit containerParent = getDeclaringClassFrag(invocation.resolveMethodBinding().getDeclaringClass(), createdFrags, repo);
			CodeUnit containerOfDeclOfInvokedMethod = GenerateUtil.getCodeUnitForRes(RJCore.jdtElementToResource(repo, container), instanceName, createdFrags, containerParent);
			// target of call
			Resource declOfInvokedMethodRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), invocation.resolveMethodBinding().getJavaElement());
			CodeUnit declOfInvokedMethodCU = GenerateUtil.getCodeUnitForRes(declOfInvokedMethodRes, null, createdFrags, containerOfDeclOfInvokedMethod);

			// call connection from source -> target
			ArtifactRel rel = new ArtifactRel(declarationThatMakesInvocation, declOfInvokedMethodCU, RJCore.calls);
			declarationThatMakesInvocation.addSourceConnection(rel);
			declOfInvokedMethodCU.addTargetConnection(rel);
		}
	}

	// Create ArtifactFragments for the given
	// nodes of fields, methods, and inner classes.
	// Created frags will be added to the list
	// of created frags.
	private void createMemberFrags(List<ASTNode> nodes,
			CodeUnit fileClassFrag, 
			List<CodeUnit> createdFrags) {

		List<IBinding> bindings = new ArrayList<IBinding>();

		for(ASTNode diffNode : nodes) {

			if(diffNode instanceof TypeDeclaration) {
				ITypeBinding binding = ((TypeDeclaration)diffNode).resolveBinding();
				bindings.add(binding);
			} else if(diffNode instanceof FieldDeclaration) {
				List<?> varDecls = ((FieldDeclaration)diffNode).fragments();
				for(Object varDecl : varDecls) {
					if(!(varDecl instanceof VariableDeclarationFragment)) continue;
					IVariableBinding binding = ((VariableDeclarationFragment)varDecl).resolveBinding();
					bindings.add(binding);
				}
			} else if(diffNode instanceof MethodDeclaration) {
				IMethodBinding binding = ((MethodDeclaration)diffNode).resolveBinding();
				bindings.add(binding);
			}
		}
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		for(IBinding binding : bindings) {
			if(binding==null || binding.getJavaElement()==null) continue;

			Resource res = RJCore.jdtElementToResource(repo, binding.getJavaElement());
			CodeUnit parentUnit = getDeclaringClassFrag(binding, createdFrags, repo);
			GenerateUtil.getCodeUnitForRes(res, null, createdFrags, parentUnit);
		}
	}

	private CodeUnit getDeclaringClassFrag(IBinding binding, List<CodeUnit> createdFrags, ReloRdfRepository repo) {

		IBinding declaringClassBinding = null;
		if(binding instanceof ITypeBinding) 
			declaringClassBinding = ((ITypeBinding)binding).getDeclaringClass();
		else if(binding instanceof IVariableBinding) 
			declaringClassBinding = ((IVariableBinding)binding).getDeclaringClass();
		else if(binding instanceof IMethodBinding) 
			declaringClassBinding = ((IMethodBinding)binding).getDeclaringClass();
		if(declaringClassBinding==null) return null;

		Resource declaringClassRes = RJCore.bindingToResource(repo, declaringClassBinding);
		CodeUnit parentUnit = getDeclaringClassFrag(declaringClassBinding, createdFrags, repo);
		CodeUnit declaringClassFrag = GenerateUtil.getCodeUnitForRes(declaringClassRes, null, createdFrags, parentUnit);
		return declaringClassFrag;
	}
}
