package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.types.resources.storage
import io.github.ayfri.kore.functions.function
import io.github.e_psi_lon.kore.bindings.smithed.Smithed


object Crafter {
    val namespace: String = "${Smithed.namespace}.crafter"

    context(DataPack)
    fun recipes(namespace: String, directory: String = "calls/smithed", block: RecipesBuilder.() -> Unit) {
        val recipes = RecipesBuilder(namespace)
        recipes.block()
        function("shaped_recipes", namespace, directory) {
            for (recipe in recipes.recipes.filterIsInstance<ShapedRecipe>()) {
                recipe.build()
            }
        }
        function("shapeless_recipes", namespace, directory) {
            for (recipe in recipes.recipes.filterIsInstance<ShapelessRecipe>()) {
                recipe.build()
            }
        }
    }

    fun input() = storage("input", namespace)
}



