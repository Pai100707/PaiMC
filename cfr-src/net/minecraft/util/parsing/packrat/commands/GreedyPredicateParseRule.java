/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;
import org.jspecify.annotations.Nullable;

public abstract class GreedyPredicateParseRule
implements Rule<StringReader, String> {
    private final int minSize;
    private final int maxSize;
    private final DelayedException<CommandSyntaxException> error;

    public GreedyPredicateParseRule(int $$0, DelayedException<CommandSyntaxException> $$1) {
        this($$0, Integer.MAX_VALUE, $$1);
    }

    public GreedyPredicateParseRule(int $$0, int $$1, DelayedException<CommandSyntaxException> $$2) {
        this.minSize = $$0;
        this.maxSize = $$1;
        this.error = $$2;
    }

    @Override
    public @Nullable String parse(ParseState<StringReader> $$0) {
        int $$3;
        int $$4;
        StringReader $$1 = $$0.input();
        String $$2 = $$1.getString();
        for ($$4 = $$3 = $$1.getCursor(); $$4 < $$2.length() && this.isAccepted($$2.charAt($$4)) && $$4 - $$3 < this.maxSize; ++$$4) {
        }
        int $$5 = $$4 - $$3;
        if ($$5 < this.minSize) {
            $$0.errorCollector().store($$0.mark(), this.error);
            return null;
        }
        $$1.setCursor($$4);
        return $$2.substring($$3, $$4);
    }

    protected abstract boolean isAccepted(char var1);

    @Override
    public /* synthetic */ @Nullable Object parse(ParseState parseState) {
        return this.parse(parseState);
    }
}

