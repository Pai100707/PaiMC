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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class ChanneledLightningTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    @Override
    public void trigger(ServerPlayer $$0, Collection<? extends Entity> $$12) {
        List $$2 = $$12.stream().map($$1 -> EntityPredicate.createContext($$0, $$1)).collect(Collectors.toList());
        this.trigger($$0, (T $$1) -> $$1.matches($$2));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)EntityPredicate.ADVANCEMENT_CODEC.listOf().optionalFieldOf("victims", List.of()).forGetter(TriggerInstance::victims)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> channeledLightning(EntityPredicate.Builder ... $$0) {
            return CriteriaTriggers.CHANNELED_LIGHTNING.createCriterion(new TriggerInstance(Optional.empty(), EntityPredicate.wrap($$0)));
        }

        public boolean matches(Collection<? extends LootContext> $$0) {
            for (ContextAwarePredicate $$1 : this.victims) {
                boolean $$2 = false;
                for (LootContext lootContext : $$0) {
                    if (!$$1.matches(lootContext)) continue;
                    $$2 = true;
                    break;
                }
                if ($$2) continue;
                return false;
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator $$0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
            $$0.validateEntities(this.victims, "victims");
        }
    }
}

