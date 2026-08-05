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
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.CriterionValidator;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

public class ItemUsedOnLocationTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer $$0, BlockPos $$12, ItemStack $$2) {
        ServerLevel $$3 = $$0.level();
        BlockState $$4 = $$3.getBlockState($$12);
        LootParams $$5 = new LootParams.Builder($$3).withParameter(LootContextParams.ORIGIN, $$12.getCenter()).withParameter(LootContextParams.THIS_ENTITY, $$0).withParameter(LootContextParams.BLOCK_STATE, $$4).withParameter(LootContextParams.TOOL, $$2).create(LootContextParamSets.ADVANCEMENT_LOCATION);
        LootContext $$6 = new LootContext.Builder($$5).create(Optional.empty());
        this.trigger($$0, $$1 -> $$1.matches($$6));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> location) implements SimpleCriterionTrigger.SimpleInstance
    {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player), (App)ContextAwarePredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location)).apply((Applicative)$$0, TriggerInstance::new));

        public static Criterion<TriggerInstance> placedBlock(Block $$0) {
            ContextAwarePredicate $$1 = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).build());
            return CriteriaTriggers.PLACED_BLOCK.createCriterion(new TriggerInstance(Optional.empty(), Optional.of($$1)));
        }

        public static Criterion<TriggerInstance> placedBlock(LootItemCondition.Builder ... $$0) {
            ContextAwarePredicate $$1 = ContextAwarePredicate.create((LootItemCondition[])Arrays.stream($$0).map(LootItemCondition.Builder::build).toArray(LootItemCondition[]::new));
            return CriteriaTriggers.PLACED_BLOCK.createCriterion(new TriggerInstance(Optional.empty(), Optional.of($$1)));
        }

        public static <T extends Comparable<T>> Criterion<TriggerInstance> placedBlockWithProperties(Block $$0, Property<T> $$1, String $$2) {
            StatePropertiesPredicate.Builder $$3 = StatePropertiesPredicate.Builder.properties().hasProperty($$1, $$2);
            ContextAwarePredicate $$4 = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$0).setProperties($$3).build());
            return CriteriaTriggers.PLACED_BLOCK.createCriterion(new TriggerInstance(Optional.empty(), Optional.of($$4)));
        }

        public static Criterion<TriggerInstance> placedBlockWithProperties(Block $$0, Property<Boolean> $$1, boolean $$2) {
            return TriggerInstance.placedBlockWithProperties($$0, $$1, String.valueOf($$2));
        }

        public static Criterion<TriggerInstance> placedBlockWithProperties(Block $$0, Property<Integer> $$1, int $$2) {
            return TriggerInstance.placedBlockWithProperties($$0, $$1, String.valueOf($$2));
        }

        public static <T extends Comparable<T> & StringRepresentable> Criterion<TriggerInstance> placedBlockWithProperties(Block $$0, Property<T> $$1, T $$2) {
            return TriggerInstance.placedBlockWithProperties($$0, $$1, ((StringRepresentable)$$2).getSerializedName());
        }

        private static TriggerInstance itemUsedOnLocation(LocationPredicate.Builder $$0, ItemPredicate.Builder $$1) {
            ContextAwarePredicate $$2 = ContextAwarePredicate.create(LocationCheck.checkLocation($$0).build(), MatchTool.toolMatches($$1).build());
            return new TriggerInstance(Optional.empty(), Optional.of($$2));
        }

        public static Criterion<TriggerInstance> itemUsedOnBlock(LocationPredicate.Builder $$0, ItemPredicate.Builder $$1) {
            return CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(TriggerInstance.itemUsedOnLocation($$0, $$1));
        }

        public static Criterion<TriggerInstance> allayDropItemOnBlock(LocationPredicate.Builder $$0, ItemPredicate.Builder $$1) {
            return CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.createCriterion(TriggerInstance.itemUsedOnLocation($$0, $$1));
        }

        public boolean matches(LootContext $$0) {
            return this.location.isEmpty() || this.location.get().matches($$0);
        }

        @Override
        public void validate(CriterionValidator $$0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate($$0);
            this.location.ifPresent($$1 -> $$0.validate((ContextAwarePredicate)$$1, LootContextParamSets.ADVANCEMENT_LOCATION, "location"));
        }
    }
}

