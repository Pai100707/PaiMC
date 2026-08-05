/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class OminousItemSpawner
extends Entity {
    private static final int SPAWN_ITEM_DELAY_MIN = 60;
    private static final int SPAWN_ITEM_DELAY_MAX = 120;
    private static final String TAG_SPAWN_ITEM_AFTER_TICKS = "spawn_item_after_ticks";
    private static final String TAG_ITEM = "item";
    private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(OminousItemSpawner.class, EntityDataSerializers.ITEM_STACK);
    public static final int TICKS_BEFORE_ABOUT_TO_SPAWN_SOUND = 36;
    private long spawnItemAfterTicks;

    public OminousItemSpawner(EntityType<? extends OminousItemSpawner> $$0, Level $$1) {
        super($$0, $$1);
        this.noPhysics = true;
    }

    public static OminousItemSpawner create(Level $$0, ItemStack $$1) {
        OminousItemSpawner $$2 = new OminousItemSpawner((EntityType<? extends OminousItemSpawner>)EntityType.OMINOUS_ITEM_SPAWNER, $$0);
        $$2.spawnItemAfterTicks = $$0.random.nextIntBetweenInclusive(60, 120);
        $$2.setItem($$1);
        return $$2;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$0 = (ServerLevel)level;
            this.tickServer($$0);
        } else {
            this.tickClient();
        }
    }

    private void tickServer(ServerLevel $$0) {
        if ((long)this.tickCount == this.spawnItemAfterTicks - 36L) {
            $$0.playSound(null, this.blockPosition(), SoundEvents.TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, SoundSource.NEUTRAL);
        }
        if ((long)this.tickCount >= this.spawnItemAfterTicks) {
            this.spawnItem();
            this.kill($$0);
        }
    }

    private void tickClient() {
        if (this.level().getGameTime() % 5L == 0L) {
            this.addParticles();
        }
    }

    /*
     * WARNING - void declaration
     */
    private void spawnItem() {
        ItemEntity $$5;
        void $$1;
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel $$0 = (ServerLevel)level;
        ItemStack $$2 = this.getItem();
        if ($$2.isEmpty()) {
            return;
        }
        Item item = $$2.getItem();
        if (item instanceof ProjectileItem) {
            ProjectileItem $$3 = (ProjectileItem)((Object)item);
            Entity $$4 = this.spawnProjectile((ServerLevel)$$1, $$3, $$2);
        } else {
            $$5 = new ItemEntity((Level)$$1, this.getX(), this.getY(), this.getZ(), $$2);
            $$1.addFreshEntity($$5);
        }
        $$1.levelEvent(3021, this.blockPosition(), 1);
        $$1.gameEvent((Entity)$$5, GameEvent.ENTITY_PLACE, this.position());
        this.setItem(ItemStack.EMPTY);
    }

    private Entity spawnProjectile(ServerLevel $$0, ProjectileItem $$12, ItemStack $$2) {
        ProjectileItem.DispenseConfig $$3 = $$12.createDispenseConfig();
        $$3.overrideDispenseEvent().ifPresent($$1 -> $$0.levelEvent($$1, this.blockPosition(), 0));
        Direction $$4 = Direction.DOWN;
        Projectile $$5 = Projectile.spawnProjectileUsingShoot($$12.asProjectile($$0, this.position(), $$2, $$4), $$0, $$2, $$4.getStepX(), $$4.getStepY(), $$4.getStepZ(), $$3.power(), $$3.uncertainty());
        $$5.setOwner(this);
        return $$5;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        this.setItem($$0.read(TAG_ITEM, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        this.spawnItemAfterTicks = $$0.getLongOr(TAG_SPAWN_ITEM_AFTER_TICKS, 0L);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        if (!this.getItem().isEmpty()) {
            $$0.store(TAG_ITEM, ItemStack.CODEC, this.getItem());
        }
        $$0.putLong(TAG_SPAWN_ITEM_AFTER_TICKS, this.spawnItemAfterTicks);
    }

    @Override
    protected boolean canAddPassenger(Entity $$0) {
        return false;
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return false;
    }

    @Override
    protected void addPassenger(Entity $$0) {
        throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    public void addParticles() {
        Vec3 $$0 = this.position();
        int $$1 = this.random.nextIntBetweenInclusive(1, 3);
        for (int $$2 = 0; $$2 < $$1; ++$$2) {
            double $$3 = 0.4;
            Vec3 $$4 = new Vec3(this.getX() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()), this.getY() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()), this.getZ() + 0.4 * (this.random.nextGaussian() - this.random.nextGaussian()));
            Vec3 $$5 = $$0.vectorTo($$4);
            this.level().addParticle(ParticleTypes.OMINOUS_SPAWNING, $$0.x(), $$0.y(), $$0.z(), $$5.x(), $$5.y(), $$5.z());
        }
    }

    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM);
    }

    private void setItem(ItemStack $$0) {
        this.getEntityData().set(DATA_ITEM, $$0);
    }

    @Override
    public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        return false;
    }
}

