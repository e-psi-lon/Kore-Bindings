plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.generation)
}

group = "io.github.e_psi_lon.kore.bindings.smithed"
version = "1.0"

repositories {
    mavenCentral()
}

bindings {
    packageName.set("io.github.e_psi_lon.kore.bindings.smithed.crafter")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    api(projects.smithed)
    implementation(projects.smithed.customBlock)
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
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
