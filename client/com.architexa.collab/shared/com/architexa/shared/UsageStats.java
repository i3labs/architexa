package com.architexa.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class UsageStats {
	
	public static SharedLogger logger = new SharedLogger();
	 
	private Map currStats = new HashMap();
	
	private Map diagramToActionsMap = new HashMap();
	
	private Map diagramToTypeMap = new HashMap();
	
	private Map diagramToItemsMap = new HashMap();
	
	private Map diagramToClosingDateMap = new HashMap();
	
	private Map diagramToOpeningDateMap = new HashMap();
	
	public static String NEW_STATS_SEPARATOR = "//;;//";
	public static String ACTIONS_SEPARATOR = ";";
	public static String KEY_VALUE_SEPARATOR = "/;/";
	public static String MAP_VALUES_SEPARATOR = ";;";
	public static String ATXA_STATS = "atxaStats--";
	
	public UsageStats() {}
	
	public UsageStats(String initVal) {
		if (initVal == null) return;
		
		String[] arr = initVal.split(NEW_STATS_SEPARATOR);
		if (arr == null || arr.length == 0) return;
		if (arr.length == 2)
			updateMap(arr[1]);
		
		String[] entries = arr[0].split(";");
		for(int i=0; i<entries.length; i++) {
			if (entries[i].length() == 0) continue;
			int entryNdx = entries[i].indexOf(":");
			String key = null;
			int val = 1;
			if (entryNdx == -1) {
				key = entries[i].substring(0, entries[i].length());
				// no value - just assume default (val = 1)
			} else {
				key = entries[i].substring(0, entryNdx);

				String valStr = entries[i].substring(entryNdx + 1);
				try {
					val = Integer.parseInt(valStr);
				} catch (NumberFormatException nfe) {
					System.err.println("key: '" + key + "' stats cannot be parsed");
					val =0;
				} finally {
					currStats.put(key, new Integer(val));
					continue;
				}
			}
			currStats.put(key, new Integer(val));
		}
	}
	
	/*
	 * All this should be put in a JSON map instead of a string and then sent to the client.
	 * next update should be made to convert it to a JSON map before sending 
	 */
	
	public JSONObject getUsageStatsJSON() {
		JSONObject obj = new JSONObject();
		obj.put("oldStr", getSortedByValStrNoMap());
		JSONArray diagramArr = populateAndGetDiagramArr();
		obj.put("diagramArr", diagramArr);
		return obj;
	}
	
	private JSONArray populateAndGetDiagramArr() {
		JSONArray diagramArr = new JSONArray();
		Set keys = diagramToTypeMap.keySet();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			String actions = (String) diagramToActionsMap.get(key);
			String type = (String) diagramToTypeMap.get(key);
			Integer items = (Integer) diagramToItemsMap.get(key);
			String openingDate = (String) diagramToOpeningDateMap.get(key);
			String closingDate = (String) diagramToClosingDateMap.get(key);
			if (closingDate == null) // If the diagram hasnt been closed yet
				closingDate = new Date().toString();
			if (items == null)
				items = new Integer(0);
			JSONObject obj = new JSONObject();
			obj.put("actions", actions);
			obj.put("items", items);
			obj.put("type", type);
			obj.put("openingDate", openingDate);
			obj.put("closingDate", closingDate);
			diagramArr.add(obj);
		}
		return diagramArr;
	}

	private void updateMap(String stringMap) {
		String[] mapEntries = stringMap.split(MAP_VALUES_SEPARATOR);
		for (int i = 0; i < mapEntries.length; i++) {
			String[] val = mapEntries[i].split(KEY_VALUE_SEPARATOR);
			if (val.length != 6) continue;
			//ID
			String key = val[0];
			//Type
			diagramToTypeMap.put(key, val[1]);
			//Opening Date
			diagramToOpeningDateMap.put(key, val[2]);
			//Closing Date
			diagramToClosingDateMap.put(key, val[3]);
			//Items
			diagramToItemsMap.put(key, Integer.valueOf(val[4]));
			//Actions
			diagramToActionsMap.put(key, val[5]);
		}
	}

	public void addDiagramType(String diagramId, String type) {
		diagramToTypeMap.put(diagramId, type);
		diagramToOpeningDateMap.put(diagramId, new Date().toString());
	}
	
	public void addToDiagramActionMap(String diagramId, String action) {
		if (diagramToActionsMap.containsKey(diagramId))
			diagramToActionsMap.put(diagramId, diagramToActionsMap.get(diagramId) + ACTIONS_SEPARATOR + action);
		else
			diagramToActionsMap.put(diagramId, action);
			
	}
	
	public void addToDiagramItemMap(String diagramId, Integer items){
		diagramToItemsMap.put(diagramId, items);
		diagramToClosingDateMap.put(diagramId, new Date().toString());
	}
	
	private static class SortComparator implements Comparator  {
		public int compare(Object lhs, Object rhs) {
			int lval = ((Integer) ((Map.Entry)lhs).getValue()).intValue();
			int rval = ((Integer) ((Map.Entry)rhs).getValue()).intValue();
			if (rval != lval) return rval - lval;
			// rval and lval are identical - compare by key
			String lkey = (String) ((Map.Entry)lhs).getKey();
			String rkey = (String) ((Map.Entry)rhs).getKey();
			return lkey.compareTo(rkey);
		}
	}

