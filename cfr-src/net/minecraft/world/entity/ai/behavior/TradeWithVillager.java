/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TradeWithVillager
extends Behavior<Villager> {
    private Set<Item> trades = ImmutableSet.of();

    public TradeWithVillager() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, (Object)((Object)MemoryStatus.VALUE_PRESENT), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, (Object)((Object)MemoryStatus.VALUE_PRESENT)));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, Villager $$1) {
        return BehaviorUtils.targetIsValid($$1.getBrain(), MemoryModuleType.INTERACTION_TARGET, EntityType.VILLAGER);
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Villager $$1, long $$2) {
        return this.checkExtraStartConditions($$0, $$1);
    }

    @Override
    protected void start(ServerLevel $$0, Villager $$1, long $$2) {
        Villager $$3 = (Villager)$$1.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        BehaviorUtils.lockGazeAndWalkToEachOther($$1, $$3, 0.5f, 2);
        this.trades = TradeWithVillager.figureOutWhatIAmWillingToTrade($$1, $$3);
    }

    @Override
    protected void tick(ServerLevel $$0, Villager $$1, long $$2) {
        Villager $$3 = (Villager)$$1.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
        if ($$1.distanceToSqr($$3) > 5.0) {
            return;
        }
        BehaviorUtils.lockGazeAndWalkToEachOther($$1, $$3, 0.5f, 2);
        $$1.gossip($$0, $$3, $$2);
        boolean $$4 = $$1.getVillagerData().profession().is(VillagerProfession.FARMER);
        if ($$1.hasExcessFood() && ($$4 || $$3.wantsMoreFood())) {
            TradeWithVillager.throwHalfStack($$1, Villager.FOOD_POINTS.keySet(), $$3);
        }
        if ($$4 && $$1.getInventory().countItem(Items.WHEAT) > Items.WHEAT.getDefaultMaxStackSize() / 2) {
            TradeWithVillager.throwHalfStack($$1, (Set<Item>)ImmutableSet.of((Object)Items.WHEAT), $$3);
        }
        if (!this.trades.isEmpty() && $$1.getInventory().hasAnyOf(this.trades)) {
            TradeWithVillager.throwHalfStack($$1, this.trades, $$3);
        }
    }

    @Override
    protected void stop(ServerLevel $$0, Villager $$1, long $$2) {
        $$1.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
    }

    private static Set<Item> figureOutWhatIAmWillingToTrade(Villager $$0, Villager $$12) {
        ImmutableSet<Item> $$2 = $$12.getVillagerData().profession().value().requestedItems();
        ImmutableSet<Item> $$3 = $$0.getVillagerData().profession().value().requestedItems();
        return $$2.stream().filter($$1 -> !$$3.contains($$1)).collect(Collectors.toSet());
    }

    private static void throwHalfStack(Villager $$0, Set<Item> $$1, LivingEntity $$2) {
        SimpleContainer $$3 = $$0.getInventory();
        ItemStack $$4 = ItemStack.EMPTY;
        for (int $$5 = 0; $$5 < $$3.getContainerSize(); ++$$5) {
            int $$9;
            Item $$7;
            ItemStack $$6 = $$3.getItem($$5);
            if ($$6.isEmpty() || !$$1.contains($$7 = $$6.getItem())) continue;
            if ($$6.getCount() > $$6.getMaxStackSize() / 2) {
                int $$8 = $$6.getCount() / 2;
            } else {
                if ($$6.getCount() <= 24) continue;
                $$9 = $$6.getCount() - 24;
            }
            $$6.shrink($$9);
            $$4 = new ItemStack($$7, $$9);
            break;
        }
        if (!$$4.isEmpty()) {
            BehaviorUtils.throwItem($$0, $$4, $$2.position());
        }
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Villager)livingEntity, l);
    }
}

