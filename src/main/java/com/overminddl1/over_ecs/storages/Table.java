package com.overminddl1.over_ecs.storages;

import com.overminddl1.over_ecs.components.ComponentInfo;
import com.overminddl1.over_ecs.components.ComponentTicks;

import java.util.ArrayList;

public class Table {
	public static final int EMPTY_ID = 0;
	private SparseSet<Column> columns;
	private ArrayList<Long> entities;

	public Table(int capacity, int column_capacity) {
		columns = new SparseSet<Column>(column_capacity);
		entities = new ArrayList<Long>(capacity);
	}

	public Table() {
		this(0, 0);
	}

	public ArrayList<Long> getEntities() {
		return entities;
	}

	public void add_column(ComponentInfo component_info) {
		columns.insert(component_info.getId(), new Column(component_info, entities.size()));
	}

	public Long swap_remove(int row) {
		for (int i = 0; i < this.columns.size(); i++) {
			this.columns.get(i).swap_remove(row);
		}
		boolean is_last = row == this.entities.size() - 1;
		StorageUtils.swap_remove(this.entities, row);
		if (is_last) {
			return null;
		} else {
			return this.entities.get(row);
		}
	}

	public void move_to_and_forget_missing(int row, Table new_table, TableMoveResult table_result) {
		assert !(row < this.size());
		boolean is_last = row == this.entities.size() - 1;
		int new_row = new_table.allocate(StorageUtils.swap_remove(this.entities, row));
		ArrayList<Column> column_values = this.columns.getValues();
		ColumnSwapRemoveResult column_result = new ColumnSwapRemoveResult();
		for (int i = 0; i < column_values.size(); i++) {
			Column column = column_values.get(i);
			column.swap_remove(row, column_result);
			Column new_column = new_table.get_column(column.component_id);
			if (new_column != null) {
				new_column.initialize(new_row, column_result.data, column_result.ticks);
			}
		}
		table_result.new_row = new_row;
		table_result.swapped_entity = is_last ? null : this.entities.get(row);
	}

	public void move_to_and_drop_missing(int row, Table new_table, TableMoveResult table_result) {
		assert !(row < this.size());
		boolean is_last = row == this.entities.size() - 1;
		int new_row = new_table.allocate(StorageUtils.swap_remove(this.entities, row));
		ArrayList<Column> column_values = this.columns.getValues();
		ColumnSwapRemoveResult column_result = new ColumnSwapRemoveResult();
		for (int i = 0; i < column_values.size(); i++) {
			Column column = column_values.get(i);
			Column new_column = new_table.get_column(column.component_id);
			if (new_column != null) {
				column.swap_remove(row, column_result);
				new_column.initialize(new_row, column_result.data, column_result.ticks);
			} else {
				column.swap_remove(row, column_result);
			}
		}
		table_result.new_row = new_row;
		table_result.swapped_entity = is_last ? null : this.entities.get(row);
	}

	public void move_to_superset(int row, Table new_table, TableMoveResult table_result) {
		assert(row < this.size());
		boolean is_last = row == this.entities.size() - 1;
		int new_row = new_table.allocate(StorageUtils.swap_remove(this.entities, row));
		ArrayList<Column> column_values = this.columns.getValues();
		ColumnSwapRemoveResult column_result = new ColumnSwapRemoveResult();
		for (int i = 0; i < column_values.size(); i++) {
			Column column = column_values.get(i);
			Column new_column = new_table.get_column(column.component_id);
			column.swap_remove(row, column_result);
			new_column.initialize(new_row, column_result.data, column_result.ticks);
		}
		table_result.new_row = new_row;
		table_result.swapped_entity = is_last ? null : this.entities.get(row);
	}

	public Column get_column(int component_id) {
		return this.columns.get(component_id);
	}

	public boolean has_column(int component_id) {
		return this.columns.contains(component_id);
	}

	public void reserve(int additional) {
		this.entities.ensureCapacity(this.entities.size() + additional);
		ArrayList<Column> column_values = this.columns.getValues();
		for (int i = 0; i < column_values.size(); i++) {
			column_values.get(i).reserveCapacity(additional);
		}
	}

	public int allocate(long entity) {
		int index = this.entities.size();
		this.entities.add(entity);
		ArrayList<Column> column_values = this.columns.getValues();
		for (int i = 0; i < column_values.size(); i++) {
			Column column = column_values.get(i);
			column.data.add(null);
			column.ticks.add(new ComponentTicks(0));
		}
		return index;
	}

	public int size() {
		return this.entities.size();
	}

	public void check_change_ticks(int change_tick) {
		ArrayList<Column> column_values = this.columns.getValues();
		for (int i = 0; i < column_values.size(); i++) {
			column_values.get(i).check_change_ticks(change_tick);
		}
	}

	public ArrayList<Column> get_column_values() {
		return this.columns.getValues();
	}

	public void clear() {
		this.entities.clear();
		ArrayList<Column> column_values = this.columns.getValues();
		for (int i = 0; i < column_values.size(); i++) {
			column_values.get(i).clear();
		}
	}
}
