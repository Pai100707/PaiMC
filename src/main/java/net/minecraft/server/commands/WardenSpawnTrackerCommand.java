package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Player;

public class WardenSpawnTrackerCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("warden_spawn_tracker")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .then(
                  Commands.literal("clear")
                     .executes(
                        $$0x -> resetTracker(
                           (CommandSourceStack)$$0x.getSource(), ImmutableList.of(((CommandSourceStack)$$0x.getSource()).getPlayerOrException())
                        )
                     )
               ))
            .then(
               Commands.literal("set")
                  .then(
                     Commands.argument("warning_level", IntegerArgumentType.integer(0, 4))
                        .executes(
                           $$0x -> setWarningLevel(
                              (CommandSourceStack)$$0x.getSource(),
                              ImmutableList.of(((CommandSourceStack)$$0x.getSource()).getPlayerOrException()),
                              IntegerArgumentType.getInteger($$0x, "warning_level")
                           )
                        )
                  )
            )
      );
   }

   private static int setWarningLevel(CommandSourceStack $$0, Collection<? extends Player> $$1, int $$2) {
      for (Player $$3 : $$1) {
         $$3.getWardenSpawnTracker().ifPresent($$1x -> $$1x.setWarningLevel($$2));
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(
            () -> Component.translatable("commands.warden_spawn_tracker.set.success.single", new Object[]{$$1.iterator().next().getDisplayName()}), true
         );
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.set.success.multiple", new Object[]{$$1.size()}), true);
      }

      return $$1.size();
   }

   private static int resetTracker(CommandSourceStack $$0, Collection<? extends Player> $$1) {
      for (Player $$2 : $$1) {
         $$2.getWardenSpawnTracker().ifPresent(WardenSpawnTracker::reset);
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(
            () -> Component.translatable("commands.warden_spawn_tracker.clear.success.single", new Object[]{$$1.iterator().next().getDisplayName()}), true
         );
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.clear.success.multiple", new Object[]{$$1.size()}), true);
      }

      return $$1.size();
   }
}
