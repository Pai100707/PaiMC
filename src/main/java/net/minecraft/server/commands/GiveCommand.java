package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class GiveCommand {
   public static final int MAX_ALLOWED_ITEMSTACKS = 100;

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("give").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
            .then(
               Commands.argument("targets", EntityArgument.players())
                  .then(
                     ((RequiredArgumentBuilder)Commands.argument("item", ItemArgument.item($$1))
                           .executes(
                              $$0x -> giveItem(
                                 (CommandSourceStack)$$0x.getSource(), ItemArgument.getItem($$0x, "item"), EntityArgument.getPlayers($$0x, "targets"), 1
                              )
                           ))
                        .then(
                           Commands.argument("count", IntegerArgumentType.integer(1))
                              .executes(
                                 $$0x -> giveItem(
                                    (CommandSourceStack)$$0x.getSource(),
                                    ItemArgument.getItem($$0x, "item"),
                                    EntityArgument.getPlayers($$0x, "targets"),
                                    IntegerArgumentType.getInteger($$0x, "count")
                                 )
                              )
                        )
                  )
            )
      );
   }

   private static int giveItem(CommandSourceStack $$0, ItemInput $$1, Collection<ServerPlayer> $$2, int $$3) throws CommandSyntaxException {
      ItemStack $$4 = $$1.createItemStack(1, false);
      int $$5 = $$4.getMaxStackSize();
      int $$6 = $$5 * 100;
      if ($$3 > $$6) {
         $$0.sendFailure(Component.translatable("commands.give.failed.toomanyitems", new Object[]{$$6, $$4.getDisplayName()}));
         return 0;
      } else {
         for (ServerPlayer $$7 : $$2) {
            int $$8 = $$3;

            while ($$8 > 0) {
               int $$9 = Math.min($$5, $$8);
               $$8 -= $$9;
               ItemStack $$10 = $$1.createItemStack($$9, false);
               boolean $$11 = $$7.getInventory().add($$10);
               if ($$11 && $$10.isEmpty()) {
                  ItemEntity $$13 = $$7.drop($$4, false);
                  if ($$13 != null) {
                     $$13.makeFakeItem();
                  }

                  $$7.level()
                     .playSound(
                        null,
                        $$7.getX(),
                        $$7.getY(),
                        $$7.getZ(),
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.PLAYERS,
                        0.2F,
                        (($$7.getRandom().nextFloat() - $$7.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
                     );
                  $$7.containerMenu.broadcastChanges();
               } else {
                  ItemEntity $$12 = $$7.drop($$10, false);
                  if ($$12 != null) {
                     $$12.setNoPickUpDelay();
                     $$12.setTarget($$7.getUUID());
                  }
               }
            }
         }

         if ($$2.size() == 1) {
            $$0.sendSuccess(
               () -> Component.translatable("commands.give.success.single", new Object[]{$$3, $$4.getDisplayName(), $$2.iterator().next().getDisplayName()}),
               true
            );
         } else {
            $$0.sendSuccess(() -> Component.translatable("commands.give.success.single", new Object[]{$$3, $$4.getDisplayName(), $$2.size()}), true);
         }

         return $$2.size();
      }
   }
}
