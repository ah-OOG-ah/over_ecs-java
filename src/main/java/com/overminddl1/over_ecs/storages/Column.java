package com.overminddl1.over_ecs.storages;

import com.overminddl1.over_ecs.Component;
import com.overminddl1.over_ecs.components.ComponentInfo;
import com.overminddl1.over_ecs.components.ComponentTicks;

import java.util.ArrayList;

public class Column {
	public int component_id;
	public ArrayList<Component> data;
	public ArrayList<ComponentTicks> ticks;

	public Column(ComponentInfo info, int capacity) {
		this.component_id = info.getId();
		data = new ArrayList<Component>(capacity);
		ticks = new ArrayList<ComponentTicks>(capacity);
	}

	public void initialize(int row, Component data, ComponentTicks ticks) {
		assert (row < this.size());
		this.data.set(row, data);
		this.ticks.set(row, ticks);
	}

	public void replace(int row, Component data, int change_tick) {
		assert (row < this.size());
		this.data.set(row, data);
		this.ticks.get(row).set_changed(change_tick);
	}

	public void initialize_data(int row, Component data) {
		assert (row < this.size());
		this.data.set(row, data);
	}

	public void add(Component data, ComponentTicks ticks) {
		this.data.add(data);
		this.ticks.add(ticks);
	}

	public int size() {
		return this.data.size();
	}

	public ComponentTicks get_ticks(int row) {
		assert (row < this.size());
		return this.ticks.get(row);
	}

	public void swap_remove(int row) {
		assert (row < this.size());
		StorageUtils.swap_remove(this.data, row);
		StorageUtils.swap_remove(this.ticks, row);
	}

	public void swap_remove(int row, ColumnSwapRemoveResult result) {
		assert (row < this.size());
		result.data = StorageUtils.swap_remove(this.data, row);
		result.ticks = StorageUtils.swap_remove(this.ticks, row);
	}

	public void reserveCapacity(int capacity) {
		this.data.ensureCapacity(this.data.size() + capacity);
		this.ticks.ensureCapacity(this.ticks.size() + capacity);
	}

	public void clear() {
		this.data.clear();
		this.ticks.clear();
	}

	public void set_len(int size) {
		while (this.data.size() < size) {
			this.data.add(null);
		}
	}

	public void check_change_ticks(int change_tick) {
		for (int i = 0; i < this.ticks.size(); i++) {
			this.ticks.get(i).check_ticks(change_tick);
		}
	}

	public Object get_data(int table_row) {
		return this.data.get(table_row);
	}
}
