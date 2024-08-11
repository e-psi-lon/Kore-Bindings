package io.github.e_psi_lon.kore.bindings.bookshelf.block

import io.github.ayfri.kore.generated.Blocks

data class MappingRegistry(
    val set: Set,
    val attrs: List<String>,
    val type: Blocks
)

enum class Set {
    CUBE,
    STAIRS,
    SLAB,
}