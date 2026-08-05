/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, Inventory $$1, ItemStack $$2) {
        int $$3 = 0;
        int $$4 = 0;
        int $$5 = 0;
        for (int $$6 = 0; $$6 < $$1.getContainerSize(); ++$$6) {
            ItemStack $$7 = $$1.getItem($$6);
            if ($$7.isEmpty()) {
                ++$$4;
                continue;
            }
            ++$$5;
            if ($$7.getCount() < $$7.getMaxStackSize()) continue;
            ++$$3;
        }
        this.trigger($$0, $$1, $$2, $$3, $$4, $$5);
    }

    private void trigger(ServerPlayer $$0, Inventory $$1, ItemStack $$2, int $$3, int $$4, int $$52) {
        this.trigger($$0, $$5 -> $$5.matches($$1, $$2, $$3, $$4, $$52));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Slots slots, List<ItemPredicate> items) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)Slots.CODEC.optionalFieldOf("slots", (Object)Slots.ANY).forGetter(TriggerInstance::slots), (App)ItemPredicate.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(TriggerInstance::items)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> hasItems(ItemPredicate.Builder ... $$0) {
            return TriggerInstance.hasItems((ItemPredicate[])Stream.of($$0).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
        }

        public static Criterion<TriggerInstance> hasItems(ItemPredicate ... $$0) {
            return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(new TriggerInstance(Optional.empty(), Slots.ANY, List.of($$0)));
        }

        public static Criterion<TriggerInstance> hasItems(ItemLike ... $$0) {
            ItemPredicate[] $$1 = new ItemPredicate[$$0.length];
            for (int $$2 = 0; $$2 < $$0.length; ++$$2) {
                $$1[$$2] = new ItemPredicate(Optional.of(HolderSet.direct($$0[$$2].asItem().builtInRegistryHolder())), MinMaxBounds.Ints.ANY, DataComponentMatchers.ANY);
            }
            return TriggerInstance.hasItems($$1);
        }

        public boolean matches(Inventory $$0, ItemStack $$12, int $$2, int $$3, int $$4) {
            if (!this.slots.matches($$2, $$3, $$4)) {
                return false;
            }
            if (this.items.isEmpty()) {
                return true;
            }
            if (this.items.size() == 1) {
                return !$$12.isEmpty() && this.items.get(0).test($$12);
            }
            ObjectArrayList $$5 = new ObjectArrayList(this.items);
            int $$6 = $$0.getContainerSize();
            for (int $$7 = 0; $$7 < $$6; ++$$7) {
                if ($$5.isEmpty()) {
                    return true;
                }
                ItemStack $$8 = $$0.getItem($$7);
                if ($$8.isEmpty()) continue;
                $$5.removeIf($$1 -> $$1.test($$8));
            }
            return $$5.isEmpty();
        }

        public record Slots(MinMaxBounds.Ints occupied, MinMaxBounds.Ints full, MinMaxBounds.Ints empty) {
            public static final Codec<Slots> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)MinMaxBounds.Ints.CODEC.optionalFieldOf("occupied", (Object)MinMaxBounds.Ints.ANY).forGetter(Slots::occupied), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("full", (Object)MinMaxBounds.Ints.ANY).forGetter(Slots::full), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("empty", (Object)MinMaxBounds.Ints.ANY).forGetter(Slots::empty)).apply((Applicative)$$0, Slots::new));
            public static final Slots ANY = new Slots(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY);

            public boolean matches(int $$0, int $$1, int $$2) {
                if (!this.full.matches($$0)) {
                    return false;
                }
                if (!this.empty.matches($$1)) {
                    return false;
                }
                return this.occupied.matches($$2);
            }
        }
    }
}

