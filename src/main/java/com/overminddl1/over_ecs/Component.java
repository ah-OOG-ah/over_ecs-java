package com.overminddl1.over_ecs;

/**
 * 1/3 of an Enitity Component System. Sublclass 'em and make 'em do what you want,
 * then slap those bad boys onto Entities
 */
public interface Component {
	Component componentClone();

	void componentReset();
}
