package io.github.e_psi_lon.kore.bindings.generation.data


/**
 * Represents a storage reference found in function files
 */
data class Storage(
    val namespace: String,  // "bs" or "bs.math"
    override val name: String,  // "const", "in", "data"
    val sourceNamespace: String  // Which parsed namespace declared this
) : NamedResource {
    /**
     * Full storage ID as used in Minecraft (e.g., "bs:const")
     */
    val minecraftId: String = "$namespace:$name"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Storage) return false
        return minecraftId == other.minecraftId
    }

    override fun hashCode(): Int = minecraftId.hashCode()
}