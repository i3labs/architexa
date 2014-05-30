package com.architexa.rse.prod.loader;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.openrdf.model.Resource;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.diagrams.services.PluggableTypes.PluggableFilterInfo;
import com.architexa.rse.prod.Activator;
import com.architexa.rse.prod.loader.ToolRunner.ShellStream;
import com.architexa.rse.prod.loader.ToolRunner.ShellStreamBase;
import com.architexa.store.ReloRdfRepository;

public class DepMgr {
	
	private static class ShellStreamWriter extends ShellStreamBase {
		private final Writer o;
		public ShellStreamWriter(Writer o) {
			this.o = o;
		}
		@Override
		public void writeLine(String l) {
			super.writeLine(l);
			try {
				o.write(l);
				o.write("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		@Override
		public void close() {
			try {
				o.flush();
				//o.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	public interface DepReader {
		void readTo(DepWriter writer);
	};

	public interface DepWriter {
		public void init();
		public ShellStream getWriter();
		public void close();
	};
	
	public static class PrintDepWriter implements DepWriter {
		public void init() {}
		public ShellStream getWriter() {
			return new ShellStreamWriter(new PrintWriter(System.out));
		}
		public void close() {}
	}
	
	protected DepReader reader = null;
	protected DepWriter writer = null;
	
	public static void main(String[] args) {
		new DepMgr(new UndLoader.UndReader(),new PrintDepWriter()).run();
	}
	
	public DepMgr(DepReader reader, DepWriter writer) {
		this.reader = reader;
		this.writer = writer;
	}

	protected void run() {
		writer.init();
		reader.readTo(writer);
		writer.close();
	}
	
    public static void hookActions() {
        PluggableTypes
    		.registerType(new PluggableFilterInfo(RJCore.classType, "DB Table", null, null, DepTranslator.tableProp, RSECore.trueStatement, new PluggableTypes.ImageDescriptorProvider() {
    	    	public ImageDescriptor getImageDescriptor(Artifact art, Resource typeRes, ReloRdfRepository repo) { 
    	    		return Activator.getImageDescriptor("/icons/datasheet.gif");
    	    	}
    		}))
    		.isGraphNode = true;
    }

}
