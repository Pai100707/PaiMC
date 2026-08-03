package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class RideCommand {
   private static final DynamicCommandExceptionType ERROR_NOT_RIDING = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.ride.not_riding", new Object[]{$$0})
   );
   private static final Dynamic2CommandExceptionType ERROR_ALREADY_RIDING = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.ride.already_riding", new Object[]{$$0, $$1})
   );
   private static final Dynamic2CommandExceptionType ERROR_MOUNT_FAILED = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.ride.mount.failure.generic", new Object[]{$$0, $$1})
   );
   private static final SimpleCommandExceptionType ERROR_MOUNTING_PLAYER = new SimpleCommandExceptionType(
      Component.translatable("commands.ride.mount.failure.cant_ride_players")
   );
   private static final SimpleCommandExceptionType ERROR_MOUNTING_LOOP = new SimpleCommandExceptionType(
      Component.translatable("commands.ride.mount.failure.loop")
   );
   private static final SimpleCommandExceptionType ERROR_WRONG_DIMENSION = new SimpleCommandExceptionType(
      Component.translatable("commands.ride.mount.failure.wrong_dimension")
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("ride").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               ((RequiredArgumentBuilder)Commands.argument("target", EntityArgument.entity())
                     .then(
                        Commands.literal("mount")
                           .then(
                              Commands.argument("vehicle", EntityArgument.entity())
                                 .executes(
                                    $$0x -> mount(
                                       (CommandSourceStack)$$0x.getSource(),
                                       EntityArgument.getEntity($$0x, "target"),
                                       EntityArgument.getEntity($$0x, "vehicle")
                                    )
                                 )
                           )
                     ))
                  .then(Commands.literal("dismount").executes($$0x -> dismount((CommandSourceStack)$$0x.getSource(), EntityArgument.getEntity($$0x, "target"))))
            )
      );
   }

   private static int mount(CommandSourceStack $$0, Entity $$1, Entity $$2) throws CommandSyntaxException {
      Entity $$3 = $$1.getVehicle();
      if ($$3 != null) {
         throw ERROR_ALREADY_RIDING.create($$1.getDisplayName(), $$3.getDisplayName());
      } else if ($$2.getType() == EntityType.PLAYER) {
         throw ERROR_MOUNTING_PLAYER.create();
      } else if ($$1.getSelfAndPassengers().anyMatch($$1x -> $$1x == $$2)) {
         throw ERROR_MOUNTING_LOOP.create();
      } else if ($$1.level() != $$2.level()) {
         throw ERROR_WRONG_DIMENSION.create();
      } else if (!$$1.startRiding($$2, true, true)) {
         throw ERROR_MOUNT_FAILED.create($$1.getDisplayName(), $$2.getDisplayName());
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.ride.mount.success", new Object[]{$$1.getDisplayName(), $$2.getDisplayName()}), true);
         return 1;
      }
   }

   private static int dismount(CommandSourceStack $$0, Entity $$1) throws CommandSyntaxException {
      Entity $$2 = $$1.getVehicle();
      if ($$2 == null) {
         throw ERROR_NOT_RIDING.create($$1.getDisplayName());
      } else {
         $$1.stopRiding();
         $$0.sendSuccess(() -> Component.translatable("commands.ride.dismount.success", new Object[]{$$1.getDisplayName(), $$2.getDisplayName()}), true);
         return 1;
      }
   }
}
