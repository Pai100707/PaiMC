package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ClearInventoryCommands {
   private static final DynamicCommandExceptionType ERROR_SINGLE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("clear.failed.single", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_MULTIPLE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("clear.failed.multiple", new Object[]{$$0})
   );

   public static void register(CommandDispatcher<CommandSourceStack> $$0, CommandBuildContext $$1) {
      $$0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("clear")
                  .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)))
               .executes(
                  $$0x -> clearUnlimited(
                     (CommandSourceStack)$$0x.getSource(), Collections.singleton(((CommandSourceStack)$$0x.getSource()).getPlayerOrException()), $$0xx -> true
                  )
               ))
            .then(
               ((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players())
                     .executes($$0x -> clearUnlimited((CommandSourceStack)$$0x.getSource(), EntityArgument.getPlayers($$0x, "targets"), $$0xx -> true)))
                  .then(
                     ((RequiredArgumentBuilder)Commands.argument("item", ItemPredicateArgument.itemPredicate($$1))
                           .executes(
                              $$0x -> clearUnlimited(
                                 (CommandSourceStack)$$0x.getSource(),
                                 EntityArgument.getPlayers($$0x, "targets"),
                                 ItemPredicateArgument.getItemPredicate($$0x, "item")
                              )
                           ))
                        .then(
                           Commands.argument("maxCount", IntegerArgumentType.integer(0))
                              .executes(
                                 $$0x -> clearInventory(
                                    (CommandSourceStack)$$0x.getSource(),
                                    EntityArgument.getPlayers($$0x, "targets"),
                                    ItemPredicateArgument.getItemPredicate($$0x, "item"),
                                    IntegerArgumentType.getInteger($$0x, "maxCount")
                                 )
                              )
                        )
                  )
            )
      );
   }

   private static int clearUnlimited(CommandSourceStack $$0, Collection<ServerPlayer> $$1, Predicate<ItemStack> $$2) throws CommandSyntaxException {
      return clearInventory($$0, $$1, $$2, -1);
   }

   private static int clearInventory(CommandSourceStack $$0, Collection<ServerPlayer> $$1, Predicate<ItemStack> $$2, int $$3) throws CommandSyntaxException {
      int $$4 = 0;

      for (ServerPlayer $$5 : $$1) {
         $$4 += $$5.getInventory().clearOrCountMatchingItems($$2, $$3, $$5.inventoryMenu.getCraftSlots());
         $$5.containerMenu.broadcastChanges();
         $$5.inventoryMenu.slotsChanged($$5.getInventory());
      }

      if ($$4 == 0) {
         if ($$1.size() == 1) {
            throw ERROR_SINGLE.create($$1.iterator().next().getName());
         } else {
            throw ERROR_MULTIPLE.create($$1.size());
         }
      } else {
         int $$6 = $$4;
         if ($$3 == 0) {
            if ($$1.size() == 1) {
               $$0.sendSuccess(() -> Component.translatable("commands.clear.test.single", new Object[]{$$6, $$1.iterator().next().getDisplayName()}), true);
            } else {
               $$0.sendSuccess(() -> Component.translatable("commands.clear.test.multiple", new Object[]{$$6, $$1.size()}), true);
            }
         } else if ($$1.size() == 1) {
            $$0.sendSuccess(() -> Component.translatable("commands.clear.success.single", new Object[]{$$6, $$1.iterator().next().getDisplayName()}), true);
         } else {
            $$0.sendSuccess(() -> Component.translatable("commands.clear.success.multiple", new Object[]{$$6, $$1.size()}), true);
         }

         return $$4;
      }
   }
}
