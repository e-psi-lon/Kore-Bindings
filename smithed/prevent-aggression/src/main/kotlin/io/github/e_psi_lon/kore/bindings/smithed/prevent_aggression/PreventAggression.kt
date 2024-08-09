package io.github.e_psi_lon.kore.bindings.smithed.prevent_aggression

import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.core.SupportedSource
import io.github.e_psi_lon.kore.bindings.smithed.Smithed

object PreventAggression: Library {
    override val namespace: String = "${Smithed.namespace}.prevent_aggression"
    override val version: String
        get() = "0.2.0"
    override val source: SupportedSource
        get() = SupportedSource.SMITHED
    override val url: String
        get() = "prevent-aggression"

}

val Smithed.preventAggression
    get() = PreventAggression