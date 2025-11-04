package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import io.github.ayfri.kore.arguments.types.resources.tagged.FunctionTagArgument
import io.github.ayfri.kore.arguments.types.resources.tagged.ItemTagArgument
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.functions.Function
import io.github.ayfri.kore.generated.arguments.tagged.*
import io.github.ayfri.kore.generated.arguments.types.*
import io.github.ayfri.kore.generated.arguments.worldgen.tagged.BiomeTagArgument
import io.github.ayfri.kore.generated.arguments.worldgen.tagged.FlatLevelGeneratorPresetTagArgument
import io.github.ayfri.kore.generated.arguments.worldgen.tagged.StructureTagArgument
import io.github.ayfri.kore.generated.arguments.worldgen.tagged.WorldPresetTagArgument
import io.github.ayfri.kore.generated.arguments.worldgen.types.*
import io.github.e_psi_lon.kore.bindings.generation.components.ClassOrMemberName
import io.github.e_psi_lon.kore.bindings.generation.components.ComponentType
import io.github.e_psi_lon.kore.bindings.generation.components.ParameterValueSource
import io.github.e_psi_lon.kore.bindings.generation.components.toClassOrMemberName

enum class DatapackComponentType(
    override val directoryName: String,
    // The default value is "json" because it's the most common case.
    override val fileExtension: String = "json",
    override val koreMethodOrClass: ClassOrMemberName,
    override val returnType: ClassName = when (koreMethodOrClass)  {
        // Smart cast to 'ClassOrMemberName.Class' is impossible, because 'koreMethodOrClass' is a property that has
        // an open or custom getter even within the branch. Manual cast was suggested by the compiler itself
        is ClassOrMemberName.Class -> koreMethodOrClass.name
        // MemberName is the type wrapped by ClassOrMemberName.Member
        is ClassOrMemberName.Member -> throw IllegalStateException("returnType must be overridden because koreMethodOrClass is meant to represent a MemberName")
    },
    override val requiredContext: ClassName? = null,
    // The default provides 'name' and 'namespace' parameters sourced from ParameterValueSource.Name and .Namespace
    override val parameters: Map<ParameterSpec, ParameterValueSource> = ComponentType.usualParam(),

) : ComponentType {
    ADVANCEMENT("advancement", koreMethodOrClass = AdvancementArgument::class.asClassName().toClassOrMemberName()),
    BANNER_PATTERN("banner_pattern", koreMethodOrClass = BannerPatternArgument::class.asClassName().toClassOrMemberName()),
    CAT_VARIANT("cat_variant", koreMethodOrClass = CatVariantArgument::class.asClassName().toClassOrMemberName()),
    CHAT_TYPE("chat", koreMethodOrClass = ChatTypeArgument::class.asClassName().toClassOrMemberName()),
    CHICKEN_VARIANT("chicken_variant", koreMethodOrClass = ChickenVariantArgument::class.asClassName().toClassOrMemberName()),
    COW_VARIANT("cow_variant", koreMethodOrClass = CowVariantArgument::class.asClassName().toClassOrMemberName()),
    DAMAGE_TYPE("damage_type", koreMethodOrClass = DamageTypeArgument::class.asClassName().toClassOrMemberName()),
    DIALOG("dialog", koreMethodOrClass = DialogArgument::class.asClassName().toClassOrMemberName()),
    DIMENSION_TYPE("dimension_type", koreMethodOrClass = DimensionTypeArgument::class.asClassName().toClassOrMemberName()),
    ENCHANTMENT("enchantment", koreMethodOrClass = EnchantmentArgument::class.asClassName().toClassOrMemberName()),
    ENCHANTMENT_PROVIDER("enchantment_provider", koreMethodOrClass = EnchantmentProviderArgument::class.asClassName().toClassOrMemberName()),
    FROG_VARIANT("frog_variant", koreMethodOrClass = FrogVariantArgument::class.asClassName().toClassOrMemberName()),
    FUNCTION(
        "function",
        fileExtension = "mcfunction",
        koreMethodOrClass = MemberName("io.github.ayfri.kore.commands", "function").toClassOrMemberName(),
        returnType = Command::class.asClassName(),
        requiredContext = Function::class.asClassName(),
        parameters = ComponentType.usualParam().plus(
            ParameterSpec.builder("group", Boolean::class).build() to ParameterValueSource.Default(false)
        )
    ),
    INSTRUMENT("instrument", koreMethodOrClass = InstrumentArgument::class.asClassName().toClassOrMemberName()),
    JUKEBOX_SONG(
        "jukebox_song",
        koreMethodOrClass = JukeboxSongArgument::class.asClassName().toClassOrMemberName(),
    ),
    LOOT_TABLE("loot_table", koreMethodOrClass = LootTableArgument::class.asClassName().toClassOrMemberName()),
    PAINTING_VARIANT("painting_variant", koreMethodOrClass = PaintingVariantArgument::class.asClassName().toClassOrMemberName()),
    RECIPE("recipe", koreMethodOrClass = RecipeArgument::class.asClassName().toClassOrMemberName()),
    STRUCTURE("structure", fileExtension = "nbt", koreMethodOrClass = StructureArgument::class.asClassName().toClassOrMemberName()),

    // TAGS
    BANNER_PATTERN_TAG("tags/banner_pattern", koreMethodOrClass = BannerPatternTagArgument::class.asClassName().toClassOrMemberName()),
    BLOCK_TAG("tags/block", koreMethodOrClass = BlockTypeTagArgument::class.asClassName().toClassOrMemberName()),
    CAT_VARIANT_TAG("tags/cat_variant", koreMethodOrClass = CatVariantTagArgument::class.asClassName().toClassOrMemberName()),
    DAMAGE_TYPE_TAG("tags/damage_type", koreMethodOrClass = DamageTypeTagArgument::class.asClassName().toClassOrMemberName()),
    DIALOG_TAG("tags/dialog", koreMethodOrClass = DialogTagArgument::class.asClassName().toClassOrMemberName()),
    ENCHANTMENT_TAG("tags/enchantment", koreMethodOrClass = EnchantmentTagArgument::class.asClassName().toClassOrMemberName()),
    ENTITY_TYPE_TAG("tags/entity_type", koreMethodOrClass = EntityTypeTagArgument::class.asClassName().toClassOrMemberName()),
    FLUID_TAG("tags/fluid", koreMethodOrClass = FluidTagArgument::class.asClassName().toClassOrMemberName()),
    FUNCTION_TAG(
        "tags/function",
        koreMethodOrClass = MemberName("io.github.ayfri.kore.commands", "function").toClassOrMemberName(),
        returnType = Command::class.asClassName(),
        requiredContext = Function::class.asClassName(),
        parameters = ComponentType.usualParam().plus(
            ParameterSpec.builder("group", Boolean::class).build() to ParameterValueSource.Default(true)
        )
    ),
    FUNCTION_TAG_ARGUMENT(
        "tags/function",
        koreMethodOrClass = FunctionTagArgument::class.asClassName().toClassOrMemberName(),
        parameters = ComponentType.usualParam("tagName")
    ) {
        override val duplicateSuffix = "FunctionTagArgument"
    },
    GAME_EVENT_TAG("tags/game_event", koreMethodOrClass = GameEventTagArgument::class.asClassName().toClassOrMemberName()),
    INSTRUMENT_TAG("tags/instrument", koreMethodOrClass = InstrumentTagArgument::class.asClassName().toClassOrMemberName()),
    ITEM_TAG("tags/item", koreMethodOrClass = ItemTagArgument::class.asClassName().toClassOrMemberName()),
    PAINTING_VARIANT_TAG("tags/painting_variant", koreMethodOrClass = PaintingVariantTagArgument::class.asClassName().toClassOrMemberName()),
    POINT_OF_INTEREST_TYPE_TAG("tags/point_of_interest_type", koreMethodOrClass = PointOfInterestTypeTagArgument::class.asClassName().toClassOrMemberName()),

    // SUBTAGS FOR WORLDGEN
    BIOME_TAG("tags/worldgen/biome", koreMethodOrClass = BiomeTagArgument::class.asClassName().toClassOrMemberName()),
    FLAT_LEVEL_GENERATOR_PRESET_TAG("tags/worldgen/flat_level_generator_preset", koreMethodOrClass = FlatLevelGeneratorPresetTagArgument::class.asClassName().toClassOrMemberName()),
    STRUCTURE_TAG("tags/worldgen/structure", koreMethodOrClass = StructureTagArgument::class.asClassName().toClassOrMemberName()),
    WORLD_PRESET_TAG("tags/worldgen/world_preset", koreMethodOrClass = WorldPresetTagArgument::class.asClassName().toClassOrMemberName()),
    TRIAL_SPAWNER("trial_spawner", koreMethodOrClass = TrialSpawnerArgument::class.asClassName().toClassOrMemberName()),
    TRIM_MATERIAL("trim_material", koreMethodOrClass = TrimMaterialArgument::class.asClassName().toClassOrMemberName()),
    TRIM_PATTERN("trim_pattern", koreMethodOrClass = TrimPatternArgument::class.asClassName().toClassOrMemberName()),
    WOLF_SOUND_VARIANT("wolf_sound_variant", koreMethodOrClass = WolfSoundVariantArgument::class.asClassName().toClassOrMemberName()),
    WOLF_VARIANT("wolf_variant", koreMethodOrClass = WolfVariantArgument::class.asClassName().toClassOrMemberName()),

    // WORLDGEN
    BIOME("worldgen/biome", koreMethodOrClass = BiomeArgument::class.asClassName().toClassOrMemberName()),
    CONFIGURED_CARVER("worldgen/configured_carver", koreMethodOrClass = ConfiguredCarverArgument::class.asClassName().toClassOrMemberName()),
    CONFIGURED_FEATURE("worldgen/configured_feature", koreMethodOrClass = ConfiguredFeatureArgument::class.asClassName().toClassOrMemberName()),
    DENSITY_FUNCTION("worldgen/density_function", koreMethodOrClass = DensityFunctionArgument::class.asClassName().toClassOrMemberName()),
    FLAT_LEVEL_GENERATOR_PRESET("worldgen/flat_level_generator_preset", koreMethodOrClass = FlatLevelGeneratorPresetArgument::class.asClassName().toClassOrMemberName()),
    MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST("worldgen/multi_noise_biome_source_parameter_list", koreMethodOrClass = MultiNoiseBiomeSourceParameterListArgument::class.asClassName().toClassOrMemberName()),
    NOISE("worldgen/noise", koreMethodOrClass = NoiseArgument::class.asClassName().toClassOrMemberName()),
    NOISE_SETTINGS("worldgen/noise_settings", koreMethodOrClass = NoiseSettingsArgument::class.asClassName().toClassOrMemberName()),
    PLACED_FEATURE("worldgen/placed_feature", koreMethodOrClass = PlacedFeatureArgument::class.asClassName().toClassOrMemberName()),
    PROCESSOR_LIST("worldgen/processor_list", koreMethodOrClass = ProcessorListArgument::class.asClassName().toClassOrMemberName()),
    WORLDGEN_STRUCTURE("worldgen/structure", koreMethodOrClass = StructureArgument::class.asClassName().toClassOrMemberName()),
    STRUCTURE_SET("worldgen/structure_set", koreMethodOrClass = StructureSetArgument::class.asClassName().toClassOrMemberName()),
    TEMPLATE_POOL("worldgen/template_pool", koreMethodOrClass = TemplatePoolArgument::class.asClassName().toClassOrMemberName()),
    WORLD_PRESET("worldgen/world_preset", koreMethodOrClass = WorldPresetArgument::class.asClassName().toClassOrMemberName())
}
