include(":crafter")
include(":actionbar")
// include(":custom-blocks")
// include(":prevent-aggression")


pluginManagement {
    repositories {
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}