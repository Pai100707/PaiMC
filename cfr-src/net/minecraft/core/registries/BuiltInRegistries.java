/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  org.slf4j.Logger
 */
package net.minecraft.core.registries;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicates;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.BuiltinTestFunctions;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.numbers.NumberFormatType;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogTypes;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.action.ActionTypes;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.DialogBodyTypes;
import net.minecraft.server.dialog.input.InputControl;
import net.minecraft.server.dialog.input.InputControlTypes;
import net.minecraft.server.jsonrpc.IncomingRpcMethod;
import net.minecraft.server.jsonrpc.IncomingRpcMethods;
import net.minecraft.server.jsonrpc.OutgoingRpcMethod;
import net.minecraft.server.jsonrpc.OutgoingRpcMethods;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionCheckTypes;
import net.minecraft.server.permissions.PermissionTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.util.Util;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.IntProviderType;
import net.minecraft.world.attribute.AttributeType;
import net.minecraft.world.attribute.AttributeTypes;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnConditions;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplays;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplays;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.EnchantmentProviderTypes;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.item.slot.SlotSources;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSources;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGenerators;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBindings;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;
import org.slf4j.Logger;

public class BuiltInRegistries {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Identifier, Supplier<?>> LOADERS = Maps.newLinkedHashMap();
    private static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry(ResourceKey.createRegistryKey(Registries.ROOT_REGISTRY_NAME), Lifecycle.stable());
    public static final DefaultedRegistry<GameEvent> GAME_EVENT = BuiltInRegistries.registerDefaulted(Registries.GAME_EVENT, "step", GameEvent::bootstrap);
    public static final Registry<SoundEvent> SOUND_EVENT = BuiltInRegistries.registerSimple(Registries.SOUND_EVENT, $$0 -> SoundEvents.ITEM_PICKUP);
    public static final DefaultedRegistry<Fluid> FLUID = BuiltInRegistries.registerDefaultedWithIntrusiveHolders(Registries.FLUID, "empty", $$0 -> Fluids.EMPTY);
    public static final Registry<MobEffect> MOB_EFFECT = BuiltInRegistries.registerSimple(Registries.MOB_EFFECT, MobEffects::bootstrap);
    public static final DefaultedRegistry<Block> BLOCK = BuiltInRegistries.registerDefaultedWithIntrusiveHolders(Registries.BLOCK, "air", $$0 -> Blocks.AIR);
    public static final Registry<DebugSubscription<?>> DEBUG_SUBSCRIPTION = BuiltInRegistries.registerSimple(Registries.DEBUG_SUBSCRIPTION, DebugSubscriptions::bootstrap);
    public static final DefaultedRegistry<EntityType<?>> ENTITY_TYPE = BuiltInRegistries.registerDefaultedWithIntrusiveHolders(Registries.ENTITY_TYPE, "pig", $$0 -> EntityType.PIG);
    public static final DefaultedRegistry<Item> ITEM = BuiltInRegistries.registerDefaultedWithIntrusiveHolders(Registries.ITEM, "air", $$0 -> Items.AIR);
    public static final Registry<Potion> POTION = BuiltInRegistries.registerSimple(Registries.POTION, Potions::bootstrap);
    public static final Registry<ParticleType<?>> PARTICLE_TYPE = BuiltInRegistries.registerSimple(Registries.PARTICLE_TYPE, $$0 -> ParticleTypes.BLOCK);
    public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = BuiltInRegistries.registerSimpleWithIntrusiveHolders(Registries.BLOCK_ENTITY_TYPE, $$0 -> BlockEntityType.FURNACE);
    public static final Registry<Identifier> CUSTOM_STAT = BuiltInRegistries.registerSimple(Registries.CUSTOM_STAT, $$0 -> Stats.JUMP);
    public static final DefaultedRegistry<ChunkStatus> CHUNK_STATUS = BuiltInRegistries.registerDefaulted(Registries.CHUNK_STATUS, "empty", $$0 -> ChunkStatus.EMPTY);
    public static final Registry<RuleTestType<?>> RULE_TEST = BuiltInRegistries.registerSimple(Registries.RULE_TEST, $$0 -> RuleTestType.ALWAYS_TRUE_TEST);
    public static final Registry<RuleBlockEntityModifierType<?>> RULE_BLOCK_ENTITY_MODIFIER = BuiltInRegistries.registerSimple(Registries.RULE_BLOCK_ENTITY_MODIFIER, $$0 -> RuleBlockEntityModifierType.PASSTHROUGH);
    public static final Registry<PosRuleTestType<?>> POS_RULE_TEST = BuiltInRegistries.registerSimple(Registries.POS_RULE_TEST, $$0 -> PosRuleTestType.ALWAYS_TRUE_TEST);
    public static final Registry<MenuType<?>> MENU = BuiltInRegistries.registerSimple(Registries.MENU, $$0 -> MenuType.ANVIL);
    public static final Registry<RecipeType<?>> RECIPE_TYPE = BuiltInRegistries.registerSimple(Registries.RECIPE_TYPE, $$0 -> RecipeType.CRAFTING);
    public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZER = BuiltInRegistries.registerSimple(Registries.RECIPE_SERIALIZER, $$0 -> RecipeSerializer.SHAPELESS_RECIPE);
    public static final Registry<Attribute> ATTRIBUTE = BuiltInRegistries.registerSimple(Registries.ATTRIBUTE, Attributes::bootstrap);
    public static final Registry<PositionSourceType<?>> POSITION_SOURCE_TYPE = BuiltInRegistries.registerSimple(Registries.POSITION_SOURCE_TYPE, $$0 -> PositionSourceType.BLOCK);
    public static final Registry<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPE = BuiltInRegistries.registerSimple(Registries.COMMAND_ARGUMENT_TYPE, ArgumentTypeInfos::bootstrap);
    public static final Registry<StatType<?>> STAT_TYPE = BuiltInRegistries.registerSimple(Registries.STAT_TYPE, $$0 -> Stats.ITEM_USED);
    public static final DefaultedRegistry<VillagerType> VILLAGER_TYPE = BuiltInRegistries.registerDefaulted(Registries.VILLAGER_TYPE, "plains", VillagerType::bootstrap);
    public static final DefaultedRegistry<VillagerProfession> VILLAGER_PROFESSION = BuiltInRegistries.registerDefaulted(Registries.VILLAGER_PROFESSION, "none", VillagerProfession::bootstrap);
    public static final Registry<PoiType> POINT_OF_INTEREST_TYPE = BuiltInRegistries.registerSimple(Registries.POINT_OF_INTEREST_TYPE, PoiTypes::bootstrap);
    public static final DefaultedRegistry<MemoryModuleType<?>> MEMORY_MODULE_TYPE = BuiltInRegistries.registerDefaulted(Registries.MEMORY_MODULE_TYPE, "dummy", $$0 -> MemoryModuleType.DUMMY);
    public static final DefaultedRegistry<SensorType<?>> SENSOR_TYPE = BuiltInRegistries.registerDefaulted(Registries.SENSOR_TYPE, "dummy", $$0 -> SensorType.DUMMY);
    public static final Registry<Activity> ACTIVITY = BuiltInRegistries.registerSimple(Registries.ACTIVITY, $$0 -> Activity.IDLE);
    public static final Registry<LootPoolEntryType> LOOT_POOL_ENTRY_TYPE = BuiltInRegistries.registerSimple(Registries.LOOT_POOL_ENTRY_TYPE, $$0 -> LootPoolEntries.EMPTY);
    public static final Registry<LootItemFunctionType<?>> LOOT_FUNCTION_TYPE = BuiltInRegistries.registerSimple(Registries.LOOT_FUNCTION_TYPE, $$0 -> LootItemFunctions.SET_COUNT);
    public static final Registry<LootItemConditionType> LOOT_CONDITION_TYPE = BuiltInRegistries.registerSimple(Registries.LOOT_CONDITION_TYPE, $$0 -> LootItemConditions.INVERTED);
    public static final Registry<LootNumberProviderType> LOOT_NUMBER_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.LOOT_NUMBER_PROVIDER_TYPE, $$0 -> NumberProviders.CONSTANT);
    public static final Registry<LootNbtProviderType> LOOT_NBT_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.LOOT_NBT_PROVIDER_TYPE, $$0 -> NbtProviders.CONTEXT);
    public static final Registry<LootScoreProviderType> LOOT_SCORE_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.LOOT_SCORE_PROVIDER_TYPE, $$0 -> ScoreboardNameProviders.CONTEXT);
    public static final Registry<FloatProviderType<?>> FLOAT_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.FLOAT_PROVIDER_TYPE, $$0 -> FloatProviderType.CONSTANT);
    public static final Registry<IntProviderType<?>> INT_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.INT_PROVIDER_TYPE, $$0 -> IntProviderType.CONSTANT);
    public static final Registry<HeightProviderType<?>> HEIGHT_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.HEIGHT_PROVIDER_TYPE, $$0 -> HeightProviderType.CONSTANT);
    public static final Registry<BlockPredicateType<?>> BLOCK_PREDICATE_TYPE = BuiltInRegistries.registerSimple(Registries.BLOCK_PREDICATE_TYPE, $$0 -> BlockPredicateType.NOT);
    public static final Registry<WorldCarver<?>> CARVER = BuiltInRegistries.registerSimple(Registries.CARVER, $$0 -> WorldCarver.CAVE);
    public static final Registry<Feature<?>> FEATURE = BuiltInRegistries.registerSimple(Registries.FEATURE, $$0 -> Feature.ORE);
    public static final Registry<StructurePlacementType<?>> STRUCTURE_PLACEMENT = BuiltInRegistries.registerSimple(Registries.STRUCTURE_PLACEMENT, $$0 -> StructurePlacementType.RANDOM_SPREAD);
    public static final Registry<StructurePieceType> STRUCTURE_PIECE = BuiltInRegistries.registerSimple(Registries.STRUCTURE_PIECE, $$0 -> StructurePieceType.MINE_SHAFT_ROOM);
    public static final Registry<StructureType<?>> STRUCTURE_TYPE = BuiltInRegistries.registerSimple(Registries.STRUCTURE_TYPE, $$0 -> StructureType.JIGSAW);
    public static final Registry<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPE = BuiltInRegistries.registerSimple(Registries.PLACEMENT_MODIFIER_TYPE, $$0 -> PlacementModifierType.COUNT);
    public static final Registry<BlockStateProviderType<?>> BLOCKSTATE_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.BLOCK_STATE_PROVIDER_TYPE, $$0 -> BlockStateProviderType.SIMPLE_STATE_PROVIDER);
    public static final Registry<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPE = BuiltInRegistries.registerSimple(Registries.FOLIAGE_PLACER_TYPE, $$0 -> FoliagePlacerType.BLOB_FOLIAGE_PLACER);
    public static final Registry<TrunkPlacerType<?>> TRUNK_PLACER_TYPE = BuiltInRegistries.registerSimple(Registries.TRUNK_PLACER_TYPE, $$0 -> TrunkPlacerType.STRAIGHT_TRUNK_PLACER);
    public static final Registry<RootPlacerType<?>> ROOT_PLACER_TYPE = BuiltInRegistries.registerSimple(Registries.ROOT_PLACER_TYPE, $$0 -> RootPlacerType.MANGROVE_ROOT_PLACER);
    public static final Registry<TreeDecoratorType<?>> TREE_DECORATOR_TYPE = BuiltInRegistries.registerSimple(Registries.TREE_DECORATOR_TYPE, $$0 -> TreeDecoratorType.LEAVE_VINE);
    public static final Registry<FeatureSizeType<?>> FEATURE_SIZE_TYPE = BuiltInRegistries.registerSimple(Registries.FEATURE_SIZE_TYPE, $$0 -> FeatureSizeType.TWO_LAYERS_FEATURE_SIZE);
    public static final Registry<MapCodec<? extends BiomeSource>> BIOME_SOURCE = BuiltInRegistries.registerSimple(Registries.BIOME_SOURCE, BiomeSources::bootstrap);
    public static final Registry<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATOR = BuiltInRegistries.registerSimple(Registries.CHUNK_GENERATOR, ChunkGenerators::bootstrap);
    public static final Registry<MapCodec<? extends SurfaceRules.ConditionSource>> MATERIAL_CONDITION = BuiltInRegistries.registerSimple(Registries.MATERIAL_CONDITION, SurfaceRules.ConditionSource::bootstrap);
    public static final Registry<MapCodec<? extends SurfaceRules.RuleSource>> MATERIAL_RULE = BuiltInRegistries.registerSimple(Registries.MATERIAL_RULE, SurfaceRules.RuleSource::bootstrap);
    public static final Registry<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPE = BuiltInRegistries.registerSimple(Registries.DENSITY_FUNCTION_TYPE, DensityFunctions::bootstrap);
    public static final Registry<MapCodec<? extends Block>> BLOCK_TYPE = BuiltInRegistries.registerSimple(Registries.BLOCK_TYPE, BlockTypes::bootstrap);
    public static final Registry<StructureProcessorType<?>> STRUCTURE_PROCESSOR = BuiltInRegistries.registerSimple(Registries.STRUCTURE_PROCESSOR, $$0 -> StructureProcessorType.BLOCK_IGNORE);
    public static final Registry<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT = BuiltInRegistries.registerSimple(Registries.STRUCTURE_POOL_ELEMENT, $$0 -> StructurePoolElementType.EMPTY);
    public static final Registry<MapCodec<? extends PoolAliasBinding>> POOL_ALIAS_BINDING_TYPE = BuiltInRegistries.registerSimple(Registries.POOL_ALIAS_BINDING, PoolAliasBindings::bootstrap);
    public static final Registry<DecoratedPotPattern> DECORATED_POT_PATTERN = BuiltInRegistries.registerSimple(Registries.DECORATED_POT_PATTERN, DecoratedPotPatterns::bootstrap);
    public static final Registry<CreativeModeTab> CREATIVE_MODE_TAB = BuiltInRegistries.registerSimple(Registries.CREATIVE_MODE_TAB, CreativeModeTabs::bootstrap);
    public static final Registry<CriterionTrigger<?>> TRIGGER_TYPES = BuiltInRegistries.registerSimple(Registries.TRIGGER_TYPE, CriteriaTriggers::bootstrap);
    public static final Registry<NumberFormatType<?>> NUMBER_FORMAT_TYPE = BuiltInRegistries.registerSimple(Registries.NUMBER_FORMAT_TYPE, NumberFormatTypes::bootstrap);
    public static final Registry<DataComponentType<?>> DATA_COMPONENT_TYPE = BuiltInRegistries.registerSimple(Registries.DATA_COMPONENT_TYPE, DataComponents::bootstrap);
    public static final Registry<GameRule<?>> GAME_RULE = BuiltInRegistries.registerSimple(Registries.GAME_RULE, GameRules::bootstrap);
    public static final Registry<MapCodec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATE_TYPE = BuiltInRegistries.registerSimple(Registries.ENTITY_SUB_PREDICATE_TYPE, EntitySubPredicates::bootstrap);
    public static final Registry<DataComponentPredicate.Type<?>> DATA_COMPONENT_PREDICATE_TYPE = BuiltInRegistries.registerSimple(Registries.DATA_COMPONENT_PREDICATE_TYPE, DataComponentPredicates::bootstrap);
    public static final Registry<MapDecorationType> MAP_DECORATION_TYPE = BuiltInRegistries.registerSimple(Registries.MAP_DECORATION_TYPE, MapDecorationTypes::bootstrap);
    public static final Registry<DataComponentType<?>> ENCHANTMENT_EFFECT_COMPONENT_TYPE = BuiltInRegistries.registerSimple(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, EnchantmentEffectComponents::bootstrap);
    public static final Registry<MapCodec<? extends LevelBasedValue>> ENCHANTMENT_LEVEL_BASED_VALUE_TYPE = BuiltInRegistries.registerSimple(Registries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE, LevelBasedValue::bootstrap);
    public static final Registry<MapCodec<? extends EnchantmentEntityEffect>> ENCHANTMENT_ENTITY_EFFECT_TYPE = BuiltInRegistries.registerSimple(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, EnchantmentEntityEffect::bootstrap);
    public static final Registry<MapCodec<? extends EnchantmentLocationBasedEffect>> ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE = BuiltInRegistries.registerSimple(Registries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE, EnchantmentLocationBasedEffect::bootstrap);
    public static final Registry<MapCodec<? extends EnchantmentValueEffect>> ENCHANTMENT_VALUE_EFFECT_TYPE = BuiltInRegistries.registerSimple(Registries.ENCHANTMENT_VALUE_EFFECT_TYPE, EnchantmentValueEffect::bootstrap);
    public static final Registry<MapCodec<? extends EnchantmentProvider>> ENCHANTMENT_PROVIDER_TYPE = BuiltInRegistries.registerSimple(Registries.ENCHANTMENT_PROVIDER_TYPE, EnchantmentProviderTypes::bootstrap);
    public static final Registry<ConsumeEffect.Type<?>> CONSUME_EFFECT_TYPE = BuiltInRegistries.registerSimple(Registries.CONSUME_EFFECT_TYPE, $$0 -> ConsumeEffect.Type.APPLY_EFFECTS);
    public static final Registry<RecipeDisplay.Type<?>> RECIPE_DISPLAY = BuiltInRegistries.registerSimple(Registries.RECIPE_DISPLAY, RecipeDisplays::bootstrap);
    public static final Registry<SlotDisplay.Type<?>> SLOT_DISPLAY = BuiltInRegistries.registerSimple(Registries.SLOT_DISPLAY, SlotDisplays::bootstrap);
    public static final Registry<RecipeBookCategory> RECIPE_BOOK_CATEGORY = BuiltInRegistries.registerSimple(Registries.RECIPE_BOOK_CATEGORY, RecipeBookCategories::bootstrap);
    public static final Registry<TicketType> TICKET_TYPE = BuiltInRegistries.registerSimple(Registries.TICKET_TYPE, $$0 -> TicketType.UNKNOWN);
    public static final Registry<IncomingRpcMethod<?, ?>> INCOMING_RPC_METHOD = BuiltInRegistries.registerSimple(Registries.INCOMING_RPC_METHOD, IncomingRpcMethods::bootstrap);
    public static final Registry<OutgoingRpcMethod<?, ?>> OUTGOING_RPC_METHOD = BuiltInRegistries.registerSimple(Registries.OUTGOING_RPC_METHOD, $$0 -> OutgoingRpcMethods.SERVER_STARTED);
    public static final Registry<MapCodec<? extends TestEnvironmentDefinition>> TEST_ENVIRONMENT_DEFINITION_TYPE = BuiltInRegistries.registerSimple(Registries.TEST_ENVIRONMENT_DEFINITION_TYPE, TestEnvironmentDefinition::bootstrap);
    public static final Registry<MapCodec<? extends GameTestInstance>> TEST_INSTANCE_TYPE = BuiltInRegistries.registerSimple(Registries.TEST_INSTANCE_TYPE, GameTestInstance::bootstrap);
    public static final Registry<MapCodec<? extends SpawnCondition>> SPAWN_CONDITION_TYPE = BuiltInRegistries.registerSimple(Registries.SPAWN_CONDITION_TYPE, SpawnConditions::bootstrap);
    public static final Registry<MapCodec<? extends Dialog>> DIALOG_TYPE = BuiltInRegistries.registerSimple(Registries.DIALOG_TYPE, DialogTypes::bootstrap);
    public static final Registry<MapCodec<? extends Action>> DIALOG_ACTION_TYPE = BuiltInRegistries.registerSimple(Registries.DIALOG_ACTION_TYPE, ActionTypes::bootstrap);
    public static final Registry<MapCodec<? extends InputControl>> INPUT_CONTROL_TYPE = BuiltInRegistries.registerSimple(Registries.INPUT_CONTROL_TYPE, InputControlTypes::bootstrap);
    public static final Registry<MapCodec<? extends DialogBody>> DIALOG_BODY_TYPE = BuiltInRegistries.registerSimple(Registries.DIALOG_BODY_TYPE, DialogBodyTypes::bootstrap);
    public static final Registry<MapCodec<? extends Permission>> PERMISSION_TYPE = BuiltInRegistries.registerSimple(Registries.PERMISSION_TYPE, PermissionTypes::bootstrap);
    public static final Registry<MapCodec<? extends PermissionCheck>> PERMISSION_CHECK_TYPE = BuiltInRegistries.registerSimple(Registries.PERMISSION_CHECK_TYPE, PermissionCheckTypes::bootstrap);
    public static final Registry<EnvironmentAttribute<?>> ENVIRONMENT_ATTRIBUTE = BuiltInRegistries.registerSimple(Registries.ENVIRONMENT_ATTRIBUTE, EnvironmentAttributes::bootstrap);
    public static final Registry<AttributeType<?>> ATTRIBUTE_TYPE = BuiltInRegistries.registerSimple(Registries.ATTRIBUTE_TYPE, AttributeTypes::bootstrap);
    public static final Registry<MapCodec<? extends SlotSource>> SLOT_SOURCE_TYPE = BuiltInRegistries.registerSimple(Registries.SLOT_SOURCE_TYPE, SlotSources::bootstrap);
    public static final Registry<Consumer<GameTestHelper>> TEST_FUNCTION = BuiltInRegistries.registerSimple(Registries.TEST_FUNCTION, BuiltinTestFunctions::bootstrap);
    public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;

    private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> $$0, RegistryBootstrap<T> $$1) {
        return BuiltInRegistries.internalRegister($$0, new MappedRegistry($$0, Lifecycle.stable(), false), $$1);
    }

    private static <T> Registry<T> registerSimpleWithIntrusiveHolders(ResourceKey<? extends Registry<T>> $$0, RegistryBootstrap<T> $$1) {
        return BuiltInRegistries.internalRegister($$0, new MappedRegistry($$0, Lifecycle.stable(), true), $$1);
    }

    private static <T> DefaultedRegistry<T> registerDefaulted(ResourceKey<? extends Registry<T>> $$0, String $$1, RegistryBootstrap<T> $$2) {
        return BuiltInRegistries.internalRegister($$0, new DefaultedMappedRegistry($$1, $$0, Lifecycle.stable(), false), $$2);
    }

    private static <T> DefaultedRegistry<T> registerDefaultedWithIntrusiveHolders(ResourceKey<? extends Registry<T>> $$0, String $$1, RegistryBootstrap<T> $$2) {
        return BuiltInRegistries.internalRegister($$0, new DefaultedMappedRegistry($$1, $$0, Lifecycle.stable(), true), $$2);
    }

    private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey<? extends Registry<T>> $$0, R $$1, RegistryBootstrap<T> $$2) {
        Bootstrap.checkBootstrapCalled(() -> "registry " + String.valueOf($$0.identifier()));
        Identifier $$3 = $$0.identifier();
        LOADERS.put($$3, () -> $$2.run($$1));
        WRITABLE_REGISTRY.register($$0, $$1, RegistrationInfo.BUILT_IN);
        return $$1;
    }

    public static void bootStrap() {
        BuiltInRegistries.createContents();
        BuiltInRegistries.freeze();
        BuiltInRegistries.validate(REGISTRY);
    }

    private static void createContents() {
        LOADERS.forEach(($$0, $$1) -> {
            if ($$1.get() == null) {
                LOGGER.error("Unable to bootstrap registry '{}'", $$0);
            }
        });
    }

    private static void freeze() {
        REGISTRY.freeze();
        for (Registry registry : REGISTRY) {
            BuiltInRegistries.bindBootstrappedTagsToEmpty(registry);
            registry.freeze();
        }
    }

    private static <T extends Registry<?>> void validate(Registry<T> $$0) {
        $$0.forEach($$1 -> {
            if ($$1.keySet().isEmpty()) {
                Util.logAndPauseIfInIde("Registry '" + String.valueOf($$0.getKey($$1)) + "' was empty after loading");
            }
            if ($$1 instanceof DefaultedRegistry) {
                Identifier $$2 = ((DefaultedRegistry)$$1).getDefaultKey();
                Objects.requireNonNull($$1.getValue($$2), "Missing default of DefaultedMappedRegistry: " + String.valueOf($$2));
            }
        });
    }

    public static <T> HolderGetter<T> acquireBootstrapRegistrationLookup(Registry<T> $$0) {
        return ((WritableRegistry)$$0).createRegistrationLookup();
    }

    private static void bindBootstrappedTagsToEmpty(Registry<?> $$0) {
        ((MappedRegistry)$$0).bindAllTagsToEmpty();
    }

    @FunctionalInterface
    static interface RegistryBootstrap<T> {
        public Object run(Registry<T> var1);
    }
}

