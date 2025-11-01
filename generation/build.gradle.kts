plugins {
    `kotlin-dsl`
    `maven-publish`
    application
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
}


group = "io.github.e_psi_lon.kore.bindings"
version = "0.8.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.poet)
    implementation(libs.kore)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.clikt)
    
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

gradlePlugin {
    plugins {
        register("koreBindingsGenerator") {
            id = "io.github.e_psi_lon.kore.bindings.generation"
            implementationClass = "io.github.e_psi_lon.kore.bindings.generation.BindingGradlePlugin"
        }
    }
}

publishing {
    
}

tasks.shadowJar {
    archiveBaseName.set("kore-bindings-generator")
    // archiveClassifier.set("")
    // archiveVersion.set("")
    manifest {
        attributes["Main-Class"] = "io.github.e_psi_lon.kore.bindings.generation.CliKt"
    }
}

application {
    mainClass.set("io.github.e_psi_lon.kore.bindings.generation.CliKt")
}
