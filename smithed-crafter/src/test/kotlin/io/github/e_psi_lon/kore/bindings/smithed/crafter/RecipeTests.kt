package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.types.literals.self
import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.commands.give
import io.github.ayfri.kore.commands.say
import io.github.ayfri.kore.generated.Items
import io.github.e_psi_lon.kore.bindings.assertions.assertsIs

fun DataPack.smithedRecipeTest() {
    Crafter.smithedRecipes(this, "test") {
        shapedRecipe {
            pattern("# #", "# #", "# #")
            key('#', Items.DIAMOND)
            result {
                give(self(), Items.DIAMOND)
            }
        }
        shapelessRecipe {
            ingredient(Item(1, "minecraft:diamond"))
            ingredient(Item(1, "minecraft:diamond"))
            ingredient(Item(1, "minecraft:diamond"))
            result {
                say("Hello, world!")
                function("test", "test_function")
            }
        }
    }
    functions.first {
        it.name == "shaped_recipes"
    }.lines.last() assertsIs """
        execute store result score @s smithed.data if entity @s[scores={smithed.data=0}] if data storage smithed.crafter:input recipe{0:[{id:"minecraft:diamond",Slot:0b},{id:"minecraft:diamond",Slot:2b}],1:[{id:"minecraft:diamond",Slot:0b},{id:"minecraft:diamond",Slot:2b}],2:[{id:"minecraft:diamond",Slot:0b},{id:"minecraft:diamond",Slot:2b}]} run give @s minecraft:diamond'
    """.trimIndent()


}