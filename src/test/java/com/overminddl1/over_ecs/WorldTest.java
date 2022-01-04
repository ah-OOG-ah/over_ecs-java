package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.bundles.BundleN;
import com.overminddl1.over_ecs.query.QueryState;
import com.overminddl1.over_ecs.query.WorldFilterQuery;
import com.overminddl1.over_ecs.query.WorldQuery;
import com.overminddl1.over_ecs.storages.ComponentSparseSet;
import com.overminddl1.over_ecs.test.ComponentsTestData.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {


	static final int entity_count = 1_000_000;
	//static final int entity_count = 30;
	static final int batch_size = 1000;
	static final List<TestingI> anArray = IntStream.range(0, entity_count / 10).mapToObj(TestingI::new).toList();

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
		QueryState query_bench_rw = world.query(WorldQuery.builder().write_component(TestingI.class));
		BundleN bundle_s = new BundleN(new TestingS("String"));
		BundleN bundle_si = new BundleN(new TestingS("String"), new TestingI(-1));
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
		ExecutorService task_pool = ForkJoinPool.commonPool();
		System.out.println("Task Pool Threads: " + ForkJoinPool.getCommonPoolParallelism());
		int r0 = simple_query_benches("RO-0", query_bench, task_pool, batch_size);
		for (int i = 1; i < 20; i++) {
			int r1 = simple_query_benches("RO-" + i, query_bench, task_pool, batch_size);
			assertEquals(r0, r1);
		}
		r0 = simple_query_benches_rw("RW-0", query_bench_rw, task_pool, batch_size);
		for (int i = 1; i < 20; i++) {
			int r1 = simple_query_benches_rw("RW-" + i, query_bench_rw, task_pool, batch_size);
			assertEquals(r0, r1);
		}
	}

	int simple_query_benches(String round, QueryState query, ExecutorService task_pool, int batch_size) {
		System.out.println("Round " + round);
		TestingI ret = new TestingI(0);
		long start_time = System.nanoTime();
		for (TestingI i : anArray) {
			ret.value += i.value;
		}
		long list_iter_time = System.nanoTime() - start_time;
		start_time = System.nanoTime();
		query.for_each((Object[] things) -> {
			var i = (TestingI) things[0];
			ret.value += i.value;
		});
		long for_each_time = System.nanoTime() - start_time;
		TestingI black_box = new TestingI(0);
		start_time = System.nanoTime();
		query.par_for_each(task_pool, batch_size, (Object[] things) -> {
			var i = (TestingI) things[0];
			black_box.value += i.value;
		});
		long par_for_each_time = System.nanoTime() - start_time;
		start_time = System.nanoTime();
		for (Object things : query) {
			var i = (TestingI) (((Object[]) things)[0]);
			ret.value += i.value;
		}
		long for_iter_time = System.nanoTime() - start_time;
		System.out.println("FastestArrayListIter time: " + (list_iter_time / 1000) + "us\nForEach time: " + (for_each_time / 1000) + "us\nParForEach time: " + (par_for_each_time / 1000) + "us\nForIter time: " + (for_iter_time / 1000) + "us");
		return ret.value;
	}

	@SuppressWarnings("unchecked")
	int simple_query_benches_rw(String round, QueryState query, ExecutorService task_pool, int batch_size) {
		System.out.println("Round " + round);
		TestingI ret = new TestingI(0);
		long start_time = System.nanoTime();
		query.for_each((Object[] things) -> {
			var i = (Mut<TestingI>) things[0];
			ret.value += i.get().value;
		});
		long for_each_time = System.nanoTime() - start_time;
		TestingI black_box = new TestingI(0);
		start_time = System.nanoTime();
		query.par_for_each(task_pool, batch_size, (Object[] things) -> {
			var i = (Mut<TestingI>) things[0];
			black_box.value += i.get().value;
		});
		long par_for_each_time = System.nanoTime() - start_time;
		start_time = System.nanoTime();
		for (Object things : query) {
			var i = (Mut<TestingI>) (((Object[]) things)[0]);
			ret.value += i.get().value;
		}
		long for_iter_time = System.nanoTime() - start_time;
		System.out.println("ForEach time: " + (for_each_time / 1000) + "us\nParForEach time: " + (par_for_each_time / 1000) + "us\nForIter time: " + (for_iter_time / 1000) + "us");
		return ret.value;
	}

	@Test
	@SuppressWarnings("unchecked")
	void sparse_access() {
		World world = new World();
		world.init_component(TestingS.class);
		world.init_component(TestingFSparse.class);
		QueryState query_si = world.query(WorldQuery.builder().read_entities().read_component(TestingS.class).write_component(TestingFSparse.class));
		QueryState query_f = world.query(WorldQuery.builder().read_entities().read_component(TestingFSparse.class));
		BundleN bundle_s = new BundleN(new TestingS("String"));
		BundleN bundle_si = new BundleN(new TestingS("String"), new TestingFSparse());
		for (int i = 0; i < entity_count; i++) {
			Entity entity = world.spawn();
			if (i % 10 == 0) {
				entity.insert_bundle(bundle_si.set_unchecked(0, new TestingS("String:" + (float) i)).set_unchecked(1, new TestingFSparse((float) i)));
			} else {
				entity.insert_bundle(bundle_s.set_unchecked(0, new TestingS("String:" + (float) i)));
			}
		}
		world.query(WorldFilterQuery.NONE).for_each(world, (thing) -> {
			assertNull(thing, "asked for nothing, should get nothing");
		});
		AtomicInteger count = new AtomicInteger(0);
		query_si.for_each(world, (Object[] things) -> {
			var e = (Long) things[0];
			var s = (TestingS) things[1];
			var i = (Mut<TestingFSparse>) things[2];
			assertNotNull(e);
			assertTrue(s.value.startsWith("String:"));
			float iv = i.get().value;
			assertTrue(s.value.endsWith(Float.toString(iv)));
			i.set().value = iv * 2;
			count.getAndIncrement();
		});
		assertEquals(entity_count / 10, count.get());
		int newCount = 0;
		for (Object thing: query_si) {
			Object[] things = (Object[]) thing;
			var e = (Long) things[0];
			var s = (TestingS) things[1];
			var i = (Mut<TestingFSparse>) things[2];
			assertNotNull(e);
			assertTrue(s.value.startsWith("String:"));
			float iv = i.get().value * 0.5F;
			assertEquals(s.value, "String:" + iv);
			//i.set().value = iv * 2;
			newCount++;
		}
		assertEquals(entity_count / 10, newCount);
		query_f.for_each_packed(world, (Object[] things) -> {
			var es = (ArrayList<Long>) things[0];
			var fs = (ComponentSparseSet) things[1];
			for (Long e : es) {
				if(Entity.id(e) % 30 == 0) {
					fs.remove(e);
				}
			}
		});
		newCount = 0;
		for (Object thing: query_si) {
			Object[] things = (Object[]) thing;
			var e = (Long) things[0];
			var s = (TestingS) things[1];
			var i = (Mut<TestingFSparse>) things[2];
			assertNotNull(e);
			assertTrue(s.value.startsWith("String:"));
			float iv = i.get().value * 0.5F;
			assertEquals(s.value, "String:" + iv);
			//i.set().value = iv * 2;
			newCount++;
		}
		assertEquals(entity_count / 30, newCount);
	}
}
