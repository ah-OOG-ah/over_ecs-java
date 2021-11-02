package com.overminddl1.over_ecs.world;

public interface DetectChanges {
	boolean is_added();

	boolean is_changed();

	void set_changed();
}
