package com.overminddl1.over_ecs.archetypes;

import java.util.ArrayList;

/**
 * the heck.
 * It holds an archetype ID and a list of status bools. That's literally it.
 */
public class AddBundle {
	public int archetype_id;
	public ArrayList<Boolean> bundle_status; // false if added, true if mutated

	public AddBundle(int archetype_id, ArrayList<Boolean> bundle_status) {
		this.archetype_id = archetype_id;
		this.bundle_status = bundle_status;
	}
}
