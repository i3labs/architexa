/**
 * Copyright (c) 2007-2008 Architexa, Inc
 */ 
package com.architexa.diagrams.strata.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.ParseException;
import org.openrdf.rio.Parser;
import org.openrdf.rio.RdfDocumentWriter;
import org.openrdf.rio.StatementHandler;
import org.openrdf.rio.StatementHandlerException;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.editors.RSEMultiPageEditor;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.BrowseModel;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.model.RootArtifact;
import com.architexa.diagrams.relo.actions.ListEditorInput;
import com.architexa.diagrams.relo.jdt.browse.ClassStrucBrowseModel;
import com.architexa.diagrams.services.PluggableTypes;
import com.architexa.diagrams.strata.StrataPlugin;
import com.architexa.diagrams.strata.cache.DepNdx;
import com.architexa.diagrams.strata.commands.LoadRelsCommand;
import com.architexa.diagrams.strata.model.StrataFactory;
import com.architexa.diagrams.strata.model.StrataRootDoc;
import com.architexa.diagrams.strata.model.policy.ClosedContainerDPolicy;
import com.architexa.diagrams.strata.model.policy.LayersDPolicy;
import com.architexa.diagrams.strata.parts.StrataRootEditPart;
import com.architexa.diagrams.ui.LongCommandStack;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.ui.RSEShareableDiagramEditorInput;
import com.architexa.diagrams.ui.RSETransferDropTargetListener;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.DefaultEditDomain;
import com.architexa.org.eclipse.gef.editparts.ZoomManager;
import com.architexa.org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import com.architexa.org.eclipse.gef.palette.PaletteGroup;
import com.architexa.org.eclipse.gef.palette.PaletteRoot;
import com.architexa.org.eclipse.gef.palette.PaletteSeparator;
import com.architexa.org.eclipse.gef.palette.SelectionToolEntry;
import com.architexa.org.eclipse.gef.palette.ToolEntry;
import com.architexa.org.eclipse.gef.requests.SimpleFactory;
import com.architexa.org.eclipse.gef.ui.actions.ActionRegistry;
import com.architexa.org.eclipse.gef.ui.actions.PrintAction;
import com.architexa.org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import com.architexa.rse.AccountSettings;
import com.architexa.rse.BuildStatus;
import com.architexa.store.PckgDirRepo;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


public class StrataEditor extends RSEEditor {
	public static final Logger logger = StrataPlugin.getLogger(StrataEditor.class);
	public static String COMMENT_LAYER ="Comment Layer";
	public static final String editorId = "com.architexa.diagrams.strata.editor"; 

	static {
    	registerEditorID(editorId);
    }
	
	@Override
	public String getEditorId() {
		return editorId;
	}

	public StrataEditor() {
		DefaultEditDomain defaultEditDomain = new DefaultEditDomain(this);
		defaultEditDomain.setCommandStack(new LongCommandStack("Strata"));
		setEditDomain(defaultEditDomain);
		setDiagramCreator(RSEMultiPageEditor.getDiagramCreator());
		BuildStatus.updateDiagramActionMap("Opened Diagram Created By", RSEMultiPageEditor.getDiagramCreator());
	}

	@Override
	protected FlyoutPreferences getPalettePreferences() {
        return new FlyoutPreferences() {
            int dockLocation = -1;
            public int getDockLocation() {
                return dockLocation;
            }
            public void setDockLocation(int location) {
                dockLocation = location;
            }

            int paletteState = -1;
            public int getPaletteState() {
                return paletteState;
            }
            public void setPaletteState(int state) {
                paletteState = state;
            }

            int paletteWidth = 125;
            public int getPaletteWidth() {
                return paletteWidth;
            }

            public void setPaletteWidth(int width) {
                paletteWidth = width;                
            }};
	}

	private static PaletteRoot paletteRoot;
	
