package com.overminddl1.over_ecs.query;

@FunctionalInterface
public interface ParPackedConsumer<T> {
	/**
	 * Performs this operation on the given arguments.
	 *
	 * @param t     The packed data
	 * @param start The start of the range this should be sliced at
	 * @param end   The end of the range this should be sliced at (exclusive)
	 */
	void accept(T t, int start, int end);
}
