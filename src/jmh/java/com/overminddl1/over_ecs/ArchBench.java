package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.bundles.BundleN;
import com.overminddl1.over_ecs.query.QueryState;
import com.overminddl1.over_ecs.query.WorldQuery;
import com.overminddl1.over_ecs.bench.ArchComponentsTestData.Transform;
import com.overminddl1.over_ecs.bench.ArchComponentsTestData.Velocity;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Threads(1)
@Fork(warmups = 1, value = 1)
@Warmup(iterations = 5, time = 2000000, timeUnit = TimeUnit.MICROSECONDS)
@Measurement(iterations = 5, time = 2000000, timeUnit = TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ArchBench {
    @Param({"10000", "100000", "1000000"})
    public int amount;

    private ExecutorService task_pool;


    private World world;
    private QueryState query_TV;
    private QueryState query_ETV;
    private QueryState query_E;

    @Setup(Level.Trial)
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        task_pool = ForkJoinPool.commonPool();

        world = new World();
        world.init_component(Transform.class);
        world.init_component(Velocity.class);

        var data = new BundleN(new Transform(0, 0), new Velocity(1, 1));
        for (int index = 0; index < amount; index++) {
            Entity entity = world.spawn();
            entity.insert_bundle(data);
        }

        query_TV = world.query(WorldQuery.builder().read_component(Transform.class).read_component(Velocity.class));
        query_ETV = world.query(WorldQuery.builder().read_entities().read_component(Transform.class).read_component(Velocity.class));
        query_E = world.query(WorldQuery.builder().read_entities());
    }

    @Benchmark
    public void query(Blackhole blackhole) throws Exception {
        query_TV.for_each((Object[] things) -> {
            var t = (Transform) things[0];
            var v = (Velocity) things[1];
            t.x += v.x;
            t.y += v.y;
        });
    }

    @Benchmark
    @SuppressWarnings("unchecked")
    public void query_packed(Blackhole blackhole) throws Exception {
        query_TV.for_each_packed((Object[] things) -> {
            var ts = (ArrayList<Transform>) things[0];
            var vs = (ArrayList<Velocity>) things[1];
            var vi = vs.iterator();
            for(var t: ts) {
                vi.hasNext();
                var v = vi.next();
                t.x += v.x;
                t.y += v.y;
            }
        });
    }

    @Benchmark
    public void entityQuery(Blackhole blackhole) throws Exception {
        query_ETV.for_each((Object[] things) -> {
            var t = (Transform) things[1];
            var v = (Velocity) things[2];
            t.x += v.x;
            t.y += v.y;
        });
    }

    @Benchmark
    @SuppressWarnings("unchecked")
    public void entityQuery_packed(Blackhole blackhole) throws Exception {
        query_ETV.for_each_packed((Object[] things) -> {
            var ts = (ArrayList<Transform>) things[1];
            var vs = (ArrayList<Velocity>) things[2];
            var vi = vs.iterator();
            for(var t: ts) {
                vi.hasNext();
                var v = vi.next();
                t.x += v.x;
                t.y += v.y;
            }
        });
    }

    @Benchmark
    public void pureEntityQuery(Blackhole blackhole) throws Exception {
        query_E.for_each((Object[] things) -> {
            Entity e = world.get_entity((Long) things[0]);

            var t = e.get(Transform.class);
            var v = e.get(Velocity.class);

            t.x += v.x;
            t.y += v.y;
        });
    }

    @Benchmark
    public void pureEntityQuery_query(Blackhole blackhole) throws Exception {
        query_E.for_each((Object[] things) -> {
            var e = (Long) things[0];
            var data = (Object[]) query_TV.get(e);

            var t = (Transform) data[0];
            var v = (Velocity) data[1];

            t.x += v.x;
            t.y += v.y;
        });
    }
}
