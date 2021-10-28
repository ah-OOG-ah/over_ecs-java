package com.overminddl1.over_ecs.bundles;

import com.overminddl1.over_ecs.Entities;
import com.overminddl1.over_ecs.archetypes.AddBundle;
import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.entities.EntityLocation;
import com.overminddl1.over_ecs.storages.SparseSets;
import com.overminddl1.over_ecs.storages.Table;

public class BundleSpawner {
	public Archetype archetype;
	public Entities entities;
	private AddBundle add_bundle;
	private BundleInfo bundle_info;
	private Table table;
	private SparseSets sparse_sets;
	private int change_tick;

	public BundleSpawner(Archetype archetype, Entities entities, AddBundle add_bundle, BundleInfo bundle_info, Table table, SparseSets sparse_sets, int change_tick) {
		this.archetype = archetype;
		this.entities = entities;
		this.add_bundle = add_bundle;
		this.bundle_info = bundle_info;
		this.table = table;
		this.sparse_sets = sparse_sets;
		this.change_tick = change_tick;
	}

	public void reserve_storage(int additional) {
		this.archetype.reserve(additional);
		this.table.reserve(additional);
	}

	public <T extends Bundle> EntityLocation spawn_non_existent(long entity, T bundle) {
		int table_row = this.table.allocate(entity);
		EntityLocation location = this.archetype.allocate(entity, table_row);
		this.bundle_info.write_components(this.table, this.sparse_sets, this.add_bundle, entity, table_row, this.change_tick, bundle);
		this.entities.getMeta(entity).location = location;
		return location;
	}

	public <T extends Bundle> long spawn(T bundle) {
		long entity = this.entities.alloc();
		this.spawn_non_existent(entity, bundle);
		return entity;
	}
}
