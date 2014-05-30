package com.architexa.diagrams.generate.search;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search2.internal.ui.SearchView;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import com.architexa.diagrams.chrono.util.MethodUtil;
import com.architexa.diagrams.generate.GeneratePlugin;
import com.architexa.diagrams.generate.GenerateUtil;
import com.architexa.diagrams.jdt.RJCore;
import com.architexa.diagrams.jdt.model.CodeUnit;
import com.architexa.diagrams.model.ArtifactFragment;
import com.architexa.diagrams.model.ArtifactRel;
import com.architexa.store.ReloRdfRepository;
import com.architexa.store.StoreUtil;


public abstract class SearchDiagramGenerator extends AbstractHandler {

	private static final Logger logger = GeneratePlugin.getLogger(SearchDiagramGenerator.class);
	protected static int noLimit = 100;

	protected abstract void openViz(IWorkbenchWindow activeWorkbenchWindow, List<ArtifactFragment> fragList);
	protected abstract int getMaxToInclude();

	//Do not put override here as method is present in an interface (Possible bug in eclipse 3.3)
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			generate();
		} catch(Exception e) {
			logger.error("Unexpected exception while generating diagram from search results", e);
		}
		return null;
	}

	private void alertNoSearchResults() {
		new MessageDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
				"View Search Results in Diagram",
				null, 
				"No search results available.", 
				MessageDialog.INFORMATION, 
				new String[]{"OK"}, 
				1).open();
	}

	private void generate() {

		ReloRdfRepository repo = StoreUtil.getDefaultStoreRepository();

		// create models for search results
		List<CodeUnit> createdCodeUnits = createFragsForSearchResults(repo);

		// if no search performed, alert user instead of opening empty diagram
		if(createdCodeUnits.size()==0) {
			alertNoSearchResults();
			return;
		}

		// add connections among these results
		addConnections(createdCodeUnits, repo);

		// open in a diagram
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		List<ArtifactFragment> topLevelClassFrags = new ArrayList<ArtifactFragment>();
		// only need to pass top level classes to avoid duplicates being added
		for(CodeUnit cu : createdCodeUnits) {
			if(cu.getParentArt()==null &&
					(RJCore.classType.equals(cu.queryType(repo)) || 
							RJCore.interfaceType.equals(cu.queryType(repo))))
				topLevelClassFrags.add(cu);
		}
		openViz(activeWorkbenchWindow, topLevelClassFrags);
	}

	private List<CodeUnit> createFragsForSearchResults(ReloRdfRepository repo) {

		List<CodeUnit> createdCodeUnits = new ArrayList<CodeUnit>();

		ISearchResultViewPart viewPart = NewSearchUI.getSearchResultView();
		if(!(viewPart instanceof SearchView)) return createdCodeUnits;

		SearchView searchView = (SearchView) viewPart;
		if(!(searchView.getCurrentSearchResult() instanceof AbstractTextSearchResult)) 
			return createdCodeUnits;

		AbstractTextSearchResult searchResult = (AbstractTextSearchResult) searchView.getCurrentSearchResult();
		List<Object> elmtsContainingMatches = Arrays.asList(searchResult.getElements());
		// (elmtsContainingMatches are the files that appear in the search result tree)

		// Getting the "important" top level classes so that the list
		// of all created code units will have less relevant components
		// filtered out and the diagram won't get too large
		List<IFile> mostRelevantClasses = 
			getMostImportantClassesContainingMatches(elmtsContainingMatches, searchResult);

		for(IFile file : mostRelevantClasses) {
			ICompilationUnit icu = (ICompilationUnit) JavaCore.create(file);
			if(icu==null) continue; // not a java file
			for(Match match : searchResult.getMatches(file)) {
				IJavaElement matchElmt = null;
				try {
					matchElmt = icu.getElementAt(match.getOffset());
				} catch (JavaModelException e) {
					logger.error("Couldn't find IJavaElement for search match " + match, e);
				}
				if(matchElmt==null) continue;
				Resource matchRes = RJCore.jdtElementToResource(repo, matchElmt);

				// Not simply adding the match model to the top level container because
				// match could instead be contained within a nested or anonymous class

				Resource matchContainerRes = MethodUtil.getContainerClass(matchRes, repo);

				// Creating model for container
				CodeUnit containerFrag = matchContainerRes==null?null:
					GenerateUtil.getCodeUnitForRes(matchContainerRes, null, createdCodeUnits, null);

				// Creating model for match
				GenerateUtil.getCodeUnitForRes(matchRes, null, createdCodeUnits, containerFrag);
			}
		}
		return createdCodeUnits;
	}

	private List<IFile> getMostImportantClassesContainingMatches(
			List<Object> allElmtsContainingMatches, 
			final AbstractTextSearchResult searchResult) {

		List<IFile> allClassesContainingMatches = new ArrayList<IFile>();
		for(Object elmt : allElmtsContainingMatches) {
			if(!(elmt instanceof IFile)) continue;
			if(JavaCore.create((IFile)elmt)==null) continue; // not a java file
			allClassesContainingMatches.add((IFile)elmt);
		}

		int maxToInclude = getMaxToInclude();
		if(maxToInclude==noLimit || allClassesContainingMatches.size()<=maxToInclude)
			return allClassesContainingMatches;

		// Don't want too large a diagram, so only including "most relevant" components:
		// Classes in open editors are "most important", and classes with the most
		// matches are the next most important. 

		final List<IFile> openEditors = new ArrayList<IFile>();
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		for(IEditorReference editor : activeWorkbenchWindow.getActivePage().getEditorReferences()) {
			try {
				IEditorInput editorInput = editor.getEditorInput();
				if(!(editorInput instanceof IFileEditorInput)) continue;
				IFile editorFile = ((IFileEditorInput)editorInput).getFile();
				if(editorFile==null) continue;
				openEditors.add(editorFile);
			} catch (PartInitException e) {
				logger.error("Unexpected exception while getting editor input for editor "+editor, e);
			}
		}

		Comparator<IFile> relevanceComparator = new Comparator<IFile>() {
			public int compare(IFile f1, IFile f2) {
				if(openEditors.contains(f1) && !openEditors.contains(f2)) return -1;
				if(openEditors.contains(f2) && !openEditors.contains(f1)) return 1;
				return -1*((Integer)searchResult.getMatchCount(f1)).compareTo(
						((Integer)searchResult.getMatchCount(f2)));
			}
		};
		// Sorted list will have open editors with the most matches
		// first and non-open editors with the fewest matches last.
		Collections.sort(allClassesContainingMatches, relevanceComparator);

		int numToAdd = Math.min(maxToInclude, allClassesContainingMatches.size());
		List<IFile> mostRelevantClasses = new ArrayList<IFile>();
		for(int i=0; i<numToAdd; i++) {
			mostRelevantClasses.add(allClassesContainingMatches.get(i));
		}
		return mostRelevantClasses;
	}

	private void addConnections(List<CodeUnit> frags, ReloRdfRepository repo) {
		for(CodeUnit frag1 : frags) {
			Resource type1 = frag1.queryType(repo);
			if(type1==null) {
				logger.error("Could not find type for frag " + frag1);
				continue;
			}

			for(CodeUnit frag2 : frags) {

				// Not adding connections within the same frag
				if(frag1.equals(frag2)) continue; 

				// Only adding connections between the same types
				Resource type2 = frag2.queryType(repo);
				if(type2==null) {
					logger.error("Could not find type for frag " + frag2);
					continue;
				}
				if(!type1.equals(type2)) continue;

				URI[] connTypes = {RJCore.calls, RJCore.overrides, RJCore.inherits};
				for(URI connType : connTypes) {
					Statement stmt = repo.getStatement(frag1.getArt().elementRes, connType, frag2.getArt().elementRes);
					if(ReloRdfRepository.nullStatement.equals(stmt)) continue;

					ArtifactRel rel = new ArtifactRel(frag1, frag2, connType);
					frag1.addSourceConnection(rel);
					frag2.addTargetConnection(rel);
				}
			}
		}
	}

}