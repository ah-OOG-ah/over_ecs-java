package com.overminddl1.over_ecs.bundles;

import com.overminddl1.over_ecs.Component;

import java.util.function.Consumer;

/**
 * An interface defining something with components that can be got and a BundleFactory
 */
public interface Bundle {
	void get_components(Consumer<Component> func);

	BundleFactory get_factory();
}
