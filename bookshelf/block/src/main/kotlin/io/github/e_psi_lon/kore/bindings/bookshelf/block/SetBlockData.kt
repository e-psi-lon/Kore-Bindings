package io.github.e_psi_lon.kore.bindings.bookshelf.block

import io.github.ayfri.kore.generated.Blocks
import kotlinx.serialization.Serializable

@Serializable
data class SetBlockData(
    val block: Blocks,
    val mode: FillMode = FillMode.REPLACE
)