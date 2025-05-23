package io.github.e_psi_lon.kore.bindings.smithed.crafter

import io.github.ayfri.kore.DataPack
import io.github.ayfri.kore.arguments.types.resources.ItemArgument
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

@Serializable(ShapedRecipe.Companion.RecipeSerializer::class)
@SerialName("recipe")
class ShapedRecipe : Recipe {
    override lateinit var dataPack: DataPack
    override lateinit var recipeNamespace: String
    override var result: Command? = null
    private val pattern: MutableList<MutableList<Char>> = mutableListOf()
    private val keys: MutableMap<Char, Item?> = mutableMapOf()

    fun initialize(dataPack: DataPack, recipeNamespace: String) {
        this.dataPack = dataPack
        this.recipeNamespace = recipeNamespace
    }

    fun pattern(vararg rows: String) {
        pattern.clear()
        for (row in rows) {
            if (row.length < 3) {
                throw IllegalArgumentException("Row must be at least 3 characters long.")
            }
            val items = row.toMutableList()
            pattern.add(items)
        }
    }

    fun key(key: Char, item: Item) {
        if (!pattern.flatten().contains(key)) {
            throw IllegalArgumentException("Pattern does not contain '$key'.")
        }
        if (key == ' ') {
            throw IllegalArgumentException("key ' ' always represent an empty slot")
        }
        keys[key] = item
    }

    fun key(key: Char, item: ItemArgument) {
        key(key, Item(id = item))
    }

    companion object {
        object RecipeSerializer : KSerializer<ShapedRecipe> {
            override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ShapedRecipe") {
                element("0", ListSerializer(Item.serializer()).descriptor, isOptional = true)
                element("1", ListSerializer(Item.serializer()).descriptor, isOptional = true)
                element("2", ListSerializer(Item.serializer()).descriptor, isOptional = true)
            }

            override fun serialize(encoder: Encoder, value: ShapedRecipe) {
                val nbtEncoder = encoder as? NbtEncoder
                    ?: error("This serializer can be used only with NBT format. Expected Encoder to be NbtEncoder, got ${this::class}")
                val nbtObject = buildNbtCompound {
                    value.pattern.forEachIndexed { rowIndex, row ->
                        val items = row.mapIndexed { colIndex, char ->
                            if (char != ' ') {
                                check(value.keys[char] != null) { "Unset key" }
                                value.keys[char]?.copy(slot = colIndex.toByte())
                            } else {
                                null
                            }
                        }.filterNotNull()
                        if (items.isNotEmpty()) {
                            put(rowIndex.toString(), nbtSerializer.encodeToNbtTag(items))
                        }
                    }
                }
                nbtEncoder.encodeNbtTag(nbtObject)
            }

            override fun deserialize(decoder: Decoder): ShapedRecipe {
                val nbtDecoder = decoder as? NbtDecoder
                    ?: error("This serializer can be used only with NBT format. Expected Decoder to be NbtDecoder, got ${this::class}")
                val nbtObject = nbtDecoder.decodeNbtTag().nbtCompound

                val shapedRecipe = ShapedRecipe()
                nbtObject.forEach { (key, nbtElement) ->
                    val rowIndex = key.toIntOrNull() ?: return@forEach
                    val items = nbtSerializer.decodeFromNbtTag<List<Item>>(nbtElement)
                    items.forEach { item ->
                        val col = item.slot?.toInt() ?: 0
                        val char = shapedRecipe.pattern[rowIndex][col]
                        shapedRecipe.keys[char] = item
                    }
                }

                return shapedRecipe
            }
        }
    }


}