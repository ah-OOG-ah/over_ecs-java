package com.overminddl1.over_ecs.storages;

import com.overminddl1.over_ecs.Components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Tables {
	private ArrayList<Table> tables;
	private HashMap<Integer, Integer> table_ids;

	public Tables() {
		this.tables = new ArrayList<Table>();
        this.table_ids = new HashMap<Integer, Integer>();

		this.tables.add(new Table(0, 0));
    }

	public int size() {
        return this.tables.size();
    }

	public Table get(int index) {
        return this.tables.get(index);
    }

	public int get_id_or_insert(int[] component_ids, Components components) {
		// Java sucks...
		int hash = Arrays.hashCode(component_ids);
		Integer id = this.table_ids.get(hash);
		if(id == null) {
			Table table = new Table(0, component_ids.length);
			for (int i = 0; i < component_ids.length; i++) {
				table.add_column(components.getInfo(component_ids[i]));
			}
			this.table_ids.put(hash, this.tables.size());
			this.tables.add(table);
			return this.tables.size() - 1;
		} else {
			return id;
		}
	}

	public ArrayList<Table> getTables() {
		return tables;
	}

	public void clear() {
        this.tables.clear();
        this.table_ids.clear();
    }

	public void check_change_ticks(int change_tick) {
		for (int i = 0; i < this.tables.size(); i++) {
            this.tables.get(i).check_change_ticks(change_tick);
        }
	}
}
