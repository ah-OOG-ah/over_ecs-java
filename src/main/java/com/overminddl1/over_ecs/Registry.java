package com.overminddl1.over_ecs;

import java.util.concurrent.atomic.AtomicInteger;

public class Registry {
	private static AtomicInteger NEXT_ID = new AtomicInteger(0);

	private int id;
	private Entities entities;
	private Components components;
	private Archetypes archetypes;
	private Storages storages;
//	private Bundles bundles;
//	private SparseSet removed_components;

	public Registry(int entityCapacity) {
		this.id = NEXT_ID.getAndIncrement();
		if (this.id == Integer.MAX_VALUE)
			throw new RuntimeException("Exceeding max amount of registries created in this session");
		this.entities = new Entities(entityCapacity);
	}
}
