package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ExperienceCommand {
   private static final SimpleCommandExceptionType ERROR_SET_POINTS_INVALID = new SimpleCommandExceptionType(
      Component.translatable("commands.experience.set.points.invalid")
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      LiteralCommandNode<CommandSourceStack> $$1 = $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("experience")
                     .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
                  .then(
                     Commands.literal("add")
                        .then(
                           Commands.argument("target", EntityArgument.players())
                              .then(
                                 ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("amount", IntegerArgumentType.integer())
                                          .executes(
                                             $$0x -> addExperience(
                                                (CommandSourceStack)$$0x.getSource(),
                                                EntityArgument.getPlayers($$0x, "target"),
                                                IntegerArgumentType.getInteger($$0x, "amount"),
                                                ExperienceCommand.Type.POINTS
                                             )
                                          ))
                                       .then(
                                          Commands.literal("points")
                                             .executes(
                                                $$0x -> addExperience(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   EntityArgument.getPlayers($$0x, "target"),
                                                   IntegerArgumentType.getInteger($$0x, "amount"),
                                                   ExperienceCommand.Type.POINTS
                                                )
                                             )
                                       ))
                                    .then(
                                       Commands.literal("levels")
                                          .executes(
                                             $$0x -> addExperience(
                                                (CommandSourceStack)$$0x.getSource(),
                                                EntityArgument.getPlayers($$0x, "target"),
                                                IntegerArgumentType.getInteger($$0x, "amount"),
                                                ExperienceCommand.Type.LEVELS
                                             )
                                          )
                                    )
                              )
                        )
                  ))
               .then(
                  Commands.literal("set")
                     .then(
                        Commands.argument("target", EntityArgument.players())
                           .then(
                              ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("amount", IntegerArgumentType.integer(0))
                                       .executes(
                                          $$0x -> setExperience(
                                             (CommandSourceStack)$$0x.getSource(),
                                             EntityArgument.getPlayers($$0x, "target"),
                                             IntegerArgumentType.getInteger($$0x, "amount"),
                                             ExperienceCommand.Type.POINTS
                                          )
                                       ))
                                    .then(
                                       Commands.literal("points")
                                          .executes(
                                             $$0x -> setExperience(
                                                (CommandSourceStack)$$0x.getSource(),
                                                EntityArgument.getPlayers($$0x, "target"),
                                                IntegerArgumentType.getInteger($$0x, "amount"),
                                                ExperienceCommand.Type.POINTS
                                             )
                                          )
                                    ))
                                 .then(
                                    Commands.literal("levels")
                                       .executes(
                                          $$0x -> setExperience(
                                             (CommandSourceStack)$$0x.getSource(),
                                             EntityArgument.getPlayers($$0x, "target"),
                                             IntegerArgumentType.getInteger($$0x, "amount"),
                                             ExperienceCommand.Type.LEVELS
                                          )
                                       )
                                 )
                           )
                     )
               ))
            .then(
               Commands.literal("query")
                  .then(
                     ((RequiredArgumentBuilder)Commands.argument("target", EntityArgument.player())
                           .then(
                              Commands.literal("points")
                                 .executes(
                                    $$0x -> queryExperience(
                                       (CommandSourceStack)$$0x.getSource(), EntityArgument.getPlayer($$0x, "target"), ExperienceCommand.Type.POINTS
                                    )
                                 )
                           ))
                        .then(
                           Commands.literal("levels")
                              .executes(
                                 $$0x -> queryExperience(
                                    (CommandSourceStack)$$0x.getSource(), EntityArgument.getPlayer($$0x, "target"), ExperienceCommand.Type.LEVELS
                                 )
                              )
                        )
                  )
            )
      );
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("xp").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).redirect($$1)
      );
   }

   private static int queryExperience(CommandSourceStack $$0, ServerPlayer $$1, ExperienceCommand.Type $$2) {
      int $$3 = $$2.query.applyAsInt($$1);
      $$0.sendSuccess(() -> Component.translatable("commands.experience.query." + $$2.name, new Object[]{$$1.getDisplayName(), $$3}), false);
      return $$3;
   }

   private static int addExperience(CommandSourceStack $$0, Collection<? extends ServerPlayer> $$1, int $$2, ExperienceCommand.Type $$3) {
      for (ServerPlayer $$4 : $$1) {
         $$3.add.accept($$4, $$2);
      }

      if ($$1.size() == 1) {
         $$0.sendSuccess(
            () -> Component.translatable("commands.experience.add." + $$3.name + ".success.single", new Object[]{$$2, $$1.iterator().next().getDisplayName()}),
            true
         );
      } else {
         $$0.sendSuccess(() -> Component.translatable("commands.experience.add." + $$3.name + ".success.multiple", new Object[]{$$2, $$1.size()}), true);
      }

      return $$1.size();
   }

   private static int setExperience(CommandSourceStack $$0, Collection<? extends ServerPlayer> $$1, int $$2, ExperienceCommand.Type $$3) throws CommandSyntaxException {
      int $$4 = 0;

      for (ServerPlayer $$5 : $$1) {
         if ($$3.set.test($$5, $$2)) {
            $$4++;
         }
      }

      if ($$4 == 0) {
         throw ERROR_SET_POINTS_INVALID.create();
      } else {
         if ($$1.size() == 1) {
            $$0.sendSuccess(
               () -> Component.translatable(
                  "commands.experience.set." + $$3.name + ".success.single", new Object[]{$$2, $$1.iterator().next().getDisplayName()}
               ),
               true
            );
         } else {
            $$0.sendSuccess(() -> Component.translatable("commands.experience.set." + $$3.name + ".success.multiple", new Object[]{$$2, $$1.size()}), true);
         }

         return $$1.size();
      }
   }

   static enum Type {
      POINTS("points", Player::giveExperiencePoints, ($$0, $$1) -> {
         if ($$1 >= $$0.getXpNeededForNextLevel()) {
            return false;
         } else {
            $$0.setExperiencePoints($$1);
            return true;
         }
      }, $$0 -> Mth.floor($$0.experienceProgress * $$0.getXpNeededForNextLevel())),
      LEVELS("levels", ServerPlayer::giveExperienceLevels, ($$0, $$1) -> {
         $$0.setExperienceLevels($$1);
         return true;
      }, $$0 -> $$0.experienceLevel);

      public final BiConsumer<ServerPlayer, Integer> add;
      public final BiPredicate<ServerPlayer, Integer> set;
      public final String name;
      final ToIntFunction<ServerPlayer> query;

      private Type(
         final String $$0, final BiConsumer<ServerPlayer, Integer> $$1, final BiPredicate<ServerPlayer, Integer> $$2, final ToIntFunction<ServerPlayer> $$3
      ) {
         this.add = $$1;
         this.name = $$0;
         this.set = $$2;
         this.query = $$3;
      }
   }
}
