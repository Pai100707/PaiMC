package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public class EffectCommands {
   private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.effect.give.failed"));
   private static final SimpleCommandExceptionType ERROR_CLEAR_EVERYTHING_FAILED = new SimpleCommandExceptionType(
      Component.translatable("commands.effect.clear.everything.failed")
   );
   private static final SimpleCommandExceptionType ERROR_CLEAR_SPECIFIC_FAILED = new SimpleCommandExceptionType(
      Component.translatable("commands.effect.clear.specific.failed")
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("effect")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .then(
                  ((LiteralArgumentBuilder)Commands.literal("clear")
                        .executes(
                           $$0x -> clearEffects(
                              (CommandSourceStack)$$0x.getSource(), ImmutableList.of(((CommandSourceStack)$$0x.getSource()).getEntityOrException())
                           )
                        ))
                     .then(
                        ((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.entities())
                              .executes($$0x -> clearEffects((CommandSourceStack)$$0x.getSource(), EntityArgument.getEntities($$0x, "targets"))))
                           .then(
                              Commands.argument("effect", ResourceArgument.resource($$1, Registries.MOB_EFFECT))
                                 .executes(
                                    $$0x -> clearEffect(
                                       (CommandSourceStack)$$0x.getSource(),
                                       EntityArgument.getEntities($$0x, "targets"),
                                       ResourceArgument.getMobEffect($$0x, "effect")
                                    )
                                 )
                           )
                     )
               ))
            .then(
               Commands.literal("give")
                  .then(
                     Commands.argument("targets", EntityArgument.entities())
                        .then(
                           ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                       "effect", ResourceArgument.resource($$1, Registries.MOB_EFFECT)
                                    )
                                    .executes(
                                       $$0x -> giveEffect(
                                          (CommandSourceStack)$$0x.getSource(),
                                          EntityArgument.getEntities($$0x, "targets"),
                                          ResourceArgument.getMobEffect($$0x, "effect"),
                                          null,
                                          0,
                                          true
                                       )
                                    ))
                                 .then(
                                    ((RequiredArgumentBuilder)Commands.argument("seconds", IntegerArgumentType.integer(1, 1000000))
                                          .executes(
                                             $$0x -> giveEffect(
                                                (CommandSourceStack)$$0x.getSource(),
                                                EntityArgument.getEntities($$0x, "targets"),
                                                ResourceArgument.getMobEffect($$0x, "effect"),
                                                IntegerArgumentType.getInteger($$0x, "seconds"),
                                                0,
                                                true
                                             )
                                          ))
                                       .then(
                                          ((RequiredArgumentBuilder)Commands.argument("amplifier", IntegerArgumentType.integer(0, 255))
                                                .executes(
                                                   $$0x -> giveEffect(
                                                      (CommandSourceStack)$$0x.getSource(),
                                                      EntityArgument.getEntities($$0x, "targets"),
                                                      ResourceArgument.getMobEffect($$0x, "effect"),
                                                      IntegerArgumentType.getInteger($$0x, "seconds"),
                                                      IntegerArgumentType.getInteger($$0x, "amplifier"),
                                                      true
                                                   )
                                                ))
                                             .then(
                                                Commands.argument("hideParticles", BoolArgumentType.bool())
                                                   .executes(
                                                      $$0x -> giveEffect(
                                                         (CommandSourceStack)$$0x.getSource(),
                                                         EntityArgument.getEntities($$0x, "targets"),
                                                         ResourceArgument.getMobEffect($$0x, "effect"),
                                                         IntegerArgumentType.getInteger($$0x, "seconds"),
                                                         IntegerArgumentType.getInteger($$0x, "amplifier"),
                                                         !BoolArgumentType.getBool($$0x, "hideParticles")
                                                      )
                                                   )
                                             )
                                       )
                                 ))
                              .then(
                                 ((LiteralArgumentBuilder)Commands.literal("infinite")
                                       .executes(
                                          $$0x -> giveEffect(
                                             (CommandSourceStack)$$0x.getSource(),
                                             EntityArgument.getEntities($$0x, "targets"),
                                             ResourceArgument.getMobEffect($$0x, "effect"),
                                             -1,
                                             0,
                                             true
                                          )
                                       ))
                                    .then(
                                       ((RequiredArgumentBuilder)Commands.argument("amplifier", IntegerArgumentType.integer(0, 255))
                                             .executes(
                                                $$0x -> giveEffect(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   EntityArgument.getEntities($$0x, "targets"),
                                                   ResourceArgument.getMobEffect($$0x, "effect"),
                                                   -1,
                                                   IntegerArgumentType.getInteger($$0x, "amplifier"),
                                                   true
                                                )
                                             ))
                                          .then(
                                             Commands.argument("hideParticles", BoolArgumentType.bool())
                                                .executes(
                                                   $$0x -> giveEffect(
                                                      (CommandSourceStack)$$0x.getSource(),
                                                      EntityArgument.getEntities($$0x, "targets"),
                                                      ResourceArgument.getMobEffect($$0x, "effect"),
                                                      -1,
                                                      IntegerArgumentType.getInteger($$0x, "amplifier"),
                                                      !BoolArgumentType.getBool($$0x, "hideParticles")
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

   private static int giveEffect(CommandSourceStack $$0, Collection<? extends Entity> $$1, Holder<MobEffect> $$2, @Nullable Integer $$3, int $$4, boolean $$5) throws CommandSyntaxException {
      MobEffect $$6 = (MobEffect)$$2.value();
      int $$7 = 0;
      int $$8;
      if ($$3 != null) {
         if ($$6.isInstantenous()) {
            $$8 = $$3;
         } else if ($$3 == -1) {
            $$8 = -1;
         } else {
            $$8 = $$3 * 20;
         }
      } else if ($$6.isInstantenous()) {
         $$8 = 1;
      } else {
         $$8 = 600;
      }

      for (Entity $$13 : $$1) {
         if ($$13 instanceof LivingEntity) {
            MobEffectInstance $$14 = new MobEffectInstance($$2, $$8, $$4, false, $$5);
            if (((LivingEntity)$$13).addEffect($$14, $$0.getEntity())) {
               $$7++;
            }
         }
      }

      if ($$7 == 0) {
         throw ERROR_GIVE_FAILED.create();
      } else {
         if ($$1.size() == 1) {
            $$0.sendSuccess(
               () -> Component.translatable(
                  "commands.effect.give.success.single", new Object[]{$$6.getDisplayName(), $$1.iterator().next().getDisplayName(), $$8 / 20}
               ),
               true
            );
         } else {
            $$0.sendSuccess(
               () -> Component.translatable("commands.effect.give.success.multiple", new Object[]{$$6.getDisplayName(), $$1.size(), $$8 / 20}), true
            );
         }

         return $$7;
      }
   }

   private static int clearEffects(CommandSourceStack $$0, Collection<? extends Entity> $$1) throws CommandSyntaxException {
      int $$2 = 0;

      for (Entity $$3 : $$1) {
         if ($$3 instanceof LivingEntity && ((LivingEntity)$$3).removeAllEffects()) {
            $$2++;
         }
      }

      if ($$2 == 0) {
         throw ERROR_CLEAR_EVERYTHING_FAILED.create();
      } else {
         if ($$1.size() == 1) {
            $$0.sendSuccess(
               () -> Component.translatable("commands.effect.clear.everything.success.single", new Object[]{$$1.iterator().next().getDisplayName()}), true
            );
         } else {
            $$0.sendSuccess(() -> Component.translatable("commands.effect.clear.everything.success.multiple", new Object[]{$$1.size()}), true);
         }

         return $$2;
      }
   }

   private static int clearEffect(CommandSourceStack $$0, Collection<? extends Entity> $$1, Holder<MobEffect> $$2) throws CommandSyntaxException {
      MobEffect $$3 = (MobEffect)$$2.value();
      int $$4 = 0;

      for (Entity $$5 : $$1) {
         if ($$5 instanceof LivingEntity && ((LivingEntity)$$5).removeEffect($$2)) {
            $$4++;
         }
      }

      if ($$4 == 0) {
         throw ERROR_CLEAR_SPECIFIC_FAILED.create();
      } else {
         if ($$1.size() == 1) {
            $$0.sendSuccess(
               () -> Component.translatable(
                  "commands.effect.clear.specific.success.single", new Object[]{$$3.getDisplayName(), $$1.iterator().next().getDisplayName()}
               ),
               true
            );
         } else {
            $$0.sendSuccess(
               () -> Component.translatable("commands.effect.clear.specific.success.multiple", new Object[]{$$3.getDisplayName(), $$1.size()}), true
            );
         }

         return $$4;
      }
   }
}
