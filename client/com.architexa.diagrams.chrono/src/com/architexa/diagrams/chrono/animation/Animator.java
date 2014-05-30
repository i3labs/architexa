package com.architexa.diagrams.chrono.animation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;

import com.architexa.diagrams.chrono.animation.AnimateOverrideCommand.AnimateOverrideConnectionCommand;
import com.architexa.diagrams.chrono.commands.InstanceCreateCommand;
import com.architexa.diagrams.chrono.commands.MemberCreateCommand;
import com.architexa.diagrams.chrono.figures.InstanceFigure;
import com.architexa.diagrams.chrono.figures.InstanceFigure.InstanceChildrenContainer;
import com.architexa.diagrams.chrono.figures.MethodBoxFigure;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.ui.ColorScheme;
import com.architexa.diagrams.chrono.util.LayoutUtil;
import com.architexa.diagrams.ui.AnimationCommand;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.Connection;
import com.architexa.org.eclipse.draw2d.IFigure;
import com.architexa.org.eclipse.draw2d.RectangleFigure;
import com.architexa.org.eclipse.draw2d.Viewport;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.PointList;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.commands.Command;




/**
 * @author hudsonr
 * Created on Apr 28, 2003
 * Modified by Elizabeth L. Murnane
 */
public class Animator {

	static Command command;

	static final long DURATION = 230;

	static long current;
	static double progress;
	static long start = -1;
	static long finish;
	static Viewport viewport;

	public static boolean PLAYBACK;
	static boolean RECORDING;

	static Map<Object, Object> initialStates;
	static Map<Object, Object> finalStates;

	public static void setCommand(Command command) {
		Animator.command = command;
	}

	public static void end() {
		Iterator<Object> iter = initialStates.keySet().iterator();
		while (iter.hasNext()) {
			IFigure f = ((IFigure)iter.next());
			f.revalidate();
			f.setVisible(true);
		}
		command = null;
		initialStates = null;
		finalStates = null;
		PLAYBACK = false;
		viewport = null;
	}

	public static boolean captureLayout(IFigure root) {
		RECORDING = true;

		while(!(root instanceof Viewport)) root = root.getParent();
		viewport = (Viewport)root;
		while(root.getParent()!= null) root = root.getParent();

		initialStates = new HashMap<Object, Object>();
		finalStates = new HashMap<Object, Object>();

		//This part records all layout results.
		root.validate();
		if(initialStates==null) {
			RECORDING = false;
			return false;
		}
		Iterator<Object> iter = initialStates.keySet().iterator();
		if(!iter.hasNext()) {
			//Nothing layed out, so abort the animation
			RECORDING = false;
			return false;
		}

		RECORDING = false;
		while(iter.hasNext()) {
			IFigure figure = (IFigure) iter.next();
			validateAndLayoutBeforeFinalRecord(figure);
			recordFinalState(figure);
		}

		start = System.currentTimeMillis();
		finish = start + DURATION;
		current = start + 20;

		PLAYBACK = true;
		return true;
	}

	public static boolean playbackState(Connection conn) {
		if(!PLAYBACK) return false;

		if(conn.getSourceAnchor().getOwner() instanceof InstanceFigure) 
			playbackLifeLineState(conn);
		return true;
	}

	static boolean playbackState(IFigure container) {
		if(!PLAYBACK) return false;

		if(container instanceof InstanceChildrenContainer) {
			playbackInstanceChildContainerState((InstanceChildrenContainer)container);
		}

		List<?> children = container.getChildren();
		Rectangle rect1, rect2;
		for (int i = 0; i < children.size(); i++) {
			IFigure child = (IFigure)children.get(i);

			fadeInIfNewlyAdded(child);

			rect1 = (Rectangle)initialStates.get(child);
			rect2 = (Rectangle)finalStates.get(child);
			if(rect2==null || rect2.equals(rect1)) continue;

			if(command instanceof AnimationCommand) {
				((AnimationCommand)command).makeAdjustmentsForAnimationPlayback(child, rect1, rect2, finalStates);
			} else if(command instanceof AnimateOverrideConnectionCommand) {
				playbackOverrideIndicator((AnimateOverrideConnectionCommand)command);
			}

			child.setBounds(new Rectangle(
					(int)Math.round(progress * rect2.x + (1-progress) * rect1.x),
					(int)Math.round(progress * rect2.y + (1-progress) * rect1.y),
					(int)Math.round(progress * rect2.width + (1-progress) * rect1.width),
					(int)Math.round(progress * rect2.height + (1-progress) * rect1.height)
			));
			if(child instanceof InstanceChildrenContainer) {
				((InstanceChildrenContainer)child).setSpacers(rect2.width);
			}
		}
		return true;
	}

