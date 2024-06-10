package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.e_psi_lon.kore.bindings.utils.testDataPack

fun main() {
    testDataPack("smithed.crafter") {
        smithedRecipeTest()
    }.apply {
        generate()
    }
}