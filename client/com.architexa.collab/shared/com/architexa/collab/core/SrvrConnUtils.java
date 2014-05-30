package com.architexa.collab.core;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SrvrConnUtils {

	public static void addParam(JSONObject param, String paramKey, Object paramValue) {
		// want to ideally do >addParam(param, paramKey, paramValue);< but jersey expects paramValue to be an array
		JSONArray arrParam = new JSONArray();
		arrParam.add(paramValue);
		param.put(paramKey, arrParam);
	}

}
