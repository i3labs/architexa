/**
 * 
 */
package com.architexa.org.eclipse.gef.tools;

import com.architexa.org.eclipse.draw2d.IFigure;

public interface LabelSource {
    String getAnnoLabelText();
    void setAnnoLabelText(String str);
    IFigure getAnnoLabelFigure();
}