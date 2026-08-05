/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public interface OwnableEntity {
    public @Nullable EntityReference<LivingEntity> getOwnerReference();

    public Level level();

    default public @Nullable LivingEntity getOwner() {
        return EntityReference.getLivingEntity(this.getOwnerReference(), this.level());
    }

    default public @Nullable LivingEntity getRootOwner() {
        ObjectArraySet $$0 = new ObjectArraySet();
        LivingEntity $$1 = this.getOwner();
        $$0.add(this);
        while ($$1 instanceof OwnableEntity) {
            OwnableEntity $$2 = (OwnableEntity)((Object)$$1);
            LivingEntity $$3 = $$2.getOwner();
            if ($$0.contains($$3)) {
                return null;
            }
            $$0.add($$1);
            $$1 = $$2.getOwner();
        }
        return $$1;
    }
}

