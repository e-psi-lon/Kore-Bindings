package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.CONTAINER
import io.github.ayfri.kore.arguments.maths.vec3
import io.github.ayfri.kore.arguments.types.resources.LootTableArgument
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.commands.command
import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.commands.loot
import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.functions.generatedFunction

interface Recipe {
    var dataPack: DataPack
    var result: Command?

    fun result(item: LootTableArgument) {
        result = Function("", "", "", dataPack).loot {
            target { replaceBlock(vec3(), CONTAINER[16]) }
            source { loot(item) }
        }
    }


    fun result(block: Function.() -> Unit) {
        val name = "generated_${hashCode()}"
        val function = Function("", "", "", dataPack).apply { block() }
        if (function.lines.size == 1) {
            result = command(function.lines.first())
            println(result.toString())
        } else {
            val generatedFunction = dataPack.generatedFunction(name) {
                comment("Generated function ${asString()}")
                block()
            }
            result = Function("", "", "", dataPack).function(generatedFunction.name)
        }
    }
}