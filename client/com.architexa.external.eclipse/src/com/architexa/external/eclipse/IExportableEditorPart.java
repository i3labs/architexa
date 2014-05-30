package com.architexa.external.eclipse;

import org.eclipse.ui.IEditorPart;

import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;

public interface IExportableEditorPart extends IEditorPart {

	Rectangle getDiagramsBoundsForExport();
	void returnToOrigLoc(Point oldLoc);
	Point getDiagramsOriginalHeight();
	void addRemoveImageExportCustomizations(boolean status);
}
