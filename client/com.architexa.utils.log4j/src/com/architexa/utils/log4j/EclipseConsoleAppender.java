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
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * @author vineet
 *
 */
public class EclipseConsoleAppender extends ConsoleAppender {
    
    protected Writer errWriter;
    protected Writer warWriter;
    protected Writer nfoWriter;
    protected Writer dbgWriter;

    protected IOConsole console = null;
    protected Writer currWriter = null;
    

    /**
     * Does a initialization of the console (and streams). If this is called by
     * the constructor the initialization happens at construction, otherwise
     * initialization is done lazily
     */
    protected void init() {
        console = findAndShowConsole("Runtime Console");
        
        IOConsoleOutputStream outputStream;
        
        outputStream = console.newOutputStream();
        outputStream.setColor(new Color(null, 255, 0, 0));
        errWriter = new OutputStreamWriter(outputStream);
        
        outputStream = console.newOutputStream();
        outputStream.setColor(new Color(null, 128, 0, 0));
        warWriter = new OutputStreamWriter(outputStream);
        
        outputStream = console.newOutputStream();
        outputStream.setColor(new Color(null, 0, 0, 0));
        nfoWriter = new OutputStreamWriter(outputStream);
        
        outputStream = console.newOutputStream();
        outputStream.setColor(new Color(null, 128, 128, 128));
        dbgWriter = new OutputStreamWriter(outputStream);
    }

    /* (non-Javadoc)
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    public void append(LoggingEvent event) {
        if (console == null) init();
        
        Writer tgtWriter = null;
        if (event.getLevel() == Level.FATAL || event.getLevel() == Level.ERROR)
            tgtWriter = errWriter;
        else if (event.getLevel() == Level.WARN)
            tgtWriter = warWriter;
        else if (event.getLevel() == Level.INFO)
            tgtWriter = nfoWriter;
        else if (event.getLevel() == Level.DEBUG)
            tgtWriter = dbgWriter;
        else
            tgtWriter = nfoWriter;
        
        // only set if we need to change 
        if (currWriter != tgtWriter) {
            try { if (currWriter != null) currWriter.flush(); } catch (IOException e) {}
            currWriter = tgtWriter;
            setWriter(tgtWriter);
        }

        super.append(event);
    }

    private IOConsole findAndShowConsole(String name) {
        IConsoleManager conMan = ConsolePlugin.getDefault().getConsoleManager();
        IConsole[] existing = conMan.getConsoles();
        for (int i = 0; i < existing.length; i++)
           if (name.equals(existing[i].getName()))
              return (IOConsole) existing[i];

        //no console found, so create a new one
        IOConsole myConsole = new IOConsole(name, null, null, true);
        conMan.addConsoles(new IConsole[]{myConsole});

        try {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
            view.display(myConsole);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
        return myConsole;
     }
}
