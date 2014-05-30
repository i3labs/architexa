package com.architexa.diagrams.chrono.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.chrono.util.SeqRelUtils;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.org.eclipse.gef.commands.Command;

public class MemberDeleteCommand extends Command {
	private static final Logger logger = SeqPlugin.getLogger(MemberDeleteCommand.class);
	NodeModel parent;
	MemberModel child;

	ArtifactFragment accessPartner;
	ArtifactFragment accessPartnerParent;
	int index = -1;
	boolean removeFromControlBlock = false;
	private List<ArtifactRel> removedRels = new ArrayList<ArtifactRel>();

	public MemberDeleteCommand(NodeModel parent, MemberModel child,	boolean removeFromControlBlock) {
		if (parent == null || child == null)
			throw new IllegalArgumentException();
		this.parent = parent;
		this.child = child;
		this.removeFromControlBlock = removeFromControlBlock;
		if (child instanceof MemberModel) {
			if (child.getPartner() != null) {
				this.accessPartner = child.getPartner();
				this.accessPartnerParent = child.getPartner().getParentArt();
			} 
		}
	}

	// called when method declaration gets deleted due to deletion of its
	// corresponding grouped member
	// and the index is saved so that undo put it back at the right place as it
	// doesn't have any partners to find the right index.
	// also called when deleting a field model
	public MemberDeleteCommand(NodeModel parent, MemberModel child, int index) {
		this(parent, child, true);
		this.index = index;
	}

	public void setAccessPartner(ArtifactFragment accessPartner,
			ArtifactFragment accessPartnerParent) {
		this.accessPartner = accessPartner;
		this.accessPartnerParent = accessPartnerParent;
	}

	@Override
	public boolean canExecute() {
		return parent != null && child != null;
	}

	@Override
	public void execute() {
		removeMember();
	}

	private void removeMember() {
		if (parent.getChildren().contains(child)) {
			if (parent.removeChild(child))
				removedRels = SeqRelUtils.removeChildAnnotatedRels(child);
			// remove from control flow blocks
			if (child.getType() == MemberModel.access && removeFromControlBlock)
				child.removeFromConditionalBlocks();
		} else
			logger.error("Duplicate delete command has been created. No child to be removed:"
							+ "\nParent: " + parent + "\tChild" + child);
	}

	@Override
	public void undo() {
		addMember();
	}

	private void addMember() {
		if (index == -1 || parent.getChildren().size() < index) { // if saved index is larger than total children
			if (child.isAccess())
				index = MethodUtil.getInvocationIndex(child.getCharStart(),
						child.getCharEnd(), (MethodBoxModel) parent);
			else
				index = accessPartner == null || accessPartnerParent == null ? -1
						: MemberUtil.getDeclarationIndex(parent, accessPartner,
								accessPartnerParent);
		}
		if (child.getType() == MemberModel.access)
			child.addToConditionalBlocks();

		if (parent.addChild(child, index))
			SeqRelUtils.addRels(removedRels);
	}
}