	@Override
	protected PaletteRoot getPaletteRoot() {
		if (paletteRoot == null) {
			paletteRoot = new PaletteRoot();

			PaletteGroup controlGroup = new PaletteGroup("Control Group");

			ToolEntry tool = new SelectionToolEntry();
			controlGroup.add(tool);
			paletteRoot.setDefaultEntry(tool);
			
//			tool = new MarqueeToolEntry();
//			controlGroup.add(tool);			

//			ImageDescriptor img = StrataPlugin.getImageDescriptor("link.gif");
//            ToolEntry ccte = new ConnectionCreationToolEntry(
//                    "Add Relationship", 
//                    "Create a named link",
//                    new SimpleFactory(NamedRel.class), 
//                    img, img);
//            ccte.setToolClass(DefaultAfterConnectionCreationTool.class);
//			controlGroup.add(ccte);
			
			controlGroup.add(new PaletteSeparator("Entities"));
			
			// import image does not work properly due to strata centering
			controlGroup.add(getImportImagePaletteEntry());
			
			controlGroup.add(getActorPaletteEntry());
			controlGroup.add(getDatabasePaletteEntry());
			controlGroup.add(getCommentPaletteEntry());
			
			controlGroup.add(new PaletteSeparator("Code Components"));

			String packageName = "newPackage";
			String className = "NewClass";
			
			SimpleFactory factory = new CreationWithParamsFactory(packageName);
			ImageDescriptor img = StrataPlugin.getImageDescriptor("icons_palette/palette_strata_package.PNG");
			controlGroup.add(new CombinedTemplateCreationEntry(
					"Package", 
					"Press here, then click in the diagram to add a package",
					factory,
					factory,
					img, 
					img));
			
			factory = new CreationWithParamsFactory(packageName+"$"+className);
			img = StrataPlugin.getImageDescriptor("icons_palette/palette_strata_class.PNG");
			controlGroup.add(new CombinedTemplateCreationEntry(
					"Class", 
					"Press here, then click in the diagram to add a class",
					factory,
					factory,
					img, 
					img));
			
			controlGroup.add(new PaletteSeparator("Relationships"));
			
			controlGroup.add(getGeneralConnectionPaletteEntry());
			
			// ANCHORED CONNECTIONS: Need to implement support for move/nesting + other commands (anywhere a ep moves...)
//			img = StrataPlugin.getImageDescriptor("link.gif");
//            ToolEntry acte = new ConnectionCreationToolEntry(
//                    "Add Anchor Relationship", 
//                    "Link two items so they move together",
//                    new SimpleFactory(AnchorRel.class), 
//                    img, img);
//            acte.setToolClass(DefaultAfterConnectionCreationTool.class);
//			controlGroup.add(acte);
			
			paletteRoot.add(controlGroup);
		}
		return paletteRoot;
	}


    @Override
	public void writeFile(RdfDocumentWriter rdfWriter, Resource rootAFRes) throws IOException {
    	StrataRootEditPart rc = (StrataRootEditPart) getGraphicalViewer().getRootEditPart().getContents();
    	BrowseModel bm = getRootArt().getBrowseModel();
    	StrataViewEditorCommon.writeFile(this, rc, bm, browseModel, rootModel, rdfWriter, rootAFRes);
	}

	@Override
	public IFile getNewSaveFile() {
		StrataRootEditPart scep = (StrataRootEditPart) getGraphicalViewer().getRootEditPart().getContents();
		return StrataViewEditorCommon.getNewSaveFile(this, scep);
	}
	
	private StrataRootDoc rootModel = null;

	@Override
	public RootArtifact getRootArt() {
		if (rootModel == null)
			rootModel = OpenStrata.getStrataDoc();
		return rootModel;
	}
	
