/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class UseBonemeal
extends Behavior<Villager> {
    private static final int BONEMEALING_DURATION = 80;
    private long nextWorkCycleTime;
    private long lastBonemealingSession;
    private int timeWorkedSoFar;
    private Optional<BlockPos> cropPos = Optional.empty();

    public UseBonemeal() {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryStatus.VALUE_ABSENT)));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel $$0, Villager $$1) {
        if ($$1.tickCount % 10 != 0 || this.lastBonemealingSession != 0L && this.lastBonemealingSession + 160L > (long)$$1.tickCount) {
            return false;
        }
        if ($$1.getInventory().countItem(Items.BONE_MEAL) <= 0) {
            return false;
        }
        this.cropPos = this.pickNextTarget($$0, $$1);
        return this.cropPos.isPresent();
    }

    @Override
    protected boolean canStillUse(ServerLevel $$0, Villager $$1, long $$2) {
        return this.timeWorkedSoFar < 80 && this.cropPos.isPresent();
    }

    private Optional<BlockPos> pickNextTarget(ServerLevel $$0, Villager $$1) {
        BlockPos.MutableBlockPos $$2 = new BlockPos.MutableBlockPos();
        Optional<BlockPos> $$3 = Optional.empty();
        int $$4 = 0;
        for (int $$5 = -1; $$5 <= 1; ++$$5) {
            for (int $$6 = -1; $$6 <= 1; ++$$6) {
                for (int $$7 = -1; $$7 <= 1; ++$$7) {
                    $$2.setWithOffset($$1.blockPosition(), $$5, $$6, $$7);
                    if (!this.validPos($$2, $$0) || $$0.random.nextInt(++$$4) != 0) continue;
                    $$3 = Optional.of($$2.immutable());
                }
            }
        }
        return $$3;
    }

    private boolean validPos(BlockPos $$0, ServerLevel $$1) {
        BlockState $$2 = $$1.getBlockState($$0);
        Block $$3 = $$2.getBlock();
        return $$3 instanceof CropBlock && !((CropBlock)$$3).isMaxAge($$2);
    }

    @Override
    protected void start(ServerLevel $$0, Villager $$1, long $$2) {
        this.setCurrentCropAsTarget($$1);
        $$1.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BONE_MEAL));
        this.nextWorkCycleTime = $$2;
        this.timeWorkedSoFar = 0;
    }

    private void setCurrentCropAsTarget(Villager $$0) {
        this.cropPos.ifPresent($$1 -> {
            BlockPosTracker $$2 = new BlockPosTracker((BlockPos)$$1);
            $$0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, $$2);
            $$0.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget($$2, 0.5f, 1));
        });
    }

    @Override
    protected void stop(ServerLevel $$0, Villager $$1, long $$2) {
        $$1.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.lastBonemealingSession = $$1.tickCount;
    }

    @Override
    protected void tick(ServerLevel $$0, Villager $$1, long $$2) {
        BlockPos $$3 = this.cropPos.get();
        if ($$2 < this.nextWorkCycleTime || !$$3.closerToCenterThan($$1.position(), 1.0)) {
            return;
        }
        ItemStack $$4 = ItemStack.EMPTY;
        SimpleContainer $$5 = $$1.getInventory();
        int $$6 = $$5.getContainerSize();
        for (int $$7 = 0; $$7 < $$6; ++$$7) {
            ItemStack $$8 = $$5.getItem($$7);
            if (!$$8.is(Items.BONE_MEAL)) continue;
            $$4 = $$8;
            break;
        }
        if (!$$4.isEmpty() && BoneMealItem.growCrop($$4, $$0, $$3)) {
            $$0.levelEvent(1505, $$3, 15);
            this.cropPos = this.pickNextTarget($$0, $$1);
            this.setCurrentCropAsTarget($$1);
            this.nextWorkCycleTime = $$2 + 40L;
        }
        ++this.timeWorkedSoFar;
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (Villager)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (Villager)livingEntity, l);
    }
}

