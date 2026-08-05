/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
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
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jspecify.annotations.Nullable;

public class PickedUpItemTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, ItemStack $$1, @Nullable Entity $$2) {
        LootContext $$32 = EntityPredicate.createContext($$0, $$2);
        this.trigger($$0, $$3 -> $$3.matches($$0, $$1, $$32));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, Optional<ContextAwarePredicate> entity) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entity)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> thrownItemPickedUpByEntity(ContextAwarePredicate $$0, Optional<ItemPredicate> $$1, Optional<ContextAwarePredicate> $$2) {
            return CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.createCriterion(new TriggerInstance(Optional.of($$0), $$1, $$2));
        }

        public static Criterion<TriggerInstance> thrownItemPickedUpByPlayer(Optional<ContextAwarePredicate> $$0, Optional<ItemPredicate> $$1, Optional<ContextAwarePredicate> $$2) {
            return CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.createCriterion(new TriggerInstance($$0, $$1, $$2));
        }

        public boolean matches(ServerPlayer $$0, ItemStack $$1, LootContext $$2) {
            if (this.item.isPresent() && !this.item.get().test($$1)) {
                return false;
            }
            return !this.entity.isPresent() || this.entity.get().matches($$2);
        }

        @Override
        public void validate(CriterionValidator $$0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
            $$0.validateEntity(this.entity, "entity");
        }
    }
}

