package com.overminddl1.over_ecs.query;

import com.overminddl1.over_ecs.World;
import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.storages.Table;
import com.overminddl1.over_ecs.storages.Tables;

public interface WorldFilterQuery extends WorldQuery {
	WorldFilterQuery NONE = new WorldFilterQuery() {
		private FilterFetch filter_fetch = new FilterFetch() {
			@Override
			public boolean is_dense() {
				return true;
			}

			@Override
			public void set_archetype(FetchState fetch_state, Archetype archetype, Tables tables) {
			}

			@Override
			public void set_table(FetchState fetch_state, Table table) {
			}

			@Override
			public Object archetype_fetch(int archetype_index) {
				return null;
			}

			@Override
			public Object table_fetch(int table_row) {
				return null;
			}

			@Override
			public boolean archetype_filter_fetch(int archetype_index) {
				return true;
			}

			@Override
			public boolean table_filter_fetch(int table_row) {
				return true;
			}
		};
		private FetchState filter_state = new FetchState() {
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

		@Override
		public FilterFetch init(World world, FetchState fetch_state, int last_change_tick, int change_tick) {
			return this.filter_fetch;
		}

		@Override
		public FetchState init_state(World world) {
			return this.filter_state;
		}
	};

	@Override
	FilterFetch init(World world, FetchState fetch_state, int last_change_tick, int change_tick);
}
