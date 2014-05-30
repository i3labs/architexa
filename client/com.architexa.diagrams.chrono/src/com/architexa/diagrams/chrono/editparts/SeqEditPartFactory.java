package com.architexa.diagrams.chrono.editparts;



import com.architexa.diagrams.chrono.controlflow.ControlFlowEditPart;
import com.architexa.diagrams.chrono.controlflow.ControlFlowModel;
import com.architexa.diagrams.chrono.controlflow.UserCreatedControlFlowEditPart;
import com.architexa.diagrams.chrono.controlflow.UserCreatedControlFlowModel;
import com.architexa.diagrams.chrono.models.ConnectionModel;
import com.architexa.diagrams.chrono.models.DiagramModel;
import com.architexa.diagrams.chrono.models.FieldModel;
import com.architexa.diagrams.chrono.models.GroupedFieldModel;
import com.architexa.diagrams.chrono.models.GroupedInstanceModel;
import com.architexa.diagrams.chrono.models.GroupedMethodBoxModel;
import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.models.MethodBoxModel;
import com.architexa.diagrams.chrono.models.UserCreatedInstanceModel;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.parts.CommentEditPart;
import com.architexa.diagrams.parts.NamedRelationPart;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartFactory;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = getPart(model);
		part.setModel(model);
		return part;
	}

	private EditPart getPart(Object model) {
		//System.out.println("getting part for " + model);
		if (model instanceof InstanceModel) {
			if (model instanceof UserCreatedInstanceModel) {
				if (model instanceof GroupedInstanceModel) {
					return new GroupedInstanceEditPart("", ((GroupedInstanceModel)model).getClassName());
				}
				return new UserCreatedInstanceEditPart("", ((UserCreatedInstanceModel)model).getInstanceName() + ((UserCreatedInstanceModel)model).getClassName());
			}
			return new InstanceEditPart(((InstanceModel)model).getInstanceName(), ((InstanceModel)model).getClassName());
		}
		if (model instanceof ConnectionModel) {
			//System.out.println("we're trying to make a connection");
			return new ConnectionEditPart();
		}
		
		if (model instanceof MethodBoxModel) {
			if (((MethodBoxModel) model).isUserCreated()) {
				if (model instanceof GroupedMethodBoxModel){
					return new GroupedMethodBoxEditPart();
				}
				return new UserCreatedMethodBoxEditPart();
			}
			MethodBoxModel mbModel = (MethodBoxModel)model;
			//System.out.println("we're trying to make a method box " + mbModel.getMethodName());
			MethodBoxEditPart methodBoxEP = new MethodBoxEditPart();
			mbModel.getInstanceModel().addMethodBox(methodBoxEP);
			return methodBoxEP;
		}
		if (model instanceof FieldModel) {
			FieldEditPart fieldEP = new FieldEditPart();
			return fieldEP;
		}
		if (model instanceof DiagramModel) {
			//System.out.println("making a diagram ep");
			return new DiagramEditPart();
		}
		if (model instanceof HiddenNodeModel) {
			//System.out.println("getting a hidden part");
			return new HiddenEditPart();
		}
		if (model instanceof ControlFlowModel) {
			if (model instanceof UserCreatedControlFlowModel)
				return new UserCreatedControlFlowEditPart();
			else
				return new ControlFlowEditPart();
		}
		
		if (model instanceof Comment){
			Comment.initComment((Comment) model);
			return new CommentEditPart();
		}
		
		if (model instanceof GroupedFieldModel) {
			return new GroupedFieldEditPart();
		}
		if (model instanceof NamedRel){
			return new NamedRelationPart();
		}
		throw new RuntimeException(
				"Can't create part for model element: "
				+ ((model != null) ? model.getClass().getName() : "null"));
	}
}
