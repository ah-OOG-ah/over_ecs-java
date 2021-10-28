package com.overminddl1.over_ecs.archetypes;

public class ArchetypeSwapRemoveResult {
	public final Long swapped_entity;
	public final int table_row;

	public ArchetypeSwapRemoveResult(Long swapped_entity, int table_row) {
        this.swapped_entity = swapped_entity;
        this.table_row = table_row;
    }
}
