package com.overminddl1.over_ecs.storages;

import java.util.ArrayList;
import java.util.function.Supplier;

public class SparseArray<V> {
	// Maybe make this a paged array for lower memory use at the cost of one extra indirection?
	private ArrayList<V> values;

	public SparseArray() {
		this.values = new ArrayList<V>();
	}

	public SparseArray(int capacity) {
		this.values = new ArrayList<V>(capacity);
	}

	public void insert(int index, V value) {
		while(index >= this.values.size()) {
			this.values.add(null);
		}
		values.set(index, value);
    }

	public boolean contains(int index) {
		if(index >= this.values.size()) {
			return false;
		}
		return this.values.get(index) != null;
	}

	public V get(int index) {
		if(index >= this.values.size()) {
			return null;
		}
		return this.values.get(index);
	}

	public V remove(int index) {
		if(index >= this.values.size()) {
			return null;
		}
		V value = this.values.get(index);
		this.values.set(index, null);
		return value;
	}

	public V get_or_insert_with(int index, Supplier<V> func) {
		while(index >= this.values.size()) {
			this.values.add(null);
		}
		V value = this.values.get(index);
		if(value == null) {
			value = func.get();
			this.values.set(index, value);
		}
		return value;
	}

	public void clear() {
		this.values.clear();
	}
}
