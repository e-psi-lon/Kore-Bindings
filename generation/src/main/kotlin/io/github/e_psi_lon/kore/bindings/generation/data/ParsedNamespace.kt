package io.github.e_psi_lon.kore.bindings.generation.data

import io.github.e_psi_lon.kore.bindings.generation.DatapackComponentType
import io.github.e_psi_lon.kore.bindings.generation.sanitizePascal


/**
 * A fully parsed namespace with all its components and resources
 */
data class ParsedNamespace(
    val name: String,  // "minecraft" or "bs.math"
    val prefix: String?,  // "bs" for "bs.math", null for "minecraft"
    val components: Map<DatapackComponentType, List<Component>>,
    val localStorages: Set<Storage>,  // Storages unique to this namespace
    val localScoreboards: Set<Scoreboard>,  // Scoreboards unique to this namespace
    val macros: List<Macro>
) {
    /**
     * Sanitized name for Kotlin object (e.g., "BsMath")
     */
    val kotlinName: String = name.sanitizePascal()

    /**
     * Whether this namespace is part of a group
     */
    val isGrouped: Boolean = prefix != null
}