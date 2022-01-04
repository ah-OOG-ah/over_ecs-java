package com.overminddl1.over_ecs.bench;

import com.overminddl1.over_ecs.Component;

public class ComponentsTestData {
	public static class TestingI implements Component {
		public int value;

		public TestingI(int value) {
			this.value = value;
		}

		@Override
		public Component componentClone() {
			return new ComponentsTestData.TestingI(this.value);
		}

		@Override
		public void componentReset() {
			this.value = -1;
		}
	}

	public static class TestingS implements Component {
		public String value;

		public TestingS(String value) {
			this.value = value;
		}

		@Override
		public Component componentClone() {
			return new ComponentsTestData.TestingS(this.value);
		}

		@Override
		public void componentReset() {
			this.value = "<undefined>";
		}
	}
}
