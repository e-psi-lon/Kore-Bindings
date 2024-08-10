include(":biome")
include(":bitwise")
// include(":block")
// include(":color")
// include(":health")
// include(":hitbox")
// include(":id")
// include(":link")
// include(":move")
// include(":math")
// include(":position")
// include(":raycast")
// include(":schedule")
// include(":sidebar")
// include(":time")
// include(":tree")
// include(":vector")
// include(":view")
// include(":weather")
// include(":xp")

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