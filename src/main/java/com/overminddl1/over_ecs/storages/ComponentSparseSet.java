package com.overminddl1.over_ecs.storages;

import com.overminddl1.over_ecs.Entity;
import com.overminddl1.over_ecs.components.ComponentInfo;
import com.overminddl1.over_ecs.components.ComponentTicks;

import java.util.ArrayList;

public class ComponentSparseSet {
	private ArrayList<Object> dense;
	private ArrayList<ComponentTicks> ticks;
	private ArrayList<Long> entities;
	private SparseArray<Integer> sparse;

	public ComponentSparseSet(ComponentInfo _component_info, int capacity) {
		this.dense = new ArrayList<Object>(capacity);
		this.ticks = new ArrayList<ComponentTicks>(capacity);
		this.entities = new ArrayList<Long>(capacity);
		this.sparse = new SparseArray<Integer>();
	}

	public void clear() {
		this.dense.clear();
		this.ticks.clear();
		this.entities.clear();
		this.sparse.clear();
	}

	public int size() {
		return this.dense.size();
	}

	public void insert(long entity, Object value, int change_tick) {
		int entity_id = Entity.id(entity);
		Integer dense_index = this.sparse.get(entity_id);
		if (dense_index != null) {
			this.dense.set(dense_index, value);
			this.ticks.set(dense_index, new ComponentTicks(change_tick));
		} else {
			int dense_index_ = this.dense.size();
			this.dense.add(dense_index_, value);
			this.sparse.insert(entity_id, dense_index_);
			assert (this.ticks.size() == dense_index_);
			assert (this.entities.size() == dense_index_);
			this.ticks.add(new ComponentTicks(change_tick));
			this.entities.add(entity);
		}
	}

	public boolean contains(long entity) {
		return this.sparse.contains(Entity.id(entity));
	}

	public Object get(long entity) {
		Integer dense_index = this.sparse.get(Entity.id(entity));
		return dense_index != null ? this.dense.get(dense_index) : null;
	}

	public ComponentTicks get_ticks(long entity) {
		Integer dense_index = this.sparse.get(Entity.id(entity));
		return dense_index != null ? this.ticks.get(dense_index) : null;
	}

	public Object remove(long entity) {
		Integer dense_index = this.sparse.remove(Entity.id(entity));
		if (dense_index != null) {
			StorageUtils.swap_remove(this.ticks, dense_index);
			StorageUtils.swap_remove(this.entities, dense_index);
			boolean is_last = dense_index == this.dense.size() - 1;
			Object value = StorageUtils.swap_remove(this.dense, dense_index);
			if (!is_last) {
				long swapped_entity = this.entities.get(dense_index);
				this.sparse.insert(Entity.id(swapped_entity), dense_index);
			}
			return value;
		}
		return null;
	}

	public void check_change_ticks(int change_tick) {
		for (int i = 0; i < this.ticks.size(); i++) {
			this.ticks.get(i).check_ticks(change_tick);
		}
	}
}
