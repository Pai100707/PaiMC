/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ObjectiveCriteriaArgument
implements ArgumentType<ObjectiveCriteria> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar.baz", "minecraft:foo");
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.criteria.invalid", $$0));

    private ObjectiveCriteriaArgument() {
    }

    public static ObjectiveCriteriaArgument criteria() {
        return new ObjectiveCriteriaArgument();
    }

    public static ObjectiveCriteria getCriteria(CommandContext<CommandSourceStack> $$0, String $$1) {
        return (ObjectiveCriteria)$$0.getArgument($$1, ObjectiveCriteria.class);
    }

    public ObjectiveCriteria parse(StringReader $$0) throws CommandSyntaxException {
        int $$1 = $$0.getCursor();
        while ($$0.canRead() && $$0.peek() != ' ') {
            $$0.skip();
        }
        String $$2 = $$0.getString().substring($$1, $$0.getCursor());
        return ObjectiveCriteria.byName($$2).orElseThrow(() -> {
            $$0.setCursor($$1);
            return ERROR_INVALID_VALUE.createWithContext((ImmutableStringReader)$$0, (Object)$$2);
        });
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
        ArrayList $$2 = Lists.newArrayList(ObjectiveCriteria.getCustomCriteriaNames());
        for (StatType statType : BuiltInRegistries.STAT_TYPE) {
            for (Object $$4 : statType.getRegistry()) {
                String $$5 = this.getName(statType, $$4);
                $$2.add($$5);
            }
        }
        return SharedSuggestionProvider.suggest($$2, $$1);
    }

    public <T> String getName(StatType<T> $$0, Object $$1) {
        return Stat.buildName($$0, $$1);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

