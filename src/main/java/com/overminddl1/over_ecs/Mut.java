package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.components.ComponentTicks;
import com.overminddl1.over_ecs.world.DetectChanges;

public class Mut<T extends Component> implements DetectChanges {
	private T value;
	private ComponentTicks component_ticks;
	private int last_change_tick;
	private int change_tick;

	public Mut(T value, ComponentTicks component_ticks, int last_change_tick, int change_tick) {
		this.value = value;
		this.component_ticks = component_ticks;
		this.last_change_tick = last_change_tick;
		this.change_tick = change_tick;
	}

	public T get() {
		return this.value;
	}

	public T set() {
		this.set_changed();
		return this.value;
	}

	@Override
	public boolean is_added() {
		return this.component_ticks.is_added(this.last_change_tick, this.change_tick);
	}

	@Override
	public boolean is_changed() {
		return this.component_ticks.is_changed(this.last_change_tick, this.change_tick);
	}

	@Override
	public void set_changed() {
		this.component_ticks.set_changed(this.change_tick);
	}

	@Override
	public String toString() {
		return "Mut{" + value + '}';
	}
}
