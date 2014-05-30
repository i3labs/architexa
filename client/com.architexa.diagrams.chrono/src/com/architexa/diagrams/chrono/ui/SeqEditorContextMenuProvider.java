package com.architexa.diagrams.chrono.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.ComboBoxLabelProvider;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;

import com.architexa.diagrams.chrono.controlflow.AddUserCreatedControlFlowAction;
import com.architexa.diagrams.chrono.editparts.DiagramEditPart;
import com.architexa.diagrams.chrono.editparts.HiddenEditPart;
import com.architexa.diagrams.chrono.editparts.InstanceEditPart;
import com.architexa.diagrams.chrono.editparts.MethodBoxEditPart;
import com.architexa.diagrams.chrono.editparts.SeqNodeEditPart;
import com.architexa.diagrams.chrono.editparts.UserCreatedInstanceEditPart;
import com.architexa.diagrams.chrono.editparts.UserCreatedMethodBoxEditPart;
import com.architexa.diagrams.chrono.sequence.SeqPlugin;
import com.architexa.diagrams.chrono.util.MemberUtil;
import com.architexa.diagrams.chrono.util.SeqUtil;
import com.architexa.diagrams.commands.ColorActionCommand;
import com.architexa.diagrams.jdt.utils.JDTUISupport;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.model.WidthDPolicy;
import com.architexa.diagrams.relo.jdt.actions.AddJavaDocAction;
import com.architexa.diagrams.relo.jdt.actions.EditJavaDocDialogAction;
import com.architexa.diagrams.ui.RSEContextMenuProvider;
import com.architexa.intro.preferences.PreferenceConstants;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.org.eclipse.gef.EditPartViewer;
import com.architexa.org.eclipse.gef.GraphicalViewer;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.org.eclipse.gef.ui.actions.ActionRegistry;
import com.architexa.rse.BuildStatus;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public class SeqEditorContextMenuProvider extends RSEContextMenuProvider {

	private static Logger logger = SeqPlugin.getLogger(SeqEditorContextMenuProvider.class);
	private static ComboBoxPropertyDescriptor truncateMethodsDescriptor = 
		new ComboBoxPropertyDescriptor("TruncateSetting", "Method Names", new String[] {
				"Show Full Names", "Truncate Long Names (Mouse-over to reveal full name)"});

	private ActionRegistry actionRegistry;
	//private IWorkbenchPart editorOrView;

	public SeqEditorContextMenuProvider(EditPartViewer viewer, ActionRegistry registry, IWorkbenchPart seqPart) {
		super(viewer, registry);
		actionRegistry = registry;
		//editorOrView = seqPart;
	}

	@Override
	public void buildContextMenu(IMenuManager menu) {
		try {
			super.buildContextMenu(menu);
	
			if(getViewer().getSelectedEditParts()==null || 
					getViewer().getSelectedEditParts().size()==0) return;
	
			EditPart ep1 = (EditPart)getViewer().getSelectedEditParts().get(0);
	
			// Open in Java Editor
			if (ep1 instanceof SeqNodeEditPart &&
					!(ep1 instanceof UserCreatedInstanceEditPart) &&
					!(ep1 instanceof UserCreatedMethodBoxEditPart)) {
				IAction openAction = ((SeqNodeEditPart)ep1).getOpenInJavaEditorAction(
						JDTUISupport.getOpenInJavaEditorActionString(), JDTUISupport.getOpenInJavaEditorActionIcon());
				if (openAction != null) menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_EDITORS, openAction);
			}
	
			// method actions, like extended delete/collapse
			if (ep1 instanceof MethodBoxEditPart) 
				((MethodBoxEditPart)ep1).buildContextMenu(menu);
	
	
			// instance actions, like grouping or showing all interactions
			boolean allSelectionsAreInstances = true;
			for (Object part : getViewer().getSelectedEditParts()) {
				if (!(part instanceof InstanceEditPart)) {
					allSelectionsAreInstances = false;
					break;
				}
			}
			if (allSelectionsAreInstances) {
				List<InstanceEditPart> selectedInstances = new ArrayList<InstanceEditPart>();
				for (Object part : getViewer().getSelectedEditParts()) {
					selectedInstances.add((InstanceEditPart)part);
				}
				((InstanceEditPart)ep1).buildContextMenu(menu, selectedInstances);
			}
	
			IAction action;
	
			// add java doc to diagram and edit java doc in source
			action = new AddJavaDocAction((AbstractGraphicalEditPart) ep1);
			if ( !(ep1.getModel() instanceof ArtifactFragment) || !((AddJavaDocAction) action).canRun((ArtifactFragment) ep1.getModel())) 
				action.setEnabled(false);
			// place separator before java doc action(s)
			menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, new Separator());
			menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, action);
			
			// add a preference to either always show full method names
			// or to truncate long method names except on mouse over
			if (ep1 instanceof MethodBoxEditPart || ep1 instanceof DiagramEditPart) {
				// adding it in the context menu of the diagram as well as methods 
				// since this is a diagram-wide preference (ie it applies to all
				// methods in the diagram, not just the selected one).
				DiagramEditPart diagramEP = (DiagramEditPart) ep1.getViewer().getContents();
				addTruncateAction(menu, diagramEP);
			}
			
			if (ep1 instanceof MethodBoxEditPart) {
				DiagramEditPart diagramEP = (DiagramEditPart) ep1.getViewer().getContents();
				addControlFlowAction(menu, diagramEP, getViewer().getSelectedEditParts());
			}
	
			// add Highlight options
			if (ep1.getModel() instanceof ArtifactFragment) {
				addHighlightAction(getViewer().getSelectedEditParts(), menu);
				//Edit Javadoc
				action = new EditJavaDocDialogAction((AbstractGraphicalEditPart) ep1, ((ArtifactFragment) ep1.getModel()).getRootArt().getRepo() );
				menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_TOOLS, action);
				if (!(ep1.getModel() instanceof ArtifactFragment) || 
						ep1 instanceof UserCreatedInstanceEditPart ||
						ep1 instanceof UserCreatedMethodBoxEditPart)
					action.setEnabled(false);
	
			}
			// add option to change connection width
			if (ep1.getModel() instanceof NamedRel) {
				List<Object> selList = new ArrayList<Object>();
				selList.add(ep1);
				WidthDPolicy.addConnectionWidthChangeAction(selList, menu, (GraphicalViewer) getViewer());
			}
		} catch (Throwable t) {
			logger.error("Could not create Sequence Diagram Menu. ", t);
		}
	}
	
	private void addControlFlowAction(IMenuManager menu, final DiagramEditPart diagramEP, final List<EditPart> selectedEps) {
			IAction act = new AddUserCreatedControlFlowAction(selectedEps, diagramEP, getViewer());
			menu.add(act);
	}

	@Override
	protected void addContextPref(IMenuManager menu) {
		// Override and do nothing here since we don't want the Show Context / Hide
		// Context options in Chrono's context menu. (Chrono's properties group
		// will contain the actions for setting whether backward messages are 
		// shown and whether long names are truncated).
	}

	private void addTruncateAction(IMenuManager menu, final DiagramEditPart diagramEP) {

		// Truncate setting goes in context menu's settings section
		MenuManager truncateMenu = new MenuManager("Method Names", "truncateMenu");
		menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_SETTINGS, truncateMenu);

		ComboBoxLabelProvider cblp = (ComboBoxLabelProvider) truncateMethodsDescriptor.getLabelProvider();
		String[] cbVals = cblp.getValues();
		for (int i = 0; i < cbVals.length; i++) {
			final Integer cbNdx = i;
			// option order is Show (cbNdx==0), Truncate (cbNdx==1)
			IAction act = new Action(cbVals[i], Action.AS_RADIO_BUTTON){
				@Override
				public void run() {
					SeqUtil.getPreferenceStore().setValue(PreferenceConstants.TruncateLongMethods, (cbNdx==1));
					boolean show = (cbNdx==0);
					// Update the display of all the methods in the diagram
					MemberUtil.updateFullOrAbbrvConnectionLabels(diagramEP, show);
				}
			};
			if (cbNdx == 0 && !SeqUtil.getPreferenceStore().getBoolean(PreferenceConstants.TruncateLongMethods))
				act.setChecked(true);
			else if (cbNdx == 1 && SeqUtil.getPreferenceStore().getBoolean(PreferenceConstants.TruncateLongMethods))
				act.setChecked(true);

			truncateMenu.add(act);
		}
	}

	 public boolean allHighlightable(List<EditPart> selEPs) {
	    	for (EditPart part : selEPs) {
				if (!(part instanceof SeqNodeEditPart) 
						&& !(part instanceof HiddenEditPart))
					return false;
	    	}
	    	return true;
	 }
	
	private String DEFAULT = "Default";
	private void addHighlightAction(List selectedEditParts,
			IMenuManager menu) {
		if (!allHighlightable(selectedEditParts))
			return;
		
		MenuManager subMenu = new MenuManager("Highlight");
		subMenu.add(getColorAction(ColorScheme.RED, selectedEditParts));
		subMenu.add(getColorAction(ColorScheme.BLUE, selectedEditParts));
		subMenu.add(getColorAction(ColorScheme.GREEN, selectedEditParts));
		subMenu.add(new Separator());
		subMenu.add(getColorAction(DEFAULT, selectedEditParts));

		// Highlight menu goes in the Appearance section
		menu.appendToGroup(RSEContextMenuProvider.GROUP_RSE_EDIT_APPEARANCE, subMenu);
	}
	
	private IAction getColorAction(final String actnText,
			final List selectedEditParts) {
		return new Action(actnText) {
			@Override
			public void run() {
				CompoundCommand cmd = new CompoundCommand("New Coloring command:" + actnText);
				BuildStatus.addUsage("Chrono > " + cmd.getLabel());
				for (Object part : selectedEditParts) {
					cmd.add(new ColorActionCommand(actnText, (ArtifactFragment)((SeqNodeEditPart)part).getModel()));
				}
				
	    		getViewer().getEditDomain().getCommandStack().execute(cmd);
			}
		};
	}
	
	private IAction getAction(String actionId) {
		return actionRegistry.getAction(actionId);
	}

//	private void closeOriginalEditor(IWorkbenchWindow activeWorkbenchWindow) {
//		if(editorOrView instanceof SeqEditor) 
//			activeWorkbenchWindow.getActivePage().closeEditor((SeqEditor)editorOrView, true);
//		else if(editorOrView instanceof SeqView)
//			activeWorkbenchWindow.getActivePage().hideView((SeqView)editorOrView);
//	}

}
