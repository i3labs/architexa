package com.architexa.diagrams.draw2d;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.ui.FontCache;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.FreeformLayout;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;



public class NonEmptyFigureSupport {

	public static void listenToModel(final ArtifactFragment af, final NonEmptyFigure nonEmptyFigure) {
		// initialize the compartments edit part
		if (nonEmptyFigure == null) return;
		if (af.getShownChildrenCnt() == 0)
           	nonEmptyFigure.figureRemoved();
        else 
        	nonEmptyFigure.figureAdded();
        	
		af.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent arg0) {
				  if (nonEmptyFigure != null) {
	                nonEmptyFigure.figureAdded();
	                if (af.getShownChildrenCnt() == 0 ) {
	                   	nonEmptyFigure.figureRemoved();
	                }
				  }
			}
		});
	}
	
	
	public static void rootListenToModel(final ArtifactFragment af, final NonEmptyFigure nonEmptyFigure, final IFigure parentLayer) {
		// initialize the compartments edit part
		if (nonEmptyFigure == null) return;
		if (af.getShownChildrenCnt() == 0)
           	nonEmptyFigure.figureRemoved();
        else 
        	nonEmptyFigure.figureAdded();
        	
		af.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent arg0) {
				  if (nonEmptyFigure != null) {
	                if (af.getShownChildrenCnt() == 0 ) {
	                	parentLayer.add(nonEmptyFigure);
	                } else if (parentLayer.getChildren().contains(nonEmptyFigure)) {
	                	parentLayer.remove(nonEmptyFigure);
                		parentLayer.setLayoutManager(new FreeformLayout());
	                }
				  }
			}
		});
	}

	public static void instructionHighlight(IFigure _emptyContent, int fontMag) {
		_emptyContent.setForegroundColor(ColorConstants.darkGray);
		_emptyContent.setFont(FontCache.font10Bold);
	}
}
