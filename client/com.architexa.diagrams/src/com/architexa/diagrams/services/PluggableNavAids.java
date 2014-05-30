/**
 * 
 */
package com.architexa.diagrams.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.parts.NavAidsSpec;
import com.architexa.diagrams.parts.RelNavAidsSpec;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Dimension;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPart;

public class PluggableNavAids {
	public interface INavAidSpecSource {
		public NavAidsSpec getNavAids(List<NavAidsSpec> prevDecorations, EditPart hostEP);
	}
	
	private static Set<INavAidSpecSource> registeredSources = new HashSet<INavAidSpecSource>();
	
	public static void registerNavAidsSource(INavAidSpecSource nass) {
		if (!registeredSources.contains(nass)) {
			registeredSources.add(nass);
		}
	}
	public static Set<INavAidSpecSource> getRegisteredNavAidsSources() {
		return registeredSources;
	}

	// TODO simplify if possible (but we really want to apply 'relationRes'
	// first, i.e. we might be forced to keep as is.
	public static INavAidSpecSource getFWDSpec(final URI relationRes) {
		return new PluggableNavAids.INavAidSpecSource() {
			public NavAidsSpec getNavAids(final List<NavAidsSpec> prevDecorations, final EditPart hostEP) {
				return new RelNavAidsSpec(hostEP, DirectedRel.getFwd(relationRes)) {
		            @Override
		            public Point getHandlesPosition(IFigure containerFig) {
		                Point decPos = containerFig.getBounds().getTopRight();
		                decPos.x = Math.max(
				                        decPos.x, 
				                        firstNAS.decorationFig.getBounds().getTopRight().x);
		                for (NavAidsSpec naSpec : prevDecorations) {
		                	if (naSpec.decorationFig==null) continue;
		                	
		                	if (naSpec.decorationFig.containsPoint(decPos.getCopy().getTranslated(3, 3))) {
		                		decPos.y = decPos.y + naSpec.decorationFig.getBounds().height;	
		                	}
		                }
		                
		                return decPos; 
		            }
		        };
			}
		};
	}


	public static INavAidSpecSource getREVSpec(final URI relationRes) {
		return new PluggableNavAids.INavAidSpecSource() {
			public NavAidsSpec getNavAids(final List<NavAidsSpec> prevDecorations, final EditPart hostEP) {
				return new RelNavAidsSpec(hostEP, DirectedRel.getRev(relationRes)) {
		            @Override
		            public Point getHandlesPosition(IFigure containerFig) {
		                Dimension prefSize = decorationFig.getPreferredSize();
		                Rectangle bounds = containerFig.getBounds();
		                int x = bounds.x - prefSize.width;
		                int y = bounds.y;
		                return new Point(x, y);
		            }
		        };
			}
		};
	}
}