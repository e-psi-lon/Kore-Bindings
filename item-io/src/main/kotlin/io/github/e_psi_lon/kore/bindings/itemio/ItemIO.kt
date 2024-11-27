package io.github.e_psi_lon.kore.bindings.itemio

import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.functions.Function
import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.core.SupportedSource

object ItemIO: Library {
    override val namespace: String
        get() = "itemio"
    override val version: String
        get() = "1.3.1"
    override val source: SupportedSource
        get() = SupportedSource.GITHUB
    override val location: String
        get() = "edayot/ItemIO"

    val container
        get() = Container

    context(Function)
    fun filterV2() = function(namespace, "calls/filter_v2", true)

    context(Function)
    fun onPlace() = function(namespace, "calls/container/init", true)

    context(Function)
    fun onDestroy() = function(namespace, "calls/container/destroy", true)

}