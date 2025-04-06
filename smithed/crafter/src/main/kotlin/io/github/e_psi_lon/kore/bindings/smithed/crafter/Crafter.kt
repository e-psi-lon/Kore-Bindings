package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.numbers.ranges.IntRangeOrInt
import io.github.ayfri.kore.arguments.selector.scores
import io.github.ayfri.kore.arguments.types.literals.literal
import io.github.ayfri.kore.arguments.types.literals.self
import io.github.ayfri.kore.arguments.types.resources.storage
import io.github.ayfri.kore.commands.execute.execute
import io.github.ayfri.kore.functions.function
import io.github.ayfri.kore.functions.setTag
import io.github.e_psi_lon.kore.bindings.smithed.Smithed
import kotlinx.serialization.encodeToString
import net.benwoodworth.knbt.StringifiedNbt

internal val nbtSerializer
    get() = StringifiedNbt {
        this.nameRootClasses = false
    }

context(DataPack)
fun SmithedCrafter.smithedRecipes(
    recipeNamespace: String,
    directory: String = "calls/smithed",
    block: RecipesBuilder.() -> Unit
) {
    val recipes = RecipesBuilder(recipeNamespace, this@DataPack)
    recipes.block()
    function("shaped_recipes", recipeNamespace, directory) {
        for (recipe in recipes.recipes.filterIsInstance<ShapedRecipe>()) {
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
                    data(input(), "recipe${nbtSerializer.encodeToString(recipe)}")
                }

                run {
                    addLine(recipe.result!!)
                }
            }

            setTag("event/shaped_recipes", namespace)
        }
    }
    function("shapeless_recipes", recipeNamespace, directory) {
        for (recipe in recipes.recipes.filterIsInstance<ShapelessRecipe>()) {
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
                        IntRangeOrInt(int = recipe.ingredients.size)
                    )
                }
                ifCondition {
                    data(input(), "recipe${nbtSerializer.encodeToString(recipe)}")
                }
                run {
                    addLine(recipe.result!!)
                }
            }
        }
        setTag(SmithedCrafter.Event.shapelessRecipes)
    }
}

fun SmithedCrafter.input() = storage("input", namespace)
