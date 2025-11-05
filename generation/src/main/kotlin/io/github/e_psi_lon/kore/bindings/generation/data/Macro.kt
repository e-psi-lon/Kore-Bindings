package io.github.e_psi_lon.kore.bindings.generation.data


/**
 * Represents a macro found in a function file
 */
data class Macro(
    val parameters: List<String>,  // ["x", "y", "z"]
) {
    val hasParameters: Boolean = parameters.isNotEmpty()
}