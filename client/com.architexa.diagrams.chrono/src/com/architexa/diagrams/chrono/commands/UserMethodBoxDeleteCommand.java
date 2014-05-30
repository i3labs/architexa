package com.architexa.diagrams.chrono.commands;


import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.models.UserCreatedMethodInvocationModel;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.gef.commands.Command;

public class UserMethodBoxDeleteCommand extends Command{
	NodeModel parent;
	MethodBoxModel child;

	ArtifactFragment accessPartner;
	ArtifactFragment accessPartnerParent;

	int index = -1;
	public UserMethodBoxDeleteCommand(NodeModel parent, MethodBoxModel child){
		if(parent == null || child == null)
			throw new IllegalArgumentException();
		this.parent = parent;
		this.child = child;

		if(child.getPartner()!=null){
			this.accessPartner = child.getPartner();
			this.accessPartnerParent = child.getPartner().getParentArt();
		}
	}

	public void setAccessPartner(ArtifactFragment accessPartner, ArtifactFragment accessPartnerParent) {
		this.accessPartner = accessPartner;
		this.accessPartnerParent = accessPartnerParent;
	}

	@Override
	public boolean canExecute(){
		return parent!=null && child!=null;
	}

	@Override
	public void execute(){
		removeMethod();
	}

	private void removeMethod(){
		if(parent.getChildren().contains(child)){
			// save the index so that the invocation can be put back at the correct index when undo is called.
			index = parent.getChildren().indexOf(child); 
			parent.removeChild(child);
		}
	}

	@Override
	public void undo(){
		addMethod();
	}

	private void addMethod() {
		if(!(child instanceof UserCreatedMethodInvocationModel)){ // index for invocation is saved already
			index = accessPartner==null||accessPartnerParent==null?-1:MemberUtil.getDeclarationIndex(parent, accessPartner, accessPartnerParent);
		}
		parent.addChild(child, index);
	}

	@Override
	public void redo(){
		removeMethod();
	}
}
