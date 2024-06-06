package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.numbers.ranges.IntRangeOrInt
import io.github.ayfri.kore.arguments.selector.scores
import io.github.ayfri.kore.arguments.types.literals.literal
import io.github.ayfri.kore.arguments.types.literals.self
import io.github.ayfri.kore.arguments.types.resources.storage
import io.github.ayfri.kore.commands.execute.execute
import io.github.ayfri.kore.features.tags.tag
import io.github.ayfri.kore.functions.function
import io.github.e_psi_lon.kore.bindings.smithed.Smithed
import kotlinx.serialization.encodeToString
import net.benwoodworth.knbt.StringifiedNbt


object Crafter {
    val namespace: String = "${Smithed.namespace}.crafter"

    context(DataPack)
    fun smithedRecipes(dataPack: DataPack, recipeNamespace: String, directory: String = "calls/smithed", block: RecipesBuilder.() -> Unit) {
        val recipes = RecipesBuilder(recipeNamespace, dataPack)
        recipes.block()
        println(recipeNamespace)
        val shapedRecipes = function("shaped_recipes", recipeNamespace, directory) {
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
                        data(input(), StringifiedNbt { }.encodeToString(recipe))
                    }

                    run {
                        recipe.result!!
                    }
                }
            }
        }
        tag("event/recipes", "functions", namespace) {
            add(shapedRecipes)
        }
        val shapelessRecipes = function("shapeless_recipes", recipeNamespace, directory) {
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
                        data(input(), StringifiedNbt {  }.encodeToString(recipe).apply { substring(1..<this.length) })
                    }
                    run {
                        recipe.result!!
                    }
                }
            }
        }
        tag("event/shapeless_recipes", "functions", namespace) {
            add(shapelessRecipes)
        }
    }

    fun input() = storage("input", namespace)
}



