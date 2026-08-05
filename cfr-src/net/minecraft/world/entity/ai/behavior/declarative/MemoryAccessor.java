/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.K1
 */
package net.minecraft.world.entity.ai.behavior.declarative;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.K1;
import java.util.Optional;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public final class MemoryAccessor<F extends K1, Value> {
    private final Brain<?> brain;
    private final MemoryModuleType<Value> memoryType;
    private final App<F, Value> value;

    public MemoryAccessor(Brain<?> $$0, MemoryModuleType<Value> $$1, App<F, Value> $$2) {
        this.brain = $$0;
        this.memoryType = $$1;
        this.value = $$2;
    }

    public App<F, Value> value() {
        return this.value;
    }

    public void set(Value $$0) {
        this.brain.setMemory(this.memoryType, Optional.of($$0));
    }

    public void setOrErase(Optional<Value> $$0) {
        this.brain.setMemory(this.memoryType, $$0);
    }

    public void setWithExpiry(Value $$0, long $$1) {
        this.brain.setMemoryWithExpiry(this.memoryType, $$0, $$1);
    }

    public void erase() {
        this.brain.eraseMemory(this.memoryType);
    }
}

