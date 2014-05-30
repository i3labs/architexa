package com.architexa.diagrams.generate.team;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.compare.contentmergeviewer.ITokenComparator;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.DocLineComparator;
import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.openrdf.model.Resource;
import org.openrdf.sesame.sail.StatementIterator;

import com.architexa.collab.UIUtils;
import com.architexa.collab.proxy.PluginUtils;
import com.architexa.diagrams.RSECore;
import com.architexa.diagrams.chrono.util.MethodDeclarationFinder;
import com.architexa.diagrams.commands.AddCommentCommand;
import com.architexa.diagrams.commands.ArtifactRelCreationCommand;
import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.generate.GenerateUtil;
import com.architexa.diagrams.jdt.Invocation;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.RJMapFromId;
import com.architexa.diagrams.jdt.actions.OpenVizAction;
import com.architexa.diagrams.jdt.builder.asm.AsmUtil;
import com.architexa.diagrams.jdt.compat.ASTUtil;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.jdt.utils.ITFBMUtil;
import com.architexa.diagrams.jdt.utils.MethodInvocationFinder;
import com.architexa.diagrams.model.Artifact;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.diagrams.model.Comment;
import com.architexa.diagrams.model.DirectedRel;
import com.architexa.diagrams.model.NamedRel;
import com.architexa.diagrams.ui.PluggableDiagramsSupport.IRSEDiagramEngine;
import com.architexa.diagrams.ui.RSEEditor;
import com.architexa.diagrams.ui.SelectableAction;
import com.architexa.diagrams.utils.RootEditPartUtils;
import com.architexa.org.eclipse.draw2d.geometry.Point;
import com.architexa.org.eclipse.gef.commands.CompoundCommand;
import com.architexa.org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


/**
 * 
 * @author Elizabeth L. Murnane
 *
 */
public abstract class UncommittedChangesDiagramGenerator extends Action {

	private static final String versionSep = "############################";
	private static final String fileSep = "@@@@@@@@@@@@@@@@@@@@@@@@@@@";

	public abstract boolean isManaged(Object resource);
	public abstract boolean isChanged(IResource resource);
	public abstract Object getLatestRevision(Object file);
	public abstract InputStream getInputStreamFromDoc(Object revision, IProgressMonitor monitor) throws CoreException;
	public abstract String getNameOfRevisionFile(Object file);
	public abstract String getCompleteFilePath(Object file);
	
	private static final Logger logger = GeneratePlugin.getLogger(UncommittedChangesDiagramGenerator.class);

	private List<IJavaElement> selectedElements;
	private IRSEDiagramEngine diagramEngine;

	public UncommittedChangesDiagramGenerator() {
	}

	public UncommittedChangesDiagramGenerator(List<IJavaElement> selectedElements, IRSEDiagramEngine diagramEngine) {
		init(selectedElements,diagramEngine);
	}
	
	public UncommittedChangesDiagramGenerator init(List<IJavaElement> selectedElements, IRSEDiagramEngine diagramEngine) {
		setText(diagramEngine.diagramType());
		setImageDescriptor(diagramEngine.getImageDescriptor());

		this.selectedElements = selectedElements;
		this.diagramEngine = diagramEngine;

		// Set this action's enabled state based on the current selection
//		try {
//			if (selectedElement == null) throw new Exception("\nSelected element is null.");
//			SelectableAction openVizAction = diagramEngine.getOpenActionClass().newInstance();
//			openVizAction.selectionChanged(this, new StructuredSelection(selectedElement));
//		} catch (Exception e) {
//			logger.error("Unexpected exception while setting enable " +
//					"state of uncommitted changes menu action. ", e);
//		}
		return this;
	}
	
	@Override
	public void run() {
		List<Object> filesWithChanges = new ArrayList<Object>();
		for (IJavaElement selectedElement : selectedElements) {
			if(selectedElement==null)
				continue;
			 filesWithChanges.addAll(getFilesInSelectionWithUncommittedChanges(selectedElement.getResource()));
		}
		if(filesWithChanges.size()==0) {
			UIUtils.openErrorPromptDialog("View Uncommitted Changes", "There are no outgoing changes in the selection.");
			return;
		}
		createDiagramOfChanges(filesWithChanges, diagramEngine);
	}

	private List<IFile> getFilesInSelectionWithUncommittedChanges(IResource selectedRes) {

		if(!isChanged(selectedRes)) return new ArrayList<IFile>(); // no uncommitted changes in the selected element

		final List<IFile> filesWithChanges = new ArrayList<IFile>();
		if(selectedRes instanceof IFile) {
			filesWithChanges.add((IFile)selectedRes);
		} else if(selectedRes instanceof IProject || selectedRes instanceof IFolder) {
			try {
				((IContainer)selectedRes).accept(new IResourceVisitor() {
					public boolean visit(IResource res) {

						// If res contains no uncommitted changes, 
						// can skip it and everything it contains
						if(!isChanged(res)) return false; 

						// If res not a java file, visit its members
						if(!res.exists() || 
								!(res instanceof IFile) || 
								!"java".equals(res.getFileExtension())) return true;

						filesWithChanges.add((IFile)res);
						return false;
					}
				});
			} catch (CoreException e) {
				logger.error("Unexpected exception while finding files " +
						"with uncommitted changes. ", e);
			}
		}
		return filesWithChanges;
	}
	
	/**
	 * For each file in filesWithChanges, find the differences
	 * between the file and its previous revision and open those
	 * differences in the type of diagram corresponding to diagramEngine  
	 * @param filesWithChanges
	 * @param diagramEngine
	 */
	public void createDiagramOfChanges(
			final List<Object> filesWithChanges,
			final IRSEDiagramEngine diagramEngine) {
		Map<Object,Object> map = new HashMap<Object,Object>();
		for (Object obj : filesWithChanges) {
			map.put(obj, null);
		}
		createDiagramOfChanges(null, map, diagramEngine);
	}

