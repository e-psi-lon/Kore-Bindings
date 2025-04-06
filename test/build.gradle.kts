plugins {
    alias(libs.plugins.kotlin)
}

group = "io.github.e_psi_lon.kore.bindings"
version = "unspecified"

repositories {
    mavenCentral()
}

fun DependencyHandlerScope.searchFullName(project: Project): String {
    project.parent?.let {
        return searchFullName(it) + ":" + project.name
    } ?: run {
        return project.name
    }
}

dependencies {
    implementation(libs.kore)
    implementation(libs.kore.oop)
    implementation(libs.kotlinx.serialization.json)
    implementation(projects.core)
    implementation(projects.smithed)
    implementation(projects.smithed.crafter)
    implementation(projects.smithed.actionbar)
    implementation(projects.smithed.customBlock)
    implementation(projects.smithed.preventAggression)
    implementation(projects.bookshelf)
    implementation(projects.bookshelf.biome)
    implementation(projects.bookshelf.bitwise)
    implementation(projects.bookshelf.block)
    implementation(projects.bookshelf.color)
    implementation(projects.bookshelf.health)
    implementation(projects.bookshelf.hitbox)
    implementation(projects.bookshelf.id)
    // implementation(projects.bookshelf.link)
    // implementation(projects.bookshelf.move)
    // implementation(projects.bookshelf.math)
    // implementation(projects.bookshelf.position)
    // implementation(projects.bookshelf.raycast)
    // implementation(projects.bookshelf.schedule)
    // implementation(projects.bookshelf.sidebar)
    // implementation(projects.bookshelf.time)
    // implementation(projects.bookshelf.tree)
    // implementation(projects.bookshelf.vector)
    // implementation(projects.bookshelf.view)
    // implementation(projects.bookshelf.weather)
    // implementation(projects.bookshelf.xp)
    implementation(projects.energy)
    implementation(projects.itemIo)
    // implementation(projects.furnaceNbtRecipes)
    // implementation(projects.smartOreGeneration)
    // implementation(projects.load)

}

var runUnitTests = tasks.register<JavaExec>("runUnitTests") {
    description = "Runs the unit tests."
    group = "verification"

    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "$group.MainKt"
    shouldRunAfter("test")
}

tasks.test {
    dependsOn(runUnitTests)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}