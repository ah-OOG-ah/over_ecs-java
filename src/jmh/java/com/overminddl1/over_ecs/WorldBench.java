package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.bundles.BundleN;
import com.overminddl1.over_ecs.query.QueryState;
import com.overminddl1.over_ecs.query.WorldQuery;
import com.overminddl1.over_ecs.test.ComponentsTestData.TestingI;
import com.overminddl1.over_ecs.test.ComponentsTestData.TestingS;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
@Threads(1)
@Fork(warmups = 0, value = 0)
@Warmup(iterations = 4, time = 10, timeUnit = TimeUnit.MICROSECONDS)
@Measurement(iterations = 7, time = 10, timeUnit = TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class WorldBench {
	@Param({"10000000"})
	public int entity_count;

	@Param({"10"})
	public int entity_divisor;

	@Param({"10000"})
	public int par_batch_size;
	private World world;
	private QueryState query_reset;
	private QueryState query_bench_ro;
	private QueryState query_bench_rw;
	private List<TestingI> anArray;
	private ExecutorService task_pool;

	@Setup(Level.Iteration)
	@SuppressWarnings("unchecked")
	public void setupIteration() throws Exception {
		if (world == null) {
			anArray = IntStream.range(0, entity_count / entity_divisor).mapToObj(TestingI::new).toList();
			task_pool = ForkJoinPool.commonPool();
			System.out.println("Task Pool Threads: " + ForkJoinPool.getCommonPoolParallelism());
			world = new World();
			world.init_component(TestingS.class);
			world.init_component(TestingI.class);
			query_reset = world.query(WorldQuery.builder().read_entities().write_component(TestingI.class));
			query_bench_ro = world.query(WorldQuery.builder().read_component(TestingI.class));
			query_bench_rw = world.query(WorldQuery.builder().write_component(TestingI.class));
			BundleN bundle_s = new BundleN(new TestingS("String"));
			BundleN bundle_si = new BundleN(new TestingS("String"), new TestingI(-1));
			for (int i = 0; i < entity_count; i++) {
				Entity entity = world.spawn();
				if (i % entity_divisor == 0) {
					entity.insert_bundle(bundle_si.set_unchecked(0, new TestingS("String:" + i)).set_unchecked(1, new TestingI(i)));
				} else {
					entity.insert_bundle(bundle_s.set_unchecked(0, new TestingS("String:" + i)));
				}
			}
		} else {
			// Else reinitialize
			for (int i = 0; i < anArray.size(); i++) {
				anArray.get(i).value = i;
			}
			query_reset.for_each((Object[] things) -> {
				Long entity = (Long) things[0];
				var i = (Mut<TestingI>) (((Object[]) things)[1]);
				i.set().value = Entity.id(entity);
			});
		}
	}

	@Benchmark
	public void list_for(Blackhole blackhole) throws Exception {
		for (int i = 0; i < anArray.size(); i++) {
			blackhole.consume(anArray.get(i).value);
		}
	}

	@Benchmark
	public void list_iter(Blackhole blackhole) throws Exception {
		for (TestingI i : anArray) {
			blackhole.consume(i.value);
		}
	}

	@Benchmark
	public void list_foreach(Blackhole blackhole) throws Exception {
		anArray.forEach(i -> {
			blackhole.consume(i.value);
		});
	}

	@Benchmark
	public void table_for_each_ro(Blackhole blackhole) throws Exception {
		query_bench_ro.for_each((Object[] things) -> {
			blackhole.consume(((TestingI) things[0]).value);
		});
	}

	@Benchmark
	@SuppressWarnings("unchecked")
	public void table_for_each_packed_ro(Blackhole blackhole) throws Exception {
		query_bench_ro.for_each_packed((Object[] things) -> {
			ArrayList<TestingI> packed = (ArrayList<TestingI>) things[0];
			for (Object thing : packed) {
				blackhole.consume(thing);
			}
		});
	}

	@Benchmark
	public void table_par_for_each_ro(Blackhole blackhole) throws Exception {
		query_bench_ro.par_for_each(task_pool, par_batch_size, (Object[] things) -> {
			blackhole.consume(((TestingI) things[0]).value);
		});
	}

	@Benchmark
	@SuppressWarnings("unchecked")
	public void table_par_for_each_packed_ro(Blackhole blackhole) throws Exception {
		query_bench_ro.par_for_each_packed(task_pool, par_batch_size, (Object[] things, int start, int end) -> {
			ArrayList<TestingI> packed = (ArrayList<TestingI>) things[0];
			for (int i = start; i < end; i++) {
				blackhole.consume(packed.get(i).value);
			}
		});
	}

	@Benchmark
	public void table_iter_ro(Blackhole blackhole) throws Exception {
		for (Object things : query_bench_ro) {
			blackhole.consume(((TestingI) (((Object[]) things)[0])).value);
		}
	}

	@Benchmark
	public void table_nocast_for_each_ro(Blackhole blackhole) throws Exception {
		query_bench_ro.for_each(blackhole::consume);
	}

	@Benchmark
	@SuppressWarnings("unchecked")
	public void table_nocast_for_each_packed_ro(Blackhole blackhole) throws Exception {
		query_bench_ro.for_each_packed((Object[] things) -> {
			ArrayList<Object> packed = (ArrayList<Object>) things[0];
			for (Object thing : packed) {
				blackhole.consume(thing);
			}
		});
	}

	@Benchmark
	public void table_nocast_par_for_each_ro(Blackhole blackhole) throws Exception {
		query_bench_ro.par_for_each(task_pool, par_batch_size, blackhole::consume);
	}

	@Benchmark
	public void table_nocast_iter_ro(Blackhole blackhole) throws Exception {
		for (Object things : query_bench_ro) {
			blackhole.consume(things);
		}
	}

	@Benchmark
	@SuppressWarnings("unchecked")
	public void table_for_each_rw_set(Blackhole blackhole) throws Exception {
		query_bench_rw.for_each((Object[] things) -> {
			blackhole.consume(((Mut<TestingI>) things[0]).set().value);
		});
	}

	@Benchmark
	@SuppressWarnings("unchecked")
	public void table_for_each_rw_get(Blackhole blackhole) throws Exception {
		query_bench_rw.for_each((Object[] things) -> {
			blackhole.consume(((Mut<TestingI>) things[0]).get().value);
		});
	}

	@Benchmark
	@SuppressWarnings("unchecked")
	public void table_par_for_each_rw_set(Blackhole blackhole) throws Exception {
		query_bench_rw.par_for_each(task_pool, par_batch_size, (Object[] things) -> {
			blackhole.consume(((Mut<TestingI>) things[0]).set().value);
		});
	}

	@Benchmark
	@SuppressWarnings("unchecked")
	public void table_par_for_each_rw_get(Blackhole blackhole) throws Exception {
		query_bench_rw.par_for_each(task_pool, par_batch_size, (Object[] things) -> {
			blackhole.consume(((Mut<TestingI>) things[0]).get().value);
		});
	}

	@Benchmark
	@SuppressWarnings("unchecked")
	public void table_iter_rw_set(Blackhole blackhole) throws Exception {
		for (Object things : query_bench_rw) {
			blackhole.consume(((Mut<TestingI>) (((Object[]) things)[0])).set().value);
		}
	}

	@Benchmark
	@SuppressWarnings("unchecked")
	public void table_iter_rw_get(Blackhole blackhole) throws Exception {
		for (Object things : query_bench_rw) {
			blackhole.consume(((Mut<TestingI>) (((Object[]) things)[0])).get().value);
		}
	}
}
