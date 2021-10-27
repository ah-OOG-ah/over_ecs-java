package com.overminddl1.over_ecs;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class EntitiesTest {
	@Test
	void create_entities() {
		Entities entities = new Entities(0);
		assertEquals(entities.alloc(), 0);
		assertEquals(entities.alloc(), 1);
		assertEquals(entities.alloc(), 2);
	}

	@Test
	void create_entity_at() {
		Entities entities = new Entities(0);
		assertEquals(entities.size(), 0);
		assertEquals(entities.capacity(), 0);
		assertNull(entities.alloc_at(4));
		assertEquals(entities.size(), 1);
		assertEquals(entities.capacity(), 5);
		assertEquals(entities.resolve_from_id(4), 4);
		assertEquals(entities.resolve_from_id(3), 3);
		assertNull(entities.resolve_from_id(5));
	}

	@Test
	void reuse_entity() {
		long entity;
		Entities entities = new Entities(0);
		assertEquals(entities.size(), 0);
		assertEquals(entities.capacity(), 0);
		entity = entities.alloc();
		assertEquals(entity, 0);
		assertNotNull(entities.free(entity));
		assertNull(entities.free(entity));
		entity = entities.alloc();
		assertEquals(entity, (1L << 32) + 0);
	}

	@Test
	void reserve_entity() {
		AtomicInteger count = new AtomicInteger();
		Entities entities = new Entities(0);
		assertEquals(0, entities.reserve_entity());
		assertEquals(0, entities.resolve_from_id(0));
		assertEquals(0, entities.count());
		assertThrows(AssertionError.class, entities::alloc);
		assertFalse(entities.contains(0));
		count.set(0);
		entities.flush((entity, location) -> {
			location.archetype_id = 0;
			count.getAndIncrement();
		});
		assertEquals(1, count.get());
		assertTrue(entities.contains(0));
		assertEquals(1, entities.count());
		assertNotNull(entities.free(0));
		// And reuse it
		assertEquals(0, entities.count());
		assertEquals((1L << 32) | 0, entities.reserve_entity());
		assertEquals(0, entities.count());
		assertTrue(entities.contains((1L << 32) | 0));
		count.set(0);
		entities.flush((entity, location) -> {
			location.archetype_id = 0;
			count.getAndIncrement();
		});
		assertEquals(1, count.get());
		assertEquals(1, entities.count());
	}

	@Test
	void reserve_new_entities() {
		AtomicInteger count = new AtomicInteger();
		Entities entities = new Entities(0);
		assertArrayEquals(new long[]{0, 1, 2, 3}, entities.reserve_entities(4));
		assertEquals(0, entities.resolve_from_id(0));
		assertEquals(0, entities.count());
		assertFalse(entities.contains(0));
		count.set(0);
		entities.flush((entity, location) -> {
			location.archetype_id = 0;
			count.getAndIncrement();
		});
		assertEquals(4, count.get());
		assertTrue(entities.contains(0));
		assertEquals(4, entities.count());
		assertNotNull(entities.free(3));
		assertNotNull(entities.free(2));
		assertNotNull(entities.free(1));
		assertNotNull(entities.free(0));
		// And reuse it
		assertEquals(0, entities.count());
		assertArrayEquals(new long[]{(1L << 32) | 0, (1L << 32) | 1, (1L << 32) | 2, (1L << 32) | 3}, entities.reserve_entities(4));
		assertEquals(0, entities.count());
		assertTrue(entities.contains((1L << 32) | 0));
		count.set(0);
		entities.flush((entity, location) -> {
			location.archetype_id = 0;
			count.getAndIncrement();
		});
		assertEquals(4, count.get());
		assertEquals(4, entities.count());
	}
}
