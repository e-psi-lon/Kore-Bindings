package io.github.e_psi_lon.kore.bindings.generation.data


/**
 * A fully parsed namespace with all its components and resources
 */
data class ParsedNamespace(
    val name: String,  // "minecraft" or "bs.math"
    val prefix: String?,  // "bs" for "bs.math", null for "minecraft"
    val components: List<Component>,
    val localStorages: Set<Storage>,  // Storages unique to this namespace
    val localScoreboards: Set<Scoreboard>,  // Scoreboards unique to this namespace
    val macros: List<Macro>
) {
    /**
     * Whether this namespace is part of a group
     */
    val isGrouped: Boolean = prefix != null
}