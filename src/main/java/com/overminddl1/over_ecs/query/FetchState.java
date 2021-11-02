package com.overminddl1.over_ecs.query;

import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.storages.Table;

public interface FetchState {
	void update_component_access(FilteredAccess access);

	void update_archetype_component_access(Archetype archetype, Access access);

	boolean matches_archetype(Archetype archetype);

	boolean matches_table(Table table);
}
