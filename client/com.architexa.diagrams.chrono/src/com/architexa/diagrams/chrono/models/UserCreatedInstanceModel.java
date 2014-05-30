package com.architexa.diagrams.chrono.models;

import com.architexa.diagrams.jdt.model.UserCreatedFragment;


public class UserCreatedInstanceModel extends InstanceModel{

	DiagramModel diagram;
	
	public UserCreatedInstanceModel(DiagramModel diagram) {
		this("New Instance", diagram);
	}
	
	public UserCreatedInstanceModel(String name, DiagramModel diagram) {
		setInstanceName(name);
		setClassName("");
		setUserCreated(true);
		UserCreatedFragment.createResource(this, diagram.getBrowseModel(), name);
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		String instance = "User Created Instance";
		return instance + " : " + str;
	}

}
