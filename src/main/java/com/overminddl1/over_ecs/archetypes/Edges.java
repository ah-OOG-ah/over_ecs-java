package com.overminddl1.over_ecs.archetypes;

import com.overminddl1.over_ecs.storages.SparseArray;

import java.util.ArrayList;

/**
 * Really not sure what this is
 */
public class Edges {

	// Genuinely no idea why these are here
	public SparseArray<AddBundle> add_bundle;
	public SparseArray<Integer> remove_bundle;
	public SparseArray<Integer> remove_bundle_intersection;

	public Edges() {
		add_bundle = new SparseArray<AddBundle>();
		remove_bundle = new SparseArray<Integer>();
		remove_bundle_intersection = new SparseArray<Integer>();
	}

	public AddBundle get_add_bundle(int bundle_id) {
		return this.add_bundle.get(bundle_id);
	}

	public void insert_add_bundle(int bundle_id, int archetype_id, ArrayList<Boolean> bundle_status) {
		this.add_bundle.insert(bundle_id, new AddBundle(archetype_id, bundle_status));
	}

	public Integer get_remove_bundle(int bundle_id) {
		return this.remove_bundle.get(bundle_id);
	}

	public void insert_remove_bundle(int bundle_id, Integer archetype_id) {
		this.remove_bundle.insert(bundle_id, archetype_id);
	}

	public Integer get_remove_bundle_intersection(int bundle_id) {
		return this.remove_bundle_intersection.get(bundle_id);
	}

	public void insert_remove_bundle_intersection(int bundle_id, Integer archetype_id) {
		this.remove_bundle_intersection.insert(bundle_id, archetype_id);
	}
}
