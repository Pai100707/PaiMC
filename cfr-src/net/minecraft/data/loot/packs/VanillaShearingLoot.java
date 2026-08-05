/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.loot.packs;

import java.util.function.BiConsumer;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.data.loot.packs.LootData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public record VanillaShearingLoot(HolderLookup.Provider registries) implements LootTableSubProvider
{
    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> $$0) {
        $$0.accept(BuiltInLootTables.BOGGED_SHEAR, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(2.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem(Items.BROWN_MUSHROOM).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0f))))).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem(Items.RED_MUSHROOM).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0f)))))));
        LootData.WOOL_ITEM_BY_DYE.forEach(($$1, $$2) -> $$0.accept(BuiltInLootTables.SHEAR_SHEEP_BY_DYE.get($$1), LootTable.lootTable().withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0f, 3.0f)).add(LootItem.lootTableItem($$2)))));
        $$0.accept(BuiltInLootTables.SHEAR_SHEEP, LootTable.lootTable().withPool(EntityLootSubProvider.createSheepDispatchPool(BuiltInLootTables.SHEAR_SHEEP_BY_DYE)));
        $$0.accept(BuiltInLootTables.SHEAR_MOOSHROOM, LootTable.lootTable().withPool(LootPool.lootPool().add(AlternativesEntry.alternatives(new LootPoolEntryContainer.Builder[]{NestedLootTable.lootTableReference(BuiltInLootTables.SHEAR_RED_MOOSHROOM).when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().components(DataComponentMatchers.Builder.components().exact(DataComponentExactPredicate.expect(DataComponents.MOOSHROOM_VARIANT, MushroomCow.Variant.RED)).build()))), NestedLootTable.lootTableReference(BuiltInLootTables.SHEAR_BROWN_MOOSHROOM).when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().components(DataComponentMatchers.Builder.components().exact(DataComponentExactPredicate.expect(DataComponents.MOOSHROOM_VARIANT, MushroomCow.Variant.BROWN)).build())))}))));
        $$0.accept(BuiltInLootTables.SHEAR_RED_MOOSHROOM, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(5.0f)).add(LootItem.lootTableItem(Items.RED_MUSHROOM))));
        $$0.accept(BuiltInLootTables.SHEAR_BROWN_MOOSHROOM, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(5.0f)).add(LootItem.lootTableItem(Items.BROWN_MUSHROOM))));
        $$0.accept(BuiltInLootTables.SHEAR_SNOW_GOLEM, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem(Items.CARVED_PUMPKIN))));
    }
}

