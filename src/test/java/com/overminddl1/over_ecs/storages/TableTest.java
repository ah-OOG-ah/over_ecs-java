package com.overminddl1.over_ecs.storages;

import com.overminddl1.over_ecs.Components;
import com.overminddl1.over_ecs.Storages;
import com.overminddl1.over_ecs.test.ComponentsTestData.TestingI;
import com.overminddl1.over_ecs.test.ComponentsTestData.TestingS;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableTest {

	@Test
	void getEntities() {
		Components components = new Components();
		Storages storages = new Storages();
		int string_id = components.init_component(storages, TestingS.class);
		int integer_id = components.init_component(storages, TestingI.class);
		assertEquals(0, string_id);
		assertEquals(1, integer_id);
		int[] columns = {integer_id, string_id};
		Table table = new Table(0, columns.length);
		for (int i = 0; i < columns.length; i++) {
			table.add_column(components.getInfo(columns[i]));
		}

		int row1 = table.allocate(1);
		table.get_column(string_id).initialize_data(row1, new TestingS("test1"));
		table.get_column(integer_id).initialize_data(row1, new TestingI(1));

		int row9 = table.allocate(9);
		table.get_column(string_id).initialize_data(row9, new TestingS("test9"));
		table.get_column(integer_id).initialize_data(row9, new TestingI(9));

		assertEquals(2, table.size());
	}
}