	public static boolean doNotBreakLoadedAFs;
	public static final URI browseModel = RSECore.createRseUri("core#browseModel");
	/**
	 * @see edu.mit.csail.pdeConsole.part.EditorPart#setInput(edu.mit.csail.pdeConsole.IEditorInput)
	 */
	@Override
    protected void setInput(IEditorInput input) {
		super.setInput(input);
		doNotBreakLoadedAFs = false;

		//TODO Should only receive RSEShareableEditorInput. Remove others
		if (input instanceof ListEditorInput) {
			BrowseModel bm = ((ListEditorInput) input).browseModel;
			getRootArt().setBrowseModel(bm);
			// need to setRoot for controller when adding mult items to a empty
			// diagram
			bm.setRootArt(getRootArt());
			//if (((ListEditorInput)input).list.size()<=0) return;
		} else if (input instanceof StrataEditorInput) {		
			if (((StrataEditorInput) input).getStrataRootDoc() != null)
				rootModel = ((StrataEditorInput) input).getStrataRootDoc();
			BrowseModel bm = new ClassStrucBrowseModel();
			getRootArt().setBrowseModel(bm);
			bm.setRootArt(getRootArt());
			setupDepNdx();
		} else if (input instanceof RSEShareableDiagramEditorInput) {
			RSEShareableDiagramEditorInput rseInput = (RSEShareableDiagramEditorInput)input;
			setMemRepo(rseInput.getMemRepo());
			//setMemRepo(StoreUtil.getMemRepository());
			//try {
			//	readFile(rseInput.getName(), rseInput.getInputStream(), getMemRepo());
			//} catch (Exception e) {
			//	e.printStackTrace();
			//}
			setupRoot();
			doNotBreakLoadedAFs  = true; 
		} else {
			checkEditorInputForOutsideWorkspace(input);
		}
	}
	
	protected void setupDepNdx() {
		super.setupRoot();
		
		// need to set depMetaNdx?
		if (getEditorInput() instanceof RSEShareableDiagramEditorInput) {
			IPath path = ((RSEShareableDiagramEditorInput)getEditorInput()).wkspcPath;
			PckgDirRepo pdr = PckgDirRepo.getPDR(getRootArt().getRepo(), path, RJCore.pckgDirContains);
			if (path != null) {
				rootModel.setPckgDirRepo(pdr);
				rootModel.setDepNdx(new DepNdx(getRootArt().getRepo(), path, pdr));
			}
		}
	}
	@Override
	protected void setupRoot() {
    	BrowseModel bm = (BrowseModel) StoreUtil.loadClass(getMemRepo(), RSECore.docRoot, browseModel);
    	getRootArt().setBrowseModel(bm);
		bm.setRootArt(getRootArt());
		getRootArt().getRepo().setFileRepo(getMemRepo());

		setupDepNdx();
//		super.setupRoot();
//		
//		// need to set depMetaNdx?
//		if (getEditorInput() instanceof RSEShareableDiagramEditorInput) {
//			IPath path = ((RSEShareableDiagramEditorInput)getEditorInput()).wkspcPath;
//			PckgDirRepo pdr = PckgDirRepo.getPDR(rootModel.getRepo(), path);
//			if (path != null) {
//				rootModel.setPckgDirRepo(pdr);
//				rootModel.setDepNdx(new DepNdx(rootModel.getRepo(), path, pdr));
//			}
//		}

	}
	
	public static void loadModel(ReloRdfRepository inputRDF, StrataRootDoc rootContent, StrataEditor editor) {
		// set cache (now done above)
		// ((StoreUnionRepository)rootContent.getRepo()).setCacheRepo(StoreUtil.getMemRepository());
		// make sure root policy flags are correct
		((ClosedContainerDPolicy) rootContent.getDiagramPolicy(ClosedContainerDPolicy.DefaultKey)).setShowingChildren(true);
		((LayersDPolicy) rootContent.getDiagramPolicy(LayersDPolicy.DefaultKey)).layersNeedBuilding = false;
		// add children and comments from file/mem repo to model hierarchy 
		if (inputRDF != null) {
		    Map<Resource,ArtifactFragment> instanceRes2AFMap = new HashMap<Resource,ArtifactFragment> ();
		    Map<Resource, ArtifactRel> instanceRes2ARMap = new HashMap<Resource,ArtifactRel> ();
		    ArtifactFragment.readChildren(rootContent, inputRDF, instanceRes2AFMap, rootContent.getInstanceRes(), rootContent);
	        addComments(inputRDF, rootContent.getInstanceRes() ,rootContent, instanceRes2AFMap);
	        addCommentRels(rootContent, inputRDF, instanceRes2AFMap, instanceRes2ARMap);
		}
		// use long command to load relationships since we are ignoring ones from the file
		ArrayList<ArtifactFragment> allChildren = new ArrayList<ArtifactFragment>();
		allChildren  = getAllChildren(rootContent.getRootArt());
		LoadRelsCommand loadRelsCmd = new LoadRelsCommand(rootContent, allChildren, inputRDF);
		editor.getEditDomain().getCommandStack().execute(loadRelsCmd);
		editor.clearDirtyFlag(); 
	}

