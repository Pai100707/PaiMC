/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.NameAndId;

public class GameProfileArgument
implements ArgumentType<Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "dd12be42-52a9-4a91-a8a1-11c01849e498", "@e");
    public static final SimpleCommandExceptionType ERROR_UNKNOWN_PLAYER = new SimpleCommandExceptionType((Message)Component.translatable("argument.player.unknown"));

    public static Collection<NameAndId> getGameProfiles(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ((Result)$$0.getArgument($$1, Result.class)).getNames((CommandSourceStack)$$0.getSource());
    }

    public static GameProfileArgument gameProfile() {
        return new GameProfileArgument();
    }

    public <S> Result parse(StringReader $$0, S $$1) throws CommandSyntaxException {
        return GameProfileArgument.parse($$0, EntitySelectorParser.allowSelectors($$1));
    }

    public Result parse(StringReader $$0) throws CommandSyntaxException {
        return GameProfileArgument.parse($$0, true);
    }

    private static Result parse(StringReader $$0, boolean $$12) throws CommandSyntaxException {
        if ($$0.canRead() && $$0.peek() == '@') {
            EntitySelectorParser $$2 = new EntitySelectorParser($$0, $$12);
            EntitySelector $$3 = $$2.parse();
            if ($$3.includesEntities()) {
                throw EntityArgument.ERROR_ONLY_PLAYERS_ALLOWED.createWithContext((ImmutableStringReader)$$0);
            }
            return new SelectorResult($$3);
        }
        int $$4 = $$0.getCursor();
        while ($$0.canRead() && $$0.peek() != ' ') {
            $$0.skip();
        }
        String $$5 = $$0.getString().substring($$4, $$0.getCursor());
        return $$1 -> {
            Optional<NameAndId> $$2 = $$1.getServer().services().nameToIdCache().get($$5);
            return Collections.singleton($$2.orElseThrow(() -> ((SimpleCommandExceptionType)ERROR_UNKNOWN_PLAYER).create()));
        };
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$12) {
        Object object = $$0.getSource();
        if (object instanceof SharedSuggestionProvider) {
            SharedSuggestionProvider $$2 = (SharedSuggestionProvider)object;
            StringReader $$3 = new StringReader($$12.getInput());
            $$3.setCursor($$12.getStart());
            EntitySelectorParser $$4 = new EntitySelectorParser($$3, $$2.permissions().hasPermission(Permissions.COMMANDS_ENTITY_SELECTORS));
            try {
                $$4.parse();
            }
            catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
            return $$4.fillSuggestions($$12, $$1 -> SharedSuggestionProvider.suggest($$2.getOnlinePlayerNames(), $$1));
        }
        return Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader, Object object) throws CommandSyntaxException {
        return this.parse(stringReader, object);
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    @FunctionalInterface
    public static interface Result {
        public Collection<NameAndId> getNames(CommandSourceStack var1) throws CommandSyntaxException;
    }

    public static class SelectorResult
    implements Result {
        private final EntitySelector selector;

        public SelectorResult(EntitySelector $$0) {
            this.selector = $$0;
        }

        @Override
        public Collection<NameAndId> getNames(CommandSourceStack $$0) throws CommandSyntaxException {
            List<ServerPlayer> $$1 = this.selector.findPlayers($$0);
            if ($$1.isEmpty()) {
                throw EntityArgument.NO_PLAYERS_FOUND.create();
            }
            ArrayList<NameAndId> $$2 = new ArrayList<NameAndId>();
            for (ServerPlayer $$3 : $$1) {
                $$2.add($$3.nameAndId());
            }
            return $$2;
        }
    }
}

