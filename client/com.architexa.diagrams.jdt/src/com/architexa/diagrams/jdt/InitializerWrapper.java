package com.architexa.diagrams.jdt;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceMethod;

import com.architexa.diagrams.jdt.builder.asm.AsmUtil;

public class InitializerWrapper extends SourceMethod implements IMethod {

	private static String initializerName = "<clinit>";

	IInitializer initializerElmt;
	public InitializerWrapper(IInitializer initializer) {
		super((JavaElement)initializer.getParent(), initializerName, null);
		this.initializerElmt = initializer;
	}

	@Override
	public String[] getExceptionTypes() throws JavaModelException {
		return new String[]{};
	}
	@Override
	public String[] getParameterNames() throws JavaModelException {
		return new String[]{};
	}
	@Override
	public String getReturnType() throws JavaModelException {
		return Signature.SIG_VOID;
	}
	@Override
	public String getSignature() throws JavaModelException {
		return AsmUtil.getMethodSignature(initializerName, "()V");

	}
	@Override
	public ITypeParameter[] getTypeParameters() throws JavaModelException {
		return new ITypeParameter[]{};
	}
	/**
	 * For our purposes, we can consider initializers like a constructor
	 * since they cannot be overridden, renamed, hidden, etc.
	 */
	@Override
	public boolean isConstructor() throws JavaModelException {
		return true;
	}
	@Override
	public boolean isMainMethod() throws JavaModelException {
		return false;
	}
	@Override
	public boolean isSimilar(IMethod method) {
		return false;
	}

	@Override
	public String[] getCategories() throws JavaModelException {
		return initializerElmt.getCategories();
	}
	@Override
	public IClassFile getClassFile() {
		return initializerElmt.getClassFile();
	}
	@Override
	public ICompilationUnit getCompilationUnit() {
		return initializerElmt.getCompilationUnit();
	}
	@Override
	public IType getDeclaringType() {
		return initializerElmt.getDeclaringType();
	}
	@Override
	public int getFlags() throws JavaModelException {
		return initializerElmt.getFlags();
	}
	@Override
	public ISourceRange getJavadocRange() throws JavaModelException {
		return initializerElmt.getJavadocRange();
	}
	@Override
	public ISourceRange getNameRange() throws JavaModelException {
		return initializerElmt.getNameRange();
	}
	@Override
	public int getOccurrenceCount() {
		return initializerElmt.getOccurrenceCount();
	}
	@Override
	public IType getType(String name, int occurrenceCount) {
		return initializerElmt.getType(name, occurrenceCount);
	}
	// ije.getTypeRoot() not available in Eclipse 3.2
	//	@Override
	//	public ITypeRoot getTypeRoot() {
	//		return initializerElmt.getTypeRoot();
	//	}
	@Override
	public boolean isBinary() {
		return initializerElmt.isBinary();
	}
	@Override
	public boolean exists() {
		return initializerElmt.exists();
	}
	@Override
	public IJavaElement getAncestor(int ancestorType) {
		return initializerElmt.getAncestor(ancestorType);
	}
	@Override
	public String getAttachedJavadoc(IProgressMonitor monitor)
	throws JavaModelException {
		return initializerElmt.getAttachedJavadoc(monitor);
	}
	@Override
	public IResource getCorrespondingResource() throws JavaModelException {
		return initializerElmt.getCorrespondingResource();
	}
	@Override
	public int getElementType() {
		return initializerElmt.getElementType();
	}
	@Override
	public String getHandleIdentifier() {
		return initializerElmt.getHandleIdentifier();
	}
	@Override
	public IJavaModel getJavaModel() {
		return initializerElmt.getJavaModel();
	}
	@Override
	public IJavaProject getJavaProject() {
		return initializerElmt.getJavaProject();
	}
	@Override
	public IOpenable getOpenable() {
		return initializerElmt.getOpenable();
	}
	@Override
	public IJavaElement getParent() {
		return initializerElmt.getParent();
	}
	@Override
	public IPath getPath() {
		return initializerElmt.getPath();
	}
	@Override
	public IJavaElement getPrimaryElement() {
		return initializerElmt.getPrimaryElement();
	}
	@Override
	public IResource getResource() {
		return initializerElmt.getResource();
	}
	@Override
	public ISchedulingRule getSchedulingRule() {
		return initializerElmt.getSchedulingRule();
	}
	@Override
	public IResource getUnderlyingResource() throws JavaModelException {
		return initializerElmt.getUnderlyingResource();
	}
	@Override
	public boolean isReadOnly() {
		return initializerElmt.isReadOnly();
	}
	@Override
	public boolean isStructureKnown() throws JavaModelException {
		return initializerElmt.isStructureKnown();
	}
	@Override
	public Object getAdapter(Class adapter) {
		return initializerElmt.getAdapter(adapter);
	}
	@Override
	public String getSource() throws JavaModelException {
		return initializerElmt.getSource();
	}
	@Override
	public ISourceRange getSourceRange() throws JavaModelException {
		return initializerElmt.getSourceRange();
	}
	@Override
	public void copy(IJavaElement container, IJavaElement sibling,
			String rename, boolean replace, IProgressMonitor monitor)
	throws JavaModelException {
		initializerElmt.copy(container, sibling, rename, replace, monitor);
	}
	@Override
	public void delete(boolean force, IProgressMonitor monitor)
	throws JavaModelException {
		initializerElmt.delete(force, monitor);
	}
	@Override
	public void move(IJavaElement container, IJavaElement sibling,
			String rename, boolean replace, IProgressMonitor monitor)
	throws JavaModelException {
		initializerElmt.move(container, sibling, rename, replace, monitor);
	}
	@Override
	public void rename(String name, boolean replace, IProgressMonitor monitor)
	throws JavaModelException {
		initializerElmt.rename(name, replace, monitor);
	}
	@Override
	public IJavaElement[] getChildren() throws JavaModelException {
		return initializerElmt.getChildren();
	}
	@Override
	public boolean hasChildren() throws JavaModelException {
		return initializerElmt.hasChildren();
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof InitializerWrapper)) return false;
		return initializerElmt.equals(
				((InitializerWrapper)obj).initializerElmt);
	}
	@Override
	public int hashCode() {
		return initializerElmt.hashCode();
	}
	@Override
	public String toString() {
		return initializerElmt.toString();
	}

	/**
	 * @see JavaElement#getHandleMemento()
	 */
	@Override
	protected char getHandleMementoDelimiter() {
		return JavaElement.JEM_INITIALIZER;
	}
	@Override
	public String readableName() {
		return ((JavaElement)getDeclaringType()).readableName();
	}

}
