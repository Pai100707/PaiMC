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
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 */
package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.ScoreHolder;

public class ScoreHolderArgument
implements ArgumentType<Result> {
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_SCORE_HOLDERS = ($$0, $$12) -> {
        StringReader $$2 = new StringReader($$12.getInput());
        $$2.setCursor($$12.getStart());
        EntitySelectorParser $$3 = new EntitySelectorParser($$2, ((CommandSourceStack)$$0.getSource()).permissions().hasPermission(Permissions.COMMANDS_ENTITY_SELECTORS));
        try {
            $$3.parse();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return $$3.fillSuggestions($$12, $$1 -> SharedSuggestionProvider.suggest(((CommandSourceStack)$$0.getSource()).getOnlinePlayerNames(), $$1));
    };
    private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "*", "@e");
    private static final SimpleCommandExceptionType ERROR_NO_RESULTS = new SimpleCommandExceptionType((Message)Component.translatable("argument.scoreHolder.empty"));
    final boolean multiple;

    public ScoreHolderArgument(boolean $$0) {
        this.multiple = $$0;
    }

    public static ScoreHolder getName(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ScoreHolderArgument.getNames($$0, $$1).iterator().next();
    }

    public static Collection<ScoreHolder> getNames(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ScoreHolderArgument.getNames($$0, $$1, Collections::emptyList);
    }

    public static Collection<ScoreHolder> getNamesWithDefaultWildcard(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ScoreHolderArgument.getNames($$0, $$1, ((CommandSourceStack)$$0.getSource()).getServer().getScoreboard()::getTrackedPlayers);
    }

    public static Collection<ScoreHolder> getNames(CommandContext<CommandSourceStack> $$0, String $$1, Supplier<Collection<ScoreHolder>> $$2) throws CommandSyntaxException {
        Collection<ScoreHolder> $$3 = ((Result)$$0.getArgument($$1, Result.class)).getNames((CommandSourceStack)$$0.getSource(), $$2);
        if ($$3.isEmpty()) {
            throw EntityArgument.NO_ENTITIES_FOUND.create();
        }
        return $$3;
    }

    public static ScoreHolderArgument scoreHolder() {
        return new ScoreHolderArgument(false);
    }

    public static ScoreHolderArgument scoreHolders() {
        return new ScoreHolderArgument(true);
    }

    public Result parse(StringReader $$0) throws CommandSyntaxException {
        return this.parse($$0, true);
    }

    public <S> Result parse(StringReader $$0, S $$1) throws CommandSyntaxException {
        return this.parse($$0, EntitySelectorParser.allowSelectors($$1));
    }

    private Result parse(StringReader $$02, boolean $$12) throws CommandSyntaxException {
        if ($$02.canRead() && $$02.peek() == '@') {
            EntitySelectorParser $$22 = new EntitySelectorParser($$02, $$12);
            EntitySelector $$32 = $$22.parse();
            if (!this.multiple && $$32.getMaxResults() > 1) {
                throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.createWithContext((ImmutableStringReader)$$02);
            }
            return new SelectorResult($$32);
        }
        int $$4 = $$02.getCursor();
        while ($$02.canRead() && $$02.peek() != ' ') {
            $$02.skip();
        }
        String $$5 = $$02.getString().substring($$4, $$02.getCursor());
        if ($$5.equals("*")) {
            return ($$0, $$1) -> {
                Collection $$2 = (Collection)$$1.get();
                if ($$2.isEmpty()) {
                    throw ERROR_NO_RESULTS.create();
                }
                return $$2;
            };
        }
        List<ScoreHolder> $$6 = List.of(ScoreHolder.forNameOnly($$5));
        if ($$5.startsWith("#")) {
            return ($$1, $$2) -> $$6;
        }
        try {
            UUID $$7 = UUID.fromString($$5);
            return ($$2, $$3) -> {
                MinecraftServer $$4 = $$2.getServer();
                Entity $$5 = null;
                ArrayList<Entity> $$6 = null;
                for (ServerLevel $$7 : $$4.getAllLevels()) {
                    Entity $$8 = $$7.getEntity($$7);
                    if ($$8 == null) continue;
                    if ($$5 == null) {
                        $$5 = $$8;
                        continue;
                    }
                    if ($$6 == null) {
                        $$6 = new ArrayList<Entity>();
                        $$6.add($$5);
                    }
                    $$6.add($$8);
                }
                if ($$6 != null) {
                    return $$6;
                }
                if ($$5 != null) {
                    return List.of($$5);
                }
                return $$6;
            };
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return ($$2, $$3) -> {
                MinecraftServer $$4 = $$2.getServer();
                ServerPlayer $$5 = $$4.getPlayerList().getPlayerByName($$5);
                if ($$5 != null) {
                    return List.of($$5);
                }
                return $$6;
            };
        }
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
        public Collection<ScoreHolder> getNames(CommandSourceStack var1, Supplier<Collection<ScoreHolder>> var2) throws CommandSyntaxException;
    }

    public static class SelectorResult
    implements Result {
        private final EntitySelector selector;

        public SelectorResult(EntitySelector $$0) {
            this.selector = $$0;
        }

        @Override
        public Collection<ScoreHolder> getNames(CommandSourceStack $$0, Supplier<Collection<ScoreHolder>> $$1) throws CommandSyntaxException {
            List<? extends Entity> $$2 = this.selector.findEntities($$0);
            if ($$2.isEmpty()) {
                throw EntityArgument.NO_ENTITIES_FOUND.create();
            }
            return List.copyOf($$2);
        }
    }

    public static class Info
    implements ArgumentTypeInfo<ScoreHolderArgument, Template> {
        private static final byte FLAG_MULTIPLE = 1;

        @Override
        public void serializeToNetwork(Template $$0, FriendlyByteBuf $$1) {
            int $$2 = 0;
            if ($$0.multiple) {
                $$2 |= 1;
            }
            $$1.writeByte($$2);
        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf $$0) {
            byte $$1 = $$0.readByte();
            boolean $$2 = ($$1 & 1) != 0;
            return new Template($$2);
        }

        @Override
        public void serializeToJson(Template $$0, JsonObject $$1) {
            $$1.addProperty("amount", $$0.multiple ? "multiple" : "single");
        }

        @Override
        public Template unpack(ScoreHolderArgument $$0) {
            return new Template($$0.multiple);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<ScoreHolderArgument> {
            final boolean multiple;

            Template(boolean $$1) {
                this.multiple = $$1;
            }

            @Override
            public ScoreHolderArgument instantiate(CommandBuildContext $$0) {
                return new ScoreHolderArgument(this.multiple);
            }

            @Override
            public ArgumentTypeInfo<ScoreHolderArgument, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

