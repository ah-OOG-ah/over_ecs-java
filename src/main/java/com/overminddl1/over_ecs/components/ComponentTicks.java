package com.overminddl1.over_ecs.components;

public class ComponentTicks {
	private static final int MAX_DELTA = (Integer.MAX_VALUE / 4) * 3; // Lack of unsigned integers really really sucks...
	public int added;
	public int changed;

	public ComponentTicks(int change_tick) {
		added = change_tick;
		changed = change_tick;
	}

	private static int clamp_tick(int last_change_tick, int change_tick) {
		int tick_delta = change_tick - last_change_tick;
		if (tick_delta > MAX_DELTA) {
			return change_tick - MAX_DELTA;
		} else {
			return last_change_tick;
		}
	}

	public boolean is_added(int last_change_tick, int change_tick) {
		int component_delta = change_tick - this.added;
		int system_delta = change_tick - last_change_tick;
		return component_delta < system_delta;
	}

	public boolean is_changed(int last_change_tick, int change_tick) {
		int component_delta = change_tick - this.changed;
		int system_delta = change_tick - last_change_tick;
		return component_delta < system_delta;
	}

	public void check_ticks(int change_tick) {
		this.added = ComponentTicks.clamp_tick(this.added, change_tick);
		this.changed = ComponentTicks.clamp_tick(this.changed, change_tick);
	}

	public void set_changed(int change_tick) {
		this.changed = change_tick;
	}
}
