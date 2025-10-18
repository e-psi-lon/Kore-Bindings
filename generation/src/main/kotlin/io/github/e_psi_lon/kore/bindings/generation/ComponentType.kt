package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import io.github.ayfri.kore.arguments.types.resources.tagged.FunctionTagArgument
import io.github.ayfri.kore.arguments.types.resources.tagged.ItemTagArgument
import io.github.ayfri.kore.generated.arguments.types.*
import io.github.ayfri.kore.generated.arguments.tagged.*
import io.github.ayfri.kore.generated.arguments.worldgen.types.*
import io.github.ayfri.kore.generated.arguments.worldgen.tagged.*
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.utils.pascalCase

private fun usualParam(name: String = "name") = mapOf(
    ParameterSpec.builder(name, String::class).build() to null,
    ParameterSpec.builder("namespace", String::class).build() to null

)


class ClassOrMemberName private constructor(
    val className: ClassName? = null,
    val memberName: MemberName? = null
) {
    constructor(className: ClassName) : this(className, null)
    constructor(memberName: MemberName) : this(null, memberName)
}

fun ClassName.toClassOrMemberName() = ClassOrMemberName(this)
fun MemberName.toClassOrMemberName() = ClassOrMemberName(memberName = this)


interface ComponentType {
	val name: String
	val directoryName: String
	val fileExtension: String
		// The default value is "json" because it's the most common case.
		get() = "json"
	val koreMethodOrClass: ClassOrMemberName
	val returnType: ClassName
		// The default value is the same as koreMethodOrClass because it's the most common case. If
		get() = if (koreMethodOrClass.className != null) {
            koreMethodOrClass.className!!
        } else {
            throw IllegalStateException("returnType must be overridden if koreMethodOrClass is a MemberName")
        }
	val requiredContext: ClassName? get() = null
	val parameters: Map<ParameterSpec, Any?>
		// The default value is namespace and name to null because it's the most common case.
		get() = usualParam()
	val duplicateSuffix: String
		get() = name.lowercase().pascalCase()
}

