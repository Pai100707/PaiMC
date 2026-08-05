/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 */
package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class TimeArgument
implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0d", "0s", "0t", "0");
    private static final SimpleCommandExceptionType ERROR_INVALID_UNIT = new SimpleCommandExceptionType((Message)Component.translatable("argument.time.invalid_unit"));
    private static final Dynamic2CommandExceptionType ERROR_TICK_COUNT_TOO_LOW = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("argument.time.tick_count_too_low", $$1, $$0));
    private static final Object2IntMap<String> UNITS = new Object2IntOpenHashMap();
    final int minimum;

    private TimeArgument(int $$0) {
        this.minimum = $$0;
    }

    public static TimeArgument time() {
        return new TimeArgument(0);
    }

    public static TimeArgument time(int $$0) {
        return new TimeArgument($$0);
    }

    public Integer parse(StringReader $$0) throws CommandSyntaxException {
        float $$1 = $$0.readFloat();
        String $$2 = $$0.readUnquotedString();
        int $$3 = UNITS.getOrDefault((Object)$$2, 0);
        if ($$3 == 0) {
            throw ERROR_INVALID_UNIT.createWithContext((ImmutableStringReader)$$0);
        }
        int $$4 = Math.round($$1 * (float)$$3);
        if ($$4 < this.minimum) {
            throw ERROR_TICK_COUNT_TOO_LOW.createWithContext((ImmutableStringReader)$$0, (Object)$$4, (Object)this.minimum);
        }
        return $$4;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
        StringReader $$2 = new StringReader($$1.getRemaining());
        try {
            $$2.readFloat();
        }
        catch (CommandSyntaxException $$3) {
            return $$1.buildFuture();
        }
        return SharedSuggestionProvider.suggest((Iterable<String>)UNITS.keySet(), $$1.createOffset($$1.getStart() + $$2.getCursor()));
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    static {
        UNITS.put((Object)"d", 24000);
        UNITS.put((Object)"s", 20);
        UNITS.put((Object)"t", 1);
        UNITS.put((Object)"", 1);
    }

    public static class Info
    implements ArgumentTypeInfo<TimeArgument, Template> {
        @Override
        public void serializeToNetwork(Template $$0, FriendlyByteBuf $$1) {
            $$1.writeInt($$0.min);
        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf $$0) {
            int $$1 = $$0.readInt();
            return new Template($$1);
        }

        @Override
        public void serializeToJson(Template $$0, JsonObject $$1) {
            $$1.addProperty("min", (Number)$$0.min);
        }

        @Override
        public Template unpack(TimeArgument $$0) {
            return new Template($$0.minimum);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<TimeArgument> {
            final int min;

            Template(int $$1) {
                this.min = $$1;
            }

            @Override
            public TimeArgument instantiate(CommandBuildContext $$0) {
                return TimeArgument.time(this.min);
            }

            @Override
            public ArgumentTypeInfo<TimeArgument, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

