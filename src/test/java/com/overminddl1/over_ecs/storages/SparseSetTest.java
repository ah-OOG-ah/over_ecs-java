package com.overminddl1.over_ecs.storages;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SparseSetTest {

	@Test
	void operations() {
		AtomicInteger count = new AtomicInteger();
		SparseSet<Integer> set = new SparseSet<>();
		assertEquals(0, set.size());
		assertFalse(set.contains(0));
		set.insert(0, 42);
		assertEquals(1, set.size());
		assertTrue(set.contains(0));
		set.insert(256, 42);
		assertEquals(2, set.size());
		assertTrue(set.contains(256));
		assertEquals(42, set.remove(0));
		assertEquals(1, set.size());
		count.set(0);
		assertEquals(42, set.get_or_insert(12, () -> { count.getAndIncrement(); return 42; }));
		assertEquals(1, count.get());
		count.set(0);
		assertEquals(42, set.get_or_insert(12, () -> { count.getAndIncrement(); return 42; }));
		assertEquals(0, count.get());
		assertEquals(42, set.remove(12));
		count.set(0);
		assertEquals(42, set.get_or_insert(12, () -> { count.getAndIncrement(); return 42; }));
		assertEquals(1, count.get());
	}
}
