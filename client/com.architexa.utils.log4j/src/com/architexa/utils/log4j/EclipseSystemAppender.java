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
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author vineet
 *
 */
public class EclipseSystemAppender extends ConsoleAppender {
    
    protected Writer errWriter = new OutputStreamWriter(System.err);
    protected Writer outWriter = new OutputStreamWriter(System.out);

    protected Writer currWriter = null;

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    public void append(LoggingEvent event) {
        Writer tgtWriter = null;
        if (event.getLevel() == Level.FATAL || event.getLevel() == Level.ERROR)
            tgtWriter = errWriter;
        else
            tgtWriter = outWriter;
        
        // only set if we need to change 
        if (currWriter != tgtWriter) {
            try { if (currWriter != null) currWriter.flush(); } catch (IOException e) {}
            currWriter = tgtWriter;
            setWriter(tgtWriter);
        }

        super.append(event);
    }

}
