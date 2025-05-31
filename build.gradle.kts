group = "filipesantoss"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.21"
}

kotlin {
    jvmToolchain(23)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
}

tasks.jar.configure {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    configurations["compileClasspath"].forEach { file ->
        from(zipTree(file.absoluteFile))
    }

    manifest {
        attributes(mapOf("Main-Class" to "filipesantoss.vortad.MainKt"))
    }
}
