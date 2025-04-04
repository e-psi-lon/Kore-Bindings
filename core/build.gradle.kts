plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.1.20"
}

group = "io.github.e_psi_lon.kore.bindings"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kore)
    api(libs.kore.oop)
    implementation(libs.kotlinx.serialization.json)
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