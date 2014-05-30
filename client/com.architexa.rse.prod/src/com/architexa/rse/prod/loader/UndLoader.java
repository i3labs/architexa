package com.architexa.rse.prod.loader;


import org.eclipse.jface.action.Action;

import com.architexa.inspector.InspectorView;
import com.architexa.inspector.ModelUtils;
import com.architexa.rse.prod.Activator;
import com.architexa.rse.prod.loader.DepMgr.DepReader;
import com.architexa.rse.prod.loader.DepMgr.DepWriter;
import com.architexa.rse.prod.loader.ToolRunner.ShellStream;
import com.architexa.store.ReloRdfRepository;

public class UndLoader {

	public static class UndReader implements DepReader {
		private String dbPath = "C:\\Dev\\runtime-empty-space\\SmallApp.udb";
		protected String pathToUnd = "C:\\Program Files\\SciTools\\bin\\pc-win64";
		protected String pathToPerl = pathToUnd + "\\uperl.exe";
		protected String plScriptPath = LoaderUtils.getScriptPath("Und2Atxa.pl");

		public void readTo(DepWriter writer) {
			// pathToPerl u2a.pl -db dbPath
			String[] scriptCmd = new String[] {pathToPerl, plScriptPath, "-db", dbPath};
			LoaderUtils.runTool(scriptCmd, "", writer.getWriter());
		}
	}

	/**
	 * Adapts DepTranslator - it manages the repository as well as minor settings needed based on the reader
	 */
	public static class UndWriter implements DepWriter {
		protected String commonIDStart = "C.Dev.runtime-empty-space.SmallApp.src.";
		protected String projName = "SmallApp";
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
		InspectorView.addViewAction(
				new Action("Load from Understand DB" , Activator.getImageDescriptor("icons/load-db.gif")) {
					@Override
					public void run() {
						new DepMgr(new UndReader(), new UndWriter()).run();
					}
				}
			);
    }
}
