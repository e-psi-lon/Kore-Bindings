package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.arguments.types.resources.LootTableArgument
import io.github.ayfri.kore.functions.Function
import io.github.e_psi_lon.kore.bindings.core.CustomItemArgument
import net.benwoodworth.knbt.StringifiedNbt

interface Recipe {
    var result: LootTableArgument?
    context(Function)
    fun build()
}