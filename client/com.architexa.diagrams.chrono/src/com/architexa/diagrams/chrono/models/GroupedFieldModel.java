package com.architexa.diagrams.chrono.models;


import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.openrdf.model.Resource;

import com.architexa.diagrams.chrono.sequence.FieldRead;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;

public class GroupedFieldModel extends MemberModel{

	private IField field;
	private ASTNode astNode;
	private FieldModel fieldModel;
	
	public GroupedFieldModel(GroupedInstanceModel groupedInstance, IField field, ASTNode astNode, FieldModel fieldModel) {
		super();
		this.instance = groupedInstance;
		this.field = field;
		this.astNode = astNode;
		this.fieldModel = fieldModel;
		setUserCreated(true);
	}

	public FieldModel getFieldModel(){
		return fieldModel;
	}
	@Override
	public void addSourceConnection(ArtifactRel conn) {
		super.addSourceConnection(conn);
		if(conn instanceof ConnectionModel) setOutgoingConnection((ConnectionModel)conn);
	}

	@Override
	public void addTargetConnection(ArtifactRel conn) {
		super.addTargetConnection(conn);
		if(conn instanceof ConnectionModel)	setIncomingConnection((ConnectionModel)conn);
	}

	@Override
	public void removeSourceConnection(ArtifactRel conn) {
		super.removeSourceConnection(conn);
		if(conn instanceof ConnectionModel)	setOutgoingConnection(null);
	}

	@Override
	public void removeTargetConnection(ArtifactRel conn) {
		super.removeTargetConnection(conn);
		if(conn instanceof ConnectionModel)	setIncomingConnection(null);
	}

	@Override
	public IField getMember() {
		return field;
	}

	@Override
	public boolean isAccess() {
		ASTNode astNode = getASTNode();
		if(astNode==null) return false;

		return (astNode instanceof FieldAccess ||
				astNode instanceof SuperFieldAccess ||
				astNode instanceof QualifiedName);
	}

	@Override
	public boolean isDeclaration() {
		return (getASTNode() instanceof FieldDeclaration);
	}

	@Override
	public ASTNode getASTNode() {
		return astNode;
	}

	public String getFieldName() {
		if(field!=null) return field.getElementName();
		return "";
	}

	public Resource getFieldAccess() {
		if(node==null && field==null) return null;

		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		if(node==null) node = new Artifact(RJCore.jdtElementToResource(repo, field));
		return (Resource) repo.getStatement(node.elementRes, RJCore.access, null).getObject();
	}

	public String getFigureToolTip() {
		if(isAccess()) return FieldRead.getStringRepresentationOfFieldRead(getASTNode());
		return MemberUtil.getFullName(getMember());
	}

	@Override
	public String getName() {
		return "";
	}

}
