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

public abstract class NumberRunParseRule
implements Rule<StringReader, String> {
    private final DelayedException<CommandSyntaxException> noValueError;
    private final DelayedException<CommandSyntaxException> underscoreNotAllowedError;

    public NumberRunParseRule(DelayedException<CommandSyntaxException> $$0, DelayedException<CommandSyntaxException> $$1) {
        this.noValueError = $$0;
        this.underscoreNotAllowedError = $$1;
    }

    @Override
    public @Nullable String parse(ParseState<StringReader> $$0) {
        int $$3;
        int $$4;
        StringReader $$1 = $$0.input();
        $$1.skipWhitespace();
        String $$2 = $$1.getString();
        for ($$4 = $$3 = $$1.getCursor(); $$4 < $$2.length() && this.isAccepted($$2.charAt($$4)); ++$$4) {
        }
        int $$5 = $$4 - $$3;
        if ($$5 == 0) {
            $$0.errorCollector().store($$0.mark(), this.noValueError);
            return null;
        }
        if ($$2.charAt($$3) == '_' || $$2.charAt($$4 - 1) == '_') {
            $$0.errorCollector().store($$0.mark(), this.underscoreNotAllowedError);
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

