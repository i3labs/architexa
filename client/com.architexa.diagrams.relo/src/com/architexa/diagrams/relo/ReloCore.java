package com.architexa.diagrams.relo;

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;

public class ReloCore implements IStartup {
    static final Logger logger = ReloPlugin.getLogger(ReloCore.class);

	public void earlyStartup() {
		try {
			earlyStartupInternal();
		} catch (Throwable t) {
			logger.error("Unexpected Error", t);
		}
	}
	private void earlyStartupInternal() {
//        PluggableTypes.registerType(new PluggableTypeInfo(RSECore.commentType, "Comment", Comment.class, CommentEditPart.class));
	}

}
