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
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.item.consume_effects.ConsumeEffect.Type;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplays;
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
import net.minecraft.world.level.levelgen.SurfaceRules.ConditionSource;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
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
   private static final net.minecraft.core.WritableRegistry<net.minecraft.core.WritableRegistry<?>> WRITABLE_REGISTRY = new net.minecraft.core.MappedRegistry<>(
      ResourceKey.createRegistryKey(Registries.ROOT_REGISTRY_NAME), Lifecycle.stable()
   );
   public static final net.minecraft.core.DefaultedRegistry<GameEvent> GAME_EVENT = registerDefaulted(Registries.GAME_EVENT, "step", GameEvent::bootstrap);
   public static final net.minecraft.core.Registry<SoundEvent> SOUND_EVENT = registerSimple(Registries.SOUND_EVENT, $$0 -> SoundEvents.ITEM_PICKUP);
   public static final net.minecraft.core.DefaultedRegistry<Fluid> FLUID = registerDefaultedWithIntrusiveHolders(Registries.FLUID, "empty", $$0 -> Fluids.EMPTY);
   public static final net.minecraft.core.Registry<MobEffect> MOB_EFFECT = registerSimple(Registries.MOB_EFFECT, MobEffects::bootstrap);
   public static final net.minecraft.core.DefaultedRegistry<Block> BLOCK = registerDefaultedWithIntrusiveHolders(Registries.BLOCK, "air", $$0 -> Blocks.AIR);
   public static final net.minecraft.core.Registry<DebugSubscription<?>> DEBUG_SUBSCRIPTION = registerSimple(
      Registries.DEBUG_SUBSCRIPTION, DebugSubscriptions::bootstrap
   );
   public static final net.minecraft.core.DefaultedRegistry<EntityType<?>> ENTITY_TYPE = registerDefaultedWithIntrusiveHolders(
      Registries.ENTITY_TYPE, "pig", $$0 -> EntityType.PIG
   );
   public static final net.minecraft.core.DefaultedRegistry<Item> ITEM = registerDefaultedWithIntrusiveHolders(Registries.ITEM, "air", $$0 -> Items.AIR);
   public static final net.minecraft.core.Registry<Potion> POTION = registerSimple(Registries.POTION, Potions::bootstrap);
   public static final net.minecraft.core.Registry<ParticleType<?>> PARTICLE_TYPE = registerSimple(Registries.PARTICLE_TYPE, $$0 -> ParticleTypes.BLOCK);
   public static final net.minecraft.core.Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = registerSimpleWithIntrusiveHolders(
      Registries.BLOCK_ENTITY_TYPE, $$0 -> BlockEntityType.FURNACE
   );
   public static final net.minecraft.core.Registry<Identifier> CUSTOM_STAT = registerSimple(Registries.CUSTOM_STAT, $$0 -> Stats.JUMP);
   public static final net.minecraft.core.DefaultedRegistry<ChunkStatus> CHUNK_STATUS = registerDefaulted(
      Registries.CHUNK_STATUS, "empty", $$0 -> ChunkStatus.EMPTY
   );
   public static final net.minecraft.core.Registry<RuleTestType<?>> RULE_TEST = registerSimple(Registries.RULE_TEST, $$0 -> RuleTestType.ALWAYS_TRUE_TEST);
   public static final net.minecraft.core.Registry<RuleBlockEntityModifierType<?>> RULE_BLOCK_ENTITY_MODIFIER = registerSimple(
      Registries.RULE_BLOCK_ENTITY_MODIFIER, $$0 -> RuleBlockEntityModifierType.PASSTHROUGH
   );
   public static final net.minecraft.core.Registry<PosRuleTestType<?>> POS_RULE_TEST = registerSimple(
      Registries.POS_RULE_TEST, $$0 -> PosRuleTestType.ALWAYS_TRUE_TEST
   );
   public static final net.minecraft.core.Registry<MenuType<?>> MENU = registerSimple(Registries.MENU, $$0 -> MenuType.ANVIL);
   public static final net.minecraft.core.Registry<RecipeType<?>> RECIPE_TYPE = registerSimple(Registries.RECIPE_TYPE, $$0 -> RecipeType.CRAFTING);
   public static final net.minecraft.core.Registry<RecipeSerializer<?>> RECIPE_SERIALIZER = registerSimple(
      Registries.RECIPE_SERIALIZER, $$0 -> RecipeSerializer.SHAPELESS_RECIPE
   );
   public static final net.minecraft.core.Registry<Attribute> ATTRIBUTE = registerSimple(Registries.ATTRIBUTE, Attributes::bootstrap);
   public static final net.minecraft.core.Registry<PositionSourceType<?>> POSITION_SOURCE_TYPE = registerSimple(
      Registries.POSITION_SOURCE_TYPE, $$0 -> PositionSourceType.BLOCK
   );
   public static final net.minecraft.core.Registry<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPE = registerSimple(
      Registries.COMMAND_ARGUMENT_TYPE, ArgumentTypeInfos::bootstrap
   );
   public static final net.minecraft.core.Registry<StatType<?>> STAT_TYPE = registerSimple(Registries.STAT_TYPE, $$0 -> Stats.ITEM_USED);
   public static final net.minecraft.core.DefaultedRegistry<VillagerType> VILLAGER_TYPE = registerDefaulted(
      Registries.VILLAGER_TYPE, "plains", VillagerType::bootstrap
   );
   public static final net.minecraft.core.DefaultedRegistry<VillagerProfession> VILLAGER_PROFESSION = registerDefaulted(
      Registries.VILLAGER_PROFESSION, "none", VillagerProfession::bootstrap
   );
   public static final net.minecraft.core.Registry<PoiType> POINT_OF_INTEREST_TYPE = registerSimple(Registries.POINT_OF_INTEREST_TYPE, PoiTypes::bootstrap);
   public static final net.minecraft.core.DefaultedRegistry<MemoryModuleType<?>> MEMORY_MODULE_TYPE = registerDefaulted(
      Registries.MEMORY_MODULE_TYPE, "dummy", $$0 -> MemoryModuleType.DUMMY
   );
   public static final net.minecraft.core.DefaultedRegistry<SensorType<?>> SENSOR_TYPE = registerDefaulted(
      Registries.SENSOR_TYPE, "dummy", $$0 -> SensorType.DUMMY
   );
   public static final net.minecraft.core.Registry<Activity> ACTIVITY = registerSimple(Registries.ACTIVITY, $$0 -> Activity.IDLE);
   public static final net.minecraft.core.Registry<LootPoolEntryType> LOOT_POOL_ENTRY_TYPE = registerSimple(
      Registries.LOOT_POOL_ENTRY_TYPE, $$0 -> LootPoolEntries.EMPTY
   );
   public static final net.minecraft.core.Registry<LootItemFunctionType<?>> LOOT_FUNCTION_TYPE = registerSimple(
      Registries.LOOT_FUNCTION_TYPE, $$0 -> LootItemFunctions.SET_COUNT
   );
   public static final net.minecraft.core.Registry<LootItemConditionType> LOOT_CONDITION_TYPE = registerSimple(
      Registries.LOOT_CONDITION_TYPE, $$0 -> LootItemConditions.INVERTED
   );
   public static final net.minecraft.core.Registry<LootNumberProviderType> LOOT_NUMBER_PROVIDER_TYPE = registerSimple(
      Registries.LOOT_NUMBER_PROVIDER_TYPE, $$0 -> NumberProviders.CONSTANT
   );
   public static final net.minecraft.core.Registry<LootNbtProviderType> LOOT_NBT_PROVIDER_TYPE = registerSimple(
      Registries.LOOT_NBT_PROVIDER_TYPE, $$0 -> NbtProviders.CONTEXT
   );
   public static final net.minecraft.core.Registry<LootScoreProviderType> LOOT_SCORE_PROVIDER_TYPE = registerSimple(
      Registries.LOOT_SCORE_PROVIDER_TYPE, $$0 -> ScoreboardNameProviders.CONTEXT
   );
   public static final net.minecraft.core.Registry<FloatProviderType<?>> FLOAT_PROVIDER_TYPE = registerSimple(
      Registries.FLOAT_PROVIDER_TYPE, $$0 -> FloatProviderType.CONSTANT
   );
   public static final net.minecraft.core.Registry<IntProviderType<?>> INT_PROVIDER_TYPE = registerSimple(
      Registries.INT_PROVIDER_TYPE, $$0 -> IntProviderType.CONSTANT
   );
   public static final net.minecraft.core.Registry<HeightProviderType<?>> HEIGHT_PROVIDER_TYPE = registerSimple(
      Registries.HEIGHT_PROVIDER_TYPE, $$0 -> HeightProviderType.CONSTANT
   );
   public static final net.minecraft.core.Registry<BlockPredicateType<?>> BLOCK_PREDICATE_TYPE = registerSimple(
      Registries.BLOCK_PREDICATE_TYPE, $$0 -> BlockPredicateType.NOT
   );
   public static final net.minecraft.core.Registry<WorldCarver<?>> CARVER = registerSimple(Registries.CARVER, $$0 -> WorldCarver.CAVE);
   public static final net.minecraft.core.Registry<Feature<?>> FEATURE = registerSimple(Registries.FEATURE, $$0 -> Feature.ORE);
   public static final net.minecraft.core.Registry<StructurePlacementType<?>> STRUCTURE_PLACEMENT = registerSimple(
      Registries.STRUCTURE_PLACEMENT, $$0 -> StructurePlacementType.RANDOM_SPREAD
   );
   public static final net.minecraft.core.Registry<StructurePieceType> STRUCTURE_PIECE = registerSimple(
      Registries.STRUCTURE_PIECE, $$0 -> StructurePieceType.MINE_SHAFT_ROOM
   );
   public static final net.minecraft.core.Registry<StructureType<?>> STRUCTURE_TYPE = registerSimple(Registries.STRUCTURE_TYPE, $$0 -> StructureType.JIGSAW);
   public static final net.minecraft.core.Registry<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPE = registerSimple(
      Registries.PLACEMENT_MODIFIER_TYPE, $$0 -> PlacementModifierType.COUNT
   );
   public static final net.minecraft.core.Registry<BlockStateProviderType<?>> BLOCKSTATE_PROVIDER_TYPE = registerSimple(
      Registries.BLOCK_STATE_PROVIDER_TYPE, $$0 -> BlockStateProviderType.SIMPLE_STATE_PROVIDER
   );
   public static final net.minecraft.core.Registry<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPE = registerSimple(
      Registries.FOLIAGE_PLACER_TYPE, $$0 -> FoliagePlacerType.BLOB_FOLIAGE_PLACER
   );
   public static final net.minecraft.core.Registry<TrunkPlacerType<?>> TRUNK_PLACER_TYPE = registerSimple(
      Registries.TRUNK_PLACER_TYPE, $$0 -> TrunkPlacerType.STRAIGHT_TRUNK_PLACER
   );
   public static final net.minecraft.core.Registry<RootPlacerType<?>> ROOT_PLACER_TYPE = registerSimple(
      Registries.ROOT_PLACER_TYPE, $$0 -> RootPlacerType.MANGROVE_ROOT_PLACER
   );
   public static final net.minecraft.core.Registry<TreeDecoratorType<?>> TREE_DECORATOR_TYPE = registerSimple(
      Registries.TREE_DECORATOR_TYPE, $$0 -> TreeDecoratorType.LEAVE_VINE
   );
   public static final net.minecraft.core.Registry<FeatureSizeType<?>> FEATURE_SIZE_TYPE = registerSimple(
      Registries.FEATURE_SIZE_TYPE, $$0 -> FeatureSizeType.TWO_LAYERS_FEATURE_SIZE
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends BiomeSource>> BIOME_SOURCE = registerSimple(
      Registries.BIOME_SOURCE, BiomeSources::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATOR = registerSimple(
      Registries.CHUNK_GENERATOR, ChunkGenerators::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends ConditionSource>> MATERIAL_CONDITION = registerSimple(
      Registries.MATERIAL_CONDITION, ConditionSource::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends RuleSource>> MATERIAL_RULE = registerSimple(
      Registries.MATERIAL_RULE, RuleSource::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPE = registerSimple(
      Registries.DENSITY_FUNCTION_TYPE, DensityFunctions::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends Block>> BLOCK_TYPE = registerSimple(Registries.BLOCK_TYPE, BlockTypes::bootstrap);
   public static final net.minecraft.core.Registry<StructureProcessorType<?>> STRUCTURE_PROCESSOR = registerSimple(
      Registries.STRUCTURE_PROCESSOR, $$0 -> StructureProcessorType.BLOCK_IGNORE
   );
   public static final net.minecraft.core.Registry<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT = registerSimple(
      Registries.STRUCTURE_POOL_ELEMENT, $$0 -> StructurePoolElementType.EMPTY
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends PoolAliasBinding>> POOL_ALIAS_BINDING_TYPE = registerSimple(
      Registries.POOL_ALIAS_BINDING, PoolAliasBindings::bootstrap
   );
   public static final net.minecraft.core.Registry<DecoratedPotPattern> DECORATED_POT_PATTERN = registerSimple(
      Registries.DECORATED_POT_PATTERN, DecoratedPotPatterns::bootstrap
   );
   public static final net.minecraft.core.Registry<CreativeModeTab> CREATIVE_MODE_TAB = registerSimple(
      Registries.CREATIVE_MODE_TAB, CreativeModeTabs::bootstrap
   );
   public static final net.minecraft.core.Registry<CriterionTrigger<?>> TRIGGER_TYPES = registerSimple(Registries.TRIGGER_TYPE, CriteriaTriggers::bootstrap);
   public static final net.minecraft.core.Registry<NumberFormatType<?>> NUMBER_FORMAT_TYPE = registerSimple(
      Registries.NUMBER_FORMAT_TYPE, NumberFormatTypes::bootstrap
   );
   public static final net.minecraft.core.Registry<DataComponentType<?>> DATA_COMPONENT_TYPE = registerSimple(
      Registries.DATA_COMPONENT_TYPE, DataComponents::bootstrap
   );
   public static final net.minecraft.core.Registry<GameRule<?>> GAME_RULE = registerSimple(Registries.GAME_RULE, GameRules::bootstrap);
   public static final net.minecraft.core.Registry<MapCodec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATE_TYPE = registerSimple(
      Registries.ENTITY_SUB_PREDICATE_TYPE, EntitySubPredicates::bootstrap
   );
   public static final net.minecraft.core.Registry<DataComponentPredicate.Type<?>> DATA_COMPONENT_PREDICATE_TYPE = registerSimple(
      Registries.DATA_COMPONENT_PREDICATE_TYPE, DataComponentPredicates::bootstrap
   );
   public static final net.minecraft.core.Registry<MapDecorationType> MAP_DECORATION_TYPE = registerSimple(
      Registries.MAP_DECORATION_TYPE, MapDecorationTypes::bootstrap
   );
   public static final net.minecraft.core.Registry<DataComponentType<?>> ENCHANTMENT_EFFECT_COMPONENT_TYPE = registerSimple(
      Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, EnchantmentEffectComponents::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends LevelBasedValue>> ENCHANTMENT_LEVEL_BASED_VALUE_TYPE = registerSimple(
      Registries.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE, LevelBasedValue::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends EnchantmentEntityEffect>> ENCHANTMENT_ENTITY_EFFECT_TYPE = registerSimple(
      Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, EnchantmentEntityEffect::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends EnchantmentLocationBasedEffect>> ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE = registerSimple(
      Registries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE, EnchantmentLocationBasedEffect::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends EnchantmentValueEffect>> ENCHANTMENT_VALUE_EFFECT_TYPE = registerSimple(
      Registries.ENCHANTMENT_VALUE_EFFECT_TYPE, EnchantmentValueEffect::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends EnchantmentProvider>> ENCHANTMENT_PROVIDER_TYPE = registerSimple(
      Registries.ENCHANTMENT_PROVIDER_TYPE, EnchantmentProviderTypes::bootstrap
   );
   public static final net.minecraft.core.Registry<Type<?>> CONSUME_EFFECT_TYPE = registerSimple(Registries.CONSUME_EFFECT_TYPE, $$0 -> Type.APPLY_EFFECTS);
   public static final net.minecraft.core.Registry<net.minecraft.world.item.crafting.display.RecipeDisplay.Type<?>> RECIPE_DISPLAY = registerSimple(
      Registries.RECIPE_DISPLAY, RecipeDisplays::bootstrap
   );
   public static final net.minecraft.core.Registry<net.minecraft.world.item.crafting.display.SlotDisplay.Type<?>> SLOT_DISPLAY = registerSimple(
      Registries.SLOT_DISPLAY, SlotDisplays::bootstrap
   );
   public static final net.minecraft.core.Registry<RecipeBookCategory> RECIPE_BOOK_CATEGORY = registerSimple(
      Registries.RECIPE_BOOK_CATEGORY, RecipeBookCategories::bootstrap
   );
   public static final net.minecraft.core.Registry<TicketType> TICKET_TYPE = registerSimple(Registries.TICKET_TYPE, $$0 -> TicketType.UNKNOWN);
   public static final net.minecraft.core.Registry<IncomingRpcMethod<?, ?>> INCOMING_RPC_METHOD = registerSimple(
      Registries.INCOMING_RPC_METHOD, IncomingRpcMethods::bootstrap
   );
   public static final net.minecraft.core.Registry<OutgoingRpcMethod<?, ?>> OUTGOING_RPC_METHOD = registerSimple(
      Registries.OUTGOING_RPC_METHOD, $$0 -> OutgoingRpcMethods.SERVER_STARTED
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends TestEnvironmentDefinition>> TEST_ENVIRONMENT_DEFINITION_TYPE = registerSimple(
      Registries.TEST_ENVIRONMENT_DEFINITION_TYPE, TestEnvironmentDefinition::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends GameTestInstance>> TEST_INSTANCE_TYPE = registerSimple(
      Registries.TEST_INSTANCE_TYPE, GameTestInstance::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends SpawnCondition>> SPAWN_CONDITION_TYPE = registerSimple(
      Registries.SPAWN_CONDITION_TYPE, SpawnConditions::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends Dialog>> DIALOG_TYPE = registerSimple(Registries.DIALOG_TYPE, DialogTypes::bootstrap);
   public static final net.minecraft.core.Registry<MapCodec<? extends Action>> DIALOG_ACTION_TYPE = registerSimple(
      Registries.DIALOG_ACTION_TYPE, ActionTypes::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends InputControl>> INPUT_CONTROL_TYPE = registerSimple(
      Registries.INPUT_CONTROL_TYPE, InputControlTypes::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends DialogBody>> DIALOG_BODY_TYPE = registerSimple(
      Registries.DIALOG_BODY_TYPE, DialogBodyTypes::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends Permission>> PERMISSION_TYPE = registerSimple(
      Registries.PERMISSION_TYPE, PermissionTypes::bootstrap
   );
   public static final net.minecraft.core.Registry<MapCodec<? extends PermissionCheck>> PERMISSION_CHECK_TYPE = registerSimple(
      Registries.PERMISSION_CHECK_TYPE, PermissionCheckTypes::bootstrap
   );
   public static final net.minecraft.core.Registry<EnvironmentAttribute<?>> ENVIRONMENT_ATTRIBUTE = registerSimple(
      Registries.ENVIRONMENT_ATTRIBUTE, EnvironmentAttributes::bootstrap
   );
   public static final net.minecraft.core.Registry<AttributeType<?>> ATTRIBUTE_TYPE = registerSimple(Registries.ATTRIBUTE_TYPE, AttributeTypes::bootstrap);
   public static final net.minecraft.core.Registry<MapCodec<? extends SlotSource>> SLOT_SOURCE_TYPE = registerSimple(
      Registries.SLOT_SOURCE_TYPE, SlotSources::bootstrap
   );
   public static final net.minecraft.core.Registry<Consumer<GameTestHelper>> TEST_FUNCTION = registerSimple(
      Registries.TEST_FUNCTION, BuiltinTestFunctions::bootstrap
   );
   public static final net.minecraft.core.Registry<? extends net.minecraft.core.Registry<?>> REGISTRY = WRITABLE_REGISTRY;

   private static <T> net.minecraft.core.Registry<T> registerSimple(
      ResourceKey<? extends net.minecraft.core.Registry<T>> $$0, BuiltInRegistries.RegistryBootstrap<T> $$1
   ) {
      return internalRegister($$0, new net.minecraft.core.MappedRegistry<>($$0, Lifecycle.stable(), false), $$1);
   }

   private static <T> net.minecraft.core.Registry<T> registerSimpleWithIntrusiveHolders(
      ResourceKey<? extends net.minecraft.core.Registry<T>> $$0, BuiltInRegistries.RegistryBootstrap<T> $$1
   ) {
      return internalRegister($$0, new net.minecraft.core.MappedRegistry<>($$0, Lifecycle.stable(), true), $$1);
   }

   private static <T> net.minecraft.core.DefaultedRegistry<T> registerDefaulted(
      ResourceKey<? extends net.minecraft.core.Registry<T>> $$0, String $$1, BuiltInRegistries.RegistryBootstrap<T> $$2
   ) {
      return internalRegister($$0, new net.minecraft.core.DefaultedMappedRegistry<>($$1, $$0, Lifecycle.stable(), false), $$2);
   }

   private static <T> net.minecraft.core.DefaultedRegistry<T> registerDefaultedWithIntrusiveHolders(
      ResourceKey<? extends net.minecraft.core.Registry<T>> $$0, String $$1, BuiltInRegistries.RegistryBootstrap<T> $$2
   ) {
      return internalRegister($$0, new net.minecraft.core.DefaultedMappedRegistry<>($$1, $$0, Lifecycle.stable(), true), $$2);
   }

   private static <T, R extends net.minecraft.core.WritableRegistry<T>> R internalRegister(
      ResourceKey<? extends net.minecraft.core.Registry<T>> $$0, R $$1, BuiltInRegistries.RegistryBootstrap<T> $$2
   ) {
      Bootstrap.checkBootstrapCalled(() -> "registry " + $$0.identifier());
      Identifier $$3 = $$0.identifier();
      LOADERS.put($$3, () -> $$2.run($$1));
      WRITABLE_REGISTRY.register((ResourceKey<net.minecraft.core.WritableRegistry<?>>)$$0, $$1, net.minecraft.core.RegistrationInfo.BUILT_IN);
      return $$1;
   }

   public static void bootStrap() {
      createContents();
      freeze();
      validate(REGISTRY);
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

      for (net.minecraft.core.Registry<?> $$0 : REGISTRY) {
         bindBootstrappedTagsToEmpty($$0);
         $$0.freeze();
      }
   }

   private static <T extends net.minecraft.core.Registry<?>> void validate(net.minecraft.core.Registry<T> $$0) {
      $$0.forEach($$1 -> {
         if ($$1.keySet().isEmpty()) {
            Util.logAndPauseIfInIde("Registry '" + $$0.getKey((T)$$1) + "' was empty after loading");
         }

         if ($$1 instanceof net.minecraft.core.DefaultedRegistry) {
            Identifier $$2 = ((net.minecraft.core.DefaultedRegistry)$$1).getDefaultKey();
            Objects.requireNonNull($$1.getValue($$2), "Missing default of DefaultedMappedRegistry: " + $$2);
         }
      });
   }

   public static <T> net.minecraft.core.HolderGetter<T> acquireBootstrapRegistrationLookup(net.minecraft.core.Registry<T> $$0) {
      return ((net.minecraft.core.WritableRegistry)$$0).createRegistrationLookup();
   }

   private static void bindBootstrappedTagsToEmpty(net.minecraft.core.Registry<?> $$0) {
      ((net.minecraft.core.MappedRegistry)$$0).bindAllTagsToEmpty();
   }

   @FunctionalInterface
   interface RegistryBootstrap<T> {
      Object run(net.minecraft.core.Registry<T> var1);
   }
}
