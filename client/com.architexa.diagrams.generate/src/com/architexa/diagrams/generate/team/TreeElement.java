package com.architexa.diagrams.generate.team;

import java.util.ArrayList;
import java.util.List;

public class TreeElement {
	private TreeElement parentElement;
	private String name;
	private List<TreeElement> children;
	private ChangeSet changeSetParent;
	private Object logEntry;

	public TreeElement() {
	}

	public TreeElement(String name, TreeElement parentElement,
			ChangeSet changeSetParent, Object logEntry) {
		this.name = name;
		this.parentElement = parentElement;
		this.setChangeSetParent(changeSetParent);
		this.setLogEntry(logEntry);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public java.util.List<TreeElement> getChildren() {
		if (children == null)
			children = new ArrayList<TreeElement>();
		return children;
	}

	public void setChildren(java.util.List<TreeElement> children) {
		this.children = children;
	}

	public TreeElement getParentElement() {
		return parentElement;
	}

	public void setParentElement(TreeElement parentElement) {
		this.parentElement = parentElement;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (parentElement == null) {
			sb.append("Revision ");
		}
		sb.append(name);
		// if(children!=null){
		// sb.append("ÑÑchildren:"+children.size());
		// }
		return sb.toString();
	}

	public void addChild(TreeElement... e) {
		if (children == null) {
			children = new ArrayList();
		}
		for (int i = 0; i < e.length; i++) {
			children.add(e[i]);
		}
	}

	public void setChangeSetParent(ChangeSet changeSetParent) {
		this.changeSetParent = changeSetParent;
	}

	public ChangeSet getChangeSetParent() {
		return changeSetParent;
	}

	public void setLogEntry(Object logEntry) {
		this.logEntry = logEntry;
	}

	public Object getLogEntry() {
		return logEntry;
	}
}
