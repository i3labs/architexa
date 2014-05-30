package com.architexa.diagrams.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {

	// Default
	private static final long serialVersionUID = 1L;
	private final int maxEntries;

	public LRUCache(int maxEntries) {
		super(maxEntries, (float) 0.75, true);
		this.maxEntries = maxEntries;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxEntries;
	}
	
}
