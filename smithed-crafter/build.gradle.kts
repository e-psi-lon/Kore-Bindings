plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
}

group = "io.github.e_psi_lon.kore"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":smithed"))
    implementation(libs.kore.oop)
    implementation(libs.kotlinx.serialization)
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