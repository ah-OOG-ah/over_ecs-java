package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.storages.SparseSets;
import com.overminddl1.over_ecs.storages.Tables;

public class Storages {
	public SparseSets sparse_sets;
	public Tables tables;

	public Storages() {
        sparse_sets = new SparseSets();
        tables = new Tables();
    }
}
