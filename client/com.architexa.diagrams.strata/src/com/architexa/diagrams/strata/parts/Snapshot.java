package com.architexa.diagrams.strata.parts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;

/**
 * Needed because GEF doesn't really support orphaning from the model - we
 * could implement it, but instead we are just capturing the position in the
 * animation snapshot
 * 
 * @author vineet
 */
public final class Snapshot {
	
	public static class FigSnapshot {
		Rectangle figBounds;
		List<FigSnapshot> childrenSnapshots = new ArrayList<FigSnapshot>(5);
		
		public FigSnapshot(IFigure figure) {
			this.figBounds = figure.getBounds().getCopy();
			for (Object figChild : figure.getChildren()) {
				childrenSnapshots.add(new FigSnapshot((IFigure)figChild));
			}
		}
		public FigSnapshot(AbstractGraphicalEditPart ep) {
			this(ep.getFigure());
		}

		public static void update(FigSnapshot figSnapshot, IFigure fig) {
			if (figSnapshot == null) return;
			fig.setBounds(figSnapshot.figBounds);
			List<?> figChildren = fig.getChildren();
			for (int i = 0; i < figSnapshot.childrenSnapshots.size(); i++) {
				if (i>=figChildren.size()) break;
				update(figSnapshot.childrenSnapshots.get(i), (IFigure) figChildren.get(i));
			}
		}
		
	}
	
	private Map<Object, FigSnapshot> modelFigSnapshotMap = new HashMap<Object, FigSnapshot>(50);
	
	public void captureRoot(AbstractGraphicalEditPart rootEditPart) {
		captureHierarchy(rootEditPart);
	}

	public void clear() {
		modelFigSnapshotMap.clear();
	}

	private void captureHierarchy(AbstractGraphicalEditPart editPart) {
		modelFigSnapshotMap.put(editPart.getModel(), new FigSnapshot(editPart));
		for (Object childEP : editPart.getChildren()) {
			captureHierarchy((AbstractGraphicalEditPart) childEP);
		}
	}
	
	public void updateFig(AbstractGraphicalEditPart editPart, IFigure fig) {
		FigSnapshot.update(modelFigSnapshotMap.get(editPart.getModel()), fig);
	}
}