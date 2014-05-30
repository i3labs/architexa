package com.architexa.diagrams.chrono.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.architexa.diagrams.model.ArtifactFragment;

public class GroupedInstanceModel extends UserCreatedInstanceModel{

	List<InstanceModel> instanceModelChildren= new ArrayList<InstanceModel>();
	List<ArtifactFragment> allInstances = new ArrayList<ArtifactFragment>();
	Map<InstanceModel,Integer> instanceToIndexMap = new HashMap< InstanceModel,Integer>();
	Map<MemberModel,InstanceModel> childToInstanceMap= new HashMap<MemberModel, InstanceModel>();
	
	public GroupedInstanceModel(DiagramModel diagram , List<InstanceModel> instanceChildren) {
		super("Grouped Instances", diagram);
		this.instanceModelChildren.addAll(instanceChildren);
	}

	public void addToAllInstances(ArtifactFragment artFrag) {
		allInstances.add(artFrag);
	}
	
	public List<ArtifactFragment> getAllInstances() {
		return allInstances;
	}
	
	public void addInstanceModelChildrenToGroup(InstanceModel instance, int index) {
		instanceToIndexMap.put(instance, index);
		if(instanceModelChildren.contains(instance)) return;
		instanceModelChildren.add(instance);
	}

	public void addAllInstanceChildrenToGroup(Map<InstanceModel,Integer> instanceChildrenMap) {
		Map<InstanceModel,Integer> toPut = new HashMap<InstanceModel, Integer>(instanceChildrenMap);
		instanceToIndexMap.putAll(toPut);
	}
	
	public void addToChildInstanceMap(MemberModel child, InstanceModel parent) {
		childToInstanceMap.put(child, parent);
	}

	public void addAllToChildInstanceMap(Map<MemberModel,InstanceModel> childInstanceMap) {
		Map<MemberModel,InstanceModel> toPut = new HashMap<MemberModel,InstanceModel>(childInstanceMap);
		childToInstanceMap.putAll(toPut);
	}
	
	public List<InstanceModel> getInstanceChildren() {
		return instanceModelChildren;
	}

	public Map<InstanceModel, Integer> getInstanceToIndexMap() {
		return instanceToIndexMap;
	}
	public Map<MemberModel, InstanceModel> getChildToInstanceMap() {
		return childToInstanceMap;
	}

	@Override
	public String toString() {
		String str = super.toString();
		String instance = "Grouped Instance";
		return instance + " : " + str;
	}
}
