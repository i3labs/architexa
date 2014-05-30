package com.architexa.diagrams.chrono.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.openrdf.model.Resource;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.chrono.models.HiddenNodeModel;
import com.architexa.diagrams.chrono.models.InstanceModel;
import com.architexa.diagrams.chrono.util.GroupedUtil;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqEditorFilter implements IEditorActionDelegate, IViewActionDelegate{

	private static IAction action;
	private static SeqEditor editor;
	private static List<HiddenNodeModel> hiddenNodes = new ArrayList<HiddenNodeModel>();

	public void setActiveEditor(IAction a, IEditorPart targetEditor) {
		action = a;
		setActionChecked(false);
		if(targetEditor == null) return;
		if (!(RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor) instanceof SeqEditor)) {
			a.setEnabled(false);
			return;
		} else
			action.setEnabled(true);
		editor = (SeqEditor) RootEditPartUtils.getEditorFromRSEMultiPageEditor(targetEditor);
	}

	public static void setActiveEditor(SeqEditor e) {
		editor = e;
		hiddenNodes.clear();
	}

	public void run(IAction action) {
		if(action.isChecked()) hideFigures();
		else unHideFigures();
	}

	public void selectionChanged(IAction a, ISelection selection) {
		action = a;
		setActionChecked(!hiddenNodes.isEmpty());
	}

	public static void setActionChecked(boolean checked) {
		if (action != null)
			action.setChecked(checked);
	}

	public static List<HiddenNodeModel> getHiddenNodes() {
		return hiddenNodes;
	}

	public static void hideFigures() {
		hiddenNodes.clear();
		setActionChecked(true);
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		List<ArtifactFragment> children = editor.getModel().getChildren();
		List<ArtifactFragment> childrenCopy = new ArrayList<ArtifactFragment>(children);

		for(ArtifactFragment af : childrenCopy) {
			if(!(af instanceof InstanceModel)) continue;

			InstanceModel child = (InstanceModel)af;
			Resource res = child.getResource();
			IJavaElement ije = RJCore.resourceToJDTElement(repo, res);

			boolean needToHide = false;
			if(ije == null || (ije instanceof IType&&((IType)ije).isBinary()) || ije.getResource()==null) {
				needToHide = true;
			} 
			if(!needToHide) {
				StatementIterator iter = repo.getStatements(res, null, null);
				if(iter.hasNext()){
					continue;
				}
			}

			int i = children.indexOf(child);
			if(i>0 && i<children.size() && children.get(i-1) instanceof HiddenNodeModel) {
				editor.getModel().removeChild(child);
				HiddenNodeModel adjacentHiddenNode = (HiddenNodeModel) children.get(i-1);
				if(!adjacentHiddenNode.getChildren().contains(child))
					adjacentHiddenNode.addChild(child);
			} else {
				editor.getModel().removeChild(child);
				HiddenNodeModel hiddenNode = new HiddenNodeModel(true);
				if(!hiddenNode.getChildren().contains(child))
					hiddenNode.addChild(child);
				hiddenNodes.add(hiddenNode);
				if(!editor.getModel().getChildren().contains(hiddenNode))
					editor.getModel().addChild(hiddenNode, i);
			}
		}
	}

	private void unHideFigures() {
		for(HiddenNodeModel hiddenNode : hiddenNodes) {
			int index = editor.getModel().getChildren().indexOf(hiddenNode);
			for(Object child : hiddenNode.getChildren()) {
				if(!(child instanceof ArtifactFragment)) continue;
				if(!editor.getModel().getChildren().contains(child))
					editor.getModel().addChild((ArtifactFragment)child, index);
				index++;
			}
		}
		for(HiddenNodeModel hiddenNode : hiddenNodes) {
			if(editor.getModel().getChildren().contains(hiddenNode))
				editor.getModel().removeChild(hiddenNode);
		}
		hiddenNodes.clear();
		setActionChecked(false);
	}

	public static void groupFigures(){
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		List<ArtifactFragment> children = editor.getModel().getChildren();
		List<ArtifactFragment> childrenCopy = new ArrayList<ArtifactFragment>(children);
		List<ArtifactFragment> toGroup = new ArrayList<ArtifactFragment>();
		CompoundCommand command = new CompoundCommand("Instance Grouping");
		for(ArtifactFragment af : childrenCopy) {
			if(!(af instanceof InstanceModel)) continue;

			InstanceModel child = (InstanceModel)af;
			Resource res = child.getResource();
			IJavaElement ije = RJCore.resourceToJDTElement(repo, res);

			boolean needToHide = false;
			if(ije == null || (ije instanceof IType&&((IType)ije).isBinary()) || ije.getResource()==null) {
				needToHide = true;
				toGroup.add(child);
			}

			if(!needToHide) {
				if(toGroup.size()>1)
					GroupedUtil.createGroupFromModels(toGroup, editor.getModel(),command);

				toGroup.clear();

				StatementIterator iter = repo.getStatements(res, null, null);
				if(iter.hasNext()){
					continue;
				}
			}
		}

		if(toGroup.size()>1)
			GroupedUtil.createGroupFromModels(toGroup, editor.getModel(),command);

		editor.getDiagramController().execute(command);
	}

	public void init(IViewPart view) {
	}

}
