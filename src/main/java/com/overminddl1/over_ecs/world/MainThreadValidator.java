package com.overminddl1.over_ecs.world;

public class MainThreadValidator {
	long main_thread_id;

	public MainThreadValidator() {
		this.main_thread_id = Thread.currentThread().getId();
	}

	public boolean is_main_thread() {
		return Thread.currentThread().getId() == main_thread_id;
	}
}
