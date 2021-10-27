package com.overminddl1.over_ecs.storages;

import com.overminddl1.over_ecs.Components;
import com.overminddl1.over_ecs.Storages;
import com.overminddl1.over_ecs.components.ComponentDescriptor;
import com.overminddl1.over_ecs.components.ComponentInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

	@Test
	void getEntities() {
		Components components = new Components();
		Storages storages = new Storages();
		int string_id = components.init_component(storages, String.class);
		int boolean_id = components.init_component(storages, Boolean.class);
		int integer_id = components.init_component(storages, Integer.class);
		assertEquals(0, string_id);
		assertEquals(1, boolean_id);
		assertEquals(2, integer_id);
		int[] columns = {integer_id, string_id};
		Table table = new Table(0, columns.length);
		for (int i = 0; i < columns.length; i++) {
			table.add_column(components.getInfo(columns[i]));
		}

		int row1 = table.allocate(1);
		table.get_column(string_id).initialize_data(row1, "test1");
		table.get_column(integer_id).initialize_data(row1, 1);

		int row9 = table.allocate(9);
		table.get_column(string_id).initialize_data(row9, "test9");
		table.get_column(integer_id).initialize_data(row9, 9);

		assertEquals(2, table.size());
	}
}
