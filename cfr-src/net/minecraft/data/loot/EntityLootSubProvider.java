/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.data.loot;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityFlagsPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SheepPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

public abstract class EntityLootSubProvider
implements LootTableSubProvider {
    protected final HolderLookup.Provider registries;
    private final FeatureFlagSet allowed;
    private final FeatureFlagSet required;
    private final Map<EntityType<?>, Map<ResourceKey<LootTable>, LootTable.Builder>> map = Maps.newHashMap();

    protected final AnyOfCondition.Builder shouldSmeltLoot() {
        HolderGetter $$0 = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return AnyOfCondition.anyOf(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnFire(true))), LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER, EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().mainhand(ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate($$0.getOrThrow(EnchantmentTags.SMELTS_LOOT), MinMaxBounds.Ints.ANY)))).build())))));
    }

    protected EntityLootSubProvider(FeatureFlagSet $$0, HolderLookup.Provider $$1) {
        this($$0, $$0, $$1);
    }

    protected EntityLootSubProvider(FeatureFlagSet $$0, FeatureFlagSet $$1, HolderLookup.Provider $$2) {
        this.allowed = $$0;
        this.required = $$1;
        this.registries = $$2;
    }

    public static LootPool.Builder createSheepDispatchPool(Map<DyeColor, ResourceKey<LootTable>> $$0) {
        AlternativesEntry.Builder $$1 = AlternativesEntry.alternatives(new LootPoolEntryContainer.Builder[0]);
        for (Map.Entry<DyeColor, ResourceKey<LootTable>> $$2 : $$0.entrySet()) {
            $$1 = $$1.otherwise((LootPoolEntryContainer.Builder<?>)NestedLootTable.lootTableReference($$2.getValue()).when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().components(DataComponentMatchers.Builder.components().exact(DataComponentExactPredicate.expect(DataComponents.SHEEP_COLOR, $$2.getKey())).build()).subPredicate(SheepPredicate.hasWool()))));
        }
        return LootPool.lootPool().add($$1);
    }

    public abstract void generate();

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> $$0) {
        this.generate();
        HashSet $$1 = new HashSet();
        BuiltInRegistries.ENTITY_TYPE.listElements().forEach($$2 -> {
            EntityType $$32 = (EntityType)$$2.value();
            if (!$$32.isEnabled(this.allowed)) {
                return;
            }
            Optional<ResourceKey<LootTable>> $$42 = $$32.getDefaultLootTable();
            if ($$42.isPresent()) {
                Map<ResourceKey<LootTable>, LootTable.Builder> $$5 = this.map.remove($$32);
                if ($$32.isEnabled(this.required) && ($$5 == null || !$$5.containsKey($$42.get()))) {
                    throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", $$42.get(), $$2.key().identifier()));
                }
                if ($$5 != null) {
                    $$5.forEach(($$3, $$4) -> {
                        if (!$$1.add($$3)) {
                            throw new IllegalStateException(String.format(Locale.ROOT, "Duplicate loottable '%s' for '%s'", $$3, $$2.key().identifier()));
                        }
                        $$0.accept((ResourceKey<LootTable>)$$3, (LootTable.Builder)$$4);
                    });
                }
            } else {
                Map<ResourceKey<LootTable>, LootTable.Builder> $$6 = this.map.remove($$32);
                if ($$6 != null) {
                    throw new IllegalStateException(String.format(Locale.ROOT, "Weird loottables '%s' for '%s', not a LivingEntity so should not have loot", $$6.keySet().stream().map($$0 -> $$0.identifier().toString()).collect(Collectors.joining(",")), $$2.key().identifier()));
                }
            }
        });
        if (!this.map.isEmpty()) {
            throw new IllegalStateException("Created loot tables for entities not supported by datapack: " + String.valueOf(this.map.keySet()));
        }
    }

    protected LootItemCondition.Builder killedByFrog(HolderGetter<EntityType<?>> $$0) {
        return DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().source(EntityPredicate.Builder.entity().of($$0, EntityType.FROG)));
    }

    protected LootItemCondition.Builder killedByFrogVariant(HolderGetter<EntityType<?>> $$0, HolderGetter<FrogVariant> $$1, ResourceKey<FrogVariant> $$2) {
        return DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().source(EntityPredicate.Builder.entity().of($$0, EntityType.FROG).components(DataComponentMatchers.Builder.components().exact(DataComponentExactPredicate.expect(DataComponents.FROG_VARIANT, $$1.getOrThrow($$2))).build())));
    }

    protected void add(EntityType<?> $$0, LootTable.Builder $$1) {
        this.add($$0, $$0.getDefaultLootTable().orElseThrow(() -> new IllegalStateException("Entity " + String.valueOf($$0) + " has no loot table")), $$1);
    }

    protected void add(EntityType<?> $$02, ResourceKey<LootTable> $$1, LootTable.Builder $$2) {
        this.map.computeIfAbsent($$02, $$0 -> new HashMap()).put($$1, $$2);
    }
}

