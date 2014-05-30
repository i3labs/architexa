package com.architexa.diagrams.chrono.commands;

import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.commands.AddNodeCommand;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.RootArtifact;

public class MemberCreateCommand extends AddNodeCommand {
	public static String NONE = "noAnimate";
	public static String FROM_INSTANCE = "fromInstance";

	public String animateType = NONE;

	private MemberModel newElement;
	private NodeModel parent;

	ArtifactFragment accessPartner;
	ArtifactFragment accessPartnerParent;

	public MemberCreateCommand(MemberModel newElement, NodeModel parent, String animateType) {
		super(getRootArt(parent), parent, newElement);
		this.newElement = newElement;
		this.parent = parent;
		this.animateType = animateType;
		setLabel("adding member");
	}

	public void setAccessPartner(ArtifactFragment accessPartner, ArtifactFragment accessPartnerParent) {
		this.accessPartner = accessPartner;
		this.accessPartnerParent = accessPartnerParent;
	}

	@Override
	public boolean canExecute() {
		return newElement != null && parent != null;
	}

	@Override
	public void execute() {
		addMember();
	}

	@Override
	public void undo() {
		if (newElement.getType() == MemberModel.access)
			newElement.removeFromConditionalBlocks();
		parent.removeChild(newElement);
	}

	@Override
	public void redo() {
		if (newElement.getType() == MemberModel.access)
			newElement.addToConditionalBlocks();
		addMember();
	}

	private void addMember() {
		int index = -1;
		if(newElement.isAccess()) index = MethodUtil.getInvocationIndex(newElement.getCharStart(), newElement.getCharEnd(), (MethodBoxModel)parent);
		else index = accessPartner==null||accessPartnerParent==null?-1:MemberUtil.getDeclarationIndex(parent, accessPartner, accessPartnerParent);
		parent.addChild(newElement, index);
	}

	public MemberModel getChild() {
		return newElement;
	}

	public String getAnimateType() {
		return animateType;
	}

	private static RootArtifact getRootArt(NodeModel art) {
		ArtifactFragment parentArt = art.getParentArt();
		while(parentArt!=null) {
			if(parentArt instanceof RootArtifact) return (RootArtifact)parentArt;
			parentArt = parentArt.getParentArt();
		}
		return null;
	}
}
