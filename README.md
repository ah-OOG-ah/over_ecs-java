# OverECS

This is an ECS framework built on the ideas of many that have come before with an API based on the well known and loved bevy_ecs for another language.

## Disclaimer

This is a work in progress and was primarily built to be a testing framework to compare performance between this 'usual' ECS style and the other popular JVM ECS frameworks.  If you are looking for a well performing ECS framework for your game or other project, I would recommend looking at artemis-odb, however even in the current state this library already outperforms all others I've tested and has a set of features that are not present in most other java ECS frameworks, especially in the area of query filtering and multithreading.

Although this is entirely usable at this point the API might be slightly in flux as I continue to work on it until it settles down.  The backend is 'complete' for the current features (though more are planned) however the user API is still only partially implemented.  As this was designed to be a project for my own  use it was sufficient for me, however I do plan to complete it and if you wish to help prod that along then please message me and/or PRs are welcome.

## License

This project is licensed under the LGPLv3 license.  This is a copyleft license that allows you to use this library in your own projects, even commercial ones, however if you modify this library and distribute it you must also distribute the source code of your modifications.  This is to ensure that the library remains open source and free for all to use.

## Features

* Efficient array aligned storage of components, though due to JVM limitations this is not as efficient as it could be due to the lack of value types.
* Efficient multithreading support with a lock free architecture.
* Efficient query filtering with a simple and powerful query language that is user extensible.
* Efficient entity creation and deletion.
* Efficient component creation and deletion.
* Extremely efficient iteration over entities and components via queries.
* Multiple query styles for different use cases whether for simple ease of use or extremely efficient massive batch updates, all are still fast.

## Planned Features

* Workset dependency graph for efficient multithreading and ordered system running.
* Closer adherence to the recent bevy_ecs featureset.

## Usage

You must first create the storage registry where everything is stored and processing is handled.  This is the main entry point for the library.

```java
World world = new World();
```

Register the component types you will be using.  These can be done at runtime later for dynamically allocated things, they just need to be registered before use for runtime performance reasons.

```java
world.init_component(Health.class);
world.init_component(HealthDamage.class);
world.init_component(Transform.class);
```

### Components

A component is a pure data storage container.  Do *not* put any logic in a component, only data.  This is a core tenet of ECS.  It is fine to put in getters/setters and other such simple things, but do *not* put logic in a component.  Optimally it would just be a java record or data class.

A component must implement `Component` in the current design, it is purely used as a marker implementation, it might be removed in an update as I'm still torn on whether the marker interface should continue to exist or if it should have something added to it...

```java
public static final class Health implements Component {
    public int basic_max_health;
    public int max_health_bonus; // Maybe better to have a list of bonuses?

    public Health(int basic_max_health) {
        this.basic_max_health = basic_max_health;
    }
    public Health() {
        this(1);
    }
}
```

Currently you are able to define a component to use either a table storage (default) or a sparseset storage.

* Table storage is the most efficient for bulk iteration, it is a simple type array of arrays.  It is less efficient when constantly adding/removing a given component from an entity rapidly, but is entirely fine if done rarely (more than every few frames) or if the overall amount of components on an entity is small.  In general opt for table storage unless you know you will be adding/removing the component rapidly.
* SparseSet storage is the most efficient for adding/removing components rapidly, it is a sparse array of arrays.  It is perfectly efficient (array iteration speed) if only iterating this component but there's an extra lookup if combined with others (one extra lookup per sparse stored component and one extra if any amount of table stored components are queried).  In general opt for table storage unless you know you will be adding/removing the component rapidly and are using it as a marker to figure out if you want to iterate it or not.  Components where their existence is only tested in the query but it's not actually looked up are even faster as it's a lookup in the secondary index only without hitting the main storage array.

Regardless of which style is used it is still faster than dynamic dispatch and the overhead of the ECS system itself is minimal compared to the actual processing of the data.  The main benefit of ECS is still the aligned and minimal memory lookup access that allows much faster iteration of many things and random capabilities otherwise.

