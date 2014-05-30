package com.architexa.diagrams.chrono.util;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.openrdf.model.Resource;

import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.sequence.FieldRead;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class LinkedTrackerUtil {

	public static NodeModel createAddNodeAndRelCommand(DiagramEditPart diagramController, Artifact art, NodeModel mostRecentNavigation, int mostRecentLineNumber, Object selElement, CompoundCommand command) {
		DiagramModel diagram = (DiagramModel) diagramController.getModel();
		Resource resToAdd = art.elementRes;
		IJavaElement selectedElt = null;
		if(selElement instanceof IJavaElement) 
			selectedElt = (IJavaElement) selElement;

		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		IJavaElement elmtToAdd = selectedElt!=null ? selectedElt : RJCore.resourceToJDTElement(repo, resToAdd);
		NodeModel addedNode = null;

		if(IJavaElement.TYPE==elmtToAdd.getElementType()) {
			addedNode = InstanceUtil.findOrCreateContainerInstanceModel(null, InstanceUtil.getClassName(resToAdd, repo), resToAdd, diagram, -1, command);
		} else if(elmtToAdd instanceof IMethod && elmtToAdd.getParent() instanceof IType) {
			if(mostRecentNavigation instanceof MethodBoxModel) {
				addMethodModelsAndConns(addedNode, resToAdd, mostRecentNavigation, mostRecentLineNumber, diagram, command);
			}
			if(addedNode==null) {
				IType containerClass = (IType) elmtToAdd.getParent();
				Resource classRes = RJCore.jdtElementToResource(repo, containerClass);
				InstanceModel instance = InstanceUtil.findOrCreateContainerInstanceModel(null, InstanceUtil.getClassName(classRes, repo), classRes, diagram, -1, command);

				IMethod method = (IMethod)elmtToAdd;
				addedNode = MethodUtil.findOrCreateMethodModel(method, resToAdd, instance, -1, command);
			}
		} else if(elmtToAdd instanceof IField && elmtToAdd.getParent() instanceof IType) {
			if(mostRecentNavigation instanceof MethodBoxModel) {
				addFieldModelsAndConns(addedNode, resToAdd, mostRecentNavigation, mostRecentLineNumber, diagram, command);
			}
			if(addedNode==null) {
				IType containerClass = (IType) elmtToAdd.getParent();
				Resource classRes = RJCore.jdtElementToResource(repo, containerClass);
				InstanceModel instance = InstanceUtil.findOrCreateContainerInstanceModel(null, InstanceUtil.getClassName(classRes, repo), classRes, diagram, -1, command);

				IField field = (IField) elmtToAdd;
				addedNode = FieldUtil.findOrCreateFieldModel(field, resToAdd, instance, -1, command);
			}
		}
		return addedNode;
	}

	private static void addMethodModelsAndConns(NodeModel addedNode, Resource resToAdd, NodeModel mostRecentNavigation, int mostRecentLineNumber, DiagramModel diagram, CompoundCommand command) {

		MethodBoxModel srcMethod = (MethodBoxModel) mostRecentNavigation;
		for(Invocation invocation : srcMethod.getCallsMade(null)) {

			Resource invocationRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), invocation.getMethodElement());
			int invocationLineNum = InstanceUtil.getLineNumber(srcMethod.getInstanceModel(), invocation.getStartPosition());
			if(resToAdd.equals(invocationRes) && invocationLineNum==mostRecentLineNumber) {

				// test whether call doesn't need to
				// be added because already in diagram
				for(MethodBoxModel child : srcMethod.getMethodChildren()) {
					if(invocation.getInvocation().equals(child.getASTNode())) {
						addedNode = child.getPartner(); 
						return;
					}
				}
				Resource classOfInstance = MethodUtil.getClassOfInstanceCalledOn(invocation, srcMethod.getInstanceModel());
				MethodBoxModel invocationModel = MethodUtil.createModelsForMethodRes(invocation, srcMethod, diagram, classOfInstance, null, false, command, null, false);
				addedNode = invocationModel.getPartner();
				return;
			}
		}
	}

	private static void addFieldModelsAndConns(NodeModel addedNode, Resource resToAdd, NodeModel mostRecentNavigation, int mostRecentLineNumber, DiagramModel diagram, CompoundCommand command) {

		MethodBoxModel methodAccessingField = (MethodBoxModel) mostRecentNavigation;
		for(FieldRead fieldRead : methodAccessingField.getFieldReadsMade()) {

			Resource fieldReadRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), fieldRead.resolveFieldBinding().getJavaElement());
			int fieldReadLineNum = InstanceUtil.getLineNumber(methodAccessingField.getInstanceModel(), fieldRead.getStartPosition());
			if(resToAdd.equals(fieldReadRes) && fieldReadLineNum==mostRecentLineNumber) {

				// test whether field read doesn't need to
				// be added because already in diagram
				for(FieldModel child : methodAccessingField.getFieldChildren()) {
					if(fieldRead.getFieldRead().equals(child.getASTNode())) {
						addedNode = child.getPartner(); 
						return;
					}
				}
				FieldModel fieldAccessModel = FieldUtil.createModelsForField(methodAccessingField, fieldRead, -1, FieldUtil.getFieldAccessType(fieldRead), diagram, command, null);
				addedNode = fieldAccessModel.getPartner();
				return;
			}
		}
	}
}
