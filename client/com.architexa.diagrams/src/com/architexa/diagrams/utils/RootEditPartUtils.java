package com.architexa.diagrams.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.IWorkbenchPart;

import com.architexa.diagrams.editors.RSEMultiPageEditor;
import com.architexa.diagrams.figures.CommentFigure;
import com.architexa.diagrams.figures.EntityFigure;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.Entity;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.parts.IRSERootEditPart;
import com.architexa.diagrams.parts.PointPositionedDiagramPolicy;
import com.architexa.diagrams.parts.RSEEditPart;
import com.architexa.org.eclipse.draw2d.FigureCanvas;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.LayoutManager;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.GraphicalEditPart;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.requests.ChangeBoundsRequest;

/**
 * RootEditPart functionality so that other classes can implement
 * IRSERootEditPart and get the functionality from here.
 */
public class RootEditPartUtils {
	public static String COMMENT_LAYER ="Comment Layer";


	public static void refresh(Object model, IRSERootEditPart rootEditPartInterface, RSEEditPart rootEditPartNode) {
		refreshCommentChildren(getModelCommentChildren(model), rootEditPartInterface, rootEditPartNode);
	}

	private static List<Comment> getModelCommentChildren(Object model){
		return ((RootArtifact)model).getCommentChildren();
	}

	public static void refreshCommentChildren(List<Comment> commentChildrenList, IRSERootEditPart rootEditPartInterface, RSEEditPart rootEditPartNode) {
		int i;
		CommentEditPart cEditPart;
		Object commentModel;
		Map<Object, EditPart> modelToEP= new HashMap<Object, EditPart>();
		List<EditPart> commentEPChildren=rootEditPartInterface.getCommentEPChildren();

		for (i = 0; i < commentEPChildren.size(); i++) {
			cEditPart = (CommentEditPart)commentEPChildren.get(i);
			modelToEP.put(cEditPart.getModel(), cEditPart);
		}

		//no need remove
		List<Comment> modelComments = commentChildrenList;

		for (i = 0; i < modelComments.size(); i++) {
			commentModel = modelComments.get(i);

			if (i < commentEPChildren.size()
					&& ((EditPart) commentEPChildren.get(i)).getModel() == commentModel)
				continue;

			cEditPart = (CommentEditPart)modelToEP.get(commentModel);

			if (cEditPart != null)
				reorderCommentChild (cEditPart, i, rootEditPartInterface, rootEditPartNode);
			else {
				cEditPart = (CommentEditPart) rootEditPartNode.createChild(commentModel);
				addCommentChild(cEditPart, i, rootEditPartInterface,rootEditPartNode);
			}
		}

		//Section to remove any edit parts not having a corresponding model 
		List<EditPart> trash = new ArrayList<EditPart>();
		for(i=0 ; i < commentEPChildren.size(); i++ ){
			EditPart part=commentEPChildren.get(i);
			if(!(modelComments.contains(part.getModel()))) trash.add(part);
		}

		for (i = 0; i < trash.size(); i++) {
			EditPart ep = (EditPart)trash.get(i);
			removeCommentChild(ep, rootEditPartInterface, rootEditPartNode);
		}
		// check if all comments have been activated
		//this method is only needed when reopening a saved strata diagram 
		//as when comments are added in strata root doc, the root doc is not initialized till later
		refreshCommentLocation(rootEditPartInterface, rootEditPartNode);
	}

	//	Not in use right now
	private static void reorderCommentChild(EditPart child, int index, IRSERootEditPart rootEditPartInterface, RSEEditPart rootEditPartNode) {
		IFigure childFigure = ((GraphicalEditPart) child).getFigure();
		LayoutManager layout = rootEditPartNode.getContentPane().getLayoutManager();
		Object constraint = null;
		if (layout != null)
			constraint = layout.getConstraint(childFigure);

		removeCommentChildVisual(child, rootEditPartNode);
		List<EditPart> children = rootEditPartInterface.getCommentEPChildren();
		children.remove(child);
		children.add(index, child);
		addCommentChildVisual(child, index, rootEditPartNode);
		rootEditPartNode.setLayoutConstraint(child, childFigure, constraint);
	}
	
	private static void addCommentChild(CommentEditPart editPart, int index, IRSERootEditPart rootEditPartInterface, RSEEditPart rootEditPartNode){
		Assert.isNotNull(editPart);
		if (index == -1)
			index = rootEditPartInterface.getCommentEPChildren().size();
		if (rootEditPartInterface.getCommentEPChildren() == null)
			rootEditPartInterface.setCommentEPChildren(new ArrayList<EditPart>(2));

		rootEditPartInterface.getCommentEPChildren().add(index, editPart);
		editPart.setParent(rootEditPartNode);
		addCommentChildVisual(editPart, index, rootEditPartNode);
		editPart.addNotify();

		if (rootEditPartNode.isActive())
			editPart.activate();
		else	
			editPart.getFigure().setVisible(false);

		rootEditPartNode.fireChildAdded(editPart, index);
	}
	


	private static void addCommentChildVisual(EditPart commentEP, int index, RSEEditPart rootEditPartNode){
		IFigure child = ((GraphicalEditPart)commentEP).getFigure();
		if(!(child instanceof CommentFigure)) return;
		rootEditPartNode.getLayer(COMMENT_LAYER).add(child);
	}

