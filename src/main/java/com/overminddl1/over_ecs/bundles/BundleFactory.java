package com.overminddl1.over_ecs.bundles;

import com.overminddl1.over_ecs.Component;
import com.overminddl1.over_ecs.Components;
import com.overminddl1.over_ecs.Storages;

import java.util.function.Supplier;

/**
 * An interface defining something that has a ID which can be get/set,
 * a way to retrieve the component IDs, and can return a bundle from given components.
 */
public interface BundleFactory {
	Integer get_unique_id();

	void set_unique_id(Integer id);

	int[] component_ids(Components components, Storages storages);

	Bundle from_components(Supplier<Component> func);
}
