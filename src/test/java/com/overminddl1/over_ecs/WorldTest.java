package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.bundles.BundleN;
import com.overminddl1.over_ecs.query.WorldFilterQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

	@Test
	void spawn_and_slow_access() {
		World world = new World();
		world.init_component(String.class);
		world.init_component(Boolean.class);
		world.init_component(Integer.class);
		Entity entity = world.spawn();
		entity.insert("String");
		assertEquals("String", entity.get(String.class));
		entity.get(Integer.class);
	}

	@Test
	void spawn_and_fast_access() {
		World world = new World();
		world.init_component(String.class);
		world.init_component(Boolean.class);
		world.init_component(Integer.class);
		BundleN bundle_s = new BundleN("String");
		BundleN bundle_sb = new BundleN("String", true);
		BundleN bundle_si = new BundleN("String", -1);
		BundleN bundle_sbi = new BundleN("String", true, -1);
		for (int i = 0; i < 1000000; i++) {
			Entity entity = world.spawn();
			if (i % 6 == 0) {
				entity.insert_bundle(bundle_sbi.set_unchecked(0, "String:" + i).set_unchecked(2, i));
			} else if (i % 3 == 0) {
				entity.insert_bundle(bundle_si.set_unchecked(0, "String:" + i).set_unchecked(1, i));
			} else if (i % 2 == 0) {
				entity.insert_bundle(bundle_sb.set_unchecked(0, "String:" + i));
			} else {
				entity.insert_bundle(bundle_s.set_unchecked(0, "String:" + i));
			}
		}
		world.query(WorldFilterQuery.NONE).for_each(world, (thing) -> {
			assertNull(thing);
			assertNotNull(thing);
		});
	}

}
