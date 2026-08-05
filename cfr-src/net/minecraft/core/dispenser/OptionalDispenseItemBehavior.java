/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.dispenser;

import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;

public abstract class OptionalDispenseItemBehavior
extends DefaultDispenseItemBehavior {
    private boolean success = true;

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean $$0) {
        this.success = $$0;
    }

    @Override
    protected void playSound(BlockSource $$0) {
        $$0.level().levelEvent(this.isSuccess() ? 1000 : 1001, $$0.pos(), 0);
    }
}

