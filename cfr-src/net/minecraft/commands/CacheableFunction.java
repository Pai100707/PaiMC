/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.commands;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerFunctionManager;

public class CacheableFunction {
    public static final Codec<CacheableFunction> CODEC = Identifier.CODEC.xmap(CacheableFunction::new, CacheableFunction::getId);
    private final Identifier id;
    private boolean resolved;
    private Optional<CommandFunction<CommandSourceStack>> function = Optional.empty();

    public CacheableFunction(Identifier $$0) {
        this.id = $$0;
    }

    public Optional<CommandFunction<CommandSourceStack>> get(ServerFunctionManager $$0) {
        if (!this.resolved) {
            this.function = $$0.get(this.id);
            this.resolved = true;
        }
        return this.function;
    }

    public Identifier getId() {
        return this.id;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object $$0) {
        if ($$0 == this) {
            return true;
        }
        if (!($$0 instanceof CacheableFunction)) return false;
        CacheableFunction $$1 = (CacheableFunction)$$0;
        if (!this.getId().equals($$1.getId())) return false;
        return true;
    }
}

