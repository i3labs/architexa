/**
 * 
 */
package com.architexa.diagrams.parts;

import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.editpolicies.OrderedLayoutEditPolicy;
import com.architexa.org.eclipse.gef.requests.CreateRequest;

/**
 * @author Abhishek Rakshit
 * Layout method for Comments, currently not used
 * Will need this if uncomment the call 
 * // installEditPolicy(EditPolicy.LAYOUT_ROLE, new ComOrderedLayoutEditPolicy());
 * in CommentEditPart.createEditPolicies()
 */
public class CommentOrderedLayoutEditPolicy extends OrderedLayoutEditPolicy{
	public final static String COM_EDIT_POLICY = "ComOrderedLayoutEditPolicy";

	@Override
	protected Command createAddCommand(EditPart child, EditPart after) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Command createMoveChildCommand(EditPart child, EditPart after) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected EditPart getInsertionReference(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
