/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public interface NeutralMob {
    public static final String TAG_ANGER_END_TIME = "anger_end_time";
    public static final String TAG_ANGRY_AT = "angry_at";
    public static final long NO_ANGER_END_TIME = -1L;

    public long getPersistentAngerEndTime();

    default public void setTimeToRemainAngry(long $$0) {
        this.setPersistentAngerEndTime(this.level().getGameTime() + $$0);
    }

    public void setPersistentAngerEndTime(long var1);

    public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget();

    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> var1);

    public void startPersistentAngerTimer();

    public Level level();

    default public void addPersistentAngerSaveData(ValueOutput $$0) {
        $$0.putLong(TAG_ANGER_END_TIME, this.getPersistentAngerEndTime());
        $$0.storeNullable(TAG_ANGRY_AT, EntityReference.codec(), this.getPersistentAngerTarget());
    }

    default public void readPersistentAngerSaveData(Level $$0, ValueInput $$1) {
        Optional<Long> $$2 = $$1.getLong(TAG_ANGER_END_TIME);
        if ($$2.isPresent()) {
            this.setPersistentAngerEndTime($$2.get());
        } else {
            Optional<Integer> $$3 = $$1.getInt("AngerTime");
            if ($$3.isPresent()) {
                this.setTimeToRemainAngry($$3.get().intValue());
            } else {
                this.setPersistentAngerEndTime(-1L);
            }
        }
        if (!($$0 instanceof ServerLevel)) {
            return;
        }
        this.setPersistentAngerTarget(EntityReference.read($$1, TAG_ANGRY_AT));
        this.setTarget(EntityReference.getLivingEntity(this.getPersistentAngerTarget(), $$0));
    }

    default public void updatePersistentAnger(ServerLevel $$0, boolean $$1) {
        LivingEntity $$2 = this.getTarget();
        EntityReference<LivingEntity> $$3 = this.getPersistentAngerTarget();
        if ($$2 != null && $$2.isDeadOrDying() && $$3 != null && $$3.matches($$2) && $$2 instanceof Mob) {
            this.stopBeingAngry();
            return;
        }
        if ($$2 != null) {
            if ($$3 == null || !$$3.matches($$2)) {
                this.setPersistentAngerTarget(EntityReference.of($$2));
            }
            this.startPersistentAngerTimer();
        }
        if (!($$3 == null || this.isAngry() || $$2 != null && NeutralMob.isValidPlayerTarget($$2) && $$1)) {
            this.stopBeingAngry();
        }
    }

    private static boolean isValidPlayerTarget(LivingEntity $$0) {
        Player $$1;
        return $$0 instanceof Player && !($$1 = (Player)$$0).isCreative() && !$$1.isSpectator();
    }

    default public boolean isAngryAt(LivingEntity $$0, ServerLevel $$1) {
        if (!this.canAttack($$0)) {
            return false;
        }
        if (NeutralMob.isValidPlayerTarget($$0) && this.isAngryAtAllPlayers($$1)) {
            return true;
        }
        EntityReference<LivingEntity> $$2 = this.getPersistentAngerTarget();
        return $$2 != null && $$2.matches($$0);
    }

    default public boolean isAngryAtAllPlayers(ServerLevel $$0) {
        return $$0.getGameRules().get(GameRules.UNIVERSAL_ANGER) != false && this.isAngry() && this.getPersistentAngerTarget() == null;
    }

    default public boolean isAngry() {
        long $$0 = this.getPersistentAngerEndTime();
        if ($$0 > 0L) {
            long $$1 = $$0 - this.level().getGameTime();
            return $$1 > 0L;
        }
        return false;
    }

    default public void playerDied(ServerLevel $$0, Player $$1) {
        if (!$$0.getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS).booleanValue()) {
            return;
        }
        EntityReference<LivingEntity> $$2 = this.getPersistentAngerTarget();
        if ($$2 == null || !$$2.matches($$1)) {
            return;
        }
        this.stopBeingAngry();
    }

    default public void forgetCurrentTargetAndRefreshUniversalAnger() {
        this.stopBeingAngry();
        this.startPersistentAngerTimer();
    }

    default public void stopBeingAngry() {
        this.setLastHurtByMob(null);
        this.setPersistentAngerTarget(null);
        this.setTarget(null);
        this.setPersistentAngerEndTime(-1L);
    }

    public @Nullable LivingEntity getLastHurtByMob();

    public void setLastHurtByMob(@Nullable LivingEntity var1);

    public void setTarget(@Nullable LivingEntity var1);

    public boolean canAttack(LivingEntity var1);

    public @Nullable LivingEntity getTarget();
}

