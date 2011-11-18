package org.archive.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NestedMap<K,V> implements Map<K,V> {
	private Map<K, V> inner = null;
	private Map<K,V> parent = null;
	public NestedMap() {
		this(null);
	}
	public NestedMap(Map<K,V> parent) {
		this(parent,new HashMap<K, V>());
	}
	public NestedMap(Map<K,V> parent, Map<K,V> inner) {
		this.parent = parent;
		this.inner = inner;
	}
	

	public void setParent(Map<K,V> parent) {
		this.parent = parent;
	}

	public void clear() {
		inner.clear();
		parent = null;
	}

	public boolean containsKey(Object key) {
		return inner.containsKey(key) ? true : 
			parent == null ? false : parent.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return inner.containsValue(value) ? true : 
			parent == null ? false : parent.containsValue(value);
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		if(parent == null) {
			return inner.entrySet();
		}
		HashSet<java.util.Map.Entry<K, V>> tmp = 
			new HashSet<java.util.Map.Entry<K,V>>();

		tmp.addAll(parent.entrySet());
		tmp.addAll(inner.entrySet());
		return tmp;
	}

	public V get(Object key) {
		if(inner.containsKey(key)) {
			return inner.get(key);
		}
		return (parent == null) ? null : parent.get(key);
	}

	public boolean isEmpty() {
		return inner.isEmpty() && ((parent == null) ? true : parent.isEmpty());
	}

	public Set<K> keySet() {
		if(parent == null) {
			return inner.keySet();
		}
		HashSet<K> tmp = new HashSet<K>(); 
		tmp.addAll(parent.keySet());
		tmp.addAll(inner.keySet());
		return tmp;
	}

	public V put(K key, V value) {
		return inner.put(key, value);
	}

	public void putAll(Map<? extends K, ? extends V> map) {
		inner.putAll(map);
	}

	public V remove(Object key) {
		return inner.remove(key);
	}

	public int size() {
		return keySet().size();
//		return inner.size() + (parent == null ? 0 : parent.size());
	}

	public Collection<V> values() {
		if(parent == null) {
			return inner.values();
		}
		ArrayList<V> tmp = new ArrayList<V>(); 
		tmp.addAll(parent.values());
		tmp.addAll(inner.values());
		return tmp;
	}

}
