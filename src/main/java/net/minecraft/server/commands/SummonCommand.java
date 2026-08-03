package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SummonCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed"));
   private static final SimpleCommandExceptionType ERROR_FAILED_PEACEFUL = new SimpleCommandExceptionType(
      Component.translatable("commands.summon.failed.peaceful")
   );
   private static final SimpleCommandExceptionType ERROR_DUPLICATE_UUID = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed.uuid"));
   private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.summon.invalidPosition"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("summon").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               ((RequiredArgumentBuilder)Commands.argument("entity", ResourceArgument.resource($$1, Registries.ENTITY_TYPE))
                     .suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES))
                     .executes(
                        $$0x -> spawnEntity(
                           (CommandSourceStack)$$0x.getSource(),
                           ResourceArgument.getSummonableEntityType($$0x, "entity"),
                           ((CommandSourceStack)$$0x.getSource()).getPosition(),
                           new CompoundTag(),
                           true
                        )
                     ))
                  .then(
                     ((RequiredArgumentBuilder)Commands.argument("pos", Vec3Argument.vec3())
                           .executes(
                              $$0x -> spawnEntity(
                                 (CommandSourceStack)$$0x.getSource(),
                                 ResourceArgument.getSummonableEntityType($$0x, "entity"),
                                 Vec3Argument.getVec3($$0x, "pos"),
                                 new CompoundTag(),
                                 true
                              )
                           ))
                        .then(
                           Commands.argument("nbt", CompoundTagArgument.compoundTag())
                              .executes(
                                 $$0x -> spawnEntity(
                                    (CommandSourceStack)$$0x.getSource(),
                                    ResourceArgument.getSummonableEntityType($$0x, "entity"),
                                    Vec3Argument.getVec3($$0x, "pos"),
                                    CompoundTagArgument.getCompoundTag($$0x, "nbt"),
                                    false
                                 )
                              )
                        )
                  )
            )
      );
   }

   public static Entity createEntity(CommandSourceStack $$0, Reference<EntityType<?>> $$1, Vec3 $$2, CompoundTag $$3, boolean $$4) throws CommandSyntaxException {
      BlockPos $$5 = BlockPos.containing($$2);
      if (!Level.isInSpawnableBounds($$5)) {
         throw INVALID_POSITION.create();
      } else if ($$0.getLevel().getDifficulty() == Difficulty.PEACEFUL && !((EntityType)$$1.value()).isAllowedInPeaceful()) {
         throw ERROR_FAILED_PEACEFUL.create();
      } else {
         CompoundTag $$6 = $$3.copy();
         $$6.putString("id", $$1.key().identifier().toString());
         ServerLevel $$7 = $$0.getLevel();
         Entity $$8 = EntityType.loadEntityRecursive($$6, $$7, EntitySpawnReason.COMMAND, $$1x -> {
            $$1x.snapTo($$2.x, $$2.y, $$2.z, $$1x.getYRot(), $$1x.getXRot());
            return $$1x;
         });
         if ($$8 == null) {
            throw ERROR_FAILED.create();
         } else {
            if ($$4 && $$8 instanceof Mob $$9) {
               $$9.finalizeSpawn($$0.getLevel(), $$0.getLevel().getCurrentDifficultyAt($$8.blockPosition()), EntitySpawnReason.COMMAND, null);
            }

            if (!$$7.tryAddFreshEntityWithPassengers($$8)) {
               throw ERROR_DUPLICATE_UUID.create();
            } else {
               return $$8;
            }
         }
      }
   }

   private static int spawnEntity(CommandSourceStack $$0, Reference<EntityType<?>> $$1, Vec3 $$2, CompoundTag $$3, boolean $$4) throws CommandSyntaxException {
      Entity $$5 = createEntity($$0, $$1, $$2, $$3, $$4);
      $$0.sendSuccess(() -> Component.translatable("commands.summon.success", new Object[]{$$5.getDisplayName()}), true);
      return 1;
   }
}
