package net.minecraft.data.loot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds.Ints;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.MossyCarpetBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public abstract class BlockLootSubProvider implements LootTableSubProvider {
   protected final Provider registries;
   protected final Set<Item> explosionResistant;
   protected final FeatureFlagSet enabledFeatures;
   protected final Map<ResourceKey<LootTable>, Builder> map;
   protected static final float[] NORMAL_LEAVES_SAPLING_CHANCES = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
   private static final float[] NORMAL_LEAVES_STICK_CHANCES = new float[]{0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F};

   protected net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder hasSilkTouch() {
      return MatchTool.toolMatches(
         net.minecraft.advancements.criterion.ItemPredicate.Builder.item()
            .withComponents(
               net.minecraft.advancements.criterion.DataComponentMatchers.Builder.components()
                  .partial(
                     DataComponentPredicates.ENCHANTMENTS,
                     EnchantmentsPredicate.enchantments(
                        List.of(
                           new EnchantmentPredicate(this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), Ints.atLeast(1))
                        )
                     )
                  )
                  .build()
            )
      );
   }

   protected net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder doesNotHaveSilkTouch() {
      return this.hasSilkTouch().invert();
   }

   protected net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder hasShears() {
      return MatchTool.toolMatches(
         net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of(this.registries.lookupOrThrow(Registries.ITEM), new ItemLike[]{Items.SHEARS})
      );
   }

   private net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder hasShearsOrSilkTouch() {
      return this.hasShears().or(this.hasSilkTouch());
   }

   private net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder doesNotHaveShearsOrSilkTouch() {
      return this.hasShearsOrSilkTouch().invert();
   }

   protected BlockLootSubProvider(Set<Item> $$0, FeatureFlagSet $$1, Provider $$2) {
      this($$0, $$1, new HashMap<>(), $$2);
   }

   protected BlockLootSubProvider(Set<Item> $$0, FeatureFlagSet $$1, Map<ResourceKey<LootTable>, Builder> $$2, Provider $$3) {
      this.explosionResistant = $$0;
      this.enabledFeatures = $$1;
      this.map = $$2;
      this.registries = $$3;
   }

   protected <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike $$0, FunctionUserBuilder<T> $$1) {
      return (T)(!this.explosionResistant.contains($$0.asItem()) ? $$1.apply(ApplyExplosionDecay.explosionDecay()) : $$1.unwrap());
   }

   protected <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike $$0, ConditionUserBuilder<T> $$1) {
      return (T)(!this.explosionResistant.contains($$0.asItem()) ? $$1.when(ExplosionCondition.survivesExplosion()) : $$1.unwrap());
   }

   public Builder createSingleItemTable(ItemLike $$0) {
      return LootTable.lootTable()
         .withPool(this.applyExplosionCondition($$0, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem($$0))));
   }

   private static Builder createSelfDropDispatchTable(
      Block $$0,
      net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder $$1,
      net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder<?> $$2
   ) {
      return LootTable.lootTable()
         .withPool(
            LootPool.lootPool()
               .setRolls(ConstantValue.exactly(1.0F))
               .add(((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder)LootItem.lootTableItem($$0).when($$1)).otherwise($$2))
         );
   }

   protected Builder createSilkTouchDispatchTable(Block $$0, net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder<?> $$1) {
      return createSelfDropDispatchTable($$0, this.hasSilkTouch(), $$1);
   }

   protected Builder createShearsDispatchTable(Block $$0, net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder<?> $$1) {
      return createSelfDropDispatchTable($$0, this.hasShears(), $$1);
   }

   protected Builder createSilkTouchOrShearsDispatchTable(Block $$0, net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder<?> $$1) {
      return createSelfDropDispatchTable($$0, this.hasShearsOrSilkTouch(), $$1);
   }

   protected Builder createSingleItemTableWithSilkTouch(Block $$0, ItemLike $$1) {
      return this.createSilkTouchDispatchTable($$0, this.applyExplosionCondition($$0, LootItem.lootTableItem($$1)));
   }

   protected Builder createSingleItemTable(ItemLike $$0, NumberProvider $$1) {
      return LootTable.lootTable()
         .withPool(
            LootPool.lootPool()
               .setRolls(ConstantValue.exactly(1.0F))
               .add(this.applyExplosionDecay($$0, LootItem.lootTableItem($$0).apply(SetItemCountFunction.setCount($$1))))
         );
   }

   protected Builder createSingleItemTableWithSilkTouch(Block $$0, ItemLike $$1, NumberProvider $$2) {
      return this.createSilkTouchDispatchTable($$0, this.applyExplosionDecay($$0, LootItem.lootTableItem($$1).apply(SetItemCountFunction.setCount($$2))));
   }

   private Builder createSilkTouchOnlyTable(ItemLike $$0) {
      return LootTable.lootTable()
         .withPool(LootPool.lootPool().when(this.hasSilkTouch()).setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem($$0)));
   }

   private Builder createPotFlowerItemTable(ItemLike $$0) {
      return LootTable.lootTable()
         .withPool(
            this.applyExplosionCondition(
               Blocks.FLOWER_POT, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Blocks.FLOWER_POT))
            )
         )
         .withPool(this.applyExplosionCondition($$0, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem($$0))));
   }

   protected Builder createSlabItemTable(Block $$0) {
      return LootTable.lootTable()
         .withPool(
            LootPool.lootPool()
               .setRolls(ConstantValue.exactly(1.0F))
               .add(
                  this.applyExplosionDecay(
                     $$0,
                     LootItem.lootTableItem($$0)
                        .apply(
                           SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))
                              .when(
                                 LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0)
                                    .setProperties(
                                       net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties()
                                          .hasProperty(SlabBlock.TYPE, SlabType.DOUBLE)
                                    )
                              )
                        )
                  )
               )
         );
   }

   protected <T extends Comparable<T> & StringRepresentable> Builder createSinglePropConditionTable(Block $$0, Property<T> $$1, T $$2) {
      return LootTable.lootTable()
         .withPool(
            this.applyExplosionCondition(
               $$0,
               LootPool.lootPool()
                  .setRolls(ConstantValue.exactly(1.0F))
                  .add(
                     LootItem.lootTableItem($$0)
                        .when(
                           LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0)
                              .setProperties(net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties().hasProperty($$1, $$2))
                        )
                  )
            )
         );
   }

   protected Builder createNameableBlockEntityTable(Block $$0) {
      return LootTable.lootTable()
         .withPool(
            this.applyExplosionCondition(
               $$0,
               LootPool.lootPool()
                  .setRolls(ConstantValue.exactly(1.0F))
                  .add(
                     LootItem.lootTableItem($$0)
                        .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME))
                  )
            )
         );
   }

   protected Builder createShulkerBoxDrop(Block $$0) {
      return LootTable.lootTable()
         .withPool(
            this.applyExplosionCondition(
               $$0,
               LootPool.lootPool()
                  .setRolls(ConstantValue.exactly(1.0F))
                  .add(
                     LootItem.lootTableItem($$0)
                        .apply(
                           CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                              .include(DataComponents.CUSTOM_NAME)
                              .include(DataComponents.CONTAINER)
                              .include(DataComponents.LOCK)
                              .include(DataComponents.CONTAINER_LOOT)
                        )
                  )
            )
         );
   }

   protected Builder createCopperOreDrops(Block $$0) {
      RegistryLookup<Enchantment> $$1 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
      return this.createSilkTouchDispatchTable(
         $$0,
         this.applyExplosionDecay(
            $$0,
            LootItem.lootTableItem(Items.RAW_COPPER)
               .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
               .apply(ApplyBonusCount.addOreBonusCount($$1.getOrThrow(Enchantments.FORTUNE)))
         )
      );
   }

   protected Builder createLapisOreDrops(Block $$0) {
      RegistryLookup<Enchantment> $$1 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
      return this.createSilkTouchDispatchTable(
         $$0,
         this.applyExplosionDecay(
            $$0,
            LootItem.lootTableItem(Items.LAPIS_LAZULI)
               .apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 9.0F)))
               .apply(ApplyBonusCount.addOreBonusCount($$1.getOrThrow(Enchantments.FORTUNE)))
         )
      );
   }

   protected Builder createRedstoneOreDrops(Block $$0) {
      RegistryLookup<Enchantment> $$1 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
      return this.createSilkTouchDispatchTable(
         $$0,
         this.applyExplosionDecay(
            $$0,
            LootItem.lootTableItem(Items.REDSTONE)
               .apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 5.0F)))
               .apply(ApplyBonusCount.addUniformBonusCount($$1.getOrThrow(Enchantments.FORTUNE)))
         )
      );
   }

   protected Builder createBannerDrop(Block $$0) {
      return LootTable.lootTable()
         .withPool(
            this.applyExplosionCondition(
               $$0,
               LootPool.lootPool()
                  .setRolls(ConstantValue.exactly(1.0F))
                  .add(
                     LootItem.lootTableItem($$0)
                        .apply(
                           CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                              .include(DataComponents.CUSTOM_NAME)
                              .include(DataComponents.ITEM_NAME)
                              .include(DataComponents.TOOLTIP_DISPLAY)
                              .include(DataComponents.BANNER_PATTERNS)
                              .include(DataComponents.RARITY)
                        )
                  )
            )
         );
   }

   protected Builder createBeeNestDrop(Block $$0) {
      return LootTable.lootTable()
         .withPool(
            LootPool.lootPool()
               .when(this.hasSilkTouch())
               .setRolls(ConstantValue.exactly(1.0F))
               .add(
                  LootItem.lootTableItem($$0)
                     .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.BEES))
                     .apply(CopyBlockState.copyState($$0).copy(BeehiveBlock.HONEY_LEVEL))
               )
         );
   }

   protected Builder createBeeHiveDrop(Block $$0) {
      return LootTable.lootTable()
         .withPool(
            LootPool.lootPool()
               .setRolls(ConstantValue.exactly(1.0F))
               .add(
                  ((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder)LootItem.lootTableItem($$0).when(this.hasSilkTouch()))
                     .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.BEES))
                     .apply(CopyBlockState.copyState($$0).copy(BeehiveBlock.HONEY_LEVEL))
                     .otherwise(LootItem.lootTableItem($$0))
               )
         );
   }

   protected Builder createCaveVinesDrop(Block $$0) {
      return LootTable.lootTable()
         .withPool(
            LootPool.lootPool()
               .add(LootItem.lootTableItem(Items.GLOW_BERRIES))
               .when(
                  LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0)
                     .setProperties(net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties().hasProperty(CaveVines.BERRIES, true))
               )
         );
   }

   protected Builder createCopperGolemStatueBlock(Block $$0) {
      return LootTable.lootTable()
         .withPool(
            this.applyExplosionCondition(
               $$0,
               LootPool.lootPool()
                  .setRolls(ConstantValue.exactly(1.0F))
                  .add(
                     LootItem.lootTableItem($$0)
                        .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME))
                        .apply(CopyBlockState.copyState($$0).copy(CopperGolemStatueBlock.POSE))
                  )
            )
         );
   }

   protected Builder createOreDrop(Block $$0, Item $$1) {
      RegistryLookup<Enchantment> $$2 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
      return this.createSilkTouchDispatchTable(
         $$0, this.applyExplosionDecay($$0, LootItem.lootTableItem($$1).apply(ApplyBonusCount.addOreBonusCount($$2.getOrThrow(Enchantments.FORTUNE))))
      );
   }

   protected Builder createMushroomBlockDrop(Block $$0, ItemLike $$1) {
      return this.createSilkTouchDispatchTable(
         $$0,
         this.applyExplosionDecay(
            $$0,
            LootItem.lootTableItem($$1)
               .apply(SetItemCountFunction.setCount(UniformGenerator.between(-6.0F, 2.0F)))
               .apply(LimitCount.limitCount(IntRange.lowerBound(0)))
         )
      );
   }

   protected Builder createGrassDrops(Block $$0) {
      RegistryLookup<Enchantment> $$1 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
      return this.createShearsDispatchTable(
         $$0,
         this.applyExplosionDecay(
            $$0,
            ((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.WHEAT_SEEDS)
                  .when(LootItemRandomChanceCondition.randomChance(0.125F)))
               .apply(ApplyBonusCount.addUniformBonusCount($$1.getOrThrow(Enchantments.FORTUNE), 2))
         )
      );
   }

   public Builder createStemDrops(Block $$0, Item $$1) {
      return LootTable.lootTable()
         .withPool(
            this.applyExplosionDecay(
               $$0,
               LootPool.lootPool()
                  .setRolls(ConstantValue.exactly(1.0F))
                  .add(
                     (net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder)LootItem.lootTableItem($$1)
                        .apply(
                           StemBlock.AGE.getPossibleValues(),
                           $$1x -> SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, ($$1x + 1) / 15.0F))
                              .when(
                                 LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0)
                                    .setProperties(
                                       net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, $$1x)
                                    )
                              )
                        )
                  )
            )
         );
   }

   public Builder createAttachedStemDrops(Block $$0, Item $$1) {
      return LootTable.lootTable()
         .withPool(
            this.applyExplosionDecay(
               $$0,
               LootPool.lootPool()
                  .setRolls(ConstantValue.exactly(1.0F))
                  .add(LootItem.lootTableItem($$1).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336F))))
            )
         );
   }

   protected Builder createShearsOnlyDrop(ItemLike $$0) {
      return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(this.hasShears()).add(LootItem.lootTableItem($$0)));
   }

   protected Builder createShearsOrSilkTouchOnlyDrop(ItemLike $$0) {
      return LootTable.lootTable()
         .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(this.hasShearsOrSilkTouch()).add(LootItem.lootTableItem($$0)));
   }

   protected Builder createMultifaceBlockDrops(Block $$0, net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder $$1) {
      return LootTable.lootTable()
         .withPool(
            LootPool.lootPool()
               .add(
                  this.applyExplosionDecay(
                     $$0,
                     ((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder)((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder)LootItem.lootTableItem(
                                 $$0
                              )
                              .when($$1))
                           .apply(
                              Direction.values(),
                              $$1x -> SetItemCountFunction.setCount(ConstantValue.exactly(1.0F), true)
                                 .when(
                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0)
                                       .setProperties(
                                          net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties()
                                             .hasProperty(MultifaceBlock.getFaceProperty($$1x), true)
                                       )
                                 )
                           ))
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(-1.0F), true))
                  )
               )
         );
   }

   protected Builder createMultifaceBlockDrops(Block $$0) {
      return LootTable.lootTable()
         .withPool(
            LootPool.lootPool()
               .add(
                  this.applyExplosionDecay(
                     $$0,
                     ((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder)LootItem.lootTableItem($$0)
                           .apply(
                              Direction.values(),
                              $$1 -> SetItemCountFunction.setCount(ConstantValue.exactly(1.0F), true)
                                 .when(
                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0)
                                       .setProperties(
                                          net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties()
                                             .hasProperty(MultifaceBlock.getFaceProperty($$1), true)
                                       )
                                 )
                           ))
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(-1.0F), true))
                  )
               )
         );
   }

   protected Builder createMossyCarpetBlockDrops(Block $$0) {
      return LootTable.lootTable()
         .withPool(
            LootPool.lootPool()
               .add(
                  this.applyExplosionDecay(
                     $$0,
                     (FunctionUserBuilder<net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder>)LootItem.lootTableItem($$0)
                        .when(
                           LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0)
                              .setProperties(
                                 net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties().hasProperty(MossyCarpetBlock.BASE, true)
                              )
                        )
                  )
               )
         );
   }

   protected Builder createLeavesDrops(Block $$0, Block $$1, float... $$2) {
      RegistryLookup<Enchantment> $$3 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
      return this.createSilkTouchOrShearsDispatchTable(
            $$0,
            ((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder)this.applyExplosionCondition($$0, LootItem.lootTableItem($$1)))
               .when(BonusLevelTableCondition.bonusLevelFlatChance($$3.getOrThrow(Enchantments.FORTUNE), $$2))
         )
         .withPool(
            LootPool.lootPool()
               .setRolls(ConstantValue.exactly(1.0F))
               .when(this.doesNotHaveShearsOrSilkTouch())
               .add(
                  ((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder)this.applyExplosionDecay(
                        $$0, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                     ))
                     .when(BonusLevelTableCondition.bonusLevelFlatChance($$3.getOrThrow(Enchantments.FORTUNE), NORMAL_LEAVES_STICK_CHANCES))
               )
         );
   }

   protected Builder createOakLeavesDrops(Block $$0, Block $$1, float... $$2) {
      RegistryLookup<Enchantment> $$3 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
      return this.createLeavesDrops($$0, $$1, $$2)
         .withPool(
            LootPool.lootPool()
               .setRolls(ConstantValue.exactly(1.0F))
               .when(this.doesNotHaveShearsOrSilkTouch())
               .add(
                  ((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder)this.applyExplosionCondition(
                        $$0, LootItem.lootTableItem(Items.APPLE)
                     ))
                     .when(
                        BonusLevelTableCondition.bonusLevelFlatChance(
                           $$3.getOrThrow(Enchantments.FORTUNE), new float[]{0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F}
                        )
                     )
               )
         );
   }

   protected Builder createMangroveLeavesDrops(Block $$0) {
      RegistryLookup<Enchantment> $$1 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
      return this.createSilkTouchOrShearsDispatchTable(
         $$0,
         ((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder)this.applyExplosionDecay(
               Blocks.MANGROVE_LEAVES, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
            ))
            .when(BonusLevelTableCondition.bonusLevelFlatChance($$1.getOrThrow(Enchantments.FORTUNE), NORMAL_LEAVES_STICK_CHANCES))
      );
   }

   protected Builder createCropDrops(Block $$0, Item $$1, Item $$2, net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder $$3) {
      RegistryLookup<Enchantment> $$4 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
      return this.applyExplosionDecay(
         $$0,
         LootTable.lootTable()
            .withPool(
               LootPool.lootPool()
                  .add(
                     ((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder)LootItem.lootTableItem($$1).when($$3))
                        .otherwise(LootItem.lootTableItem($$2))
                  )
            )
            .withPool(
               LootPool.lootPool()
                  .when($$3)
                  .add(
                     LootItem.lootTableItem($$2).apply(ApplyBonusCount.addBonusBinomialDistributionCount($$4.getOrThrow(Enchantments.FORTUNE), 0.5714286F, 3))
                  )
            )
      );
   }

   protected Builder createDoublePlantShearsDrop(Block $$0) {
      return LootTable.lootTable()
         .withPool(
            LootPool.lootPool().when(this.hasShears()).add(LootItem.lootTableItem($$0).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))))
         );
   }

   protected Builder createDoublePlantWithSeedDrops(Block $$0, Block $$1) {
      RegistryLookup<Block> $$2 = this.registries.lookupOrThrow(Registries.BLOCK);
      net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder<?> $$3 = ((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder)LootItem.lootTableItem(
               $$1
            )
            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)))
            .when(this.hasShears()))
         .otherwise(
            ((net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder)this.applyExplosionCondition(
                  $$0, LootItem.lootTableItem(Items.WHEAT_SEEDS)
               ))
               .when(LootItemRandomChanceCondition.randomChance(0.125F))
         );
      return LootTable.lootTable()
         .withPool(
            LootPool.lootPool()
               .add($$3)
               .when(
                  LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0)
                     .setProperties(
                        net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties()
                           .hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER)
                     )
               )
               .when(
                  LocationCheck.checkLocation(
                     net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                        .setBlock(
                           net.minecraft.advancements.criterion.BlockPredicate.Builder.block()
                              .of($$2, new Block[]{$$0})
                              .setProperties(
                                 net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER)
                              )
                        ),
                     new BlockPos(0, 1, 0)
                  )
               )
         )
         .withPool(
            LootPool.lootPool()
               .add($$3)
               .when(
                  LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0)
                     .setProperties(
                        net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties()
                           .hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER)
                     )
               )
               .when(
                  LocationCheck.checkLocation(
                     net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                        .setBlock(
                           net.minecraft.advancements.criterion.BlockPredicate.Builder.block()
                              .of($$2, new Block[]{$$0})
                              .setProperties(
                                 net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER)
                              )
                        ),
                     new BlockPos(0, -1, 0)
                  )
               )
         );
   }

   protected Builder createCandleDrops(Block $$0) {
      return LootTable.lootTable()
         .withPool(
            LootPool.lootPool()
               .setRolls(ConstantValue.exactly(1.0F))
               .add(
                  this.applyExplosionDecay(
                     $$0,
                     LootItem.lootTableItem($$0)
                        .apply(
                           List.of(2, 3, 4),
                           $$1 -> SetItemCountFunction.setCount(ConstantValue.exactly($$1.intValue()))
                              .when(
                                 LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0)
                                    .setProperties(
                                       net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties().hasProperty(CandleBlock.CANDLES, $$1)
                                    )
                              )
                        )
                  )
               )
         );
   }

   public Builder createSegmentedBlockDrops(Block $$0) {
      return $$0 instanceof SegmentableBlock $$1
         ? LootTable.lootTable()
            .withPool(
               LootPool.lootPool()
                  .setRolls(ConstantValue.exactly(1.0F))
                  .add(
                     this.applyExplosionDecay(
                        $$0,
                        LootItem.lootTableItem($$0)
                           .apply(
                              IntStream.rangeClosed(1, 4).boxed().toList(),
                              $$2 -> SetItemCountFunction.setCount(ConstantValue.exactly($$2.intValue()))
                                 .when(
                                    LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0)
                                       .setProperties(
                                          net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties()
                                             .hasProperty($$1.getSegmentAmountProperty(), $$2)
                                       )
                                 )
                           )
                     )
                  )
            )
         : noDrop();
   }

   protected static Builder createCandleCakeDrops(Block $$0) {
      return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem($$0)));
   }

   public static Builder noDrop() {
      return LootTable.lootTable();
   }

   protected abstract void generate();

   @Override
   public void generate(BiConsumer<ResourceKey<LootTable>, Builder> $$0) {
      this.generate();
      Set<ResourceKey<LootTable>> $$1 = new HashSet<>();

      for (Block $$2 : BuiltInRegistries.BLOCK) {
         if ($$2.isEnabled(this.enabledFeatures)) {
            $$2.getLootTable()
               .ifPresent(
                  $$3 -> {
                     if ($$1.add((ResourceKey<LootTable>)$$3)) {
                        Builder $$4 = this.map.remove($$3);
                        if ($$4 == null) {
                           throw new IllegalStateException(
                              String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", $$3.identifier(), BuiltInRegistries.BLOCK.getKey($$2))
                           );
                        }

                        $$0.accept((ResourceKey<LootTable>)$$3, $$4);
                     }
                  }
               );
         }
      }

      if (!this.map.isEmpty()) {
         throw new IllegalStateException("Created block loot tables for non-blocks: " + this.map.keySet());
      }
   }

   protected void addNetherVinesDropTable(Block $$0, Block $$1) {
      RegistryLookup<Enchantment> $$2 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
      Builder $$3 = this.createSilkTouchOrShearsDispatchTable(
         $$0,
         LootItem.lootTableItem($$0)
            .when(BonusLevelTableCondition.bonusLevelFlatChance($$2.getOrThrow(Enchantments.FORTUNE), new float[]{0.33F, 0.55F, 0.77F, 1.0F}))
      );
      this.add($$0, $$3);
      this.add($$1, $$3);
   }

   protected Builder createDoorTable(Block $$0) {
      return this.createSinglePropConditionTable($$0, DoorBlock.HALF, DoubleBlockHalf.LOWER);
   }

   protected void dropPottedContents(Block $$0) {
      this.add($$0, (Function<Block, Builder>)($$0x -> this.createPotFlowerItemTable(((FlowerPotBlock)$$0x).getPotted())));
   }

   protected void otherWhenSilkTouch(Block $$0, Block $$1) {
      this.add($$0, this.createSilkTouchOnlyTable($$1));
   }

   protected void dropOther(Block $$0, ItemLike $$1) {
      this.add($$0, this.createSingleItemTable($$1));
   }

   protected void dropWhenSilkTouch(Block $$0) {
      this.otherWhenSilkTouch($$0, $$0);
   }

   protected void dropSelf(Block $$0) {
      this.dropOther($$0, $$0);
   }

   protected void add(Block $$0, Function<Block, Builder> $$1) {
      this.add($$0, $$1.apply($$0));
   }

   protected void add(Block $$0, Builder $$1) {
      this.map.put((ResourceKey<LootTable>)$$0.getLootTable().orElseThrow(() -> new IllegalStateException("Block " + $$0 + " does not have loot table")), $$1);
   }
}
