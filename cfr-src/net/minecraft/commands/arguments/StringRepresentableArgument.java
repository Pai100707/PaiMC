/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonPrimitive
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.commands.arguments;

import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public class StringRepresentableArgument<T extends Enum<T>>
implements ArgumentType<T> {
    private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.enum.invalid", $$0));
    private final Codec<T> codec;
    private final Supplier<T[]> values;

    protected StringRepresentableArgument(Codec<T> $$0, Supplier<T[]> $$1) {
        this.codec = $$0;
        this.values = $$1;
    }

    public T parse(StringReader $$0) throws CommandSyntaxException {
        String $$1 = $$0.readUnquotedString();
        return (T)((Enum)this.codec.parse((DynamicOps)JsonOps.INSTANCE, (Object)new JsonPrimitive($$1)).result().orElseThrow(() -> ERROR_INVALID_VALUE.createWithContext((ImmutableStringReader)$$0, (Object)$$1)));
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$02, SuggestionsBuilder $$1) {
        return SharedSuggestionProvider.suggest(Arrays.stream((Enum[])this.values.get()).map($$0 -> ((StringRepresentable)$$0).getSerializedName()).map(this::convertId).collect(Collectors.toList()), $$1);
    }

    public Collection<String> getExamples() {
        return Arrays.stream((Enum[])this.values.get()).map($$0 -> ((StringRepresentable)$$0).getSerializedName()).map(this::convertId).limit(2L).collect(Collectors.toList());
    }

    protected String convertId(String $$0) {
        return $$0;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

