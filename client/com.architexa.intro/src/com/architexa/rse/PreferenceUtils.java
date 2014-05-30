package com.architexa.rse;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.architexa.intro.AtxaIntroPlugin;

public class PreferenceUtils {
	public static final String pageId = "Architexa Build Path Properties";
	static final Logger logger = AtxaIntroPlugin.getLogger(PreferenceUtils.class);
//	Tree projPackgTree;
//	Map<String, IJavaProject> projectNameToResMap = new HashMap<String, IJavaProject>();
	public static ISharedImages isi = JavaUI.getSharedImages();
	private static String LIBRARIES = "libraries"; 
	
	public static Tree addAndReturnProjPackgTree(Composite container, Map<String, IJavaProject> projectNameToResMap) {
		Tree projPackgTree = new Tree(container, SWT.BORDER | SWT.CHECK);
		GridData treeData = new GridData(SWT.FILL, SWT.FILL, true, true);
		treeData.heightHint = 350;
		treeData.widthHint = 250;
		projPackgTree.setLayoutData(treeData);
		IWorkspaceRoot root = AtxaIntroPlugin.getWorkspace().getRoot();

		try {
			IJavaProject[] javaProjects = JavaCore.create(root).getJavaProjects();
			List<IJavaProject> projList = new ArrayList<IJavaProject>(Arrays.asList(javaProjects));
			Collections.sort(projList, new Comparator<IJavaProject>() {
				public int compare(IJavaProject p1, IJavaProject p2) {
					return p1.getElementName().compareToIgnoreCase(p2.getElementName());
				}
			});
			for (IJavaProject project : projList) {
				if (projectNameToResMap != null)
					projectNameToResMap.put(project.getElementName(), project);
				TreeItem projItem = new TreeItem(projPackgTree, SWT.NONE);
				projItem.setText(project.getElementName());
				projItem.setChecked(true);
				projItem.setImage(isi.getImage(ISharedImages.IMG_OBJS_PACKFRAG_ROOT));
				Map<IJavaElement, TreeItem> srcRootFoldersToTreeItemMap = new HashMap<IJavaElement, TreeItem>();
				
				List<IPackageFragment> packageList = Arrays.asList(project.getPackageFragments());
				Collections.sort(packageList, new Comparator<IPackageFragment>() {
					public int compare(IPackageFragment o1, IPackageFragment o2) {
						return o1.getParent().getElementName().compareToIgnoreCase(o2.getParent().getElementName());
					}
				});
				
				// Libraries
				addLibrariesToTree(project, projItem);
				
				for (IPackageFragment pckg : packageList) {
					IJavaElement parentFolderElem = pckg.getParent();
					if (!pckg.containsJavaResources() || 
							parentFolderElem instanceof JarPackageFragmentRoot)	
						continue;
					
					TreeItem srcFolderTreeItem;
					if (srcRootFoldersToTreeItemMap.containsKey(parentFolderElem)) {
						srcFolderTreeItem = srcRootFoldersToTreeItemMap.get(parentFolderElem);
					} else {
						srcFolderTreeItem = new TreeItem(projItem, SWT.NONE);
						if (parentFolderElem.getElementName().equals(""))
							srcFolderTreeItem.setText("(default)");
						else {
							String parentFolderPath = parentFolderElem.getPath().toString().replace("/"+project.getElementName()+"/", "");
							srcFolderTreeItem.setText(parentFolderPath);
						}
						srcFolderTreeItem.setChecked(true);
						srcFolderTreeItem.setImage(isi.getImage(ISharedImages.IMG_OBJS_PACKFRAG_ROOT));
						srcRootFoldersToTreeItemMap.put(parentFolderElem, srcFolderTreeItem);
					}
					
					TreeItem pckgItem = new TreeItem(srcFolderTreeItem, SWT.NONE);
					if (pckg.getElementName().equals(""))
						pckgItem.setText("(default)");
					else pckgItem.setText(pckg.getElementName());
					pckgItem.setChecked(true);
					pckgItem.setImage(isi.getImage(ISharedImages.IMG_OBJS_PACKAGE));
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		if (projectNameToResMap != null)
			setUpTreeFromPreferences(projPackgTree);
		addSelectionListenerToTree(projPackgTree);
		projPackgTree.pack();
		projPackgTree.update();
		return projPackgTree;
	}
	
	private static void addLibrariesToTree(IJavaProject project, TreeItem projItem) {
		try {
			
			TreeItem libFolderTreeItem = new TreeItem(projItem, SWT.NONE);
			libFolderTreeItem.setText(LIBRARIES);
			libFolderTreeItem.setImage(isi.getImage(ISharedImages.IMG_OBJS_LIBRARY));
			
			List<IPackageFragmentRoot> rootList = Arrays.asList(project.getPackageFragmentRoots());
		
			// this comparison was causing problems on start up/ first user
			// install. It has been changed to be extra paranoid so users will
			// have a good first experience
			Collections.sort(rootList, new Comparator<IPackageFragmentRoot>() {
				public int compare(IPackageFragmentRoot o1, IPackageFragmentRoot o2) {
					try {
						if (o1 == null || o2 == null || o2.getParent()==null || o2.getParent().getElementName()==null || o1.getElementName()==null) 
							return 0;
//						return o1.getElementName().compareToIgnoreCase(o2.getParent().getElementName());
						return o1.getElementName().compareToIgnoreCase(o2.getElementName());
					} catch (Throwable t) {
						System.err.println("Error comparing packages");
						return 0;
					}
				}
			});
			List<IJavaElement> libItemList = new ArrayList<IJavaElement>();
			for (IPackageFragmentRoot lib : rootList) {
				if (lib instanceof JarPackageFragmentRoot) {
					String prop = project.getResource().getPersistentProperty(new QualifiedName(pageId, lib.getElementName()));
					if (libItemList.contains(lib)) continue;
					libItemList.add(lib);
					TreeItem item = new TreeItem(libFolderTreeItem, SWT.NONE);
					item.setText(lib.getElementName());
					item.setImage(isi.getImage(ISharedImages.IMG_OBJS_JAR));
					if (prop!= null && prop.equalsIgnoreCase("true")) {
						item.setChecked(true);
						libFolderTreeItem.setChecked(true);
					}
					continue;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void setUpTreeFromPreferences(Tree tree) {
		// Setting up the menu from the preference
		Map storedUnselectedPrjList = BuildSettings.getStoredUnselectedProjPackgMap();
		if (storedUnselectedPrjList == null) return;
		for (TreeItem item : tree.getItems()) {
			// If Parent is unchecked uncheck all children
			List<String> list = (List<String>) storedUnselectedPrjList.get(item.getText());
			// All packages checked
			if (list == null) continue;
			// finds children and unchecks those that should be 
			uncheckFromPref(list, item);
		}
	}
	
	private static void uncheckFromPref(List<String> list, TreeItem treeItem) {
		TreeItem[] treeItems = treeItem.getItems();
		int size = treeItems.length;
//		if (size <= 0) return;
		if (list.isEmpty()) { // Project Unchecked
			treeItem.setChecked(false);
			selectOrDeselectAllChildren(treeItems, false);
			return;	
		}
		
		for (TreeItem childItem : treeItems) {
			if(childItem.getText().equals(LIBRARIES)) continue;
			List<String> srcFldList = BuildSettings.itemToSrcMap.get(childItem.getText());
			TreeItem parentTreeItem = childItem.getParentItem();
			boolean itemsSrcFldrsMatch = srcFldList != null && srcFldList.contains(parentTreeItem .getText());
			if (list.contains(childItem.getText()) && itemsSrcFldrsMatch ) {
				childItem.setChecked(false);
				size--;
			}
			uncheckFromPref(list, childItem);
			if (size == 0) treeItem.setChecked(false);
		}
	}
	
	private static void addSelectionListenerToTree(final Tree tree) {
		tree.addSelectionListener(new SelectionListener() {
			int count =1;
			public void widgetSelected(SelectionEvent e) {
				// This hack prevents the listener from firing when the
				// preference tree area is 'selected' for the first time.
				// Otherwise all the children of the first node will be checked
				// even if the preferences are different...
				try {
					TreeItem item = (TreeItem) e.item;
					if (item.equals(tree.getItems()[0]) && count == 1) {
						count++;
						return;
					}
					boolean isSelected = item.getChecked();
					selectOrDeselectAllAncestors(item, isSelected);
					selectOrDeselectAllChildren(item.getItems(), isSelected);
				} catch (Throwable t) {
					logger.error("Error in preference page for Item: " + ((TreeItem) e.item) + "\nTree Items: " + tree.getItems(), t);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				e.getSource();
			}
		});
	}

	public static void selectOrDeselectAllAncestors(TreeItem item,
			boolean isSelected) {
		while (item.getParentItem() != null) {
			item.getParentItem().setChecked(isSelected);
			if (!isSelected) {
				for (TreeItem childItem : item.getParentItem().getItems()) {
					if (childItem.getChecked()) {
						item.getParentItem().setChecked(true);
						break;
					}
				}
			}
			item = item.getParentItem();
		}
	}
	
	public static void selectOrDeselectAllChildren(TreeItem[] treeItems, boolean select) {
		for(TreeItem item : treeItems) {
			if (select && item.getText().equals(LIBRARIES)) continue;
			selectOrDeselectAllChildren(item.getItems(), select);
			item.setChecked(select);
		}
	}
	
	public static void addSelectButtons(Composite container, final Tree tree) {	
		
		Composite buttonColumn = new Composite(container, SWT.NONE);
		GridLayout buttonColumnLayout = new GridLayout(1, true);
		buttonColumn.setLayout(buttonColumnLayout);
		buttonColumn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));	
		
		Button selectAll = new Button(buttonColumn, SWT.PUSH);
		selectAll.setText("Select All");
		selectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				PreferenceUtils.selectOrDeselectAllChildren(tree.getItems(), true);
			}
		});

		Button deselectAll = new Button(buttonColumn, SWT.PUSH);
		deselectAll.setText("Deselect All");
		deselectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deselectAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				PreferenceUtils.selectOrDeselectAllChildren(tree.getItems(), false);
			}
			
		});
		
		buttonColumn.pack();
		buttonColumn.update();
	}
	
	
	public static void setLibraryFilters(Tree projPackgTree, Map<String, IJavaProject> projectNameToResMap) {
		try {
			for (TreeItem projectItem : projPackgTree.getItems()) {
				IJavaProject project = projectNameToResMap.get(projectItem.getText());
				if (project == null) {
					logger.error("Atxa: Could not find project to save preferences: " + projectItem.getText());
					continue;
				}
				
				for (TreeItem child : projectItem.getItems()) {
					if (!child.getText().equalsIgnoreCase(LIBRARIES)) continue;
					
					URL[] classPathURLArray = getProjectClasspath(project);
					List<URL> classPathURLs = new ArrayList<URL>();
					for (TreeItem libs : child.getItems()) {
						String libName = libs.getText();
						for (URL url : classPathURLArray) {
							String urlStr = url.toString();
							
							if (libName.equalsIgnoreCase(urlStr.substring(urlStr.lastIndexOf("/") + 1))) {
								project.getResource().setPersistentProperty(new QualifiedName(PreferenceUtils.pageId, libName), getBooleanString(libs.getChecked()));
								if (libs.getChecked())
									classPathURLs.add(url);
							} 
						}
					}
					
					IProject proj = project.getResource().getProject();
					setPersistentProperty(proj, classPathURLs);
						
				}
			}
		} catch (Exception e) {
			logger.error("Error writing persistent properties.\n");
			e.printStackTrace();
		}
	}
	
