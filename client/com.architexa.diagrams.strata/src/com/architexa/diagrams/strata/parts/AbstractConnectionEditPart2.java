package com.architexa.diagrams.strata.parts;

import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPolicy;
import com.architexa.org.eclipse.gef.editparts.AbstractConnectionEditPart;

/**
 * Provides support for notification when source and target are set in the edit
 * policy, this is needed when the model moves to a new parent and the edit part
 * changes.
 * 
 * @author vineet
 */
public abstract class AbstractConnectionEditPart2 extends AbstractConnectionEditPart {
	
	public interface SrcTgtNotificationEditPolicy {
		void setSource(EditPart ep);		
		void setTarget(EditPart ep);		
	};

	@Override
	protected abstract void createEditPolicies();


	@Override
	public void setSource(EditPart editPart) {
		if (this.getSource() == editPart) return;
		super.setSource(editPart);

		EditPolicyIterator policyIt = getEditPolicyIterator();
		while (policyIt.hasNext()) {
			EditPolicy ep = policyIt.next();
			if (ep instanceof SrcTgtNotificationEditPolicy)
				((SrcTgtNotificationEditPolicy)ep).setSource(editPart);
		}
	}


	@Override
	public void setTarget(EditPart editPart) {
		if (this.getTarget() == editPart) return;
		super.setTarget(editPart);

		EditPolicyIterator policyIt = getEditPolicyIterator();
		while (policyIt.hasNext()) {
			EditPolicy ep = policyIt.next();
			if (ep instanceof SrcTgtNotificationEditPolicy)
				((SrcTgtNotificationEditPolicy)ep).setTarget(editPart);
		}
	}
}
