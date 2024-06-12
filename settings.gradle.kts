rootProject.name = "Kore-Bindings"

pluginManagement {
    repositories {
        mavenCentral()
    }
}


include(":core")
include("smithed")
include("bookshelf")
// include(":energy")
// include(":item-io")
// include(":furnace-nbt-recipes")
// include(":smart-ore-generation")