	private static void removeCommentChild(EditPart child, IRSERootEditPart rootEditPartInterface, RSEEditPart rootEditPartNode){
		Assert.isNotNull(child);
		int index = rootEditPartInterface.getCommentEPChildren().indexOf(child);
		if (index < 0)
			return;
		rootEditPartNode.fireRemovingChild(child, index);
		if (rootEditPartNode.isActive())
			child.deactivate();
		child.removeNotify();
		removeCommentChildVisual(child, rootEditPartNode);
		child.setParent(null);
		rootEditPartInterface.getCommentEPChildren().remove(child);
	}

	private static void removeCommentChildVisual(EditPart childEditPart, RSEEditPart rootEditPartNode) {
		IFigure child = ((GraphicalEditPart)childEditPart).getFigure();
		if(!(child instanceof CommentFigure)) return;
		rootEditPartNode.getLayer(COMMENT_LAYER).remove(child);
	}
	
	/**
	 * Method to activate any inactive comment and set its location according to the saved top left 
	 * in its model.
	 * @param rootEditPartInterface 
	 */
	private static void refreshCommentLocation(IRSERootEditPart rootEditPartInterface, RSEEditPart rootEditPartNode) { 
		List<EditPart> commentEPChildren=rootEditPartInterface.getCommentEPChildren();
		CommentEditPart cEditPart;
		for (int i = 0; i < commentEPChildren.size(); i++) {
			cEditPart = (CommentEditPart)commentEPChildren.get(i);
			if(rootEditPartNode.isActive() && !(cEditPart.isActive())){
				cEditPart.activate();
				cEditPart.getFigure().setVisible(true);
			}

			Point zero=new Point(0,0);
			if(cEditPart.getFigure().getBounds().getTopLeft().equals(zero)){
				if( ((Comment)cEditPart.getModel()).getTopLeft()!=null && 
						!(((Comment)cEditPart.getModel()).getTopLeft().equals(zero))){
					Point topLeft = ((Comment)cEditPart.getModel()).getTopLeft();
					setCommentLocation((Comment) cEditPart.getModel(), topLeft, rootEditPartInterface);
				}
			}
		}
	}
	
	public static void setCommentLocation(Comment com, Point loc, IRSERootEditPart rootEditPart) {
		List<EditPart> childrenEP=rootEditPart.getCommentEPChildren();
		for(EditPart part : childrenEP){
			if(part.getModel().equals(com)){
				IFigure figure=((CommentEditPart)part).getFigure();
				figure.translateToRelative(loc);
				figure.translateFromParent(loc);
				PointPositionedDiagramPolicy.setLoc(com, loc);
				break;
			}
		}
	}

	public static Command getResizeCommand(ChangeBoundsRequest req) {
		if(!(req.getEditParts().get(0) instanceof CommentEditPart)) return null;
		CommentEditPart entity = (CommentEditPart) req.getEditParts().get(0);
		if(!(entity.getFigure() instanceof EntityFigure)) return null;
		final EntityFigure entityFig = (EntityFigure) entity.getFigure();
		 final Object resizingEntity = entity.getModel();
		 if (!(resizingEntity instanceof Entity)) return null;
		final Rectangle rect = entityFig.getBounds().getCopy();
		final Rectangle trans = req.getTransformedRectangle(rect);
		return new Command("Resize") {
			Rectangle oldBounds;
			@Override
			public void execute() {
				oldBounds = rect.getCopy();
				entityFig.resizeImg(trans);
				((Entity) resizingEntity).setSize(((Entity) resizingEntity).getSize().width + trans.width, trans.getSize().height );
			}
			@Override
			public void undo() {
				changeBounds();
			}
			@Override
			public void redo() {
				changeBounds();
			}
			private void changeBounds() {
				Rectangle curBounds = entityFig.getBounds().getCopy();
				if(oldBounds!=null) 
					entityFig.resizeImg(oldBounds);
				oldBounds = curBounds;
			}
		};
	}
	
	//TODO: should this return null for non RSEEditors?   
	public static IWorkbenchPart getEditorFromRSEMultiPageEditor(IWorkbenchPart targetEditor) {
		if (targetEditor instanceof RSEMultiPageEditor)
			return ((RSEMultiPageEditor) targetEditor).getRseEditor();
		return targetEditor;
	}
	
	public static boolean isOutOfViewportBounds(FigureCanvas control, Rectangle rect) {
		// Handling scrolled viewport
		int hScroll = control.getHorizontalBar().getSelection();
		int vScroll = control.getVerticalBar().getSelection();
		org.eclipse.swt.graphics.Rectangle viewportBounds = control.getBounds();
		viewportBounds.x = viewportBounds.x + hScroll;
		viewportBounds.y = viewportBounds.y + vScroll;
		if (!viewportBounds.contains(rect.getTopLeft().x, rect.getTopLeft().y) 
				|| !viewportBounds.contains(rect.getBottomLeft().x, rect.getBottomLeft().y)
				|| !viewportBounds.contains(rect.getTopRight().x, rect.getTopRight().y)
				|| !viewportBounds.contains(rect.getBottomRight().x, rect.getBottomRight().y))
			return true;
		
		return false;
	}
}
