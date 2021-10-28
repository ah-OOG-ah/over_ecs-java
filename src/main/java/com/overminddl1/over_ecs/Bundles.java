package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.bundles.BundleFactory;
import com.overminddl1.over_ecs.bundles.BundleInfo;
import com.overminddl1.over_ecs.components.ComponentInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class Bundles {
	private ArrayList<BundleInfo> bundle_infos;
	private HashMap<Class, Integer> bundle_ids;

	public Bundles() {
		bundle_infos = new ArrayList<BundleInfo>();
		bundle_ids = new HashMap<Class, Integer>();
	}

	private static BundleInfo initialize_bundle(String type_name, int[] component_ids, int id, Components components) {
		ArrayList<StorageType> storage_types = new ArrayList<StorageType>();
		for (int i = 0; i < component_ids.length; i++) {
			int component_id = component_ids[i];
			ComponentInfo component_info = components.getInfo(component_id);
			storage_types.add(component_info.getDescriptor().getStorageType());
		}
		if (storage_types.size() != storage_types.stream().distinct().count()) {
			throw new IllegalArgumentException("Bundle " + type_name + " has duplicate storage types");
		}
		return new BundleInfo(id, component_ids, storage_types);
	}

	public BundleInfo get(int bundle_id) {
		return bundle_infos.get(bundle_id);
	}

	public Integer get_id(Class type) {
		return bundle_ids.get(type);
	}

	public BundleInfo init_info(Components components, Storages storages, BundleFactory bf) {
		Class bundle_class = bf.get_bundle_class();
		Integer id = this.bundle_ids.get(bundle_class);
		if (id == null) {
			int[] component_ids = bf.component_ids(components, storages);
			int new_id = this.bundle_infos.size();
			BundleInfo bundle_info = Bundles.initialize_bundle(bundle_class.getName(), component_ids, new_id, components);
			this.bundle_infos.add(bundle_info);
			return bundle_info;
		} else {
			return this.bundle_infos.get(id);
		}
	}
}
