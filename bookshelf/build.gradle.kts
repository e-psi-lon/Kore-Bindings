plugins {
    kotlin("jvm")
}

group = "io.github.e_psi_lon.kore.bindings"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kore)
    api(project(":core"))
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

