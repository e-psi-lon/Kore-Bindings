package io.github.e_psi_lon.kore.bindings.core


interface Library {
    val namespace: String
    val version: String
    val source: SupportedSource
    val location: String
    val externalDependencies: List<Library> get() = emptyList()
    fun generate() {}
    fun generateZip() {}
}