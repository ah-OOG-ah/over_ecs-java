package com.overminddl1.over_ecs.storages;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

public class SparseSet<T> {
	private ArrayList<T> dense;
	private ArrayList<Integer> indices;
	private SparseArray<Integer> sparse;

	public SparseSet(int capacity) {
		this.dense = new ArrayList<T>(capacity);
		this.indices = new ArrayList<Integer>(capacity);
		this.sparse = new SparseArray<Integer>();
	}

	public SparseSet() {
		this(0);
	}

	public void insert(int index, T value) {
		Integer dense_index = this.sparse.get(index);
		if (dense_index != null) {
			this.dense.set(dense_index, value);
		} else {
			this.sparse.insert(index, this.dense.size());
			this.indices.add(index);
			this.dense.add(value);
		}
	}

	public T get_or_insert(int index, T value) {
		Integer dense_index = this.sparse.get(index);
		if (dense_index != null) {
			return this.dense.get(dense_index);
		} else {
			this.sparse.insert(index, this.dense.size());
			this.indices.add(index);
			this.dense.add(value);
			return value;
		}
	}

	public T get_or_insert_with(int index, Supplier<T> func) {
		Integer dense_index = this.sparse.get(index);
		if (dense_index != null) {
			return this.dense.get(dense_index);
		} else {
			T value = func.get();
			this.sparse.insert(index, this.dense.size());
			this.indices.add(index);
			this.dense.add(value);
			return value;
		}
	}

	public T replace_with(int index, Function<T, T> func) {
		Integer dense_index = this.sparse.get(index);
		if (dense_index != null) {
			T value = this.dense.get(dense_index);
			value = func.apply(value);
			if (value != null) {
				this.dense.set(dense_index, value);
			} else {
				this.remove(index);
			}
			return value;
		} else {
			T value = func.apply(null);
			if (value != null) {
				this.sparse.insert(index, this.dense.size());
				this.indices.add(index);
				this.dense.add(value);
			}
			return value;
		}
	}

	public int size() {
		return this.dense.size();
	}

	public boolean contains(int index) {
		return this.sparse.contains(index);
	}

	public T get(int index) {
		Integer dense_index = this.sparse.get(index);
		if (dense_index != null) {
			return this.dense.get(dense_index);
		} else {
			return null;
		}
	}

	public T remove(int index) {
		Integer dense_index = this.sparse.remove(index);
		if (dense_index != null) {
			boolean is_last = dense_index == this.dense.size() - 1;
			T value = StorageUtils.swap_remove(this.dense, dense_index);
			StorageUtils.swap_remove(this.indices, dense_index);
			if (!is_last) {
				int swapped_index = this.indices.get(dense_index);
				this.sparse.insert(swapped_index, dense_index);
			}
			return value;
		} else {
			return null;
		}
	}

	public ArrayList<Integer> getIndices() {
		return indices;
	}

	public ArrayList<T> getValues() {
		return dense;
	}
}
