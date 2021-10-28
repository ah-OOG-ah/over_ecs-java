package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.entities.EntityLocation;
import com.overminddl1.over_ecs.entities.EntityMeta;
import com.overminddl1.over_ecs.entities.FlushLocator;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Entities {
	// Does this JIT optimize this stuff anywhere near as well as native languages do?
	// Maybe need to split out into primitive arrays... kotlin or scala would be so much easier...
	private ArrayList<EntityMeta> meta;
	private ArrayList<Integer> pending;
	private AtomicInteger free_cursor;
	private int size;

	public Entities(int capacity) {
		this.meta = new ArrayList<>(capacity);
		this.pending = new ArrayList<>(capacity);
		this.free_cursor = new AtomicInteger(0);
		this.size = 0;
	}

	public Entities() {
		this(1024);
	}

	private boolean needs_flush() {
		return this.free_cursor.get() != this.pending.size();
	}

	public long[] reserve_entities(int count) {
		int range_end = this.free_cursor.getAndAdd(-count);
		int range_start = range_end - count;
		int free_range_start = Math.max(range_start, 0);
		int free_range_end = Math.max(range_end, 0);
		int new_id_start = 0;
		int new_id_end = 0;
		if (range_start < 0) {
			int base = this.meta.size();
			new_id_end = base - range_start;
			new_id_start = base - Math.min(range_end, 0);
		}

		long[] ids = new long[count];
		int i = 0;
		for (int p = free_range_start; p < free_range_end; p++) {
			int id = this.pending.get(this.pending.size() - i - 1);
			int generation = this.meta.get(id).generation;
			ids[i++] = Entity.init(generation, id);
		}
		for (int id = new_id_start; id < new_id_end; id++) {
			ids[i++] = Entity.init(0, id);
		}
		return ids;
	}

	public long reserve_entity() {
		int n = this.free_cursor.getAndDecrement();
		if (n > 0) {
			int id = this.pending.get(n - 1);
			return Entity.init(this.meta.get(id).generation, id);
		} else {
			return Entity.init(0, this.meta.size() - n);
		}
	}

	public long alloc() {
		assert !this.needs_flush();
		this.size += 1;
		if (this.pending.size() > 0) {
			int id = this.pending.remove(this.pending.size() - 1);
			int new_free_cursor = this.pending.size();
			this.free_cursor.set(new_free_cursor);
			return Entity.init(this.meta.get(id).generation, id);
		} else {
			assert this.meta.size() < Integer.MAX_VALUE;
			int id = this.meta.size();
			this.meta.add(EntityMeta.EMPTY.clone());
			return Entity.init(0, id);
		}
	}

	// Returns the previous entity data if it existed, or null if it didn't
	public EntityLocation alloc_at(long entity) {
		assert !this.needs_flush();
		int id = Entity.id(entity);
		EntityLocation loc = null;
		if (id >= this.meta.size()) {
			this.pending.ensureCapacity(id + 1);
			this.meta.ensureCapacity(id + 1);
			int i = this.meta.size();
			while (this.meta.size() <= id) {
				this.pending.add(this.pending.size() - 1);
				this.meta.add(EntityMeta.EMPTY.clone());
			}
			this.free_cursor.set(this.pending.size());
			this.size += 1;
		} else {
			int idx = this.pending.indexOf(id);
			if (idx >= 0) {
				this.pending.set(id, this.pending.remove(this.pending.size() - 1));
				this.free_cursor.set(this.pending.size());
				this.size += 1;
			} else {
				EntityMeta meta = this.meta.get(id);
				loc = meta.location;
				meta.location = EntityLocation.EMPTY.clone();
			}
		}

		this.meta.get(id).generation = Entity.generation(entity);

		return loc;
	}

	public EntityLocation alloc_at_without_replacement(long entity) {
		assert !this.needs_flush();
		int id = Entity.id(entity);
		if (id >= this.meta.size()) {
			this.pending.ensureCapacity(id + 1);
			this.meta.ensureCapacity(id + 1);
			int i = this.meta.size();
			while (this.meta.size() <= id) {
				this.pending.add(this.pending.size() - 1);
				this.meta.add(EntityMeta.EMPTY.clone());
			}
			this.free_cursor.set(this.pending.size());
			this.size += 1;
			return null;
		} else {
			int idx = this.pending.indexOf(id);
			if (idx >= 0) {
				this.pending.set(id, this.pending.remove(this.pending.size() - 1));
				this.free_cursor.set(this.pending.size());
				this.size += 1;
				return null;
			} else {
				EntityMeta meta = this.meta.get(id);
				if (meta.location.archetype_id == EntityLocation.INVALID_ARCHETYPE_ID) {
					meta.generation = Entity.generation(entity);
					return null;
				} else if (meta.generation == Entity.generation(entity)) {
					return meta.location;
				} else {
					return null;
				}
			}
		}
	}

	public int size() {
		return this.size;
	}

	public int capacity() {
		return this.meta.size();
	}

	public EntityLocation free(long entity) {
		assert !this.needs_flush();
		int id = Entity.id(entity);
		if (id < this.meta.size()) {
			EntityMeta meta = this.meta.get(id);
			if (meta.generation != Entity.generation(entity)) {
				return null;
			}
			meta.generation += 1;
			EntityLocation loc = meta.clear_location();
			this.pending.add(id);
			this.free_cursor.set(this.pending.size());
			this.size -= 1;
			return loc;
		} else {
			return null;
		}
	}

	public void reserve(int additional) {
		assert !this.needs_flush();
		int freelist_size = this.free_cursor.get();
		int shortfall = additional - freelist_size;
		if (shortfall > 0) {
			this.meta.ensureCapacity(this.meta.size() + shortfall);
		}
	}

	public void clear() {
		this.meta.clear();
		this.pending.clear();
		this.free_cursor.set(0);
		this.size = 0;
	}

	public boolean contains(long entity) {
		int id = Entity.id(entity);
		if (id < this.meta.size()) {
			return this.meta.get(id).generation == Entity.generation(entity);
		} else {
			return false;
		}
	}

	public int count() {
		return this.size;
	}

	public EntityLocation get(long entity) {
		int id = Entity.id(entity);
		if (id < this.meta.size()) {
			EntityMeta meta = this.meta.get(id);
			if (meta.generation != Entity.generation(entity) || meta.location.archetype_id == EntityLocation.INVALID_ARCHETYPE_ID) {
				return null;
			} else {
				return meta.location;
			}
		} else {
			return null;
		}
	}

	public Long resolve_from_id(int id) {
		if (this.meta.size() > id) {
			return this.meta.get(id).as_entity(id);
		} else {
			int free_cursor = this.free_cursor.get();
			if (free_cursor >= 0) {
				return null;
			} else if (id < this.meta.size() + -free_cursor) {
				return Entity.init(0, id);
			} else {
				return null;
			}
		}
	}

	public void flush(FlushLocator init) {
		int current_free_cursor = this.free_cursor.get();
		int new_free_cursor = current_free_cursor;
		if (current_free_cursor < 0) {
			int old_meta_size = this.meta.size();
			int new_meta_size = old_meta_size + -current_free_cursor;
			this.meta.ensureCapacity(new_meta_size);
			for (int id = old_meta_size; id < new_meta_size; id++) {
				EntityMeta meta = EntityMeta.EMPTY.clone();
				this.meta.add(meta);
				init.locate(Entity.init(meta.generation, id), meta.location);
			}
			this.size += -current_free_cursor;
			this.free_cursor.set(0);
			new_free_cursor = 0;
		}
		this.size += this.pending.size() - new_free_cursor;
		while (this.pending.size() > new_free_cursor) {
			int id = this.pending.remove(this.pending.size() - 1);
			EntityMeta meta = this.meta.get(id);
			init.locate(Entity.init(meta.generation, id), meta.location);
		}
	}

	public void flush_as_invalid() {
		this.flush((meta, location) -> {
			location.archetype_id = EntityLocation.INVALID_ARCHETYPE_ID;
		});
	}

	public EntityMeta getMeta(long entity) {
		return this.meta.get(Entity.id(entity));
	}
}
