plugins {
    alias(libs.plugins.kotlin)
}

group = "io.github.e_psi_lon.kore.bindings.smithed"
version = "1.0"

repositories {
    mavenCentral()
}


dependencies {
    implementation(libs.kotlinx.serialization.json)
    api(projects.smithed)
    implementation(libs.kore.oop)
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
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}