	private static void addCommentRels(StrataRootDoc rootContent, ReloRdfRepository inputRDF, Map<Resource, ArtifactFragment> instanceRes2AFMap, Map<Resource, ArtifactRel> instanceRes2ARMap) {
		StatementIterator si = inputRDF.getStatements(null, inputRDF.rdfType, RSECore.link);
		while (si.hasNext()) {
			Resource viewRes = si.next().getSubject();
	        
	        URI linkRes = (URI) inputRDF.getStatement(viewRes, RSECore.model, null).getObject();
	        ArtifactRel artRel = PluggableTypes.getAR(linkRes);
	        if (artRel instanceof NamedRel)
	        	ArtifactRel.readRDF(rootContent, inputRDF, viewRes, instanceRes2AFMap, instanceRes2ARMap);
		}
		si.close();
	}

	public static void readFileIntoRepo(IFile file, final ReloRdfRepository tgtRepo) throws IOException, ParseException, StatementHandlerException, CoreException {
		BufferedReader in = new BufferedReader(new InputStreamReader(file.getContents(/*force*/true)));
        Parser parser = StoreUtil.getRDFParser(tgtRepo);
		parser.setStatementHandler(new StatementHandler() {
            public void handleStatement(Resource subj, URI pred, Value obj) throws StatementHandlerException {
            	tgtRepo.addStatement(subj, pred, obj);
            	logger.info("Added: " + subj + " --[" + pred + "]--> " + obj);
            }});
		tgtRepo.startTransaction();
		parser.parse(in, ReloRdfRepository.atxaRdfNamespace);
		tgtRepo.commitTransaction();
	}

