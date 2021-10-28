package com.overminddl1.over_ecs.bundles;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class BundleTest {
	static class TestBundle implements Bundle {
		int i = 0;
		@Override
		public void get_components(Consumer<Object> func) {
			switch (i++) {
				case 0: func.accept("test"); break;
				case 1: func.accept(42); break;
				default: throw new RuntimeException("Too many components!");
			}
		}
	}

	@Test
	void get_components() {
		Bundle bundle = new TestBundle();
		bundle.get_components((Object o) -> {assertEquals("test", o);});
		bundle.get_components((Object o) -> {assertEquals(42, o);});
	}
}
