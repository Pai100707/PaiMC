/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.util.parsing.packrat;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;

public interface DelayedException<T extends Exception> {
    public T create(String var1, int var2);

    public static DelayedException<CommandSyntaxException> create(SimpleCommandExceptionType $$0) {
        return ($$1, $$2) -> $$0.createWithContext((ImmutableStringReader)StringReaderTerms.createReader($$1, $$2));
    }

    public static DelayedException<CommandSyntaxException> create(DynamicCommandExceptionType $$0, String $$1) {
        return ($$2, $$3) -> $$0.createWithContext((ImmutableStringReader)StringReaderTerms.createReader($$2, $$3), (Object)$$1);
    }
}

