package com.architexa.diagrams.eclipse.gef;

import com.architexa.org.eclipse.gef.tools.LabelSource;

public interface UndoableLabelSource extends LabelSource{
	public String getOldAnnoLabelText();
    public void setOldAnnoLabelText(String str);
}
