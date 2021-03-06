import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
}

group = "me.michieldhadamus"
version = "2.3.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.clojars.org/")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("org.apache.storm:storm-core:2.3.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("org.apache.storm:storm-kafka-client:2.3.0")
    implementation("org.apache.kafka:kafka-clients:3.1.0")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "tutorial.ExclamationTopology"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE


    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}