package io.github.e_psi_lon.kore.bindings.smithed.actionbar

import io.github.ayfri.kore.arguments.chatcomponents.ChatComponents
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    var raw: String? = null,
    var json: ChatComponents? = null,
    var priority: MessagePriority? = null,
    var freeze: Int? = null,
) {
    fun text(text: String) {
        if (json != null) throw IllegalStateException("Cannot set both raw and json.")
        this.raw = text
    }

    fun json(json: ChatComponents) {
        if (raw != null) throw IllegalStateException("Cannot set both raw and json.")
        this.json = json
    }

    fun priority(priority: MessagePriority) {
        this.priority = priority
    }

    fun duration(duration: Int) {
        this.freeze = duration
    }
}