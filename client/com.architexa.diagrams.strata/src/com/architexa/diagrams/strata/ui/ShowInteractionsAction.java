package com.architexa.diagrams.strata.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.jdt.Filters;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.strata.parts.StrataArtFragEditPart;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.ui.RSENestedAction;
import com.architexa.org.eclipse.gef.commands.Command;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.ui.actions.SelectionAction;
import com.architexa.store.ReloRdfRepository;

public class ShowInteractionsAction extends SelectionAction implements RSENestedAction{

    public static final String ShowInteractionsAction_Id = "showInteractions";
    public static final String ShowInteractionsAction_Label = "Show Interactions";
    public static final String ShowInteractionsAction_Tooltip = "Show intermediary items connecting the selected items";
    public static final String ShowInteractionsAction_CommandName = "showInteractions";
	private ReloRdfRepository repo;
	private StrataRootEditPart rc;
	
    public ShowInteractionsAction (IWorkbenchPart part) {
		super(part);
	}

	@Override
	protected boolean calculateEnabled() {
		Command cmd = createInteractionsCommand(getSelectedObjects());
		if (cmd == null)
			return false;
		return cmd.canExecute();
	}


	public Command createInteractionsCommand(List<?> selectedObjects) {
		if (selectedObjects.isEmpty()) return null;

		List<StrataArtFragEditPart> selEPList = new ArrayList<StrataArtFragEditPart>();
		List<Artifact>  selArtList = new ArrayList<Artifact>();
		for (Object obj : selectedObjects) {
			if (!(obj instanceof StrataArtFragEditPart)) continue;
			selEPList.add((StrataArtFragEditPart) obj);
			selArtList.add(((StrataArtFragEditPart) obj).getArtFrag().getArt());
		}
		if (selEPList.size() != 2) return null;
		
		StrataEditor editor = (StrataEditor) getWorkbenchPart();
		this.repo = editor.getRootController().getRepo();
		this.rc = editor.getRootController();
		
		
//		List<Map<Artifact,Artifact>> listOfMapRelsToArts = new ArrayList<Map<Artifact,Artifact>>();
		
		List<List<Artifact>> listOfSelArtLists = new ArrayList<List<Artifact>>();
		for (Artifact selArt : selArtList) {
			List<Artifact> allNestedArts = new ArrayList<Artifact>();
			allNestedArts.addAll(queryAllNestedChildren(selArt));
			listOfSelArtLists.add(allNestedArts);
		}
		
		List <Artifact> connectionArts = new ArrayList<Artifact>();
		for (Artifact childArt : listOfSelArtLists.get(0)){
			
			List<Artifact> allRels = new ArrayList<Artifact>();
			
			DirectedRel revRel = DirectedRel.getRev(RJCore.containmentBasedRefType);			  
			
			allRels.addAll(rc.getRootModel().getDepNdx().queryContainedList(childArt, revRel, Filters.getTypeFilter(repo, RJCore.classType)));
		
			DirectedRel fwdRel = DirectedRel.getFwd(RJCore.containmentBasedRefType);			  
			allRels.addAll(rc.getRootModel().getDepNdx().queryContainedList(childArt, fwdRel, Filters.getTypeFilter(repo, RJCore.classType)));

			for (Artifact relArt : allRels) {
				if (listOfSelArtLists.get(1).contains(relArt)) {
					connectionArts.add(relArt);
					connectionArts.add(childArt);
				}
			}
		}
		
		// Rels not added ??
		Map<Artifact, ArtifactFragment> addedArtToAFMap = new HashMap<Artifact, ArtifactFragment>();
		CompoundCommand cc = new CompoundCommand(ShowInteractionsAction_Label);
		for (Artifact art : connectionArts) {
			 StrataArtFragEditPart.addArtAndContainers(cc, addedArtToAFMap, rc, repo, art);
		}
		return cc;	
	}

    private List<Artifact> queryAllNestedChildren(Artifact artifact) {
    	List<Artifact> pckgChildren = artifact.queryPckgDirContainsArtList(repo, DirectedRel.getFwd(RJCore.pckgDirContains));
		if (pckgChildren.size() == 0)
			pckgChildren = artifact.queryArtList(repo, DirectedRel.getFwd(RSECore.contains));
    	
    	for (Artifact art : new ArrayList<Artifact>(pckgChildren)) {
//    		if (pckgChildren.contains(art)) continue;
        	pckgChildren.addAll(queryAllNestedChildren(art));
    	}
    	return pckgChildren;
	}

	@Override
	public void run() {
    	execute(createInteractionsCommand(getSelectedObjects()));
	}

	protected List<StrataArtFragEditPart> getAllArtFragEP() {
        StrataEditor editor = (StrataEditor) getWorkbenchPart();
        return editor.getRootController().getAllArtFragEP();
    }

    @Override
	protected void init() {
		super.init();
		setText(ShowInteractionsAction_Label);
		setToolTipText(ShowInteractionsAction_Tooltip);
		setId(ShowInteractionsAction_Id);
		
		// use images of delete for now
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(
				ISharedImages.IMG_TOOL_DELETE_DISABLED));
		setEnabled(false);
	}
}