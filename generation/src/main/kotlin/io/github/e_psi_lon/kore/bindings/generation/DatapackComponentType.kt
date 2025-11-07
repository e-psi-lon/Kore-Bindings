package io.github.e_psi_lon.kore.bindings.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asClassName
import io.github.ayfri.kore.arguments.types.resources.tagged.FunctionTagArgument
import io.github.ayfri.kore.arguments.types.resources.tagged.ItemTagArgument
import io.github.ayfri.kore.commands.Command
import io.github.ayfri.kore.commands.function
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
import io.github.e_psi_lon.kore.bindings.generation.components.ComponentType.Companion.classOrMemberOf
import io.github.e_psi_lon.kore.bindings.generation.components.ParameterValueSource
import io.github.e_psi_lon.kore.bindings.generation.components.toClassOrMemberName
import io.github.e_psi_lon.kore.bindings.generation.poet.asMemberName
import net.benwoodworth.knbt.NbtCompound
import kotlin.reflect.KFunction4

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
    ADVANCEMENT("advancement", koreMethodOrClass = classOrMemberOf<AdvancementArgument>()),
    BANNER_PATTERN("banner_pattern", koreMethodOrClass = classOrMemberOf<BannerPatternArgument>()),
    CAT_VARIANT("cat_variant", koreMethodOrClass = classOrMemberOf<CatVariantArgument>()),
    CHAT_TYPE("chat", koreMethodOrClass = classOrMemberOf<ChatTypeArgument>()),
    CHICKEN_VARIANT("chicken_variant", koreMethodOrClass = classOrMemberOf<ChickenVariantArgument>()),
    COW_VARIANT("cow_variant", koreMethodOrClass = classOrMemberOf<CowVariantArgument>()),
    DAMAGE_TYPE("damage_type", koreMethodOrClass = classOrMemberOf<DamageTypeArgument>()),
    DIALOG("dialog", koreMethodOrClass = classOrMemberOf<DialogArgument>()),
    DIMENSION_TYPE("dimension_type", koreMethodOrClass = classOrMemberOf<DimensionTypeArgument>()),
    ENCHANTMENT("enchantment", koreMethodOrClass = classOrMemberOf<EnchantmentArgument>()),
    ENCHANTMENT_PROVIDER("enchantment_provider", koreMethodOrClass = classOrMemberOf<EnchantmentProviderArgument>()),
    FROG_VARIANT("frog_variant", koreMethodOrClass = classOrMemberOf<FrogVariantArgument>()),
    FUNCTION(
        "function",
        fileExtension = "mcfunction",
        koreMethodOrClass = run {
            val temp: KFunction4<Function, String, Boolean, NbtCompound?, Command> = Function::function
            temp.asMemberName().toClassOrMemberName()
        },
        returnType = Command::class.asClassName(),
        requiredContext = Function::class.asClassName(),
        parameters = ComponentType.usualParam() +
            (ParameterSpec.builder("group", Boolean::class).build() to ParameterValueSource.Default(false))
    ),
    INSTRUMENT("instrument", koreMethodOrClass = classOrMemberOf<InstrumentArgument>()),
    JUKEBOX_SONG(
        "jukebox_song",
        koreMethodOrClass = classOrMemberOf<JukeboxSongArgument>(),
    ),
    LOOT_TABLE("loot_table", koreMethodOrClass = classOrMemberOf<LootTableArgument>()),
    PAINTING_VARIANT("painting_variant", koreMethodOrClass = classOrMemberOf<PaintingVariantArgument>()),
    RECIPE("recipe", koreMethodOrClass = classOrMemberOf<RecipeArgument>()),
    STRUCTURE("structure", fileExtension = "nbt", koreMethodOrClass = classOrMemberOf<StructureArgument>()),

    // TAGS
    BANNER_PATTERN_TAG("tags/banner_pattern", koreMethodOrClass = classOrMemberOf<BannerPatternTagArgument>()),
    BLOCK_TAG("tags/block", koreMethodOrClass = classOrMemberOf<BlockTypeTagArgument>()),
    CAT_VARIANT_TAG("tags/cat_variant", koreMethodOrClass = classOrMemberOf<CatVariantTagArgument>()),
    DAMAGE_TYPE_TAG("tags/damage_type", koreMethodOrClass = classOrMemberOf<DamageTypeTagArgument>()),
    DIALOG_TAG("tags/dialog", koreMethodOrClass = classOrMemberOf<DialogTagArgument>()),
    ENCHANTMENT_TAG("tags/enchantment", koreMethodOrClass = classOrMemberOf<EnchantmentTagArgument>()),
    ENTITY_TYPE_TAG("tags/entity_type", koreMethodOrClass = classOrMemberOf<EntityTypeTagArgument>()),
    FLUID_TAG("tags/fluid", koreMethodOrClass = classOrMemberOf<FluidTagArgument>()),
    FUNCTION_TAG(
        "tags/function",
        koreMethodOrClass = run {
            val temp: KFunction4<Function, String, Boolean, NbtCompound?, Command> = Function::function
            temp.asMemberName().toClassOrMemberName()
        },
        returnType = Command::class.asClassName(),
        requiredContext = Function::class.asClassName(),
        parameters = ComponentType.usualParam() +
            (ParameterSpec.builder("group", Boolean::class).build() to ParameterValueSource.Default(true))
    ),
    FUNCTION_TAG_ARGUMENT(
        "tags/function",
        koreMethodOrClass = classOrMemberOf<FunctionTagArgument>(),
        parameters = ComponentType.usualParam("tagName")
    ) {
        override val duplicateSuffix = "FunctionTagArgument"
    },
    GAME_EVENT_TAG("tags/game_event", koreMethodOrClass = classOrMemberOf<GameEventTagArgument>()),
    INSTRUMENT_TAG("tags/instrument", koreMethodOrClass = classOrMemberOf<InstrumentTagArgument>()),
    ITEM_TAG("tags/item", koreMethodOrClass = classOrMemberOf<ItemTagArgument>()),
    PAINTING_VARIANT_TAG("tags/painting_variant", koreMethodOrClass = classOrMemberOf<PaintingVariantTagArgument>()),
    POINT_OF_INTEREST_TYPE_TAG("tags/point_of_interest_type", koreMethodOrClass = classOrMemberOf<PointOfInterestTypeTagArgument>()),

    // SUBTAGS FOR WORLDGEN
    BIOME_TAG("tags/worldgen/biome", koreMethodOrClass = classOrMemberOf<BiomeTagArgument>()),
    FLAT_LEVEL_GENERATOR_PRESET_TAG("tags/worldgen/flat_level_generator_preset", koreMethodOrClass = classOrMemberOf<FlatLevelGeneratorPresetTagArgument>()),
    STRUCTURE_TAG("tags/worldgen/structure", koreMethodOrClass = classOrMemberOf<StructureTagArgument>()),
    WORLD_PRESET_TAG("tags/worldgen/world_preset", koreMethodOrClass = classOrMemberOf<WorldPresetTagArgument>()),
    TRIAL_SPAWNER("trial_spawner", koreMethodOrClass = classOrMemberOf<TrialSpawnerArgument>()),
    TRIM_MATERIAL("trim_material", koreMethodOrClass = classOrMemberOf<TrimMaterialArgument>()),
    TRIM_PATTERN("trim_pattern", koreMethodOrClass = classOrMemberOf<TrimPatternArgument>()),
    WOLF_SOUND_VARIANT("wolf_sound_variant", koreMethodOrClass = classOrMemberOf<WolfSoundVariantArgument>()),
    WOLF_VARIANT("wolf_variant", koreMethodOrClass = classOrMemberOf<WolfVariantArgument>()),

    // WORLDGEN
    BIOME("worldgen/biome", koreMethodOrClass = classOrMemberOf<BiomeArgument>()),
    CONFIGURED_CARVER("worldgen/configured_carver", koreMethodOrClass = classOrMemberOf<ConfiguredCarverArgument>()),
    CONFIGURED_FEATURE("worldgen/configured_feature", koreMethodOrClass = classOrMemberOf<ConfiguredFeatureArgument>()),
    DENSITY_FUNCTION("worldgen/density_function", koreMethodOrClass = classOrMemberOf<DensityFunctionArgument>()),
    FLAT_LEVEL_GENERATOR_PRESET("worldgen/flat_level_generator_preset", koreMethodOrClass = classOrMemberOf<FlatLevelGeneratorPresetArgument>()),
    MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST("worldgen/multi_noise_biome_source_parameter_list", koreMethodOrClass = classOrMemberOf<MultiNoiseBiomeSourceParameterListArgument>()),
    NOISE("worldgen/noise", koreMethodOrClass = classOrMemberOf<NoiseArgument>()),
    NOISE_SETTINGS("worldgen/noise_settings", koreMethodOrClass = classOrMemberOf<NoiseSettingsArgument>()),
    PLACED_FEATURE("worldgen/placed_feature", koreMethodOrClass = classOrMemberOf<PlacedFeatureArgument>()),
    PROCESSOR_LIST("worldgen/processor_list", koreMethodOrClass = classOrMemberOf<ProcessorListArgument>()),
    WORLDGEN_STRUCTURE("worldgen/structure", koreMethodOrClass = classOrMemberOf<StructureArgument>()),
    STRUCTURE_SET("worldgen/structure_set", koreMethodOrClass = classOrMemberOf<StructureSetArgument>()),
    TEMPLATE_POOL("worldgen/template_pool", koreMethodOrClass = classOrMemberOf<TemplatePoolArgument>()),
    WORLD_PRESET("worldgen/world_preset", koreMethodOrClass = classOrMemberOf<WorldPresetArgument>());
}
