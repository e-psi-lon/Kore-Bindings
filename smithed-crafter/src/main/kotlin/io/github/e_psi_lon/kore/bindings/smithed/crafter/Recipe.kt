package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.functions.Function

interface Recipe {
    val dataPack: DataPack
    var result: Command?
    context(Function)
    fun build()
}