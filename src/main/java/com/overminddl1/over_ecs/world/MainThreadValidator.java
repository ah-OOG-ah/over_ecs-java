package com.overminddl1.over_ecs.world;

public class MainThreadValidator {
	long main_thread_id;

	public MainThreadValidator() {
		this.main_thread_id = Thread.currentThread().threadId();
	}

	public boolean is_main_thread() {
		return Thread.currentThread().threadId() == main_thread_id;
	}
}
