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
 * Created on Aug 12, 2005
 */
package com.architexa.diagrams.relo.jdt.browse;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ui.IStartup;

import com.architexa.diagrams.jdt.ui.JDTTracker;
import com.architexa.diagrams.relo.jdt.ReloJDTPlugin;


/**
 * TODO {P0} We don't work new window action:
 *      Shouldn't be too hard to do, will need to add a listener to that effect
 *      
 * @author vineet
 */
public class LiveJDTTracker extends JDTTracker implements IStartup {
    static final Logger logger = ReloJDTPlugin.getLogger(LiveJDTTracker.class);

    private static LiveJDTTracker liveTracker = null;
    private static LiveJDTTracker getLiveInstance() {
        if (liveTracker == null) {
            liveTracker = new LiveJDTTracker();
            liveTracker.addListeners();
            logger.info("LiveTracking initialized.");
        }
        return liveTracker;
    }

	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
        init();
    }
    public static void init() {
        // make sure that we don't double initialize
    	clean();
        getLiveInstance();
    }
    public static void clean() {
    	if (liveTracker == null) return;
		getLiveInstance().removeListeners();
    }
    @SuppressWarnings("unchecked")
    public static List getNavigationPaths() {
        return new ArrayList(getLiveInstance().navigationPaths);
    }
    
    // this is just a method for debugging (hence suppressing unused)
    @SuppressWarnings("unused")
    private String dump(Object obj) {
        if (obj instanceof IJavaElement)
            return ((IJavaElement)obj).getElementName();
        else
            return obj.toString();
    }


}
