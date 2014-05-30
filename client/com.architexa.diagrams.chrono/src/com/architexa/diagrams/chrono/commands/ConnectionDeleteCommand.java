package com.architexa.diagrams.chrono.commands;



import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.org.eclipse.gef.commands.Command;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class ConnectionDeleteCommand extends Command {

	ConnectionModel connection;

	public ConnectionDeleteCommand(ConnectionModel conn) {
		if (conn == null) {
			throw new IllegalArgumentException();
		}
		setLabel("connection deletion");
		this.connection = conn;
	}

	@Override
	public void execute() {
		connection.disconnect();
	}

	@Override
	public void undo() {
		connection.reconnect();
	}

	@Override
	public void redo() {
		connection.disconnect();
	}

	public void setConnection(ConnectionModel connection) {
		this.connection = connection;
	}

	public ConnectionModel getConnection() {
		return connection;
	}

}
