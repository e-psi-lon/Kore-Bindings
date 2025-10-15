package io.github.e_psi_lon.kore.bindings.generation.data

/**
 * Root structure representing a fully parsed datapack
 */
data class Datapack(
    val namespaces: List<ParsedNamespace>,
    val namespaceGroups: List<NamespaceGroup>
)



