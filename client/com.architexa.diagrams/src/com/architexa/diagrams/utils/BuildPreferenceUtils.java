package com.architexa.diagrams.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.architexa.diagrams.Activator;
import com.architexa.rse.BuildSettings;
import com.architexa.rse.PreferenceUtils;

/**
 * This class and BuildPreferencePage should ideally move to be right next to
 * each other
 */
public class BuildPreferenceUtils {
	private static final Logger logger = Activator.getLogger(BuildPreferenceUtils.class);

	/**
	 * Checks if selection is in build - and if so returns true. If not in build
	 * the user is sent to the preference dialog
	 */
	public static boolean selectionInBuild(List<?> selection) {
		int resp = BuildPreferenceUtils.checkBuildSelection(selection);
		if (resp == 0) {
			PreferenceDialog prefDlg = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "buildPreferencePage", null, null);
			if (prefDlg  != null) prefDlg.open();
		} else if (resp == 1)
			return true;
		return false;
	}

	/**
	 * @return checks if selection is in the architxa build settings and returns one of three values:
	 * 0=OK (change build settings), 1=Ignore and Proceed, 2=Cancel
	 */
	private static int checkBuildSelection (List<?> selectionList) {
		try {
			// for each selected item get it's project and package and compare to what is stored in the BuildSettings
			Set<String> itemsNotInBuild = new HashSet<String>();
			getChildrenItemsNotInBuild(selectionList, itemsNotInBuild);
			
			// open dialog
			MessageDialog dialog = new MessageDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Selected items not in Architexa build path",
					null, getDialogMsg(itemsNotInBuild),
					MessageDialog.WARNING, 
					new String[] { "OK", "Ignore and Proceed", "Cancel" }, 
					0);
			
			if (!itemsNotInBuild.isEmpty()) 
				return dialog.open();
			
			return 1;
		} catch (CoreException e) {
			logger.error("Error checking for build selection: ", e);
			return 1;
		}
	}

	private static void getChildrenItemsNotInBuild(List<?> selectionList, Set<String> itemsNotInBuild) throws CoreException {
		for (Object selObj : selectionList) {
			if (!(selObj instanceof IJavaElement)) continue;
			IJavaElement selJE = (IJavaElement)selObj;
			if (selObj instanceof JarPackageFragmentRoot) continue;
			IJavaProject projJE = selJE.getJavaProject();
			IPackageFragment packageJE = getPackage(selJE);
//			if (packageJE == null) continue; //Project or strange JE selected
			
			// Check if a Jar and not selected in the preferences
			IJavaElement jarRoot = null;
			if (selJE instanceof JarPackageFragmentRoot) {
				jarRoot = selJE;
			} else if (selJE instanceof PackageFragment 
					&& ((PackageFragment)selJE).getPackageFragmentRoot() instanceof JarPackageFragmentRoot) {
				jarRoot = ((PackageFragment)selJE).getPackageFragmentRoot();
			} else if (selJE instanceof ClassFile 
					&& ((ClassFile)selJE).getPackageFragmentRoot() instanceof JarPackageFragmentRoot) {
				jarRoot = ((ClassFile)selJE).getPackageFragmentRoot();
			}
			
			if (jarRoot != null) {
				IResource projRes = projJE.getCorrespondingResource();
				String check = projRes.getPersistentProperty(new QualifiedName(PreferenceUtils.pageId, jarRoot.getElementName()));
				
				if (check == null || !check.equalsIgnoreCase("true"))
					itemsNotInBuild.add(jarRoot.getPath().toString());
				continue;
			}
			
			Map unselPackagesMap = BuildSettings.getUnselectedPackagesToSrcFldrMapFromProj(projJE.getElementName());
			
			if (unselPackagesMap==null) continue; //projJE is checked
			if (unselPackagesMap.isEmpty())//projJE is completely unchecked 
				itemsNotInBuild.add(selJE.getPath().toString());
			else if (packageJE != null 
					&& unselPackagesMap.get(packageJE.getElementName()) != null) {
				String parentFolderPath = packageJE.getParent().getPath().toString().replace("/"+projJE.getElementName()+"/", "");
				if (unselPackagesMap.get(packageJE.getElementName()).equals(parentFolderPath)) // project checked but package is not
					itemsNotInBuild.add(selJE.getPath().toString());
			} else if (selJE instanceof IPackageFragmentRoot) {
				IPackageFragmentRoot packageFragRoot = (IPackageFragmentRoot)selJE;
				getChildrenItemsNotInBuild(Arrays.asList(packageFragRoot.getChildren()), itemsNotInBuild);
			} else if (selJE instanceof IJavaProject) {
				IJavaProject projFrag = (IJavaProject)selJE;
				getChildrenItemsNotInBuild(Arrays.asList(projFrag.getChildren()), itemsNotInBuild);
			} 
		}
	}


	private static IPackageFragment getPackage(IJavaElement javaElmt) {
		if (javaElmt instanceof IPackageFragment) return (IPackageFragment) javaElmt;
		if (javaElmt == null) return null;
		else return getPackage(javaElmt.getParent());
	}
	
	private static String buildStrInfo = "\nClick OK to change your build preferences now?\n";
	private static String getDialogMsg(Set<String> itemsNotInBuild) {
		StringBuffer strBuff = new StringBuffer(); 
		strBuff.append("The following items are not in Architexa's build path.\n\n");
		
		if (!itemsNotInBuild.isEmpty()) {
			// dialog gets big if we have too many items. Just add ...
			int count = 0;
			for (String item : itemsNotInBuild) {
				strBuff.append(item + "\n");
				count++;
				if (count>20) {
					strBuff.append("....\n");
					break;
				}
			}
			strBuff.append(buildStrInfo);
		}
		
		return strBuff.toString();
	}

}
