package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.test.ComponentsTestData.TestingI;
import com.overminddl1.over_ecs.test.ComponentsTestData.TestingS;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentsTest {

	@Test
	void init_component() {
		Storages storages = new Storages();
		Components components = new Components();
		assertEquals(0, components.size());
		assertEquals(0, components.init_component(storages, TestingS.class));
		assertEquals(1, components.size());
		assertEquals(1, components.init_component(storages, TestingI.class));
		assertEquals(2, components.size());
	}
}
