package com.overminddl1.over_ecs.bundles;

import java.util.function.Consumer;

public interface Bundle {
	void get_components(Consumer<Object> func);
}
