/*
 * Created on Jul 15, 2004
 *
 */
package com.architexa.diagrams.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides debugging support.
 * 
 * It works primarily by giving a unique id to each instance of a using
 * class. This can be helpful to track if a particular class is being
 * instantiated unexpectedly
 * 
 * It can also be configured to track various instantiated instances if needed.
 * 
 * @author vineet
 */
public class DbgRes {

	public int id = -1;
	public String clsSig = "";

	public static HashMap<Class<?>, Object> classMap = new HashMap<Class<?>, Object>();

	public DbgRes(Class<?> cls, Object instance) {
		initIdPerClass(cls);
		// initIdPerClassAndLogInstance(cls, instance);
	}
	public DbgRes(Class<?> cls, Object instance, String _clsSig) {
		this(cls, instance);
		clsSig = _clsSig + ":";
	}

	public static void restartIds() {
		classMap.clear();
	}

	@SuppressWarnings("unchecked")
	protected void initIdPerClassAndLogInstance(Class<?> cls, Object instance) {
		Map<Integer,Object> prevDataMap = (Map<Integer,Object>) classMap.get(cls);
		if (prevDataMap == null) {
			id = 0;
			prevDataMap = new HashMap<Integer,Object>();
		} else {
			id = prevDataMap.size() + 1;
		}
		prevDataMap.put(new Integer(id), instance);
		classMap.put(cls, prevDataMap);
	}

	private void initIdPerClass(Class<?> cls) {
		Integer prevId = (Integer) classMap.get(cls);
		if (prevId == null) {
			id = 0;
		} else {
			id = prevId.intValue() + 1;
		}
		classMap.put(cls, new Integer(id));
	}

	public String getAbsoluteTrailer() {
		return this.toString();
	}

	public String getTrailer() {
		// return getAbsoluteTrailer();
		return " ";
	}

	@Override
	public String toString() {
		return "/" + clsSig + id;
	}

	// only makes sense in option two above
	@SuppressWarnings("unchecked")
	public static Map getDataMap(String className) {
		// perhaps we should store this map with class name instead of class 
		// (or take class as the param)
		for (Class<?> cls : classMap.keySet()) {
			if (cls.toString().indexOf(className) != -1) {
				return (Map<Integer,Object>) classMap.get(cls);
			}
		}
		return null;
	}

}