package com.overminddl1.over_ecs.query;

import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.storages.Table;
import com.overminddl1.over_ecs.storages.Tables;

public interface Fetch {
	boolean is_dense();

	void set_archetype(FetchState fetch_state, Archetype archetype, Tables tables);

	void set_table(FetchState fetch_state, Table table);

	Object archetype_fetch(int archetype_index);

	Object table_fetch(int table_row);

	Object archetype_fetch_packed();

	Object table_fetch_packed();
}
