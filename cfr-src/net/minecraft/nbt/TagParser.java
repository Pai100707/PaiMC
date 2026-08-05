/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 */
package net.minecraft.nbt;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.parsing.packrat.commands.Grammar;

public class TagParser<T> {
    public static final SimpleCommandExceptionType ERROR_TRAILING_DATA = new SimpleCommandExceptionType((Message)Component.translatable("argument.nbt.trailing"));
    public static final SimpleCommandExceptionType ERROR_EXPECTED_COMPOUND = new SimpleCommandExceptionType((Message)Component.translatable("argument.nbt.expected.compound"));
    public static final char ELEMENT_SEPARATOR = ',';
    public static final char NAME_VALUE_SEPARATOR = ':';
    private static final TagParser<Tag> NBT_OPS_PARSER = TagParser.create(NbtOps.INSTANCE);
    public static final Codec<CompoundTag> FLATTENED_CODEC = Codec.STRING.comapFlatMap($$0 -> {
        try {
            Tag $$1 = NBT_OPS_PARSER.parseFully((String)$$0);
            if ($$1 instanceof CompoundTag) {
                CompoundTag $$2 = (CompoundTag)$$1;
                return DataResult.success((Object)$$2, (Lifecycle)Lifecycle.stable());
            }
            return DataResult.error(() -> "Expected compound tag, got " + String.valueOf($$1));
        }
        catch (CommandSyntaxException $$3) {
            return DataResult.error(() -> ((CommandSyntaxException)$$3).getMessage());
        }
    }, CompoundTag::toString);
    public static final Codec<CompoundTag> LENIENT_CODEC = Codec.withAlternative(FLATTENED_CODEC, CompoundTag.CODEC);
    private final DynamicOps<T> ops;
    private final Grammar<T> grammar;

    private TagParser(DynamicOps<T> $$0, Grammar<T> $$1) {
        this.ops = $$0;
        this.grammar = $$1;
    }

    public DynamicOps<T> getOps() {
        return this.ops;
    }

    public static <T> TagParser<T> create(DynamicOps<T> $$0) {
        return new TagParser<T>($$0, SnbtGrammar.createParser($$0));
    }

    private static CompoundTag castToCompoundOrThrow(StringReader $$0, Tag $$1) throws CommandSyntaxException {
        if ($$1 instanceof CompoundTag) {
            CompoundTag $$2 = (CompoundTag)$$1;
            return $$2;
        }
        throw ERROR_EXPECTED_COMPOUND.createWithContext((ImmutableStringReader)$$0);
    }

    public static CompoundTag parseCompoundFully(String $$0) throws CommandSyntaxException {
        StringReader $$1 = new StringReader($$0);
        return TagParser.castToCompoundOrThrow($$1, NBT_OPS_PARSER.parseFully($$1));
    }

    public T parseFully(String $$0) throws CommandSyntaxException {
        return this.parseFully(new StringReader($$0));
    }

    public T parseFully(StringReader $$0) throws CommandSyntaxException {
        T $$1 = this.grammar.parseForCommands($$0);
        $$0.skipWhitespace();
        if ($$0.canRead()) {
            throw ERROR_TRAILING_DATA.createWithContext((ImmutableStringReader)$$0);
        }
        return $$1;
    }

    public T parseAsArgument(StringReader $$0) throws CommandSyntaxException {
        return this.grammar.parseForCommands($$0);
    }

    public static CompoundTag parseCompoundAsArgument(StringReader $$0) throws CommandSyntaxException {
        Tag $$1 = NBT_OPS_PARSER.parseAsArgument($$0);
        return TagParser.castToCompoundOrThrow($$0, $$1);
    }
}