	/**
	 * @see com.architexa.org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
	 */
	@Override
	public void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		
		StrataViewEditorCommon.configureGraphicalViewer(this, getGraphicalViewer(), getActionRegistry(), null, graphicalViewManager);

	}

    @SuppressWarnings("unchecked")
	@Override
	protected void createActions() {
		super.createActions();

		ActionRegistry registry = getActionRegistry();
		IAction action;

		// below copied from super class
		/*
		action = new UndoAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());
		
		action = new RedoAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());
		
		action = new SelectAllAction(this);
		registry.registerAction(action);
		
		action = new DeleteAction((IWorkbenchPart)this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
		
		action = new SaveAction(this);
		registry.registerAction(action);
		getPropertyActions().add(action.getId());
		*/
		
		registry.registerAction(new PrintAction(this));

//		registry.registerAction(new JDTLinkedTracker.LinkedTrackerAction(this));
		
        action = new BreakAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new FocusAction(this, rootModel);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

        action = new ReLayerAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

		action = new OpenInJDTEditorAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
		
		// Experimental Actions
		if (AccountSettings.EXPERIMENTAL_MODE) {
			action = new CollapseAllPackagesAction(this);
			registry.registerAction(action);
			getSelectionActions().add(action.getId());
			
			action = new ReduceAction(this);
			registry.registerAction(action);
			getSelectionActions().add(action.getId());
			
			action = new ShowDependersAndDependeesAction(this);
			registry.registerAction(action);
			getSelectionActions().add(action.getId());
			
			action = new ShowInteractionsAction(this);
			registry.registerAction(action);
			getSelectionActions().add(action.getId());
		}
	}

	@Override
	public void initializeGraphicalViewer() {
    	super.initializeGraphicalViewer();
    	getGraphicalViewer().setContents(getRootArt());
		
    	TransferDropTargetListener dragSrc = new RSETransferDropTargetListener(this, getGraphicalViewer());
		getGraphicalViewer().addDropTargetListener(dragSrc);
	}

	@Override
	public boolean canDropOnEditor(IStructuredSelection sel) {
		return StrataViewEditorCommon.canDropOnEditor(sel);
	}

	// @tag unify-core
    public StrataRootEditPart getRootController() {
        return (StrataRootEditPart) this.getGraphicalViewer().getEditPartRegistry().get(getRootArt());
    }

	@SuppressWarnings("rawtypes")
	@Override
    public Object getAdapter(Class type){
		if (type == ZoomManager.class)
			return getGraphicalViewer().getProperty(ZoomManager.class.toString());

		return super.getAdapter(type);
	}

	public static ArrayList<ArtifactFragment> getAllChildren(ArtifactFragment afParent) {
		ArrayList<ArtifactFragment> theseChildren = new ArrayList<ArtifactFragment>();
		theseChildren.add(afParent);
		for (ArtifactFragment af : afParent.getShownChildren()) {
			theseChildren.addAll(getAllChildren(af));
		}
		return theseChildren;
	}

	public static void addComments(ReloRdfRepository repo, Resource res , StrataRootDoc rootContent, Map<Resource, ArtifactFragment> instanceRes2AFMap) {
		
		StatementIterator commentToAddIter = repo.getStatements(res, RSECore.contains, null);
		while(commentToAddIter.hasNext()){ 
			
			Value obj = commentToAddIter.next().getObject();
			if(!(obj instanceof Resource) || repo.getStatement((Resource)obj, RSECore.model, null)==null) continue;
			
			Resource resource = (Resource) obj;
			Resource modelRes = (Resource) repo.getStatement(resource, RSECore.model, null).getObject();
			Value type = repo.getStatement(modelRes, repo.rdfType, null).getObject();
			if(RSECore.commentType.equals(type)){
				String text=repo.getStatement(resource, Comment.commentTxt, null).getObject().toString();
				String posX=repo.getStatement(resource, RSECore.createRseUri("core#posX"), null).getObject().toString();
				String posY=repo.getStatement(resource, RSECore.createRseUri("core#posY"), null).getObject().toString();
				int x=Integer.parseInt(posX);
				int y=Integer.parseInt(posY);
				Point createLoc=new Point(x,y);
				
				Comment com = new Comment(createLoc);
				Comment.initComment(com);
				StrataFactory.initAF(com); // do we need it here
				com.setAnnoLabelText(text);
				rootContent.addComment(com);
				instanceRes2AFMap.put(resource, com);
			}
		}
	}

	private void setMemRepo(ReloRdfRepository memRepo) {
		this.memRepo = memRepo;
	}

	public ReloRdfRepository getMemRepo() {
		return memRepo;
	}

	//Currently only for Chrono
	public void addRemoveImageExportCustomizations(boolean status) {}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return StrataPlugin.getImageDescriptor("/icons/office-document.png");
	}

	
	public class CreationWithParamsFactory extends SimpleFactory {
		String fragName;
		public CreationWithParamsFactory(String fragName) {
			super(null);
			this.fragName = fragName;
		}

		@Override
		public Object getNewObject() {
	    	BrowseModel bm = getRootArt().getBrowseModel();
			try {
				Class<?> type = bm.getUserCreatedFragmentClass();
				Constructor<?> constructor = type.getDeclaredConstructor(BrowseModel.class, String.class);
				constructor.setAccessible(true);
				return constructor.newInstance(bm, fragName);
			} catch (Exception exc) {
				logger.error("Unable to create new instance of " + bm.getUserCreatedFragmentClass(), exc);
				return null;
			}
		}
	}
	
}
