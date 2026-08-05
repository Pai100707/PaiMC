/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.syncher;

import net.minecraft.network.syncher.EntityDataSerializer;

public record EntityDataAccessor<T>(int id, EntityDataSerializer<T> serializer) {
    @Override
    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if ($$0 == null || this.getClass() != $$0.getClass()) {
            return false;
        }
        EntityDataAccessor $$1 = (EntityDataAccessor)$$0;
        return this.id == $$1.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return "<entity data: " + this.id + ">";
    }
}

