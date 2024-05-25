package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.DataPack

class RecipesBuilder(val namespace: String, private val dataPack: DataPack) {
    val recipes = mutableListOf<Recipe>()

    fun shapelessRecipe(block: ShapelessRecipe.() -> Unit) {
        val recipe = ShapelessRecipe(dataPack)
        recipe.block()
        recipes.add(recipe)
    }

    fun shapedRecipe(block: ShapedRecipe.() -> Unit) {
        val recipe = ShapedRecipe(dataPack)
        recipe.block()
        recipes.add(recipe)
    }
}