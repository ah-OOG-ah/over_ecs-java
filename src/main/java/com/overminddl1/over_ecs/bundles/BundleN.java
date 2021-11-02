package com.overminddl1.over_ecs.bundles;

import com.overminddl1.over_ecs.Component;
import com.overminddl1.over_ecs.Components;
import com.overminddl1.over_ecs.Storages;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class BundleN implements Bundle {
	private BundleNFactory factory;
	private Component[] components;

	public BundleN(Component... components) {
		this.components = components;
		this.factory = new BundleNFactory(components.length);
	}

	public Object get(int index) {
		return this.components[index];
	}

	public BundleN set(int index, Component value) {
		if (value.getClass() != this.components[index].getClass()) {
			this.factory.id = null;
			this.factory.types = null;
		}
		this.components[index] = value;
		return this;
	}

	public BundleN set_unchecked(int index, Component value) {
		this.components[index] = value;
		return this;
	}

	public int size() {
		return this.components.length;
	}

	@Override
	public void get_components(Consumer<Component> func) {
		for (int i = 0; i < this.components.length; i++) {
			func.accept(this.components[i]);
		}
	}

	@Override
	public BundleFactory get_factory() {
		return factory;
	}

	private class BundleNFactory implements BundleFactory {
		Integer id;
		int count;
		int[] types;

		BundleNFactory(int count) {
			this.id = null;
			this.count = count;
			this.types = null;
		}

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
			if (this.types == null) {
				this.types = new int[this.count];
				for (int i = 0; i < this.count; i++) {
					this.types[i] = components.getId(BundleN.this.components[i].getClass());
				}
			}
			return this.types;
		}

		@Override
		public Bundle from_components(Supplier<Component> func) {
			Component[] components = new Component[this.count];
			for (int i = 0; i < this.count; i++) {
				components[i] = func.get();
			}
			return new BundleN(components);
		}
	}
}
