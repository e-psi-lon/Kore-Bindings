package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.arguments.ItemSlotType
import io.github.ayfri.kore.arguments.maths.vec3
import io.github.ayfri.kore.arguments.selector.scores
import io.github.ayfri.kore.arguments.types.literals.self
import io.github.ayfri.kore.arguments.types.resources.LootTableArgument
import io.github.ayfri.kore.commands.execute.execute
import io.github.ayfri.kore.commands.loot
import io.github.ayfri.kore.functions.Function
import io.github.e_psi_lon.kore.bindings.smithed.Smithed
import kotlinx.serialization.encodeToString
import net.benwoodworth.knbt.StringifiedNbt

class ShapedRecipe: Recipe {
    override var result: LootTableArgument? = null
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
        result = item
    }

    context(Function)
    override fun build() {
        if (result == null) {
            throw IllegalArgumentException("Result must be set.")
        }
        if (pattern.isEmpty()) {
            throw IllegalArgumentException("Pattern must be set.")
        }
        if (keys.isEmpty()) {
            throw IllegalArgumentException("Keys must be set.")
        }
        val items = mutableListOf(
            mutableListOf<Item?>(),
            mutableListOf(),
            mutableListOf()
        )
        for ((index, row) in pattern.withIndex()) {
            if (row.size < 3) {
                throw IllegalArgumentException("Row must be at least 3 characters long.")
            }
            for ((index2, char) in row.withIndex()) {
                if (keys[char] == null) {
                    throw IllegalArgumentException("Key $char is not set.")
                }
                if (char != ' ') {
                    items[index].add(keys[char]?.addSlot(index2))
                }
            }
        }
        if (items.flatten().contains(null)) {
            throw IllegalArgumentException("Some slots are not set.")
        }

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
                loot {
                    target {
                        replaceBlock(vec3(), ItemSlotType.invoke { "container.16" })
                    }
                    source {
                        loot(result!!)
                    }
                }
            }
        }

    }

}