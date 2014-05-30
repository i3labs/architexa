package com.architexa.diagrams.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;


import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.EditPart;

/**
 * @author Abhishek
 *
 */
public class RootEditPart extends RSEEditPart implements IRSERootEditPart {

	@Override
	protected IFigure createFigure() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void createEditPolicies() {
		// TODO Auto-generated method stub
	}

	public List<EditPart> commentChildren= new ArrayList<EditPart>();
	public List<EditPart> getCommentEPChildren(){
		return commentChildren;
	}	

	public void setCommentEPChildren(List<EditPart> children) {
		commentChildren = children;
	}
	
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propName = evt.getPropertyName();
		if(RootArtifact.PROPERTY_COMMENT_CHILDREN.equals(propName))
			refresh();
	}

	@Override
	public void refresh(){
		super.refresh();
		RootEditPartUtils.refresh(getModel(), this, this);
	}

	/**
	 * Creates edit parts for comment models which do not have one
	 * And removes any edit parts whose comments do not exist any more
	 * @param commentChildrenList: List of Comment Model children
	 */
	public void refreshCommentChildren(List<Comment> commentChildrenList){
		RootEditPartUtils.refreshCommentChildren(commentChildrenList, this, this);
	}


	/**
	 * Helper method to set the position of a comment figure
	 * @param com: Comment model to be positioned
	 * @param loc: Point where the comment has to be positioned
	 */
	public void setCommentLocation(Comment com, Point loc) {
		RootEditPartUtils.setCommentLocation(com, loc, this);
	}
}
