package net.minecraft.commands.arguments;

import com.google.common.collect.Iterables;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;

public class EntityArgument implements ArgumentType<EntitySelector> {
   private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "@e", "@e[type=foo]", "dd12be42-52a9-4a91-a8a1-11c01849e498");
   public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_ENTITY = new SimpleCommandExceptionType(Component.translatable("argument.entity.toomany"));
   public static final SimpleCommandExceptionType ERROR_NOT_SINGLE_PLAYER = new SimpleCommandExceptionType(Component.translatable("argument.player.toomany"));
   public static final SimpleCommandExceptionType ERROR_ONLY_PLAYERS_ALLOWED = new SimpleCommandExceptionType(
      Component.translatable("argument.player.entities")
   );
   public static final SimpleCommandExceptionType NO_ENTITIES_FOUND = new SimpleCommandExceptionType(Component.translatable("argument.entity.notfound.entity"));
   public static final SimpleCommandExceptionType NO_PLAYERS_FOUND = new SimpleCommandExceptionType(Component.translatable("argument.entity.notfound.player"));
   public static final SimpleCommandExceptionType ERROR_SELECTORS_NOT_ALLOWED = new SimpleCommandExceptionType(
      Component.translatable("argument.entity.selector.not_allowed")
   );
   final boolean single;
   final boolean playersOnly;

   protected EntityArgument(boolean $$0, boolean $$1) {
      this.single = $$0;
      this.playersOnly = $$1;
   }

   public static EntityArgument entity() {
      return new EntityArgument(true, false);
   }

   public static Entity getEntity(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return ((EntitySelector)$$0.getArgument($$1, EntitySelector.class)).findSingleEntity((net.minecraft.commands.CommandSourceStack)$$0.getSource());
   }

   public static EntityArgument entities() {
      return new EntityArgument(false, false);
   }

   public static Collection<? extends Entity> getEntities(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      Collection<? extends Entity> $$2 = getOptionalEntities($$0, $$1);
      if ($$2.isEmpty()) {
         throw NO_ENTITIES_FOUND.create();
      } else {
         return $$2;
      }
   }

   public static Collection<? extends Entity> getOptionalEntities(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return ((EntitySelector)$$0.getArgument($$1, EntitySelector.class)).findEntities((net.minecraft.commands.CommandSourceStack)$$0.getSource());
   }

   public static Collection<ServerPlayer> getOptionalPlayers(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return ((EntitySelector)$$0.getArgument($$1, EntitySelector.class)).findPlayers((net.minecraft.commands.CommandSourceStack)$$0.getSource());
   }

   public static EntityArgument player() {
      return new EntityArgument(true, true);
   }

   public static ServerPlayer getPlayer(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return ((EntitySelector)$$0.getArgument($$1, EntitySelector.class)).findSinglePlayer((net.minecraft.commands.CommandSourceStack)$$0.getSource());
   }

   public static EntityArgument players() {
      return new EntityArgument(false, true);
   }

   public static Collection<ServerPlayer> getPlayers(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      List<ServerPlayer> $$2 = ((EntitySelector)$$0.getArgument($$1, EntitySelector.class))
         .findPlayers((net.minecraft.commands.CommandSourceStack)$$0.getSource());
      if ($$2.isEmpty()) {
         throw NO_PLAYERS_FOUND.create();
      } else {
         return $$2;
      }
   }

   public EntitySelector parse(StringReader $$0) throws CommandSyntaxException {
      return this.parse($$0, true);
   }

   public <S> EntitySelector parse(StringReader $$0, S $$1) throws CommandSyntaxException {
      return this.parse($$0, EntitySelectorParser.allowSelectors($$1));
   }

   private EntitySelector parse(StringReader $$0, boolean $$1) throws CommandSyntaxException {
      int $$2 = 0;
      EntitySelectorParser $$3 = new EntitySelectorParser($$0, $$1);
      EntitySelector $$4 = $$3.parse();
      if ($$4.getMaxResults() > 1 && this.single) {
         if (this.playersOnly) {
            $$0.setCursor(0);
            throw ERROR_NOT_SINGLE_PLAYER.createWithContext($$0);
         } else {
            $$0.setCursor(0);
            throw ERROR_NOT_SINGLE_ENTITY.createWithContext($$0);
         }
      } else if ($$4.includesEntities() && this.playersOnly && !$$4.isSelfSelector()) {
         $$0.setCursor(0);
         throw ERROR_ONLY_PLAYERS_ALLOWED.createWithContext($$0);
      } else {
         return $$4;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      if ($$0.getSource() instanceof net.minecraft.commands.SharedSuggestionProvider $$2) {
         StringReader $$3 = new StringReader($$1.getInput());
         $$3.setCursor($$1.getStart());
         EntitySelectorParser $$4 = new EntitySelectorParser($$3, $$2.permissions().hasPermission(Permissions.COMMANDS_ENTITY_SELECTORS));

         try {
            $$4.parse();
         } catch (CommandSyntaxException var7) {
         }

         return $$4.fillSuggestions($$1, $$1x -> {
            Collection<String> $$2x = $$2.getOnlinePlayerNames();
            Iterable<String> $$3x = (Iterable<String>)(this.playersOnly ? $$2x : Iterables.concat($$2x, $$2.getSelectedEntities()));
            net.minecraft.commands.SharedSuggestionProvider.suggest($$3x, $$1x);
         });
      } else {
         return Suggestions.empty();
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info implements ArgumentTypeInfo<EntityArgument, EntityArgument.Info.Template> {
      private static final byte FLAG_SINGLE = 1;
      private static final byte FLAG_PLAYERS_ONLY = 2;

      public void serializeToNetwork(EntityArgument.Info.Template $$0, FriendlyByteBuf $$1) {
         int $$2 = 0;
         if ($$0.single) {
            $$2 |= 1;
         }

         if ($$0.playersOnly) {
            $$2 |= 2;
         }

         $$1.writeByte($$2);
      }

      public EntityArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf $$0) {
         byte $$1 = $$0.readByte();
         return new EntityArgument.Info.Template(($$1 & 1) != 0, ($$1 & 2) != 0);
      }

      public void serializeToJson(EntityArgument.Info.Template $$0, JsonObject $$1) {
         $$1.addProperty("amount", $$0.single ? "single" : "multiple");
         $$1.addProperty("type", $$0.playersOnly ? "players" : "entities");
      }

      public EntityArgument.Info.Template unpack(EntityArgument $$0) {
         return new EntityArgument.Info.Template($$0.single, $$0.playersOnly);
      }

      public final class Template implements ArgumentTypeInfo.Template<EntityArgument> {
         final boolean single;
         final boolean playersOnly;

         Template(final boolean $$1, final boolean $$2) {
            this.single = $$1;
            this.playersOnly = $$2;
         }

         public EntityArgument instantiate(net.minecraft.commands.CommandBuildContext $$0) {
            return new EntityArgument(this.single, this.playersOnly);
         }

         @Override
         public ArgumentTypeInfo<EntityArgument, ?> type() {
            return Info.this;
         }
      }
   }
}
