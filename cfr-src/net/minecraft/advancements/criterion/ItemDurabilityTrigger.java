/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, ItemStack $$1, int $$22) {
        this.trigger($$0, $$2 -> $$2.matches($$1, $$22));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, MinMaxBounds.Ints durability, MinMaxBounds.Ints delta) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("durability", (Object)MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::durability), (App)MinMaxBounds.Ints.CODEC.optionalFieldOf("delta", (Object)MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::delta)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> changedDurability(Optional<ItemPredicate> $$0, MinMaxBounds.Ints $$1) {
            return TriggerInstance.changedDurability(Optional.empty(), $$0, $$1);
        }

        public static Criterion<TriggerInstance> changedDurability(Optional<ContextAwarePredicate> $$0, Optional<ItemPredicate> $$1, MinMaxBounds.Ints $$2) {
            return CriteriaTriggers.ITEM_DURABILITY_CHANGED.createCriterion(new TriggerInstance($$0, $$1, $$2, MinMaxBounds.Ints.ANY));
        }

        public boolean matches(ItemStack $$0, int $$1) {
            if (this.item.isPresent() && !this.item.get().test($$0)) {
                return false;
            }
            if (!this.durability.matches($$0.getMaxDamage() - $$1)) {
                return false;
            }
            return this.delta.matches($$0.getDamageValue() - $$1);
        }
    }
}

