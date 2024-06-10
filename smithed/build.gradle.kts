plugins {
    kotlin("jvm") version "2.0.0"
}

group = "io.github.e_psi_lon.kore.bindings"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kore)
    parent?.subprojects?.find { it.name == "core" }?.let { api(it) }
    implementation(libs.kotlinx.serialization.json)
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

