package io.github.e_psi_lon.kore.bindings.smithed.actionbar

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.types.resources.storage
import io.github.ayfri.kore.commands.data
import io.github.ayfri.kore.features.tags.tag
import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.functions.function
import kotlinx.serialization.encodeToString
import net.benwoodworth.knbt.StringifiedNbt

fun SmithedActionbar.messageStorage() = storage("message", namespace)

context(Function)
fun send(message: Message.() -> Unit) {
    data(SmithedActionbar.messageStorage()) {
        modify("input", StringifiedNbt.encodeToString(Message().apply(message)))
        SmithedActionbar.message()
    }

}

context(DataPack)
fun SmithedActionbar.onClickLockedContainer(namespace: String, onEvent: Function.() -> Unit) {
    function("generated_${onEvent.hashCode()}", namespace, "generated_scopes", onEvent)
    val event = SmithedActionbar.Event.Player.onClickLockedContainer
    tag(event.name, "function", event.namespace) {
        add("${namespace}:generated_scopes/generated_${onEvent.hashCode()}")
    }
}
