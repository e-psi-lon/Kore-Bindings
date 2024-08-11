package io.github.e_psi_lon.kore.bindings.bookshelf.block

import io.github.ayfri.kore.generated.Blocks
import kotlinx.serialization.Serializable

@Serializable
data class FillMask(
    val block: Blocks,
    val negate: Boolean = false,
    val x: Int = 0,
    val y: Int = 0,
    val z: Int = 0,
)
