package com.architexa.utils.log4j;

import org.apache.log4j.FileAppender;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

/**
 * Basically a FileAppender, but stores the log relative to the calling bundles
 * metadata directory
 * 
 * @author vineet
 */
public class MetadataFileAppender extends FileAppender {

    protected String bundleName = null;
    protected String baseFileName = null;

    public String getBundle() {
        return bundleName;
    }

    public void setBundle(String bundle) {
        bundleName = bundle.trim();
    }

    public void setFile(String file) {
    	baseFileName = file.trim();
	}
    public String getFile() {
    	return baseFileName;
	}
    
    public void activateOptions() {
    	if (baseFileName != null) {
        	IPath newPath = Platform
        						.getStateLocation(Platform.getBundle(bundleName))
        						.addTrailingSeparator()
        						.append(baseFileName);
        	super.setFile(newPath.toString());
    	}
    	super.activateOptions();
    }
    

}
