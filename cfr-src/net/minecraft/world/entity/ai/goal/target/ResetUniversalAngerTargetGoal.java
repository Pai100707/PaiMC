/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal.target;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;

public class ResetUniversalAngerTargetGoal<T extends Mob>
extends Goal {
    private static final int ALERT_RANGE_Y = 10;
    private final T mob;
    private final boolean alertOthersOfSameType;
    private int lastHurtByPlayerTimestamp;

    public ResetUniversalAngerTargetGoal(T $$0, boolean $$1) {
        this.mob = $$0;
        this.alertOthersOfSameType = $$1;
    }

    @Override
    public boolean canUse() {
        return ResetUniversalAngerTargetGoal.getServerLevel(this.mob).getGameRules().get(GameRules.UNIVERSAL_ANGER) != false && this.wasHurtByPlayer();
    }

    private boolean wasHurtByPlayer() {
        return ((LivingEntity)this.mob).getLastHurtByMob() != null && ((LivingEntity)this.mob).getLastHurtByMob().getType() == EntityType.PLAYER && ((LivingEntity)this.mob).getLastHurtByMobTimestamp() > this.lastHurtByPlayerTimestamp;
    }

    @Override
    public void start() {
        this.lastHurtByPlayerTimestamp = ((LivingEntity)this.mob).getLastHurtByMobTimestamp();
        ((NeutralMob)this.mob).forgetCurrentTargetAndRefreshUniversalAnger();
        if (this.alertOthersOfSameType) {
            this.getNearbyMobsOfSameType().stream().filter($$0 -> $$0 != this.mob).map($$0 -> (NeutralMob)((Object)$$0)).forEach(NeutralMob::forgetCurrentTargetAndRefreshUniversalAnger);
        }
        super.start();
    }

    private List<? extends Mob> getNearbyMobsOfSameType() {
        double $$0 = ((LivingEntity)this.mob).getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB $$1 = AABB.unitCubeFromLowerCorner(((Entity)this.mob).position()).inflate($$0, 10.0, $$0);
        return ((Entity)this.mob).level().getEntitiesOfClass(this.mob.getClass(), $$1, EntitySelector.NO_SPECTATORS);
    }
}

