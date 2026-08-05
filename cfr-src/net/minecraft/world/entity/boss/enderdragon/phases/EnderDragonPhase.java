/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonDeathPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoldingPatternPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoverPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonLandingApproachPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonLandingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingAttackingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingFlamingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingScanningPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonTakeoffPhase;

public class EnderDragonPhase<T extends DragonPhaseInstance> {
    private static EnderDragonPhase<?>[] phases = new EnderDragonPhase[0];
    public static final EnderDragonPhase<DragonHoldingPatternPhase> HOLDING_PATTERN = EnderDragonPhase.create(DragonHoldingPatternPhase.class, "HoldingPattern");
    public static final EnderDragonPhase<DragonStrafePlayerPhase> STRAFE_PLAYER = EnderDragonPhase.create(DragonStrafePlayerPhase.class, "StrafePlayer");
    public static final EnderDragonPhase<DragonLandingApproachPhase> LANDING_APPROACH = EnderDragonPhase.create(DragonLandingApproachPhase.class, "LandingApproach");
    public static final EnderDragonPhase<DragonLandingPhase> LANDING = EnderDragonPhase.create(DragonLandingPhase.class, "Landing");
    public static final EnderDragonPhase<DragonTakeoffPhase> TAKEOFF = EnderDragonPhase.create(DragonTakeoffPhase.class, "Takeoff");
    public static final EnderDragonPhase<DragonSittingFlamingPhase> SITTING_FLAMING = EnderDragonPhase.create(DragonSittingFlamingPhase.class, "SittingFlaming");
    public static final EnderDragonPhase<DragonSittingScanningPhase> SITTING_SCANNING = EnderDragonPhase.create(DragonSittingScanningPhase.class, "SittingScanning");
    public static final EnderDragonPhase<DragonSittingAttackingPhase> SITTING_ATTACKING = EnderDragonPhase.create(DragonSittingAttackingPhase.class, "SittingAttacking");
    public static final EnderDragonPhase<DragonChargePlayerPhase> CHARGING_PLAYER = EnderDragonPhase.create(DragonChargePlayerPhase.class, "ChargingPlayer");
    public static final EnderDragonPhase<DragonDeathPhase> DYING = EnderDragonPhase.create(DragonDeathPhase.class, "Dying");
    public static final EnderDragonPhase<DragonHoverPhase> HOVERING = EnderDragonPhase.create(DragonHoverPhase.class, "Hover");
    private final Class<? extends DragonPhaseInstance> instanceClass;
    private final int id;
    private final String name;

    private EnderDragonPhase(int $$0, Class<? extends DragonPhaseInstance> $$1, String $$2) {
        this.id = $$0;
        this.instanceClass = $$1;
        this.name = $$2;
    }

    public DragonPhaseInstance createInstance(EnderDragon $$0) {
        try {
            Constructor<DragonPhaseInstance> $$1 = this.getConstructor();
            return $$1.newInstance($$0);
        }
        catch (Exception $$2) {
            throw new Error($$2);
        }
    }

    protected Constructor<? extends DragonPhaseInstance> getConstructor() throws NoSuchMethodException {
        return this.instanceClass.getConstructor(EnderDragon.class);
    }

    public int getId() {
        return this.id;
    }

    public String toString() {
        return this.name + " (#" + this.id + ")";
    }

    public static EnderDragonPhase<?> getById(int $$0) {
        if ($$0 < 0 || $$0 >= phases.length) {
            return HOLDING_PATTERN;
        }
        return phases[$$0];
    }

    public static int getCount() {
        return phases.length;
    }

    private static <T extends DragonPhaseInstance> EnderDragonPhase<T> create(Class<T> $$0, String $$1) {
        EnderDragonPhase<T> $$2 = new EnderDragonPhase<T>(phases.length, $$0, $$1);
        phases = Arrays.copyOf(phases, phases.length + 1);
        EnderDragonPhase.phases[$$2.getId()] = $$2;
        return $$2;
    }
}

