package com.overminddl1.over_ecs.bundles;

import com.overminddl1.over_ecs.Archetypes;
import com.overminddl1.over_ecs.Entities;
import com.overminddl1.over_ecs.archetypes.AddBundle;
import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.archetypes.ArchetypeSwapRemoveResult;
import com.overminddl1.over_ecs.entities.EntityLocation;
import com.overminddl1.over_ecs.storages.SparseSets;
import com.overminddl1.over_ecs.storages.Table;
import com.overminddl1.over_ecs.storages.TableMoveResult;

public class BundleInserter {
	public Archetype archetype;
	public Entities entities;
	private BundleInfo bundle_info;
	private Table table;
	private SparseSets sparse_sets;
	private InsertBundleResult result;
	private Archetypes archetypes;
	private int change_tick;

	public BundleInserter(Archetype archetype, Entities entities, BundleInfo bundle_info, Table table, SparseSets sparse_sets, InsertBundleResult result, Archetypes archetypes, int change_tick) {
		this.archetype = archetype;
		this.entities = entities;
		this.bundle_info = bundle_info;
		this.table = table;
		this.sparse_sets = sparse_sets;
		this.result = result;
		this.archetypes = archetypes;
		this.change_tick = change_tick;
	}

	public <T extends Bundle> EntityLocation insert(long entity, int archetype_index, T bundle) {
		EntityLocation location = new EntityLocation(archetype_index, this.archetype.getId());
		if (this.result.new_table == null && this.result.new_archetype == null) {
			AddBundle add_bundle = this.archetype.getEdges().get_add_bundle(this.bundle_info.getId());
			this.bundle_info.write_components(this.table, this.sparse_sets, add_bundle, entity, this.archetype.getEntityTableRow(archetype_index), this.change_tick, bundle);
			return location;
		} else if (this.result.new_table == null && this.result.new_archetype != null) {
			Archetype new_archetype = this.result.new_archetype;
			ArchetypeSwapRemoveResult result = this.archetype.swap_remove(location.index);
			if (result.swapped_entity != null) {
				this.entities.getMeta(result.swapped_entity).location = location;
			}
			EntityLocation new_location = new_archetype.allocate(entity, result.table_row);
			this.entities.getMeta(entity).location = new_location;
			AddBundle add_bundle = this.archetype.getEdges().get_add_bundle(this.bundle_info.getId());
			this.bundle_info.write_components(this.table, this.sparse_sets, add_bundle, entity, result.table_row, this.change_tick, bundle);
			return new_location;
		} else {
			Archetype new_archetype = this.result.new_archetype;
			Table new_table = this.result.new_table;
			ArchetypeSwapRemoveResult result = this.archetype.swap_remove(location.index);
			if (result.swapped_entity != null) {
				this.entities.getMeta(result.swapped_entity).location = location;
			}
			TableMoveResult move_result = new TableMoveResult();
			this.table.move_to_superset(result.table_row, new_table, move_result);
			EntityLocation new_location = new_archetype.allocate(entity, move_result.new_row);
			this.entities.getMeta(entity).location = new_location;
			if (move_result.swapped_entity != null) {
				EntityLocation swapped_location = this.entities.get(move_result.swapped_entity);
				Archetype swapped_archetype;
				if (this.archetype.getId() == swapped_location.archetype_id) {
					swapped_archetype = this.archetype;
				} else if (new_archetype.getId() == swapped_location.archetype_id) {
					swapped_archetype = new_archetype;
				} else {
					swapped_archetype = this.archetypes.get(swapped_location.archetype_id);
				}
				swapped_archetype.setEntityTableRow(swapped_location.index, result.table_row);
			}
			AddBundle add_bundle = this.archetype.getEdges().get_add_bundle(this.bundle_info.getId());
			this.bundle_info.write_components(new_table, this.sparse_sets, add_bundle, entity, move_result.new_row, this.change_tick, bundle);
			return new_location;
		}
	}
}
