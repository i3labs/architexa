package com.architexa.rse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.TreeItem;

import com.architexa.intro.AtxaIntroPlugin;

public class BuildSettings {
	
	private static final String UNSELECTED_PROJECT_MAP = "projList";
	private static final String BUILD_SCHEDULE = "buildSchedule";
	private static final String BUILD_TIME = "buildTime";
	
	private static final String DEFAULT_PROJECT_MAP = "";
	public static final String DEFAULT_BUILD_SCHEDULE = "silent";
	private static final String DEFAULT_BUILD_TIME = "2";
	
	public static final String BUILD_REQUEST = "onRequest";
	public static final String BUILD_DAILY = "daily";
	public static final String BUILD_INSTANTLY = "instantly";
	public static final String BUILD_SILENTLY = "silent";
	
	
	public static void initializeDefaultPreferences() {
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(UNSELECTED_PROJECT_MAP, DEFAULT_PROJECT_MAP);
		store.setDefault(BUILD_SCHEDULE, DEFAULT_BUILD_SCHEDULE);
		store.setDefault(BUILD_TIME, DEFAULT_BUILD_TIME);
	}
	
	private static IPreferenceStore getPreferenceStore() {
		return AtxaIntroPlugin.getDefault().getPreferenceStore();
	}
	
	public static void addProjectSettings(String elementName, Map pckgToSrcFldrMap) {
		Map map = getStoredUnselectedProjPackgMap();
		if (pckgToSrcFldrMap == null) {
			// if proj not in list all the packages are selected do nothing
			if (!map.keySet().contains(elementName))
				return;
			// proj in list and package list null remove from map
			map.remove(elementName);
			setStoredUnselectedProjPackgMap(map);
			return;
		}
		
		map.put(elementName, pckgToSrcFldrMap.keySet());
		setStoredUnselectedProjPackgMap(map, pckgToSrcFldrMap);
	}

	// This map is from the package name to a list of src folders it is contained in.
	// Typically it will only be contained in one folder.
	// It is generated from the saved preference string
	public static Map<String, List<String>> itemToSrcMap = new HashMap<String, List<String>>();
	
	public static Map getStoredUnselectedProjPackgMap() {
		String[] strArr = getPreferenceStore().getString(UNSELECTED_PROJECT_MAP).split(";");
		Map projTopackgMap = new HashMap();
		itemToSrcMap.clear();
		for (int i = 0; i < strArr.length; i++) {
			String val = strArr[i];
			String[] keyList = val.split("::");
			if (keyList.length == 0) continue;
			if (keyList.length == 1) { 
				projTopackgMap.put(keyList[0], new ArrayList());
				continue;
			}
			
			String packgStr = keyList[1];
			String[] packgs = packgStr.split(",");
			List packgList = new ArrayList();
			for (int j = 0; j < packgs.length; j++) {
				if (packgs[j].contains("[")) {
					String item = packgs[j].substring(packgs[j].indexOf("]")+1);
					String srcName = packgs[j].substring(packgs[j].indexOf("[")+1,packgs[j].indexOf("]")); 
					
					List<String> srcFldrList = itemToSrcMap.get(item);
					if (srcFldrList == null)
						srcFldrList = new ArrayList<String>();
					if (!srcFldrList.contains(srcName))
						srcFldrList.add(srcName);
					itemToSrcMap.put(item, srcFldrList);
					
				}
				String pckg = packgs[j].substring(packgs[j].indexOf("]")+1);
				packgList.add(pckg);
			}
			
			projTopackgMap.put(keyList[0], packgList);
		}
		
		return projTopackgMap;
	}
	
	public static List getUnselectedPackagesFromProj (String projName) {
		String[] strArr = getPreferenceStore().getString(UNSELECTED_PROJECT_MAP).split(";");
		for (int i = 0; i < strArr.length; i++) {
			String val = strArr[i];
			String[] keyList = val.split("::");
			if (keyList == null || keyList.length==0) continue;
			if (projName.equals(keyList[0])) {
				List<Object> packgList = new ArrayList<Object>();
				if (keyList.length == 1) return packgList;
				String packgStr = keyList[1];
				String[] packgs = packgStr.split(",");
				for (int j = 0; j < packgs.length ; j++)
					packgList.add(packgs[j]);
				return packgList;
			}
		}
		return null;
	}
	public static Map getUnselectedPackagesToSrcFldrMapFromProj (String projName) {
		String[] strArr = getPreferenceStore().getString(UNSELECTED_PROJECT_MAP).split(";");
		for (int i = 0; i < strArr.length; i++) {
			String val = strArr[i];
			String[] keyList = val.split("::");
			if (keyList == null || keyList.length==0) continue;
			if (projName.equals(keyList[0])) {
				Map<Object,Object> packgMap = new HashMap<Object,Object>();
				if (keyList.length == 1) return packgMap;
				String packgStr = keyList[1];
				String[] packgs = packgStr.split(",");
				for (int j = 0; j < packgs.length ; j++) {
					if (packgs[j].contains("[")) {
						String srcFldr = packgs[j].substring(packgs[j].indexOf("[")+1, packgs[j].indexOf("]"));
						String pckg = packgs[j].substring(packgs[j].indexOf("]")+1);
						packgMap.put(pckg, srcFldr);
					}
				}
					
				return packgMap;
			}
		}
		return null;
	}

