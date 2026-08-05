/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.server.PlayerAdvancements;

public class ImpossibleTrigger
implements CriterionTrigger<TriggerInstance> {
    @Override
    public void addPlayerListener(PlayerAdvancements $$0, CriterionTrigger.Listener<TriggerInstance> $$1) {
    }

    @Override
    public void removePlayerListener(PlayerAdvancements $$0, CriterionTrigger.Listener<TriggerInstance> $$1) {
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements $$0) {
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public record TriggerInstance() implements CriterionTriggerInstance
    {
        public static final Codec<TriggerInstance> CODEC = MapCodec.unitCodec((Object)new TriggerInstance());

        @Override
        public void validate(CriterionValidator $$0) {
        }
    }
}