	static void recordFinalState(Connection conn) {
		PointList points1 = (PointList)initialStates.get(conn);
		PointList points2 = conn.getPoints().getCopy();

		if (points1 != null && points1.size() != points2.size()) {
			Point p = new Point(), q = new Point();

			int size1 = points1.size() - 1;
			int size2 = points2.size() - 1;

			int i1 = size1;
			int i2 = size2;

			double current1 = 1.0;
			double current2 = 1.0;

			double prev1 = 1.0;
			double prev2 = 1.0;

			while (i1 > 0 || i2 > 0) {
				if (Math.abs(current1 - current2) < 0.1
						&& i1 > 0 && i2 > 0) {
					//Both points are the same, use them and go on;
					prev1 = current1;
					prev2 = current2;
					i1--;
					i2--;
					current1 = (double)i1 / size1;
					current2 = (double)i2 / size2;
				} else if (current1 < current2) {
					//2 needs to catch up
					// current1 < current2 < prev1
					points1.getPoint(p, i1);
					points1.getPoint(q, i1 + 1);

					p.x = (int)(((q.x * (current2 - current1) + p.x * (prev1 - current2))
							/ (prev1 - current1)));
					p.y = (int)(((q.y * (current2 - current1) + p.y * (prev1 - current2))
							/ (prev1 - current1)));

					points1.insertPoint(p, i1 + 1);

					prev1 = prev2 = current2;
					i2--;
					current2 = (double)i2 / size2;

				} else {
					//1 needs to catch up
					// current2< current1 < prev2

					points2.getPoint(p, i2);
					points2.getPoint(q, i2 + 1);

					p.x = (int)(((q.x * (current1 - current2) + p.x * (prev2 - current1))
							/ (prev2 - current2)));
					p.y = (int)(((q.y * (current1 - current2) + p.y * (prev2 - current1))
							/ (prev2 - current2)));

					points2.insertPoint(p, i2 + 1);

					prev2 = prev1 = current1;
					i1--;
					current1 = (double)i1 / size1;
				}
			}
		}
		finalStates.put(conn, points2);
	}

	static void recordFinalState(IFigure child) {
		if(child instanceof Connection) {
			recordFinalState((Connection)child);
			return;
		}
		Rectangle rect2 = child.getBounds().getCopy();
		Rectangle rect1 = (Rectangle)initialStates.get(child);
		if(rect1.isEmpty()) {
			rect1.x = rect2.x;
			rect1.y = rect2.y;
			rect1.width = rect2.width;
		}

		if(command instanceof MemberCreateCommand && child.equals(((MemberCreateCommand)command).getChild().getFigure())) {
			// child is the new method being created

			// Determine whether animating its motion and if so set the start and direction
			String animateType = ((MemberCreateCommand)command).getAnimateType();
			if(MemberCreateCommand.FROM_INSTANCE.equals(animateType)) {
				// Animate vertically down from the instance
				rect1.x = rect2.x;
				rect1.y = 0;
				rect1.width = rect2.width;
				rect1.height = rect2.height;
			} else if(MemberCreateCommand.NONE.equals(animateType)) {
				rect1.x = rect2.x;
				rect1.y = rect2.height/2 + rect2.y;
				rect1.width = rect2.width;
				rect1.height = 0;
			}
		} else if(command instanceof MemberCreateCommand) {
			// child is not the method being created

			rect1.x = rect2.x;
			rect1.y = rect2.y;
			rect1.width = rect2.width;
			rect1.height = rect2.height;
		} else if(command instanceof AnimationCommand) {
			((AnimationCommand)command).setAnimationStates(child, rect1, rect2);
		}
		finalStates.put(child, rect2);
	}

