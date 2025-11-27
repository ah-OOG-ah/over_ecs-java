plugins {
    java
    id("me.champeau.jmh") version "0.7.3"
    `maven-publish`
}

group = "com.overminddl1.over_ecs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

gradle.projectsEvaluated {
    tasks.withType(JavaCompile::class) {
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    jmh("org.openjdk.jmh:jmh-core:1.33")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.33")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    minHeapSize = "512m"
    maxHeapSize = "4096M"
}
