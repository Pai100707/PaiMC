package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class KillCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("kill")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .executes($$0x -> kill((CommandSourceStack)$$0x.getSource(), ImmutableList.of(((CommandSourceStack)$$0x.getSource()).getEntityOrException()))))
            .then(
               Commands.argument("targets", EntityArgument.entities())
                  .executes($$0x -> kill((CommandSourceStack)$$0x.getSource(), EntityArgument.getEntities($$0x, "targets")))
            )
      );
   }

   private static int kill(CommandSourceStack $$0, Collection<? extends Entity> $$1) {
      for (Entity $$2 : $$1) {
         $$2.kill($$0.getLevel());
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(() -> Component.translatable("commands.kill.success.single", new Object[]{$$1.iterator().next().getDisplayName()}), true);
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.kill.success.multiple", new Object[]{$$1.size()}), true);
      }

      return $$1.size();
   }
}
