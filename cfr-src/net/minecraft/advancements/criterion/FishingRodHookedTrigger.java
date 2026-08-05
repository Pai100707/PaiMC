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
import java.util.Collection;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class FishingRodHookedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, ItemStack $$1, FishingHook $$2, Collection<ItemStack> $$32) {
        LootContext $$4 = EntityPredicate.createContext($$0, $$2.getHookedIn() != null ? $$2.getHookedIn() : $$2);
        this.trigger($$0, $$3 -> $$3.matches($$1, $$4, $$32));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> rod, Optional<ContextAwarePredicate> entity, Optional<ItemPredicate> item) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)ItemPredicate.CODEC.optionalFieldOf("rod").forGetter(TriggerInstance::rod), (App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entity), (App)ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> fishedItem(Optional<ItemPredicate> $$0, Optional<EntityPredicate> $$1, Optional<ItemPredicate> $$2) {
            return CriteriaTriggers.FISHING_ROD_HOOKED.createCriterion(new TriggerInstance(Optional.empty(), $$0, EntityPredicate.wrap($$1), $$2));
        }

        public boolean matches(ItemStack $$0, LootContext $$1, Collection<ItemStack> $$2) {
            if (this.rod.isPresent() && !this.rod.get().test($$0)) {
                return false;
            }
            if (this.entity.isPresent() && !this.entity.get().matches($$1)) {
                return false;
            }
            if (this.item.isPresent()) {
                boolean $$3 = false;
                Entity $$4 = $$1.getOptionalParameter(LootContextParams.THIS_ENTITY);
                if ($$4 instanceof ItemEntity) {
                    ItemEntity $$5 = (ItemEntity)$$4;
                    if (this.item.get().test($$5.getItem())) {
                        $$3 = true;
                    }
                }
                for (ItemStack $$6 : $$2) {
                    if (!this.item.get().test($$6)) continue;
                    $$3 = true;
                    break;
                }
                if (!$$3) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator $$0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
            $$0.validateEntity(this.entity, "entity");
        }
    }
}

