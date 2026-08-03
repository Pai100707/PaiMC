package net.minecraft.server.commands;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.scores.Team;

public class SpreadPlayersCommand {
   private static final int MAX_ITERATION_COUNT = 10000;
   private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_TEAMS = new Dynamic4CommandExceptionType(
      ($$0, $$1, $$2, $$3) -> Component.translatableEscape("commands.spreadplayers.failed.teams", new Object[]{$$0, $$1, $$2, $$3})
   );
   private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_ENTITIES = new Dynamic4CommandExceptionType(
      ($$0, $$1, $$2, $$3) -> Component.translatableEscape("commands.spreadplayers.failed.entities", new Object[]{$$0, $$1, $$2, $$3})
   );
   private static final Dynamic2CommandExceptionType ERROR_INVALID_MAX_HEIGHT = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("commands.spreadplayers.failed.invalid.height", new Object[]{$$0, $$1})
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("spreadplayers").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               Commands.argument("center", Vec2Argument.vec2())
                  .then(
                     Commands.argument("spreadDistance", FloatArgumentType.floatArg(0.0F))
                        .then(
                           ((RequiredArgumentBuilder)Commands.argument("maxRange", FloatArgumentType.floatArg(1.0F))
                                 .then(
                                    Commands.argument("respectTeams", BoolArgumentType.bool())
                                       .then(
                                          Commands.argument("targets", EntityArgument.entities())
                                             .executes(
                                                $$0x -> spreadPlayers(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   Vec2Argument.getVec2($$0x, "center"),
                                                   FloatArgumentType.getFloat($$0x, "spreadDistance"),
                                                   FloatArgumentType.getFloat($$0x, "maxRange"),
                                                   ((CommandSourceStack)$$0x.getSource()).getLevel().getMaxY() + 1,
                                                   BoolArgumentType.getBool($$0x, "respectTeams"),
                                                   EntityArgument.getEntities($$0x, "targets")
                                                )
                                             )
                                       )
                                 ))
                              .then(
                                 Commands.literal("under")
                                    .then(
                                       Commands.argument("maxHeight", IntegerArgumentType.integer())
                                          .then(
                                             Commands.argument("respectTeams", BoolArgumentType.bool())
                                                .then(
                                                   Commands.argument("targets", EntityArgument.entities())
                                                      .executes(
                                                         $$0x -> spreadPlayers(
                                                            (CommandSourceStack)$$0x.getSource(),
                                                            Vec2Argument.getVec2($$0x, "center"),
                                                            FloatArgumentType.getFloat($$0x, "spreadDistance"),
                                                            FloatArgumentType.getFloat($$0x, "maxRange"),
                                                            IntegerArgumentType.getInteger($$0x, "maxHeight"),
                                                            BoolArgumentType.getBool($$0x, "respectTeams"),
                                                            EntityArgument.getEntities($$0x, "targets")
                                                         )
                                                      )
                                                )
                                          )
                                    )
                              )
                        )
                  )
            )
      );
   }

   private static int spreadPlayers(CommandSourceStack $$0, Vec2 $$1, float $$2, float $$3, int $$4, boolean $$5, Collection<? extends Entity> $$6) throws CommandSyntaxException {
      ServerLevel $$7 = $$0.getLevel();
      int $$8 = $$7.getMinY();
      if ($$4 < $$8) {
         throw ERROR_INVALID_MAX_HEIGHT.create($$4, $$8);
      } else {
         RandomSource $$9 = RandomSource.create();
         double $$10 = $$1.x - $$3;
         double $$11 = $$1.y - $$3;
         double $$12 = $$1.x + $$3;
         double $$13 = $$1.y + $$3;
         SpreadPlayersCommand.Position[] $$14 = createInitialPositions($$9, $$5 ? getNumberOfTeams($$6) : $$6.size(), $$10, $$11, $$12, $$13);
         spreadPositions($$1, $$2, $$7, $$9, $$10, $$11, $$12, $$13, $$4, $$14, $$5);
         double $$15 = setPlayerPositions($$6, $$7, $$14, $$4, $$5);
         $$0.sendSuccess(
            () -> Component.translatable(
               "commands.spreadplayers.success." + ($$5 ? "teams" : "entities"),
               new Object[]{$$14.length, $$1.x, $$1.y, String.format(Locale.ROOT, "%.2f", $$15)}
            ),
            true
         );
         return $$14.length;
      }
   }

   private static int getNumberOfTeams(Collection<? extends Entity> $$0) {
      Set<Team> $$1 = Sets.newHashSet();

      for (Entity $$2 : $$0) {
         if ($$2 instanceof Player) {
            $$1.add($$2.getTeam());
         } else {
            $$1.add(null);
         }
      }

      return $$1.size();
   }

   private static void spreadPositions(
      Vec2 $$0,
      double $$1,
      ServerLevel $$2,
      RandomSource $$3,
      double $$4,
      double $$5,
      double $$6,
      double $$7,
      int $$8,
      SpreadPlayersCommand.Position[] $$9,
      boolean $$10
   ) throws CommandSyntaxException {
      boolean $$11 = true;
      double $$12 = Float.MAX_VALUE;

      int $$13;
      for ($$13 = 0; $$13 < 10000 && $$11; $$13++) {
         $$11 = false;
         $$12 = Float.MAX_VALUE;

         for (int $$14 = 0; $$14 < $$9.length; $$14++) {
            SpreadPlayersCommand.Position $$15 = $$9[$$14];
            int $$16 = 0;
            SpreadPlayersCommand.Position $$17 = new SpreadPlayersCommand.Position();

            for (int $$18 = 0; $$18 < $$9.length; $$18++) {
               if ($$14 != $$18) {
                  SpreadPlayersCommand.Position $$19 = $$9[$$18];
                  double $$20 = $$15.dist($$19);
                  $$12 = Math.min($$20, $$12);
                  if ($$20 < $$1) {
                     $$16++;
                     $$17.x = $$17.x + ($$19.x - $$15.x);
                     $$17.z = $$17.z + ($$19.z - $$15.z);
                  }
               }
            }

            if ($$16 > 0) {
               $$17.x /= $$16;
               $$17.z /= $$16;
               double $$21 = $$17.getLength();
               if ($$21 > 0.0) {
                  $$17.normalize();
                  $$15.moveAway($$17);
               } else {
                  $$15.randomize($$3, $$4, $$5, $$6, $$7);
               }

               $$11 = true;
            }

            if ($$15.clamp($$4, $$5, $$6, $$7)) {
               $$11 = true;
            }
         }

         if (!$$11) {
            for (SpreadPlayersCommand.Position $$22 : $$9) {
               if (!$$22.isSafe($$2, $$8)) {
                  $$22.randomize($$3, $$4, $$5, $$6, $$7);
                  $$11 = true;
               }
            }
         }
      }

      if ($$12 == Float.MAX_VALUE) {
         $$12 = 0.0;
      }

      if ($$13 >= 10000) {
         if ($$10) {
            throw ERROR_FAILED_TO_SPREAD_TEAMS.create($$9.length, $$0.x, $$0.y, String.format(Locale.ROOT, "%.2f", $$12));
         } else {
            throw ERROR_FAILED_TO_SPREAD_ENTITIES.create($$9.length, $$0.x, $$0.y, String.format(Locale.ROOT, "%.2f", $$12));
         }
      }
   }

   private static double setPlayerPositions(Collection<? extends Entity> $$0, ServerLevel $$1, SpreadPlayersCommand.Position[] $$2, int $$3, boolean $$4) {
      double $$5 = 0.0;
      int $$6 = 0;
      Map<Team, SpreadPlayersCommand.Position> $$7 = Maps.newHashMap();

      for (Entity $$8 : $$0) {
         SpreadPlayersCommand.Position $$10;
         if ($$4) {
            Team $$9 = $$8 instanceof Player ? $$8.getTeam() : null;
            if (!$$7.containsKey($$9)) {
               $$7.put($$9, $$2[$$6++]);
            }

            $$10 = $$7.get($$9);
         } else {
            $$10 = $$2[$$6++];
         }

         $$8.teleportTo($$1, Mth.floor($$10.x) + 0.5, $$10.getSpawnY($$1, $$3), Mth.floor($$10.z) + 0.5, Set.of(), $$8.getYRot(), $$8.getXRot(), true);
         double $$12 = Double.MAX_VALUE;

         for (SpreadPlayersCommand.Position $$13 : $$2) {
            if ($$10 != $$13) {
               double $$14 = $$10.dist($$13);
               $$12 = Math.min($$14, $$12);
            }
         }

         $$5 += $$12;
      }

      return $$0.size() < 2 ? 0.0 : $$5 / $$0.size();
   }

   private static SpreadPlayersCommand.Position[] createInitialPositions(RandomSource $$0, int $$1, double $$2, double $$3, double $$4, double $$5) {
      SpreadPlayersCommand.Position[] $$6 = new SpreadPlayersCommand.Position[$$1];

      for (int $$7 = 0; $$7 < $$6.length; $$7++) {
         SpreadPlayersCommand.Position $$8 = new SpreadPlayersCommand.Position();
         $$8.randomize($$0, $$2, $$3, $$4, $$5);
         $$6[$$7] = $$8;
      }

      return $$6;
   }

   static class Position {
      double x;
      double z;

      double dist(SpreadPlayersCommand.Position $$0) {
         double $$1 = this.x - $$0.x;
         double $$2 = this.z - $$0.z;
         return Math.sqrt($$1 * $$1 + $$2 * $$2);
      }

      void normalize() {
         double $$0 = this.getLength();
         this.x /= $$0;
         this.z /= $$0;
      }

      double getLength() {
         return Math.sqrt(this.x * this.x + this.z * this.z);
      }

      public void moveAway(SpreadPlayersCommand.Position $$0) {
         this.x = this.x - $$0.x;
         this.z = this.z - $$0.z;
      }

      public boolean clamp(double $$0, double $$1, double $$2, double $$3) {
         boolean $$4 = false;
         if (this.x < $$0) {
            this.x = $$0;
            $$4 = true;
         } else if (this.x > $$2) {
            this.x = $$2;
            $$4 = true;
         }

         if (this.z < $$1) {
            this.z = $$1;
            $$4 = true;
         } else if (this.z > $$3) {
            this.z = $$3;
            $$4 = true;
         }

         return $$4;
      }

      public int getSpawnY(BlockGetter $$0, int $$1) {
         MutableBlockPos $$2 = new MutableBlockPos(this.x, $$1 + 1, this.z);
         boolean $$3 = $$0.getBlockState($$2).isAir();
         $$2.move(Direction.DOWN);
         boolean $$4 = $$0.getBlockState($$2).isAir();

         while ($$2.getY() > $$0.getMinY()) {
            $$2.move(Direction.DOWN);
            boolean $$5 = $$0.getBlockState($$2).isAir();
            if (!$$5 && $$4 && $$3) {
               return $$2.getY() + 1;
            }

            $$3 = $$4;
            $$4 = $$5;
         }

         return $$1 + 1;
      }

      public boolean isSafe(BlockGetter $$0, int $$1) {
         BlockPos $$2 = BlockPos.containing(this.x, this.getSpawnY($$0, $$1) - 1, this.z);
         BlockState $$3 = $$0.getBlockState($$2);
         return $$2.getY() < $$1 && !$$3.liquid() && !$$3.is(BlockTags.FIRE);
      }

      public void randomize(RandomSource $$0, double $$1, double $$2, double $$3, double $$4) {
         this.x = Mth.nextDouble($$0, $$1, $$3);
         this.z = Mth.nextDouble($$0, $$2, $$4);
      }
   }
}
