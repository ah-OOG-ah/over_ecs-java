package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.bundles.Bundle;
import com.overminddl1.over_ecs.bundles.BundleFactory;
import com.overminddl1.over_ecs.bundles.BundleInfo;
import com.overminddl1.over_ecs.bundles.BundleSpawner;
import com.overminddl1.over_ecs.entities.AllocAtWithoutReplacement;
import com.overminddl1.over_ecs.entities.EntityLocation;
import com.overminddl1.over_ecs.query.QueryState;
import com.overminddl1.over_ecs.query.WorldFilterQuery;
import com.overminddl1.over_ecs.query.WorldQuery;
import com.overminddl1.over_ecs.storages.Column;
import com.overminddl1.over_ecs.storages.SparseSet;
import com.overminddl1.over_ecs.storages.Table;
import com.overminddl1.over_ecs.world.ArchetypeComponentAccess;
import com.overminddl1.over_ecs.world.MainThreadValidator;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class World {
	private static AtomicInteger NEXT_ID = new AtomicInteger(0);

	private int id;
	private Entities entities;
	private Components components;
	private Archetypes archetypes;
	private Storages storages;
	private Bundles bundles;
	private SparseSet<ArrayList<Long>> removed_components;
	private ArchetypeComponentAccess archetype_component_access;
	private MainThreadValidator main_thread_validator;
	private AtomicInteger change_tick;
	private int last_change_tick;

	public World() {
		this.id = NEXT_ID.getAndIncrement();
		if (this.id == Integer.MAX_VALUE)
			throw new RuntimeException("Exceeding max amount of registries created in this session");
		this.entities = new Entities();
		this.components = new Components();
		this.archetypes = new Archetypes();
		this.storages = new Storages();
		this.bundles = new Bundles();
		this.removed_components = new SparseSet<>();
		this.archetype_component_access = new ArchetypeComponentAccess();
		this.main_thread_validator = new MainThreadValidator();
		this.change_tick = new AtomicInteger(1);
		this.last_change_tick = 0;
	}

	public int getId() {
		return this.id;
	}

	public Entities getEntities() {
		return entities;
	}

	public Archetypes getArchetypes() {
		return archetypes;
	}

	public Components getComponents() {
		return components;
	}

	public Storages getStorages() {
		return storages;
	}

	public Bundles getBundles() {
		return bundles;
	}

	public SparseSet<ArrayList<Long>> getRemovedComponents() {
		return removed_components;
	}

	public int getChangeTick() {
		return this.change_tick.get();
	}

	public int setChangeTick(int change_tick) {
		return this.change_tick.getAndSet(change_tick);
	}

	public int getLastChangeTick() {
		return this.last_change_tick;
	}

	public int incrementChangeTick() {
		return this.change_tick.getAndIncrement();
	}

	public void check_change_ticks() {
		int change_tick = this.getChangeTick();
		this.storages.tables.check_change_ticks(change_tick);
		this.storages.sparse_sets.check_change_ticks(change_tick);
		ArrayList<Column> values = this.archetypes.resource().getUniqueComponents().getValues();
		for (int i = 0; i < values.size(); i++) {
			values.get(i).check_change_ticks(change_tick);
		}
	}

	public void clear_entities() {
		this.storages.tables.clear();
		this.storages.sparse_sets.clear();
		this.archetypes.clear_entities();
		this.entities.clear();
	}

	@Override
	public String toString() {
		return new StringBuilder().append("World{").append("\n\tid=").append(this.id).append(",\n\tentity_count=").append(this.entities.size()).append(",\n\tarchetype_count=").append(this.archetypes.size()).append(",\n\tcomponent_count=").append(this.components.size()).append(",\n\tresource_count=").append(this.archetypes.resource().getUniqueComponents().size()).append("\n}").toString();
	}

	public int init_component(Class<? extends Component> component_class) {
		return components.init_component(this.storages, component_class);
	}

	public Entity get_or_spawn(long entity) {
		this.flush();
		AllocAtWithoutReplacement result = this.entities.alloc_at_without_replacement(entity);
		if (result.location != null) {
			return new Entity(this, entity, result.location);
		} else if (result.wrong_generation_error == false) {
			return this.spawn_at_internal(entity);
		} else {
			return null;
		}
	}

	public Entity get_entity(long entity) {
		EntityLocation location = this.entities.get(entity);
		if (location != null) {
			return new Entity(this, entity, location);
		} else {
			return null;
		}
	}

	public Entity spawn() {
		this.flush();
		long entity = this.entities.alloc();
		return this.spawn_at_internal(entity);
	}

	private Entity spawn_at_internal(long entity) {
		Archetype archetype = this.archetypes.empty();
		int table_row = this.storages.tables.get(archetype.getTableId()).allocate(entity);
		EntityLocation location = archetype.allocate(entity, table_row);
		this.entities.getMeta(Entity.id(entity)).location = location;
		return new Entity(this, entity, location);
	}

	public <B extends Bundle> long[] spawn_batch(B[] bundles, BundleFactory bundle_factory) {
		this.flush();
		BundleInfo bundle_info = this.bundles.init_info(this.components, this.storages, bundle_factory);
		this.entities.reserve(bundles.length);
		BundleSpawner spawner = bundle_info.get_bundle_spawner(this.entities, this.archetypes, this.components, this.storages, this.change_tick.get());
		spawner.reserve_storage(bundles.length);
		long[] entities = new long[bundles.length];
		for (int i = 0; i < entities.length; i++) {
			B bundle = bundles[i];
			entities[i] = spawner.spawn(bundle);
		}
		return entities;
	}

	public <T extends Component> T get_component(long entity, Class<T> component_class) {
		Entity e = this.get_entity(entity);
		if (e == null) {
			return null;
		} else {
			return e.get(component_class);
		}
	}

	public <T extends Component> T set_component(long entity, Class<T> component_class) {
		Entity e = this.get_entity(entity);
		if (e == null) {
			return null;
		} else {
			return e.set(component_class);
		}
	}

	public <T extends Component> void insert_component(long entity, T component) {
		Entity e = this.get_entity(entity);
		if (e == null) {
			return;
		} else {
			e.insert(component);
		}
	}

	public void remove_component(long entity, Class<? extends Component> component_class) {
		Entity e = this.get_entity(entity);
		if (e == null) {
			return;
		} else {
			e.remove(component_class);
		}
	}

	public <T extends Bundle> void insert_bundle(long entity, T bundle) {
		Entity e = this.get_entity(entity);
		if (e == null) {
			return;
		} else {
			e.insert_bundle(bundle);
		}
	}

	public <T extends BundleFactory> void remove_bundle(long entity, T bundle_factory) {
		Entity e = this.get_entity(entity);
		if (e == null) {
			return;
		} else {
			e.remove_bundle(bundle_factory);
		}
	}

	public void flush() {
		Archetype empty_archetype = this.archetypes.empty();
		Table table = this.storages.tables.get(empty_archetype.getTableId());
		this.entities.flush((entity, meta) -> {
			meta.location = empty_archetype.allocate(entity, table.allocate(entity));
		});
	}

	public boolean despawn(long entity) {
		Entity e = this.get_entity(entity);
		if (e == null) {
			return false;
		} else {
			e.despawn();
			return true;
		}
	}

	public void clear_trackers() {
		ArrayList<ArrayList<Long>> values = this.removed_components.getValues();
		for (int i = 0; i < values.size(); i++) {
			values.get(i).clear();
		}
		this.last_change_tick = this.incrementChangeTick();
	}

	public <Q extends WorldQuery> QueryState query(Q fetch_query) {
		return new QueryState(this, fetch_query, WorldFilterQuery.NONE);
	}
}
