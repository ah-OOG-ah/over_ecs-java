package com.overminddl1.over_ecs.query;

import com.overminddl1.over_ecs.Archetypes;
import com.overminddl1.over_ecs.World;
import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.entities.EntityLocation;
import com.overminddl1.over_ecs.storages.Table;
import com.overminddl1.over_ecs.storages.Tables;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class QueryState implements Iterable<Object> {
	ArrayList<Integer> matched_table_ids;
	ArrayList<Integer> matched_archetype_ids;
	FetchState fetch_state;
	FetchState filter_state;
	WorldQuery fetch_factory;
	WorldFilterQuery filter_factory;
	private int world_id;
	private int archetype_generation;
	private BitSet matched_tables;
	private BitSet matched_archetypes;
	private Access archetype_component_access;
	private FilteredAccess component_access;
	private World world;

	public QueryState(World world, WorldQuery fetch_factory, WorldFilterQuery filter_factory) {
		this.world = world;
		this.world_id = world.getId();
		this.archetype_generation = 0;
		this.matched_tables = new BitSet();
		this.matched_archetypes = new BitSet();
		this.archetype_component_access = new Access();
		this.component_access = new FilteredAccess();
		this.matched_table_ids = new ArrayList<Integer>();
		this.matched_archetype_ids = new ArrayList<Integer>();
		this.fetch_state = fetch_factory.init_state(world);
		this.filter_state = filter_factory.init_state(world);
		this.fetch_factory = fetch_factory;
		this.filter_factory = filter_factory;

		this.fetch_state.update_component_access(this.component_access);
		FilteredAccess filter_component_access = new FilteredAccess();
		this.filter_state.update_component_access(filter_component_access);
		this.component_access.extend(filter_component_access);

		this.validate_world_and_update_archetypes(world);
	}

	public boolean is_empty(World world, int last_change_tick, int change_tick) {
		return this.iterate_manual(world, last_change_tick, change_tick).none_remaining();
	}

	public void validate_world_and_update_archetypes(World world) {
		if (world.getId() != this.world_id) {
			throw new RuntimeException("Attempted to use a QueryState with a mismatched World. QueryStates can only be used with the World they were created from.");
		}
		Archetypes archetypes = world.getArchetypes();
		int new_generation = archetypes.generation();
		int old_generation = this.archetype_generation;
		this.archetype_generation = new_generation;
		for (int i = old_generation; i < new_generation; i++) {
			this.new_archetype(archetypes.get(i));
		}
	}

	public void new_archetype(Archetype archetype) {
		if (this.fetch_state.matches_archetype(archetype) && this.filter_state.matches_archetype(archetype)) {
			this.fetch_state.update_archetype_component_access(archetype, this.archetype_component_access);
			this.filter_state.update_archetype_component_access(archetype, this.archetype_component_access);
			int archetype_id = archetype.getId();
			if (!this.matched_archetypes.get(archetype_id)) {
				this.matched_archetypes.set(archetype_id, true);
				this.matched_archetype_ids.add(archetype_id);
			}
			int table_id = archetype.getTableId();
			if (!this.matched_tables.get(table_id)) {
				this.matched_tables.set(table_id, true);
				this.matched_table_ids.add(table_id);
			}
		}
	}

	public Object get(long entity) {
		return this.get(this.world, entity);
	}

	public Object get(World world, long entity) {
		this.validate_world_and_update_archetypes(world);
		return this.get_manual(world, entity, world.getLastChangeTick(), world.getChangeTick());
	}

	public Object get_manual(long entity, int last_change_tick, int change_tick) {
		return this.get_manual(this.world, entity, last_change_tick, change_tick);
	}

	public Object get_manual(World world, long entity, int last_change_tick, int change_tick) {
		EntityLocation location = world.getEntities().get(entity);
		if (location == null) {
			return null;
		}
		if (!this.matched_archetypes.get(location.archetype_id)) {
			return null;
		}
		Archetype archetype = world.getArchetypes().get(location.archetype_id);
		Fetch fetch = fetch_factory.init_fetch(world, this.fetch_state, last_change_tick, change_tick);
		FilterFetch filter = filter_factory.init_fetch(world, this.filter_state, last_change_tick, change_tick);
		fetch.set_archetype(this.fetch_state, archetype, world.getStorages().tables);
		filter.set_archetype(this.filter_state, archetype, world.getStorages().tables);
		if (filter.archetype_filter_fetch(location.index)) {
			return fetch.archetype_fetch(location.index);
		} else {
			return null;
		}
	}

	@Override
	public QueryIter iterator() {
		return this.iterate(this.world);
	}

	public void forEach(Consumer<? super Object> action) {
		this.for_each(this.world, action);
	}

	public QueryIter iterate(World world) {
		this.validate_world_and_update_archetypes(world);
		return this.iterate_manual(world, world.getLastChangeTick(), world.getChangeTick());
	}

	public QueryCombinationIter iterate_combinations() {
		return this.iterate_combinations(this.world);
	}

	public QueryCombinationIter iterate_combinations(World world) {
		this.validate_world_and_update_archetypes(world);
		return this.iterate_combinations_manual(world, world.getLastChangeTick(), world.getChangeTick());
	}

	public QueryIter iterate_manual(int last_change_tick, int change_tick) {
		return this.iterate_manual(this.world, last_change_tick, change_tick);
	}

	public QueryIter iterate_manual(World world, int last_change_tick, int change_tick) {
		return new QueryIter(world, this, last_change_tick, change_tick);
	}

	public QueryCombinationIter iterate_combinations_manual(int last_change_tick, int change_tick) {
		return this.iterate_combinations_manual(this.world, last_change_tick, change_tick);
	}

	public QueryCombinationIter iterate_combinations_manual(World world, int last_change_tick, int change_tick) {
		return new QueryCombinationIter(world, this, last_change_tick, change_tick);
	}

	public <T> void for_each(Consumer<T> func) {
		this.for_each(this.world, func);
	}

	public <T> void for_each(World world, Consumer<T> func) {
		this.validate_world_and_update_archetypes(world);
		this.for_each_manual(world, func, world.getLastChangeTick(), world.getChangeTick());
	}

	public <T> void for_each_manual(Consumer<T> func, int last_change_tick, int change_tick) {
		this.for_each_manual(this.world, func, last_change_tick, change_tick);
	}

	@SuppressWarnings("unchecked")
	public <T> void for_each_manual(World world, Consumer<T> func, int last_change_tick, int change_tick) {
		Fetch fetch = fetch_factory.init_fetch(world, this.fetch_state, last_change_tick, change_tick);
		FilterFetch filter = filter_factory.init_fetch(world, this.filter_state, last_change_tick, change_tick);
		if (fetch.is_dense() && filter.is_dense()) {
			Tables tables = world.getStorages().tables;
			for (int table_idx = 0; table_idx < this.matched_table_ids.size(); table_idx++) {
				Table table = tables.get(this.matched_table_ids.get(table_idx));
				fetch.set_table(this.fetch_state, table);
				filter.set_table(this.filter_state, table);
				for (int table_index = 0; table_index < table.size(); table_index++) {
					if (!filter.table_filter_fetch(table_index)) {
						continue;
					}
					func.accept((T) fetch.table_fetch(table_index));
				}
			}
		} else {
			Archetypes archetypes = world.getArchetypes();
			Tables tables = world.getStorages().tables;
			for (int i = 0; i < this.matched_archetype_ids.size(); i++) {
				Archetype archetype = archetypes.get(this.matched_archetype_ids.get(i));
				fetch.set_archetype(this.fetch_state, archetype, tables);
				filter.set_archetype(this.filter_state, archetype, tables);
				for (int archetype_index = 0; archetype_index < archetype.size(); archetype_index++) {
					if (!filter.archetype_filter_fetch(archetype_index)) {
						continue;
					}
					func.accept((T) fetch.archetype_fetch(archetype_index));
				}
			}
		}
	}

	public <T> void for_each_packed(Consumer<T> func) {
		this.for_each_packed(this.world, func);
	}

	public <T> void for_each_packed(World world, Consumer<T> func) {
		this.validate_world_and_update_archetypes(world);
		this.for_each_packed_manual(world, func, world.getLastChangeTick(), world.getChangeTick());
	}

	public <T> void for_each_packed_manual(Consumer<T> func, int last_change_tick, int change_tick) {
		this.for_each_packed_manual(this.world, func, last_change_tick, change_tick);
	}

	@SuppressWarnings("unchecked")
	public <T> void for_each_packed_manual(World world, Consumer<T> func, int last_change_tick, int change_tick) {
		if (this.filter_factory != WorldFilterQuery.NONE) {
			throw new UnsupportedOperationException("Filter queries are not supported on dense iterations");
		}
		Fetch fetch = fetch_factory.init_fetch(world, this.fetch_state, last_change_tick, change_tick);
		if (fetch.is_dense()) {
			Tables tables = world.getStorages().tables;
			for (int table_idx = 0; table_idx < this.matched_table_ids.size(); table_idx++) {
				Table table = tables.get(this.matched_table_ids.get(table_idx));
				fetch.set_table(this.fetch_state, table);
				func.accept((T) fetch.table_fetch_packed());
			}
		} else {
			Archetypes archetypes = world.getArchetypes();
			Tables tables = world.getStorages().tables;
			for (int i = 0; i < this.matched_archetype_ids.size(); i++) {
				Archetype archetype = archetypes.get(this.matched_archetype_ids.get(i));
				fetch.set_archetype(this.fetch_state, archetype, tables);
				func.accept((T) fetch.archetype_fetch_packed());
			}
		}
	}

	public <T> void par_for_each(ExecutorService task_pool, int batch_size, Consumer<T> func) {
		this.par_for_each(this.world, task_pool, batch_size, func);
	}

	public <T> void par_for_each(World world, ExecutorService task_pool, int batch_size, Consumer<T> func) {
		this.validate_world_and_update_archetypes(world);
		this.par_for_each_manual(world, task_pool, batch_size, func, world.getLastChangeTick(), world.getChangeTick());
	}

	public <T> void par_for_each_manual(ExecutorService task_pool, int batch_size, Consumer<T> func, int last_change_tick, int change_tick) {
		this.par_for_each_manual(this.world, task_pool, batch_size, func, last_change_tick, change_tick);
	}

	@SuppressWarnings("unchecked")
	public <T> void par_for_each_manual(World world, ExecutorService task_pool, int batch_size, Consumer<T> func, int last_change_tick, int change_tick) {
		ArrayDeque<Future<Void>> tasks = new ArrayDeque<Future<Void>>(); // Java's tasks system really really sucks, what on earth?!?
		final Fetch fetch = fetch_factory.init_fetch(world, this.fetch_state, last_change_tick, change_tick);
		final FilterFetch filter = filter_factory.init_fetch(world, this.filter_state, last_change_tick, change_tick);
		if (fetch.is_dense() && filter.is_dense()) {
			Tables tables = world.getStorages().tables;
			for (int i = 0; i < this.matched_table_ids.size(); i++) {
				final int final_i = i; // The heck java...
				Table table = tables.get(this.matched_table_ids.get(i));
				int offset = 0;
				while (offset < table.size()) {
					final int final_offset = offset;
					tasks.add(task_pool.submit(() -> {
						Fetch inner_fetch = fetch;
						FilterFetch inner_filter = filter;
						//if (final_i == 0 && final_offset > 0) {
							inner_fetch = fetch_factory.init_fetch(world, this.fetch_state, last_change_tick, change_tick);
							inner_filter = filter_factory.init_fetch(world, this.filter_state, last_change_tick, change_tick);
						//}
						inner_fetch.set_table(this.fetch_state, table);
						inner_filter.set_table(this.filter_state, table);
						int len = Math.min(batch_size, table.size() - final_offset);
						for (int table_index = final_offset; table_index < (final_offset + len); table_index++) {
							if (!inner_filter.table_filter_fetch(table_index)) {
								continue;
							}
							func.accept((T) inner_fetch.table_fetch(table_index));
						}
						return null;
					}));
					offset += batch_size;
				}
			}
		} else {
			Archetypes archetypes = world.getArchetypes();
			for (int i = 0; i < this.matched_archetype_ids.size(); i++) {
				final int final_i = i; // The heck java...
				int offset = 0;
				Archetype archetype = archetypes.get(this.matched_archetype_ids.get(i));
				Tables tables = world.getStorages().tables;
				while (offset < archetype.size()) {
					final int final_offset = offset;
					tasks.add(task_pool.submit(() -> {
						Fetch inner_fetch = fetch;
						FilterFetch inner_filter = filter;
						//if (final_i == 0 && final_offset > 0) {
							inner_fetch = fetch_factory.init_fetch(world, this.fetch_state, last_change_tick, change_tick);
							inner_filter = filter_factory.init_fetch(world, this.filter_state, last_change_tick, change_tick);
						//}
						inner_fetch.set_archetype(this.fetch_state, archetype, tables);
						inner_filter.set_archetype(this.filter_state, archetype, tables);
						int len = Math.min(batch_size, archetype.size() - final_offset);
						for (int archetype_index = final_offset; archetype_index < (final_offset + len); archetype_index++) {
							if (!inner_filter.archetype_filter_fetch(archetype_index)) {
								continue;
							}
							func.accept((T) inner_fetch.archetype_fetch(archetype_index));
						}
						return null;
					}));
					offset += batch_size;
				}
			}
		}
		try {
			while (!tasks.isEmpty()) {
				tasks.remove().get();
			}
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public <T> void par_for_each_packed(ExecutorService task_pool, int batch_size, ParPackedConsumer<T> func) {
		this.par_for_each_packed(this.world, task_pool, batch_size, func);
	}

	public <T> void par_for_each_packed(World world, ExecutorService task_pool, int batch_size, ParPackedConsumer<T> func) {
		this.validate_world_and_update_archetypes(world);
		this.par_for_each_packed_manual(world, task_pool, batch_size, func, world.getLastChangeTick(), world.getChangeTick());
	}

	public <T> void par_for_each_packed_manual(ExecutorService task_pool, int batch_size, ParPackedConsumer<T> func, int last_change_tick, int change_tick) {
		this.par_for_each_packed_manual(this.world, task_pool, batch_size, func, last_change_tick, change_tick);
	}

	@SuppressWarnings("unchecked")
	public <T> void par_for_each_packed_manual(World world, ExecutorService task_pool, int batch_size, ParPackedConsumer<T> func, int last_change_tick, int change_tick) {
		if (this.filter_factory != WorldFilterQuery.NONE) {
			throw new UnsupportedOperationException("Filter queries are not supported on dense iterations");
		}
		ArrayDeque<Future<Void>> tasks = new ArrayDeque<Future<Void>>(); // Java's tasks system really really sucks, what on earth?!?
		final Fetch fetch = fetch_factory.init_fetch(world, this.fetch_state, last_change_tick, change_tick);
		if (fetch.is_dense()) {
			Tables tables = world.getStorages().tables;
			for (int i = 0; i < this.matched_table_ids.size(); i++) {
				final int final_i = i; // The heck java...
				Table table = tables.get(this.matched_table_ids.get(i));
				int offset = 0;
				while (offset < table.size()) {
					final int final_offset = offset;
					tasks.add(task_pool.submit(() -> {
						Fetch inner_fetch = fetch;
						if (final_i == 0 && final_offset > 0) {
							inner_fetch = fetch_factory.init_fetch(world, this.fetch_state, last_change_tick, change_tick);
						}
						inner_fetch.set_table(this.fetch_state, table);
						int len = Math.min(batch_size, table.size() - final_offset);
						func.accept((T) inner_fetch.table_fetch_packed(), final_offset, final_offset + len);
						return null;
					}));
					offset += batch_size;
				}
			}
		} else {
			Archetypes archetypes = world.getArchetypes();
			for (int i = 0; i < this.matched_archetype_ids.size(); i++) {
				final int final_i = i; // The heck java...
				int offset = 0;
				Archetype archetype = archetypes.get(this.matched_archetype_ids.get(i));
				Tables tables = world.getStorages().tables;
				while (offset < archetype.size()) {
					final int final_offset = offset;
					tasks.add(task_pool.submit(() -> {
						Fetch inner_fetch = fetch;
						if (final_i == 0 && final_offset > 0) {
							inner_fetch = fetch_factory.init_fetch(world, this.fetch_state, last_change_tick, change_tick);
						}
						inner_fetch.set_archetype(this.fetch_state, archetype, tables);
						int len = Math.min(batch_size, archetype.size() - final_offset);
						func.accept((T) inner_fetch.archetype_fetch_packed(), final_offset, final_offset + len);
						return null;
					}));
					offset += batch_size;
				}
			}
		}
		try {
			while (!tasks.isEmpty()) {
				tasks.remove().get();
			}
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
