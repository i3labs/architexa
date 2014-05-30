package com.architexa.diagrams.chrono.editparts;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import com.architexa.diagrams.chrono.animation.Animator;
import com.architexa.diagrams.chrono.figures.MemberFigure;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.jdt.IJavaElementContainer;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.utils.JDTUISupport;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.draw2d.FigureListener;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.gef.ConnectionEditPart;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.Request;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public abstract class MemberEditPart extends SeqNodeEditPart implements FigureListener, IJavaElementContainer {
	private static final Logger logger = SeqPlugin.getLogger(MemberEditPart.class);

	private IJavaElement cachedIJE = null; 
	public IJavaElement getJaveElement() {
		if (cachedIJE == null && getModel() instanceof MemberModel)
			cachedIJE = ((MemberModel)getModel()).getMember();
		return cachedIJE;
	}

	public Artifact getContainedArtifact() {
		return ((MemberModel)getModel()).getArt();
	}

	public MemberEditPart getPartnerEP() {
		ArtifactFragment partnerModel = null;
		if (getModel() instanceof MemberModel) {
			MemberModel model = (MemberModel) getModel();
			if (model.getPartner() != null)
				partnerModel = model.getPartner();
			else return null;
		}

		if (!getSourceConnections().isEmpty()) {
			for (Object conn : getSourceConnections()) {
				if (!(conn instanceof ConnectionEditPart)) continue;
				EditPart target = ((ConnectionEditPart)conn).getTarget();
				if (target!=null && partnerModel.equals(target.getModel())) 
					return (MemberEditPart) target;
			}
		}
		if (!getTargetConnections().isEmpty()) {
			for (Object conn : getTargetConnections()) {
				if (!(conn instanceof ConnectionEditPart)) continue;
				EditPart source = ((ConnectionEditPart)conn).getSource();
				if (source!=null && partnerModel.equals(source.getModel())) 
					return (MemberEditPart) source;
			}
		}
		return null;
	}

	@Override
	public void performRequest(Request req) { 
		if(REQ_OPEN.equals(req.getType())) {
			// Open a member in a java editor when it is double clicked
			// (No longer displaying all calls when double click a method
			// since it can be slow and result in the UI freezing, so doing
			// the same thing relo does when double click, at least for now)
			openMemberInJavaEditor();
		}
	}

	@Override
	public IAction getOpenInJavaEditorAction(String actionName, ImageDescriptor image) {

		IAction action = new Action(actionName, image) {
			@Override
			public void run() {
				openMemberInJavaEditor();
			}
		};
		return action;
	}

	protected void openMemberInJavaEditor() {
		if(!(getModel() instanceof MemberModel)) return;
		MemberModel model = (MemberModel)getModel();
		IMember member = model.getMember();
		IJavaElement containerClass = RJCore.resourceToJDTElement(StoreUtil.getDefaultStoreRepository(), model.getInstanceModel().getResource());

		try {

			if((containerClass==null) || (model.getCharStart()==-1 && model.getCharEnd()==-1 && model.getLineNum()==-1)) {
				IJavaElement elementToOpen = JDTUISupport.getElementToOpen(member);
				IEditorPart editor = JavaUI.openInEditor(elementToOpen);
				JavaUI.revealInEditor(editor, elementToOpen);
				return;
			}

			// Someone has set a line number or char range to display that is not
			// the member declaration (e.g. the member access)
			IEditorPart editor = JavaUI.openInEditor(containerClass);
			IMarker marker = ((IFile)containerClass.getResource()).createMarker(IMarker.TEXT);
			if(editor==null || marker==null) return;

			if(model.getLineNum()!=-1) {
				marker.setAttribute(IMarker.LINE_NUMBER, model.getLineNum());
			} else if(model.getCharStart()!=-1 && model.getCharEnd()!=-1) {
				marker.setAttribute(IMarker.CHAR_START, model.getCharStart());
				marker.setAttribute(IMarker.CHAR_END, model.getCharEnd());
			}
			IDE.gotoMarker(editor, marker);

		} catch (PartInitException e) {
			logger.error("Unexpected Exception.", e);
		} catch (CoreException e) {
			logger.error("Unexpected Exception.", e);
		}
	}

	@Override
	public void figureMoved(IFigure source) {
		super.figureMoved(source);
		updateBounds();
	}

	protected void updateBounds() {

		if(Animator.PLAYBACK) return;

		// Check whether attempts to align have been made more
		// than the allowed number of times, and if so return
		// since that probably means we're in an infinite loop
		int count = !DiagramEditPart.alignmentAttempts.containsKey(this) ? 0 : DiagramEditPart.alignmentAttempts.get(this)+1;
		if(count > DiagramEditPart.alignmentInfiniteLoopEscape) return;
		DiagramEditPart.alignmentAttempts.put(this, count);

		ArtifactFragment thisModel = null;
		Boolean isAccess = false;
		if(getModel() instanceof MemberModel){
			thisModel = (MemberModel) getModel();
			isAccess = ((MemberModel) thisModel).isAccess();
		}
//		else if(getModel() instanceof UserCreatedMemberModel){
//			thisModel = (UserCreatedMemberModel) getModel();
//			isAccess = ((UserCreatedMemberModel) thisModel).isAccess();
//		}

		if(isAccess && getPartnerEP()!=null) {
			MemberEditPart declarationEP = getPartnerEP();
			declarationEP.updateBounds();
			return;
		}

		if(getPartnerEP()==null) {
			// No partner to align with, so reset gap to the default size
			// (a previous alignment could have resulted in a gap that is 
			// larger than necessary anymore)
			((MemberFigure)getFigure()).resetGapToDefault();
			return;
		}

		MemberEditPart accessEP = getPartnerEP();

		ArtifactFragment accessInstance = null;
		if(accessEP.getModel() instanceof MemberModel) {
			accessInstance = ((MemberModel)accessEP.getModel()).getInstanceModel();
		}

		ArtifactFragment thisInstance = null;
		if(thisModel instanceof MemberModel) {
			thisInstance = ((MemberModel)thisModel).getInstanceModel();
		}

		if(accessInstance.equals(thisInstance)) {
			// Call to same class, no alignment needed
			return;
		}

		MemberFigure accessFig = (MemberFigure) accessEP.getFigure();
		MemberFigure declarationFig = (MemberFigure) getFigure();

		int accessTop = accessFig.getBounds().getCopy().getTop().y;
		int declarationTop = declarationFig.getBounds().getCopy().getTop().y;

		int accessBottom = accessFig.getBounds().getCopy().getBottom().y;
		int declarationBottom = declarationFig.getBounds().getCopy().getBottom().y;

		if(accessTop==declarationTop && accessBottom==declarationBottom) {
			// Already aligned properly
			return;
		}

		// Align tops and bottoms of member partners
		MemberFigure.alignTop(accessFig, declarationFig);
		MemberFigure.alignBottom(accessFig, declarationFig);
	}

}