	public static void recordInitialState(Connection connection) {
		if(!RECORDING) return;
		PointList points = connection.getPoints().getCopy();
		if (points.size()==2
				&& points.getPoint(0).equals(Point.SINGLETON.setLocation(0,0))
				&& points.getPoint(1).equals(Point.SINGLETON.setLocation(100,100))) {
			initialStates.put(connection, null);
		} else {
			initialStates.put(connection, points);
		}
	}

	public static void recordInitialState(IFigure container) {
		if(!RECORDING) return;

		List<?> children = container.getChildren();
		IFigure child;
		for(int i=0; i<children.size();i++) {
			child = (IFigure)children.get(i);
			initialStates.put(child, child.getBounds().getCopy());
		}
	}

	static void swap() {
		Map<Object, Object> temp = finalStates;
		finalStates = initialStates;
		initialStates = temp;
	}

	public static boolean step() {
		current = System.currentTimeMillis() + 30;
		progress = (double)(current - start)/(finish - start);
		progress = Math.min(progress, 0.999);
		Iterator<Object> iter = initialStates.keySet().iterator();

		while(iter.hasNext()) ((IFigure)iter.next()).revalidate();
		viewport.validate();

		return current < finish;
	}

	private static void validateAndLayoutBeforeFinalRecord(IFigure figure) {
		figure.validate();
		figure.revalidate();
		if(figure.getLayoutManager()!=null) {
			figure.getLayoutManager().layout(figure);
		}
		if(figure.getParent()!=null && figure.getParent().getLayoutManager()!=null) {
			figure.getParent().getLayoutManager().layout(figure.getParent());
		}
	}

	private static void playbackLifeLineState(Connection lifeLine) {
		InstanceFigure instanceMoving = (InstanceFigure)lifeLine.getSourceAnchor().getOwner();

		int x = instanceMoving.findLocationForAnchor(InstanceFigure.bottomInstanceBox).x;
		int startY; 
		int endY;
		if(lifeLine.equals(instanceMoving.getLifeLineInInstancePanel())) {
			startY = instanceMoving.findLocationForAnchor(InstanceFigure.bottomInstanceBox).y;
			endY = startY + DiagramModel.INSTANCE_PANEL_MARGINS;
		} else {
			startY = instanceMoving.findLocationForAnchor(InstanceFigure.bottomInstanceBox).y + DiagramModel.INSTANCE_PANEL_MARGINS;
			endY = instanceMoving.findLocationForAnchor(InstanceFigure.bottomChildrenContainer).y;
		}

		PointList points = lifeLine.getPoints();
		points.removeAllPoints();
		points.addPoint(new Point(x, startY));
		points.addPoint(new Point(x, endY));
		lifeLine.setPoints(points);
	}

	private static void playbackInstanceChildContainerState(InstanceChildrenContainer childContainer) {
		if(command instanceof AnimateCallMadeCommand && childContainer.equals(((AnimateCallMadeCommand)command).getInstanceOfDeclFigure().getChildrenContainer())) return;
		InstanceFigure instanceMoving = childContainer.getInstanceFigure();
		Rectangle childContainerBounds = childContainer.getBounds().getCopy();
		if(childContainer.getParent().getLayoutManager() instanceof AnimationLayoutManager) {
			childContainerBounds.height = LayoutUtil.getHeightToContainAllChildren(childContainer);
		}
		childContainer.setBounds(new Rectangle(instanceMoving.getBounds().x - DiagramModel.SIDE_MARGIN - 1, childContainerBounds.y, instanceMoving.getBounds().width/*childContainerBounds.width*/, childContainerBounds.height));
	}

