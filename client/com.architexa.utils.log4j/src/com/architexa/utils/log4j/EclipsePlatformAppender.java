/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
/*
 * Created on Feb 4, 2005
 *
 */
package com.architexa.utils.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * @author vineet
 *
 */
public class EclipsePlatformAppender extends AppenderSkeleton {

    protected String bundleName = null;
    private ILog platformLogger = null;
    
    public static boolean logging = true;

    /**
     * 
     */
    public EclipsePlatformAppender() {
        super();
    }

    public String getBundle() {
        return bundleName;
    }

    public void setBundle(String bundle) {
        bundleName = bundle.trim();
        
        // see if we need to reactivate
        if (platformLogger != null) activateOptions();
    }

    public void activateOptions() {
        if (bundleName != null) {
            platformLogger = Platform.getLog(Platform.getBundle(bundleName));
        } else {
            LogLog.warn("Bundle option not set for appender [" + name + "].");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    protected void append(LoggingEvent event) {
        if (platformLogger == null) return;
        
        int severity = IStatus.OK;

        if (event.getLevel() == Level.FATAL)
            severity = IStatus.ERROR;
        else if (event.getLevel() == Level.ERROR)
            severity = IStatus.ERROR;
        else if (event.getLevel() == Level.WARN)
            severity = IStatus.WARNING;
        else if (event.getLevel() == Level.INFO)
            severity = IStatus.INFO;
        else if (event.getLevel() == Level.DEBUG)
            severity = IStatus.OK;
        else
            severity = IStatus.OK;
        
        Throwable exception = null;
        if (event.getThrowableInformation() != null)
            exception = event.getThrowableInformation().getThrowable();
        
        if (logging)
        	platformLogger.log(new Status(severity, bundleName, 0, event.getMessage().toString(), exception));

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.log4j.Appender#requiresLayout()
     */
    public boolean requiresLayout() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.Appender#close()
     */
    public void close() {
    }

}
