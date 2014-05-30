package com.architexa.rse.prod.loader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

// based on com.architexa.docmaps.PygmentsShell
public class ToolRunner {
	
	private static final boolean debugging = true; 
	
	public interface ShellStream {
		int getWriteCnt();
		void writeLine(String l);
		void close();
	};
	public static class ShellStreamBase implements ShellStream {
		int writeCnt = 0;
		public int getWriteCnt() {
			return writeCnt;
		}
		public void writeLine(String l) {
			writeCnt += l.length();
		}
		public void close() {}
	}
	
	private final ShellStream output;

	private boolean shellFinished = false;
	private Process shellProcess;
	
	private Thread sndThread;
	private Thread errThread;
	private Thread rcvThread;

	private Thread mainThread;
	
	public ToolRunner(ShellStream output) {
		this.output = output;
	}

	public void run(List<String> cmd, final String cmdInput) throws IOException {
		run(cmd.toArray(new String[] {}), cmdInput);
	}
	public void run(String[] cmd, final String cmdInput) throws IOException {
		shellProcess = Runtime.getRuntime().exec(cmd);
		
		// have threads for the io streams - so that we don't deadlock
		
		sndThread = new Thread("ShellRunner sndThread") {
		    @Override
			public void run() {
		    	try {
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream()));
					out.write(cmdInput);
					out.close();
		    	} catch (IOException t) {
		    		// ignore messages coming out when we are cleaning up
		    		if (!cleanedUp) t.printStackTrace();
		    	} catch (Throwable t) {
		    		t.printStackTrace();
		    	}
		    	if (debugging) System.err.print("s");
		    }
		};

		errThread = new Thread("ShellRunner errThread") {
		    @Override
			public void run() {
		    	try {
		    		BufferedReader bre = new BufferedReader(new InputStreamReader(shellProcess.getErrorStream()));
		    		String line;
					while ((line = bre.readLine()) != null) {
						output.writeLine("ERR: " + line);
						System.err.println(line);
					}
					bre.close();
		    	} catch (Throwable t) {
		    		t.printStackTrace();
		    	}
		    	if (debugging) System.err.print("e");
		    }};
		
		rcvThread = new Thread("ShellRunner rcvThread") {
		    @Override
			public void run() {
		    	try {
		    		BufferedReader bri = new BufferedReader(new InputStreamReader(shellProcess.getInputStream()));
		    		String line;
					while ((line = bri.readLine()) != null) {
						output.writeLine(line);
						//System.out.println(line);
					}
					bri.close();
		    	} catch (Throwable t) {
		    		t.printStackTrace();
		    	}
		    	if (debugging) System.err.print("r");
		    }
		};
		
		// so that we know when tool finishes
		new Thread("ShellRunner monThread") {
		    @Override
			public void run() {
		    	try {
					shellProcess.waitFor();
					shellFinished = true;
					cleanup(false);
				} catch (Throwable e) {
					e.printStackTrace();
				}
		    }
		}.start();

		sndThread.start();
		errThread.start();
		rcvThread.start();
		
		// wait for 5s then force cleanup - tools sometimes hangs otherwise
		// note: the 5s limit causes failures in complex/large files - for e.g.
		// we have a 5% failure rate when importing mrdoob/three.js
		// TODO: understand and improve failure rate
		if (!shellFinished) {
			try {
				mainThread = Thread.currentThread();
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
		}
		cleanup(true);
    	if (debugging) System.err.println("ShellRunner processed and cleaned up!");
	}
	
	private boolean cleanedUp = false;

	private static int succ = 0;
	private static int fail = 0;
	
	private void waitForProcessThreads() {
		try {
			sndThread.join();
			errThread.join();
			rcvThread.join();
		} catch (InterruptedException e) {}
	}
	
	/**
	 * synchronized because it will be accessed close to each other from the
	 * main thread and the monitoring thread
	 */
	private synchronized void cleanup(boolean fromMainThread) {
		if (cleanedUp) {
			// the second call in here - we don't really want to do anything -
			// so we basically just return after all threads have wrapped up
			waitForProcessThreads();
			return;
		}
		cleanedUp = true;
		
		// is the main thread sleeping - wake it up (we check that we are not fromMainThread
		// - because otherwise the interrupted flag gets set again and the next timeout ends immediately
		if (mainThread != null && !fromMainThread)
			mainThread.interrupt();

		if (debugging) {
			if (!shellFinished) {
				System.err.print("*** Killing Tool.");
				fail++;
			} else {
				System.err.print("*** Tool Finished Itself.");
				succ++;
			}
		}

		// if pygments has not finished - it might be stuck - kill it
		shellProcess.destroy();

		// threads should die automatically - once the process is destroyed 

		if (debugging) {
			System.err.println(" ToolRunner [s:" + succ + ",f:" + fail + "] wrote: " + output.getWriteCnt() + sndThread.isAlive() + errThread.isAlive() + rcvThread.isAlive());
			if (sndThread.isAlive()) {
				// this should happen very rarely - it will happen with strange source
				System.err.println(" *** Source Data was not written!");
			}
		}
		waitForProcessThreads();
		output.close();
	}

}