	public Map getChangeMap(List<ChangeSet> selectedChangeSetList, Map<Object, Object> changedFilesToComparedRevisions) {

		// If not opening from the history view then the passes in map will be enough
		if (selectedChangeSetList == null) return changedFilesToComparedRevisions;
		
		boolean singleRevision = false;
		Object[] selectedClasses = null;
		List treeList = new ArrayList();
		if (selectedChangeSetList.size() == 1
				&& selectedChangeSetList.get(0).getAffectedResources().length == 1) {
			singleRevision = true;
			selectedClasses = selectedChangeSetList.get(0).getAffectedResources();
		}

		if (!singleRevision) {
			List allTreeList = new ArrayList();
			for (ChangeSet selectedChangeSet : selectedChangeSetList) {
				TreeElement root = new TreeElement(
						selectedChangeSet.getSelectedRevisionNumber(), null,
						null, selectedChangeSet);
				Map map = selectedChangeSet
						.getAffectedResourcesNamesToLogEntrMap();
				for (Object obj : map.keySet()) {
					TreeElement child = new TreeElement(
							map.get(obj).toString(), root, selectedChangeSet,
							obj);
					root.addChild(child);
					allTreeList.add(child);
				}
				treeList.add(root);
				allTreeList.add(root);
			}

			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			ChangeSetSelectionDialog dialog = new ChangeSetSelectionDialog(
					shell, new DirectoryTreeLabelProvider(),
					new TreeContentProvider());
			dialog.setTitle("Select Affected Files");
			dialog.setInput(treeList);
			dialog.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_DEFAULT));
			dialog.setMessage("Selected change set(s) includes multiple affected files."
					+ "\nPlease select which files should be included in the diagram." +
							"\n(Note: Diagram generation may take some time if many files are selected).");
			dialog.setExpandedElements(treeList.toArray());
			dialog.setInitialElementSelections(allTreeList);
			dialog.open();
			selectedClasses = dialog.getResult();
		}

		final Object[] useSelectedClasses = selectedClasses;

		Map<Object, Object> changedFilesToCompareAndOpen = changedFilesToComparedRevisions;
		// if we are opening a revision, open items selected in the dialog and
		// add them to the map
		if (useSelectedClasses != null && useSelectedClasses.length != 0) {
			if (!singleRevision) {
				changedFilesToCompareAndOpen = openChangedDocsFromDialog(
						new NullProgressMonitor(), useSelectedClasses);

				if (changedFilesToCompareAndOpen == null
						|| changedFilesToCompareAndOpen.size() == 0)
					logger.warn("Unable to open diagram because no file from change set selected.");
			} else {
				Map<Object, Object> singleResourceCompareMap = new HashMap<Object, Object>();
				Object sel = useSelectedClasses[0];
				selectedChangeSetList.get(0).addToMap(sel,
						singleResourceCompareMap);
				changedFilesToCompareAndOpen = singleResourceCompareMap;
			}
		}
		return changedFilesToCompareAndOpen;
	}

	/* 
	 * If revisionToCompareTo is non-null, the list filesWithChanges
	 * should only contain one element. When the list has more than
	 * one element, revisionToCompareTo should be null and each file
	 * in the list will be compared to its previous revision 
	 * 
	 * selectedChangeSet will only be non-null for RevisionView / history diagrams
	 * In this case we open the dialog and use the results to create the map
	 */
	public void createDiagramOfChanges(final List<ChangeSet> selectedChangeSetList, 
		final Map<Object, Object> changedFilesToComparedRevisions,
		final IRSEDiagramEngine diagramEngine) {
		try {
			final Map changedFilesToCompareAndOpen = getChangeMap(selectedChangeSetList, changedFilesToComparedRevisions);
			Job uncommitedDiagramJob = new Job(
					"Generating diagram based on team history...") {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					// Retrieve changes within progress monitor
					if (changedFilesToCompareAndOpen == null) return Status.CANCEL_STATUS;
					monitor.beginTask("Analyzing changes from repository..", changedFilesToCompareAndOpen.size()*5);
					final Map<CodeUnit, Comment> changeToAssocComment = new HashMap<CodeUnit, Comment>();
					final List<CodeUnit> toAddToDiagram = getChangesToAddToDiagram(
							changedFilesToCompareAndOpen, changeToAssocComment, diagramEngine, monitor);
					monitor.done();
					if(toAddToDiagram.size()==0) {
						com.architexa.collab.UIUtils.openErrorPromptDialog("View Uncommitted Changes", "There are no outgoing changes in the selection.");
						return Status.CANCEL_STATUS;
					}
					
					// Open Diagram in UI thread
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							findAndOpenChanges(toAddToDiagram, changeToAssocComment, diagramEngine);
						}	
					});
					
					return Status.OK_STATUS;
				}
			};
			uncommitedDiagramJob.setUser(true);
			uncommitedDiagramJob.setSystem(false);
			uncommitedDiagramJob.setPriority(Job.DECORATE);
			uncommitedDiagramJob.schedule();
			
		} catch (Exception e) {
			logger.error("Unexpected exception while generating diagram based on team history. ", e);
		}
	}
	
	protected Map<Object, Object> openChangedDocsFromDialog(IProgressMonitor monitor, Object[] selections) {
			if(selections==null || selections.length==0);

			// To determine the changes made in the selected revision, 
			// we need to compare the selected revision with the previous
			// one before it, so getting that previous revision and putting
			// it in the map of selected res -> previous version res
			Map<Object, Object> selectedResToPreviousVersionOfRes = new HashMap<Object, Object>();
			for(Object sel : selections) {
				ChangeSet changeSet = ((TreeElement)sel).getChangeSetParent();
				Object logPath = ((TreeElement)sel).getLogEntry();
				if (changeSet == null) continue;
				changeSet.addToMap(logPath, selectedResToPreviousVersionOfRes);
				monitor.worked(1);
			}
			return selectedResToPreviousVersionOfRes;	
	}
	
	protected List<CodeUnit> getChangesToAddToDiagram(Map<Object, Object> changedFilesToComparedRevisions, 	
			Map<CodeUnit, Comment> changeToAssocComment, IRSEDiagramEngine diagramEngine, IProgressMonitor monitor) {
		List<CodeUnit> createdFrags = new ArrayList<CodeUnit>();
		Map<CodeUnit, Position> createdFragPositions = new HashMap<CodeUnit, Position>();
		
		// Find all the changes in the selection, create ArtifactFragments for
		// them, and add the frags to the created frag list. Store the code positions
		// of the changes and use them to find related comments. 
		findChangesAndCreateFrags(
				changedFilesToComparedRevisions,
				createdFrags, createdFragPositions, 
				changeToAssocComment, monitor);

		// Open the created frags in a new diagram
		List<ArtifactFragment> toAddToDiagram = new ArrayList<ArtifactFragment>();
		for(CodeUnit createdFrag : createdFrags) {
			// Only sending top level types to avoid any diagram duplicates.
			// Any created frag that is not a top level type (i.e., "a member") 
			// will be a child of a created frag that is a top level type, so
			// there is no need to send them separately.
			if(createdFrag.getParentArt()==null) toAddToDiagram.add(createdFrag);
		}
		return createdFrags;
	}


	private void findAndOpenChanges(List<?> toAddToDiagram, Map<CodeUnit, Comment> changeToAssocComment, IRSEDiagramEngine diagramEngine) {

		// First make sure we'll be able to actually open the changes in a diagram
		OpenVizAction openVizAction = null;
		try {
			SelectableAction action = diagramEngine.getOpenActionClass().newInstance();
			if(action instanceof OpenVizAction) openVizAction = (OpenVizAction)action;
		} catch (IllegalAccessException e) {
			logger.error("Unexpected exception while instantiating class " + diagramEngine.getOpenActionClass(), e);
		} catch (InstantiationException e) {
			logger.error("Unexpected exception while instantiating class " + diagramEngine.getOpenActionClass(), e);
		}
		if(openVizAction==null) return;

		
		try {
			// Open viz
			IWorkbenchWindow activeWorkbenchWindow = GeneratePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();	
			openVizAction.openViz(activeWorkbenchWindow, toAddToDiagram, lToRDocMap, lToPathMap, docBuff);
			lToRDocMap.clear();
			lToPathMap.clear();
			// Add related comments to viz
			IEditorPart activeEditor = (IEditorPart) RootEditPartUtils.getEditorFromRSEMultiPageEditor(activeWorkbenchWindow.getActivePage().getActiveEditor()); 
			if (activeEditor instanceof RSEEditor) 
				openCommentsRelatedToChanges((RSEEditor)activeEditor, changeToAssocComment);
			
		} catch (Exception e) {
			logger.error("Unexpected exception while opening diagram based on team history. ", e);
		}
	}

	private void openCommentsRelatedToChanges(RSEEditor diagramEditor, Map<CodeUnit, Comment> changeToAssocComment) {
		AbstractGraphicalEditPart root = diagramEditor.getRootController();
		if(root==null) return;

		Object rootArt = root.getModel();
		if(!(rootArt instanceof ArtifactFragment)) return;

		CompoundCommand addCommentsCmd = new CompoundCommand();

		for(CodeUnit change : changeToAssocComment.keySet()) {

			Comment commentModel = changeToAssocComment.get(change);
			com.architexa.diagrams.model.Comment.initComment(commentModel);

			Point changeFragLoc = new Point(100, 100); //TODO get actual location of changed frag

			AddCommentCommand addComment = new AddCommentCommand((ArtifactFragment)rootArt, commentModel, changeFragLoc);
			addCommentsCmd.add(addComment);

			// Add connection from comment to frag
			NamedRel namedRel = new NamedRel();
			ArtifactRelCreationCommand addRel = new ArtifactRelCreationCommand(namedRel);
			addRel.setSourceAF(commentModel, changeFragLoc);
			addRel.setTargetAF(change, changeFragLoc);
			addCommentsCmd.add(addRel);
			commentModel.setAnchored(true);
		}
		root.getViewer().getEditDomain().getCommandStack().execute(addCommentsCmd);
	}

	Map<IDocument, IDocument> lToRDocMap = new HashMap<IDocument, IDocument>();
	Map<IDocument, String> lToPathMap = new HashMap<IDocument, String>();
	StringBuffer docBuff = new StringBuffer();
