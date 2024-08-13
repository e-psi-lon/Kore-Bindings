plugins {
    kotlin("jvm")
}

group = "io.github.e_psi_lon.kore.bindings"
version = "unspecified"

repositories {
    mavenCentral()
}

fun DependencyHandlerScope.searchFullName(project: Project): String {
    project.parent?.let {
        return searchFullName(it) + ":" + project.name
    } ?: run {
        return project.name
    }
}

dependencies {
    implementation(libs.kore)
    implementation(libs.kore.oop)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":core"))
    implementation(project(":smithed"))
    implementation(project(":smithed:crafter"))
    implementation(project(":smithed:actionbar"))
    implementation(project(":smithed:custom-block"))
    implementation(project(":smithed:prevent-aggression"))
    implementation(project(":bookshelf"))
    implementation(project(":bookshelf:biome"))
    implementation(project(":bookshelf:bitwise"))
    implementation(project(":bookshelf:block"))
    implementation(project(":bookshelf:color"))
    implementation(project(":bookshelf:health"))
    implementation(project(":bookshelf:hitbox"))
    implementation(project(":bookshelf:id"))

}

var runUnitTests = tasks.register<JavaExec>("runUnitTests") {
    description = "Runs the unit tests."
    group = "verification"

    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "$group.MainKt"
    shouldRunAfter("test")
}

tasks.test {
    dependsOn(runUnitTests)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}