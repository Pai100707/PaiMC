/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.function.Consumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BaseFireBlock;

public enum InsideBlockEffectType {
    FREEZE($$0 -> {
        $$0.setIsInPowderSnow(true);
        if ($$0.canFreeze()) {
            $$0.setTicksFrozen(Math.min($$0.getTicksRequiredToFreeze(), $$0.getTicksFrozen() + 1));
        }
    }),
    CLEAR_FREEZE(Entity::clearFreeze),
    FIRE_IGNITE(BaseFireBlock::fireIgnite),
    LAVA_IGNITE(Entity::lavaIgnite),
    EXTINGUISH(Entity::clearFire);

    private final Consumer<Entity> effect;

    private InsideBlockEffectType(Consumer<Entity> $$0) {
        this.effect = $$0;
    }

    public Consumer<Entity> effect() {
        return this.effect;
    }
}

