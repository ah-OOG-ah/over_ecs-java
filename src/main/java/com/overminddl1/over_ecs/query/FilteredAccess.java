package com.overminddl1.over_ecs.query;

import java.util.BitSet;

public class FilteredAccess {
	private Access access;
	private BitSet with;
	private BitSet without;

	public FilteredAccess() {
		this.access = new Access();
		this.with = new BitSet();
		this.without = new BitSet();
	}

	public Access access() {
		return this.access;
	}

	public void add_read(int index) {
		this.access.add_read(index);
		this.add_with(index);
	}

	public void add_write(int index) {
		this.access.add_write(index);
		this.add_with(index);
	}

	public void add_with(int index) {
		this.with.set(index);
	}

	public void add_without(int index) {
		this.without.set(index);
	}

	public boolean is_compatible(FilteredAccess other) {
		if (this.access.is_compatible(other.access)) {
			return true;
		} else {
			return this.with.intersects(other.without) || this.without.intersects(other.with);
		}
	}

	public void extend(FilteredAccess other) {
		this.access.extend(other.access);
		this.with.or(other.with);
		this.without.or(other.without);
	}
}
