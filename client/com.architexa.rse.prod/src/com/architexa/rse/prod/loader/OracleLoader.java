package com.architexa.rse.prod.loader;


import org.eclipse.jface.action.Action;

import com.architexa.inspector.InspectorView;
import com.architexa.inspector.ModelUtils;
import com.architexa.rse.prod.Activator;
import com.architexa.rse.prod.loader.DepMgr.DepReader;
import com.architexa.rse.prod.loader.DepMgr.DepWriter;
import com.architexa.rse.prod.loader.DepMgr.PrintDepWriter;
import com.architexa.rse.prod.loader.ToolRunner.ShellStream;
import com.architexa.store.ReloRdfRepository;

public class OracleLoader {

	/**
	 * Adapts OracleReader
	 */
	public static class OracleReaderEx implements DepReader {

		public void readTo(DepWriter writer) {
			try {
				OracleReader or = new OracleReader(writer.getWriter());
				or.initDB("jdbc:oracle:thin:@localhost:1521:ORCL", "hr", "hrPassword");
				or.readFromDB();
				or.cleanup();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Adapts DepTranslator - it manages the repository as well as minor settings needed based on the reader
	 */
	public static class OracleWriter implements DepWriter {
		protected String commonIDStart = "";
		protected String projName = "OracleConn";
		private ReloRdfRepository repo = ModelUtils.getRepo();
		public void init() {
			repo.startTransaction();
		}
		public ShellStream getWriter() {
			return new DepTranslator(repo, projName, commonIDStart);
		}
		public void close() {
			repo.commitTransaction();
		}
	}
	
    public static void hookActions() {
    	DepMgr.hookActions();
		InspectorView.addViewAction(
				new Action("Load from Oracle DB" , Activator.getImageDescriptor("icons/load-db.gif")) {
					@Override
					public void run() {
						new DepMgr(new OracleReaderEx(), new OracleWriter()).run();
						System.err.println("Done Loading!");
					}
				}
			);
	}

    public static void main(String[] args) {
		new DepMgr(new OracleReaderEx(), new PrintDepWriter()).run();
		System.err.println("Done Reading!");
		System.out.flush();
	}



}
