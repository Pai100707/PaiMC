/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.loot.packs;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public record VanillaChargedCreeperExplosionLoot(HolderLookup.Provider registries) implements LootTableSubProvider
{
    private static final List<Entry> ENTRIES = List.of(new Entry(BuiltInLootTables.CHARGED_CREEPER_PIGLIN, EntityType.PIGLIN, Items.PIGLIN_HEAD), new Entry(BuiltInLootTables.CHARGED_CREEPER_CREEPER, EntityType.CREEPER, Items.CREEPER_HEAD), new Entry(BuiltInLootTables.CHARGED_CREEPER_SKELETON, EntityType.SKELETON, Items.SKELETON_SKULL), new Entry(BuiltInLootTables.CHARGED_CREEPER_WITHER_SKELETON, EntityType.WITHER_SKELETON, Items.WITHER_SKELETON_SKULL), new Entry(BuiltInLootTables.CHARGED_CREEPER_ZOMBIE, EntityType.ZOMBIE, Items.ZOMBIE_HEAD));

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> $$0) {
        HolderGetter $$1 = this.registries.lookupOrThrow(Registries.ENTITY_TYPE);
        ArrayList<ConditionUserBuilder> $$2 = new ArrayList<ConditionUserBuilder>(ENTRIES.size());
        for (Entry $$3 : ENTRIES) {
            $$0.accept($$3.lootTable, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem($$3.item))));
            LootItemCondition.Builder $$4 = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of($$1, $$3.entityType)));
            $$2.add(NestedLootTable.lootTableReference($$3.lootTable).when($$4));
        }
        $$0.accept(BuiltInLootTables.CHARGED_CREEPER, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(AlternativesEntry.alternatives((LootPoolEntryContainer.Builder[])$$2.toArray(LootPoolEntryContainer.Builder[]::new)))));
    }

    static final class Entry
    extends Record {
        final ResourceKey<LootTable> lootTable;
        final EntityType<?> entityType;
        final Item item;

        Entry(ResourceKey<LootTable> $$0, EntityType<?> $$1, Item $$2) {
            this.lootTable = $$0;
            this.entityType = $$1;
            this.item = $$2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "lootTable;entityType;item", "lootTable", "entityType", "item"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "lootTable;entityType;item", "lootTable", "entityType", "item"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "lootTable;entityType;item", "lootTable", "entityType", "item"}, this, $$0);
        }

        public ResourceKey<LootTable> lootTable() {
            return this.lootTable;
        }

        public EntityType<?> entityType() {
            return this.entityType;
        }

        public Item item() {
            return this.item;
        }
    }
}

