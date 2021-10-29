package com.overminddl1.over_ecs.bundles;

import com.overminddl1.over_ecs.Components;
import com.overminddl1.over_ecs.Storages;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BundleTest {
	static class TestBundle implements Bundle {
		int i = 0;
		Object[] components;

		TestBundle(Object... components) {
            this.components = components;
        }

		@Override
		public void get_components(Consumer<Object> func) {
			func.accept(this.components[this.i++]);
		}

		@Override
		public BundleFactory get_factory() {
			return new TestBundleFactory();
		}
	}

	static class TestBundleFactory implements BundleFactory {
		Integer id = null;

		@Override
		public void set_unique_id(Integer id) {
			this.id = id;
		}

		@Override
		public Integer get_unique_id() {
			return this.id;
		}

		@Override
		public int[] component_ids(Components components, Storages storages) {
			return new int[]{
					components.getId(String.class),
					components.getId(Integer.class),
			};
		}

		@Override
		public Bundle from_components(Supplier<Object> func) {
			// The passed here object allows us to reuse an existing storage, but can't for strings and integers...
			return new TestBundle("test", 42);
		}
	}

	@Test
	void get_components() {
		Components components = new Components();
		Storages storages = new Storages();
		components.init_component(storages, String.class);
		components.init_component(storages, Boolean.class);
		components.init_component(storages, Integer.class);
		BundleFactory bundle_factory = new TestBundleFactory();
		assertArrayEquals(new int[]{0, 2}, bundle_factory.component_ids(components, storages));
		Bundle bundle = bundle_factory.from_components(() -> null);
		bundle.get_components((Object o) -> {
			assertEquals("test", o);
		});
		bundle.get_components((Object o) -> {
			assertEquals(42, o);
		});
	}
}
