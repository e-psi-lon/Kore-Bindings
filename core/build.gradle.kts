plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
}

group = "io.github.e_psi_lon.kore"
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
}