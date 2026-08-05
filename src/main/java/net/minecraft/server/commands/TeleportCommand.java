package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class TeleportCommand {
   private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(
      Component.translatable("commands.teleport.invalidPosition")
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      LiteralCommandNode<CommandSourceStack> $$1 = $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("teleport")
                     .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                  .then(
                     Commands.argument("location", Vec3Argument.vec3())
                        .executes(
                           $$0x -> teleportToPos(
                              (CommandSourceStack)$$0x.getSource(),
                              Collections.singleton(((CommandSourceStack)$$0x.getSource()).getEntityOrException()),
                              ((CommandSourceStack)$$0x.getSource()).getLevel(),
                              Vec3Argument.getCoordinates($$0x, "location"),
                              null,
                              null
                           )
                        )
                  ))
               .then(
                  Commands.argument("destination", EntityArgument.entity())
                     .executes(
                        $$0x -> teleportToEntity(
                           (CommandSourceStack)$$0x.getSource(),
                           Collections.singleton(((CommandSourceStack)$$0x.getSource()).getEntityOrException()),
                           EntityArgument.getEntity($$0x, "destination")
                        )
                     )
               ))
            .then(
               ((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.entities())
                     .then(
                        ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("location", Vec3Argument.vec3())
                                 .executes(
                                    $$0x -> teleportToPos(
                                       (CommandSourceStack)$$0x.getSource(),
                                       EntityArgument.getEntities($$0x, "targets"),
                                       ((CommandSourceStack)$$0x.getSource()).getLevel(),
                                       Vec3Argument.getCoordinates($$0x, "location"),
                                       null,
                                       null
                                    )
                                 ))
                              .then(
                                 Commands.argument("rotation", RotationArgument.rotation())
                                    .executes(
                                       $$0x -> teleportToPos(
                                          (CommandSourceStack)$$0x.getSource(),
                                          EntityArgument.getEntities($$0x, "targets"),
                                          ((CommandSourceStack)$$0x.getSource()).getLevel(),
                                          Vec3Argument.getCoordinates($$0x, "location"),
                                          RotationArgument.getRotation($$0x, "rotation"),
                                          null
                                       )
                                    )
                              ))
                           .then(
                              ((LiteralArgumentBuilder)Commands.literal("facing")
                                    .then(
                                       Commands.literal("entity")
                                          .then(
                                             ((RequiredArgumentBuilder)Commands.argument("facingEntity", EntityArgument.entity())
                                                   .executes(
                                                      $$0x -> teleportToPos(
                                                         (CommandSourceStack)$$0x.getSource(),
                                                         EntityArgument.getEntities($$0x, "targets"),
                                                         ((CommandSourceStack)$$0x.getSource()).getLevel(),
                                                         Vec3Argument.getCoordinates($$0x, "location"),
                                                         null,
                                                         new LookAt.LookAtEntity(EntityArgument.getEntity($$0x, "facingEntity"), Anchor.FEET)
                                                      )
                                                   ))
                                                .then(
                                                   Commands.argument("facingAnchor", EntityAnchorArgument.anchor())
                                                      .executes(
                                                         $$0x -> teleportToPos(
                                                            (CommandSourceStack)$$0x.getSource(),
                                                            EntityArgument.getEntities($$0x, "targets"),
                                                            ((CommandSourceStack)$$0x.getSource()).getLevel(),
                                                            Vec3Argument.getCoordinates($$0x, "location"),
                                                            null,
                                                            new LookAt.LookAtEntity(
                                                               EntityArgument.getEntity($$0x, "facingEntity"),
                                                               EntityAnchorArgument.getAnchor($$0x, "facingAnchor")
                                                            )
                                                         )
                                                      )
                                                )
                                          )
                                    ))
                                 .then(
                                    Commands.argument("facingLocation", Vec3Argument.vec3())
                                       .executes(
                                          $$0x -> teleportToPos(
                                             (CommandSourceStack)$$0x.getSource(),
                                             EntityArgument.getEntities($$0x, "targets"),
                                             ((CommandSourceStack)$$0x.getSource()).getLevel(),
                                             Vec3Argument.getCoordinates($$0x, "location"),
                                             null,
                                             new LookAt.LookAtPosition(Vec3Argument.getVec3($$0x, "facingLocation"))
                                          )
                                       )
                                 )
                           )
                     ))
                  .then(
                     Commands.argument("destination", EntityArgument.entity())
                        .executes(
                           $$0x -> teleportToEntity(
                              (CommandSourceStack)$$0x.getSource(), EntityArgument.getEntities($$0x, "targets"), EntityArgument.getEntity($$0x, "destination")
                           )
                        )
                  )
            )
      );
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tp").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).redirect($$1)
      );
   }

   private static int teleportToEntity(CommandSourceStack $$0, Collection<? extends Entity> $$1, Entity $$2) throws CommandSyntaxException {
      for (Entity $$3 : $$1) {
         performTeleport(
            $$0, $$3, (ServerLevel)$$2.level(), $$2.getX(), $$2.getY(), $$2.getZ(), EnumSet.noneOf(Relative.class), $$2.getYRot(), $$2.getXRot(), null
         );
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(
            () -> Component.translatable("commands.teleport.success.entity.single", new Object[]{$$1.iterator().next().getDisplayName(), $$2.getDisplayName()}),
            true
         );
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.teleport.success.entity.multiple", new Object[]{$$1.size(), $$2.getDisplayName()}), true);
      }

      return $$1.size();
   }

   private static int teleportToPos(
      CommandSourceStack $$0, Collection<? extends Entity> $$1, ServerLevel $$2, Coordinates $$3, Coordinates $$4, LookAt $$5
   ) throws CommandSyntaxException {
      Vec3 $$6 = $$3.getPosition($$0);
      Vec2 $$7 = $$4 == null ? null : $$4.getRotation($$0);

      for (Entity $$8 : $$1) {
         Set<Relative> $$9 = getRelatives($$3, $$4, $$8.level().dimension() == $$2.dimension());
         if ($$7 == null) {
            performTeleport($$0, $$8, $$2, $$6.x, $$6.y, $$6.z, $$9, $$8.getYRot(), $$8.getXRot(), $$5);
         } else {
            performTeleport($$0, $$8, $$2, $$6.x, $$6.y, $$6.z, $$9, $$7.y, $$7.x, $$5);
         }
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.teleport.success.location.single",
               new Object[]{$$1.iterator().next().getDisplayName(), formatDouble($$6.x), formatDouble($$6.y), formatDouble($$6.z)}
            ),
            true
         );
      } else {
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.teleport.success.location.multiple", new Object[]{$$1.size(), formatDouble($$6.x), formatDouble($$6.y), formatDouble($$6.z)}
            ),
            true
         );
      }

      return $$1.size();
   }

   private static Set<Relative> getRelatives(Coordinates $$0, Coordinates $$1, boolean $$2) {
      Set<Relative> $$3 = Relative.direction($$0.isXRelative(), $$0.isYRelative(), $$0.isZRelative());
      Set<Relative> $$4 = $$2 ? Relative.position($$0.isXRelative(), $$0.isYRelative(), $$0.isZRelative()) : Set.of();
      Set<Relative> $$5 = $$1 == null ? Relative.ROTATION : Relative.rotation($$1.isYRelative(), $$1.isXRelative());
      return Relative.union(new Set[]{$$3, $$4, $$5});
   }

   private static String formatDouble(double $$0) {
      return String.format(Locale.ROOT, "%f", $$0);
   }

   private static void performTeleport(
      CommandSourceStack $$0, Entity $$1, ServerLevel $$2, double $$3, double $$4, double $$5, Set<Relative> $$6, float $$7, float $$8, LookAt $$9
   ) throws CommandSyntaxException {
      BlockPos $$10 = BlockPos.containing($$3, $$4, $$5);
      if (!Level.isInSpawnableBounds($$10)) {
         throw INVALID_POSITION.create();
      } else {
         double $$11 = $$6.contains(Relative.X) ? $$3 - $$1.getX() : $$3;
         double $$12 = $$6.contains(Relative.Y) ? $$4 - $$1.getY() : $$4;
         double $$13 = $$6.contains(Relative.Z) ? $$5 - $$1.getZ() : $$5;
         float $$14 = $$6.contains(Relative.Y_ROT) ? $$7 - $$1.getYRot() : $$7;
         float $$15 = $$6.contains(Relative.X_ROT) ? $$8 - $$1.getXRot() : $$8;
         float $$16 = Mth.wrapDegrees($$14);
         float $$17 = Mth.wrapDegrees($$15);
         if ($$1.teleportTo($$2, $$11, $$12, $$13, $$6, $$16, $$17, true)) {
            if ($$9 != null) {
               $$9.perform($$0, $$1);
            }

            if (!($$1 instanceof LivingEntity $$18 && $$18.isFallFlying())) {
               $$1.setDeltaMovement($$1.getDeltaMovement().multiply(1.0, 0.0, 1.0));
               $$1.setOnGround(true);
            }

            if ($$1 instanceof PathfinderMob $$19) {
               $$19.getNavigation().stop();
            }
         }
      }
   }
}
