package io.github.e_psi_lon.kore.bindings.generation.data

/**
 * Root structure representing a fully parsed datapack
 */
data class Datapack(
    val namespaces: List<ParsedNamespace>,
    val namespaceGroups: Map<String, NamespaceGroup>
) {
    /**
     * Get all namespaces that belong to a prefix (e.g., all "bs.*" namespaces)
     */
    fun getNamespacesByPrefix(prefix: String): List<ParsedNamespace> =
        namespaces.filter { it.name.startsWith("$prefix.") }

    /**
     * Get standalone namespaces (no prefix grouping)
     */
    fun getStandaloneNamespaces(): List<ParsedNamespace> =
        namespaces.filter { !it.name.contains('.') }
}



