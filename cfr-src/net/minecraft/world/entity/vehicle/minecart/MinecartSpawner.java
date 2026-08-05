/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.vehicle.minecart;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MinecartSpawner
extends AbstractMinecart {
    private final BaseSpawner spawner = new BaseSpawner(){

        @Override
        public void broadcastEvent(Level $$0, BlockPos $$1, int $$2) {
            $$0.broadcastEntityEvent(MinecartSpawner.this, (byte)$$2);
        }
    };
    private final Runnable ticker;

    public MinecartSpawner(EntityType<? extends MinecartSpawner> $$0, Level $$1) {
        super($$0, $$1);
        this.ticker = this.createTicker($$1);
    }

    @Override
    protected Item getDropItem() {
        return Items.MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.MINECART);
    }

    private Runnable createTicker(Level $$0) {
        return $$0 instanceof ServerLevel ? () -> this.spawner.serverTick((ServerLevel)$$0, this.blockPosition()) : () -> this.spawner.clientTick($$0, this.blockPosition());
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.SPAWNER.defaultBlockState();
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.spawner.load(this.level(), this.blockPosition(), $$0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        this.spawner.save($$0);
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        this.spawner.onEventTriggered(this.level(), $$0);
    }

    @Override
    public void tick() {
        super.tick();
        this.ticker.run();
    }

    public BaseSpawner getSpawner() {
        return this.spawner;
    }
}

