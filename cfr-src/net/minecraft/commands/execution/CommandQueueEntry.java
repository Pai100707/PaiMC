/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.execution;

import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;

public record CommandQueueEntry<T>(Frame frame, EntryAction<T> action) {
    public void execute(ExecutionContext<T> $$0) {
        this.action.execute($$0, this.frame);
    }
}

