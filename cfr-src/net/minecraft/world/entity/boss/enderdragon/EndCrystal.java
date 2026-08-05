/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.boss.enderdragon;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class EndCrystal
extends Entity {
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_BEAM_TARGET = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> DATA_SHOW_BOTTOM = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.BOOLEAN);
    private static final boolean DEFAULT_SHOW_BOTTOM = true;
    public int time;

    public EndCrystal(EntityType<? extends EndCrystal> $$0, Level $$1) {
        super($$0, $$1);
        this.blocksBuilding = true;
        this.time = this.random.nextInt(100000);
    }

    public EndCrystal(Level $$0, double $$1, double $$2, double $$3) {
        this((EntityType<? extends EndCrystal>)EntityType.END_CRYSTAL, $$0);
        this.setPos($$1, $$2, $$3);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_BEAM_TARGET, Optional.empty());
        $$0.define(DATA_SHOW_BOTTOM, true);
    }

    @Override
    public void tick() {
        ++this.time;
        this.applyEffectsFromBlocks();
        this.handlePortal();
        if (this.level() instanceof ServerLevel) {
            BlockPos $$0 = this.blockPosition();
            if (((ServerLevel)this.level()).getDragonFight() != null && this.level().getBlockState($$0).isAir()) {
                this.level().setBlockAndUpdate($$0, BaseFireBlock.getState(this.level(), $$0));
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        $$0.storeNullable("beam_target", BlockPos.CODEC, this.getBeamTarget());
        $$0.putBoolean("ShowBottom", this.showsBottom());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        this.setBeamTarget($$0.read("beam_target", BlockPos.CODEC).orElse(null));
        this.setShowBottom($$0.getBooleanOr("ShowBottom", true));
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public final boolean hurtClient(DamageSource $$0) {
        if (this.isInvulnerableToBase($$0)) {
            return false;
        }
        return !($$0.getEntity() instanceof EnderDragon);
    }

    @Override
    public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (this.isInvulnerableToBase($$1)) {
            return false;
        }
        if ($$1.getEntity() instanceof EnderDragon) {
            return false;
        }
        if (!this.isRemoved()) {
            this.remove(Entity.RemovalReason.KILLED);
            if (!$$1.is(DamageTypeTags.IS_EXPLOSION)) {
                DamageSource $$3 = $$1.getEntity() != null ? this.damageSources().explosion(this, $$1.getEntity()) : null;
                $$0.explode(this, $$3, null, this.getX(), this.getY(), this.getZ(), 6.0f, false, Level.ExplosionInteraction.BLOCK);
            }
            this.onDestroyedBy($$0, $$1);
        }
        return true;
    }

    @Override
    public void kill(ServerLevel $$0) {
        this.onDestroyedBy($$0, this.damageSources().generic());
        super.kill($$0);
    }

    private void onDestroyedBy(ServerLevel $$0, DamageSource $$1) {
        EndDragonFight $$2 = $$0.getDragonFight();
        if ($$2 != null) {
            $$2.onCrystalDestroyed(this, $$1);
        }
    }

    public void setBeamTarget(@Nullable BlockPos $$0) {
        this.getEntityData().set(DATA_BEAM_TARGET, Optional.ofNullable($$0));
    }

    public @Nullable BlockPos getBeamTarget() {
        return this.getEntityData().get(DATA_BEAM_TARGET).orElse(null);
    }

    public void setShowBottom(boolean $$0) {
        this.getEntityData().set(DATA_SHOW_BOTTOM, $$0);
    }

    public boolean showsBottom() {
        return this.getEntityData().get(DATA_SHOW_BOTTOM);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double $$0) {
        return super.shouldRenderAtSqrDistance($$0) || this.getBeamTarget() != null;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.END_CRYSTAL);
    }
}

