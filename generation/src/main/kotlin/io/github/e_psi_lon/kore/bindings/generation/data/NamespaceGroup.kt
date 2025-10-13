package io.github.e_psi_lon.kore.bindings.generation.data

/**
 * Represents a group of namespaces sharing a common prefix (e.g., bs.math, bs.log)
 */
data class NamespaceGroup(
    val prefix: String,
    val subNamespaces: List<String>,
    val sharedResources: SharedResources
)
