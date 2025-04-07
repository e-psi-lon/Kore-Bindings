package io.github.e_psi_lon.kore.bindings.core

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.generation.DataPackGenerationOptions
import io.github.ayfri.kore.generation.DataPackJarGenerationOptions
import io.github.ayfri.kore.generation.mergeWithPacks

class DataPackWithLibs(
    private val dataPack: DataPack,
) {
    val libraries = mutableSetOf<Library>()

    fun generate(init: DataPackGenerationOptions.() -> Unit = {}) {
        val paths = libraries.map { it.fetchDataPack().toPath() }
        dataPack.generate {
            mergeWithPacks(*paths.toTypedArray())
            init()
        }
    }

    fun generateZip(init: DataPackGenerationOptions.() -> Unit = {}) {
        val paths = libraries.map { it.fetchDataPack().toPath() }
        dataPack.generateZip {
            mergeWithPacks(*paths.toTypedArray())
            init()
        }
    }

    fun generateJar(init: DataPackJarGenerationOptions.() -> Unit = {}) {
        throw NotImplementedError("Jar generation is not supported yet.")
        /*
        val paths = libraries.map { it.fetchDataPack().toPath() }
        dataPack.generateJar {
            // mergeWithPacks(*paths.toTypedArray())
            init()
        }
        */
    }
}

fun DataPack.registerLibs(vararg libraries: Library): DataPackWithLibs {
    val dataPackWithLibs = DataPackWithLibs(this)
    dataPackWithLibs.libraries.addAll(libraries)
    libraries.forEach { library ->
        library.externalDependencies.forEach { dependency ->
            dataPackWithLibs.libraries.add(dependency)
        }
    }
    return dataPackWithLibs
}
