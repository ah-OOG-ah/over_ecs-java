package com.overminddl1.over_ecs.storages;

import com.overminddl1.over_ecs.components.ComponentInfo;

import java.util.ArrayList;

public class SparseSets {
	private SparseSet<ComponentSparseSet> sets;

	public SparseSets() {
		this.sets = new SparseSet<ComponentSparseSet>();
	}

	public ComponentSparseSet get_or_insert(ComponentInfo info) {
		ComponentSparseSet set = this.sets.get(info.getId());
		if (set == null) {
			set = new ComponentSparseSet(info, 64);
			this.sets.insert(info.getId(), set);
		}
		return set;
	}

	public ComponentSparseSet get(int component_id) {
		return this.sets.get(component_id);
	}

	public void clear() {
		ArrayList<ComponentSparseSet> values = this.sets.getValues();
		for (int i = 0; i < values.size(); i++) {
			values.get(i).clear();
		}
	}

	public void check_change_ticks(int change_tick) {
		ArrayList<ComponentSparseSet> values = this.sets.getValues();
		for (int i = 0; i < values.size(); i++) {
			values.get(i).check_change_ticks(change_tick);
		}
	}
}
