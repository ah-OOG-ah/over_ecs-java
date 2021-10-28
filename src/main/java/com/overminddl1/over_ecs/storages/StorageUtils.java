package com.overminddl1.over_ecs.storages;

import java.util.ArrayList;

public class StorageUtils {
	public static <T> T swap_remove(ArrayList<T> collection, int index) {
		if (index == collection.size() - 1) {
			return collection.remove(collection.size() - 1);
		} else {
			return collection.set(index, collection.remove(collection.size() - 1));
		}
	}
}
