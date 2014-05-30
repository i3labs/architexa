package com.architexa.diagrams.chrono.figures;



import com.architexa.diagrams.chrono.util.LayoutUtil;
import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.geometry.Insets;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public abstract class MemberFigure extends FigureWithGap {

	MemberFigure partner;

	public MemberFigure() {
		GapFigure gapFig = new GapFigure("");
		gapFig.setSize(2, MEMBER_GAP);
		if(SeqUtil.debugHighlightingOn) {
			gapFig.setBackgroundColor(ColorConstants.darkGreen);
			gapFig.setOpaque(true);
		}
		setGap(gapFig);
	}

	public abstract int getType();
	public abstract MemberFigure getPartner();

	public void setPartner(MemberFigure partner) {
		this.partner = partner;
		if(partner!=null && partner.getPartner()==null)
			partner.setPartner(this);
	}

	public void resetGapToDefault() {
		getGap().setSize(getGap().getSize().width, METHOD_BOX_GAP);
	}

	@Override
	public void setBounds(Rectangle rect) {
		boolean boundsSame = rect.equals(getBounds());
		super.setBounds(rect);
		// If bounds have actually changed, framework will have fired figureMoved
		// event for us. Don't want to fire extra figureMoved events here in that 
		// case, so only firing a figureMoved event in order to trigger alignment 
		// if the bounds have not changed.
		if(boundsSame) fireFigureMoved();
	}

	@Override
	public void fireFigureMoved() {
		super.fireFigureMoved();
	}

	public static void alignTop(MemberFigure access, MemberFigure declaration) {

		int accessTop = access.getBounds().getCopy().getTop().y;
		int declarationTop = declaration.getBounds().getCopy().getTop().y;

		int differenceBetweenTops = accessTop - declarationTop;
		if(differenceBetweenTops > 0) {
			GapFigure gap = declaration.getGap();
			if(gap==null) return;

			if(SeqUtil.debugHighlightingOn)	gap.setBackgroundColor(ColorConstants.red);
			gap.setSize(5, gap.getSize().height+differenceBetweenTops);
			LayoutUtil.refresh(gap);
		}
		if(differenceBetweenTops < 0) {
			GapFigure gap = declaration.getGap();
			if(gap==null) return;

			int differenceAbs = Math.abs(differenceBetweenTops);
			if(gap.getSize().height >= differenceAbs) {
				if(SeqUtil.debugHighlightingOn) gap.setBackgroundColor(ColorConstants.blue);
				gap.setSize(10, gap.getSize().height-differenceAbs);
				LayoutUtil.refresh(gap);
			} else {
				// TODO
				// Can only be aligned if we lower the invocation. Currently,
				// doing that causes problems, so at least just setting the 
				// declaration's gap back to the default size to eliminate a big 
				// gap that resulted from many alignment attempts to align the
				// un-alignable partners
				declaration.resetGapToDefault();
				//gap.setBackgroundColor(ColorConstants.blue);
				//if(sourceBox.getGap()==null) return;
				//sourceBox.getGap().setSize(15, sourceBox.getGap().getSize().height+differenceAbs);
			}
		}
	}

	public static void alignBottom(MemberFigure access, MemberFigure declaration) {

		if(!(access instanceof MethodBoxFigure && declaration instanceof MethodBoxFigure)) return;

		int accessTop = access.getBounds().getCopy().getTop().y;
		int declarationTop = declaration.getBounds().getCopy().getTop().y;
		if(accessTop!=declarationTop) return; // tops are aligned first, so wait until their alignment done 

		MethodBoxFigure invocationFig = (MethodBoxFigure) access;

		int invocationBottom = invocationFig.getBounds().getCopy().getBottom().y;
		int declarationBottom = declaration.getBounds().getCopy().getBottom().y;

		int differenceBetweenBottoms = invocationBottom - declarationBottom;
		if(differenceBetweenBottoms != 0) {
			GapFigure innerSizerGap = invocationFig.getInnerSizerGap();
			if(innerSizerGap!=null) {
				IFigure container = invocationFig.getContainer();
				Insets containerInsets = container.getBorder().getInsets(container);
				Insets sourceBorderInsets = invocationFig.getBorder()==null ? new Insets(0) : invocationFig.getBorder().getInsets(invocationFig);
				int height = declaration.getBounds().height - containerInsets.top - containerInsets.bottom - sourceBorderInsets.top - sourceBorderInsets.bottom;
				if(innerSizerGap.getSize().height==height) return; // inner sizer already correct size

				if(SeqUtil.debugHighlightingOn) innerSizerGap.setBackgroundColor(ColorConstants.red);
				innerSizerGap.setSize(3, height);
				LayoutUtil.refresh(innerSizerGap);
			}
		}
	}

}
