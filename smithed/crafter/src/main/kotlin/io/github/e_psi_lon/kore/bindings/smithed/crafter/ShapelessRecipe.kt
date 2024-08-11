package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.commands.Command
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.benwoodworth.knbt.*


@Serializable(with = ShapelessRecipe.Companion.RecipeSerializer::class)
@SerialName("recipe")
class ShapelessRecipe : Recipe {
    override lateinit var dataPack: DataPack
    override lateinit var recipeNamespace: String
    internal val ingredients = mutableListOf<Item>()
    override var result: Command? = null


    internal fun initialize(dataPack: DataPack, recipeNamespace: String) {
        this.dataPack = dataPack
        this.recipeNamespace = recipeNamespace
    }


    fun ingredient(ingredient: Item) {
        if (ingredients.size >= 9)
            throw IllegalStateException("You can't have more than 9 ingredients.")
        if (ingredient.count == null)
            throw IllegalArgumentException("You must specify the count of the ingredient in a shapeless recipe.")
        ingredients.add(ingredient)
    }

    companion object {
        object RecipeSerializer : KSerializer<ShapelessRecipe> {
            override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ShapelessRecipe") {
                element("ingredients", ListSerializer(Item.serializer()).descriptor)
            }

            override fun serialize(encoder: Encoder, value: ShapelessRecipe) {
                val nbtEncoder = encoder as? NbtEncoder
                    ?: error("This serializer can be used only with NBT format. Expected Encoder to be NbtEncoder, got ${encoder::class}")
                val nbtObject = buildNbtCompound {
                    put("ingredients", StringifiedNbt { }.encodeToNbtTag(value.ingredients))
                }
                nbtEncoder.encodeNbtTag(nbtObject)
            }

            override fun deserialize(decoder: Decoder): ShapelessRecipe {
                val nbtDecoder = decoder as? NbtDecoder
                    ?: error("This serializer can be used only with NBT format. Expected Decoder to be NbtDecoder, got ${decoder::class}")
                val nbtObject = nbtDecoder.decodeNbtTag().nbtCompound

                val shapelessRecipe = ShapelessRecipe()
                val ingredientsTag = nbtObject["ingredients"] ?: error("Expected ingredients tag")
                val ingredients = StringifiedNbt { }.decodeFromNbtTag<List<Item>>(ingredientsTag)

                shapelessRecipe.ingredients.addAll(ingredients)
                return shapelessRecipe
            }
        }
    }
}