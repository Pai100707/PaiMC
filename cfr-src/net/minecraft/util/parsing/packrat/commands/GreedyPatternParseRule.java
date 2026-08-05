/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public final class GreedyPatternParseRule
implements Rule<StringReader, String> {
    private final Pattern pattern;
    private final DelayedException<CommandSyntaxException> error;

    public GreedyPatternParseRule(Pattern $$0, DelayedException<CommandSyntaxException> $$1) {
        this.pattern = $$0;
        this.error = $$1;
    }

    @Override
    public String parse(ParseState<StringReader> $$0) {
        StringReader $$1 = $$0.input();
        String $$2 = $$1.getString();
        Matcher $$3 = this.pattern.matcher($$2).region($$1.getCursor(), $$2.length());
        if (!$$3.lookingAt()) {
            $$0.errorCollector().store($$0.mark(), this.error);
            return null;
        }
        $$1.setCursor($$3.end());
        return $$3.group(0);
    }

    @Override
    public /* synthetic */ Object parse(ParseState parseState) {
        return this.parse(parseState);
    }
}

