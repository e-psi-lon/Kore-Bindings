plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
}

group = "io.github.e_psi_lon.kore.bindings"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kore)
    api(projects.core)
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

