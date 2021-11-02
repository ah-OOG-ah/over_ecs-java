package com.overminddl1.over_ecs.query;

import java.util.BitSet;

public class Access {
	private boolean reads_all;
	private BitSet reads_and_writes;
	private BitSet writes;

	public Access() {
		reads_all = false;
		reads_and_writes = new BitSet();
		writes = new BitSet();
	}

	public void add_read(int index) {
		this.reads_and_writes.set(index);
	}

	public void add_write(int index) {
		this.reads_and_writes.set(index);
		this.writes.set(index);
	}

	public boolean has_read(int index) {
		if (this.reads_all) {
			return true;
		} else {
			return this.reads_and_writes.get(index);
		}
	}

	public boolean has_write(int index) {
		return this.writes.get(index);
	}

	public void read_all() {
		this.reads_all = true;
	}

	public boolean reads_all() {
		return this.reads_all;
	}

	public void clear() {
		this.reads_all = false;
		this.reads_and_writes.clear();
		this.writes.clear();
	}

	public void extend(Access other) {
		this.reads_all = this.reads_all || other.reads_all;
		this.reads_and_writes.or(other.reads_and_writes);
		this.writes.or(other.writes);
	}

	public boolean is_compatible(Access other) {
		if (this.reads_all) {
			return 0 == other.writes.cardinality();
		} else if (other.reads_all) {
			return 0 == this.writes.cardinality();
		} else {
			return (!this.writes.intersects(other.reads_and_writes)) && (!this.reads_and_writes.intersects(other.writes));
		}
	}

	public int[] get_conflicts(Access other) {
		BitSet conflicts = new BitSet();
		if (this.reads_all) {
			conflicts.or(other.writes);
		}
		if (other.reads_all) {
			conflicts.or(this.writes);
		}
		BitSet write_intersect = (BitSet) this.writes.clone();
		write_intersect.and(other.reads_and_writes);
		conflicts.or(write_intersect);
		BitSet rw_intersect = (BitSet) this.writes.clone();
		rw_intersect.and(other.writes);
		conflicts.or(rw_intersect);
		return conflicts.stream().toArray();
	}
}