	 public static URL[] getProjectClasspath(IJavaProject jp) {
		if (!jp.exists()) return new URL[0];

		try {
			String defaultRoot = jp.getProject().getWorkspace().getRoot().getRawLocation().toOSString();

			IClasspathEntry[] icpes = jp.getResolvedClasspath(false);
			List<URL> list = new ArrayList<URL>(icpes.length + 1);
			
			// we are only supporting the default output location, there can be
			// more, but we don't know how to get them
            IPath projLocation = jp.getProject().getLocation();
            IPath outputLocation = jp.getOutputLocation();
            IPath relOutputLocation = outputLocation.removeFirstSegments(1); // bec. both have the project name in it
			list.add(projLocation.append(relOutputLocation).toFile().toURL());

			for (IClasspathEntry entry : icpes) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					String path = entry.getPath().toOSString();

					if (!(entry.getPath().toFile().exists())) {
						//Check if entry is located in the default workspace root
						File file1 = new File(defaultRoot + path);
						//Check if entry is located in the project's folder or one of its subfolders
						File file2 = new File(projLocation.removeLastSegments(1).removeTrailingSeparator() + path);
						if (file1.exists()) {
							path = defaultRoot + path;
						} else if(file2.exists()) {
							path = projLocation.removeLastSegments(1).removeTrailingSeparator() + path;
						} else {
							continue;
						}
					}

					list.add(new URL("file:///" + path));
				}
			}
			return list.toArray(new URL[list.size()]);
		} catch (JavaModelException e) {
			logger.error("Unexpected Exception", e);
		} catch (MalformedURLException e) {
			logger.error("Unexpected Exception", e);
		}

		return new URL[0];
	}
	
	public static final String JARS_SELECTED = "jarsSelected";
	public static int divVal = 1000;
	public static void setPersistentProperty(IProject proj,
			List<URL> classPathURLs) {
		try {
			if (classPathURLs.isEmpty()) {
				clearAllLibFilters(proj);
				return;
			}
				
			String str = classPathURLs.toString();
			int len = str.length(); 
			int i = 0;
			if ( len > divVal) {
				while ((len / divVal) > 0) {
					proj.setPersistentProperty(new QualifiedName(JARS_SELECTED, proj.getName() + i), str.substring(i, i + divVal));
					i += divVal;
					len = len % divVal;
				}
				proj.setPersistentProperty(new QualifiedName(JARS_SELECTED, proj.getName() + i), str.substring(i, i + len));
			} else
				proj.setPersistentProperty(new QualifiedName(JARS_SELECTED, JARS_SELECTED), classPathURLs.toString());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private static void clearAllLibFilters(IProject proj) {
		QualifiedName keyName = new QualifiedName(JARS_SELECTED, JARS_SELECTED);
		try {
			proj.setPersistentProperty(keyName, "");
		} catch (CoreException e) {
			e.printStackTrace();
		}
		// This is not supported by eclipse 3.3 or below, but may be more robust
//		try {
//			Map propMap = proj.getPersistentProperties();
//			for (Object key : propMap.keySet()) {
//				if (!(key instanceof QualifiedName)) continue;
//				QualifiedName keyName = (QualifiedName)key;
//				if (JARS_SELECTED.equalsIgnoreCase(keyName.getQualifier()))
//					proj.setPersistentProperty(keyName, "");
//			}
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
		
	}
	
	private static String getBooleanString(Boolean bool) {
		if (bool) return "true";
		return "false";
	}
	
	private static Map<TreeItem, TreeItem> itemToSrcFldrMap = new HashMap<TreeItem, TreeItem>();
	
	private static Map<TreeItem, List<TreeItem>> getUnselectedProjectsMap (Tree projPackgTree) {
		List<TreeItem> unchkdList;
		Map<TreeItem, List<TreeItem>> projToUnchkdPackgMap = new HashMap<TreeItem, List<TreeItem>>();
		itemToSrcFldrMap.clear();
		for (TreeItem item : projPackgTree.getItems()) {
			unchkdList = new ArrayList<TreeItem>();
			if (!item.getChecked() && !item.getGrayed()) {
				projToUnchkdPackgMap.put(item, unchkdList);
				continue;
			}
			addChildrenToMap(item.getItems(), unchkdList);
			
			if (unchkdList.isEmpty()) continue;
			projToUnchkdPackgMap.put(item, unchkdList);
		}
		return projToUnchkdPackgMap;
	}
	
	private static void addChildrenToMap (TreeItem[] treeItems, List<TreeItem> unchkdList) {
		for (TreeItem childItem : treeItems) {
			if (childItem.getText().equalsIgnoreCase(LIBRARIES)) continue;
			if (!childItem.getChecked()) {
				unchkdList.add(childItem);
				if(childItem.getParentItem()!=null)
					itemToSrcFldrMap.put(childItem, childItem.getParentItem());
			}
			addChildrenToMap(childItem.getItems(), unchkdList);
		}
	}

	public static void setProjFilters(Tree projPackgTree) {
		BuildSettings.setStoredUnselectedProjPackgMap(PreferenceUtils.getUnselectedProjectsMap(projPackgTree), itemToSrcFldrMap);
	}
	
}
