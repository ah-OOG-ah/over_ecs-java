package com.overminddl1.over_ecs.query;

public interface FilterFetch extends Fetch {
	boolean archetype_filter_fetch(int archetype_index);

	boolean table_filter_fetch(int table_row);
}
