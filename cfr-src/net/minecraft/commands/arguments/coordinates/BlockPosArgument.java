/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.LocalCoordinates;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class BlockPosArgument
implements ArgumentType<Coordinates> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "~0.5 ~1 ~-5");
    public static final SimpleCommandExceptionType ERROR_NOT_LOADED = new SimpleCommandExceptionType((Message)Component.translatable("argument.pos.unloaded"));
    public static final SimpleCommandExceptionType ERROR_OUT_OF_WORLD = new SimpleCommandExceptionType((Message)Component.translatable("argument.pos.outofworld"));
    public static final SimpleCommandExceptionType ERROR_OUT_OF_BOUNDS = new SimpleCommandExceptionType((Message)Component.translatable("argument.pos.outofbounds"));

    public static BlockPosArgument blockPos() {
        return new BlockPosArgument();
    }

    public static BlockPos getLoadedBlockPos(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        ServerLevel $$2 = ((CommandSourceStack)$$0.getSource()).getLevel();
        return BlockPosArgument.getLoadedBlockPos($$0, $$2, $$1);
    }

    public static BlockPos getLoadedBlockPos(CommandContext<CommandSourceStack> $$0, ServerLevel $$1, String $$2) throws CommandSyntaxException {
        BlockPos $$3 = BlockPosArgument.getBlockPos($$0, $$2);
        if (!$$1.hasChunkAt($$3)) {
            throw ERROR_NOT_LOADED.create();
        }
        if (!$$1.isInWorldBounds($$3)) {
            throw ERROR_OUT_OF_WORLD.create();
        }
        return $$3;
    }

    public static BlockPos getBlockPos(CommandContext<CommandSourceStack> $$0, String $$1) {
        return ((Coordinates)$$0.getArgument($$1, Coordinates.class)).getBlockPos((CommandSourceStack)$$0.getSource());
    }

    public static BlockPos getSpawnablePos(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        BlockPos $$2 = BlockPosArgument.getBlockPos($$0, $$1);
        if (!Level.isInSpawnableBounds($$2)) {
            throw ERROR_OUT_OF_BOUNDS.create();
        }
        return $$2;
    }

    public Coordinates parse(StringReader $$0) throws CommandSyntaxException {
        if ($$0.canRead() && $$0.peek() == '^') {
            return LocalCoordinates.parse($$0);
        }
        return WorldCoordinates.parseInt($$0);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
        if ($$0.getSource() instanceof SharedSuggestionProvider) {
            Collection<SharedSuggestionProvider.TextCoordinates> $$4;
            String $$2 = $$1.getRemaining();
            if (!$$2.isEmpty() && $$2.charAt(0) == '^') {
                Set<SharedSuggestionProvider.TextCoordinates> $$3 = Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL);
            } else {
                $$4 = ((SharedSuggestionProvider)$$0.getSource()).getRelevantCoordinates();
            }
            return SharedSuggestionProvider.suggestCoordinates($$2, $$4, $$1, Commands.createValidator(this::parse));
        }
        return Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

