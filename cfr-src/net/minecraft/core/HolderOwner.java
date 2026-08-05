/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core;

public interface HolderOwner<T> {
    default public boolean canSerializeIn(HolderOwner<T> $$0) {
        return $$0 == this;
    }
}

