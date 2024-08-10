package io.github.e_psi_lon.kore.bindings.bookshelf.biome

import io.github.ayfri.kore.arguments.types.resources.PredicateArgument
import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.utils.nbt
import io.github.e_psi_lon.kore.bindings.bookshelf.Bookshelf
import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.core.SupportedSource

object Biome : Library {
    override val namespace = "${Bookshelf.namespace}.biome"
    override val version: String
        get() = "2.1.1"
    override val source: SupportedSource
        get() = TODO("Not yet implemented") // SupportedSource.BOOKSHELF not yet implemented
    override val url: String
        get() = "biome"

    context(Function)
    fun getBiome() =
        function("get_biome", namespace, true)

    context(Function)
    fun getTemperature(scale: Double) =
        function("get_temperature", namespace, arguments = nbt { "scale" to scale })

    fun canRain() =
        PredicateArgument("can_rain", namespace)

    fun canSnow() =
        PredicateArgument("can_snow", namespace)

    fun hasPrecipitation() =
        PredicateArgument("has_precipitation", namespace)
}


val Bookshelf.biome
    get() = Biome
