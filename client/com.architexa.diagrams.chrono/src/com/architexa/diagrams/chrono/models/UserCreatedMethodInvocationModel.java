package com.architexa.diagrams.chrono.models;


public class UserCreatedMethodInvocationModel extends MethodInvocationModel{

	MethodInvocationModel model;

	public void setMethodInvocationModel(MethodInvocationModel model){
		this.model = model;
	}

	public MethodInvocationModel getMethodInvocationModel(){
		return model;
	}
	
	public UserCreatedMethodInvocationModel(InstanceModel instance, int type) {
		super(instance, type);
		setUserCreated(true);
	}
	
}
