package io.github.e_psi_lon.kore.bindings.bookshelf

import io.github.ayfri.kore.arguments.types.resources.storage

object Bookshelf {
    val namespace: String = "bs"

    fun outStorage() = storage("out", namespace)

    fun outScoreboard() = "${namespace}:out"

    fun inScoreboard() = "${namespace}:in"
}
