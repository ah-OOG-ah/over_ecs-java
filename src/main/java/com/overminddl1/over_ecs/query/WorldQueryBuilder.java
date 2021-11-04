package com.overminddl1.over_ecs.query;

import com.overminddl1.over_ecs.Component;
import com.overminddl1.over_ecs.World;
import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.storages.Table;
import com.overminddl1.over_ecs.storages.Tables;

import java.util.ArrayList;

public class WorldQueryBuilder implements WorldQuery {
	private ArrayList<WorldQuery> world_queries;

	public WorldQueryBuilder() {
		this.world_queries = new ArrayList<>();
	}

	@Override
	public FetchState init_state(World world) {
		return new ImplFetchState(world);
	}

	@Override
	public Fetch init_fetch(World world, FetchState fetch_state, int last_change_tick, int change_tick) {
		return new ImplFetch(world, fetch_state, last_change_tick, change_tick);
	}

	public WorldQueryBuilder add_world_query(WorldQuery world_query) {
		this.world_queries.add(world_query);
		return this;
	}

	public WorldQueryBuilder read_entities() {
		return this.add_world_query(new EntitiesWorldQuery());
	}

	public WorldQueryBuilder read_component(Class<? extends Component> component_class) {
		return this.add_world_query(new ComponentReadWorldQuery(component_class));
	}

	public WorldQueryBuilder write_component(Class<? extends Component> component_class) {
		return this.add_world_query(new ComponentReadWriteWorldQuery(component_class));
	}

	private class ImplFetchState implements FetchState {
		FetchState[] states;

		public ImplFetchState(World world) {
			this.states = new FetchState[WorldQueryBuilder.this.world_queries.size()];
			for (int i = 0; i < WorldQueryBuilder.this.world_queries.size(); i++) {
				this.states[i] = WorldQueryBuilder.this.world_queries.get(i).init_state(world);
			}
		}

		@Override
		public void update_component_access(FilteredAccess access) {
			for (int i = 0; i < this.states.length; i++) {
				this.states[i].update_component_access(access);
			}
		}

		@Override
		public void update_archetype_component_access(Archetype archetype, Access access) {
			for (int i = 0; i < this.states.length; i++) {
				this.states[i].update_archetype_component_access(archetype, access);
			}
		}

		@Override
		public boolean matches_archetype(Archetype archetype) {
			for (int i = 0; i < this.states.length; i++) {
				if (!this.states[i].matches_archetype(archetype)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean matches_table(Table table) {
			for (int i = 0; i < this.states.length; i++) {
				if (!this.states[i].matches_table(table)) {
					return false;
				}
			}
			return true;
		}
	}

	private class ImplFetch implements Fetch {
		Fetch[] fetches;
		Object[] holder;

		public ImplFetch(World world, FetchState fetch_state, int last_change_tick, int change_tick) {
			ImplFetchState state = (ImplFetchState) fetch_state;
			this.fetches = new Fetch[WorldQueryBuilder.this.world_queries.size()];
			for (int i = 0; i < WorldQueryBuilder.this.world_queries.size(); i++) {
				this.fetches[i] = WorldQueryBuilder.this.world_queries.get(i).init_fetch(world, state.states[i], last_change_tick, change_tick);
			}
			this.holder = new Object[this.fetches.length];
		}

		@Override
		public boolean is_dense() {
			for (int i = 0; i < this.fetches.length; i++) {
				if (!this.fetches[i].is_dense()) {
					return false;
				}
			}
			return true;
		}

		@Override
		public void set_archetype(FetchState fetch_state, Archetype archetype, Tables tables) {
			ImplFetchState state = (ImplFetchState) fetch_state;
			for (int i = 0; i < this.fetches.length; i++) {
				this.fetches[i].set_archetype(state.states[i], archetype, tables);
			}
		}

		@Override
		public void set_table(FetchState fetch_state, Table table) {
			ImplFetchState state = (ImplFetchState) fetch_state;
			for (int i = 0; i < this.fetches.length; i++) {
				this.fetches[i].set_table(state.states[i], table);
			}
		}

		@Override
		public Object archetype_fetch(int archetype_index) {
			for (int i = 0; i < this.fetches.length; i++) {
				this.holder[i] = this.fetches[i].archetype_fetch(archetype_index);
			}
			return this.holder;
		}

		@Override
		public Object table_fetch(int table_row) {
			for (int i = 0; i < this.fetches.length; i++) {
				this.holder[i] = this.fetches[i].table_fetch(table_row);
			}
			return this.holder;
		}

		@Override
		public Object archetype_fetch_packed() {
			for (int i = 0; i < this.fetches.length; i++) {
				this.holder[i] = this.fetches[i].archetype_fetch_packed();
			}
			return this.holder;
		}

		@Override
		public Object table_fetch_packed() {
			for (int i = 0; i < this.fetches.length; i++) {
				this.holder[i] = this.fetches[i].table_fetch_packed();
			}
			return this.holder;
		}
	}
}
