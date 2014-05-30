package com.architexa.diagrams.chrono.models;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.dom.ASTNode;
import org.openrdf.model.Resource;

import com.architexa.diagrams.chrono.sequence.FieldRead;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class FieldModel extends MemberModel {

	private IField field;

	public FieldModel(InstanceModel instance, IField field, ASTNode astNode, int type) {
		super(RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), field));
		this.instance = instance;
		this.field = field;
		this.type = type;
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
	public String getName() {
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
	public FieldModel getPartner() {
		MemberModel partner = super.getPartner();
		if(partner==null || !(partner instanceof FieldModel)) return null;
		return (FieldModel) partner;
	}

	/**
	 * Removes this field and its partner
	 */
	@Override
	public void deleteBasic() {
		FieldModel partner = getPartner();
		if(getIncomingConnection()!=null) getIncomingConnection().disconnect();
		if(getOutgoingConnection()!=null) getOutgoingConnection().disconnect();
		super.deleteBasic();
		if(partner!=null) partner.deleteBasic();
	}
}