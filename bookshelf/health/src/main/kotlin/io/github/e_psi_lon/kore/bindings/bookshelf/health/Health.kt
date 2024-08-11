package io.github.e_psi_lon.kore.bindings.bookshelf.health

import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.functions.generatedFunction
import io.github.ayfri.kore.utils.nbt
import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.bookshelf.Bookshelf
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtFloat

object Health : Library {
    override val namespace = "health"
    override val version
        get() = "unspecified"
    override val source
        get() = TODO("Not yet implemented")
    override val url = "health"

    context(Function)
    fun addHealth(health: Float) {
        function(namespace, "add_health", true, nbt { "points" to health })
    }

    context(Function)
    fun addMaxHealth(health: Float) {
        function(namespace, "add_max_health", true, nbt { "points" to health })
    }

    context(Function)
    fun getHealth(scale: Float) {
        function(namespace, "get_health", true, nbt { "scale" to scale })
    }

    context(Function)
    fun getMaxHealth(scale: Float) {
        function(namespace, "get_max_health", true, nbt { "scale" to scale })
    }

    context(Function)
    fun setHealth(health: Float) {
        function(namespace, "set_health", true, nbt { "points" to health })
    }

    context(Function)
    fun setMaxHealth(health: Float) {
        function(namespace, "set_max_health", true, nbt { "points" to health })
    }

    context(Function)
    fun timeToLive(time: Int, unit: TimeUnit, onDeath: Function.() -> Command, generatedNamespace: String = namespace) {
        val function = Function("", "", "", datapack).apply { onDeath() }
        val onDeathCommand = if (function.isInlinable) {
            Function("", generatedNamespace, "", datapack).onDeath()
        } else {
            val name = "generated_${hashCode()}"
            val generatedFunction = datapack.generatedFunction(name, generatedNamespace, datapack.configuration.generatedFunctionsFolder) {
                onDeath()
            }
            Function("", "", "", datapack).function(generatedFunction.namespace, "${generatedFunction.directory}/${generatedFunction.name}")
        }
        function(namespace, "time_to_live", true, nbt {
            "time" to time
            "unit" to unit.unit
            "on_death" to onDeathCommand
        })
    }
}

val Bookshelf.health
    get() = Health