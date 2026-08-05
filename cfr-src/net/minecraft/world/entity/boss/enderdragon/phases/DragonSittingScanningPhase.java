/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class DragonSittingScanningPhase
extends AbstractDragonSittingPhase {
    private static final int SITTING_SCANNING_IDLE_TICKS = 100;
    private static final int SITTING_ATTACK_Y_VIEW_RANGE = 10;
    private static final int SITTING_ATTACK_VIEW_RANGE = 20;
    private static final int SITTING_CHARGE_VIEW_RANGE = 150;
    private static final TargetingConditions CHARGE_TARGETING = TargetingConditions.forCombat().range(150.0);
    private final TargetingConditions scanTargeting = TargetingConditions.forCombat().range(20.0).selector(($$1, $$2) -> Math.abs($$1.getY() - $$0.getY()) <= 10.0);
    private int scanningTime;

    public DragonSittingScanningPhase(EnderDragon $$0) {
        super($$0);
    }

    @Override
    public void doServerTick(ServerLevel $$0) {
        ++this.scanningTime;
        Player $$1 = $$0.getNearestPlayer(this.scanTargeting, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if ($$1 != null) {
            if (this.scanningTime > 25) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_ATTACKING);
            } else {
                Vec3 $$2 = new Vec3($$1.getX() - this.dragon.getX(), 0.0, $$1.getZ() - this.dragon.getZ()).normalize();
                Vec3 $$3 = new Vec3(Mth.sin(this.dragon.getYRot() * ((float)Math.PI / 180)), 0.0, -Mth.cos(this.dragon.getYRot() * ((float)Math.PI / 180))).normalize();
                float $$4 = (float)$$3.dot($$2);
                float $$5 = (float)(Math.acos($$4) * 57.2957763671875) + 0.5f;
                if ($$5 < 0.0f || $$5 > 10.0f) {
                    float $$9;
                    double $$6 = $$1.getX() - this.dragon.head.getX();
                    double $$7 = $$1.getZ() - this.dragon.head.getZ();
                    double $$8 = Mth.clamp(Mth.wrapDegrees(180.0 - Mth.atan2($$6, $$7) * 57.2957763671875 - (double)this.dragon.getYRot()), -100.0, 100.0);
                    this.dragon.yRotA *= 0.8f;
                    float $$10 = $$9 = (float)Math.sqrt($$6 * $$6 + $$7 * $$7) + 1.0f;
                    if ($$9 > 40.0f) {
                        $$9 = 40.0f;
                    }
                    this.dragon.yRotA += (float)$$8 * (0.7f / $$9 / $$10);
                    this.dragon.setYRot(this.dragon.getYRot() + this.dragon.yRotA);
                }
            }
        } else if (this.scanningTime >= 100) {
            $$1 = $$0.getNearestPlayer(CHARGE_TARGETING, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
            if ($$1 != null) {
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                this.dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(new Vec3($$1.getX(), $$1.getY(), $$1.getZ()));
            }
        }
    }

    @Override
    public void begin() {
        this.scanningTime = 0;
    }

    public EnderDragonPhase<DragonSittingScanningPhase> getPhase() {
        return EnderDragonPhase.SITTING_SCANNING;
    }
}

