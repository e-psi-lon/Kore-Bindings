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
import io.github.e_psi_lon.kore.bindings.core.Library
import io.github.e_psi_lon.kore.bindings.core.SupportedSource
import io.github.e_psi_lon.kore.bindings.smithed.Smithed
import io.github.e_psi_lon.kore.bindings.smithed.custom_block.CustomBlock
import kotlinx.serialization.encodeToString
import net.benwoodworth.knbt.StringifiedNbt


object Crafter: Library {
    internal val nbtSerializer
        get() = StringifiedNbt {
            this.nameRootClasses = false
        }
    override val namespace: String = "${Smithed.namespace}.crafter"
    override val version: String
        get() = "0.2.0"
    override val source: SupportedSource
        get() = SupportedSource.SMITHED
    override val url: String
        get() = "crafter"
    override val externalDependencies: List<Library>
        get() = listOf(CustomBlock)

    context(DataPack)
    fun smithedRecipes(dataPack: DataPack, recipeNamespace: String, directory: String = "calls/smithed", block: RecipesBuilder.() -> Unit) {
        val recipes = RecipesBuilder(recipeNamespace, dataPack)
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
                        recipe.result!!
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
                        recipe.result!!
                    }
                }
            }
            setTag("event/shapeless_recipes", namespace)
        }
    }

    fun input() = storage("input", namespace)
}


val Smithed.crafter get() = Crafter