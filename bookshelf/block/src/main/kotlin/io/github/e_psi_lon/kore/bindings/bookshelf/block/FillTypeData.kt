package io.github.e_psi_lon.kore.bindings.bookshelf.block

import io.github.ayfri.kore.generated.Blocks
import kotlinx.serialization.Serializable

@Serializable
data class FillTypeData(
    val type: Blocks,
    val from: List<Int>,
    val to: List<Int>,
    val mode: FillMode = FillMode.REPLACE,
    val limit: Int = 4096,
    val masks: List<FillMask>
) {
    init {
        require(from.size == 3) { "from must have 3 elements" }
        require(to.size == 3) { "to must have 3 elements" }
    }
}
