package io.github.e_psi_lon.kore.bindings

import io.github.e_psi_lon.kore.bindings.smithed.crafter.smithedRecipeTest
import io.github.e_psi_lon.kore.bindings.utils.testDataPack

fun main() {
    testDataPack("Kore-Bindings-test") {
        smithedRecipeTest()
    }.apply {
        generate()
    }
}