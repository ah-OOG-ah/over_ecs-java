package com.overminddl1.over_ecs.components;

import com.overminddl1.over_ecs.Component;
import com.overminddl1.over_ecs.StorageType;

public class ComponentDescriptor {
	private final String name;
	private final StorageType storage_type;
	private final boolean is_multi_thread_safe;
	private final Class component_class;
	// Blah stupid java, no chance of packing...

	public ComponentDescriptor(Class component_class) {
		this.name = component_class.getName();
		@SuppressWarnings("unchecked")
		Component annot = (Component) component_class.getAnnotation(Component.class);
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

	public ComponentDescriptor(Class<Object> component_class, StorageType storage_type, boolean is_multi_thread_safe) {
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

	public Class getCls() {
		return this.component_class;
	}
}
