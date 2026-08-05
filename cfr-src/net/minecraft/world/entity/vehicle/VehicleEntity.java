/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.vehicle;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;

public abstract class VehicleEntity
extends Entity {
    protected static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);

    public VehicleEntity(EntityType<?> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    public boolean hurtClient(DamageSource $$0) {
        return true;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (this.isRemoved()) {
            return true;
        }
        if (this.isInvulnerableToBase($$1)) {
            return false;
        }
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.markHurt();
        this.setDamage(this.getDamage() + $$2 * 10.0f);
        this.gameEvent(GameEvent.ENTITY_DAMAGE, $$1.getEntity());
        var6_4 = $$1.getEntity();
        if (!(var6_4 instanceof Player)) ** GOTO lbl-1000
        $$3 = (Player)var6_4;
        if ($$3.getAbilities().instabuild) {
            v0 = true;
        } else lbl-1000:
        // 2 sources

        {
            v0 = $$4 = false;
        }
        if ($$4 == false && this.getDamage() > 40.0f || this.shouldSourceDestroy($$1)) {
            this.destroy($$0, $$1);
        } else if ($$4) {
            this.discard();
        }
        return true;
    }

    protected boolean shouldSourceDestroy(DamageSource $$0) {
        return false;
    }

    @Override
    public boolean ignoreExplosion(Explosion $$0) {
        return $$0.getIndirectSourceEntity() instanceof Mob && $$0.level().getGameRules().get(GameRules.MOB_GRIEFING) == false;
    }

    public void destroy(ServerLevel $$0, Item $$1) {
        this.kill($$0);
        if (!$$0.getGameRules().get(GameRules.ENTITY_DROPS).booleanValue()) {
            return;
        }
        ItemStack $$2 = new ItemStack($$1);
        $$2.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        this.spawnAtLocation($$0, $$2);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_ID_HURT, 0);
        $$0.define(DATA_ID_HURTDIR, 1);
        $$0.define(DATA_ID_DAMAGE, Float.valueOf(0.0f));
    }

    public void setHurtTime(int $$0) {
        this.entityData.set(DATA_ID_HURT, $$0);
    }

    public void setHurtDir(int $$0) {
        this.entityData.set(DATA_ID_HURTDIR, $$0);
    }

    public void setDamage(float $$0) {
        this.entityData.set(DATA_ID_DAMAGE, Float.valueOf($$0));
    }

    public float getDamage() {
        return this.entityData.get(DATA_ID_DAMAGE).floatValue();
    }

    public int getHurtTime() {
        return this.entityData.get(DATA_ID_HURT);
    }

    public int getHurtDir() {
        return this.entityData.get(DATA_ID_HURTDIR);
    }

    protected void destroy(ServerLevel $$0, DamageSource $$1) {
        this.destroy($$0, this.getDropItem());
    }

    @Override
    public int getDimensionChangingDelay() {
        return 10;
    }

    protected abstract Item getDropItem();
}

