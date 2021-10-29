package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.archetypes.ArchetypeSwapRemoveResult;
import com.overminddl1.over_ecs.bundles.*;
import com.overminddl1.over_ecs.components.ComponentInfo;
import com.overminddl1.over_ecs.entities.EntityLocation;
import com.overminddl1.over_ecs.storages.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

	@SuppressWarnings("unchecked")
	public <T> T set(Class<T> component_class) {
		Integer component_id = this.world.getComponents().getId(component_class);
		if(component_id != null) {
			return (T)this.set_component(component_id);
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

	public Object set_component(int component_id) {
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
			column.get_ticks(table_row).set_changed(this.world.getChangeTick());
			return column.data.get(table_row);
		} else if(storage_type == StorageType.SparseSet) {
			ComponentSparseSet sparse_set = this.world.getStorages().sparse_sets.get(component_id);
			if(sparse_set == null) {
				return null;
			}
			sparse_set.get_ticks(entity).set_changed(this.world.getChangeTick());
			return sparse_set.get(entity);
		} else {
			throw new RuntimeException("Unsupported storage type: " + storage_type);
		}
	}

	public <T extends Bundle> Entity insert_bundle(T bundle) {
		int change_tick = this.world.getChangeTick();
		BundleInfo bundle_info = this.world.getBundles().init_info(this.world.getComponents(), this.world.getStorages(), bundle.get_factory());
		BundleInserter bundle_inserter = bundle_info.get_bundle_inserter(this.world.getEntities(), this.world.getArchetypes(), this.world.getComponents(), this.world.getStorages(), this.location.archetype_id, change_tick);
		this.location = bundle_inserter.insert(this.entity, this.location.index, bundle);
		return this;
	}

	public <T extends BundleFactory> Bundle remove_bundle(T bundle_factory) {
		Components components = this.world.getComponents();
		Archetypes archetypes = this.world.getArchetypes();
		Storages storages = this.world.getStorages();
		SparseSet<ArrayList<Long>> removed_components = this.world.getRemovedComponents();
		long entity = this.entity;
		BundleInfo bundle_info = this.world.getBundles().init_info(components, storages, bundle_factory);
		EntityLocation old_location = this.location;
		Integer new_archetype_id = Entity.remove_bundle_from_archetype(archetypes, storages, components, old_location.archetype_id, bundle_info, false);
		if(new_archetype_id == null) {
			return null;
		}
		if(new_archetype_id == old_location.archetype_id) {
			return null;
		}
		Archetype old_archetype = archetypes.get(old_location.archetype_id);
		int[] bundle_components = bundle_info.getComponentIds();
		Entity self = this;
		Bundle result = bundle_factory.from_components(new Supplier<Object>() {
			int i = 0;
			@Override
			public Object get() {
				int component_id = bundle_components[i++];
				return Entity.take_component(components, storages, old_archetype, removed_components, component_id, entity, old_location);
			}
		});
		this.move_entity_from_remove(this, this.location, old_location.archetype_id, old_location, this.world.getEntities(), this.world.getArchetypes(), this.world.getStorages(), new_archetype_id, false);
		return result;
	}

	private static void move_entity_from_remove(Entity entity, EntityLocation location, int archetype_id, EntityLocation old_location, Entities entities, Archetypes archetypes, Storages storages, Integer new_archetype_id, boolean drop) {
		Archetype old_archetype = archetypes.get(archetype_id);
		ArchetypeSwapRemoveResult remove_result = old_archetype.swap_remove(old_location.index);
		if(remove_result.swapped_entity != null) {
			entities.getMeta(remove_result.swapped_entity).location = old_location;
		}
		int old_table_row = remove_result.table_row;
		int old_table_id = old_archetype.getTableId();
		Archetype new_archetype = archetypes.get(new_archetype_id);
		EntityLocation new_location;
		if(old_table_id == new_archetype.getTableId()) {
			new_location = new_archetype.allocate(entity.entity, old_table_row);
		} else {
			Table old_table = storages.tables.get(old_table_id);
			Table new_table = storages.tables.get(new_archetype.getTableId());
			TableMoveResult move_result = new TableMoveResult();
			if(drop) {
				old_table.move_to_and_drop_missing(old_table_row, new_table, move_result);
			} else {
				old_table.move_to_and_forget_missing(old_table_row, new_table, move_result);
			}
			new_location = new_archetype.allocate(entity.entity, move_result.new_row);
			if(move_result.swapped_entity != null) {
				EntityLocation swapped_location = entities.get(move_result.swapped_entity);
				;
				archetypes.get(swapped_location.archetype_id).setEntityTableRow(swapped_location.index, old_table_row);
			}
		}
		entity.location = new_location;
		entities.getMeta(entity.id()).location = new_location;
	}

	private static Object take_component(Components components, Storages storages, Archetype archetype, SparseSet<ArrayList<Long>> removed_components_set, int component_id, long entity, EntityLocation location) {
		ComponentInfo component_info = components.getInfo(component_id);
		ArrayList<Long> removed_components = removed_components_set.get_or_insert_with(component_id, ArrayList::new);
		removed_components.add(entity);
		StorageType storage_type = component_info.getStorageType();
		if(storage_type == StorageType.Table) {
			Table table = storages.tables.get(archetype.getTableId());
			Column column = table.get_column(component_id);
			int table_row = archetype.getEntityTableRow(location.index);
			return column.get_data(table_row);
		} else if(storage_type == StorageType.SparseSet) {
			return storages.sparse_sets.get(component_id).remove(entity);
		} else {
			throw new RuntimeException("Unsupported storage type: " + storage_type);
		}
	}

	private static Integer remove_bundle_from_archetype(Archetypes archetypes, Storages storages, Components components, int archetype_id, BundleInfo bundle_info, boolean intersection) {
		Archetype current_archetype = archetypes.get(archetype_id);
		Integer remove_bundle_result;
		if(intersection) {
			remove_bundle_result = current_archetype.getEdges().get_remove_bundle_intersection(bundle_info.getId());
		} else {
			remove_bundle_result = current_archetype.getEdges().get_remove_bundle(bundle_info.getId());
		}
		Integer result = remove_bundle_result;
		if(result == null) {
			ArrayList<Integer> removed_table_components = new ArrayList<Integer>();
			ArrayList<Integer> removed_sparse_set_components = new ArrayList<Integer>();
			int[] components_ids = bundle_info.getComponentIds();
			for (int i = 0; i < components_ids.length; i++) {
				int component_id = components_ids[i];
				if(current_archetype.contains(component_id)) {
					ComponentInfo component_info = components.getInfo(component_id);
					if(component_info.getStorageType() == StorageType.Table) {
						removed_table_components.add(component_id);
					} else if(component_info.getStorageType() == StorageType.SparseSet) {
						removed_sparse_set_components.add(component_id);
					} else {
						throw new RuntimeException("Unsupported storage type: " + component_info.getStorageType());
					}
				} else if(!intersection) {
					current_archetype.getEdges().insert_remove_bundle(bundle_info.getId(), null);
					return null;
				}
			}
			Collections.sort(removed_table_components);
			Collections.sort(removed_sparse_set_components);
			int[] next_table_components = current_archetype.getTableComponents();
			int[] next_sparse_set_components = current_archetype.getSparseSetComponents();
			Entity.sorted_remove(next_table_components, removed_table_components);
			Entity.sorted_remove(next_sparse_set_components, removed_sparse_set_components);
			int next_table_id = current_archetype.getTableId();
			if(removed_table_components.size() > 0) {
				next_table_id = storages.tables.get_id_or_insert(next_table_components, components);
			}
			int new_archetype_id = archetypes.get_id_or_insert(next_table_id, next_table_components, next_sparse_set_components);
			result = new_archetype_id;
		}
		if(intersection) {
			current_archetype.getEdges().insert_remove_bundle_intersection(bundle_info.getId(), result);
		} else {
			current_archetype.getEdges().insert_remove_bundle(bundle_info.getId(), result);
		}
		return result;
	}

	private static int[] sorted_remove(int[] list, List<Integer> removed) {
		return Arrays.stream(list).filter((i) -> {
			return Collections.binarySearch(removed, i) >= 0;
		}).toArray();
    }


}
