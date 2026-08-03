package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PlaySoundCommand {
   private static final SimpleCommandExceptionType ERROR_TOO_FAR = new SimpleCommandExceptionType(Component.translatable("commands.playsound.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> $$0) {
      RequiredArgumentBuilder<CommandSourceStack, Identifier> $$1 = (RequiredArgumentBuilder<CommandSourceStack, Identifier>)Commands.argument(
            "sound", IdentifierArgument.id()
         )
         .suggests(SuggestionProviders.cast(SuggestionProviders.AVAILABLE_SOUNDS))
         .executes(
            $$0x -> playSound(
               (CommandSourceStack)$$0x.getSource(),
               getCallingPlayerAsCollection(((CommandSourceStack)$$0x.getSource()).getPlayer()),
               IdentifierArgument.getId($$0x, "sound"),
               SoundSource.MASTER,
               ((CommandSourceStack)$$0x.getSource()).getPosition(),
               1.0F,
               1.0F,
               0.0F
            )
         );

      for (SoundSource $$2 : SoundSource.values()) {
         $$1.then(source($$2));
      }

      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("playsound").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then($$1)
      );
   }

   private static LiteralArgumentBuilder<CommandSourceStack> source(SoundSource $$0) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal($$0.getName())
            .executes(
               $$1 -> playSound(
                  (CommandSourceStack)$$1.getSource(),
                  getCallingPlayerAsCollection(((CommandSourceStack)$$1.getSource()).getPlayer()),
                  IdentifierArgument.getId($$1, "sound"),
                  $$0,
                  ((CommandSourceStack)$$1.getSource()).getPosition(),
                  1.0F,
                  1.0F,
                  0.0F
               )
            ))
         .then(
            ((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players())
                  .executes(
                     $$1 -> playSound(
                        (CommandSourceStack)$$1.getSource(),
                        EntityArgument.getPlayers($$1, "targets"),
                        IdentifierArgument.getId($$1, "sound"),
                        $$0,
                        ((CommandSourceStack)$$1.getSource()).getPosition(),
                        1.0F,
                        1.0F,
                        0.0F
                     )
                  ))
               .then(
                  ((RequiredArgumentBuilder)Commands.argument("pos", Vec3Argument.vec3())
                        .executes(
                           $$1 -> playSound(
                              (CommandSourceStack)$$1.getSource(),
                              EntityArgument.getPlayers($$1, "targets"),
                              IdentifierArgument.getId($$1, "sound"),
                              $$0,
                              Vec3Argument.getVec3($$1, "pos"),
                              1.0F,
                              1.0F,
                              0.0F
                           )
                        ))
                     .then(
                        ((RequiredArgumentBuilder)Commands.argument("volume", FloatArgumentType.floatArg(0.0F))
                              .executes(
                                 $$1 -> playSound(
                                    (CommandSourceStack)$$1.getSource(),
                                    EntityArgument.getPlayers($$1, "targets"),
                                    IdentifierArgument.getId($$1, "sound"),
                                    $$0,
                                    Vec3Argument.getVec3($$1, "pos"),
                                    (Float)$$1.getArgument("volume", Float.class),
                                    1.0F,
                                    0.0F
                                 )
                              ))
                           .then(
                              ((RequiredArgumentBuilder)Commands.argument("pitch", FloatArgumentType.floatArg(0.0F, 2.0F))
                                    .executes(
                                       $$1 -> playSound(
                                          (CommandSourceStack)$$1.getSource(),
                                          EntityArgument.getPlayers($$1, "targets"),
                                          IdentifierArgument.getId($$1, "sound"),
                                          $$0,
                                          Vec3Argument.getVec3($$1, "pos"),
                                          (Float)$$1.getArgument("volume", Float.class),
                                          (Float)$$1.getArgument("pitch", Float.class),
                                          0.0F
                                       )
                                    ))
                                 .then(
                                    Commands.argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F))
                                       .executes(
                                          $$1 -> playSound(
                                             (CommandSourceStack)$$1.getSource(),
                                             EntityArgument.getPlayers($$1, "targets"),
                                             IdentifierArgument.getId($$1, "sound"),
                                             $$0,
                                             Vec3Argument.getVec3($$1, "pos"),
                                             (Float)$$1.getArgument("volume", Float.class),
                                             (Float)$$1.getArgument("pitch", Float.class),
                                             (Float)$$1.getArgument("minVolume", Float.class)
                                          )
                                       )
                                 )
                           )
                     )
               )
         );
   }

   private static Collection<ServerPlayer> getCallingPlayerAsCollection(@Nullable ServerPlayer $$0) {
      return $$0 != null ? List.of($$0) : List.of();
   }

   private static int playSound(
      CommandSourceStack $$0, Collection<ServerPlayer> $$1, Identifier $$2, SoundSource $$3, Vec3 $$4, float $$5, float $$6, float $$7
   ) throws CommandSyntaxException {
      Holder<SoundEvent> $$8 = Holder.direct(SoundEvent.createVariableRangeEvent($$2));
      double $$9 = Mth.square(((SoundEvent)$$8.value()).getRange($$5));
      ServerLevel $$10 = $$0.getLevel();
      long $$11 = $$10.getRandom().nextLong();
      List<ServerPlayer> $$12 = new ArrayList<>();

      for (ServerPlayer $$13 : $$1) {
         if ($$13.level() == $$10) {
            double $$14 = $$4.x - $$13.getX();
            double $$15 = $$4.y - $$13.getY();
            double $$16 = $$4.z - $$13.getZ();
            double $$17 = $$14 * $$14 + $$15 * $$15 + $$16 * $$16;
            Vec3 $$18 = $$4;
            float $$19 = $$5;
            if ($$17 > $$9) {
               if ($$7 <= 0.0F) {
                  continue;
               }

               double $$20 = Math.sqrt($$17);
               $$18 = new Vec3($$13.getX() + $$14 / $$20 * 2.0, $$13.getY() + $$15 / $$20 * 2.0, $$13.getZ() + $$16 / $$20 * 2.0);
               $$19 = $$7;
            }

            $$13.connection.send(new ClientboundSoundPacket($$8, $$3, $$18.x(), $$18.y(), $$18.z(), $$19, $$6, $$11));
            $$12.add($$13);
         }
      }

      int $$21 = $$12.size();
      if ($$21 == 0) {
         throw ERROR_TOO_FAR.create();
      } else {
         if ($$21 == 1) {
            $$0.sendSuccess(
               () -> Component.translatable("commands.playsound.success.single", new Object[]{Component.translationArg($$2), $$12.getFirst().getDisplayName()}),
               true
            );
         } else {
            $$0.sendSuccess(() -> Component.translatable("commands.playsound.success.multiple", new Object[]{Component.translationArg($$2), $$21}), true);
         }

         return $$21;
      }
   }
}
