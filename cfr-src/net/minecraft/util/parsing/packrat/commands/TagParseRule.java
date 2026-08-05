/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;
import org.jspecify.annotations.Nullable;

public class TagParseRule<T>
implements Rule<StringReader, Dynamic<?>> {
    private final TagParser<T> parser;

    public TagParseRule(DynamicOps<T> $$0) {
        this.parser = TagParser.create($$0);
    }

    @Override
    public @Nullable Dynamic<T> parse(ParseState<StringReader> $$0) {
        $$0.input().skipWhitespace();
        int $$1 = $$0.mark();
        try {
            return new Dynamic(this.parser.getOps(), this.parser.parseAsArgument($$0.input()));
        }
        catch (Exception $$2) {
            $$0.errorCollector().store($$1, $$2);
            return null;
        }
    }

    @Override
    public /* synthetic */ @Nullable Object parse(ParseState parseState) {
        return this.parse((ParseState<StringReader>)parseState);
    }
}

