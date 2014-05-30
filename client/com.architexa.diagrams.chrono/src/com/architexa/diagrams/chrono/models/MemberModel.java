package com.architexa.diagrams.chrono.models;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.dom.ASTNode;
import org.openrdf.model.Resource;

import com.architexa.diagrams.chrono.controlflow.ControlFlowBlock;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public abstract class MemberModel extends NodeModel {

	private ASTNode astNode;
	InstanceModel instance;

	ConnectionModel outgoing;
	ConnectionModel incoming;

	protected int type = declaration;
	public static int access = 0;
	public static int declaration = 1;

	private int lineNum = -1;
	private int charStart = -1;
	private int charEnd = -1;

	public MemberModel() {
		super();
	}

	public ASTNode getASTNode() {
		if (astNode == null) astNode = RJCore.getCorrespondingASTNode(getMember());
		return astNode;
	}
	
	public void setASTNode(ASTNode astNode) {
		this.astNode = astNode;
	}
	
	public MemberModel(Resource res) {
		super(res);
	}

	public abstract IMember getMember();
	public abstract String getName();

	public int getType() {
		return type;
	}

	public boolean isAccess() {
		return access==type;
	}

	public boolean isDeclaration() {
		return declaration==type;
	}
	
	public void setInstanceModel(InstanceModel instance) {
		this.instance = instance;
	}

	public InstanceModel getInstanceModel() {
		return instance;
	}

	public void setOutgoingConnection(ConnectionModel outgoing) {
		this.outgoing = outgoing;
	}

	public ConnectionModel getOutgoingConnection() {
		return outgoing;
	}

	public void setIncomingConnection(ConnectionModel incoming) {
		this.incoming = incoming;
	}

	public ConnectionModel getIncomingConnection() {
		return incoming;
	}

	public void setLineNum(int line) {
		lineNum = line;
	}

	public int getLineNum() {
		return lineNum;
	}

	public void setCharStart(int start) {
		charStart = start;
	}

	public int getCharStart() {
		return charStart;
	}

	public void setCharEnd(int end) {
		charEnd = end;
	}

	public int getCharEnd() {
		return charEnd;
	}

	public NodeModel getParent() {
		return (NodeModel) super.getParentArt();
	}

	//TODO: Replace uses of getOutgoingConnection() and getIncomingConnection() 
	//in order to find partner with use of this method
	public MemberModel getPartner() {
		if(getOutgoingConnection()!=null && getOutgoingConnection().getTarget() instanceof MemberModel) 
			return (MemberModel)getOutgoingConnection().getTarget();
		if(getIncomingConnection()!=null && getIncomingConnection().getSource() instanceof MemberModel) 
			return (MemberModel)getIncomingConnection().getSource();
		return null;
	}

//	public MemberModel getGroupedPartner() {
//		if(getOutgoingConnection()!=null && getOutgoingConnection().getTarget() instanceof MemberModel)
//			return (MemberModel)getOutgoingConnection().getTarget();
//		if(getIncomingConnection()!=null && getIncomingConnection().getSource() instanceof MemberModel)
//			return (MemberModel)getIncomingConnection().getSource();
//		return null;
//	}
	/**
	 * Removes only this member
	 * 
	 */
	public void deleteBasic() {
		if(getParent()!=null) {
			for(ArtifactFragment child:this.getChildren()){
				if(child instanceof HiddenNodeModel){
					HiddenNodeModel partner=((HiddenNodeModel) child).getPartner();
					if(partner!=null)
						((InstanceModel)partner.getParentArt()).removeChild(partner);
				}
			}
			getParent().removeChild(this);
		}
	}

	public void removeFromConditionalBlocks() {
		for (ControlFlowBlock block : getConditionalBlocksContainedIn()) {
			block.statementDeleted(this);
		}
	}

	public void addToConditionalBlocks() {
		for (ControlFlowBlock block : getConditionalBlocksContainedIn()) {
			block.statementAdded(this);
		}
	}
}
