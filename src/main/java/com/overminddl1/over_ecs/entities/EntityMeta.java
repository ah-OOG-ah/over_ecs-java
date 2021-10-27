package com.overminddl1.over_ecs.entities;

import com.overminddl1.over_ecs.Entity;

public class EntityMeta {
	public static final EntityMeta EMPTY = new EntityMeta(0, EntityLocation.INVALID_ARCHETYPE_ID, EntityLocation.INVALID_INDEX);

	public int generation;
	public EntityLocation location;

	public EntityMeta(int generation, EntityLocation location) {
		this.generation = generation;
		this.location = location;
	}

	public EntityMeta(int generation, int archetype_id, int index) {
		this(generation, new EntityLocation(archetype_id, index));
	}

	public EntityMeta clone() {
		return new EntityMeta(generation, location.clone());
	}

	public long as_entity(int id) {
		return Entity.init(generation, id);
	}

	public EntityLocation clear_location() {
		EntityLocation old = location;
		location = EntityLocation.EMPTY.clone();
		return old;
	}
}
