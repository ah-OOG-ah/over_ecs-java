package com.overminddl1.over_ecs.bundles;

import com.overminddl1.over_ecs.Component;
import com.overminddl1.over_ecs.Components;
import com.overminddl1.over_ecs.Storages;
import com.overminddl1.over_ecs.test.ComponentsTestData.TestingI;
import com.overminddl1.over_ecs.test.ComponentsTestData.TestingS;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BundleTest {
	@Test
	void get_components() {
		Components components = new Components();
		Storages storages = new Storages();
		components.init_component(storages, TestingS.class);
		components.init_component(storages, TestingI.class);
		BundleFactory bundle_factory = new TestBundleFactory();
		assertArrayEquals(new int[]{0, 1}, bundle_factory.component_ids(components, storages));
		Bundle bundle = bundle_factory.from_components(() -> null);
		bundle.get_components((Component o) -> {
			assertEquals("test", ((TestingS) o).value);
		});
		bundle.get_components((Component o) -> {
			assertEquals(42, ((TestingI) o).value);
		});
	}

	static class TestBundle implements Bundle {
		int i = 0;
		Component[] components;

		TestBundle(Component... components) {
			this.components = components;
		}

		@Override
		public void get_components(Consumer<Component> func) {
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
		public Integer get_unique_id() {
			return this.id;
		}

		@Override
		public void set_unique_id(Integer id) {
			this.id = id;
		}

		@Override
		public int[] component_ids(Components components, Storages storages) {
			return new int[]{
					components.getId(TestingS.class),
					components.getId(TestingI.class),
			};
		}

		@Override
		public Bundle from_components(Supplier<Component> func) {
			// The passed here object allows us to reuse an existing storage, but can't for strings and integers...
			return new TestBundle(new TestingS("test"), new TestingI(42));
		}
	}
}
