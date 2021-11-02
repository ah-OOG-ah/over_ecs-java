package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.bundles.BundleN;
import com.overminddl1.over_ecs.query.QueryState;
import com.overminddl1.over_ecs.query.WorldFilterQuery;
import com.overminddl1.over_ecs.query.WorldQuery;
import com.overminddl1.over_ecs.test.ComponentsTestData.TestingI;
import com.overminddl1.over_ecs.test.ComponentsTestData.TestingS;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {


	@Test
	void spawn_and_slow_access() {
		World world = new World();
		world.init_component(TestingS.class);
		world.init_component(TestingI.class);
		Entity entity = world.spawn();
		entity.insert(new TestingS("String"));
		assertEquals("String", entity.get(TestingS.class).value);
		assertNull(entity.get(TestingI.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	void spawn_and_fast_access() {
		World world = new World();
		world.init_component(TestingS.class);
		world.init_component(TestingI.class);
		QueryState query_si = world.query(WorldQuery.builder().read_entities().read_component(TestingS.class).write_component(TestingI.class));
		QueryState query_bench = world.query(WorldQuery.builder().read_component(TestingI.class));
		BundleN bundle_s = new BundleN(new TestingS("String"));
		BundleN bundle_si = new BundleN(new TestingS("String"), new TestingI(-1));
		final int entity_count = 10000000;
		for (int i = 0; i < entity_count; i++) {
			Entity entity = world.spawn();
			if (i % 10 == 0) {
				entity.insert_bundle(bundle_si.set_unchecked(0, new TestingS("String:" + i)).set_unchecked(1, new TestingI(i)));
			} else {
				entity.insert_bundle(bundle_s.set_unchecked(0, new TestingS("String:" + i)));
			}
		}
		world.query(WorldFilterQuery.NONE).for_each(world, (thing) -> {
			assertNull(thing, "asked for nothing, should get nothing");
		});
		AtomicInteger count = new AtomicInteger(0);
		query_si.for_each(world, (Object[] things) -> {
			var e = (Long) things[0];
			var s = (TestingS) things[1];
			var i = (Mut<TestingI>) things[2];
			assertNotNull(e);
			assertTrue(s.value.startsWith("String:"));
			int iv = i.get().value;
			assertTrue(s.value.endsWith(Integer.toString(iv)));
			i.set().value = iv * 2;
			count.getAndIncrement();
		});
		assertEquals(entity_count / 10, count.get());
		int r0 = simple_query_benches(0, query_bench);
		int r1 = simple_query_benches(1, query_bench);
		int r2 = simple_query_benches(2, query_bench);
		int r3 = simple_query_benches(3, query_bench);
		int r4 = simple_query_benches(4, query_bench);
		int r5 = simple_query_benches(5, query_bench);
		assertEquals(r0, r1);
		assertEquals(r0, r2);
		assertEquals(r0, r3);
		assertEquals(r0, r4);
		assertEquals(r0, r5);
	}

	int simple_query_benches(int round, QueryState query) {
		System.out.println("Round " + round);
		TestingI ret = new TestingI(0);
		long start_time = System.nanoTime();
		query.for_each((Object[] things) -> {
			var i = (TestingI) things[0];
			ret.value += i.value;
		});
		long for_each_time = System.nanoTime() - start_time;
		start_time = System.nanoTime();
		for (Object things : query) {
			var i = (TestingI) (((Object[]) things)[0]);
			ret.value += i.value;
		}
		long for_iter_time = System.nanoTime() - start_time;
		System.out.println("ForEach time: " + (for_each_time / 1000) + "us\nForIter time: " + (for_iter_time / 1000) + "us");
		return ret.value;
	}

}