//	public String getAsStr() {
//		return getStr(currStats.entrySet().iterator());
//		//return getSortedByValStr();
//	}
	public String getSortedByValStr() {
		Map.Entry[] currStatsAsArr = (Map.Entry[]) currStats.entrySet().toArray(new Map.Entry[] {});
		Arrays.sort(currStatsAsArr, new SortComparator());
		return getStr(Arrays.asList(currStatsAsArr).iterator(), true);
	}
	
	private String getStr(Iterator mapEntryIt, boolean addMap) {
		StringBuffer currVal = new StringBuffer();
		while (mapEntryIt.hasNext()) {
			Map.Entry entry = (Entry) mapEntryIt.next();
			currVal.append(entry.getKey() + ":" + entry.getValue() + ";");
		}
		
		if (addMap) addMapValues(currVal);
		return currVal.toString();
	}
	
	public String getSortedByValStrNoMap() {
		Map.Entry[] currStatsAsArr = (Map.Entry[]) currStats.entrySet().toArray(new Map.Entry[] {});
		Arrays.sort(currStatsAsArr, new SortComparator());
		return getStr(Arrays.asList(currStatsAsArr).iterator(), false);
	}
	
	private void addMapValues(StringBuffer currVal) {
		currVal.append(NEW_STATS_SEPARATOR);
		
		List keys = new ArrayList(diagramToTypeMap.keySet());
		for (int i = 0; i < keys.size(); i++) {
			String key = (String) keys.get(i);
			//ID
			currVal.append(key);
			currVal.append(KEY_VALUE_SEPARATOR);
			//Type
			currVal.append(diagramToTypeMap.get(key));
			currVal.append(KEY_VALUE_SEPARATOR);
			//Opening Date 
			currVal.append(getDiagramOpeningDate(key));
			currVal.append(KEY_VALUE_SEPARATOR);
			//Closing Date 
			currVal.append(getDiagramClosingDate(key));
			currVal.append(KEY_VALUE_SEPARATOR);
			//Items
			currVal.append(getDiagramItems(key));
			currVal.append(KEY_VALUE_SEPARATOR);
			//Actions
			currVal.append(getDiagramActions(key));
			currVal.append(MAP_VALUES_SEPARATOR);
		}
	}

	private String getDiagramClosingDate(String key) {
		if (diagramToClosingDateMap.containsKey(key))
			return (String)diagramToClosingDateMap.get(key);
		return new Date().toString();
	}
	
	private String getDiagramOpeningDate(String key) {
		if (diagramToOpeningDateMap.containsKey(key))
			return (String)diagramToOpeningDateMap.get(key);
		return new Date().toString();
	}
	
	private Integer getDiagramItems(String key) {
		if (diagramToItemsMap.containsKey(key))
			return (Integer) diagramToItemsMap.get(key);
		return new Integer(0);
	}
	
	private String getDiagramActions(String key) {
		if (diagramToActionsMap.containsKey(key))
			return (String) diagramToActionsMap.get(key);
		return "";
	}
	
	public void addEntry(String key, int val) {
		Object oldValObj = currStats.get(key);
		int oldVal = 0;
		if (oldValObj instanceof Integer) oldVal = ((Integer)oldValObj).intValue();
		currStats.put(key, new Integer(oldVal + val));
	}
	public void replaceEntry(String key, Integer i) {
		currStats.put(key, i);
	}

	public void addEntry(String key) {
		addEntry(key, 1);
	}
	
	public void clear() {
		currStats.clear();
		diagramToActionsMap.clear();
		diagramToItemsMap.clear();
		diagramToOpeningDateMap.clear();
		diagramToClosingDateMap.clear();
		diagramToTypeMap.clear();
	}

	public void merge(UsageStats toMerge) {
		Iterator entries = toMerge.currStats.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Entry) entries.next();
			addEntry(entry.getKey().toString(), ((Integer)entry.getValue()).intValue());
		}
	}
}
