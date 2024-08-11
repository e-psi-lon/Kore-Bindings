plugins {
    kotlin("jvm")
    alias(libs.plugins.serialization)
}

group = "io.github.e_psi_lon.kore.bindings.bookshelf"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kore.oop)
    api(libs.kore)
    api(project(":bookshelf"))
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
