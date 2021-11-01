package com.overminddl1.over_ecs;

import com.overminddl1.over_ecs.bundles.BundleFactory;
import com.overminddl1.over_ecs.bundles.BundleInfo;
import com.overminddl1.over_ecs.components.ComponentInfo;

import java.util.ArrayList;
import java.util.Arrays;

public class Bundles {
	private ArrayList<BundleInfo> bundle_infos;
	//private HashMap<Class, Integer> bundle_ids;

	public Bundles() {
		bundle_infos = new ArrayList<BundleInfo>();
		//bundle_ids = new HashMap<Class, Integer>();
	}

	private static BundleInfo initialize_bundle(String type_name, int[] component_ids, int id, Components components) {
		StorageType[] storage_types = new StorageType[component_ids.length];
		for (int i = 0; i < component_ids.length; i++) {
			int component_id = component_ids[i];
			ComponentInfo component_info = components.getInfo(component_id);
			storage_types[i] = component_info.getDescriptor().getStorageType();
		}
		if (component_ids.length != Arrays.stream(component_ids).distinct().count()) {
			throw new IllegalArgumentException("Bundle " + type_name + " has duplicate components");
		}
		return new BundleInfo(id, component_ids, storage_types);
	}

	public BundleInfo get(int bundle_id) {
		return bundle_infos.get(bundle_id);
	}

//	public Integer get_id(Class type) {
//		return bundle_ids.get(type);
//	}

	public BundleInfo init_info(Components components, Storages storages, BundleFactory bf) {
		Integer id = bf.get_unique_id();
		if (id == null) {
			int[] component_ids = bf.component_ids(components, storages);
			int new_id = this.bundle_infos.size();
			BundleInfo bundle_info = Bundles.initialize_bundle(bf.getClass().getName(), component_ids, new_id, components);
			if (this.bundle_infos.size() > 100000) {
				throw new IllegalArgumentException("Too many bundles, possible Bundle ID leak in: " + bf.getClass().getName());
			}
			bf.set_unique_id(this.bundle_infos.size());
			this.bundle_infos.add(bundle_info);
			return bundle_info;
		} else {
			return this.bundle_infos.get(id);
		}
	}
}