//	String versionSep = "//$$$$$$$$//";
//	String fileSep = "//********//";
//	StringBuffer rDocBuff = new StringBuffer();
	private void findChangesAndCreateFrags(Map<Object, Object> filesWithChangesToRevisionMap,
			List<CodeUnit> createdFrags, Map<CodeUnit, Position> createdFragPositions, 
			Map<CodeUnit, Comment> changeToAssocComment, IProgressMonitor monitor) {
		for(Object fileWithChanges : filesWithChangesToRevisionMap.keySet()) {
			String fileName = getNameOfRevisionFile(fileWithChanges).replace(".java", "");
			if (monitor.isCanceled()) return;
			monitor.subTask("Comparing: " + fileName);
//			monitor.worked(1);

			Object revisionToCompareTo = filesWithChangesToRevisionMap.get(fileWithChanges);

			// If not yet checked in, compare to an empty revision so
			// that everything will be considered an uncommitted change
			if(revisionToCompareTo==null && !isManaged(fileWithChanges)) 
				revisionToCompareTo = new EmptyRevision();

			// If the given revision is null, use its latest revision
			Object revision = revisionToCompareTo != null ? revisionToCompareTo : getLatestRevision(fileWithChanges);
			if (revision == null) {
				logger.error("Could not get remote file for "+fileName+". Please check internet connection.");
				continue;
			}
			
			
			
			// Get the IDocuments for the file with changes
			// and the revision that file is being compared to
//			IDocument lDoc = getRevisionFileDocument(fileWithChanges, monitor);
			IDocument lDoc = getRevisionFileDocument(fileWithChanges, new SubProgressMonitor(monitor, 1));
			if (lDoc == null && fileWithChanges instanceof IFile) lDoc = ITFBMUtil.getCurrentFileDocument((IFile) fileWithChanges);
			
			// No previous revision available for the file
			IDocument rDoc = (revision instanceof EmptyRevision) ? new Document("") : getRevisionFileDocument(revision, monitor);

			// Find positions of changes in the file
			List<Position> positionsOfChanges = getPositionsOfChangesInFile(lDoc, /*fileWithChanges,*/ rDoc,/* revision,*/ monitor);								
			if(positionsOfChanges.size()==0) {
				logger.info("File "+fileName+" contains no changes");
				continue;
			}

			lToRDocMap.put(lDoc, rDoc);
			lToPathMap.put(lDoc, getCompleteFilePath(fileWithChanges));
			
			//Add file name
			docBuff.append(getNameOfRevisionFile(fileWithChanges));
			docBuff.append("\n" + versionSep + "\n");
			docBuff.append(lDoc.get());
			docBuff.append("\n" + versionSep + "\n");
			docBuff.append(rDoc.get());
			docBuff.append("\n" + fileSep + "\n");
//			lDocBuff.append("\n\n=======================\n\n=======================\n\n");
//			rDocBuff.append(rDoc.get());
//			rDocBuff.append("\n\n=======================\n\n=======================\n\n");
			ICompilationUnit fileIJE = null;
			CompilationUnit fileCU = null;
			if(fileWithChanges instanceof IFile) {
				fileIJE = JavaCore.createCompilationUnitFrom((IFile)fileWithChanges);
				ASTUtil.emptyCache(fileIJE);
				fileCU = ASTUtil.getAst(fileIJE);
			} else {
				fileCU = TeamUtil.convertIDocumentToCompilationUnit(lDoc, monitor);
			}

			// If the fileCU happens to be empty, try
			// to use IParent.getChildren() to find it
			try {
				if (fileIJE!=null && (fileCU==null || fileCU.toString().length()==0)) {
					IJavaElement[] members = fileIJE.getChildren();
					for (IJavaElement member : members) {
						if(!(member instanceof IType)) continue;
						ASTNode memberNode = RJCore.getCorrespondingASTNode((IType)member);
						while(memberNode!=null && !(memberNode instanceof CompilationUnit))
							memberNode = memberNode.getParent();
						if(memberNode instanceof CompilationUnit) {
							fileCU = (CompilationUnit) memberNode;
							break;
						}
					}
				}
			} catch (JavaModelException e) {
				logger.error("Empty File Compilation Unit: ", e);
			}
			
			if(fileCU==null) {
				logger.error("Could not get CompilationUnit for file with changes "+fileName);
				continue;
			}

			// Find changed members (fields, methods,
			// inner classes) and calls at those positions
			final List<ASTNode> members = new ArrayList<ASTNode>();
			// use string to invocation map so we can prevent duplicates from
			// being added even if they are different instances
			Map<String, Invocation> invocationsMap = new HashMap<String, Invocation>();
			MethodInvocationFinder invocationFinder = new MethodInvocationFinder(fileCU);
			for(final Position position : positionsOfChanges) {

				final int positionStart = position.getOffset();
				final int positionEnd = positionStart+position.getLength();

				// Find changed fields, methods, and inner classes
				fileCU.accept(new ASTVisitor() {
					public void testNodeWithinChangedPos(ASTNode node) {
						int nodeStart = node.getStartPosition();
						int nodeEnd = nodeStart+node.getLength();
						// node is a change if it's within position of a change
						if(positionStart<=nodeStart &&
								nodeEnd<=positionEnd) members.add(node);
						// add node if it contains a change, for example
						// the following test will return true for the 
						// change boolean b = true; -> boolean b = false;
						else if(nodeStart<=positionStart &&
								positionEnd<=nodeEnd) members.add(node);
					}
					@Override
					public boolean visit(FieldDeclaration node) {
						testNodeWithinChangedPos(node); return false;
					}
					@Override
					public boolean visit(MethodDeclaration node) {
						testNodeWithinChangedPos(node); return false;
					}
					@Override
					public boolean visit(TypeDeclaration node) {
						testNodeWithinChangedPos(node); return true;
						// return true because if node is an inner class that has
						// changed, want to also visit its members so that ones 
						// that have changed can also be added in the diagram
					}
				});

				// Find changed method calls
				for(int i=position.getOffset(); i<=position.getOffset()+position.getLength(); i++) {
					Invocation invocation = invocationFinder.findInvocation(i); 
					// only add an invocation once, do not want duplicate methods being added
					if(invocation!=null && !invocationsMap.keySet().contains(invocation.toString())) {
						invocationsMap.put(invocation.toString(), invocation);
					}
				}
			}
			ArrayList<Invocation> invocations = new ArrayList<Invocation>(invocationsMap.values());
			
			// Create ArtifactFragments for the member and method call changes
			createFrags(fileName, fileIJE, fileCU, invocations, members, createdFrags, createdFragPositions);
			
			// Find comments associated with these changes
			createComments(fileCU, lDoc, invocations, members, createdFrags, createdFragPositions, changeToAssocComment);
		}
	}

	List<Position> getPositionsOfChangesInFile(
			IDocument lDoc, IDocument rDoc,
			IProgressMonitor monitor) {
		List<Position> positionsOfChanges = new ArrayList<Position>();

		if(rDoc==null) {
			//Unversioned, so everything in file is an uncommitted change
			Position entireFilePos = new Position(0, lDoc.getLength());
			positionsOfChanges.add(entireFilePos);
			return positionsOfChanges;
		} 

		DocLineComparator lComparator = new DocLineComparator(lDoc, null, true);
		final DocLineComparator rComparator = new DocLineComparator(rDoc, null, true);
		RangeDifference[] differences = RangeDifferencer.findDifferences(lComparator, rComparator);
		
		if(differences.length==0) return positionsOfChanges;

		for(RangeDifference difference : differences) {
			
			Position currentDiffPos = extract2Pos(lDoc, lComparator, difference.leftStart(), difference.leftLength());
			
			/*LineInformation lineInfo= LineInformation.create(lDoc);
			int lineWithDiffs = lineInfo.getLineOfOffset(currentDiffPos.getOffset());
			List<Position> positionsOfDiffs = simpleTokenDiff(currentDiffPos, revisionPos, rDoc, revisionDiff, lDoc, currentDiff, lineWithDiffs);
			positionsOfChanges.addAll(positionsOfDiffs);*/
			
			positionsOfChanges.add(currentDiffPos);
		}
		return positionsOfChanges;
	}

	
	
	// Create ArtifactFragments for the given invocations and members.
	// These created frags will be added to the given created frag list.
	private void createFrags(
			String fileName,
			IJavaElement fileWithChanges,
			CompilationUnit fileCU,
			List<Invocation> invocations,
			List<ASTNode> members,
			List<CodeUnit> createdFrags,
			Map<CodeUnit, Position> createdFragPositions) {

		// Create frag for this file
		// containing uncommitted changes
		try {

			// Try to get the resource from the existing code/index, otherwise create one
			String packageName = TeamUtil.getPackageName(fileCU);
			IJavaElement ijeWithChangesQuery = RJMapFromId.idToJdtElement(packageName+"$"+fileName);
			Resource classWithChangesRes = RJCore.jdtElementToResource(StoreUtil.getDefaultStoreRepository(), ijeWithChangesQuery);
			if (ijeWithChangesQuery == null || classWithChangesRes == null)
				classWithChangesRes = getClassRes(fileCU, fileName, StoreUtil.getDefaultStoreRepository());

			CodeUnit fileClassFrag = GenerateUtil.getCodeUnitForRes(classWithChangesRes, null, createdFrags, null);

			// Create the source, target, and call
			// frags for the uncommitted method calls
			createCallFrags(fileWithChanges, fileCU, fileClassFrag, invocations, createdFrags, createdFragPositions);

			// Create frags for new members
			createMemberFrags(members, fileClassFrag, createdFrags, createdFragPositions);
		} catch (Throwable t) {
			logger.error("Error Creating Code Review Diagram", t);
			com.architexa.collab.UIUtils.openErrorPromptDialog("Error Creating Code Review Diagram", "There was a problem creating a diagram for your change set.");
		}

	}

	// Create ArtifactFragments for the source, 
	// target, and connection of method calls.
	// Created frags will be added to the list
	// of created frags.
	private void createCallFrags(IJavaElement fileIJE,
			CompilationUnit fileCU, 
			CodeUnit fileClassFrag, 
			List<Invocation> invocations,
			List<CodeUnit> createdFrags, 
			Map<CodeUnit, Position> createdFragPositions) {
		
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		
		MethodDeclarationFinder declarationFinder = new MethodDeclarationFinder(fileCU);
		for(Invocation invocation : invocations) {
			
			// source of call
			
			MethodDeclaration declaration = declarationFinder.findDeclaration(invocation.getStartPosition());
			IJavaElement declarationElmt = findMethodDeclarationIJE(fileIJE, declaration);
			
			// class containing declaration that makes invocation
			CodeUnit declarationParentFrag = null;
			if (declaration == null || declaration.resolveBinding() != null)
				declarationParentFrag = getDeclaringClassFrag(declaration.resolveBinding(), createdFrags, repo);
			else if(declarationElmt!=null) 
				declarationParentFrag = getDeclaringClassFrag(declarationElmt, createdFrags, repo);
			else declarationParentFrag = getDeclaringClassFrag(fileCU, declaration, createdFrags, repo);
			
			// declaration that makes invocation
			Resource declarationRes = null;
			String methodStr = declaration.getName().getIdentifier();
			if(declarationElmt!=null) declarationRes = RJCore.jdtElementToResource(repo, declarationElmt);
			else {
				StatementIterator si = repo.getStatements(declarationParentFrag.getArt().elementRes, RSECore.contains, null);
				while (si.hasNext()) {
					Resource childRes = (Resource) si.next().getObject();
		            if (childRes.toString().contains(methodStr)) {
		            	declarationRes = childRes;
		            	break;
		            }
				}
				si.close();
			}
			if (declarationRes == null)
				declarationRes = AsmUtil.getMethodRes(methodStr, declarationParentFrag.getArt().elementRes, repo);
			
			CodeUnit declarationThatMakesInvocation = GenerateUtil.getCodeUnitForRes(declarationRes, null, createdFrags, declarationParentFrag);
			createdFragPositions.put(declarationThatMakesInvocation, new Position(declaration.getStartPosition(), declaration.getLength()));
			
			List<Artifact> artList = declarationThatMakesInvocation.getArt().queryArtList(repo, new DirectedRel(RJCore.calls, true), null);
			for (Artifact art : artList) {
				// Check if this particular declaration calls one of the
				// invocations that has a change. We do not want to add all
				// sorts of methods that have nothing to do with the changes
				// being made
				if (!artIsInvocation(art, invocations)) continue;

				Artifact parentArt = art.queryParentArtifact(repo);
				CodeUnit childCU = new CodeUnit(art.elementRes);
				
				if (parentArt!=null) {
					CodeUnit parentCU = new CodeUnit(parentArt.elementRes);	
					parentCU.appendShownChild(childCU);
					createdFrags.add(parentCU );
				}
				createdFrags.add(childCU );
				// call connection from source -> target
				ArtifactRel rel = new ArtifactRel(declarationThatMakesInvocation, childCU, RJCore.calls);
				declarationThatMakesInvocation.addSourceConnection(rel);
				childCU.addTargetConnection(rel);

			}
		}
	}
	
	private boolean artIsInvocation(Artifact art, List<Invocation> invocations) {
		for (Invocation i : invocations) {
			if (art.toString().contains(i.toString())) return true;	
		}
		return false;
	}
	// Old method using IJE, and resource bindings, Often resource bindings were
	// not present so we could not create resources matching the actual code.
	// Now we create resources by getting the class and querying the RDF
