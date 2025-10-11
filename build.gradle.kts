plugins {
    alias(libs.plugins.kotlin)
}

repositories {
    mavenCentral()
}

// Add a task that runs all "generateBindings" tasks. Notice that some subprojects don't have this task.
tasks.register("generateAllBindings") {
    group = "build"
    description = "Runs all generateBindings tasks."

    subprojects.forEach { subproject ->
        val generateBindingsTask = subproject.tasks.findByName("generateBindings")
        if (generateBindingsTask != null) {
            dependsOn(generateBindingsTask)
        }
    }
}