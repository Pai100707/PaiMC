package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
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

public class ScoreHolderArgument implements ArgumentType<ScoreHolderArgument.Result> {
   public static final SuggestionProvider<net.minecraft.commands.CommandSourceStack> SUGGEST_SCORE_HOLDERS = ($$0, $$1) -> {
      StringReader $$2 = new StringReader($$1.getInput());
      $$2.setCursor($$1.getStart());
      EntitySelectorParser $$3 = new EntitySelectorParser(
         $$2, ((net.minecraft.commands.CommandSourceStack)$$0.getSource()).permissions().hasPermission(Permissions.COMMANDS_ENTITY_SELECTORS)
      );

      try {
         $$3.parse();
      } catch (CommandSyntaxException var5) {
      }

      return $$3.fillSuggestions(
         $$1,
         $$1x -> net.minecraft.commands.SharedSuggestionProvider.suggest(
            ((net.minecraft.commands.CommandSourceStack)$$0.getSource()).getOnlinePlayerNames(), $$1x
         )
      );
   };
   private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "*", "@e");
   private static final SimpleCommandExceptionType ERROR_NO_RESULTS = new SimpleCommandExceptionType(Component.translatable("argument.scoreHolder.empty"));
   final boolean multiple;

   public ScoreHolderArgument(boolean $$0) {
      this.multiple = $$0;
   }

   public static ScoreHolder getName(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return getNames($$0, $$1).iterator().next();
   }

   public static Collection<ScoreHolder> getNames(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return getNames($$0, $$1, Collections::emptyList);
   }

   public static Collection<ScoreHolder> getNamesWithDefaultWildcard(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return getNames($$0, $$1, ((net.minecraft.commands.CommandSourceStack)$$0.getSource()).getServer().getScoreboard()::getTrackedPlayers);
   }

   public static Collection<ScoreHolder> getNames(
      CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1, Supplier<Collection<ScoreHolder>> $$2
   ) throws CommandSyntaxException {
      Collection<ScoreHolder> $$3 = ((ScoreHolderArgument.Result)$$0.getArgument($$1, ScoreHolderArgument.Result.class))
         .getNames((net.minecraft.commands.CommandSourceStack)$$0.getSource(), $$2);
      if ($$3.isEmpty()) {
         throw EntityArgument.NO_ENTITIES_FOUND.create();
      } else {
         return $$3;
      }
   }

   public static ScoreHolderArgument scoreHolder() {
      return new ScoreHolderArgument(false);
   }

   public static ScoreHolderArgument scoreHolders() {
      return new ScoreHolderArgument(true);
   }

   public ScoreHolderArgument.Result parse(StringReader $$0) throws CommandSyntaxException {
      return this.parse($$0, true);
   }

   public <S> ScoreHolderArgument.Result parse(StringReader $$0, S $$1) throws CommandSyntaxException {
      return this.parse($$0, EntitySelectorParser.allowSelectors($$1));
   }

   private ScoreHolderArgument.Result parse(StringReader $$0, boolean $$1) throws CommandSyntaxException {
      if ($$0.canRead() && $$0.peek() == '@') {
         EntitySelectorParser $$2 = new EntitySelectorParser($$0, $$1);
         EntitySelector $$3 = $$2.parse();
         if (!this.multiple && $$3.getMaxResults() > 1) {
            throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.createWithContext($$0);
         } else {
            return new ScoreHolderArgument.SelectorResult($$3);
         }
      } else {
         int $$4 = $$0.getCursor();

         while ($$0.canRead() && $$0.peek() != ' ') {
            $$0.skip();
         }

         String $$5 = $$0.getString().substring($$4, $$0.getCursor());
         if ($$5.equals("*")) {
            return ($$0x, $$1x) -> {
               Collection<ScoreHolder> $$2 = (Collection<ScoreHolder>)$$1x.get();
               if ($$2.isEmpty()) {
                  throw ERROR_NO_RESULTS.create();
               } else {
                  return $$2;
               }
            };
         } else {
            List<ScoreHolder> $$6 = List.of(ScoreHolder.forNameOnly($$5));
            if ($$5.startsWith("#")) {
               return ($$1x, $$2) -> $$6;
            } else {
               try {
                  UUID $$7 = UUID.fromString($$5);
                  return ($$2, $$3) -> {
                     MinecraftServer $$4x = $$2.getServer();
                     ScoreHolder $$5x = null;
                     List<ScoreHolder> $$6x = null;

                     for (ServerLevel $$7x : $$4x.getAllLevels()) {
                        Entity $$8 = $$7x.getEntity($$7);
                        if ($$8 != null) {
                           if ($$5x == null) {
                              $$5x = $$8;
                           } else {
                              if ($$6x == null) {
                                 $$6x = new ArrayList<>();
                                 $$6x.add($$5x);
                              }

                              $$6x.add($$8);
                           }
                        }
                     }

                     if ($$6x != null) {
                        return $$6x;
                     } else {
                        return $$5x != null ? List.of($$5x) : $$6;
                     }
                  };
               } catch (IllegalArgumentException var7) {
                  return ($$2, $$3) -> {
                     MinecraftServer $$4x = $$2.getServer();
                     ServerPlayer $$5x = $$4x.getPlayerList().getPlayerByName($$5);
                     return $$5x != null ? List.of($$5x) : $$6;
                  };
               }
            }
         }
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info implements ArgumentTypeInfo<ScoreHolderArgument, ScoreHolderArgument.Info.Template> {
      private static final byte FLAG_MULTIPLE = 1;

      public void serializeToNetwork(ScoreHolderArgument.Info.Template $$0, FriendlyByteBuf $$1) {
         int $$2 = 0;
         if ($$0.multiple) {
            $$2 |= 1;
         }

         $$1.writeByte($$2);
      }

      public ScoreHolderArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf $$0) {
         byte $$1 = $$0.readByte();
         boolean $$2 = ($$1 & 1) != 0;
         return new ScoreHolderArgument.Info.Template($$2);
      }

      public void serializeToJson(ScoreHolderArgument.Info.Template $$0, JsonObject $$1) {
         $$1.addProperty("amount", $$0.multiple ? "multiple" : "single");
      }

      public ScoreHolderArgument.Info.Template unpack(ScoreHolderArgument $$0) {
         return new ScoreHolderArgument.Info.Template($$0.multiple);
      }

      public final class Template implements ArgumentTypeInfo.Template<ScoreHolderArgument> {
         final boolean multiple;

         Template(final boolean $$1) {
            this.multiple = $$1;
         }

         public ScoreHolderArgument instantiate(net.minecraft.commands.CommandBuildContext $$0) {
            return new ScoreHolderArgument(this.multiple);
         }

         @Override
         public ArgumentTypeInfo<ScoreHolderArgument, ?> type() {
            return Info.this;
         }
      }
   }

   @FunctionalInterface
   public interface Result {
      Collection<ScoreHolder> getNames(net.minecraft.commands.CommandSourceStack var1, Supplier<Collection<ScoreHolder>> var2) throws CommandSyntaxException;
   }

   public static class SelectorResult implements ScoreHolderArgument.Result {
      private final EntitySelector selector;

      public SelectorResult(EntitySelector $$0) {
         this.selector = $$0;
      }

      @Override
      public Collection<ScoreHolder> getNames(net.minecraft.commands.CommandSourceStack $$0, Supplier<Collection<ScoreHolder>> $$1) throws CommandSyntaxException {
         List<? extends Entity> $$2 = this.selector.findEntities($$0);
         if ($$2.isEmpty()) {
            throw EntityArgument.NO_ENTITIES_FOUND.create();
         } else {
            return List.copyOf($$2);
         }
      }
   }
}
