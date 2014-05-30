package com.architexa.diagrams.chrono.models;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.URI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.figures.ConnectionFigure;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.org.eclipse.draw2d.ConnectionAnchor;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class ConnectionModel extends ArtifactRel {

	public static String PROPERTY_LOCATION = "connectionLocation";	
	public static String PROPERTY_SET_VISIBLE = "setVisible";
	public static String PROPERTY_REFRESH = "refresh";

	public static URI CALL = RJCore.calls;
	public static URI RETURN = RJCore.returns;
	public static URI OVERRIDES = RJCore.overrides;
	public static URI FIELD_READ = RSECore.createRseUri("jdt#fieldRead");
	public static URI FIELD_WRITE = RSECore.createRseUri("jdt#fieldWrite");

	private ConnectionFigure line;
	private ArtifactFragment source;
	private ArtifactFragment target;
	private ConnectionAnchor anchor;

	private String label = "";
	private Map<String, Boolean> argLabelToIsExpandableMap = new LinkedHashMap<String, Boolean>();
	private boolean isConnected;

	public ConnectionModel(ArtifactFragment source, ArtifactFragment target, String label, LinkedHashMap<String, Boolean> argLabelToIsExpandableMap, URI type) {
		super(source, target, type);
		this.label = label;
		this.argLabelToIsExpandableMap = argLabelToIsExpandableMap;
		if (source == null || target == null || source.equals(target)) {
			return;
		}
		disconnect();
		this.source = source;
		this.target = target;
		reconnect();
	}

	public void reconnect(NodeModel newSource, NodeModel newTarget) {
		if (newSource == null || newTarget == null || newSource.equals(newTarget)) {
			return;
		}
		disconnect();
		this.source = newSource;
		this.target = newTarget;
		reconnect();
	}

	public void reconnect() {
		if(isConnected) return;
		source.addSourceConnection(this);
		target.addTargetConnection(this);
		isConnected = true;
	}

	public void disconnect() {
		if(!isConnected) return;
		source.removeSourceConnection(this);
		target.removeTargetConnection(this);
		isConnected = false;
	}

	public void setSource(NodeModel newSource) {
		source = newSource;
	}

	public ArtifactFragment getSource() {
		return source;
	}

	public void setTarget(NodeModel target) {
		this.target = target;
	}

	public ArtifactFragment getTarget() {
		return target;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public Map<String, Boolean> getArgLabelToIsExpandableMap() {
		return argLabelToIsExpandableMap;
	}

	public void setLine(ConnectionFigure line) {
		this.line = line;
	}

	public ConnectionFigure getLine() {
		return line;
	}

	public void setAnchor(ConnectionAnchor anchor) {
		this.anchor = anchor;
	}

	public ConnectionAnchor getAnchor() {
		return anchor;
	}

	public void setVisible(boolean visible) {
		firePropertyChange(PROPERTY_SET_VISIBLE, null, visible);
	}

	@Override
	public String toString() {
		return super.toString() + ": " + label;
	}

}
