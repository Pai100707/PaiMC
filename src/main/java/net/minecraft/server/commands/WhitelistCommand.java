package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;
import net.minecraft.world.entity.player.Player;

public class WhitelistCommand {
   private static final SimpleCommandExceptionType ERROR_ALREADY_ENABLED = new SimpleCommandExceptionType(
      Component.translatable("commands.whitelist.alreadyOn")
   );
   private static final SimpleCommandExceptionType ERROR_ALREADY_DISABLED = new SimpleCommandExceptionType(
      Component.translatable("commands.whitelist.alreadyOff")
   );
   private static final SimpleCommandExceptionType ERROR_ALREADY_WHITELISTED = new SimpleCommandExceptionType(
      Component.translatable("commands.whitelist.add.failed")
   );
   private static final SimpleCommandExceptionType ERROR_NOT_WHITELISTED = new SimpleCommandExceptionType(
      Component.translatable("commands.whitelist.remove.failed")
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                 "whitelist"
                              )
                              .requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
                           .then(Commands.literal("on").executes($$0x -> enableWhitelist((CommandSourceStack)$$0x.getSource()))))
                        .then(Commands.literal("off").executes($$0x -> disableWhitelist((CommandSourceStack)$$0x.getSource()))))
                     .then(Commands.literal("list").executes($$0x -> showList((CommandSourceStack)$$0x.getSource()))))
                  .then(
                     Commands.literal("add")
                        .then(
                           Commands.argument("targets", GameProfileArgument.gameProfile())
                              .suggests(
                                 ($$0x, $$1) -> {
                                    PlayerList $$2 = ((CommandSourceStack)$$0x.getSource()).getServer().getPlayerList();
                                    return SharedSuggestionProvider.suggest(
                                       $$2.getPlayers()
                                          .stream()
                                          .<NameAndId>map(Player::nameAndId)
                                          .filter($$1x -> !$$2.getWhiteList().isWhiteListed($$1x))
                                          .map(NameAndId::name),
                                       $$1
                                    );
                                 }
                              )
                              .executes($$0x -> addPlayers((CommandSourceStack)$$0x.getSource(), GameProfileArgument.getGameProfiles($$0x, "targets")))
                        )
                  ))
               .then(
                  Commands.literal("remove")
                     .then(
                        Commands.argument("targets", GameProfileArgument.gameProfile())
                           .suggests(
                              ($$0x, $$1) -> SharedSuggestionProvider.suggest(
                                 ((CommandSourceStack)$$0x.getSource()).getServer().getPlayerList().getWhiteListNames(), $$1
                              )
                           )
                           .executes($$0x -> removePlayers((CommandSourceStack)$$0x.getSource(), GameProfileArgument.getGameProfiles($$0x, "targets")))
                     )
               ))
            .then(Commands.literal("reload").executes($$0x -> reload((CommandSourceStack)$$0x.getSource())))
      );
   }

   private static int reload(CommandSourceStack $$0) {
      $$0.getServer().getPlayerList().reloadWhiteList();
      $$0.sendSuccess(() -> Component.translatable("commands.whitelist.reloaded"), true);
      $$0.getServer().kickUnlistedPlayers();
      return 1;
   }

   private static int addPlayers(CommandSourceStack $$0, Collection<NameAndId> $$1) throws CommandSyntaxException {
      UserWhiteList $$2 = $$0.getServer().getPlayerList().getWhiteList();
      int $$3 = 0;

      for (NameAndId $$4 : $$1) {
         if (!$$2.isWhiteListed($$4)) {
            UserWhiteListEntry $$5 = new UserWhiteListEntry($$4);
            $$2.add($$5);
            $$0.sendSuccess(() -> Component.translatable("commands.whitelist.add.success", new Object[]{Component.literal($$4.name())}), true);
            $$3++;
         }
      }

      if ($$3 == 0) {
         throw ERROR_ALREADY_WHITELISTED.create();
      } else {
         return $$3;
      }
   }

   private static int removePlayers(CommandSourceStack $$0, Collection<NameAndId> $$1) throws CommandSyntaxException {
      UserWhiteList $$2 = $$0.getServer().getPlayerList().getWhiteList();
      int $$3 = 0;

      for (NameAndId $$4 : $$1) {
         if ($$2.isWhiteListed($$4)) {
            UserWhiteListEntry $$5 = new UserWhiteListEntry($$4);
            $$2.remove($$5);
            $$0.sendSuccess(() -> Component.translatable("commands.whitelist.remove.success", new Object[]{Component.literal($$4.name())}), true);
            $$3++;
         }
      }

      if ($$3 == 0) {
         throw ERROR_NOT_WHITELISTED.create();
      } else {
         $$0.getServer().kickUnlistedPlayers();
         return $$3;
      }
   }

   private static int enableWhitelist(CommandSourceStack $$0) throws CommandSyntaxException {
      if ($$0.getServer().isUsingWhitelist()) {
         throw ERROR_ALREADY_ENABLED.create();
      } else {
         $$0.getServer().setUsingWhitelist(true);
         $$0.sendSuccess(() -> Component.translatable("commands.whitelist.enabled"), true);
         $$0.getServer().kickUnlistedPlayers();
         return 1;
      }
   }

   private static int disableWhitelist(CommandSourceStack $$0) throws CommandSyntaxException {
      if (!$$0.getServer().isUsingWhitelist()) {
         throw ERROR_ALREADY_DISABLED.create();
      } else {
         $$0.getServer().setUsingWhitelist(false);
         $$0.sendSuccess(() -> Component.translatable("commands.whitelist.disabled"), true);
         return 1;
      }
   }

   private static int showList(CommandSourceStack $$0) {
      String[] $$1 = $$0.getServer().getPlayerList().getWhiteListNames();
      if ($$1.length == 0) {
         $$0.sendSuccess(() -> Component.translatable("commands.whitelist.none"), false);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.whitelist.list", new Object[]{$$1.length, String.join(", ", $$1)}), false);
      }

      return $$1.length;
   }
}
