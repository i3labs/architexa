package com.architexa.diagrams.chrono.util;


import java.util.LinkedHashMap;
import java.util.List;

import org.openrdf.model.URI;

import com.architexa.diagrams.chrono.commands.ConnectionCreateCommand;
import com.architexa.diagrams.chrono.commands.ConnectionDeleteCommand;
import com.architexa.diagrams.chrono.figures.ConnectionFigure;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.GroupedFieldModel;
import com.architexa.diagrams.chrono.models.MemberModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.MethodInvocationModel;
import com.architexa.diagrams.chrono.models.NodeModel;
import com.architexa.diagrams.chrono.models.UserCreatedMethodInvocationModel;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.org.eclipse.draw2d.ColorConstants;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.draw2d.geometry.Rectangle;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;

/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class ConnectionUtil {

	/**
	 * Creates a connection of the given type from source to target
	 * 
	 * @param message the method call message or field access message 
	 * that the connection corresponds to
	 * @param argLabelToIsExpandableMap a mapping from each argument in the connection
	 * message to whether or not the argument is an expandable invocation
	 * @param source the source of the connection
	 * @param target the target of the connection
	 * @param type the type of connection (ConnectionModel.CALL, ConnectionModel.RETURN,
	 * or ConnectionModel.OVERRIDES or ConnectionModel.FIELD_READ or ConnectionModel.FIELD_WRITE)
	 * @return the created connection or null if it could not be created
	 */
	public static ConnectionModel createConnection(String message, LinkedHashMap<String, Boolean> argLabelToIsExpandableMap, NodeModel source, NodeModel target, URI type) {
		ConnectionCreateCommand callConnectionCommand = new ConnectionCreateCommand(source, target, message, type, argLabelToIsExpandableMap);
		if(callConnectionCommand.canExecute()) callConnectionCommand.execute();

		return callConnectionCommand.getConnection();
	}

	/**
	 * @return the created connection or null if it could not be created
	 */
	public static ConnectionModel createConnection(String message, NodeModel source, NodeModel target, URI type) {
		return createConnection(message, new LinkedHashMap<String, Boolean>(), source, target, type);
	}

	public static ConnectionModel createConnection(String message, ArtifactFragment source, ArtifactFragment target, URI type) {
		return createConnection(message, new LinkedHashMap<String, Boolean>(), source, target, type);
	}
	private static ConnectionModel createConnection(String message,
			LinkedHashMap<String, Boolean> argLabelToIsExpandableMap,
			ArtifactFragment source, ArtifactFragment target, URI type) {
		ConnectionCreateCommand callConnectionCommand = new ConnectionCreateCommand(source, target, message, type, argLabelToIsExpandableMap);
		if(callConnectionCommand.canExecute()) callConnectionCommand.execute();

		return callConnectionCommand.getConnection();
	}

	/**
	 * 
	 * @param connection
	 * @param methodBox
	 * @return true if the given connection intersects the given method box, any incoming
	 * or outgoing connection of the method box, or the source or target of any incoming or
	 * outgoing connection
	 */
	public static boolean overlaps(ConnectionFigure connection, MethodBoxModel methodBox) {

		if(true) return false;

		if(methodBox.getOutgoingConnection()!=null && connection.equals(methodBox.getOutgoingConnection().getLine())) return false;
		if(methodBox.getIncomingConnection()!=null && connection.equals(methodBox.getIncomingConnection().getLine())) return false;

		Rectangle methodBoxBoundary;
		if(SeqUtil.debugHighlightingOn) {
			methodBox.getFigure().setBackgroundColor(ColorConstants.red);
		}
		System.out.println("are they different? " + methodBox.getFigure().getBounds() + ", " + methodBox.getFigure().getMethodBox().getBounds() + ", " + methodBox.getFigure().getContainer().getBounds());

		if(methodBox.getIncomingConnection()==null) methodBoxBoundary = methodBox.getFigure().getMethodBox().getBounds();
		else {
			Point boxLeft1 = methodBox.getFigure().getMethodBox().getBounds().getTopLeft();
			Point boxLeft2 = ((MethodBoxModel)methodBox.getIncomingConnection().getSource()).getFigure().getMethodBox().getBounds().getTopLeft();
			Point left = boxLeft1.x < boxLeft2.x ? boxLeft1 : boxLeft2;

			Point boxRight1 = methodBox.getFigure().getMethodBox().getBounds().getBottomRight();
			Point boxRight2 = ((MethodBoxModel)methodBox.getIncomingConnection().getSource()).getFigure().getMethodBox().getBounds().getBottomRight();
			Point right = boxRight1.x > boxRight2.x ? boxRight1 : boxRight2;

			methodBoxBoundary = new Rectangle(left, right);
		}

		Rectangle connectionBounds = connection.getBounds();

		System.out.println("boundaries: " + methodBoxBoundary + ", " + connectionBounds);

		return methodBoxBoundary.x < connectionBounds.x + connectionBounds.width 
		&& methodBoxBoundary.y + DiagramModel.TOP_MARGIN < connectionBounds.y + connectionBounds.height 
		&& methodBoxBoundary.x + methodBoxBoundary.width > connectionBounds.x 
		&& methodBoxBoundary.y + methodBoxBoundary.height + DiagramModel.TOP_MARGIN > connectionBounds.y;
	}

	private static boolean isDuplicateCommand(List<EditPart> selectedParts, Object modelOrConnToDelete){
		for(EditPart part: selectedParts){
			if(part.getModel().equals(modelOrConnToDelete)) //delete command has been created or will be created 
				return true;
		}
		return false;
	}

	//Method to get the partner connection if one exists
	private static ConnectionModel getPartnerConnection(ConnectionModel conn){
		ArtifactFragment source = conn.getSource();
		if (conn.getType().equals(ConnectionModel.CALL)) {
			if(source instanceof MemberModel)
				return ((MemberModel)source).getIncomingConnection();
//			else if(source instanceof UserCreatedMemberModel)
//				return ((UserCreatedMemberModel)source).getIncomingConnection();
		} else if(conn.getType().equals(ConnectionModel.RETURN)) {
			if(source instanceof MemberModel)
				return ((MemberModel)source).getOutgoingConnection();
//			else if(source instanceof UserCreatedMemberModel)
//				return ((UserCreatedMemberModel)source).getOutgoingConnection();
		}
		return null;
	}

	//Method to check if the parent instance is present in the selected list
	private static boolean parentInstanceSelected(ArtifactFragment target, List<EditPart> selectedParts) {
		if(target instanceof MemberModel)
			return isDuplicateCommand(selectedParts, ((MemberModel) target).getInstanceModel());
//		else if(target instanceof UserCreatedMemberModel)
//			return isDuplicateCommand(selectedParts, ((UserCreatedMemberModel) target).getUserInstanceModel());
		return false;
	}

	/*
	 * When multiple selections are made 
	 * > Return connection are responsible to call the corresponding invocation to create the delete commands
	 * > For connections between field models Field Declaration is called to create the delete commands
	 */
	public static void deleteConnection(ConnectionModel connModel, List<EditPart> selectedParts, CompoundCommand cmd){
		ArtifactFragment source = connModel.getSource();
		ArtifactFragment target = connModel.getDest();
		ConnectionModel partnerConn = getPartnerConnection(connModel);

		//check if source or targets parent instance is selected
		if(parentInstanceSelected(source, selectedParts) || parentInstanceSelected(target, selectedParts)) return;

		if(isDuplicateCommand(selectedParts, source)|| isDuplicateCommand(selectedParts, target)) return;
		else if(isDuplicateCommand(selectedParts, partnerConn) && connModel.getType().equals(ConnectionModel.CALL)) return;
		else if(connModel.getType().equals(ConnectionModel.RETURN)) deleteReturnConnection(connModel, cmd, selectedParts);
		else if(connModel.getType().equals(ConnectionModel.OVERRIDES)){
			cmd.add(new ConnectionDeleteCommand(connModel));
		}else if(connModel.getType().equals(ConnectionModel.CALL) && getPartnerConnection(connModel)!=null) deleteReturnConnection(getPartnerConnection(connModel),cmd, selectedParts);
		else deleteReturnConnection(connModel, cmd, selectedParts); // No return for FIELD READ/WRITE
	}

	private static void deleteReturnConnection(ConnectionModel connModel,
			CompoundCommand command, List<EditPart> selectedParts) {
		ArtifactFragment target = connModel.getDest();

		// In case of field the target is the declaration
		if(target instanceof FieldModel){ // deleting field models
			MemberUtil.deleteFieldDeclaration((FieldModel) target, ((FieldModel) target).getParent(), command, true);
		}else if(target instanceof GroupedFieldModel) //deleting grouped field models
			GroupedMemberUtil.deleteGroupedFieldDeclaration((GroupedFieldModel) target, (NodeModel) ((GroupedFieldModel)target).getParent(), command);

		if(target instanceof MethodInvocationModel){
			MemberUtil.getInvocationDeleteCommand((MemberModel) target, ((MethodInvocationModel) target).getParent(), command, selectedParts);
		}else if(target instanceof UserCreatedMethodInvocationModel){
			GroupedMemberUtil.getInvocationDeleteCommand((MemberModel) target, (NodeModel) ((UserCreatedMethodInvocationModel) target).getParent(), command);
		}
	}
}
