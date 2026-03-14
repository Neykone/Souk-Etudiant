// build.gradle.kts (Project: SoukEtudiant)
plugins {
    id("com.android.application") version "8.2.0" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")
        classpath("io.realm:realm-gradle-plugin:10.15.1")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}