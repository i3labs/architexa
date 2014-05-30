package com.architexa.diagrams.chrono.editpolicies;


import java.util.ArrayList;


import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.util.ConnectionUtil;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import com.architexa.org.eclipse.gef.requests.GroupRequest;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqConnectionEditPolicy extends ConnectionEditPolicy {

	@Override
	protected Command getDeleteCommand(GroupRequest request) {
		ConnectionModel link = (ConnectionModel)getHost().getModel();
		CompoundCommand cmd = new CompoundCommand("Delete Connection");
		ConnectionUtil.deleteConnection(link, new ArrayList<EditPart>(request.getEditParts()), cmd);
		
		// check if command list is empty
		if(!cmd.getCommands().isEmpty())
			return cmd;
		else
			return null;
	}
}
