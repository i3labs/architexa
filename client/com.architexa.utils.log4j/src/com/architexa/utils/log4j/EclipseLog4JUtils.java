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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.DefaultRepositorySelector;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RepositorySelector;
import org.apache.log4j.spi.RootLogger;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * @author vineet
 *
 */
public class EclipseLog4JUtils {

    public static final String PLUGIN_ID = "com.architexa.utils.log4j";

    private static Map pluginIdToRepositorySelectors = new HashMap();
    
    protected static void init(String pluginId) {
        Hierarchy h = new Hierarchy(new RootLogger(Level.DEBUG));
        RepositorySelector repositorySelector = new DefaultRepositorySelector(h);
        pluginIdToRepositorySelectors.put(pluginId, repositorySelector);

        LoggerRepository loggerRepo = repositorySelector.getLoggerRepository();

        // basic logging
        //configure(loggerRepo, pluginId, "log4j.properties");

        // release logging
        //configure(loggerRepo, pluginId, "log4jRel.properties");
        
        // combined
        boolean success = configure(loggerRepo, pluginId, "log4j.properties");
        if (!success) 
        	success = configure(loggerRepo, pluginId, "log4jRel.properties");
        if (!success)
        	configureForConsole(loggerRepo);
    }
    
	private static boolean configure(LoggerRepository loggerRepo, String pluginId, String propertyFile) {
    	Bundle bundle = Platform.getBundle(pluginId);
    	if (bundle == null) return false;
        URL url = bundle.getEntry(propertyFile);
        if (url == null) return false;

        try {
            InputStream propertiesInputStream = url.openStream();
            Properties props = new Properties();
            props.load(propertiesInputStream);
            propertiesInputStream.close();
            
            if ("true".equals(Platform.getDebugOption(PLUGIN_ID + "/outputToConsole"))
                    || "true".equals(Platform.getDebugOption(pluginId + "/outputToConsole"))) {
                // change EclipseSystemAppender to EclipseConsoleAppender
            	Iterator entryIt = props.entrySet().iterator();
            	while (entryIt.hasNext()) {
            		Entry entry = (Entry) entryIt.next();
                    if ("com.architexa.utils.log4j.EclipseSystemAppender".equals(entry.getValue())
                        || "com.architexa.utils.log4j.EclipsePlatformAppender".equals(entry.getValue())) {
                        entry.setValue("com.architexa.utils.log4j.EclipseConsoleAppender");
                    }
                }
            }
            
            new PropertyConfigurator().doConfigure(props, loggerRepo);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void configureForConsole(LoggerRepository loggerRepo) {
        Properties props = new Properties();
        props.setProperty("log4j.rootLogger","ALL, A1");
        props.setProperty("log4j.appender.A1","org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.A1.layout","org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.A1.layout.ConversionPattern","%6r %-5p %C.%M(%F:%L) %x - %m%n");
        new PropertyConfigurator().doConfigure(props, loggerRepo);
	}
    
    protected static RepositorySelector getRepositorySelector(String pluginId) {
        RepositorySelector repositorySelector = (RepositorySelector) pluginIdToRepositorySelectors.get(pluginId);
        if (repositorySelector == null) {
            init(pluginId);
            repositorySelector = (RepositorySelector) pluginIdToRepositorySelectors.get(pluginId);
        }
        //System.err.println("Returned: " + pluginClazz);
        return repositorySelector;
    }

    public static Logger getLogger(String pluginId, String name) {
        Logger logger = getRepositorySelector(pluginId).getLoggerRepository().getLogger(name);
        Enumeration allAppenders = logger.getAllAppenders();
        while (allAppenders.hasMoreElements()) {
			Object appender = (Object) allAppenders.nextElement();
			if (appender instanceof EclipsePlatformAppender)
				((EclipsePlatformAppender)appender).setBundle(pluginId);
		}
        return logger;
    }
    public static Logger getLogger(String pluginId, Class clazz) {
        return getLogger(pluginId, clazz.getName());
    }
}
