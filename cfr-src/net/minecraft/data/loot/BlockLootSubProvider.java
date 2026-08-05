/*
 * Decompiled with CFR 0.152.
 */
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
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public abstract class BlockLootSubProvider
implements LootTableSubProvider {
    protected final HolderLookup.Provider registries;
    protected final Set<Item> explosionResistant;
    protected final FeatureFlagSet enabledFeatures;
    protected final Map<ResourceKey<LootTable>, LootTable.Builder> map;
    protected static final float[] NORMAL_LEAVES_SAPLING_CHANCES = new float[]{0.05f, 0.0625f, 0.083333336f, 0.1f};
    private static final float[] NORMAL_LEAVES_STICK_CHANCES = new float[]{0.02f, 0.022222223f, 0.025f, 0.033333335f, 0.1f};

    protected LootItemCondition.Builder hasSilkTouch() {
        return MatchTool.toolMatches(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1))))).build()));
    }

    protected LootItemCondition.Builder doesNotHaveSilkTouch() {
        return this.hasSilkTouch().invert();
    }

    protected LootItemCondition.Builder hasShears() {
        return MatchTool.toolMatches(ItemPredicate.Builder.item().of((HolderGetter<Item>)this.registries.lookupOrThrow(Registries.ITEM), Items.SHEARS));
    }

    private LootItemCondition.Builder hasShearsOrSilkTouch() {
        return this.hasShears().or(this.hasSilkTouch());
    }

    private LootItemCondition.Builder doesNotHaveShearsOrSilkTouch() {
        return this.hasShearsOrSilkTouch().invert();
    }

    protected BlockLootSubProvider(Set<Item> $$0, FeatureFlagSet $$1, HolderLookup.Provider $$2) {
        this($$0, $$1, new HashMap<ResourceKey<LootTable>, LootTable.Builder>(), $$2);
    }

    protected BlockLootSubProvider(Set<Item> $$0, FeatureFlagSet $$1, Map<ResourceKey<LootTable>, LootTable.Builder> $$2, HolderLookup.Provider $$3) {
        this.explosionResistant = $$0;
        this.enabledFeatures = $$1;
        this.map = $$2;
        this.registries = $$3;
    }

    protected <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike $$0, FunctionUserBuilder<T> $$1) {
        if (!this.explosionResistant.contains($$0.asItem())) {
            return $$1.apply(ApplyExplosionDecay.explosionDecay());
        }
        return $$1.unwrap();
    }

    protected <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike $$0, ConditionUserBuilder<T> $$1) {
        if (!this.explosionResistant.contains($$0.asItem())) {
            return $$1.when(ExplosionCondition.survivesExplosion());
        }
        return $$1.unwrap();
    }

    public LootTable.Builder createSingleItemTable(ItemLike $$0) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition($$0, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem($$0))));
    }

    private static LootTable.Builder createSelfDropDispatchTable(Block $$0, LootItemCondition.Builder $$1, LootPoolEntryContainer.Builder<?> $$2) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(((LootPoolSingletonContainer.Builder)LootItem.lootTableItem($$0).when($$1)).otherwise($$2)));
    }

    protected LootTable.Builder createSilkTouchDispatchTable(Block $$0, LootPoolEntryContainer.Builder<?> $$1) {
        return BlockLootSubProvider.createSelfDropDispatchTable($$0, this.hasSilkTouch(), $$1);
    }

    protected LootTable.Builder createShearsDispatchTable(Block $$0, LootPoolEntryContainer.Builder<?> $$1) {
        return BlockLootSubProvider.createSelfDropDispatchTable($$0, this.hasShears(), $$1);
    }

    protected LootTable.Builder createSilkTouchOrShearsDispatchTable(Block $$0, LootPoolEntryContainer.Builder<?> $$1) {
        return BlockLootSubProvider.createSelfDropDispatchTable($$0, this.hasShearsOrSilkTouch(), $$1);
    }

    protected LootTable.Builder createSingleItemTableWithSilkTouch(Block $$0, ItemLike $$1) {
        return this.createSilkTouchDispatchTable($$0, (LootPoolEntryContainer.Builder)this.applyExplosionCondition($$0, LootItem.lootTableItem($$1)));
    }

    protected LootTable.Builder createSingleItemTable(ItemLike $$0, NumberProvider $$1) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, LootItem.lootTableItem($$0).apply(SetItemCountFunction.setCount($$1)))));
    }

    protected LootTable.Builder createSingleItemTableWithSilkTouch(Block $$0, ItemLike $$1, NumberProvider $$2) {
        return this.createSilkTouchDispatchTable($$0, (LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, LootItem.lootTableItem($$1).apply(SetItemCountFunction.setCount($$2))));
    }

    private LootTable.Builder createSilkTouchOnlyTable(ItemLike $$0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(this.hasSilkTouch()).setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem($$0)));
    }

    private LootTable.Builder createPotFlowerItemTable(ItemLike $$0) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition(Blocks.FLOWER_POT, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem(Blocks.FLOWER_POT)))).withPool(this.applyExplosionCondition($$0, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem($$0))));
    }

    protected LootTable.Builder createSlabItemTable(Block $$0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, LootItem.lootTableItem($$0).apply((LootItemFunction.Builder)((Object)SetItemCountFunction.setCount(ConstantValue.exactly(2.0f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE))))))));
    }

    protected <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block $$0, Property<T> $$1, T $$2) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition($$0, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem($$0).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty($$1, $$2))))));
    }

    protected LootTable.Builder createNameableBlockEntityTable(Block $$0) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition($$0, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem($$0).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME))))));
    }

    protected LootTable.Builder createShulkerBoxDrop(Block $$0) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition($$0, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem($$0).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME).include(DataComponents.CONTAINER).include(DataComponents.LOCK).include(DataComponents.CONTAINER_LOOT))))));
    }

    protected LootTable.Builder createCopperOreDrops(Block $$0) {
        HolderGetter $$1 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable($$0, (LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.RAW_COPPER).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 5.0f)))).apply(ApplyBonusCount.addOreBonusCount($$1.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder createLapisOreDrops(Block $$0) {
        HolderGetter $$1 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable($$0, (LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.LAPIS_LAZULI).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0f, 9.0f)))).apply(ApplyBonusCount.addOreBonusCount($$1.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder createRedstoneOreDrops(Block $$0) {
        HolderGetter $$1 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable($$0, (LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.REDSTONE).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0f, 5.0f)))).apply(ApplyBonusCount.addUniformBonusCount($$1.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder createBannerDrop(Block $$0) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition($$0, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem($$0).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME).include(DataComponents.ITEM_NAME).include(DataComponents.TOOLTIP_DISPLAY).include(DataComponents.BANNER_PATTERNS).include(DataComponents.RARITY))))));
    }

    protected LootTable.Builder createBeeNestDrop(Block $$0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(this.hasSilkTouch()).setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem($$0).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.BEES))).apply(CopyBlockState.copyState($$0).copy(BeehiveBlock.HONEY_LEVEL)))));
    }

    protected LootTable.Builder createBeeHiveDrop(Block $$0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(((LootPoolEntryContainer.Builder)((Object)((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem($$0).when(this.hasSilkTouch())).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.BEES))).apply(CopyBlockState.copyState($$0).copy(BeehiveBlock.HONEY_LEVEL)))).otherwise(LootItem.lootTableItem($$0))));
    }

    protected LootTable.Builder createCaveVinesDrop(Block $$0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.GLOW_BERRIES)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CaveVines.BERRIES, true))));
    }

    protected LootTable.Builder createCopperGolemStatueBlock(Block $$0) {
        return LootTable.lootTable().withPool(this.applyExplosionCondition($$0, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem($$0).apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY).include(DataComponents.CUSTOM_NAME))).apply(CopyBlockState.copyState($$0).copy(CopperGolemStatueBlock.POSE))))));
    }

    protected LootTable.Builder createOreDrop(Block $$0, Item $$1) {
        HolderGetter $$2 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable($$0, (LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, LootItem.lootTableItem($$1).apply(ApplyBonusCount.addOreBonusCount($$2.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder createMushroomBlockDrop(Block $$0, ItemLike $$1) {
        return this.createSilkTouchDispatchTable($$0, (LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem($$1).apply(SetItemCountFunction.setCount(UniformGenerator.between(-6.0f, 2.0f)))).apply(LimitCount.limitCount(IntRange.lowerBound(0)))));
    }

    protected LootTable.Builder createGrassDrops(Block $$0) {
        HolderGetter $$1 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createShearsDispatchTable($$0, (LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.WHEAT_SEEDS).when(LootItemRandomChanceCondition.randomChance(0.125f))).apply(ApplyBonusCount.addUniformBonusCount($$1.getOrThrow(Enchantments.FORTUNE), 2))));
    }

    public LootTable.Builder createStemDrops(Block $$0, Item $$12) {
        return LootTable.lootTable().withPool(this.applyExplosionDecay($$0, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder)LootItem.lootTableItem($$12).apply(StemBlock.AGE.getPossibleValues(), $$1 -> SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, (float)($$1 + 1) / 15.0f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, $$1.intValue())))))));
    }

    public LootTable.Builder createAttachedStemDrops(Block $$0, Item $$1) {
        return LootTable.lootTable().withPool(this.applyExplosionDecay($$0, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem($$1).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336f)))))));
    }

    protected LootTable.Builder createShearsOnlyDrop(ItemLike $$0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).when(this.hasShears()).add(LootItem.lootTableItem($$0)));
    }

    protected LootTable.Builder createShearsOrSilkTouchOnlyDrop(ItemLike $$0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).when(this.hasShearsOrSilkTouch()).add(LootItem.lootTableItem($$0)));
    }

    protected LootTable.Builder createMultifaceBlockDrops(Block $$0, LootItemCondition.Builder $$12) {
        return LootTable.lootTable().withPool(LootPool.lootPool().add((LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, ((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem($$0).when($$12)).apply(Direction.values(), $$1 -> SetItemCountFunction.setCount(ConstantValue.exactly(1.0f), true).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MultifaceBlock.getFaceProperty($$1), true))))).apply(SetItemCountFunction.setCount(ConstantValue.exactly(-1.0f), true)))));
    }

    protected LootTable.Builder createMultifaceBlockDrops(Block $$0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().add((LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem($$0).apply(Direction.values(), $$1 -> SetItemCountFunction.setCount(ConstantValue.exactly(1.0f), true).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MultifaceBlock.getFaceProperty($$1), true))))).apply(SetItemCountFunction.setCount(ConstantValue.exactly(-1.0f), true)))));
    }

    protected LootTable.Builder createMossyCarpetBlockDrops(Block $$0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().add((LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, (FunctionUserBuilder)((Object)LootItem.lootTableItem($$0).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MossyCarpetBlock.BASE, true)))))));
    }

    protected LootTable.Builder createLeavesDrops(Block $$0, Block $$1, float ... $$2) {
        HolderGetter $$3 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchOrShearsDispatchTable($$0, (LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)this.applyExplosionCondition($$0, LootItem.lootTableItem($$1))).when(BonusLevelTableCondition.bonusLevelFlatChance($$3.getOrThrow(Enchantments.FORTUNE), $$2))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).when(this.doesNotHaveShearsOrSilkTouch()).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)this.applyExplosionDecay($$0, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f))))).when(BonusLevelTableCondition.bonusLevelFlatChance($$3.getOrThrow(Enchantments.FORTUNE), NORMAL_LEAVES_STICK_CHANCES))));
    }

    protected LootTable.Builder createOakLeavesDrops(Block $$0, Block $$1, float ... $$2) {
        HolderGetter $$3 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createLeavesDrops($$0, $$1, $$2).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).when(this.doesNotHaveShearsOrSilkTouch()).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)this.applyExplosionCondition($$0, LootItem.lootTableItem(Items.APPLE))).when(BonusLevelTableCondition.bonusLevelFlatChance($$3.getOrThrow(Enchantments.FORTUNE), 0.005f, 0.0055555557f, 0.00625f, 0.008333334f, 0.025f))));
    }

    protected LootTable.Builder createMangroveLeavesDrops(Block $$0) {
        HolderGetter $$1 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchOrShearsDispatchTable($$0, (LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)this.applyExplosionDecay(Blocks.MANGROVE_LEAVES, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f))))).when(BonusLevelTableCondition.bonusLevelFlatChance($$1.getOrThrow(Enchantments.FORTUNE), NORMAL_LEAVES_STICK_CHANCES)));
    }

    protected LootTable.Builder createCropDrops(Block $$0, Item $$1, Item $$2, LootItemCondition.Builder $$3) {
        HolderGetter $$4 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.applyExplosionDecay($$0, LootTable.lootTable().withPool(LootPool.lootPool().add(((LootPoolSingletonContainer.Builder)LootItem.lootTableItem($$1).when($$3)).otherwise(LootItem.lootTableItem($$2)))).withPool(LootPool.lootPool().when($$3).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem($$2).apply(ApplyBonusCount.addBonusBinomialDistributionCount($$4.getOrThrow(Enchantments.FORTUNE), 0.5714286f, 3))))));
    }

    protected LootTable.Builder createDoublePlantShearsDrop(Block $$0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(this.hasShears()).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem($$0).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0f))))));
    }

    protected LootTable.Builder createDoublePlantWithSeedDrops(Block $$0, Block $$1) {
        HolderGetter $$2 = this.registries.lookupOrThrow(Registries.BLOCK);
        AlternativesEntry.Builder $$3 = ((LootPoolSingletonContainer.Builder)((LootPoolEntryContainer.Builder)((Object)LootItem.lootTableItem($$1).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0f))))).when(this.hasShears())).otherwise((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)this.applyExplosionCondition($$0, LootItem.lootTableItem(Items.WHEAT_SEEDS))).when(LootItemRandomChanceCondition.randomChance(0.125f)));
        return LootTable.lootTable().withPool(LootPool.lootPool().add($$3).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))).when(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)$$2, $$0).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER))), new BlockPos(0, 1, 0)))).withPool(LootPool.lootPool().add($$3).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER))).when(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)$$2, $$0).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))), new BlockPos(0, -1, 0))));
    }

    protected LootTable.Builder createCandleDrops(Block $$0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, (FunctionUserBuilder)LootItem.lootTableItem($$0).apply(List.of(Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4)), $$1 -> SetItemCountFunction.setCount(ConstantValue.exactly($$1.intValue())).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CandleBlock.CANDLES, $$1.intValue())))))));
    }

    public LootTable.Builder createSegmentedBlockDrops(Block $$0) {
        if ($$0 instanceof SegmentableBlock) {
            SegmentableBlock $$1 = (SegmentableBlock)((Object)$$0);
            return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder)this.applyExplosionDecay($$0, (FunctionUserBuilder)LootItem.lootTableItem($$0).apply(IntStream.rangeClosed(1, 4).boxed().toList(), $$2 -> SetItemCountFunction.setCount(ConstantValue.exactly($$2.intValue())).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty($$1.getSegmentAmountProperty(), $$2.intValue())))))));
        }
        return BlockLootSubProvider.noDrop();
    }

    protected static LootTable.Builder createCandleCakeDrops(Block $$0) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem($$0)));
    }

    public static LootTable.Builder noDrop() {
        return LootTable.lootTable();
    }

    protected abstract void generate();

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> $$0) {
        this.generate();
        HashSet $$1 = new HashSet();
        for (Block $$2 : BuiltInRegistries.BLOCK) {
            if (!$$2.isEnabled(this.enabledFeatures)) continue;
            $$2.getLootTable().ifPresent($$3 -> {
                if ($$1.add($$3)) {
                    LootTable.Builder $$4 = this.map.remove($$3);
                    if ($$4 == null) {
                        throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", $$3.identifier(), BuiltInRegistries.BLOCK.getKey($$2)));
                    }
                    $$0.accept((ResourceKey<LootTable>)$$3, $$4);
                }
            });
        }
        if (!this.map.isEmpty()) {
            throw new IllegalStateException("Created block loot tables for non-blocks: " + String.valueOf(this.map.keySet()));
        }
    }

    protected void addNetherVinesDropTable(Block $$0, Block $$1) {
        HolderGetter $$2 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        LootTable.Builder $$3 = this.createSilkTouchOrShearsDispatchTable($$0, (LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem($$0).when(BonusLevelTableCondition.bonusLevelFlatChance($$2.getOrThrow(Enchantments.FORTUNE), 0.33f, 0.55f, 0.77f, 1.0f)));
        this.add($$0, $$3);
        this.add($$1, $$3);
    }

    protected LootTable.Builder createDoorTable(Block $$0) {
        return this.createSinglePropConditionTable($$0, DoorBlock.HALF, DoubleBlockHalf.LOWER);
    }

    protected void dropPottedContents(Block $$02) {
        this.add($$02, (Block $$0) -> this.createPotFlowerItemTable(((FlowerPotBlock)$$0).getPotted()));
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

    protected void add(Block $$0, Function<Block, LootTable.Builder> $$1) {
        this.add($$0, $$1.apply($$0));
    }

    protected void add(Block $$0, LootTable.Builder $$1) {
        this.map.put($$0.getLootTable().orElseThrow(() -> new IllegalStateException("Block " + String.valueOf($$0) + " does not have loot table")), $$1);
    }
}

