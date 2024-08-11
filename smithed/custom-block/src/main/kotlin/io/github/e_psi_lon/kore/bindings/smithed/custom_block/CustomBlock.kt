package io.github.e_psi_lon.kore.bindings.smithed.custom_block

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.types.resources.storage
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.commands.execute.execute
import io.github.ayfri.kore.features.tags.tag
import io.github.ayfri.kore.functions.function
import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.core.SupportedSource
import io.github.e_psi_lon.kore.bindings.smithed.Smithed

object CustomBlock : Library {
    override val namespace: String = "${Smithed.namespace}.custom_block"
    override val version: String
        get() = "0.2.0"
    override val source: SupportedSource
        get() = SupportedSource.SMITHED
    override val url: String
        get() = "custom-block"

    fun main() = storage("place", namespace)

    context(DataPack)
    fun onPlace(namespace: String, vararg blockAndFunction: Pair<String, Command>) {
        function("on_place", namespace) {
            blockAndFunction.forEach { (block, function) ->
                execute {
                    ifCondition {
                        data(main(), "{blockApi:{id:\"$block\"}}")
                    }
                    run {
                        function
                    }
                }
            }
        }
        tag("event/on_place", "functions", namespace) {
            add("$namespace:on_place")
        }
    }
}

val Smithed.customBlock
    get() = CustomBlock