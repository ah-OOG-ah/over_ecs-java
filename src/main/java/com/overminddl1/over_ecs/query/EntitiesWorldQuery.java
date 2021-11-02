package com.overminddl1.over_ecs.query;

import com.overminddl1.over_ecs.World;
import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.storages.Table;
import com.overminddl1.over_ecs.storages.Tables;

import java.util.ArrayList;

public class EntitiesWorldQuery implements WorldQuery {
	@Override
	public FetchState init_state(World world) {
		return new FetchState() {
			@Override
			public void update_component_access(FilteredAccess access) {
			}

			@Override
			public void update_archetype_component_access(Archetype archetype, Access access) {
			}

			@Override
			public boolean matches_archetype(Archetype archetype) {
				return true;
			}

			@Override
			public boolean matches_table(Table table) {
				return true;
			}
		};
	}

	@Override
	public Fetch init_fetch(World world, FetchState fetch_state, int last_change_tick, int change_tick) {
		return new Fetch() {
			ArrayList<Long> entities = null;

			@Override
			public boolean is_dense() {
				return true;
			}

			@Override
			public void set_archetype(FetchState fetch_state, Archetype archetype, Tables tables) {
				this.entities = archetype.getEntities();
			}

			@Override
			public void set_table(FetchState fetch_state, Table table) {
				this.entities = table.getEntities();
			}

			@Override
			public Object archetype_fetch(int archetype_index) {
				return this.entities.get(archetype_index);
			}

			@Override
			public Object table_fetch(int table_row) {
				return this.entities.get(table_row);
			}
		};
	}
}
