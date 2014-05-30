package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.gef.commands.Command;

public class UserMethodBoxCreateCommand extends Command{

	public static String NONE = "noAnimate";
	public static String FROM_INSTANCE = "fromInstance";

	public String animateType = NONE;

	private MethodBoxModel newElement;
	private ArtifactFragment parent;
	private int index = -1;

	ArtifactFragment accessPartner;
	ArtifactFragment accessPartnerParent;

	public UserMethodBoxCreateCommand(MethodBoxModel newElement, ArtifactFragment parent, String animateType) {
		this.newElement = newElement;
		this.parent = parent;
		this.animateType = animateType;
		setLabel("method creation");
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
		addGroupedMethod();
	}

	private void addGroupedMethod(){
		index = accessPartner==null || accessPartnerParent == null ? -1 : MemberUtil.getDeclarationIndex(parent, accessPartner, accessPartnerParent);
		parent.appendShownChild(newElement ,index);
	}

	@Override
	public void undo() {
		parent.removeShownChild(newElement);
	}

	@Override
	public void redo() {
		addGroupedMethod();
	}

	public String getAnimateType() {
		return animateType;
	}

}
