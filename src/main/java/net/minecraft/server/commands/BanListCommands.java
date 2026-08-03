package net.minecraft.server.commands;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.BanListEntry;
import net.minecraft.server.players.PlayerList;

public class BanListCommands {
   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("banlist")
                     .requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
                  .executes(
                     $$0x -> {
                        PlayerList $$1 = ((CommandSourceStack)$$0x.getSource()).getServer().getPlayerList();
                        return showList(
                           (CommandSourceStack)$$0x.getSource(), Lists.newArrayList(Iterables.concat($$1.getBans().getEntries(), $$1.getIpBans().getEntries()))
                        );
                     }
                  ))
               .then(
                  Commands.literal("ips")
                     .executes(
                        $$0x -> showList(
                           (CommandSourceStack)$$0x.getSource(), ((CommandSourceStack)$$0x.getSource()).getServer().getPlayerList().getIpBans().getEntries()
                        )
                     )
               ))
            .then(
               Commands.literal("players")
                  .executes(
                     $$0x -> showList(
                        (CommandSourceStack)$$0x.getSource(), ((CommandSourceStack)$$0x.getSource()).getServer().getPlayerList().getBans().getEntries()
                     )
                  )
            )
      );
   }

   private static int showList(CommandSourceStack $$0, Collection<? extends BanListEntry<?>> $$1) {
      if ($$1.isEmpty()) {
         $$0.sendSuccess(() -> Component.translatable("commands.banlist.none"), false);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.banlist.list", new Object[]{$$1.size()}), false);

         for (BanListEntry<?> $$2 : $$1) {
            $$0.sendSuccess(
               () -> Component.translatable("commands.banlist.entry", new Object[]{$$2.getDisplayName(), $$2.getSource(), $$2.getReasonMessage()}), false
            );
         }
      }

      return $$1.size();
   }
}
