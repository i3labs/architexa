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
 * Created on Jul 31, 2005
 */
package com.architexa.diagrams.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.Activator;


/**
 * Similar to UIJob, but we also allow waiting for another job (family) as well -
 * this is important when you need to wait for another long task, but want to
 * inform the user that you are waiting. We don't inherit from UIJob because
 * they have marked run final and we would like to idealy change the method -
 * since we can get to the UI thread faster, we want to first wait for the job
 * family to be done first and then wait for the UI thread.
 * 
 * Note: we assume that there is atleast one task in the family, after that we
 * check that there are no more jobs of the family queue, and if not we run the
 * task - i.e. if the family expects multiple tasks then they should have the
 * last task respond to the particular family given here, or implement a more
 * complex strategy here.
 * 
 * @author vineet
 * 
 */
public abstract class WaitableUIJob extends Job {

    private Display cachedDisplay;
    public WaitableUIJob(String name) {
        super(name);
    }
    public WaitableUIJob(Display jobDisplay, String name) {
        super(name);
        cacheDisplay(jobDisplay);
    }
    
    private void cacheDisplay(Display cachedDisplay) {
        this.cachedDisplay = cachedDisplay;
    }
    private Display getDisplay() {
        if (cachedDisplay != null) return cachedDisplay;
        if (PlatformUI.isWorkbenchRunning())
            return PlatformUI.getWorkbench().getDisplay();
        return null;
    }

    private Object waitFamily = null;
	private boolean cancelled = false;
    public void waitForJobWithFamily(Object family) {
        this.waitFamily = family;
    }

    /**
     * This method is final so that overrides don't happen here by mistake.
     * Clients need to implement runInUIThread
     */
    @Override
    public final IStatus run(final IProgressMonitor monitor) {
        return run2(monitor);
    }

    public IStatus run2(final IProgressMonitor monitor) {
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;
        
        if (continueWaiting(null)) {
            IJobChangeListener jcl = new JobFamilyDoneListener(monitor);
            Job.getJobManager().addJobChangeListener(jcl);
        } else {
            getUIThreadAndRun(monitor);
        }

        return Job.ASYNC_FINISH;
    }

    private final class JobFamilyDoneListener extends JobChangeAdapter {
        private final IProgressMonitor monitor;
        private JobFamilyDoneListener(IProgressMonitor monitor) {
            this.monitor = monitor;
        }
        
        @Override
		public void running(IJobChangeEvent event) {
        	if (monitor.isCanceled())
        		cancelled  = true;
    	}
        
        @Override
        public void done(IJobChangeEvent event) {
            if (continueWaiting(event.getJob())) return;
            Job.getJobManager().removeJobChangeListener(this);

            getUIThreadAndRun(monitor);
        }
    }

    /**
     * By default we check only when a Job is done, if this method returns true
     * runInUIThread will be called
     * 
     * @param doneJob -
     *            job that has just been done
     * @return - false to continue waiting, true to run job
     */
    protected boolean continueWaiting(Job doneJob) {
        if (cancelled) return false;
    	if (waitFamily == null) return false;
        if (doneJob == null) return true;
        if (!doneJob.belongsTo(waitFamily)) return true;
        if (Job.getJobManager().find(waitFamily).length > 0) return true;
        return false;
    }

    /**
     * This is the method that needs to be called if other listeners are added
     * (this method should only be called if the Job is running), i.e. 
     * listeners should be added in the run2 method
     * 
     * @param monitor
     */
    protected void getUIThreadAndRun(final IProgressMonitor monitor) {
        Display display = getDisplay();
        
        if (display == null || display.isDisposed()) {
            WaitableUIJob.this.done(Status.CANCEL_STATUS);
            return;
        }
        
        display.asyncExec(new Runnable() {
            public void run() {
                IStatus result = null;
                try {
                    //As we are in the UI Thread we can
                    //always know what to tell the job.
                    setThread(Thread.currentThread());
                    if (monitor.isCanceled())
                        result = Status.CANCEL_STATUS;
                    else
                        result = runInUIThread(monitor);
                    
                } catch (Throwable t) {
                    result = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "Unexpected Error", t);
                } finally {
                    if (result == null)
                        result = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "Unexpected Error", null);
                    WaitableUIJob.this.done(result);
                }
            }});
    }

    /**
     * Run the job in the UI Thread.
     * 
     * @param monitor
     * @return IStatus
     */
    public abstract IStatus runInUIThread(IProgressMonitor monitor);
}
