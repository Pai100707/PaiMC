/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.attribute;

import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.SpatialAttributeInterpolator;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface EnvironmentAttributeReader {
    public static final EnvironmentAttributeReader EMPTY = new EnvironmentAttributeReader(){

        @Override
        public <Value> Value getDimensionValue(EnvironmentAttribute<Value> $$0) {
            return $$0.defaultValue();
        }

        @Override
        public <Value> Value getValue(EnvironmentAttribute<Value> $$0, Vec3 $$1, @Nullable SpatialAttributeInterpolator $$2) {
            return $$0.defaultValue();
        }
    };

    public <Value> Value getDimensionValue(EnvironmentAttribute<Value> var1);

    default public <Value> Value getValue(EnvironmentAttribute<Value> $$0, BlockPos $$1) {
        return this.getValue($$0, Vec3.atCenterOf($$1));
    }

    default public <Value> Value getValue(EnvironmentAttribute<Value> $$0, Vec3 $$1) {
        return this.getValue($$0, $$1, null);
    }

    public <Value> Value getValue(EnvironmentAttribute<Value> var1, Vec3 var2, @Nullable SpatialAttributeInterpolator var3);
}

