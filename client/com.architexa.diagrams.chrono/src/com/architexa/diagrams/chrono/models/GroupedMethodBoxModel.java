package com.architexa.diagrams.chrono.models;

import java.util.ArrayList;
import java.util.List;

public class GroupedMethodBoxModel extends UserCreatedMethodBoxModel{

	
	List<MethodBoxModel> methodChildrenList = new ArrayList<MethodBoxModel>();

	public GroupedMethodBoxModel(InstanceModel groupedInstance, int type) {
		super(type, groupedInstance);
	}
	
	public void addMethodToList(MethodBoxModel model){
		if(!methodChildrenList.contains(model))
			methodChildrenList.add(model);
	}

	public void addMethodToList(List<MethodBoxModel> methodList){
		if(methodList!=null){
			List<MethodBoxModel> list = new ArrayList<MethodBoxModel>(methodList);
			methodChildrenList.addAll(list);
		}
	}
	public List<MethodBoxModel> getSavedMethodChildrenList(){
		return methodChildrenList;
	}

}
