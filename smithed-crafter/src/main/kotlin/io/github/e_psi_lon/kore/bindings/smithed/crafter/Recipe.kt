package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.functions.Function
import io.github.e_psi_lon.kore.bindings.core.CustomItemArgument
import net.benwoodworth.knbt.StringifiedNbt

interface Recipe {
    val snbt: StringifiedNbt
        get() = StringifiedNbt {  }
    var result: CustomItemArgument?
    context(Function)
    fun build()
}