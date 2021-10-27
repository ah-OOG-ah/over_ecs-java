package com.overminddl1.over_ecs;

// Java can't wrap a 'primitive' type, so static accesses on the "long' entity type...
// Again, this would be so much easier in kotlin or scala...
public final class Entity {

	public static long init(int generation, int id) {
		return ((long) generation << 32) | id;
	}

	public static int id(long entity) {
		return (int) (entity & 0xFFFFFFFF);
	}

	public static int generation(long entity) {
		return (int) ((entity >> 32) & 0xFFFFFFFF);
	}
}
