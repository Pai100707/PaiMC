package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ChatType.Bound;
import net.minecraft.network.chat.ClickEvent.SuggestCommand;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;

public class TeamMsgCommand {
   private static final Style SUGGEST_STYLE = Style.EMPTY
      .withHoverEvent(new ShowText(Component.translatable("chat.type.team.hover")))
      .withClickEvent(new SuggestCommand("/teammsg "));
   private static final SimpleCommandExceptionType ERROR_NOT_ON_TEAM = new SimpleCommandExceptionType(Component.translatable("commands.teammsg.failed.noteam"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      LiteralCommandNode<CommandSourceStack> $$1 = $$0.register(
         (LiteralArgumentBuilder)Commands.literal("teammsg").then(Commands.argument("message", MessageArgument.message()).executes($$0x -> {
            CommandSourceStack $$1x = (CommandSourceStack)$$0x.getSource();
            Entity $$2 = $$1x.getEntityOrException();
            PlayerTeam $$3 = $$2.getTeam();
            if ($$3 == null) {
               throw ERROR_NOT_ON_TEAM.create();
            } else {
               List<ServerPlayer> $$4 = $$1x.getServer().getPlayerList().getPlayers().stream().filter($$2x -> $$2x == $$2 || $$2x.getTeam() == $$3).toList();
               if (!$$4.isEmpty()) {
                  MessageArgument.resolveChatMessage($$0x, "message", $$4x -> sendMessage($$1x, $$2, $$3, $$4, $$4x));
               }

               return $$4.size();
            }
         }))
      );
      $$0.register((LiteralArgumentBuilder)Commands.literal("tm").redirect($$1));
   }

   private static void sendMessage(CommandSourceStack $$0, Entity $$1, PlayerTeam $$2, List<ServerPlayer> $$3, PlayerChatMessage $$4) {
      Component $$5 = $$2.getFormattedDisplayName().withStyle(SUGGEST_STYLE);
      Bound $$6 = ChatType.bind(ChatType.TEAM_MSG_COMMAND_INCOMING, $$0).withTargetName($$5);
      Bound $$7 = ChatType.bind(ChatType.TEAM_MSG_COMMAND_OUTGOING, $$0).withTargetName($$5);
      OutgoingChatMessage $$8 = OutgoingChatMessage.create($$4);
      boolean $$9 = false;

      for (ServerPlayer $$10 : $$3) {
         Bound $$11 = $$10 == $$1 ? $$7 : $$6;
         boolean $$12 = $$0.shouldFilterMessageTo($$10);
         $$10.sendChatMessage($$8, $$12, $$11);
         $$9 |= $$12 && $$4.isFullyFiltered();
      }

      if ($$9) {
         $$0.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
      }
   }
}
