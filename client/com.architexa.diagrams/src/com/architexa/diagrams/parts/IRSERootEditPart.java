package com.architexa.diagrams.parts;

import java.util.List;

import com.architexa.org.eclipse.gef.EditPart;

public interface IRSERootEditPart {

	List<EditPart> getCommentEPChildren();
	void setCommentEPChildren(List<EditPart> children);

}
