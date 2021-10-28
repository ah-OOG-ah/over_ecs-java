package com.overminddl1.over_ecs.archetypes;

import com.overminddl1.over_ecs.StorageType;
import com.overminddl1.over_ecs.entities.EntityLocation;
import com.overminddl1.over_ecs.storages.Column;
import com.overminddl1.over_ecs.storages.SparseSet;
import com.overminddl1.over_ecs.storages.StorageUtils;

import java.util.ArrayList;

public class Archetype {
	private int id;
	private ArrayList<Long> entities;
	private Edges edges;
	private TableInfo table_info;
	private int[] table_components;
	private int[] sparse_set_components;
	private SparseSet<Column> unique_components;
	private SparseSet<ArchetypeComponentInfo> components;

	public Archetype(int id, int table_id, int[] table_components, int[] sparse_set_components, int[] table_archetype_components, int[] sparse_set_archetype_components) {
		assert (table_components.length == table_archetype_components.length);
		assert (sparse_set_components.length == sparse_set_archetype_components.length);
		this.id = id;
		this.entities = new ArrayList<Long>();
		this.edges = new Edges();
		this.table_info = new TableInfo(table_id);
		this.table_components = table_components;
		this.sparse_set_components = sparse_set_components;
		this.unique_components = new SparseSet<Column>();
		this.components = new SparseSet<ArchetypeComponentInfo>(table_components.length + sparse_set_components.length);

		for (int i = 0; i < table_components.length; i++) {
			int component_id = table_components[i];
			int archetype_component_id = table_archetype_components[i];
			components.insert(component_id, new ArchetypeComponentInfo(StorageType.Table, archetype_component_id));
		}

		for (int i = 0; i < sparse_set_components.length; i++) {
			int component_id = sparse_set_components[i];
			int archetype_component_id = sparse_set_archetype_components[i];
			components.insert(component_id, new ArchetypeComponentInfo(StorageType.SparseSet, archetype_component_id));
		}
	}

	public int getId() {
		return this.id;
	}

	public int getTableId() {
		return this.table_info.id;
	}

	public ArrayList<Long> getEntities() {
		return this.entities;
	}

	public ArrayList<Integer> getEntityTableRows() {
		return this.table_info.entity_rows;
	}

	public int[] getTableComponents() {
		return this.table_components;
	}

	public int[] getSparseSetComponents() {
		return this.sparse_set_components;
	}

	public SparseSet<Column> getUniqueComponents() {
		return this.unique_components;
	}

	public SparseSet<ArchetypeComponentInfo> getComponents() {
		return this.components;
	}

	public Edges getEdges() {
		return this.edges;
	}

	public int getEntityTableRow(int index) {
		return this.table_info.entity_rows.get(index);
	}

	public void setEntityTableRow(int index, int table_row) {
		this.table_info.entity_rows.set(index, table_row);
	}

	public EntityLocation allocate(long entity, int table_row) {
		this.entities.add(entity);
		this.table_info.entity_rows.add(table_row);
		return new EntityLocation(this.id, this.entities.size() - 1);
	}

	public void reserve(int additional) {
		this.entities.ensureCapacity(this.entities.size() + additional);
		this.table_info.entity_rows.ensureCapacity(this.table_info.entity_rows.size() + additional);
	}

	public ArchetypeSwapRemoveResult swap_remove(int index) {
		boolean is_last = index == this.entities.size() - 1;
		StorageUtils.swap_remove(this.entities, index);
		return new ArchetypeSwapRemoveResult(is_last ? null : this.entities.get(index), StorageUtils.swap_remove(this.table_info.entity_rows, index));
	}

	public int size() {
		return this.entities.size();
	}

	public boolean contains(int component_id) {
		return this.components.contains(component_id);
	}

	public StorageType getStorageType(int component_id) {
		ArchetypeComponentInfo info = this.components.get(component_id);
		if (info == null) {
			return null;
		}
		return info.storage_type;
	}

	public int getArchetypeComponentId(int component_id) {
		ArchetypeComponentInfo info = this.components.get(component_id);
		if (info == null) {
			return -1;
		}
		return info.archetype_component_id;
	}

	public void clear_entities() {
		this.entities.clear();
		this.table_info.entity_rows.clear();
	}
}
