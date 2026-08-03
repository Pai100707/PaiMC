package net.minecraft.commands.arguments.coordinates;

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
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class BlockPosArgument implements ArgumentType<Coordinates> {
   private static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "~0.5 ~1 ~-5");
   public static final SimpleCommandExceptionType ERROR_NOT_LOADED = new SimpleCommandExceptionType(Component.translatable("argument.pos.unloaded"));
   public static final SimpleCommandExceptionType ERROR_OUT_OF_WORLD = new SimpleCommandExceptionType(Component.translatable("argument.pos.outofworld"));
   public static final SimpleCommandExceptionType ERROR_OUT_OF_BOUNDS = new SimpleCommandExceptionType(Component.translatable("argument.pos.outofbounds"));

   public static BlockPosArgument blockPos() {
      return new BlockPosArgument();
   }

   public static BlockPos getLoadedBlockPos(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      ServerLevel $$2 = ((net.minecraft.commands.CommandSourceStack)$$0.getSource()).getLevel();
      return getLoadedBlockPos($$0, $$2, $$1);
   }

   public static BlockPos getLoadedBlockPos(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, ServerLevel $$1, String $$2) throws CommandSyntaxException {
      BlockPos $$3 = getBlockPos($$0, $$2);
      if (!$$1.hasChunkAt($$3)) {
         throw ERROR_NOT_LOADED.create();
      } else if (!$$1.isInWorldBounds($$3)) {
         throw ERROR_OUT_OF_WORLD.create();
      } else {
         return $$3;
      }
   }

   public static BlockPos getBlockPos(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return ((Coordinates)$$0.getArgument($$1, Coordinates.class)).getBlockPos((net.minecraft.commands.CommandSourceStack)$$0.getSource());
   }

   public static BlockPos getSpawnablePos(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      BlockPos $$2 = getBlockPos($$0, $$1);
      if (!Level.isInSpawnableBounds($$2)) {
         throw ERROR_OUT_OF_BOUNDS.create();
      } else {
         return $$2;
      }
   }

   public Coordinates parse(StringReader $$0) throws CommandSyntaxException {
      return (Coordinates)($$0.canRead() && $$0.peek() == '^' ? LocalCoordinates.parse($$0) : WorldCoordinates.parseInt($$0));
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      if (!($$0.getSource() instanceof net.minecraft.commands.SharedSuggestionProvider)) {
         return Suggestions.empty();
      } else {
         String $$2 = $$1.getRemaining();
         Collection<net.minecraft.commands.SharedSuggestionProvider.TextCoordinates> $$3;
         if (!$$2.isEmpty() && $$2.charAt(0) == '^') {
            $$3 = Collections.singleton(net.minecraft.commands.SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL);
         } else {
            $$3 = ((net.minecraft.commands.SharedSuggestionProvider)$$0.getSource()).getRelevantCoordinates();
         }

         return net.minecraft.commands.SharedSuggestionProvider.suggestCoordinates($$2, $$3, $$1, net.minecraft.commands.Commands.createValidator(this::parse));
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
