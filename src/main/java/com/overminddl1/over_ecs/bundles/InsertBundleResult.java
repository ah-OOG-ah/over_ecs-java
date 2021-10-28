package com.overminddl1.over_ecs.bundles;

import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.storages.Table;

public class InsertBundleResult {
	public Archetype new_archetype;
	public Table new_table;

	public InsertBundleResult(Archetype new_archetype, Table new_table) {
        this.new_archetype = new_archetype;
        this.new_table = new_table;
    }

	public InsertBundleResult(Archetype new_archetype) {
		this(new_archetype, null);
	}

	public InsertBundleResult() {
        this(null, null);
    }
}
