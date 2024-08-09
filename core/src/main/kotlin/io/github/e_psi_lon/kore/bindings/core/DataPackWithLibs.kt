package io.github.e_psi_lon.kore.bindings.core

import io.github.ayfri.kore.DataPack

class DataPackWithLibs(
    private val dataPack: DataPack,
) {
    val libraries = mutableListOf<Library>()

    private fun merge() {}

    fun generate() {
        dataPack.generate()
        libraries.forEach { it.generate() }
        merge()
    }

    fun generateZip() {
        dataPack.generateZip()
        libraries.forEach { it.generateZip() }
        merge()
    }
}

fun DataPack.registerLibs(vararg libraries: Library): DataPackWithLibs {
    val dataPackWithLibs = DataPackWithLibs(this)
    dataPackWithLibs.libraries.addAll(libraries)
    libraries.forEach { library -> library.externalDependencies.forEach { dataPackWithLibs.libraries.add(it) } }
    return dataPackWithLibs
}