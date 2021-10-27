package com.overminddl1.over_ecs.archetypes;

import java.util.ArrayList;

class TableInfo {
	int id;
	ArrayList<Integer> entity_rows;

	public TableInfo(int id) {
        this.id = id;
        entity_rows = new ArrayList<Integer>();
    }
}
