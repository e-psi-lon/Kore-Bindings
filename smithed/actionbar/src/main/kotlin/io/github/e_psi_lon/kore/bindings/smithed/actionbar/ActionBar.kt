package io.github.e_psi_lon.kore.bindings.smithed.actionbar

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.types.resources.storage
import io.github.e_psi_lon.kore.bindings.smithed.Smithed
import io.github.ayfri.kore.commands.data
import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.features.tags.tag
import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.functions.function
import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.core.SupportedSource
import kotlinx.serialization.encodeToString
import net.benwoodworth.knbt.StringifiedNbt

object ActionBar: Library {
    override val namespace = "${Smithed.namespace}.actionbar"
    override val version: String
        get() = "0.4.1"
    override val source: SupportedSource
        get() = SupportedSource.SMITHED
    override val url: String
        get() = "actionbar"

    fun message() = storage("message", namespace)

    context(Function)
    private fun messageSend() = function("#$namespace", "message")

    context(Function)
    fun send(message: Message.() -> Unit) {
        data(message()) {
            modify("input", StringifiedNbt.encodeToString(Message().apply(message)))
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

val Smithed.actionBar
    get() = ActionBar