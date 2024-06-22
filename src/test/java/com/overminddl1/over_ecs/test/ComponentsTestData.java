package com.overminddl1.over_ecs.test;

import com.overminddl1.over_ecs.Component;
import com.overminddl1.over_ecs.ComponentRefinement;

public class ComponentsTestData {
	public static class TestingI implements Component {
		public int value;

		public TestingI(int value) {
			this.value = value;
		}
		public TestingI() {
			this(-1);
		}
	}

	public static class TestingS implements Component {
		public String value;

		public TestingS(String value) {
			this.value = value;
		}
		public TestingS() {
			this("<undefined>");
		}
	}

	public static class TestingSplit implements Component {
		public String string;
		public int integer;

		public TestingSplit(String string, int integer) {
            this.string = string;
            this.integer = integer;
        }
		public TestingSplit() {
			this("<undefined>", -1);
		}
	}

	@ComponentRefinement(storageType = "sparseset")
	public static class TestingFSparse implements Component {
		public float value;

		public TestingFSparse(float value) {
			this.value = value;
		}
		public TestingFSparse() {
			this(-0.0F);
		}
	}
}
