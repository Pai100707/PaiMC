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
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$02) {
        this.trigger($$02, $$0 -> true);
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> located(LocationPredicate.Builder $$0) {
            return CriteriaTriggers.LOCATION.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().located($$0)))));
        }

        public static Criterion<TriggerInstance> located(EntityPredicate.Builder $$0) {
            return CriteriaTriggers.LOCATION.createCriterion(new TriggerInstance(Optional.of(EntityPredicate.wrap($$0.build()))));
        }

        public static Criterion<TriggerInstance> located(Optional<EntityPredicate> $$0) {
            return CriteriaTriggers.LOCATION.createCriterion(new TriggerInstance(EntityPredicate.wrap($$0)));
        }

        public static Criterion<TriggerInstance> sleptInBed() {
            return CriteriaTriggers.SLEPT_IN_BED.createCriterion(new TriggerInstance(Optional.empty()));
        }

        public static Criterion<TriggerInstance> raidWon() {
            return CriteriaTriggers.RAID_WIN.createCriterion(new TriggerInstance(Optional.empty()));
        }

        public static Criterion<TriggerInstance> avoidVibration() {
            return CriteriaTriggers.AVOID_VIBRATION.createCriterion(new TriggerInstance(Optional.empty()));
        }

        public static Criterion<TriggerInstance> tick() {
            return CriteriaTriggers.TICK.createCriterion(new TriggerInstance(Optional.empty()));
        }

        public static Criterion<TriggerInstance> walkOnBlockWithEquipment(HolderGetter<Block> $$0, HolderGetter<Item> $$1, Block $$2, Item $$3) {
            return TriggerInstance.located(EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of($$1, $$3))).steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of($$0, $$2))));
        }
    }
}