	public static void setStoredUnselectedProjPackgMap(Map projToUnckdPackgMap) {
		setStoredUnselectedProjPackgMap(projToUnckdPackgMap, null);
	}

	// stored as: {projName}::[srcFldrName]pckgName,[srcFldrName]pckgName
	public static void setStoredUnselectedProjPackgMap(Map projToUnckdPackgMap, Map itemToSrcFldrMap) {
		StringBuffer buff = new StringBuffer();
		List projects = new ArrayList(projToUnckdPackgMap.keySet());
		int size = projects.size();
		for (int i = 0; i < size ; i++) {
			Object prj = projects.get(i);
			List packgList = new ArrayList ((Collection)projToUnckdPackgMap.get(prj));
			String projName;
			if (prj instanceof TreeItem)
				projName = ((TreeItem) prj).getText();
			else 
				projName = (String) prj;
			buff.append(projName + "::");
			for (int j = 0; j < packgList.size(); j++) {
				Object packg = packgList.get(j);
				String packgName;
				if (packg instanceof TreeItem)
					packgName = ((TreeItem) packg).getText();
				else 
					packgName = (String) packg;
				if (itemToSrcFldrMap!=null && itemToSrcFldrMap.get(packg) != null) {
					if (itemToSrcFldrMap.get(packg) instanceof TreeItem)
						buff.append("["+ ((TreeItem) itemToSrcFldrMap.get(packg)).getText() +"]");
					else
						buff.append("["+ itemToSrcFldrMap.get(packg) +"]");
				}
					
				buff.append(packgName);
				
				if(j < packgList.size() - 1)
					buff.append(",");
			}
			
			if (i < size - 1)
				buff.append(";");
		}
		getPreferenceStore().setValue(UNSELECTED_PROJECT_MAP, buff.toString());
	}
	
	public static void setDefaultStoredUnselectedProjPackgMap() {
		getPreferenceStore().setValue(UNSELECTED_PROJECT_MAP, DEFAULT_PROJECT_MAP);
	}

	public static String getBuildSchedule() {
		return getPreferenceStore().getString(BUILD_SCHEDULE);
	}
	public static String getBuildDailyTime() {
		return getPreferenceStore().getString(BUILD_TIME);
	}
	
	public static void setBuildSchedule(String schedule) {
		getPreferenceStore().setValue(BUILD_SCHEDULE, schedule);
	}
	public static void setBuildTime(String buildTimeFromDialog) {
		getPreferenceStore().setValue(BUILD_TIME, buildTimeFromDialog);
	}
	
	
	// check if incremental build should run. (Should only run if building 'instantaneously'
	public static boolean checkBuildSchedule() {
		if (getBuildSchedule().equals(""))
			setBuildSchedule(DEFAULT_BUILD_SCHEDULE);
		return getBuildSchedule().equals(BUILD_INSTANTLY);
	}

	public static boolean checkBuildTime() {
		Calendar now = Calendar.getInstance();
		if (getBuildDailyTime()==null || getBuildDailyTime().equals(""))
			setBuildTime(DEFAULT_BUILD_TIME);
		return now.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(getBuildDailyTime());
	}

	public static boolean isDailyBuild() {
		return getBuildSchedule().equals(BUILD_DAILY);
	}
	
	public static boolean isSilentBuild() {
		if (getBuildSchedule().equals(""))
			setBuildSchedule(DEFAULT_BUILD_SCHEDULE);
		return getBuildSchedule().equals(BUILD_SILENTLY);
	}

	public static Map<String, List<String>> findDuplicatedPackages() {
		Map<String, List<String>> projectToDupPckgsMap = new HashMap<String, List<String>>();
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
				
				List<IPackageFragment> packageList = Arrays.asList(project.getPackageFragments());
				Collections.sort(packageList, new Comparator<IPackageFragment>() {
					public int compare(IPackageFragment o1, IPackageFragment o2) {
						return o1.getElementName().compareToIgnoreCase(o2.getElementName());
					}
				});
				List<String> duplicatePackages = new ArrayList<String>();
				IPackageFragment tmp = null;
				for (IPackageFragment pckg : packageList) {
					String elementName = pckg.getElementName();
					if (tmp!=null && tmp.getElementName().equals(elementName) && !duplicatePackages.contains(elementName))
						duplicatePackages.add(elementName);
					tmp = pckg;
				}
				projectToDupPckgsMap.put(project.getElementName(), duplicatePackages);
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return projectToDupPckgsMap;
	}

}
