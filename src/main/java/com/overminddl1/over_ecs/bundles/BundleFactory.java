package com.overminddl1.over_ecs.bundles;

import com.overminddl1.over_ecs.Components;
import com.overminddl1.over_ecs.Storages;

import java.util.function.Supplier;

public interface BundleFactory {
	Class<? extends Bundle> get_bundle_class();
	int[] component_ids(Components components, Storages storages);
	Bundle from_components(Supplier<Object> func);
}
