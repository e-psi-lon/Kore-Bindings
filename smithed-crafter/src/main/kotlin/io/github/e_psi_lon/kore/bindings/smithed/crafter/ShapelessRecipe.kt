package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.CONTAINER
import io.github.ayfri.kore.arguments.maths.vec3
import io.github.ayfri.kore.arguments.numbers.ranges.IntRangeOrInt
import io.github.ayfri.kore.arguments.selector.scores
import io.github.ayfri.kore.arguments.types.literals.literal
import io.github.ayfri.kore.arguments.types.literals.self
import io.github.ayfri.kore.arguments.types.resources.LootTableArgument
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.commands.execute.execute
import io.github.ayfri.kore.commands.loot
import kotlinx.serialization.encodeToString
import io.github.e_psi_lon.kore.bindings.smithed.Smithed
import net.benwoodworth.knbt.StringifiedNbt


class ShapelessRecipe(override val dataPack: DataPack): Recipe {
    private val ingredients = mutableListOf<Item>()
    override var result: Command? = null

    fun ingredient(ingredient: Item) {
        if (ingredients.size >= 9)
            throw IllegalStateException("You can't have more than 9 ingredients.")
        if (ingredients.contains(ingredient))
            throw IllegalStateException("You can't have the same ingredient multiple times.")
        if (ingredient.count == null)
            throw IllegalArgumentException("You must specify the count of the ingredient in a shapeless recipe.")
        ingredients.add(ingredient)
    }

    fun result(item: LootTableArgument) {
        result = Function("", "", "", dataPack).loot {
            target {
                replaceBlock(vec3(), CONTAINER[16])
            }
            source {
                loot(item)
            }
        }
    }

    context(Function)
    override fun build() {
        if (result == null) {
            throw IllegalStateException("Result is not defined.")
        }
        if (ingredients.isEmpty()) {
            throw IllegalStateException("Ingredients are not defined.")
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
                score(
                    literal("count"),
                    Smithed.data(),
                    IntRangeOrInt(int = ingredients.size)
                )
            }
            ifCondition {
                data(Crafter.input(), "{recipe:${StringifiedNbt {  }.encodeToString(ingredients)}}")
            }
            run {
                result!!
            }
        }
    }
}