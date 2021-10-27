package com.overminddl1.over_ecs.archetypes;

import java.util.Arrays;

public class ArchetypeIdentity {
	public final int[] table_components;
	public final int[] sparse_set_components;

	public ArchetypeIdentity(int[] table_components, int[] sparse_set_components) {
        this.table_components = table_components;
        this.sparse_set_components = sparse_set_components;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ArchetypeIdentity that = (ArchetypeIdentity) o;
		return Arrays.equals(table_components, that.table_components) && Arrays.equals(sparse_set_components, that.sparse_set_components);
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(table_components);
		result = 31 * result + Arrays.hashCode(sparse_set_components);
		return result;
	}
}
