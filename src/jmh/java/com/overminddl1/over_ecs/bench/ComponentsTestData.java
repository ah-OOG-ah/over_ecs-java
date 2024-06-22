package com.overminddl1.over_ecs.bench;

import com.overminddl1.over_ecs.Component;

public class ComponentsTestData {
	public static class TestingI implements Component {
		public int value;

		public TestingI(int value) {
			this.value = value;
		}
	}

	public static class TestingS implements Component {
		public String value;

		public TestingS(String value) {
			this.value = value;
		}
	}
}
