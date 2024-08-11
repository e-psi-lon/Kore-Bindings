package io.github.e_psi_lon.kore.bindings.bookshelf.block

import io.github.ayfri.kore.generated.Blocks
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlockData(
    val type: Blocks,
    val properties: Map<String, String>,
    @SerialName("extra_nbt")
    val extraNbt: Map<String, String> = emptyMap()
)
