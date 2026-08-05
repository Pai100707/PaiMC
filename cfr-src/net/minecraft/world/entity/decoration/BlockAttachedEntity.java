/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.decoration;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class BlockAttachedEntity
extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int checkInterval;
    protected BlockPos pos;

    protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> $$0, Level $$1) {
        super($$0, $$1);
    }

    protected BlockAttachedEntity(EntityType<? extends BlockAttachedEntity> $$0, Level $$1, BlockPos $$2) {
        this($$0, $$1);
        this.pos = $$2;
    }

    protected abstract void recalculateBoundingBox();

    @Override
    public void tick() {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$0 = (ServerLevel)level;
            this.checkBelowWorld();
            if (this.checkInterval++ == 100) {
                this.checkInterval = 0;
                if (!this.isRemoved() && !this.survives()) {
                    this.discard();
                    this.dropItem($$0, null);
                }
            }
        }
    }

    public abstract boolean survives();

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity $$0) {
        if ($$0 instanceof Player) {
            Player $$1 = (Player)$$0;
            if (!this.level().mayInteract($$1, this.pos)) {
                return true;
            }
            return this.hurtOrSimulate(this.damageSources().playerAttack($$1), 0.0f);
        }
        return false;
    }

    @Override
    public boolean hurtClient(DamageSource $$0) {
        return !this.isInvulnerableToBase($$0);
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (this.isInvulnerableToBase($$1)) {
            return false;
        }
        if (!$$0.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue() && $$1.getEntity() instanceof Mob) {
            return false;
        }
        if (!this.isRemoved()) {
            this.kill($$0);
            this.markHurt();
            this.dropItem($$0, $$1.getEntity());
        }
        return true;
    }

    @Override
    public boolean ignoreExplosion(Explosion $$0) {
        Entity $$1 = $$0.getDirectSourceEntity();
        if ($$1 != null && $$1.isInWater()) {
            return true;
        }
        if ($$0.shouldAffectBlocklikeEntities()) {
            return super.ignoreExplosion($$0);
        }
        return true;
    }

    @Override
    public void move(MoverType $$0, Vec3 $$1) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$2 = (ServerLevel)level;
            if (!this.isRemoved() && $$1.lengthSqr() > 0.0) {
                this.kill($$2);
                this.dropItem($$2, null);
            }
        }
    }

    @Override
    public void push(double $$0, double $$1, double $$2) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$3 = (ServerLevel)level;
            if (!this.isRemoved() && $$0 * $$0 + $$1 * $$1 + $$2 * $$2 > 0.0) {
                this.kill($$3);
                this.dropItem($$3, null);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        $$0.store("block_pos", BlockPos.CODEC, this.getPos());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        BlockPos $$1 = $$0.read("block_pos", BlockPos.CODEC).orElse(null);
        if ($$1 == null || !$$1.closerThan(this.blockPosition(), 16.0)) {
            LOGGER.error("Block-attached entity at invalid position: {}", (Object)$$1);
            return;
        }
        this.pos = $$1;
    }

    public abstract void dropItem(ServerLevel var1, @Nullable Entity var2);

    @Override
    protected boolean repositionEntityAfterLoad() {
        return false;
    }

    @Override
    public void setPos(double $$0, double $$1, double $$2) {
        this.pos = BlockPos.containing($$0, $$1, $$2);
        this.recalculateBoundingBox();
        this.needsSync = true;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    @Override
    public void thunderHit(ServerLevel $$0, LightningBolt $$1) {
    }

    @Override
    public void refreshDimensions() {
    }
}

