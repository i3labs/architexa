package com.architexa.diagrams.chrono.models;


public class UserCreatedMethodBoxModel extends MethodBoxModel{


	private String methodName = "New Method";
	
	public UserCreatedMethodBoxModel() {
		super();
	}
	
	public UserCreatedMethodBoxModel(String name) {
		super();
		setMethodName(name);
	}

	public UserCreatedMethodBoxModel(String name, InstanceModel userInstance) {
		super(null, userInstance);
		setMethodName(name);
	}
	
	public UserCreatedMethodBoxModel(int type, InstanceModel userInstance) {
		super(null, userInstance);
		this.type = type;
	}

	@Override
	public String getMethodName() {
		return methodName;
	}
	
	@Override
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

}
