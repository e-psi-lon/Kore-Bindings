package io.github.e_psi_lon.kore.bindings.bookshelf.block

import io.github.ayfri.kore.generated.Blocks
import kotlinx.serialization.Serializable

@Serializable
data class SetTypeData(
    val type: Blocks,
    val mode: FillMode = FillMode.REPLACE
)
