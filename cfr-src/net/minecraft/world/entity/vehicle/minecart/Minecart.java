/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.vehicle.minecart;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Minecart
extends AbstractMinecart {
    private float rotationOffset;
    private float playerRotationOffset;

    public Minecart(EntityType<?> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    public InteractionResult interact(Player $$0, InteractionHand $$1) {
        if (!$$0.isSecondaryUseActive() && !this.isVehicle() && (this.level().isClientSide() || $$0.startRiding(this))) {
            this.playerRotationOffset = this.rotationOffset;
            if (!this.level().isClientSide()) {
                return $$0.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected Item getDropItem() {
        return Items.MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.MINECART);
    }

    @Override
    public void activateMinecart(ServerLevel $$0, int $$1, int $$2, int $$3, boolean $$4) {
        if ($$4) {
            if (this.isVehicle()) {
                this.ejectPassengers();
            }
            if (this.getHurtTime() == 0) {
                this.setHurtDir(-this.getHurtDir());
                this.setHurtTime(10);
                this.setDamage(50.0f);
                this.markHurt();
            }
        }
    }

    @Override
    public boolean isRideable() {
        return true;
    }

    @Override
    public void tick() {
        double $$0 = this.getYRot();
        Vec3 $$1 = this.position();
        super.tick();
        double $$2 = ((double)this.getYRot() - $$0) % 360.0;
        if (this.level().isClientSide() && $$1.distanceTo(this.position()) > 0.01) {
            this.rotationOffset += (float)$$2;
            this.rotationOffset %= 360.0f;
        }
    }

    @Override
    protected void positionRider(Entity $$0, Entity.MoveFunction $$1) {
        Player $$2;
        super.positionRider($$0, $$1);
        if (this.level().isClientSide() && $$0 instanceof Player && ($$2 = (Player)$$0).shouldRotateWithMinecart() && Minecart.useExperimentalMovement(this.level())) {
            float $$3 = (float)Mth.rotLerp(0.5, (double)this.playerRotationOffset, (double)this.rotationOffset);
            $$2.setYRot($$2.getYRot() - ($$3 - this.playerRotationOffset));
            this.playerRotationOffset = $$3;
        }
    }
}

