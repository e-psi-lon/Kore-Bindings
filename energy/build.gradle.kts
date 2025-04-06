plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.generation)
}

bindings {
    packageName.set("io.github.e_psi_lon.kore.bindings.energy")
}


group = "io.github.e_psi_lon.kore.bindings"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kore)
    api(projects.core)
    api(projects.load)
    implementation(libs.kore.oop)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

