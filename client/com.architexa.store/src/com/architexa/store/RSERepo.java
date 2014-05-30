package com.architexa.store;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Basically an interface used as a 'tag' on any classes that write to a file by
 * RSE.
 */
public class RSERepo {
	
	///////////////////////////
	// Location support
	///////////////////////////

	private IPath path;
	
	// for debug
	private static int cnt = 0;
	private int id = cnt++;

	public RSERepo() {
		path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		System.err.println("Creating repo " + this.getClass().getName() + ":" + id + " at location: " + path);
	}

	public RSERepo(IPath _path) {
		setPath(_path);
		System.err.println("[P]Creating repo " + this.getClass().getName() + ":" + id + " at location: " + path);
	}
	
	protected void setPath(IPath _path) {
		if (_path == null || _path == Path.EMPTY)
			path = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		else
			path = _path;
	}

	/**
	 * @return - the location of the workspace 
	 */
	protected IPath getPath() {
		return path;
	}
	
	/**
	 * @return - the location to store the repo
	 */
	protected IPath getLocation() {
    	return path.addTrailingSeparator().append(".metadata").
    			addTrailingSeparator().append(".plugins").addTrailingSeparator().
    			append(ReloStorePlugin.PLUGIN_ID).addTrailingSeparator();
	}

	public void setRepo(RSERepo srcRepo) {
		this.path = srcRepo.path;
	}

}
