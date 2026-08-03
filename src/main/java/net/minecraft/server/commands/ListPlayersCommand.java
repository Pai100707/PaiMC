package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;

public class ListPlayersCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("list").executes($$0x -> listPlayers((CommandSourceStack)$$0x.getSource())))
            .then(Commands.literal("uuids").executes($$0x -> listPlayersWithUuids((CommandSourceStack)$$0x.getSource())))
      );
   }

   private static int listPlayers(CommandSourceStack $$0) {
      return format($$0, Player::getDisplayName);
   }

   private static int listPlayersWithUuids(CommandSourceStack $$0) {
      return format(
         $$0, $$0x -> Component.translatable("commands.list.nameAndId", new Object[]{$$0x.getName(), Component.translationArg($$0x.getGameProfile().id())})
      );
   }

   private static int format(CommandSourceStack $$0, Function<ServerPlayer, Component> $$1) {
      PlayerList $$2 = $$0.getServer().getPlayerList();
      List<ServerPlayer> $$3 = $$2.getPlayers();
      Component $$4 = ComponentUtils.formatList($$3, $$1);
      $$0.sendSuccess(() -> Component.translatable("commands.list.players", new Object[]{$$3.size(), $$2.getMaxPlayers(), $$4}), false);
      return $$3.size();
   }
}