If you were to specify a component to use sparseset storage you'd add the appropriate attribute as below:

```java
@ComponentRefinement(storageType = "SparseSet")
public static final class SomeTempTag implements Component {
    ...
}
```
The `ComponentRefinement` also has a `isMultiThreadSafe` argument that defaults to `true`, but it can be set to false if you want to ensure the system will not automatically multi-thread access to this component when possible.

### Entities

An entity in its most basic is just an integer (a `long` java type), a unique ID of a linked set of components, easy to store, very temporary for the current `World`, if you wish to persist an ID then make a UUID component or so and attach it and look up using it via a secondary index to build the game mappings across serialization and deserialization.

To create an entity you 'can' just simply call:

```java
Entity entity = world.spawn();
```

And this will return an `Entity`, which is just a wrapper around the entity integer with various helper functions, you can then add a component to it like:

```java
entity.insert(new Health(20));
```

However doing this multiple times means moving and removing the entity backend storage multiple times in full, which is not efficient, rather you should create a bundle of all the components you wish to add and then add them all at once so it is placed into perfect memory position directly:

```java
// Create a bundle of components to create an entity from, can be arbitrarily large:
BundleN bundle = new BundleN(new Health(20), new Transform());
// Create a new entity in perfect memory position directly:
Entity entity = entity.insert_bundle(bundle);
```

(oh if only java had proper tuples...)

There are similar get/set to get and updated an existing component as well as a remove_bundle.  These exist on world as well where you pass in an entity `long` id.

However, you should not be calling get/set directly in the game code as that is not efficient, 2 lookups needed each.  Rather you should use queries to get the data you need in a single lookup for all matching components across all entities.

### Queries

A query is a way to filter entities based on the components they have.  You can create a query such as:

```java
QueryState query = world.query(WorldQuery.builder()
        .read_entities()
        .read_component(HealthDamage.class)
        .write_component(Transform.class));
```

This will create a new query object (cached in world) that you should hold on to and use for access to within the system.  This specific query performs 3 lookup, one for the entity list, one for the Health component in read mode, and one for the Transform component in read/write mode.  You should not write to a component set you are reading from (and java as a language is horrible enough to not have a good way to enforce this...) or you can get things out of sync and cause issues (mainly missing updates).

To use this query it has a few methods depending on what you want to do:

```java
// The most common usage, iterate over all matching sets:
query.for_each(world, (Object[] components) -> {
    // Again java's lack of proper tuples are horrible...
    // Am planning further api changes around this though (likely data holder class)
    // But first get the components:
    var e = (Long) components[0];
    var hp = (HealthDamage) components[1];
    var i = (Mut<Transform>) components[2];
    // Then do something with them:
    if (hp.damage > 0) {
        // Health damaged, move the entity to 0, 0, 0 I guess, every update:
        i.set().set_position(0, 0, 0);
    }
});
```

The point of the difference between read and read/write is that the `Mut` wrapper will propagate update notifications to the query filters that watch for them, so you can operate only on things that had something change in them.  This is a very powerful feature that allows for very efficient processing of only the things that need to be processed.

Queries have other calls such as `.single(world)` if you know there will only be a single one (like a special single use tag component) in the world, like the player in singleplayer (throws exception if more th an 1), in addition to multi-threaded core distributed version of for_each that will run the query in parallel across all available cores, etc...

### Systems

Right now systems are entirely manually done via manual Query creation and iteration.  This is a bit more verbose than the bevy_ecs style.  I plan to add a more bevy_ecs style system in the future but for now this works fine.

Likewise the graph system and dispatcher is not yet built, so you must manually order your systems and run them in the correct order.  This is more verbose but also allows for more control over the system order and dependencies at least and it's not hard to do until the system gets built anyway.


### Similarities to SQL

In SQL parlance a component is a 'Column' of data, where an entity is a primary key to a row of data.  Table storage (default) for components puts it all in one 'table' in SQL parlance, and SparseSet is like a foreign linked table.  Queries are of course still Queries.
