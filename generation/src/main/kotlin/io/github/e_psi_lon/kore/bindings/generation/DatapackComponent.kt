package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import io.github.ayfri.kore.arguments.types.resources.*
import io.github.ayfri.kore.arguments.types.resources.tagged.*
import io.github.ayfri.kore.arguments.types.resources.worldgen.*
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.utils.pascalCase


interface DatapackComponent {
	val name: String
	val folderName: String
	val fileExtension: String
		// The default value is "json" because it's the most common case.
		get() = "json"
	val koreMethodOrClass: ClassName
	val returnType: ClassName
		// The default value is the same as koreMethodOrClass because it's the most common case.
		get() = koreMethodOrClass
	val requiredContext: ClassName? get() = null
	val parameters: Map<ParameterSpec, Any?>
		// The default value is namespace and name to null because it's the most common case.
		get() = mapOf(
			ParameterSpec.builder("name", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null
		)
	val duplicateSuffix: String
		get() = name.lowercase().pascalCase()
}

enum class DatapackComponentType: DatapackComponent {
	ADVANCEMENT {
		override val folderName = "advancement"
		override val koreMethodOrClass = AdvancementArgument::class.asClassName()
	},
	BANNER_PATTERN {
		override val folderName = "banner_pattern"
		override val koreMethodOrClass = BannerPatternArgument::class.asClassName()
	},
	CHAT_TYPE {
		override val folderName = "chat"
		override val koreMethodOrClass = ChatTypeArgument::class.asClassName()
	},
	DAMAGE_TYPE {
		override val folderName = "damage_type"
		override val koreMethodOrClass = DamageTypeArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("damageType", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null
		)

	},
	DIMENSION_TYPE {
		override val folderName = "dimension_type"
		override val koreMethodOrClass = DimensionTypeArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("dimension", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null
		)
	},
	ENCHANTMENT {
		override val folderName = "enchantement"
		override val koreMethodOrClass = EnchantmentArgument::class.asClassName()
	},
	/*ENCHANTMENT_PROVIDER {
		override val folderName = "enchantment_provider"
		override val fileExtension = "json"
	},*/
	FUNCTION {
		override val folderName = "function"
		override val fileExtension = "mcfunction"
		override val koreMethodOrClass = ClassName("io.github.ayfri.kore.commands", "function")
		override val requiredContext = Function::class.asClassName()
		override val returnType = Command::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("name", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
			ParameterSpec.builder("group", Boolean::class).build() to false
		)
	},
	INSTRUMENT {
		override val folderName = "instrument"
		override val koreMethodOrClass = InstrumentArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("instrument", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null
		)
	},
	JUKEBOX_SONG {
		override val folderName = "jukebox_song"
		override val koreMethodOrClass = JukeboxSongArgument::class.asClassName()
		override val returnType = JukeboxSongArgument::class.asClassName()
	},
	LOOT_TABLE {
		override val folderName = "loot_table"
		override val koreMethodOrClass = LootTableArgument::class.asClassName()
	},
	PAINTING_VARIANT {
		override val folderName = "painting_variant"
		override val koreMethodOrClass = PaintingVariantArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("paintingVariant", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	RECIPE {
		override val folderName = "recipe"
		override val koreMethodOrClass = RecipeArgument::class.asClassName()
	},
	STRUCTURE {
		override val folderName = "structure"
		override val fileExtension = "nbt"
		override val koreMethodOrClass = StructureArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("structure", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null
		)
	},
	// TAGS
	BANNER_PATTERN_TAG {
		override val folderName = "tags/banner_pattern"
		override val koreMethodOrClass = BannerPatternTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	BLOCK_TAG {
		override val folderName = "tags/block"
		override val koreMethodOrClass = BlockTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	CAT_VARIANT_TAG {
		override val folderName = "tags/cat_variant"
		override val koreMethodOrClass = CatVariantTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	DAMAGE_TYPE_TAG {
		override val folderName = "tags/damage_type"
		override val koreMethodOrClass = DamageTypeTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	ENCHANTMENT_TAG {
		override val folderName = "tags/enchantment"
		override val koreMethodOrClass = EnchantmentTagArgument::class.asClassName()
	},
	ENTITY_TYPE_TAG {
		override val folderName = "tags/entity_type"
		override val koreMethodOrClass = EntityTypeTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	FLUID_TAG {
		override val folderName = "tags/fluid"
		override val koreMethodOrClass = FluidTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	FUNCTION_TAG {
		override val folderName = "tags/function"
		override val fileExtension = "json"
		override val koreMethodOrClass = ClassName("io.github.ayfri.kore.commands", "function")
		override val requiredContext = Function::class.asClassName()
		override val returnType = Command::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("name", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
			ParameterSpec.builder("group", Boolean::class).build() to true
		)
	},
	FUNCTION_TAG_ARGUMENT {
		override val folderName = "tags/function"
		override val koreMethodOrClass = FunctionTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
		override val duplicateSuffix = "FunctionTagArgument"
	},
	GAME_EVENT_TAG {
		override val folderName = "tags/game_event"
		override val koreMethodOrClass = GameEventTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	INSTRUMENT_TAG {
		override val folderName = "tags/instrument"
		override val koreMethodOrClass = InstrumentTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	ITEM_TAG {
		override val folderName = "tags/item"
		override val koreMethodOrClass = ItemTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	PAINTING_VARIANT_TAG {
		override val folderName = "tags/painting_variant"
		override val koreMethodOrClass = PaintingVariantTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	POINT_OF_INTEREST_TYPE_TAG {
		override val folderName = "tags/point_of_interest_type"
		override val koreMethodOrClass = PointOfInterestTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	// SUBTAGS FOR WORLDGEN
	BIOME_TAG {
		override val folderName = "tags/worldgen/biome"
		override val koreMethodOrClass = BiomeTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	FLAT_LEVEL_GENERATOR_PRESET_TAG {
		override val folderName = "tags/worldgen/flat_level_generator_preset"
		override val koreMethodOrClass = FlatLevelGeneratorPresetTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	STRUCTURE_TAG {
		override val folderName = "tags/worldgen/structure"
		override val koreMethodOrClass = StructureTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	WORLD_PRESET_TAG {
		override val folderName = "tags/worldgen/world_preset"
		override val koreMethodOrClass = WorldPresetTagArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("tagName", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null,
		)
	},
	/* TRIAL_SPAWNER {
		override val folderName = "trial_spawner"
	},*/
	TRIM_MATERIAL {
		override val folderName = "trim_material"
		override val koreMethodOrClass = TrimMaterialArgument::class.asClassName()
	},
	TRIM_PATTERN {
		override val folderName = "trim_pattern"
		override val koreMethodOrClass = TrimPatternArgument::class.asClassName()
	},
	WOLF_VARIANT {
		override val folderName = "wolf_variant"
		override val koreMethodOrClass = WolfVariantArgument::class.asClassName()
	},
	// WORLDGEN
	BIOME {
		override val folderName = "worldgen/biome"
		override val koreMethodOrClass = BiomeArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("biome", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null
		)
	},
	/*
	CONFIGURED_CARVER {
		override val folderName = "worldgen/configured_carver"
	},*/
	CONFIGURED_FEATURE {
		override val folderName = "worldgen/configured_feature"
		override val koreMethodOrClass = ConfiguredFeatureArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("feature", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null
		)
	},
	DENSITY_FUNCTION {
		override val folderName = "worldgen/density_function"
		override val koreMethodOrClass = DensityFunctionArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("densityFunctionType", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null
		)
	},
	FLAT_LEVEL_GENERATOR_PRESET {
		override val folderName = "worldgen/flat_level_generator_preset"
		override val koreMethodOrClass = FlatLevelGeneratorPresetArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("preset", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null
		)
	},
	/* MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST {
		override val folderName = "worldgen/multi_noise_biome_source_parameter_list"
	},*/
	NOISE {
		override val folderName = "worldgen/noise"
		override val koreMethodOrClass = NoiseArgument::class.asClassName()
	},
	NOISE_SETTINGS {
		override val folderName = "worldgen/noise_settings"
		override val koreMethodOrClass = NoiseSettingsArgument::class.asClassName()
	},
	PLACED_FEATURE {
		override val folderName = "worldgen/placed_feature"
		override val koreMethodOrClass = PlacedFeatureArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("feature", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null
		)
	},
	PROCESSOR_LIST {
		override val folderName = "worldgen/processor_list"
		override val koreMethodOrClass = ProcessorListArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("feature", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null
		)
	},
	WORLDGEN_STRUCTURE {
		override val folderName = "worldgen/structure"
		override val koreMethodOrClass = StructureArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("structure", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null
		)
	},
	STRUCTURE_SET {
		override val folderName = "worldgen/structure_set"
		override val koreMethodOrClass = StructureSetArgument::class.asClassName()
	},
	TEMPLATE_POOL {
		override val folderName = "worldgen/template_pool"
		override val koreMethodOrClass = TemplatePoolArgument::class.asClassName()
	},
	WORLD_PRESET {
		override val folderName = "worldgen/world_preset"
		override val koreMethodOrClass = WorldPresetArgument::class.asClassName()
		override val parameters = mapOf(
			ParameterSpec.builder("worldPreset", String::class).build() to null,
			ParameterSpec.builder("namespace", String::class).build() to null
		)
	}
}