/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface ItemOwner {
    public Level level();

    public Vec3 position();

    public float getVisualRotationYInDegrees();

    default public @Nullable LivingEntity asLivingEntity() {
        return null;
    }

    public static ItemOwner offsetFromOwner(ItemOwner $$0, Vec3 $$1) {
        return new OffsetFromOwner($$0, $$1);
    }

    public record OffsetFromOwner(ItemOwner owner, Vec3 offset) implements ItemOwner
    {
        @Override
        public Level level() {
            return this.owner.level();
        }

        @Override
        public Vec3 position() {
            return this.owner.position().add(this.offset);
        }

        @Override
        public float getVisualRotationYInDegrees() {
            return this.owner.getVisualRotationYInDegrees();
        }

        @Override
        public @Nullable LivingEntity asLivingEntity() {
            return this.owner.asLivingEntity();
        }
    }
}

