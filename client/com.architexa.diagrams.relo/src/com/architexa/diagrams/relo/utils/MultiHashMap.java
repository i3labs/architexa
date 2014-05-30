/* 
 * Copyright (c) 2004-2005 Massachusetts Institute of Technology. This code was
 * developed as part of the Haystack (http://haystack.lcs.mit.edu/) research 
 * project at MIT. Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER  
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */

/*
 * Created on Sep 3, 2005
 */
package com.architexa.diagrams.relo.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * A Map with slightly different semantics. Putting a value into the map will
 * add the value to a Collection at that key. Getting a value will return a
 * Collection, holding all the values put to that key.
 * 
 * Wraps the apache commons MultiHashMap while providing typing information, we
 * don't implement Map<K,V> sice we return different types.
 * 
 * Javadoc copied from MultiHashMap
 * 
 * @see org.apache.commons.collections.MultiHashMap
 * @see org.apache.commons.collections.MultiMap
 * 
 * @author vineet
 */
public class MultiHashMap<K,V> implements Cloneable {

    org.apache.commons.collections.MultiHashMap internalMap;

    public MultiHashMap() {
        internalMap = new org.apache.commons.collections.MultiHashMap();
    }

    public MultiHashMap(int sz) {
        internalMap = new org.apache.commons.collections.MultiHashMap(sz);
    }

    public MultiHashMap(org.apache.commons.collections.MultiHashMap innerMap) {
        this.internalMap = innerMap;
    }

    /**
     * Gets the number of keys in this map.
     *
     * @return the number of key-collection mappings in this map
     */
    public int size() {
        return internalMap.size();
    }

    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    public boolean containsKey(Object arg0) {
        return internalMap.containsKey(arg0);
    }

    /**
     * Checks whether the map contains the value specified.
     * <p>
     * This checks all collections against all keys for the value, and thus could be slow.
     * 
     * @param value  the value to search for
     * @return true if the map contains the value
     */
    public boolean containsValue(Object arg0) {
        return internalMap.containsValue(arg0);
    }

    /**
     * Gets the collection of values associated with the specified key.
     * <p>
     * Implementations typically return <code>null</code> if no values have
     * been mapped to the key, however the implementation may choose to
     * return an empty collection.
     * <p>
     * Implementations may choose to return a clone of the internal collection.
     *
     * @param key  the key to retrieve
     * @return the <code>Collection</code> of values, implementations should
     *  return <code>null</code> for no mapping, but may return an empty collection
     * @throws ClassCastException if the key is of an invalid type
     * @throws NullPointerException if the key is null and null keys are invalid
     */
    @SuppressWarnings("unchecked")
    public Collection<V> get(Object arg0) {
        return (Collection<V>) internalMap.get(arg0);
    }

    /**
     * Adds the value to the collection associated with the specified key.
     * <p>
     * Unlike a normal <code>Map</code> the previous value is not replaced.
     * Instead the new value is added to the collection stored against the key.
     *
     * @param key  the key to store against
     * @param value  the value to add to the collection at the key
     * @return the value added if the map changed and null if the map did not change
     */    
    @SuppressWarnings("unchecked")
    public V put(K arg0, V arg1) {
        return (V) internalMap.put(arg0, arg1);
    }

    /**
     * Adds a collection of values to the collection associated with the specified key.
     *
     * @param key  the key to store against
     * @param values  the values to add to the collection at the key, null ignored
     * @return true if this map changed
     * @since Commons Collections 3.1
     */    
    public boolean putAll(K key, Collection<V> values) {
        return internalMap.putAll(key, values);
    }

    /**
     * Removes all values associated with the specified key.
     * <p>
     * @param key  the key to remove values from
     * @return the <code>Collection</code> of values removed, implementations should
     *  return <code>null</code> for no mapping found, but may return an empty collection
     * @throws UnsupportedOperationException if the map is unmodifiable
     * @throws ClassCastException if the key is of an invalid type
     * @throws NullPointerException if the key is null and null keys are invalid
     */
    @SuppressWarnings("unchecked")
    public V remove(Object arg0) {
        return (V) internalMap.remove(arg0);
    }

    /**
     * Removes a specific value from map.
     * <p>
     * The item is removed from the collection mapped to the specified key.
     * Other values attached to that key are unaffected.
     * <p>
     * If the last value for a key is removed, <code>null</code> will be returned
     * from a subsequant <code>get(key)</code>.
     * 
     * @param key  the key to remove from
     * @param item  the value to remove
     * @return the value removed (which was passed in), null if nothing removed
     */
    @SuppressWarnings("unchecked")
    public V remove(Object key, Object item) {
        return (V) internalMap.remove(key, item);
    }

    @SuppressWarnings("unchecked")
    public void putAll(Map<? extends K, ? extends V> arg0) {
        internalMap.putAll(arg0);
    }

    public void clear() {
        internalMap.clear();
    }

    @SuppressWarnings("unchecked")
    public Set<K> keySet() {
        return internalMap.keySet();
    }

    /**
     * Gets a collection containing all the values in the map.
     * <p>
     * This returns a collection containing the combination of values from all keys.
     *
     * @return a collection view of the values contained in this map
     */
    @SuppressWarnings("unchecked")
    public Collection<V> values() {
        return internalMap.values();
    }

    @SuppressWarnings("unchecked")
    public Set<Entry<K, Collection<V>>> entrySet() {
        return internalMap.entrySet();
    }

    @Override
    public Object clone() {
        return new MultiHashMap((org.apache.commons.collections.MultiHashMap) internalMap.clone());
    }
}
