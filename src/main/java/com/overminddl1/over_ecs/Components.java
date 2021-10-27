package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.components.ComponentDescriptor;
import com.overminddl1.over_ecs.components.ComponentInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class Components {
	private final ArrayList<ComponentInfo> components;
	private final HashMap<Class, Integer> indices;

	public Components() {
		components = new ArrayList<ComponentInfo>();
		indices = new HashMap<Class, Integer>();
	}

	public <T> int init_component(Storages storages, Class<T> component_class) {
		Integer index = this.indices.get(component_class);
		if(index == null) {
			this.indices.put(component_class, this.components.size());
			index = this.components.size();
			ComponentDescriptor descriptor = new ComponentDescriptor(component_class);
			ComponentInfo info = new ComponentInfo(index, descriptor);
			if(descriptor.getStorageType() == StorageType.SparseSet) {
				storages.sparse_sets.get_or_insert(info);
			}
			this.components.add(info);
		}
		return index;
	}

	public int size() {
		return this.components.size();
	}

	public ComponentInfo getInfo(int id) {
		if(id >= this.components.size()) {
			return null;
		}
		return this.components.get(id);
	}

	public Integer getId(Class<Object> component_class) {
		return this.indices.get(component_class);
	}
}
