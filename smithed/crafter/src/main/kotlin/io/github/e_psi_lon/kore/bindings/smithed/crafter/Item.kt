package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.arguments.components.ComponentsScope
import io.github.ayfri.kore.arguments.components.ComponentsSerializer
import io.github.ayfri.kore.arguments.types.resources.ItemArgument
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    @SerialName("Count")
    val count: Byte? = null,
    val id: String? = null,
    @Serializable(with = ComponentsSerializer::class)
    val tags: ComponentsScope? = null,
    @SerialName("Slot")
    val slot: Byte? = null,
    @SerialName("item_tag")
    val itemTags: List<String>? = null
) {
    constructor(count: Byte? = null, id: ItemArgument, tags: ComponentsScope? = null, slot: Byte? = null, itemTags: List<String>? = null) : this(count, id.asString(), tags, slot, itemTags)
    fun setSlot(slot: Int) = Item(count, id, tags, slot.toByte(), itemTags)

    companion object {
        internal val NULL_ITEM = Item()
    }
}