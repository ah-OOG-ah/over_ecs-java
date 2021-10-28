package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.storages.SparseSet;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Registry {
	private static AtomicInteger NEXT_ID = new AtomicInteger(0);

	private int id;
	private Entities entities;
	private Components components;
	private Archetypes archetypes;
	private Storages storages;
	private Bundles bundles;
	private SparseSet<ArrayList<Long>> removed_components;
//	private ArchetypeComponentAccess archetype_component_access;
//	private MainThreadValidator main_thread_validator;
	private AtomicInteger change_tick;
	private int last_change_tick;

	public Registry(int entityCapacity) {
		this.id = NEXT_ID.getAndIncrement();
		if (this.id == Integer.MAX_VALUE)
			throw new RuntimeException("Exceeding max amount of registries created in this session");
		this.entities = new Entities(entityCapacity);
	}
}
