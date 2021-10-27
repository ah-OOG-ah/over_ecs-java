package com.overminddl1.over_ecs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComponentsTest {

	@Test
	void init_component() {
		Storages storages = new Storages();
		Components components = new Components();
		assertEquals(0, components.size());
		assertEquals(0, components.init_component(storages, Integer.class));
		assertEquals(1, components.size());
		assertEquals(1, components.init_component(storages, String.class));
		assertEquals(2, components.size());
	}
}
