package com.overminddl1.over_ecs.entities;

public class EntityLocation {
	public static final int INVALID_ARCHETYPE_ID = 0xFFFFFFFF;
	public static final int INVALID_INDEX = 0xFFFFFFFF;
	public static final EntityLocation EMPTY = new EntityLocation(INVALID_ARCHETYPE_ID, INVALID_INDEX);

	public int archetype_id;
	public int index;

	public EntityLocation(int archetype_id, int index) {
		this.archetype_id = archetype_id;
		this.index = index;
	}

	public EntityLocation clone() {
		return new EntityLocation(archetype_id, index);
	}
}
