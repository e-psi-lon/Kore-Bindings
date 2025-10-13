package io.github.e_psi_lon.kore.bindings.generation.data

/**
 * Resources that are shared across multiple namespaces in a group
 */
data class SharedResources(
    val storages: Set<Storage>,
    val scoreboards: Set<Scoreboard>
) {
    companion object {
        val EMPTY = SharedResources(emptySet(), emptySet())
    }
}