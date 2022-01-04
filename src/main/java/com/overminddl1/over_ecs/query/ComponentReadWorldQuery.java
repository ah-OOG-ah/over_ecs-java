package com.overminddl1.over_ecs.query;

import com.overminddl1.over_ecs.Component;
import com.overminddl1.over_ecs.StorageType;
import com.overminddl1.over_ecs.World;
import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.components.ComponentInfo;
import com.overminddl1.over_ecs.storages.Column;
import com.overminddl1.over_ecs.storages.ComponentSparseSet;
import com.overminddl1.over_ecs.storages.Table;
import com.overminddl1.over_ecs.storages.Tables;

import java.util.ArrayList;

public class ComponentReadWorldQuery implements WorldQuery {
	Class<? extends Component> component_class;

	public ComponentReadWorldQuery(Class<? extends Component> component_class) {
		this.component_class = component_class;
	}

	@Override
	public FetchState init_state(World world) {
		return new ImplFetchState(world, this.component_class);
	}

	@Override
	public Fetch init_fetch(World world, FetchState fetch_state, int last_change_tick, int change_tick) {
		return new ImplFetch(world, fetch_state, last_change_tick, change_tick, this.component_class);
	}

	private static class ImplFetchState implements FetchState {
		Class<? extends Component> component_class;
		int component_id;

		public ImplFetchState(World world, Class<? extends Component> component_class) {
			this.component_class = component_class;
			this.component_id = world.init_component(component_class);
		}

		@Override
		public void update_component_access(FilteredAccess access) {
			if (access.access().has_write(this.component_id)) {
				throw new RuntimeException(this.component_class.getName() + " conflicts with a previous access in this query.  Shared access cannot coincide with exclusive access.");
			}
			access.add_read(this.component_id);
		}

		@Override
		public void update_archetype_component_access(Archetype archetype, Access access) {
			Integer archetype_component_id = archetype.getArchetypeComponentId(this.component_id);
			if (archetype_component_id != null) {
				access.add_read(archetype_component_id);
			}
		}

		@Override
		public boolean matches_archetype(Archetype archetype) {
			return archetype.contains(this.component_id);
		}

		@Override
		public boolean matches_table(Table table) {
			return table.has_column(this.component_id);
		}
	}

	private static class ImplFetch implements Fetch {
		ArrayList<Component> table_components;
		ArrayList<Integer> entity_table_rows;
		ArrayList<Long> entities;
		ComponentSparseSet sparse_set;
		boolean is_dense;
		StorageType storage_type;

		public ImplFetch(World world, FetchState fetch_state, int last_change_tick, int change_tick, Class<? extends Component> component_class) {
			ComponentInfo info = world.getComponents().getInfoFromClass(component_class);
			if (info == null) {
				throw new RuntimeException("Component class " + component_class.getName() + " is not registered with the world.");
			}
			this.storage_type = info.getStorageType();
			switch(this.storage_type) {
				case Table:
					this.is_dense = true;
					this.sparse_set = null;
					break;
				case SparseSet:
					this.is_dense = false;
					this.sparse_set = world.getStorages().sparse_sets.get(((ImplFetchState) fetch_state).component_id);
					break;
			}
			this.table_components = null;
			this.entities = null;
			this.entity_table_rows = null;
		}

		@Override
		public boolean is_dense() {
			return this.is_dense;
		}

		@Override
		public void set_archetype(FetchState fetch_state, Archetype archetype, Tables tables) {
			switch(this.storage_type) {
				case Table:
					this.entity_table_rows = archetype.getEntityTableRows();
					Column column = tables.get(archetype.getTableId()).get_column(((ImplFetchState) fetch_state).component_id);
					this.table_components = column.data;
					break;
				case SparseSet:
					this.entities = archetype.getEntities();
					break;
			}
		}

		@Override
		public void set_table(FetchState fetch_state, Table table) {
			this.table_components = table.get_column(((ImplFetchState) fetch_state).component_id).data;
		}

		@Override
		public Component archetype_fetch(int archetype_index) {
			switch(this.storage_type) {
				case Table:
					int table_row = this.entity_table_rows.get(archetype_index);
					return this.table_components.get(table_row);
				case SparseSet:
					long entity = this.entities.get(archetype_index);
					return this.sparse_set.get(entity);
			}
			throw new RuntimeException("Unreachable");
		}

		@Override
		public Component table_fetch(int table_row) {
			return this.table_components.get(table_row);
		}

		@Override
		public Object archetype_fetch_packed() {
			switch(this.storage_type) {
				case Table:
					return this.table_components;
				case SparseSet:
					return this.sparse_set;
			}
			throw new RuntimeException("Unreachable");
		}

		@Override
		public Object table_fetch_packed() {
			return this.table_components;
		}
	}
}
