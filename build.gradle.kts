// build.gradle.kts (Project: SoukEtudiant)
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("io.realm.kotlin") version "1.0.0" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}