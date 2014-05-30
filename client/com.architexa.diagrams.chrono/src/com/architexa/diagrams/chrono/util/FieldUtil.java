package com.architexa.diagrams.chrono.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.architexa.diagrams.chrono.commands.ConnectionCreateCommand;
import com.architexa.diagrams.chrono.commands.MemberCreateCommand;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.sequence.FieldRead;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class FieldUtil {

	/**
	 * Returns a FieldModel representing the given field declaration. If
	 * the given instance already contains a FieldModel representing the
	 * field, that FieldModel is returned. Otherwise, a new FieldModel
	 * is created, added to the instance at the given index, and then returned.
	 * 
	 */
	public static FieldModel findOrCreateFieldModel(IField field, Resource fieldRes, InstanceModel instance, int indexToAddAt, Command cmd) {
		for(ArtifactFragment af : instance.getChildren()) {
			if(!(af instanceof FieldModel)) continue;
			FieldModel fm = (FieldModel)af;
			if(fieldRes.equals(fm.getArt().elementRes)) return fm;
		}
		// Model for the field isn't already present in the instance,
		// so make a model for it and add it to the instance
		FieldModel fieldDeclaration = new FieldModel(instance, field, RJCore.getCorrespondingASTNode(field), MemberModel.declaration);
		instance.addChild(fieldDeclaration, indexToAddAt);

		return fieldDeclaration;
	}

	public static ImageDescriptor getFieldIconDescriptor(Resource access) {
		String iconKey = null;
		if(access==null) 
			iconKey = ISharedImages.IMG_FIELD_DEFAULT;
		else if (access.equals(RJCore.publicAccess))
			iconKey = ISharedImages.IMG_FIELD_PUBLIC;
		else if (access.equals(RJCore.protectedAccess))
			iconKey = ISharedImages.IMG_FIELD_PROTECTED;
		else if (access.equals(RJCore.privateAccess))
			iconKey = ISharedImages.IMG_FIELD_PRIVATE;
		else iconKey =  ISharedImages.IMG_FIELD_DEFAULT;

		return SeqUtil.getImageDescriptorFromKey(iconKey);
	}

	/**
	 * If a field is read and used as an argument in the given method invocation or 
	 * if a field is read and the return value of the invoked method is assigned to it, 
	 * field models are created for it along with the read or write connections between them,
	 * and the field models are added to the correct instances.
	 * Multiple pairs of field models may be created if:
	 *  - the invocation contains multiple fields in its arguments or 
	 *  - the invocation contains one or more field arguments and its return value is 
	 *  also assigned to a field
	 * 
	 * @param invocation the method invocation that may contain one or more field arguments
	 * and may have its return value assigned to a field
	 * @param methodThatReadsField the method model that contains invocation and may
	 * therefore read and/or assign fields
	 * @param indexOfInvocationModel the index of the invocation model in its parent declaration model
	 * @param diagram is the parent diagram model
	 * @param conditionalBlockStmts is the list of statements to be added in the block calling this method
	 */
	public static void handleFieldReadOrWrite(Invocation invocation, MethodBoxModel methodThatReadsField, int indexOfInvocationModel, DiagramModel diagram, CompoundCommand command, List<MemberModel> conditionalBlockStmts) {

		// Finding any fields that are read and used as arguments in the invocation
		// and adding them to the diagram

		List<FieldRead> fieldArgs = new ArrayList<FieldRead>();
		for(Expression exp : invocation.getArguments()) {
			getFieldParameters(exp, fieldArgs);
		}
		for(FieldRead read : fieldArgs) {
			createModelsForField(methodThatReadsField, read, indexOfInvocationModel, ConnectionModel.FIELD_READ, diagram, command, conditionalBlockStmts);
			indexOfInvocationModel = indexOfInvocationModel + 1;
		}


		// Determining whether the return value of the invoked method is assigned to a
		// field and if so adding that field to the diagram

		if(!(invocation.getInvocation().getParent() instanceof Assignment)) return;

		Expression leftHandSide = ((Assignment)invocation.getInvocation().getParent()).getLeftHandSide();

		FieldRead read = null;
		if(leftHandSide instanceof FieldAccess) {
			read = new FieldRead((FieldAccess)leftHandSide);
		} else if(leftHandSide instanceof SuperFieldAccess) {
			read = new FieldRead((SuperFieldAccess)leftHandSide);
		} else if(leftHandSide instanceof QualifiedName) {
			read = new FieldRead((QualifiedName)leftHandSide);
		}
		if(read!=null) createModelsForField(methodThatReadsField, read, indexOfInvocationModel+1, ConnectionModel.FIELD_WRITE, diagram, command, conditionalBlockStmts);
	}

	/**
	 * Creates the field models and the read or write connection between them
	 * for the field being read and/or assigned and adds the field models to 
	 * the correct instances
	 *  
	 * @param methodThatReadsField the method model that accesses the field
	 * @param read the field being read and/or assigned
	 * @param indexOfFieldRead the index at which to add the created field access model 
	 * to methodThatReadsField
	 * @param readOrWrite the type of access (read if the field is only read, write 
	 * if it is read and then assigned)
	 * @param diagram is the parent diagram model
	 * @param conditionalBlockStmts is the list of statements to be added in the block calling this method
	 * @return the created field model corresponding to the access in the method
	 */
	public static FieldModel createModelsForField(MethodBoxModel methodThatReadsField, FieldRead read, int indexOfFieldRead, URI readOrWrite, DiagramModel diagram, CompoundCommand command, List<MemberModel> conditionalBlockStmts) {

		IField fieldElt = (IField) read.resolveFieldBinding().getJavaElement();
		FieldModel fieldRead = createFieldReadModel(read, fieldElt, methodThatReadsField.getInstanceModel());

		// to check if the call already exists in the diagram
		for(ArtifactFragment child:methodThatReadsField.getChildren()){
			if(child instanceof FieldModel &&
					fieldRead.getCharStart() == ((FieldModel)child).getCharStart() &&
					fieldRead.getCharEnd() == ((FieldModel)child).getCharEnd()){
				return null;
			}
		}

		command.add(new MemberCreateCommand(fieldRead,methodThatReadsField,MemberCreateCommand.NONE));

		InstanceModel fieldOwner = getFieldOwner(read, methodThatReadsField, diagram, command);
		FieldModel fieldDeclaration = new FieldModel(fieldOwner, fieldElt, RJCore.getCorrespondingASTNode(fieldElt), MemberModel.declaration);
		// if the field declaration has the same instance parent then it should show up 
		// as a self call with the parent method of the access as its parent.
		MemberCreateCommand fieldDeclCreateCmd;
		if(fieldOwner.equals(methodThatReadsField.getInstanceModel()))
			fieldDeclCreateCmd = new MemberCreateCommand(fieldDeclaration,methodThatReadsField,"");
		else
			fieldDeclCreateCmd = new MemberCreateCommand(fieldDeclaration,fieldOwner,"");
		
		fieldDeclCreateCmd.setAccessPartner(fieldRead, methodThatReadsField);
		command.add(fieldDeclCreateCmd);

		command.add(new ConnectionCreateCommand(fieldRead, fieldDeclaration, read.toString(), readOrWrite));
		//add field to conditional block
		if (conditionalBlockStmts != null)
			conditionalBlockStmts.add(fieldRead);
		return fieldRead;
	}

	public static FieldModel createFieldReadModel(FieldRead read, IField fieldElt, InstanceModel instance) {
		FieldModel fieldAccess = new FieldModel(instance, fieldElt, read.getFieldRead(), MemberModel.access);
		fieldAccess.setCharStart(read.getStartPosition());
		fieldAccess.setCharEnd(read.getStartPosition() + read.getLength());
		return fieldAccess;
	}

	/**
	 * 
	 * @param read the field being read
	 * @param methodThatReadsField the method model that references the field
	 * @param diagram
	 * @return the instance model to which the given field belongs
	 */
	public static InstanceModel getFieldOwner(FieldRead read, MethodBoxModel methodThatReadsField, DiagramModel diagram, CompoundCommand command) {

		String instanceName = getInstanceCalledOn(read);
		if("this".equals(instanceName) || "super".equals(instanceName)) 
			return methodThatReadsField.getInstanceModel();

		IType declaringClass = ((IField)read.resolveFieldBinding().getJavaElement()).getDeclaringType();
		Resource declaringClassRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), declaringClass);
		String className = InstanceUtil.getClassName(declaringClassRes, StoreUtil.getDefaultStoreRepository());

		return InstanceUtil.findOrCreateContainerInstanceModel(instanceName, className, declaringClassRes, diagram, -1, command);
	}

	/**
	 * 
	 * @return the name of the instance to which the given field belongs
	 */
	public static String getInstanceCalledOn(FieldRead read) {
		String[] instancesAndField = read.toString().split("\\.");
		if(instancesAndField.length<2) return null;
		return instancesAndField[instancesAndField.length-2];
	}

	/**
	 * TODO: Ensure it's correct and handles all field access cases. Right now it
	 * doesn't matter too much because read and write connections look the same.
	 * @return ConnectionModel.FIELD_READ or ConnectionModel.FIELD_WRITE
	 */
	public static URI getFieldAccessType(FieldRead fieldRead) {

		ASTNode parent = fieldRead.getFieldRead();
		while(parent!=null && !(parent instanceof Assignment && 
				Assignment.Operator.ASSIGN.equals(((Assignment)parent).getOperator()))) {
			parent = parent.getParent();
		}

		if(parent==null || (!(parent instanceof Assignment)) || 
				!Assignment.Operator.ASSIGN.equals(((Assignment)parent).getOperator()))
			return ConnectionModel.FIELD_READ;

		Assignment assignment = (Assignment) parent;
		Expression leftHandSide = assignment.getLeftHandSide();
		if(fieldRead.equals(leftHandSide)) return ConnectionModel.FIELD_WRITE;

		// If the access of the field (call it field)
		// is on the right hand side, it's a read
		// If it's on the left hand side, check whether it's like
		// foo.field = 5 (write) or foo.field.bar = 5 (read)
		parent = fieldRead.getFieldRead();
		while(parent!=null && !parent.equals(assignment)) {

			if(parent.equals(assignment.getRightHandSide())) 
				return ConnectionModel.FIELD_READ;

			if(parent.equals(leftHandSide) && leftHandSide instanceof QualifiedName 
					&& ((QualifiedName)leftHandSide).getName().equals(fieldRead.getName()))
				return ConnectionModel.FIELD_WRITE;

			parent = parent.getParent();
		}

		return ConnectionModel.FIELD_READ;
	}

	private static void getFieldParameters(Expression param, List<FieldRead> reads) {
		if(param instanceof InfixExpression) {
			getFieldParameters(((InfixExpression)param).getLeftOperand(), reads);
			getFieldParameters(((InfixExpression)param).getRightOperand(), reads);
			for(Object extendedOperand : ((InfixExpression)param).extendedOperands()) {
				getFieldParameters((Expression)extendedOperand, reads);
			}
		} else if(param instanceof ParenthesizedExpression) {
			getFieldParameters(((ParenthesizedExpression)param).getExpression(), reads);
		}
		else if(param instanceof FieldAccess) reads.add(new FieldRead((FieldAccess)param));
		else if(param instanceof SuperFieldAccess) reads.add(new FieldRead((SuperFieldAccess)param));
		else if(param instanceof QualifiedName) reads.add(new FieldRead((QualifiedName)param));
	}

}
