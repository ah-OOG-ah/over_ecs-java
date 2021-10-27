package com.overminddl1.over_ecs.archetypes;

import com.overminddl1.over_ecs.StorageType;

public class ArchetypeComponentInfo {
	StorageType storage_type;
	int archetype_component_id;

	ArchetypeComponentInfo(StorageType storage_type, int archetype_component_id) {
        this.storage_type = storage_type;
        this.archetype_component_id = archetype_component_id;
    }
}
