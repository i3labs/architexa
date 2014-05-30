package com.architexa.rse.prod.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.architexa.rse.prod.loader.ToolRunner.ShellStream;

public class LoaderUtils {
	/**
	 * We want to let scripts included with the loader source to be called by
	 * external programs. We really need to just get the included files path -
	 * but that might cause problems - so we just copy it into a new file and
	 * return its path
	 */
	public static String getScriptPath(String scriptName) {
		InputStream is = UndLoader.class.getResourceAsStream(scriptName);
		try {
			File streamFile = File.createTempFile("atxa-script", ".pl");
			OutputStream os = new FileOutputStream(streamFile);

			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = is.read(bytes)) != -1) {
				os.write(bytes, 0, read);
			}
			is.close();
			os.flush();
			os.close();
			
			return streamFile.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void runTool(String[] cmd, String code, ShellStream output) {
		try {
			System.err.print("To execute: ");
			for (String c : cmd) {
				System.err.print(c);
			}
			new ToolRunner(output).run(cmd, code);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
