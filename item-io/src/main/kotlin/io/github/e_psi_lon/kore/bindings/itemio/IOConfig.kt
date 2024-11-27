package io.github.e_psi_lon.kore.bindings.itemio

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IOConfig(
    @SerialName("Slot")
    val slot: Int? = null,
    val mode: IOConfigMode? = null,
    @SerialName("allowed_side")
    val allowedSide: AllowedSide = AllowedSide.NONE,
    val filters: List<Filter>? = null,
    val maxStackSize: Int? = null
)

enum class IOConfigMode {
    INPUT,
    OUTPUT,
    BOTH
}

@Serializable
data class AllowedSide(
    val north: Boolean,
    val east: Boolean,
    val south: Boolean,
    val west: Boolean,
    val top: Boolean,
    val bottom: Boolean
) {
    companion object {
        val ALL = AllowedSide(
            north = true,
            east = true,
            south = true,
            west = true,
            top = true,
            bottom = true
        )
        val NONE = AllowedSide(
            north = false,
            east = false,
            south = false,
            west = false,
            top = false,
            bottom = false
        )
    }
}