/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.inventory;

import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface RemoteSlot {
    public static final RemoteSlot PLACEHOLDER = new RemoteSlot(){

        @Override
        public void receive(HashedStack $$0) {
        }

        @Override
        public void force(ItemStack $$0) {
        }

        @Override
        public boolean matches(ItemStack $$0) {
            return true;
        }
    };

    public void force(ItemStack var1);

    public void receive(HashedStack var1);

    public boolean matches(ItemStack var1);

    public static class Synchronized
    implements RemoteSlot {
        private final HashedPatchMap.HashGenerator hasher;
        private @Nullable ItemStack remoteStack = null;
        private @Nullable HashedStack remoteHash = null;

        public Synchronized(HashedPatchMap.HashGenerator $$0) {
            this.hasher = $$0;
        }

        @Override
        public void force(ItemStack $$0) {
            this.remoteStack = $$0.copy();
            this.remoteHash = null;
        }

        @Override
        public void receive(HashedStack $$0) {
            this.remoteStack = null;
            this.remoteHash = $$0;
        }

        @Override
        public boolean matches(ItemStack $$0) {
            if (this.remoteStack != null) {
                return ItemStack.matches(this.remoteStack, $$0);
            }
            if (this.remoteHash != null && this.remoteHash.matches($$0, this.hasher)) {
                this.remoteStack = $$0.copy();
                return true;
            }
            return false;
        }

        public void copyFrom(Synchronized $$0) {
            this.remoteStack = $$0.remoteStack;
            this.remoteHash = $$0.remoteHash;
        }
    }
}

