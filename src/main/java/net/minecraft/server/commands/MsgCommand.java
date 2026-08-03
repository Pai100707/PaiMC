package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.ChatType.Bound;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class MsgCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      LiteralCommandNode<CommandSourceStack> $$1 = $$0.register(
         (LiteralArgumentBuilder)Commands.literal("msg")
            .then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("message", MessageArgument.message()).executes($$0x -> {
               Collection<ServerPlayer> $$1x = EntityArgument.getPlayers($$0x, "targets");
               if (!$$1x.isEmpty()) {
                  MessageArgument.resolveChatMessage($$0x, "message", $$2 -> sendMessage((CommandSourceStack)$$0x.getSource(), $$1x, $$2));
               }

               return $$1x.size();
            })))
      );
      $$0.register((LiteralArgumentBuilder)Commands.literal("tell").redirect($$1));
      $$0.register((LiteralArgumentBuilder)Commands.literal("w").redirect($$1));
   }

   private static void sendMessage(CommandSourceStack $$0, Collection<ServerPlayer> $$1, PlayerChatMessage $$2) {
      Bound $$3 = ChatType.bind(ChatType.MSG_COMMAND_INCOMING, $$0);
      OutgoingChatMessage $$4 = OutgoingChatMessage.create($$2);
      boolean $$5 = false;

      for (ServerPlayer $$6 : $$1) {
         Bound $$7 = ChatType.bind(ChatType.MSG_COMMAND_OUTGOING, $$0).withTargetName($$6.getDisplayName());
         $$0.sendChatMessage($$4, false, $$7);
         boolean $$8 = $$0.shouldFilterMessageTo($$6);
         $$6.sendChatMessage($$4, $$8, $$3);
         $$5 |= $$8 && $$2.isFullyFiltered();
      }

      if ($$5) {
         $$0.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
      }
   }
}
