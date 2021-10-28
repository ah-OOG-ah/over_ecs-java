package com.overminddl1.over_ecs.bundles;

import com.overminddl1.over_ecs.*;
import com.overminddl1.over_ecs.archetypes.AddBundle;
import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.archetypes.Edges;
import com.overminddl1.over_ecs.components.ComponentInfo;
import com.overminddl1.over_ecs.components.ComponentTicks;
import com.overminddl1.over_ecs.storages.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

public class BundleInfo {
	private int id;
	private int[] component_ids;
	private ArrayList<StorageType> storage_types;

	public BundleInfo(int id, int[] component_ids, ArrayList<StorageType> storage_types) {
		this.id = id;
		this.component_ids = component_ids;
		this.storage_types = storage_types;
	}

	public int getId() {
		return id;
	}


	public int[] getComponentIds() {
		return component_ids;
	}

	public ArrayList<StorageType> getStorageTypes() {
		return storage_types;
	}

	public BundleInserter get_bundle_inserter(Entities entities, Archetypes archetypes, Components components, Storages storages, int archetype_id, int change_tick) {
		int new_archetype_id = this.add_bundle_to_archetype(archetypes, storages, components, archetype_id);
		if(new_archetype_id == archetype_id) {
			Archetype archetype = archetypes.get(archetype_id);
			int table_id = archetype.getTableId();
			return new BundleInserter(archetype, entities, this, storages.tables.get(table_id), storages.sparse_sets, new InsertBundleResult(), archetypes, change_tick);
		} else {
			Archetype archetype = archetypes.get(new_archetype_id);
			Archetype new_archetype = archetypes.get(new_archetype_id);
			int table_id = archetype.getTableId();
			int new_table_id = new_archetype.getTableId();
			if(table_id == new_table_id) {
				return new BundleInserter(archetype, entities, this, storages.tables.get(table_id), storages.sparse_sets, new InsertBundleResult(new_archetype), archetypes, change_tick);
			} else {
				Table table = storages.tables.get(table_id);
				Table new_table = storages.tables.get(new_table_id);
				return new BundleInserter(archetype, entities, this, table, storages.sparse_sets, new InsertBundleResult(new_archetype, new_table), archetypes, change_tick);
			}
		}
	}

	public BundleSpawner get_bundle_spawner(Entities entities, Archetypes archetypes, Components components, Storages storages, int change_tick) {
		int new_archetype_id = this.add_bundle_to_archetype(archetypes, storages, components, Archetypes.EMPTY_ID);
		Archetype empty_archetype = archetypes.get(Archetypes.EMPTY_ID);
		Archetype archetype = archetypes.get(new_archetype_id);
		Table table = storages.tables.get(archetype.getTableId());
		AddBundle add_bundle = empty_archetype.getEdges().get_add_bundle(this.getId());
		return new BundleSpawner(archetype, entities, add_bundle, this, table, storages.sparse_sets, change_tick);
	}

	public <T extends Bundle> void write_components(Table table, SparseSets sparse_sets, AddBundle add_bundle, long entity, int table_row, int change_tick, T bundle) {
		BundleInfo self = this;
		bundle.get_components(new Consumer<Object>() {
			int bundle_component = 0;
			@Override
			public void accept(Object component) {
				int component_id = self.component_ids[bundle_component];
				StorageType storage_type = self.storage_types.get(bundle_component);
				if(storage_type == StorageType.Table) {
					Column column = table.get_column(component_id);
					if(!add_bundle.bundle_status.get(bundle_component)) {
						column.initialize(table_row, component, new ComponentTicks(change_tick));
					} else {
						column.replace(table_row, component, change_tick);
					}
				} else if(storage_type == StorageType.SparseSet) {
					ComponentSparseSet sparse_set = sparse_sets.get(component_id);
					sparse_set.insert(entity, component, change_tick);
				} else {
					throw new RuntimeException("Unknown storage type: " + storage_type);
				}
				bundle_component += 1;
			}
		});
	}

	public int add_bundle_to_archetype(Archetypes archetypes, Storages storages, Components components, int archetype_id) {
		AddBundle add_bundle = archetypes.get(archetype_id).getEdges().get_add_bundle(this.getId());
		if(add_bundle != null) {
			return add_bundle.archetype_id;
		}
		ArrayList<Integer> new_table_components = new ArrayList<Integer>();
		ArrayList<Integer> new_sparse_set_components = new ArrayList<Integer>();
		ArrayList<Boolean> bundle_status = new ArrayList<Boolean>();
		Archetype current_archetype = archetypes.get(archetype_id);
		for (int i = 0; i < this.component_ids.length; i++) {
			int component_id = this.component_ids[i];
			if(current_archetype.contains(component_id)) {
				bundle_status.add(true);
			} else {
				bundle_status.add(false);
				ComponentInfo component_info = components.getInfo(component_id);
				StorageType storage_type = component_info.getDescriptor().getStorageType();
				if(storage_type == StorageType.Table) {
					new_table_components.add(component_id);
				} else if(storage_type == StorageType.SparseSet) {
					new_sparse_set_components.add(component_id);
				} else {
					throw new RuntimeException("Unknown storage type: " + storage_type);
				}
			}
		}
		if(new_table_components.size() == 0 && new_sparse_set_components.size() == 0) {
			Edges edges = current_archetype.getEdges();
			edges.insert_add_bundle(this.id, archetype_id, bundle_status);
			return archetype_id;
		} else {
			int table_id;
			int[] table_components;
			int[] sparse_set_components;
			if (new_table_components.size() == 0) {
				table_id = current_archetype.getTableId();
				table_components = current_archetype.getTableComponents();
			} else {
				int[] tc = current_archetype.getTableComponents();
				new_table_components.ensureCapacity(new_table_components.size() + tc.length);
				for (int i = 0; i < tc.length; i++) {
					new_table_components.add(tc[i]);
				}
				Collections.sort(new_table_components);
				table_components = new_table_components.stream().mapToInt(Integer::intValue).toArray();
				table_id = storages.tables.get_id_or_insert(table_components, components);
			}
			if (new_sparse_set_components.size() == 0) {
				sparse_set_components = current_archetype.getSparseSetComponents();
			} else {
				int[] sc = current_archetype.getSparseSetComponents();
				new_sparse_set_components.ensureCapacity(new_sparse_set_components.size() + sc.length);
				for (int i = 0; i < sc.length; i++) {
					new_sparse_set_components.add(sc[i]);
				}
				Collections.sort(new_sparse_set_components);
				sparse_set_components = new_sparse_set_components.stream().mapToInt(Integer::intValue).toArray();
			}
			int new_archetype_id = archetypes.get_id_or_insert(table_id, table_components, sparse_set_components);
			archetypes.get(archetype_id).getEdges().insert_add_bundle(this.id, new_archetype_id, bundle_status);
			return new_archetype_id;
		}
	}
}
