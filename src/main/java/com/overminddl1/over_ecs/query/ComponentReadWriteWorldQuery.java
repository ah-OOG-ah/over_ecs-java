package com.overminddl1.over_ecs.query;

import com.overminddl1.over_ecs.Component;
import com.overminddl1.over_ecs.Mut;
import com.overminddl1.over_ecs.StorageType;
import com.overminddl1.over_ecs.World;
import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.components.ComponentInfo;
import com.overminddl1.over_ecs.components.ComponentTicks;
import com.overminddl1.over_ecs.storages.Column;
import com.overminddl1.over_ecs.storages.ComponentSparseSet;
import com.overminddl1.over_ecs.storages.Table;
import com.overminddl1.over_ecs.storages.Tables;

import java.util.ArrayList;

public class ComponentReadWriteWorldQuery implements WorldQuery {
	Class<? extends Component> component_class;

	public ComponentReadWriteWorldQuery(Class<? extends Component> component_class) {
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
			if (access.access().has_read(this.component_id)) {
				throw new RuntimeException(this.component_class.getName() + " conflicts with a previous access in this query.  Exclusive component access must be unique.");
			}
			access.add_write(this.component_id);
		}

		@Override
		public void update_archetype_component_access(Archetype archetype, Access access) {
			Integer archetype_component_id = archetype.getArchetypeComponentId(this.component_id);
			if (archetype_component_id != null) {
				access.add_write(archetype_component_id);
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
		ArrayList<ComponentTicks> table_ticks;
		ArrayList<Long> entities;
		ArrayList<Integer> entity_table_rows;
		ComponentSparseSet sparse_set;
		int last_change_tick;
		int change_tick;
		boolean is_dense;
		StorageType storage_type;
		private Mut<Component> mutable;

		public ImplFetch(World world, FetchState fetch_state, int last_change_tick, int change_tick, Class<? extends Component> component_class) {
			ComponentInfo info = world.getComponents().getInfoFromClass(component_class);
			if (info == null) {
				throw new RuntimeException("Component class " + component_class.getName() + " is not registered with the world.");
			}
			this.storage_type = info.getStorageType();
			if (this.storage_type == StorageType.Table) {
				this.is_dense = true;
				this.sparse_set = null;
			} else if (this.storage_type == StorageType.SparseSet) {
				this.is_dense = false;
				this.sparse_set = world.getStorages().sparse_sets.get(((ImplFetchState) fetch_state).component_id);
			} else {
				throw new RuntimeException("Invalid StorageType: " + this.storage_type.getClass().getName());
			}
			this.table_components = null;
			this.table_ticks = null;
			this.entities = null;
			this.entity_table_rows = null;
			this.last_change_tick = last_change_tick;
			this.change_tick = change_tick;
			this.mutable = new Mut<Component>(null, null, last_change_tick, change_tick);
		}

		@Override
		public boolean is_dense() {
			return this.is_dense;
		}

		@Override
		public void set_archetype(FetchState fetch_state, Archetype archetype, Tables tables) {
			if (this.storage_type == StorageType.Table) {
				this.entity_table_rows = archetype.getEntityTableRows();
				Column column = tables.get(archetype.getTableId()).get_column(((ImplFetchState) fetch_state).component_id);
				this.table_components = column.data;
				this.table_ticks = column.ticks;
			} else if (this.storage_type == StorageType.SparseSet) {
				this.entities = archetype.getEntities();
			} else {
				throw new RuntimeException("Invalid StorageType: " + this.storage_type.getClass().getName());
			}
		}

		@Override
		public void set_table(FetchState fetch_state, Table table) {
			Column column = table.get_column(((ImplFetchState) fetch_state).component_id);
			this.table_components = column.data;
			this.table_ticks = column.ticks;
		}

		@Override
		public Object archetype_fetch(int archetype_index) {
			if (this.storage_type == StorageType.Table) {
				int table_row = this.entity_table_rows.get(archetype_index);
				Component value = this.table_components.get(table_row);
				ComponentTicks ticks = this.table_ticks.get(table_row);
				return this.mutable.unsafe_update_internal(value, ticks);
				//return new Mut<Component>(value, ticks, this.last_change_tick, this.change_tick);
			} else if (this.storage_type == StorageType.SparseSet) {
				long entity = this.entities.get(archetype_index);
				Component value = this.sparse_set.get(entity);
				ComponentTicks ticks = this.sparse_set.get_ticks(entity);
				return this.mutable.unsafe_update_internal(value, ticks);
				//return new Mut<Component>(value, ticks, this.last_change_tick, this.change_tick);
			} else {
				throw new RuntimeException("Invalid StorageType: " + this.storage_type.getClass().getName());
			}
		}

		@Override
		public Object table_fetch(int table_row) {
			Component value = this.table_components.get(table_row);
			ComponentTicks ticks = this.table_ticks.get(table_row);
			return this.mutable.unsafe_update_internal(value, ticks);
			//return new Mut<Component>(value, ticks, this.last_change_tick, this.change_tick);
		}
	}
}
