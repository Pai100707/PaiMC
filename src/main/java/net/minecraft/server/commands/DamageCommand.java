package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class DamageCommand {
   private static final SimpleCommandExceptionType ERROR_INVULNERABLE = new SimpleCommandExceptionType(Component.translatable("commands.damage.invulnerable"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("damage").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               Commands.argument("target", EntityArgument.entity())
                  .then(
                     ((RequiredArgumentBuilder)Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                           .executes(
                              $$0x -> damage(
                                 (CommandSourceStack)$$0x.getSource(),
                                 EntityArgument.getEntity($$0x, "target"),
                                 FloatArgumentType.getFloat($$0x, "amount"),
                                 ((CommandSourceStack)$$0x.getSource()).getLevel().damageSources().generic()
                              )
                           ))
                        .then(
                           ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                       "damageType", ResourceArgument.resource($$1, Registries.DAMAGE_TYPE)
                                    )
                                    .executes(
                                       $$0x -> damage(
                                          (CommandSourceStack)$$0x.getSource(),
                                          EntityArgument.getEntity($$0x, "target"),
                                          FloatArgumentType.getFloat($$0x, "amount"),
                                          new DamageSource(ResourceArgument.getResource($$0x, "damageType", Registries.DAMAGE_TYPE))
                                       )
                                    ))
                                 .then(
                                    Commands.literal("at")
                                       .then(
                                          Commands.argument("location", Vec3Argument.vec3())
                                             .executes(
                                                $$0x -> damage(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   EntityArgument.getEntity($$0x, "target"),
                                                   FloatArgumentType.getFloat($$0x, "amount"),
                                                   new DamageSource(
                                                      ResourceArgument.getResource($$0x, "damageType", Registries.DAMAGE_TYPE),
                                                      Vec3Argument.getVec3($$0x, "location")
                                                   )
                                                )
                                             )
                                       )
                                 ))
                              .then(
                                 Commands.literal("by")
                                    .then(
                                       ((RequiredArgumentBuilder)Commands.argument("entity", EntityArgument.entity())
                                             .executes(
                                                $$0x -> damage(
                                                   (CommandSourceStack)$$0x.getSource(),
                                                   EntityArgument.getEntity($$0x, "target"),
                                                   FloatArgumentType.getFloat($$0x, "amount"),
                                                   new DamageSource(
                                                      ResourceArgument.getResource($$0x, "damageType", Registries.DAMAGE_TYPE),
                                                      EntityArgument.getEntity($$0x, "entity")
                                                   )
                                                )
                                             ))
                                          .then(
                                             Commands.literal("from")
                                                .then(
                                                   Commands.argument("cause", EntityArgument.entity())
                                                      .executes(
                                                         $$0x -> damage(
                                                            (CommandSourceStack)$$0x.getSource(),
                                                            EntityArgument.getEntity($$0x, "target"),
                                                            FloatArgumentType.getFloat($$0x, "amount"),
                                                            new DamageSource(
                                                               ResourceArgument.getResource($$0x, "damageType", Registries.DAMAGE_TYPE),
                                                               EntityArgument.getEntity($$0x, "entity"),
                                                               EntityArgument.getEntity($$0x, "cause")
                                                            )
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

   private static int damage(CommandSourceStack $$0, Entity $$1, float $$2, DamageSource $$3) throws CommandSyntaxException {
      if ($$1.hurtServer($$0.getLevel(), $$3, $$2)) {
         $$0.sendSuccess(() -> Component.translatable("commands.damage.success", new Object[]{$$2, $$1.getDisplayName()}), true);
         return 1;
      } else {
         throw ERROR_INVULNERABLE.create();
      }
   }
}
