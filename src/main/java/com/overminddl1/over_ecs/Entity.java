package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.components.ComponentInfo;
import com.overminddl1.over_ecs.entities.EntityLocation;
import com.overminddl1.over_ecs.storages.Column;
import com.overminddl1.over_ecs.storages.ComponentSparseSet;
import com.overminddl1.over_ecs.storages.Table;

public final class Entity {
	public static long init(int generation, int id) {
		return ((long) generation << 32) | id;
	}

	public static int id(long entity) {
		return (int) (entity & 0xFFFFFFFF);
	}

	public static int generation(long entity) {
		return (int) ((entity >> 32) & 0xFFFFFFFF);
	}

	private World world;
	private long entity;
	private EntityLocation location;

	public Entity(World world, long entity, EntityLocation location) {
		this.world = world;
		this.entity = entity;
		this.location = location;
	}

	public int id() {
		return Entity.id(this.entity);
	}

	public int generation() {
		return Entity.generation(this.entity);
	}

	public EntityLocation location() {
        return this.location;
    }

	public Archetype archetype() {
        return this.world.getArchetypes().get(this.location.archetype_id);
    }

	public boolean contains_component(Class component_class) {
		Integer component_id = world.getComponents().getId(component_class);
		if(component_id != null) {
			return this.contains_component_id(component_id);
		} else {
			return false;
		}
	}

	public boolean contains_component_id(int component_id) {
		return this.world.getArchetypes().get(this.location.archetype_id).contains(component_id);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> component_class) {
		Integer component_id = this.world.getComponents().getId(component_class);
		if(component_id != null) {
			return (T)this.get_component(component_id);
		} else {
			return null;
		}
	}

	public Object get_component(int component_id) {
		Archetype archetype = this.world.getArchetypes().get(this.location.archetype_id);
		ComponentInfo component_info = this.world.getComponents().getInfo(component_id);
		StorageType storage_type = component_info.getDescriptor().getStorageType();
		if(storage_type == StorageType.Table) {
			Table table = this.world.getStorages().tables.get(archetype.getTableId());
			Column column = table.get_column(component_id);
			if(column == null) {
				return null;
			}
			int table_row = archetype.getEntityTableRow(this.location.index);
			return column.data.get(table_row);
		} else if(storage_type == StorageType.SparseSet) {
			ComponentSparseSet sparse_set = this.world.getStorages().sparse_sets.get(component_id);
			if(sparse_set == null) {
				return null;
			}
			return sparse_set.get(entity);
		} else {
			throw new RuntimeException("Unsupported storage type: " + storage_type);
		}
	}
}
