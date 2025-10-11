plugins {
    alias(libs.plugins.kotlin)
}

group = "io.github.e_psi_lon.kore.bindings.smithed"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kore)
    api(projects.core)
    api(projects.smithed)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kore.oop)
    testImplementation(kotlin("test"))
}
var runUnitTests = tasks.register<JavaExec>("runUnitTests") {
    description = "Runs the unit tests."
    group = "verification"

    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "$group.crafter.MainKt"
    shouldRunAfter("test")
}

tasks.test {
    dependsOn(runUnitTests)
}


kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