//	private void createCallFrags(IJavaElement fileIJE,
//			CompilationUnit fileCU, 
//			CodeUnit fileClassFrag, 
//			List<Invocation> invocations,
//			List<CodeUnit> createdFrags, 
//			Map<CodeUnit, Position> createdFragPositions) {
//		
//		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
//		
//		MethodDeclarationFinder declarationFinder = new MethodDeclarationFinder(fileCU);
//		for(Invocation invocation : invocations) {
//			
//			// source of call
//			
//			MethodDeclaration declaration = declarationFinder.findDeclaration(invocation.getStartPosition());
//			IJavaElement declarationElmt = findMethodDeclarationIJE(fileIJE, declaration);
//			
//			// class containing declaration that makes invocation
//			CodeUnit declarationParentFrag = null;
//			if (declaration == null || declaration.resolveBinding() != null)
//				declarationParentFrag = getDeclaringClassFrag(declaration.resolveBinding(), createdFrags, repo);
//			else if(declarationElmt!=null) 
//				declarationParentFrag = getDeclaringClassFrag(declarationElmt, createdFrags, repo);
//			else declarationParentFrag = getDeclaringClassFrag(fileCU, declaration, createdFrags, repo);
//			
//			// declaration that makes invocation
//			Resource declarationRes = null;
//			if(declarationElmt!=null) declarationRes = RJCore.jdtElementToResource(repo, declarationElmt);
//			else declarationRes = AsmUtil.getMethodRes(declaration.getName().getIdentifier(), declarationParentFrag.getArt().elementRes, repo);
//			
//			CodeUnit declarationThatMakesInvocation = GenerateUtil.getCodeUnitForRes(declarationRes, null, createdFrags, declarationParentFrag);
//			createdFragPositions.put(declarationThatMakesInvocation, new Position(declaration.getStartPosition(), declaration.getLength()));
//			
//			// class containing target of call
//			CodeUnit containerOfDeclOfInvokedMethod = findInvocationTargetClass(
//					fileCU, declaration, declarationParentFrag, invocation, createdFrags, createdFragPositions, repo);
//
//			// target of call
//			Resource declOfInvokedMethodRes = null;
//			if(invocation.resolveMethodBinding()!=null)
//				declOfInvokedMethodRes = RJCore.jdtElementToResource(repo, invocation.resolveMethodBinding().getJavaElement());
//			if(declOfInvokedMethodRes==null) {
//				String invokedMethodName = invocation.getName();
//				if(invokedMethodName==null) invokedMethodName = invocation.toString();
//				declOfInvokedMethodRes = AsmUtil.getMethodRes(invokedMethodName, 
//						containerOfDeclOfInvokedMethod.getArt().elementRes, repo);
//			}
//			CodeUnit declOfInvokedMethodCU = GenerateUtil.getCodeUnitForRes(declOfInvokedMethodRes, null, createdFrags, containerOfDeclOfInvokedMethod);
//
//			// call connection from source -> target
//			ArtifactRel rel = new ArtifactRel(declarationThatMakesInvocation, declOfInvokedMethodCU, RJCore.calls);
//			declarationThatMakesInvocation.addSourceConnection(rel);
//			declOfInvokedMethodCU.addTargetConnection(rel);
//		}
//	}
	
	
	private IJavaElement findMethodDeclarationIJE(
			IJavaElement fileIJE, MethodDeclaration declaration) {
		if(fileIJE==null || declaration==null) return null;

		IJavaElement declarationElmt = null;
		if(declaration.resolveBinding()!=null)
			declarationElmt = declaration.resolveBinding().getJavaElement();
		if(declarationElmt!=null) return declarationElmt;

		try {
			if(fileIJE instanceof ICompilationUnit)
				declarationElmt = ((ICompilationUnit)fileIJE).getElementAt(declaration.getStartPosition());
		} catch (JavaModelException e) {
			logger.error("Unexpected exception while finding java element " +
					"for declaration "+declaration.getName(), e);
		}
		return declarationElmt;
	}
	
