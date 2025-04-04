package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.types.literals.self
import io.github.ayfri.kore.commands.execute.execute
import io.github.ayfri.kore.commands.function
import io.github.ayfri.kore.commands.give
import io.github.ayfri.kore.commands.say
import io.github.ayfri.kore.functions.function
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
    function("test") {
        execute {
            run {
                say("Hello, world!")
            }
        }
    }
    functions.first {
        it.name == "shaped_recipes"
    }.lines.last() assertsIs """
        execute store result score @s smithed.data if entity @s[scores={smithed.data=0}] if data storage smithed.crafter:input recipe{0:[{id:"minecraft:diamond",Slot:0b},{id:"minecraft:diamond",Slot:2b}],1:[{id:"minecraft:diamond",Slot:0b},{id:"minecraft:diamond",Slot:2b}],2:[{id:"minecraft:diamond",Slot:0b},{id:"minecraft:diamond",Slot:2b}]} run give @s minecraft:diamond
    """.trimIndent()
    functions.first {
        it.name == "shapeless_recipes"
    }.lines.last() assertsIs """
        execute store result score @s smithed.data if entity @s[scores={smithed.data=0}] if score count smithed.data matches 3 if data storage smithed.crafter:input recipe{ingredients:[{Count:1b,id:"minecraft:diamond"},{Count:1b,id:"minecraft:diamond"},{Count:1b,id:"minecraft:diamond"}]} run function test:generated_scopes/generated_1453128758
    """.trimIndent()

}