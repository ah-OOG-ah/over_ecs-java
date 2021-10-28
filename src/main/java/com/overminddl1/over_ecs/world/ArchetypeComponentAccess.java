package com.overminddl1.over_ecs.world;

import com.overminddl1.over_ecs.storages.SparseSet;

public class ArchetypeComponentAccess {
	private static final int UNIQUE_ACCESS = 0;
	private static final int BASE_ACCESS = 1;

	private SparseSet<Integer> access;

	public ArchetypeComponentAccess() {
		this.access = new SparseSet<Integer>();
	}

	public boolean read(int archetype_id) {
		return this.access.replace_with(archetype_id, (archetype_id_access) -> {
			if (archetype_id_access == null) return BASE_ACCESS;
			else return archetype_id_access + 1;
		}) != UNIQUE_ACCESS;
	}

	public void drop_read(int archetype_id) {
		this.access.replace_with(archetype_id, (archetype_id_access) -> (archetype_id_access - 1));
	}

	public boolean write(int archetype_id) {
		int archetype_id_access = this.access.get_or_insert(archetype_id, BASE_ACCESS);
		if (archetype_id_access == BASE_ACCESS) {
			this.access.insert(archetype_id, UNIQUE_ACCESS);
			return true;
		} else {
			return false;
		}
	}

	public void drop_write(int archetype_id) {
		this.access.insert(archetype_id, BASE_ACCESS);
	}
}