enum class DatapackComponentType: ComponentType {
	ADVANCEMENT {
		override val directoryName = "advancement"
		override val koreMethodOrClass = AdvancementArgument::class.asClassName().toClassOrMemberName()
	},
	BANNER_PATTERN {
		override val directoryName = "banner_pattern"
		override val koreMethodOrClass = BannerPatternArgument::class.asClassName().toClassOrMemberName()
	},
	CAT_VARIANT {
		override val directoryName = "cat_variant"
		override val koreMethodOrClass = CatVariantArgument::class.asClassName().toClassOrMemberName()
	},
	CHAT_TYPE {
		override val directoryName = "chat"
		override val koreMethodOrClass = ChatTypeArgument::class.asClassName().toClassOrMemberName()
	},
	CHICKEN_VARIANT {
		override val directoryName = "chicken_variant"
		override val koreMethodOrClass = ChickenVariantArgument::class.asClassName().toClassOrMemberName()
	},
	COW_VARIANT {
		override val directoryName = "cow_variant"
		override val koreMethodOrClass = CowVariantArgument::class.asClassName().toClassOrMemberName()
	},
	DAMAGE_TYPE {
		override val directoryName = "damage_type"
		override val koreMethodOrClass = DamageTypeArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("damageType")
	},
	DIALOG {
		override val directoryName = "dialog"
		override val koreMethodOrClass = DialogArgument::class.asClassName().toClassOrMemberName()
	},
	DIMENSION_TYPE {
		override val directoryName = "dimension_type"
		override val koreMethodOrClass = DimensionTypeArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("dimension")
	},
	ENCHANTMENT {
		override val directoryName = "enchantment"
		override val koreMethodOrClass = EnchantmentArgument::class.asClassName().toClassOrMemberName()
	},
	ENCHANTMENT_PROVIDER {
		override val directoryName = "enchantment_provider"
		override val fileExtension = "json"
		override val koreMethodOrClass = EnchantmentProviderArgument::class.asClassName().toClassOrMemberName()
	},
	FROG_VARIANT {
		override val directoryName = "frog_variant"
		override val koreMethodOrClass = FrogVariantArgument::class.asClassName().toClassOrMemberName()
	},
	FUNCTION {
		override val directoryName = "function"
		override val fileExtension = "mcfunction"
		override val koreMethodOrClass = MemberName("io.github.ayfri.kore.commands", "function").toClassOrMemberName()
		override val requiredContext = Function::class.asClassName()
		override val returnType = Command::class.asClassName()
		override val parameters = usualParam().plus(
			ParameterSpec.builder("group", Boolean::class).build() to false
		)
	},
	INSTRUMENT {
		override val directoryName = "instrument"
		override val koreMethodOrClass = InstrumentArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("instrument")
	},
	JUKEBOX_SONG {
		override val directoryName = "jukebox_song"
		override val koreMethodOrClass = JukeboxSongArgument::class.asClassName().toClassOrMemberName()
		override val returnType = JukeboxSongArgument::class.asClassName()
	},
	LOOT_TABLE {
		override val directoryName = "loot_table"
		override val koreMethodOrClass = LootTableArgument::class.asClassName().toClassOrMemberName()
	},
	PAINTING_VARIANT {
		override val directoryName = "painting_variant"
		override val koreMethodOrClass = PaintingVariantArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("paintingVariant")
	},
	RECIPE {
		override val directoryName = "recipe"
		override val koreMethodOrClass = RecipeArgument::class.asClassName().toClassOrMemberName()
	},
	STRUCTURE {
		override val directoryName = "structure"
		override val fileExtension = "nbt"
		override val koreMethodOrClass = StructureArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("structure")
	},
	// TAGS
	BANNER_PATTERN_TAG {
		override val directoryName = "tags/banner_pattern"
		override val koreMethodOrClass = BannerPatternTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
	BLOCK_TAG {
		override val directoryName = "tags/block"
		override val koreMethodOrClass = BlockTypeTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
	CAT_VARIANT_TAG {
		override val directoryName = "tags/cat_variant"
		override val koreMethodOrClass = CatVariantTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
	DAMAGE_TYPE_TAG {
		override val directoryName = "tags/damage_type"
		override val koreMethodOrClass = DamageTypeTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
    DIALOG_TAG {
        override val directoryName = "tags/dialog"
        override val koreMethodOrClass = DialogTagArgument::class.asClassName().toClassOrMemberName()

    },
	ENCHANTMENT_TAG {
		override val directoryName = "tags/enchantment"
		override val koreMethodOrClass = EnchantmentTagArgument::class.asClassName().toClassOrMemberName()
	},
	ENTITY_TYPE_TAG {
		override val directoryName = "tags/entity_type"
		override val koreMethodOrClass = EntityTypeTagArgument::class.asClassName()
		override val parameters = usualParam("tagName")
	},
	FLUID_TAG {
		override val directoryName = "tags/fluid"
		override val koreMethodOrClass = FluidTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
	FUNCTION_TAG {
		override val directoryName = "tags/function"
		override val fileExtension = "json"
		override val koreMethodOrClass = MemberName("io.github.ayfri.kore.commands", "function").toClassOrMemberName()
		override val requiredContext = Function::class.asClassName()
		override val returnType = Command::class.asClassName()
		override val parameters = usualParam().plus(
			ParameterSpec.builder("group", Boolean::class).build() to true
		)
	},
    FUNCTION_TAG_ARGUMENT {
		override val directoryName = "tags/function"
		override val koreMethodOrClass = FunctionTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
		override val duplicateSuffix = "FunctionTagArgument"
	},
	GAME_EVENT_TAG {
		override val directoryName = "tags/game_event"
		override val koreMethodOrClass = GameEventTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
	INSTRUMENT_TAG {
		override val directoryName = "tags/instrument"
		override val koreMethodOrClass = InstrumentTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
    ITEM_TAG {
		override val directoryName = "tags/item"
		override val koreMethodOrClass = ItemTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
	PAINTING_VARIANT_TAG {
		override val directoryName = "tags/painting_variant"
		override val koreMethodOrClass = PaintingVariantTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
	POINT_OF_INTEREST_TYPE_TAG {
		override val directoryName = "tags/point_of_interest_type"
		override val koreMethodOrClass = PointOfInterestTypeTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
	// SUBTAGS FOR WORLDGEN
	BIOME_TAG {
		override val directoryName = "tags/worldgen/biome"
		override val koreMethodOrClass = BiomeTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
	FLAT_LEVEL_GENERATOR_PRESET_TAG {
		override val directoryName = "tags/worldgen/flat_level_generator_preset"
		override val koreMethodOrClass = FlatLevelGeneratorPresetTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
	STRUCTURE_TAG {
		override val directoryName = "tags/worldgen/structure"
		override val koreMethodOrClass = StructureTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
	WORLD_PRESET_TAG {
		override val directoryName = "tags/worldgen/world_preset"
		override val koreMethodOrClass = WorldPresetTagArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("tagName")
	},
	TRIAL_SPAWNER {
		override val directoryName = "trial_spawner"
        override val koreMethodOrClass = TrialSpawnerArgument::class.asClassName().toClassOrMemberName()

	},
	TRIM_MATERIAL {
		override val directoryName = "trim_material"
		override val koreMethodOrClass = TrimMaterialArgument::class.asClassName().toClassOrMemberName()
	},
	TRIM_PATTERN {
		override val directoryName = "trim_pattern"
		override val koreMethodOrClass = TrimPatternArgument::class.asClassName().toClassOrMemberName()
	},
	WOLF_SOUND_VARIANT {
		override val directoryName = "wolf_sound_variant"
		override val koreMethodOrClass = WolfSoundVariantArgument::class.asClassName().toClassOrMemberName()
	},
	WOLF_VARIANT {
		override val directoryName = "wolf_variant"
		override val koreMethodOrClass = WolfVariantArgument::class.asClassName().toClassOrMemberName()
	},
	// WORLDGEN
	BIOME {
		override val directoryName = "worldgen/biome"
		override val koreMethodOrClass = BiomeArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("biome")
	},
	CONFIGURED_CARVER {
		override val directoryName = "worldgen/configured_carver"
		override val koreMethodOrClass = ConfiguredCarverArgument::class.asClassName().toClassOrMemberName()
	},
	CONFIGURED_FEATURE {
		override val directoryName = "worldgen/configured_feature"
		override val koreMethodOrClass = ConfiguredFeatureArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("feature")
	},
	DENSITY_FUNCTION {
		override val directoryName = "worldgen/density_function"
		override val koreMethodOrClass = DensityFunctionArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("densityFunctionType")
	},
	FLAT_LEVEL_GENERATOR_PRESET {
		override val directoryName = "worldgen/flat_level_generator_preset"
		override val koreMethodOrClass = FlatLevelGeneratorPresetArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("preset")
	},
	MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST {
		override val directoryName = "worldgen/multi_noise_biome_source_parameter_list"
	    override val koreMethodOrClass = MultiNoiseBiomeSourceParameterListArgument::class.asClassName().toClassOrMemberName()
	},
	NOISE {
		override val directoryName = "worldgen/noise"
		override val koreMethodOrClass = NoiseArgument::class.asClassName().toClassOrMemberName()
	},
	NOISE_SETTINGS {
		override val directoryName = "worldgen/noise_settings"
		override val koreMethodOrClass = NoiseSettingsArgument::class.asClassName().toClassOrMemberName()
	},
	PLACED_FEATURE {
		override val directoryName = "worldgen/placed_feature"
		override val koreMethodOrClass = PlacedFeatureArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("feature")
	},
	PROCESSOR_LIST {
		override val directoryName = "worldgen/processor_list"
		override val koreMethodOrClass = ProcessorListArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("feature")
	},
	WORLDGEN_STRUCTURE {
		override val directoryName = "worldgen/structure"
		override val koreMethodOrClass = StructureArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("structure")
	},
	STRUCTURE_SET {
		override val directoryName = "worldgen/structure_set"
		override val koreMethodOrClass = StructureSetArgument::class.asClassName().toClassOrMemberName()
	},
	TEMPLATE_POOL {
		override val directoryName = "worldgen/template_pool"
		override val koreMethodOrClass = TemplatePoolArgument::class.asClassName().toClassOrMemberName()
	},
	WORLD_PRESET {
		override val directoryName = "worldgen/world_preset"
		override val koreMethodOrClass = WorldPresetArgument::class.asClassName().toClassOrMemberName()
		override val parameters = usualParam("worldPreset")
	}
}
