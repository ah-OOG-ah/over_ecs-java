package com.overminddl1.over_ecs;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface ComponentRefinement {
	// Valid is only "Table" or "SparseSet" at this point
	String storageType() default "Table";

	boolean isMultiThreadSafe() default true;
}