	private static void playbackOverrideIndicator(AnimateOverrideConnectionCommand overridesCmd) {

		Rectangle rect1 = overridesCmd.getIndicatorAnimateStart();
		Rectangle rect2 = overridesCmd.getIndicatorAnimateEnd();

		RectangleFigure indicatorOfOverridden = overridesCmd.getOverriddenFigure().getOverriddenIndicator();
		indicatorOfOverridden.setBounds(new Rectangle(
				(int)Math.round(progress * rect2.x + (1-progress) * rect1.x),
				(int)Math.round(progress * rect2.y + (1-progress) * rect1.y),
				(int)Math.round(progress * rect2.width + (1-progress) * rect1.width),
				(int)Math.round(progress * rect2.height + (1-progress) * rect1.height)
		));

		overridesCmd.getOverrideConnection().getLine().revalidate();
	}

	private static void fadeInIfNewlyAdded(IFigure newFigure) {
		if((command instanceof InstanceCreateCommand && ((InstanceCreateCommand)command).getFadeInCreation() && newFigure.equals(((InstanceCreateCommand)command).getChild().getFigure())) 
				|| (command instanceof AnimateCallMadeCommand && ((AnimateCallMadeCommand)command).isInstanceNew() && newFigure.equals(((AnimateCallMadeCommand)command).getInstanceOfDeclFigure()))
				|| (command instanceof AnimateCalledByCommand && ((AnimateCalledByCommand)command).isInstanceNew() && newFigure.equals(((AnimateCalledByCommand)command).getInstanceFigure()))
				|| (command instanceof AnimateOverrideCommand && ((AnimateOverrideCommand)command).isInstanceNew() && newFigure.equals(((AnimateOverrideCommand)command).getInstanceFigure()))
				|| (command instanceof AnimateChainExpandCommand && ((AnimateChainExpandCommand)command).isInstanceNew() && newFigure.equals(((AnimateChainExpandCommand)command).getInstanceFigure()))) {
			((InstanceFigure)newFigure).setColors(fadeIn(ColorScheme.instanceFigureBackground), fadeIn(ColorScheme.instanceFigureText), fadeIn(ColorScheme.instanceFigureBorder));
		} else if((command instanceof MemberCreateCommand && newFigure.equals(((MemberCreateCommand)command).getChild().getFigure())) 
				|| (command instanceof AnimateCallMadeCommand && newFigure.equals(((AnimateCallMadeCommand)command).getDeclarationFigure()))
				|| (command instanceof AnimateCalledByCommand && ((((AnimateCalledByCommand)command).isDeclarationNew() && (newFigure.equals(((AnimateCalledByCommand)command).getDeclarationFigure()))) || (((AnimateCalledByCommand)command).isDeclMakingInvocNew() && newFigure.equals(((AnimateCalledByCommand)command).getDeclMakingInvocFigure()))))
				|| (command instanceof AnimateOverrideCommand && ((AnimateOverrideCommand)command).isMethodNew() && newFigure.equals(((AnimateOverrideCommand)command).getNewFigure()))
				|| (command instanceof AnimateChainExpandCommand && newFigure.equals(((AnimateChainExpandCommand)command).getNewDeclarationFigure()))) {
			((MethodBoxFigure)newFigure).setColors(fadeIn(ColorScheme.methodDeclarationFigureBackground), fadeIn(ColorScheme.noConnectionsBoxLabelText));
		}
	}

	private static Color fadeIn(Color originalColor) {
		return fade(ColorConstants.white, originalColor);
	}

	private static Color fadeOut(Color newColor) {
		return fade(newColor, ColorConstants.white);
	}

	private static Color fade(Color color1, Color color2) {
		int red   = (int) (progress * color2.getRed()   + (1 - progress) * color1.getRed());
		int green = (int) (progress * color2.getGreen() + (1 - progress) * color1.getGreen());
		int blue  = (int) (progress * color2.getBlue()  + (1 - progress) * color1.getBlue());

		return new Color(null, red, green, blue);
	}

}