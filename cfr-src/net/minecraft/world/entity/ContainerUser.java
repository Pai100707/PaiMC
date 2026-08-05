/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;

public interface ContainerUser {
    public boolean hasContainerOpen(ContainerOpenersCounter var1, BlockPos var2);

    public double getContainerInteractionRange();

    default public LivingEntity getLivingEntity() {
        if (this instanceof LivingEntity) {
            return (LivingEntity)((Object)this);
        }
        throw new IllegalStateException("A container user must be a LivingEntity");
    }
}

