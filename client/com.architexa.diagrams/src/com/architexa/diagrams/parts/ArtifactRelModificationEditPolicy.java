package com.architexa.diagrams.parts;

import org.openrdf.model.URI;

import com.architexa.diagrams.commands.AddNamedRelCommand;
import com.architexa.diagrams.commands.AddRelCommand;
import com.architexa.diagrams.commands.ArtifactRelCreationCommand;
import com.architexa.diagrams.commands.HideRelCommand;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import com.architexa.org.eclipse.gef.requests.CreateConnectionRequest;
import com.architexa.org.eclipse.gef.requests.ReconnectRequest;


public class ArtifactRelModificationEditPolicy extends GraphicalNodeEditPolicy {

	  public static final String KEY = "Artifact Relation Modification Edit Policy";

	  	@Override
	    protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
	        Object link = request.getNewObject();
	        if (link instanceof ArtifactRel && !(getHost() instanceof IRSERootEditPart)) {
	        	Command cmd = new ArtifactRelCreationCommand((ArtifactRel) link, getHost());
	        	request.setStartCommand(cmd);
	        	return cmd;
	        }
	        return null;
	    }
	    
	    @Override
	    protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
	    	Object link = request.getNewObject();
	    	if (!(request.getStartCommand() instanceof ArtifactRelCreationCommand))
	    		return null;
	    	 ArtifactRelCreationCommand cmd = (ArtifactRelCreationCommand)request.getStartCommand();
	    	 if (link instanceof ArtifactRel && !(getHost() instanceof IRSERootEditPart)) {
	    		cmd.setTarget(getHost() );
	    		return cmd;
	         }
	         return null;
	    }

	    @Override
	    protected Command getReconnectSourceCommand(ReconnectRequest request) {
	    	if (!validConnectionPoint(request.getTarget())) return null;

	    	ArtifactFragment newSrcAF = (ArtifactFragment) request.getTarget().getModel();

	    	ArtifactRel relModel = (ArtifactRel) request.getConnectionEditPart().getModel();
	    	ArtifactFragment srcAF = relModel.getSrc();
	    	ArtifactFragment destAF = relModel.getDest();
	    	URI rel = relModel.relationRes;

	    	// Attempting to reconnect to same source
	    	if(srcAF.getArt().equals(newSrcAF.getArt())) return null;

	    	CompoundCommand cc = new CompoundCommand("Change connection's source");

	    	// delete the conn to the original source
	    	cc.add(new HideRelCommand(relModel)); 

	    	// and add a new conn to the new source
	    	if (relModel instanceof NamedRel)
	    		cc.add(new AddNamedRelCommand(newSrcAF, relModel, destAF));
	    	else
	    		cc.add(new AddRelCommand(newSrcAF, new DirectedRel(rel, true), destAF));


	    	return cc;
	    }

	    @Override
	    protected Command getReconnectTargetCommand(ReconnectRequest request) {
	    	if (!validConnectionPoint(request.getTarget())) return null;

	    	ArtifactFragment newDestAF = (ArtifactFragment) request.getTarget().getModel();

	    	ArtifactRel relModel = (ArtifactRel) request.getConnectionEditPart().getModel();
	    	ArtifactFragment srcAF = relModel.getSrc();
	    	ArtifactFragment destAF = relModel.getDest();
	    	URI rel = relModel.relationRes;

	    	// Attempting to reconnect to same target
	    	if (destAF.getArt().equals(newDestAF.getArt())) return null;

	    	CompoundCommand cc = new CompoundCommand("Change connection's target");

	    	// delete the conn to the original target
	    	cc.add(new HideRelCommand(relModel));

	    	// and add a new conn to the new target
	    	if (relModel instanceof NamedRel) 
	    		cc.add(new AddNamedRelCommand(srcAF, relModel, newDestAF));
	    	else
	    		cc.add(new AddRelCommand(srcAF, new DirectedRel(rel, true), newDestAF));

	    	return cc;
	    }

	    protected boolean validConnectionPoint(EditPart ep) {
	        return (!(ep instanceof IRSERootEditPart) || ep instanceof CommentEditPart);
	    }
	    
}
