package com.overminddl1.over_ecs.query;

import com.overminddl1.over_ecs.World;

public interface WorldQuery {
	static WorldQueryBuilder builder() {
		return new WorldQueryBuilder();
	}

	FetchState init_state(World world);

	Fetch init_fetch(World world, FetchState fetch_state, int last_change_tick, int change_tick);
}
