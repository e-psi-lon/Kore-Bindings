package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.arguments.components.ComponentsScope
import io.github.ayfri.kore.arguments.components.ComponentsSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val count: Byte? = null,
    val id: String? = null,
    @Serializable(with = ComponentsSerializer::class)
    val tags: ComponentsScope? = null,
    val slot: Byte? = null,
    val itemTags: List<String>? = null
) {
    fun setSlot(slot: Int) = Item(count, id, tags, slot.toByte(), itemTags)

    companion object {
        internal val NULL_ITEM = Item()
    }
}