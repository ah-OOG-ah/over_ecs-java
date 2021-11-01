package com.overminddl1.over_ecs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

	@Test
	void spawn() {
		World world = new World();
		world.init_component(String.class);
		world.init_component(Boolean.class);
		world.init_component(Integer.class);
		Entity entity = world.spawn();
		entity.insert("String");
		assertEquals("String", entity.get(String.class));
		entity.get(Integer.class);
	}

}