//	private CodeUnit findInvocationTargetClass(
//			CompilationUnit fileCU,
//			MethodDeclaration declaration, CodeUnit declarationParentFrag, 
//			Invocation invocation, List<CodeUnit> createdFrags, 
//			Map<CodeUnit, Position> createdFragPositions, ReloRdfRepository repo) {
//
//		String targetExpression = MethodUtil.getInstanceCalledOn(invocation);
//
//		if(invocation.resolveMethodBinding()!=null &&
//				invocation.resolveMethodBinding().getDeclaringClass()!=null) {
//			// Use binding and java element info if available
//			IJavaElement container = invocation.resolveMethodBinding().getDeclaringClass().getJavaElement();
//			if(container!=null) {
//				CodeUnit containerParent = getDeclaringClassFrag(invocation.resolveMethodBinding().getDeclaringClass(), createdFrags, repo);
//				CodeUnit containerOfDeclOfInvokedMethod = GenerateUtil.getCodeUnitForRes(RJCore.jdtElementToResource(repo, container), targetExpression, createdFrags, containerParent);
//				createdFragPositions.put(containerOfDeclOfInvokedMethod, new Position(invocation.getStartPosition(), invocation.getLength()));
//				return containerOfDeclOfInvokedMethod;
//			}
//		}
//
//		// Figure out the type of the expression that the invocation 
//		// is called on and make the target Resource from it
//		if(targetExpression==null || targetExpression.equals("this")) {
//			// Self call, so target class is just class that contains
//			// the declaration making the invocation
//			return declarationParentFrag;
//		}
//
//		TargetTypeFinder targetTypeFinder = new TargetTypeFinder(declaration, targetExpression);
//		Resource targetClassRes = targetTypeFinder.search();
//		if(targetClassRes==null) {
//			// Couldn't find any variable, field, or class that matched the target
//			// expression, so just use the expression's name as the target
//			// class's name (ie for x.bar(), the target class will be x). 
//			targetClassRes = getClassRes(fileCU, targetExpression, repo);
//		}
//		CodeUnit targetClassFrag = GenerateUtil.getCodeUnitForRes(targetClassRes, null, createdFrags, null);
//		return targetClassFrag;
//	}

	// Create ArtifactFragments for the given
	// nodes of fields, methods, and inner classes.
	// Created frags will be added to the list
	// of created frags.
	private void createMemberFrags(List<ASTNode> nodes,
			CodeUnit fileClassFrag, 
			List<CodeUnit> createdFrags, 
			Map<CodeUnit, Position> createdFragPositions) {

		Map<IBinding, Position> bindingToPos = new HashMap<IBinding, Position>();

		for(ASTNode diffNode : nodes) {

			Position nodePos = new Position(diffNode.getStartPosition(), diffNode.getLength());
			
			if(diffNode instanceof TypeDeclaration) {
				ITypeBinding binding = ((TypeDeclaration)diffNode).resolveBinding();
				bindingToPos.put(binding, nodePos);
			} else if(diffNode instanceof FieldDeclaration) {
				List<?> varDecls = ((FieldDeclaration)diffNode).fragments();
				for(Object varDecl : varDecls) {
					if(!(varDecl instanceof VariableDeclarationFragment)) continue;
					IVariableBinding binding = ((VariableDeclarationFragment)varDecl).resolveBinding();
					bindingToPos.put(binding, nodePos);
				}
			} else if(diffNode instanceof MethodDeclaration) {
				IMethodBinding binding = ((MethodDeclaration)diffNode).resolveBinding();
				bindingToPos.put(binding, nodePos);
			}
		}
		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();
		for(IBinding binding : bindingToPos.keySet()) {
			if(binding==null || binding.getJavaElement()==null) continue;

			Resource res = RJCore.jdtElementToResource(repo, binding.getJavaElement());
			CodeUnit parentUnit = getDeclaringClassFrag(binding, createdFrags, repo);
			CodeUnit memberFrag = GenerateUtil.getCodeUnitForRes(res, null, createdFrags, parentUnit);
			createdFragPositions.put(memberFrag, bindingToPos.get(binding));
		}
	}

	private CodeUnit getDeclaringClassFrag(IBinding binding, List<CodeUnit> createdFrags, ReloRdfRepository repo) {

		IBinding declaringClassBinding = null;
		if(binding instanceof ITypeBinding) 
			declaringClassBinding = ((ITypeBinding)binding).getDeclaringClass();
		else if(binding instanceof IVariableBinding) 
			declaringClassBinding = ((IVariableBinding)binding).getDeclaringClass();
		else if(binding instanceof IMethodBinding) 
			declaringClassBinding = ((IMethodBinding)binding).getDeclaringClass();
		if(declaringClassBinding==null) return null;

		Resource declaringClassRes = RJCore.bindingToResource(repo, declaringClassBinding);
		CodeUnit parentUnit = getDeclaringClassFrag(declaringClassBinding, createdFrags, repo);
		CodeUnit declaringClassFrag = GenerateUtil.getCodeUnitForRes(declaringClassRes, null, createdFrags, parentUnit);
		return declaringClassFrag;
	}

	private CodeUnit getDeclaringClassFrag(IJavaElement childElmt, List<CodeUnit> createdFrags, ReloRdfRepository repo) {

		IJavaElement declaringClassElmt = childElmt.getParent();
		if(declaringClassElmt==null || 
				IJavaElement.TYPE!=declaringClassElmt.getElementType()) 
			return null;
		Resource declaringClassRes = RJCore.jdtElementToResource(repo, declaringClassElmt);
		CodeUnit parentUnit = getDeclaringClassFrag(declaringClassElmt, createdFrags, repo);
		CodeUnit declaringClassFrag = GenerateUtil.getCodeUnitForRes(declaringClassRes, null, createdFrags, parentUnit);
		return declaringClassFrag;
	}

	private CodeUnit getDeclaringClassFrag(CompilationUnit fileCU,
			ASTNode childNode, List<CodeUnit> createdFrags, ReloRdfRepository repo) {

		ASTNode declaringClassNode = childNode.getParent();
		if(declaringClassNode==null || 
				!(declaringClassNode instanceof TypeDeclaration))
			return null;

		String declaringClassName = ((TypeDeclaration)declaringClassNode).getName().getIdentifier();
		Resource declaringClassRes = getClassRes(fileCU, declaringClassName, repo);
		CodeUnit parentUnit = getDeclaringClassFrag(fileCU, declaringClassNode, createdFrags, repo);
		CodeUnit declaringClassFrag = GenerateUtil.getCodeUnitForRes(declaringClassRes, null, createdFrags, parentUnit);
		return declaringClassFrag;
	}

	private void createComments(CompilationUnit fileCU, IDocument fileDoc, 
			List<Invocation> invocations, List<ASTNode> members,
			List<CodeUnit> createdFrags, Map<CodeUnit, Position> createdFragPositions, 
			Map<CodeUnit, Comment> changeToAssocComment) {
		List<?> commentList = fileCU.getCommentList();
		if(commentList==null) return;

		for(Object o : commentList) {
			if(!(o instanceof org.eclipse.jdt.core.dom.Comment)) continue;
			org.eclipse.jdt.core.dom.Comment commentNode = (org.eclipse.jdt.core.dom.Comment) o;

			CodeUnit associatedFrag = findAssociatedNode(fileCU, commentNode, invocations, members, createdFrags, createdFragPositions);
			if(associatedFrag==null) continue;

			Comment commentModel = new Comment();

			String commentString = commentNode.toString().trim();
			if(commentNode instanceof BlockComment || 
					commentNode instanceof LineComment) {
				// Comment text is only available from
				// ast node for java doc comments, so
				// for line and block comments need to
				// get comment text from file's document
				try {
					commentString = fileDoc.get(commentNode.getStartPosition(), commentNode.getLength());
				} catch (BadLocationException e) {
					logger.error("Unable to get comment at position " + commentNode.getStartPosition(), e);
				}
			}
			commentModel.setAnnoLabelText(commentString);
			changeToAssocComment.put(associatedFrag, commentModel);
		}
	}

	private CodeUnit findAssociatedNode(CompilationUnit fileCU, org.eclipse.jdt.core.dom.Comment comment, 
			List<Invocation> invocations, List<ASTNode> members, 
			List<CodeUnit> createdFrags, Map<CodeUnit, Position> createdFragPositions) {

		int commentStart = comment.getStartPosition();
		int commentEnd = commentStart + comment.getLength();
		int commentLine = fileCU.getLineNumber(commentStart);

		CodeUnit containerFrag = null;
		int containerFragStart = -1;
		int containerFragEnd = -1;

		for(CodeUnit frag : createdFrags) {

			// Associate it with the change that's on the same
			// line as the comment or the line following the comment
			Position fragPos = createdFragPositions.get(frag);
			if(fragPos==null) continue;

			int fragLine = fileCU.getLineNumber(fragPos.getOffset());

			if(commentLine==fragLine || commentLine+1==fragLine)
				return frag;

			// If no changed element next to or right after the comment,
			// then if a container of a change also contains the comment,
			// associate the comment with that container
			if(fragPos.getOffset() <= commentStart && commentEnd <= fragPos.getOffset()+fragPos.getLength()) {
				if(containerFrag==null) containerFrag = frag;

				// If frag more tightly contains the comment than a frag 
				// already iterated over, use frag. (For example, if we 
				// have: class C { foo() { //comment } }
				// then class C and method foo both contain the comment,
				// but foo() more tightly contains it and we want to 
				// associate the comment with foo, not c)
				if(containerFragStart==-1) containerFrag = frag;
				else if(containerFragStart <= fragPos.getOffset() &&
						fragPos.getOffset()+fragPos.getLength() <= containerFragEnd)
					containerFrag = frag;

				// Keep iterating over the createdFrags to see if
				// we can find a better association for the comment
			}
		}
		return containerFrag;
	}

	private Resource getClassRes(CompilationUnit fileCU, String className, 
			ReloRdfRepository repo) {
		String packageName = TeamUtil.getPackageName(fileCU);
		return AsmUtil.getClassRes(packageName+"."+className, repo);
	}

	public IDocument getRevisionFileDocument(final Object revision, IProgressMonitor monitor) {
		try {
			InputStream is = getInputStreamFromDoc(revision, monitor);
			if (is == null) return null;
			monitor.worked(5);
			return new Document(readInputStreamAsString(is));
		} catch (Throwable e) {
			logger.error("Error reading Synchronize file", e);
			return null;
		}
	}

	private int getTokenEnd(ITokenComparator tc, int start, int count) {
		if (count <= 0)
			return tc.getTokenStart(start);
		int index= start + count - 1;
		return tc.getTokenStart(index) + tc.getTokenLength(index);
	}
	
	private Position extract2Pos(IDocument doc, ITokenComparator tc, int start, int length) {
		int count= tc.getRangeCount();
		if (length > 0 && count > 0) {
			int startPos= tc.getTokenStart(start);
			int endPos;

			if (length == 1) {
				endPos= startPos + tc.getTokenLength(start);
			} else {
				endPos= tc.getTokenStart(start + length);
			}

			return new Position(startPos, endPos - startPos);

		}
		return new Position(start, length); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	private Constructor<? extends ITokenComparator> getTokenComparatorCons() throws ClassNotFoundException, SecurityException, NoSuchMethodException {
		String tokenComparatorClassName = "org.eclipse.compare.contentmergeviewer.TokenComparator"; 
		if (PluginUtils.getPluginVer("org.eclipse.compare") < 3.4)
			tokenComparatorClassName = "org.eclipse.compare.internal.TokenComparator";
		Class<? extends ITokenComparator> tokenComparatorClass = (Class<? extends ITokenComparator>) Class.forName(tokenComparatorClassName);
		return tokenComparatorClass.getConstructor(String.class);
	}

	private List<Position> simpleTokenDiff(Position fLeftPos, Position fRightPos,
			IDocument rightDoc, String rightString,
			IDocument leftDoc, String leftString, int lineNum) {

		List<Position> positions = new ArrayList<Position>();

		ITokenComparator leftComparator = null; 
		ITokenComparator rightComparator = null;
		try {
			Constructor<? extends ITokenComparator> tokenComparatorCons = getTokenComparatorCons();
			rightComparator = tokenComparatorCons.newInstance(rightString);
			leftComparator = tokenComparatorCons.newInstance(leftString);
		} catch (Throwable t) {
			logger.error("Problem instantiating class. ", t);
		}

		RangeDifference[] ranges = RangeDifferencer.findRanges(leftComparator, rightComparator);
		for(int i=0; i<ranges.length; i++) {
			RangeDifference difference = ranges[i];
			int kind = difference.kind();
			if(kind == RangeDifference.NOCHANGE) continue;

			int offsetOfLine = 0;
			try {
				offsetOfLine = leftDoc.getLineOffset(lineNum==0 ? 0 : lineNum-1);

				int leftStart = fLeftPos.getOffset() + leftComparator.getTokenStart(difference.leftStart());
				int leftEnd = fLeftPos.getOffset() + getTokenEnd(leftComparator, difference.leftStart(), difference.leftLength());

				int offset = offsetOfLine + leftStart;
				int length = leftEnd - leftStart;

				Position position = new Position(offset, length);
				positions.add(position);

			} catch (BadLocationException e2) {
				// silently ignored
			}
		}
		return positions;
	}

	
    /* ==============  UTILS =============*/
	
	private Position createPosition(IDocument doc, Position range, int start, int end) {
		try {
			int l= end-start;
			if (range != null) {
				int dl= range.length;
				if (l > dl)
					l= dl;					
			} else {
				int dl= doc.getLength();
				if (start+l > dl)
					l= dl-start;
			}

			Position p= null;
			try {
				p= new Position(start, l);
			} catch (RuntimeException ex) {
				p= new Position(0, 0);
			}

			try {
				doc.addPosition(CompareUIPlugin.PLUGIN_ID + ".DIFF_RANGE_CATEGORY", p);
			} catch (BadPositionCategoryException ex) {
				// silently ignored
			}
			return p;
		} catch (BadLocationException ee) {
			// silently ignored
		}
		return null;
	}
	
	public String getDiagramEngineId() {
		if(diagramEngine==null) return null;
		return diagramEngine.editorId();
	}
	public static String readInputStreamAsString(InputStream in) throws IOException {
	
	    BufferedInputStream bis = new BufferedInputStream(in);
	    ByteArrayOutputStream buf = new ByteArrayOutputStream();
	    int result = bis.read();
	    while(result != -1) {
	      byte b = (byte)result;
	      buf.write(b);
	      result = bis.read();
	    }        
	    return buf.toString();
	}

}
