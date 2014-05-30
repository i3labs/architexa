package com.architexa.rse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

public class BuildSelectionWizardPage extends WizardPage{

	public static final String PAGE_NAME = "Index Selection";
//	public static ISharedImages isi = JavaUI.getSharedImages();
	Tree projPackgTree;
	Map<String, IJavaProject> projectNameToResMap = new HashMap<String, IJavaProject>();
	
	protected BuildSelectionWizardPage() {
		super(PAGE_NAME, "Package Selection", null);
	}

	public void createControl(Composite parent) {
		
		Composite topLevel = new Composite(parent, SWT.NONE);
		topLevel.setLayout(new GridLayout());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 350;
		data.widthHint = 250;
		topLevel.setLayoutData(data);
		parent.setLayoutData(data);
		createBuildPrefContents(topLevel);
		setControl(topLevel);
		setPageComplete(true);
	}

	
	private Control createBuildPrefContents(Composite tabFolder) {
		Composite container = new Composite(tabFolder,SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight=0;
		container.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		container.setLayoutData(data);
		
		Label desc = new Label(container, SWT.NONE);
		desc.setText("Select the packages below to include in the Architexa index." +
				"\nArchitexa pre-processes the code to keep an index to provide" +
				"\nthe most up to date diagrams. No code is sent to the server."+
				"\nIn order to speed up the process we recommend you to remove " +
				"\nnon-relevant packages\n");
		desc.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false, 2, 1));
		
		addProjectSelectionArea(container );
		return container;
	}
	
	private void addProjectSelectionArea(Composite container) {
		projPackgTree = PreferenceUtils.addAndReturnProjPackgTree(container, projectNameToResMap);
		PreferenceUtils.addSelectButtons(container, projPackgTree);
	}
	
	
//	public Map getUnselectedProjectsMap () {
//		List unchkdList;
//		Map projToUnchkdPackgMap = new HashMap();
//		
//		TreeItem[] itemList = projPackgTree.getItems();
//		for (int i = 0; i < itemList.length; i++) {
//			TreeItem item = itemList[i];
//			unchkdList = new ArrayList();
//			if (!item.getChecked() && !item.getGrayed()) {
//				projToUnchkdPackgMap.put(item, unchkdList);
//				continue;
//			}
//
//			TreeItem[] packgList = item.getItems();
//			for (int j = 0 ;j < packgList.length; j++) {
//				TreeItem childItem = packgList[j];
//				if (!childItem.getChecked())
//					unchkdList.add(childItem);
//			}
//			
//			if (unchkdList.isEmpty()) continue;
//			projToUnchkdPackgMap.put(item, unchkdList);
//		}
//		return projToUnchkdPackgMap;
//	}
//	
//	public void addSelectButtons(Composite container, final Tree tree) {	
//		
//		Composite buttonColumn = new Composite(container, SWT.NONE);
//		GridLayout buttonColumnLayout = new GridLayout(1, true);
//		buttonColumn.setLayout(buttonColumnLayout);
//		buttonColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));	
//		
//		Button selectAll = new Button(buttonColumn, SWT.PUSH);
//		selectAll.setText("Select All");
//		selectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		selectAll.addListener(SWT.Selection, new Listener() {
//			public void handleEvent(Event event) {
//				selectOrDeselectAllInTree(tree.getItems(), true);
//			}
//		});
//
//		Button deselectAll = new Button(buttonColumn, SWT.PUSH);
//		deselectAll.setText("Deselect All");
//		deselectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		deselectAll.addListener(SWT.Selection, new Listener() {
//			public void handleEvent(Event e) {
//				selectOrDeselectAllInTree(tree.getItems(), false);
//			}
//			
//		});
//		
//		buttonColumn.pack();
//		buttonColumn.update();
//	}
//	
//	private void selectOrDeselectAllInTree(TreeItem[] treeItems, boolean select) {
//		for (int i = 0; i < treeItems.length; i++) {
//			TreeItem item = treeItems[i];
//			selectOrDeselectAllInTree(item.getItems(), select);
//			item.setChecked(select);
//		}
//	}


//	private Tree addAndReturnProjPackgTree(Composite container) {
//		projPackgTree = new Tree(container, SWT.BORDER | SWT.CHECK);
//		GridData treeData = new GridData(SWT.FILL, SWT.FILL, true, true);
//		treeData.heightHint = 230;
//		treeData.widthHint = 150;
//		projPackgTree.setLayoutData(treeData);
//		IWorkspaceRoot root = AtxaIntroPlugin.getWorkspace().getRoot();
//
//		try {
//			IJavaProject[] javaProjects = JavaCore.create(root).getJavaProjects();
//			for (int i = 0; i < javaProjects.length; i++) {
//				IJavaProject project = javaProjects[i];
//				TreeItem projItem = new TreeItem(projPackgTree, SWT.NONE);
//				projItem.setText(project.getElementName());
//				projItem.setChecked(true);
//				projItem.setImage(isi.getImage(ISharedImages.IMG_OBJS_PACKFRAG_ROOT));
//				IPackageFragment[] packgList = project.getPackageFragments();
//				for (int j = 0; j < packgList.length; j++) {
//					IPackageFragment pckg = packgList[j];
//					if (pckg.getParent() instanceof JarPackageFragmentRoot || !pckg.containsJavaResources())
//						continue;
//					TreeItem pckgItem = new TreeItem(projItem, SWT.NONE);
//					pckgItem.setText(pckg.getElementName());
//					pckgItem.setChecked(true);
//					pckgItem.setImage(isi.getImage(ISharedImages.IMG_OBJS_PACKAGE));
//				}
//			}
//		} catch (JavaModelException e) {
//			e.printStackTrace();
//		}
//
//		addSelectionListenerToTree(projPackgTree);
//		projPackgTree.pack();
//		projPackgTree.update();
//		return projPackgTree;
//	}
//	
//	private void addSelectionListenerToTree(Tree tree) {
//		tree.addSelectionListener(new SelectionListener() {
//			public void widgetSelected(SelectionEvent e) {
//				TreeItem item = (TreeItem) e.item;
//				boolean isSelected = item.getChecked();
//				if (item.getParentItem() != null) {
//					item.getParentItem().setChecked(isSelected);
//					if (!isSelected) {
//						TreeItem[] itemList = item.getParentItem().getItems();
//						for(int i = 0; i < itemList.length; i++) {
//							TreeItem childItem = itemList[i];
//							if (childItem.getChecked()) {
//								item.getParentItem().setChecked(true);
//								break;
//							}
//						}
//					}
//				}
//				
//				TreeItem[] itemList = item.getItems();
//				for(int i = 0; i < itemList.length; i++) {
//					TreeItem childItem = itemList[i];
//					childItem.setChecked(isSelected);
//				}
//			}
//
//			public void widgetDefaultSelected(SelectionEvent e) {
//				e.getSource();
//			}
//		});
//	}

	public void setPreferenceFilters() {
		PreferenceUtils.setProjFilters(projPackgTree);
		PreferenceUtils.setLibraryFilters(projPackgTree, projectNameToResMap);
	}
	
}
