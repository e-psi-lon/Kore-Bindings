package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.functions.Function
import kotlinx.serialization.encodeToString
import io.github.e_psi_lon.kore.bindings.core.CustomItemArgument

class ShapelessRecipe: Recipe {
    private val ingredients = mutableListOf<Item>()
    override var result: CustomItemArgument? = null

    fun ingredient(ingredient: Item) {
        if (ingredients.size >= 9)
            throw IllegalStateException("You can't have more than 9 ingredients.")
        if (ingredients.contains(ingredient))
            throw IllegalStateException("You can't have the same ingredient multiple times.")
        if (ingredient.count == null)
            throw IllegalArgumentException("You must specify the count of the ingredient in a shapeless recipe.")
        ingredients.add(ingredient)
    }

    fun result(item: CustomItemArgument) {
        result = item
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
                data(Crafter.input(), "{recipe:${snbt.encodeToString(ingredients)}}")
            }
            run {
                loot {
                    target {
                        replaceBlock2(vec3(), ItemSlotType.invoke { "container.16" })
                    }

                    source {
                        loot(result!!.lootTable)
                    }
                }
            }
        }
    }
}