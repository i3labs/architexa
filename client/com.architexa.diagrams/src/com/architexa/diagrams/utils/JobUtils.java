package com.architexa.diagrams.utils;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.architexa.diagrams.Activator;

public class JobUtils {
	private static final Logger logger = Activator.getLogger(JobUtils.class);

	/**
	 * Method runs the runnable immediately if there was no job or if the job
	 * has already run successfully otherwise it adds the runnable to run after
	 * the job finishes.
	 */
	public static void performOnCompletion(Job job, final Runnable runnable) {
		if (job == null || job.getResult() == Status.OK_STATUS) {
			try {
				runnable.run();
			} catch (Throwable t) {
				logger.error("Unexpected Error", t);
			}
			return;
		}
		
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				try {
					runnable.run();
				} catch (Throwable t) {
					logger.error("Unexpected Error", t);
				}
			}
		});
	}

}
