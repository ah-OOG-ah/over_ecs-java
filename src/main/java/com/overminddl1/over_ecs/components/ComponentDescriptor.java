package com.overminddl1.over_ecs.components;

import com.overminddl1.over_ecs.Component;
import com.overminddl1.over_ecs.ComponentRefinement;
import com.overminddl1.over_ecs.StorageType;

public class ComponentDescriptor {
	private final String name;
	private final StorageType storage_type;
	private final boolean is_multi_thread_safe;
	private final Class<? extends Component> component_class;
	// Blah stupid java, no chance of packing...

	public ComponentDescriptor(Class<? extends Component> component_class) {
		this.name = component_class.getName();
		@SuppressWarnings("unchecked")
		ComponentRefinement annot = (ComponentRefinement) component_class.getAnnotation(ComponentRefinement.class);
		if (annot == null || annot.storageType().equalsIgnoreCase("table")) {
			this.storage_type = StorageType.Table;
		} else if (annot.storageType().equalsIgnoreCase("sparseset")) {
			this.storage_type = StorageType.SparseSet;
		} else {
			throw new RuntimeException("Invalid storage type: " + annot.storageType());
		}
		this.is_multi_thread_safe = annot == null || annot.isMultiThreadSafe();
		this.component_class = component_class;
	}

	public ComponentDescriptor(Class<? extends Component> component_class, StorageType storage_type, boolean is_split, boolean is_multi_thread_safe) {
		this.name = component_class.getName();
		this.storage_type = storage_type;
		this.is_multi_thread_safe = is_multi_thread_safe;
		this.component_class = component_class;
	}

	public String getName() {
		return this.name;
	}

	public StorageType getStorageType() {
		return this.storage_type;
	}

	public boolean isMultiThreadSafe() {
		return this.is_multi_thread_safe;
	}

	public Class<? extends Component> getCls() {
		return this.component_class;
	}
}
