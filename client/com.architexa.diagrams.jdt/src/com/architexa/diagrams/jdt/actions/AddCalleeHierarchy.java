package com.architexa.diagrams.jdt.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.jdt.Activator;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.ui.CalleeHierarchyWizardDialog;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.org.eclipse.gef.EditPart;
import com.architexa.store.ReloRdfRepository;

/**
 * Action that will incrementally add all of the enabled and
 * visible calls the starting makes, every call those called 
 * methods make, every call those methods make, etc.
 * 
 * A wizard guides the user as the calls are added by prompting
 * to continue or cancel before each of these sets of calls are
 * added to the diagram.
 *
 */
public abstract class AddCalleeHierarchy extends Action {

	protected static String actionLbl = "Add Callee Hierarchy";

	private EditPart startingMethodEP;
	private String startingMethodLabel="";
	private IAction addAllCallsAction; 
	private ReloRdfRepository repo;

	private boolean first = true;
	private boolean addAll = false;
	private boolean exit = false;

	public AddCalleeHierarchy(EditPart startingMethodEP, 
			String startingMethodName, String startingMethodClass,
			ReloRdfRepository repo) {
		super(actionLbl+"..."); // put ... since this action opens a dialog
		setImageDescriptor(Activator.getImageDescriptor("icons/addAll_callees_hier.png"));

		this.startingMethodEP = startingMethodEP;
		startingMethodLabel = startingMethodClass.trim()+"."+startingMethodName.trim();

		this.repo = repo;
	}

	/**
	 * Implementers of AddCalleeHierarchy MUST call this method to set the action that 
	 * will add to the diagram the first set of calls in the hierarchy - the enabled 
	 * and visible calls in the menu.
	 * 
	 * Implementers should override the run() method and set this action before calling
	 * super.run(). It needs to be set once this AddCalleeHierarchy action is run so that 
	 * when the set of visible and enabled calls in the menu is determined, the
	 * retrieved set of menu items is current; but it needs to be set before super.run(), 
	 * which will open the wizard dialog, making the menu's items unavailable
	 *
	 */
	public void setAddAllCallsAction(IAction action) {
		this.addAllCallsAction = action;
	}

	/**
	 * @return true if the given method has had its callee hierarchy added 
	 * to the diagram or is queued to have its callee hierarchy added
	 */
	public abstract boolean isMethodAlreadyDone(ArtifactFragment methodModel);
	/** 
	 * Adds method to the list of reached methods that have had their callee hierarchy 
	 * added to the diagram or are queued to have their callee hierarchy added
	 */
	public abstract void addMethodDone(ArtifactFragment methodModel);

	/**
	 * @return true if the given method makes at least 
	 * one call not yet in the diagram, false otherwise
	 */
	public abstract boolean makesCallNotInDiagram(EditPart methodEP, DirectedRel rel);

	/**
	 * Adds all the calls that the given method makes to the diagram
	 */
	public abstract void displayAllCallsMade(EditPart methodEP, DirectedRel rel);

	/**
	 * 
	 * @return the next level of methods in the callee hierarchy, ie all the
	 * given method's invoked methods so the calls they make can now be added 
	 */
	public abstract List<EditPart> getNextLevelOfMethods(EditPart methodEP);

	@Override
	public void run() {
		showLifeCycle(startingMethodEP, new DirectedRel(RJCore.calls, true));
	}

	private void showLifeCycle(EditPart method, DirectedRel rel) {

		ArtifactFragment methodModel = (ArtifactFragment) method.getModel();
		if(isMethodAlreadyDone(methodModel)) return;
		addMethodDone(methodModel);

		String className = methodModel.getParentArt().getArt().queryName(repo);
		String desc = "Incrementally adding callee hierarchy of "
			+startingMethodLabel.trim()+":"+
			"\n\n" +
			(first?"First":"Now")+" will add all calls made directly by "
			+className.trim()+"."+CodeUnit.getLabel(repo, methodModel.getArt(), 
					methodModel.getParentArt().getArt()).trim();

		if (first) {
			first = false;
			// Let user know what is about to happen and give option to cancel.
			// (don't want to show selected method's expansion automatically and
			// only show wizard prompts for subsequent calls because the option
			// has a "...", so the user will expect a popup right from the start.
			// plus if there is only one level of calls with no subsequent calls, 
			// no popup would appear at all, which would seem buggy)
			MessageDialog lifecycleIntroDialog = new MessageDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					actionLbl, 
					null,
					desc,
					MessageDialog.QUESTION, 
					new String[]{"Add","Cancel"}, 
					0);
			lifecycleIntroDialog.open();

			if (lifecycleIntroDialog.getReturnCode()==1) {
				// user chose Cancel
				exit = true;
				return;
			} else if (lifecycleIntroDialog.getReturnCode()==0 ) {
				// user chose Continue.
				// method is the selected method whose nav aid is showing, 
				// so add only the calls visible and enabled in the menu
				addAllCallsAction.run();
			}
		} else if (!exit && !addAll && makesCallNotInDiagram(method, rel)) {
			// show dialog with options if addAll flag has not been set and if this method has invocations to add
			MessageDialog methodLifecycleWizardDialog = new CalleeHierarchyWizardDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
					actionLbl,
					null, 
					desc, 
					MessageDialog.QUESTION, 
					// new String[]{"Add this set ","Skip this set","Add entire hierarchy","Exit completely"}, 
					new String[]{"Add","Skip","Stop"}, 
					0);
			methodLifecycleWizardDialog.open();
			if (methodLifecycleWizardDialog.getReturnCode() == 2 || methodLifecycleWizardDialog.getReturnCode() == -1) {
				exit = true;
				return;
			} else if (methodLifecycleWizardDialog.getReturnCode() == 3 ) {
				addAll = true;
				displayAllCallsMade(method, rel);
			}
			else if (methodLifecycleWizardDialog.getReturnCode() == 0)
				displayAllCallsMade(method, rel);
		} else // do not show wizard and just display calls if addAll has been checked
			displayAllCallsMade(method, rel);

		// continue adding the hierarchy by repeating 
		// for each of the called methods just added
		List<EditPart> nextLevelMethods = getNextLevelOfMethods(method);
		for(EditPart nlm : nextLevelMethods) {
			if (exit) return;
			showLifeCycle(nlm, rel);
		}
	}

}
