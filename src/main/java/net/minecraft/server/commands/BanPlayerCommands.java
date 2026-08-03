package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import org.jspecify.annotations.Nullable;

public class BanPlayerCommands {
   private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.ban.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("ban").requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
            .then(
               ((RequiredArgumentBuilder)Commands.argument("targets", GameProfileArgument.gameProfile())
                     .executes($$0x -> banPlayers((CommandSourceStack)$$0x.getSource(), GameProfileArgument.getGameProfiles($$0x, "targets"), null)))
                  .then(
                     Commands.argument("reason", MessageArgument.message())
                        .executes(
                           $$0x -> banPlayers(
                              (CommandSourceStack)$$0x.getSource(),
                              GameProfileArgument.getGameProfiles($$0x, "targets"),
                              MessageArgument.getMessage($$0x, "reason")
                           )
                        )
                  )
            )
      );
   }

   private static int banPlayers(CommandSourceStack $$0, Collection<NameAndId> $$1, @Nullable Component $$2) throws CommandSyntaxException {
      UserBanList $$3 = $$0.getServer().getPlayerList().getBans();
      int $$4 = 0;

      for (NameAndId $$5 : $$1) {
         if (!$$3.isBanned($$5)) {
            UserBanListEntry $$6 = new UserBanListEntry($$5, null, $$0.getTextName(), null, $$2 == null ? null : $$2.getString());
            $$3.add($$6);
            $$4++;
            $$0.sendSuccess(() -> Component.translatable("commands.ban.success", new Object[]{Component.literal($$5.name()), $$6.getReasonMessage()}), true);
            ServerPlayer $$7 = $$0.getServer().getPlayerList().getPlayer($$5.id());
            if ($$7 != null) {
               $$7.connection.disconnect(Component.translatable("multiplayer.disconnect.banned"));
            }
         }
      }

      if ($$4 == 0) {
         throw ERROR_ALREADY_BANNED.create();
      } else {
         return $$4;
      }
   }
}
