package com.overminddl1.over_ecs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

	@Test
	void spawn() {
		World world = new World();
		Entity entity = world.spawn();
		entity.insert("String");
		assertEquals("String", entity.get(String.class));
//		world.init_component(Integer.class);
//		entity.get(Integer.class);
	}
}
