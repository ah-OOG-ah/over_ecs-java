package com.overminddl1.over_ecs.archetypes;

import java.util.ArrayList;

/**
 * Stores an ID and a list of ints called entity_rows.
 * I have absolutely no idea why there's so many tiny classes like this.
 */
class TableInfo {
	int id;
	ArrayList<Integer> entity_rows;

	public TableInfo(int id) {
		this.id = id;
		entity_rows = new ArrayList<Integer>();
	}
}
