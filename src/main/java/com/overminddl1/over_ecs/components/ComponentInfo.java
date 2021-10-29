package com.overminddl1.over_ecs.components;

import com.overminddl1.over_ecs.StorageType;

public class ComponentInfo {
	private int id;
	private ComponentDescriptor descriptor;

	public ComponentInfo(int id, ComponentDescriptor descriptor) {
		this.id = id;
		this.descriptor = descriptor;
	}

	public int getId() {
		return id;
	}

	public ComponentDescriptor getDescriptor() {
		return this.descriptor;
	}

	public String getName() {
		return this.descriptor.getName();
	}

	public Class getCls() {
		return this.descriptor.getCls();
	}

	public StorageType getStorageType() {
		return this.descriptor.getStorageType();
	}
}
