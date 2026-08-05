/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.commands.arguments.selector;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;

public record SelectorPattern(String pattern, EntitySelector resolved) {
    public static final Codec<SelectorPattern> CODEC = Codec.STRING.comapFlatMap(SelectorPattern::parse, SelectorPattern::pattern);

    public static DataResult<SelectorPattern> parse(String $$0) {
        try {
            EntitySelectorParser $$1 = new EntitySelectorParser(new StringReader($$0), true);
            return DataResult.success((Object)new SelectorPattern($$0, $$1.parse()));
        }
        catch (CommandSyntaxException $$2) {
            return DataResult.error(() -> "Invalid selector component: " + $$0 + ": " + $$2.getMessage());
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object $$0) {
        if (!($$0 instanceof SelectorPattern)) return false;
        SelectorPattern $$1 = (SelectorPattern)$$0;
        if (!this.pattern.equals($$1.pattern)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.pattern.hashCode();
    }

    @Override
    public String toString() {
        return this.pattern;
    }
}

