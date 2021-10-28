package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.archetypes.Archetype;
import com.overminddl1.over_ecs.archetypes.ArchetypeIdentity;
import com.overminddl1.over_ecs.storages.Table;

import java.util.ArrayList;
import java.util.HashMap;

public class Archetypes {
	public static final int EMPTY_ID = 0;
	public static final int RESOURCE_ID = 1;
	public static final int INVALID_ID = -1;


	protected ArrayList<Archetype> archetypes;
	protected int archetype_component_count;
	protected HashMap<ArchetypeIdentity, Integer> archetype_ids;

	public Archetypes() {
		this.archetypes = new ArrayList<Archetype>();
		this.archetype_component_count = 0;
		this.archetype_ids = new HashMap<ArchetypeIdentity, Integer>();

		this.get_id_or_insert(Table.EMPTY_ID, new int[0], new int[0]);
		this.archetypes.add(new Archetype(Archetypes.RESOURCE_ID, Table.EMPTY_ID, new int[0], new int[0], new int[0], new int[0]));
	}

	public int generation() {
		return this.archetypes.size();
	}

	public int size() {
		return this.archetypes.size();
	}

	public Archetype empty() {
		return this.archetypes.get(Archetypes.EMPTY_ID);
	}

	public Archetype resource() {
		return this.archetypes.get(Archetypes.RESOURCE_ID);
	}

	public Archetype get(int id) {
		return this.archetypes.get(id);
	}

	public ArrayList<Archetype> getArchetypes() {
		return archetypes;
	}

	public int get_id_or_insert(int table_id, int[] table_components, int[] sparse_set_components) {
		ArchetypeIdentity archetype_identity = new ArchetypeIdentity(table_components, sparse_set_components);
		Integer archetype_id = this.archetype_ids.get(archetype_identity);
		if (archetype_id != null) {
			return archetype_id;
		} else {
			int id = this.archetypes.size();
			int[] table_archetype_components = new int[table_components.length];
			for (int i = 0; i < table_archetype_components.length; i++) {
				table_archetype_components[i] = this.archetype_component_count++;
			}
			int[] sparse_set_archetype_components = new int[sparse_set_components.length];
			for (int i = 0; i < sparse_set_archetype_components.length; i++) {
				sparse_set_archetype_components[i] = this.archetype_component_count++;
			}
			this.archetypes.add(new Archetype(id, table_id, table_components, sparse_set_components, table_archetype_components, sparse_set_archetype_components));
			return id;
		}
	}

	public int archetype_components_len() {
		return this.archetype_component_count;
	}

	public void clear_entities() {
		for (Archetype archetype : this.archetypes) {
			archetype.clear_entities();
		}
	}
}
