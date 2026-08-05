/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.vehicle.minecart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class MinecartTNT
extends AbstractMinecart {
    private static final byte EVENT_PRIME = 10;
    private static final String TAG_EXPLOSION_POWER = "explosion_power";
    private static final String TAG_EXPLOSION_SPEED_FACTOR = "explosion_speed_factor";
    private static final String TAG_FUSE = "fuse";
    private static final float DEFAULT_EXPLOSION_POWER_BASE = 4.0f;
    private static final float DEFAULT_EXPLOSION_SPEED_FACTOR = 1.0f;
    private static final int NO_FUSE = -1;
    private @Nullable DamageSource ignitionSource;
    private int fuse = -1;
    private float explosionPowerBase = 4.0f;
    private float explosionSpeedFactor = 1.0f;

    public MinecartTNT(EntityType<? extends MinecartTNT> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.TNT.defaultBlockState();
    }

    @Override
    public void tick() {
        double $$0;
        super.tick();
        if (this.fuse > 0) {
            --this.fuse;
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
        } else if (this.fuse == 0) {
            this.explode(this.ignitionSource, this.getDeltaMovement().horizontalDistanceSqr());
        }
        if (this.horizontalCollision && ($$0 = this.getDeltaMovement().horizontalDistanceSqr()) >= (double)0.01f) {
            this.explode(this.ignitionSource, $$0);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        AbstractArrow $$4;
        Entity $$3 = $$1.getDirectEntity();
        if ($$3 instanceof AbstractArrow && ($$4 = (AbstractArrow)$$3).isOnFire()) {
            DamageSource $$5 = this.damageSources().explosion(this, $$1.getEntity());
            this.explode($$5, $$4.getDeltaMovement().lengthSqr());
        }
        return super.hurtServer($$0, $$1, $$2);
    }

    @Override
    public void destroy(ServerLevel $$0, DamageSource $$1) {
        double $$2 = this.getDeltaMovement().horizontalDistanceSqr();
        if (MinecartTNT.damageSourceIgnitesTnt($$1) || $$2 >= (double)0.01f) {
            if (this.fuse < 0) {
                this.primeFuse($$1);
                this.fuse = this.random.nextInt(20) + this.random.nextInt(20);
            }
            return;
        }
        this.destroy($$0, this.getDropItem());
    }

    @Override
    protected Item getDropItem() {
        return Items.TNT_MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.TNT_MINECART);
    }

    protected void explode(@Nullable DamageSource $$0, double $$1) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$2 = (ServerLevel)level;
            if ($$2.getGameRules().get(GameRules.TNT_EXPLODES).booleanValue()) {
                double $$3 = Math.min(Math.sqrt($$1), 5.0);
                $$2.explode(this, $$0, null, this.getX(), this.getY(), this.getZ(), (float)((double)this.explosionPowerBase + (double)this.explosionSpeedFactor * this.random.nextDouble() * 1.5 * $$3), false, Level.ExplosionInteraction.TNT);
                this.discard();
            } else if (this.isPrimed()) {
                this.discard();
            }
        }
    }

    @Override
    public boolean causeFallDamage(double $$0, float $$1, DamageSource $$2) {
        if ($$0 >= 3.0) {
            double $$3 = $$0 / 10.0;
            this.explode(this.ignitionSource, $$3 * $$3);
        }
        return super.causeFallDamage($$0, $$1, $$2);
    }

    @Override
    public void activateMinecart(ServerLevel $$0, int $$1, int $$2, int $$3, boolean $$4) {
        if ($$4 && this.fuse < 0) {
            this.primeFuse(null);
        }
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 10) {
            this.primeFuse(null);
        } else {
            super.handleEntityEvent($$0);
        }
    }

    public void primeFuse(@Nullable DamageSource $$0) {
        ServerLevel $$1;
        Level level = this.level();
        if (level instanceof ServerLevel && !($$1 = (ServerLevel)level).getGameRules().get(GameRules.TNT_EXPLODES).booleanValue()) {
            return;
        }
        this.fuse = 80;
        if (!this.level().isClientSide()) {
            if ($$0 != null && this.ignitionSource == null) {
                this.ignitionSource = this.damageSources().explosion(this, $$0.getEntity());
            }
            this.level().broadcastEntityEvent(this, (byte)10);
            if (!this.isSilent()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    public int getFuse() {
        return this.fuse;
    }

    public boolean isPrimed() {
        return this.fuse > -1;
    }

    @Override
    public float getBlockExplosionResistance(Explosion $$0, BlockGetter $$1, BlockPos $$2, BlockState $$3, FluidState $$4, float $$5) {
        if (this.isPrimed() && ($$3.is(BlockTags.RAILS) || $$1.getBlockState($$2.above()).is(BlockTags.RAILS))) {
            return 0.0f;
        }
        return super.getBlockExplosionResistance($$0, $$1, $$2, $$3, $$4, $$5);
    }

    @Override
    public boolean shouldBlockExplode(Explosion $$0, BlockGetter $$1, BlockPos $$2, BlockState $$3, float $$4) {
        if (this.isPrimed() && ($$3.is(BlockTags.RAILS) || $$1.getBlockState($$2.above()).is(BlockTags.RAILS))) {
            return false;
        }
        return super.shouldBlockExplode($$0, $$1, $$2, $$3, $$4);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.fuse = $$0.getIntOr(TAG_FUSE, -1);
        this.explosionPowerBase = Mth.clamp($$0.getFloatOr(TAG_EXPLOSION_POWER, 4.0f), 0.0f, 128.0f);
        this.explosionSpeedFactor = Mth.clamp($$0.getFloatOr(TAG_EXPLOSION_SPEED_FACTOR, 1.0f), 0.0f, 128.0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putInt(TAG_FUSE, this.fuse);
        if (this.explosionPowerBase != 4.0f) {
            $$0.putFloat(TAG_EXPLOSION_POWER, this.explosionPowerBase);
        }
        if (this.explosionSpeedFactor != 1.0f) {
            $$0.putFloat(TAG_EXPLOSION_SPEED_FACTOR, this.explosionSpeedFactor);
        }
    }

    @Override
    protected boolean shouldSourceDestroy(DamageSource $$0) {
        return MinecartTNT.damageSourceIgnitesTnt($$0);
    }

    private static boolean damageSourceIgnitesTnt(DamageSource $$0) {
        Entity entity = $$0.getDirectEntity();
        if (entity instanceof Projectile) {
            Projectile $$1 = (Projectile)entity;
            return $$1.isOnFire();
        }
        return $$0.is(DamageTypeTags.IS_FIRE) || $$0.is(DamageTypeTags.IS_EXPLOSION);
    }
}

