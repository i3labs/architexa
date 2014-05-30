package com.architexa.diagrams.jdt.builder.asm;

import org.apache.log4j.Logger;
import org.objectweb.asm.signature.SignatureVisitor;
import org.openrdf.model.Resource;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.store.ReloRdfRepository;


/**
 * TODO: examine for deletion
 */
public class AsmSignatureVisitor implements SignatureVisitor {

	private static final Logger logger = Activator.getLogger(AsmSignatureVisitor.class);


	//private Resource ownerKey;

	/**
	 * Creates a new AsmSignatureVisitor object.
	 * 
	 * @param rrr
	 * @param ownerKey
	 */
	protected AsmSignatureVisitor(ReloRdfRepository rrr, Resource ownerKey) {
		//this.ownerKey = ownerKey;
	}

	public SignatureVisitor visitArrayType() {
		return this;
	}

	public void visitBaseType(char arg0) {
		logger.debug("visitBaseType(" + arg0 + ") - start");

		// visitType(Type.getType(String.valueOf(arg0)));
	}

	public SignatureVisitor visitClassBound() {
		return this;
	}

	public void visitClassType(String arg0) {
		logger.debug("visitClassType(" + arg0 + ") - start");

		// visitType(Type.getType(arg0));
	}

	public void visitEnd() {
	}

	public SignatureVisitor visitExceptionType() {
		return this;
	}

	/**
	 * @param arg0 name the name of the formal parameter.
	 */
	public void visitFormalTypeParameter(String arg0) {
		logger.debug("visitFormalTypeParameter(" + arg0 + ") - start");

		// commented since we are really not doing anything here, and it was giving an error
		// in particular because arg0 is the name and not the type
		//visitType(Type.getType(arg0));
	}

	public void visitInnerClassType(String arg0) {
		logger.debug("visitInnerClassType(" + arg0 + ") - start");
	}

	public SignatureVisitor visitInterface() {
		return this;
	}

	public SignatureVisitor visitInterfaceBound() {
		return this;
	}

	public SignatureVisitor visitParameterType() {
		return this;
	}

	public SignatureVisitor visitReturnType() {
		return this;
	}

	public SignatureVisitor visitSuperclass() {
		return this;
	}

	public void visitTypeArgument() {
	}

	public SignatureVisitor visitTypeArgument(char arg0) {
		logger.debug("visitTypeArgument(" + arg0 + ") - start");

		return this;
	}

	public void visitTypeVariable(String arg0) {
		logger.debug("visitTypeVariable(" + arg0 + ") - start");

		// visitType(Type.getType(arg0));
	}

	/**
	 * @param t
	 */
	// no one calls this - adding references needs to be fixed up
	//private void visitType(Type t) {
		// addReference(ownerKey, AsmUtil.toReloClassResource(t));
	//}
}
