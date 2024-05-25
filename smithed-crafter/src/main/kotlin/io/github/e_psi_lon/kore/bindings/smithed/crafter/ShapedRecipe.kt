package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.CONTAINER
import io.github.ayfri.kore.arguments.maths.vec3
import io.github.ayfri.kore.arguments.selector.scores
import io.github.ayfri.kore.arguments.types.literals.self
import io.github.ayfri.kore.arguments.types.resources.LootTableArgument
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.commands.execute.execute
import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.commands.loot
import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.functions.generatedFunction
import io.github.e_psi_lon.kore.bindings.smithed.Smithed
import jdk.javadoc.internal.doclets.formats.html.markup.RawHtml.comment
import kotlinx.serialization.encodeToString
import net.benwoodworth.knbt.StringifiedNbt

class ShapedRecipe(override val dataPack: DataPack): Recipe {
    override var result: Command? = null
    private val pattern: MutableList<MutableList<Char>> = mutableListOf()
    private val keys: MutableMap<Char, Item?> = mutableMapOf()

    fun pattern(vararg rows: String) {
        pattern.clear()
        for (row in rows) {
            if (row.length < 3) {
                throw IllegalArgumentException("Row must be at least 3 characters long.")
            }
            val items = row.toMutableList()
            pattern.add(items)
        }
    }

    fun key(key: Char, item: Item) {
        if (!pattern.flatten().contains(key)) {
            throw IllegalArgumentException("Pattern does not contain '$key'.")
        }
        keys[key] = item
    }

    fun result(item: LootTableArgument) {
        result = Function("", "", "", dataPack).loot {
            target { replaceBlock(vec3(), CONTAINER[16]) }
            source { loot(item) }
        }
    }

    fun result(function: Function.() -> Unit) {
        val name = "generated_${hashCode()}"
        val generatedFunction = dataPack.generatedFunction(name) { function() }
        if (generatedFunction.name == name) comment("Generated function ${hashCode()}")
        result = Function("", "", "", dataPack).function(generatedFunction.name)
    }

    fun result(command: Command) {
        result = command
    }

    context(Function)
    override fun build() {
        check(result != null) { "Result must be set." }
        check(pattern.isNotEmpty()) { "Pattern must be set." }
        check(keys.isNotEmpty()) { "Keys must be set." }
        val items = mutableListOf(
            mutableListOf<Item?>(),
            mutableListOf(),
            mutableListOf()
        )
        for ((index, row) in pattern.withIndex()) {
            check(row.size == 3) { "Row must be 3 characters long." }
            for ((index2, char) in row.withIndex()) {
                check(keys.containsKey(char)) { "Key $char is not set." }
                if (char != ' ') {
                    items[index].add(keys[char]?.setSlot(index2))
                }
            }
        }
        check(items.flatten().contains(null)) { "Some slots are not set." }

        execute {
            storeResult {
                Smithed.data(self())
            }
            ifCondition {
                entity(
                    self {
                        scores {
                            Smithed.data() equalTo 0
                        }
                    }
                )
            }

            ifCondition {
                val recipe = items.mapIndexed { index, list ->
                    index.toString() to list.map { it!! }
                }.toMap()
                data(Crafter.input(), "{recipe:${StringifiedNbt { }.encodeToString(recipe)}}")
            }

            run {
                result!!
            }
        }

    }

}