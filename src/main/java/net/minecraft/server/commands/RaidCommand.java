package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.phys.Vec3;

public class RaidCommand {
   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                    "raid"
                                 )
                                 .requires(Commands.hasPermission(Commands.LEVEL_ADMINS)))
                              .then(
                                 Commands.literal("start")
                                    .then(
                                       Commands.argument("omenlvl", IntegerArgumentType.integer(0))
                                          .executes($$0x -> start((CommandSourceStack)$$0x.getSource(), IntegerArgumentType.getInteger($$0x, "omenlvl")))
                                    )
                              ))
                           .then(Commands.literal("stop").executes($$0x -> stop((CommandSourceStack)$$0x.getSource()))))
                        .then(Commands.literal("check").executes($$0x -> check((CommandSourceStack)$$0x.getSource()))))
                     .then(
                        Commands.literal("sound")
                           .then(
                              Commands.argument("type", ComponentArgument.textComponent($$1))
                                 .executes($$0x -> playSound((CommandSourceStack)$$0x.getSource(), ComponentArgument.getResolvedComponent($$0x, "type")))
                           )
                     ))
                  .then(Commands.literal("spawnleader").executes($$0x -> spawnLeader((CommandSourceStack)$$0x.getSource()))))
               .then(
                  Commands.literal("setomen")
                     .then(
                        Commands.argument("level", IntegerArgumentType.integer(0))
                           .executes($$0x -> setRaidOmenLevel((CommandSourceStack)$$0x.getSource(), IntegerArgumentType.getInteger($$0x, "level")))
                     )
               ))
            .then(Commands.literal("glow").executes($$0x -> glow((CommandSourceStack)$$0x.getSource())))
      );
   }

   private static int glow(CommandSourceStack $$0) throws CommandSyntaxException {
      Raid $$1 = getRaid($$0.getPlayerOrException());
      if ($$1 != null) {
         for (Raider $$3 : $$1.getAllRaiders()) {
            $$3.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1000, 1));
         }
      }

      return 1;
   }

   private static int setRaidOmenLevel(CommandSourceStack $$0, int $$1) throws CommandSyntaxException {
      Raid $$2 = getRaid($$0.getPlayerOrException());
      if ($$2 != null) {
         int $$3 = $$2.getMaxRaidOmenLevel();
         if ($$1 > $$3) {
            $$0.sendFailure(Component.literal("Sorry, the max raid omen level you can set is " + $$3));
         } else {
            int $$4 = $$2.getRaidOmenLevel();
            $$2.setRaidOmenLevel($$1);
            $$0.sendSuccess(() -> Component.literal("Changed village's raid omen level from " + $$4 + " to " + $$1), false);
         }
      } else {
         $$0.sendFailure(Component.literal("No raid found here"));
      }

      return 1;
   }

   private static int spawnLeader(CommandSourceStack $$0) {
      $$0.sendSuccess(() -> Component.literal("Spawned a raid captain"), false);
      Raider $$1 = (Raider)EntityType.PILLAGER.create($$0.getLevel(), EntitySpawnReason.COMMAND);
      if ($$1 == null) {
         $$0.sendFailure(Component.literal("Pillager failed to spawn"));
         return 0;
      } else {
         $$1.setPatrolLeader(true);
         $$1.setItemSlot(EquipmentSlot.HEAD, Raid.getOminousBannerInstance($$0.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
         $$1.setPos($$0.getPosition().x, $$0.getPosition().y, $$0.getPosition().z);
         $$1.finalizeSpawn($$0.getLevel(), $$0.getLevel().getCurrentDifficultyAt(BlockPos.containing($$0.getPosition())), EntitySpawnReason.COMMAND, null);
         $$0.getLevel().addFreshEntityWithPassengers($$1);
         return 1;
      }
   }

   private static int playSound(CommandSourceStack $$0, Component $$1) {
      if ($$1 != null && $$1.getString().equals("local")) {
         ServerLevel $$2 = $$0.getLevel();
         Vec3 $$3 = $$0.getPosition().add(5.0, 0.0, 0.0);
         $$2.playSeededSound(null, $$3.x, $$3.y, $$3.z, SoundEvents.RAID_HORN, SoundSource.NEUTRAL, 2.0F, 1.0F, $$2.random.nextLong());
      }

      return 1;
   }

   private static int start(CommandSourceStack $$0, int $$1) throws CommandSyntaxException {
      ServerPlayer $$2 = $$0.getPlayerOrException();
      BlockPos $$3 = $$2.blockPosition();
      if ($$2.level().isRaided($$3)) {
         $$0.sendFailure(Component.literal("Raid already started close by"));
         return -1;
      } else {
         Raids $$4 = $$2.level().getRaids();
         Raid $$5 = $$4.createOrExtendRaid($$2, $$2.blockPosition());
         if ($$5 != null) {
            $$5.setRaidOmenLevel($$1);
            $$4.setDirty();
            $$0.sendSuccess(() -> Component.literal("Created a raid in your local village"), false);
         } else {
            $$0.sendFailure(Component.literal("Failed to create a raid in your local village"));
         }

         return 1;
      }
   }

   private static int stop(CommandSourceStack $$0) throws CommandSyntaxException {
      ServerPlayer $$1 = $$0.getPlayerOrException();
      BlockPos $$2 = $$1.blockPosition();
      Raid $$3 = $$1.level().getRaidAt($$2);
      if ($$3 != null) {
         $$3.stop();
         $$0.sendSuccess(() -> Component.literal("Stopped raid"), false);
         return 1;
      } else {
         $$0.sendFailure(Component.literal("No raid here"));
         return -1;
      }
   }

   private static int check(CommandSourceStack $$0) throws CommandSyntaxException {
      Raid $$1 = getRaid($$0.getPlayerOrException());
      if ($$1 != null) {
         StringBuilder $$2 = new StringBuilder();
         $$2.append("Found a started raid! ");
         $$0.sendSuccess(() -> Component.literal($$2.toString()), false);
         StringBuilder $$3 = new StringBuilder();
         $$3.append("Num groups spawned: ");
         $$3.append($$1.getGroupsSpawned());
         $$3.append(" Raid omen level: ");
         $$3.append($$1.getRaidOmenLevel());
         $$3.append(" Num mobs: ");
         $$3.append($$1.getTotalRaidersAlive());
         $$3.append(" Raid health: ");
         $$3.append($$1.getHealthOfLivingRaiders());
         $$3.append(" / ");
         $$3.append($$1.getTotalHealth());
         $$0.sendSuccess(() -> Component.literal($$3.toString()), false);
         return 1;
      } else {
         $$0.sendFailure(Component.literal("Found no started raids"));
         return 0;
      }
   }

   
   private static Raid getRaid(ServerPlayer $$0) {
      return $$0.level().getRaidAt($$0.blockPosition());
   }
}
