package io.github.e_psi_lon.kore.bindings.smithed.actionbar

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.types.resources.storage
import io.github.e_psi_lon.kore.bindings.smithed.Smithed
import io.github.ayfri.kore.commands.data
import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.features.tags.tag
import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.functions.function
import kotlinx.serialization.encodeToString
import net.benwoodworth.knbt.StringifiedNbt

object ActionBar {
    val namespace = "${Smithed.namespace}.actionbar"

    fun message() = storage("message", namespace)

    context(Function)
    private fun messageSend() = function("#$namespace", "message")

    context(Function)
    fun send(message: Message) {
        data(message()) {
            modify("input", StringifiedNbt.encodeToString(message))
            messageSend()
        }

    }

    context(DataPack)
    fun onClickLockedContainer(namespace: String, onEvent: Function.() -> Unit) {
        function("generated_${onEvent.hashCode()}", namespace, "generated_scopes", onEvent)
        tag("event/player/on_click_locked_container", "function", "smithed.actionbar") {
            add("${namespace}:generated_scopes/generated_${onEvent.hashCode()}")
        }
    }
}