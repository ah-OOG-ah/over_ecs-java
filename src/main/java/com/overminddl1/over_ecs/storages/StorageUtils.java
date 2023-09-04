package com.overminddl1.over_ecs.storages;

import java.util.ArrayList;

/**
 * Does one thing, and one thing only: provide a swap_remove method for an ArrayList
 */
public class StorageUtils {

	/**
	 * If index is the last item, remove it and return the element.
	 * Else, swap it with the last item and return it.
	 * Basically a significantly more performant remove that as a side effect shuffles your list.
	 */
	public static <T> T swap_remove(ArrayList<T> collection, int index) {
		if (index == collection.size() - 1) {
			return collection.remove(collection.size() - 1);
		} else {
			return collection.set(index, collection.remove(collection.size() - 1));
		}
	}
}
