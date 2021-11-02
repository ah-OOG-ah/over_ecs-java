package com.overminddl1.over_ecs.query;

import com.overminddl1.over_ecs.Archetypes;
import com.overminddl1.over_ecs.World;
import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.storages.Table;
import com.overminddl1.over_ecs.storages.Tables;

import java.util.ArrayList;
import java.util.Iterator;

public class QueryIter implements Iterator {
	Tables tables;
	Archetypes archetypes;
	QueryState query_state;
	World world;
	int cur_table_id_index;
	ArrayList<Integer> table_ids;
	int cur_archetype_id_index;
	ArrayList<Integer> archetype_ids;
	Fetch fetch;
	FilterFetch filter;
	int current_len;
	int current_index;

	public QueryIter(World world, QueryState query_state, int last_change_tick, int change_tick) {
		this.fetch = query_state.fetch_factory.init(world, query_state.fetch_state, last_change_tick, change_tick);
		this.filter = query_state.filter_factory.init(world, query_state.filter_state, last_change_tick, change_tick);
		this.world = world;
		this.query_state = query_state;
		this.tables = world.getStorages().tables;
		this.archetypes = world.getArchetypes();
		this.cur_table_id_index = 0;
		this.table_ids = query_state.matched_table_ids;
		this.cur_archetype_id_index = 0;
		this.archetype_ids = query_state.matched_archetype_ids;
		this.current_len = 0;
		this.current_index = 0;
	}

	public boolean none_remaining() {
		if (this.fetch.is_dense() && this.filter.is_dense()) {
			while (true) {
				if (this.current_index == this.current_len) {
					if (this.cur_table_id_index >= this.table_ids.size()) {
						return true;
					}
					int table_id = this.table_ids.get(this.cur_table_id_index++);
					Table table = this.tables.get(table_id);
					this.filter.set_table(this.query_state.filter_state, table);
					this.current_len = table.size();
					this.current_index = 0;
					continue;
				}
				if (!this.filter.table_filter_fetch(this.current_index)) {
					this.current_index += 1;
					continue;
				}
				return false;
			}
		} else {
			while (true) {
				if (this.current_index == this.current_len) {
					if (this.cur_archetype_id_index >= this.archetype_ids.size()) {
						return true;
					}
					int archetype_id = this.archetype_ids.get(this.cur_archetype_id_index++);
					Archetype archetype = this.archetypes.get(archetype_id);
					this.filter.set_archetype(this.query_state.filter_state, archetype, this.tables);
					this.current_len = archetype.size();
					this.current_index = 0;
					continue;
				}
				if (!this.filter.archetype_filter_fetch(this.current_index)) {
					this.current_index += 1;
					continue;
				}
				return false;
			}
		}
	}

	@Override
	public boolean hasNext() {
		if (this.fetch.is_dense() && this.filter.is_dense()) {
			while (true) {
				if (this.current_index == this.current_len) {
					if (this.cur_table_id_index >= this.table_ids.size()) {
						return false;
					}
					int table_id = this.table_ids.get(this.cur_table_id_index++);
					Table table = this.tables.get(table_id);
					this.fetch.set_table(this.query_state.fetch_state, table);
					this.filter.set_table(this.query_state.filter_state, table);
					this.current_len = table.size();
					this.current_index = 0;
					continue;
				}
				if (!this.filter.table_filter_fetch(this.current_index)) {
					this.current_index += 1;
					continue;
				}
				this.current_index += 1;
				return true;
			}
		} else {
			while (true) {
				if (this.current_index == this.current_len) {
					if (this.cur_archetype_id_index >= this.archetype_ids.size()) {
						return false;
					}
					int archetype_id = this.archetype_ids.get(this.cur_archetype_id_index++);
					Archetype archetype = this.archetypes.get(archetype_id);
					this.fetch.set_archetype(this.query_state.fetch_state, archetype, this.tables);
					this.filter.set_archetype(this.query_state.filter_state, archetype, this.tables);
					this.current_len = archetype.size();
					this.current_index = 0;
					continue;
				}
				if (!this.filter.archetype_filter_fetch(this.current_index)) {
					this.current_index += 1;
					continue;
				}
				this.current_index += 1;
				return true;
			}
		}
	}

	@Override
	public Object next() {
		if (this.fetch.is_dense() && this.filter.is_dense()) {
			return this.fetch.table_fetch(this.current_index - 1);
		} else {
			return this.fetch.archetype_fetch(this.current_index);
		}
	}
}
