package com.architexa.diagrams.chrono.commands;


import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;

import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.gef.commands.Command;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class ConnectionCreateCommand extends Command {

	private static final Logger logger = SeqPlugin.getLogger(ConnectionCreateCommand.class);
	private ConnectionModel connection = null;

	private ArtifactFragment source;
	private ArtifactFragment target;
	private String message;
	private URI type;

	private LinkedHashMap<String, Boolean> argLabelToIsExpandableMap = new LinkedHashMap<String, Boolean>();
	private boolean visible = true;

	public ConnectionCreateCommand(ArtifactFragment source, ArtifactFragment target, String message, URI type) {
		if(source == null || target == null){
			logger.error("Trying to create connection with NULL Source/Target:\nSource: "+ source != null? source:"null" +"\nTarget: "+ target!=null? target:"null");
			throw new IllegalArgumentException();
		}
		setLabel("connection creation");
		this.source = source;
		this.target = target;
		this.message = message;
		this.type = type;
	}

	public ConnectionCreateCommand(NodeModel source, NodeModel target, String message, URI type, boolean visible) {
		this(source, target, message, type);
		this.visible = visible;
	}

	public ConnectionCreateCommand(NodeModel source, NodeModel target, String message, URI type, LinkedHashMap<String, Boolean> argLabelToIsExpandableMap) {
		this(source, target, message, type);
		this.argLabelToIsExpandableMap = new LinkedHashMap<String, Boolean>(argLabelToIsExpandableMap);
	}

	public ConnectionCreateCommand(ArtifactFragment source, ArtifactFragment target, String message, URI type, LinkedHashMap<String, Boolean> argLabelToIsExpandableMap) {
		this(source, target, message, type);
		this.argLabelToIsExpandableMap = new LinkedHashMap<String, Boolean>(argLabelToIsExpandableMap);
	}

	@Override
	public void execute() {
		connection = new ConnectionModel(source, target, message, argLabelToIsExpandableMap, type);
		if(connection == null) logger.error("Could not create connection model.\nSource: "+ source != null? source:"null" +"\nTarget: "+ target!=null? target:"null");
		connection.setVisible(visible);
	}

	public void setTarget(NodeModel target) {
		this.target = target;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public ConnectionModel getConnection() {
		return connection;
	}

	@Override
	public void undo() {
		connection.disconnect();
	}

	@Override
	public void redo() {
		connection.reconnect();
		connection.setVisible(visible);
	}
}
