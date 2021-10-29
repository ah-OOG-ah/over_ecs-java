package com.overminddl1.over_ecs.bundles;

import com.overminddl1.over_ecs.Components;
import com.overminddl1.over_ecs.Storages;

import java.util.function.Supplier;

public interface BundleFactory {
	void set_unique_id(Integer id);
	Integer get_unique_id();
	int[] component_ids(Components components, Storages storages);

	Bundle from_components(Supplier<Object> func);
}
