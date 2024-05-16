plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
}

group = "io.github.e_psi_lon.kore"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kore)
    api(libs.kore.oop)
    implementation(libs.kotlinx.serialization)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}