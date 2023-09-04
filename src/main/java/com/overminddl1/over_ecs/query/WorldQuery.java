package com.overminddl1.over_ecs.query;

import com.overminddl1.over_ecs.World;

/**
 * You wanna ask the world a question? This is how you do it.
 * Won't answer the existential crisis you got from reading this code tho.
 */
public interface WorldQuery {
	static WorldQueryBuilder builder() {
		return new WorldQueryBuilder();
	}

	FetchState init_state(World world);

	Fetch init_fetch(World world, FetchState fetch_state, int last_change_tick, int change_tick);
}
